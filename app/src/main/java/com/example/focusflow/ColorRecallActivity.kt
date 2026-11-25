package com.example.focusflow

import android.animation.ObjectAnimator
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import kotlin.random.Random

class ColorRecallActivity : BaseGameActivity() {

    override val gameNumber = 2
    override val gameName = "Color Recall"
    override val gameWeight = SessionManager.WEIGHT_COLOR
    override val maxRawScore = TOTAL_ROUNDS
    override val nextScreen: Class<*> = PatternEchoActivity::class.java

    companion object {
        private const val TOTAL_ROUNDS = 10
        private const val REVEAL_MS = 2000L     // الكلمة بتظهر ثانيتين بس
        private const val FEEDBACK_MS = 700L
    }

    private enum class Phase { WAITING, SHOWING, QUESTION, FEEDBACK }
    private enum class QuestionType { INK, WORD }

    private val names = listOf("Red", "Blue", "Green", "Yellow")
    private val inkColors by lazy {
        listOf(R.color.ink_red, R.color.ink_blue, R.color.ink_green, R.color.ink_yellow)
            .map { color(it) }
    }

    private var phase = Phase.WAITING
    private var round = 0
    private var wordIndex = 0
    private var inkIndex = 0
    private var questionType = QuestionType.INK
    private var correctIndex = 0
    private var revealAnimator: ObjectAnimator? = null

    private lateinit var stimulusGroup: LinearLayout
    private lateinit var questionGroup: LinearLayout
    private lateinit var cardStimulus: FrameLayout
    private lateinit var tvCardHint: TextView
    private lateinit var tvMemorize: TextView
    private lateinit var tvWord: TextView
    private lateinit var tvRound: TextView
    private lateinit var tvQuestionTag: TextView
    private lateinit var tvQuestion: TextView
    private lateinit var progressReveal: ProgressBar
    private lateinit var answerButtons: List<Button>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_color_recall)
        setupHeader()

        stimulusGroup = findViewById(R.id.stimulusGroup)
        questionGroup = findViewById(R.id.questionGroup)
        cardStimulus = findViewById(R.id.cardStimulus)
        tvCardHint = findViewById(R.id.tvCardHint)
        tvMemorize = findViewById(R.id.tvMemorize)
        tvWord = findViewById(R.id.tvWord)
        tvRound = findViewById(R.id.tvRound)
        tvQuestionTag = findViewById(R.id.tvQuestionTag)
        tvQuestion = findViewById(R.id.tvQuestion)
        progressReveal = findViewById(R.id.progressReveal)
        answerButtons = listOf(
            findViewById(R.id.btnAns0),
            findViewById(R.id.btnAns1),
            findViewById(R.id.btnAns2),
            findViewById(R.id.btnAns3)
        )

        findViewById<TextView>(R.id.tvInstruction).text =
            "Tap the card to flip it. The word shows for just 2 seconds, so remember it."

        cardStimulus.setOnClickListener { onCardTapped() }
        answerButtons.forEachIndexed { i, button ->
            button.text = names[i]
            button.setOnClickListener { onAnswer(i) }
        }

        startRound()
    }

    private fun startRound() {
        round++
        phase = Phase.WAITING

        // كلمة ولون حبر مختلفين دايماً
        wordIndex = Random.nextInt(4)
        do {
            inkIndex = Random.nextInt(4)
        } while (inkIndex == wordIndex)

        // الأسئلة بتتبدّل: مرة عن لون الحبر ومرة عن الكلمة
        questionType = if (round % 2 == 1) QuestionType.INK else QuestionType.WORD

        questionGroup.visibility = View.GONE
        stimulusGroup.visibility = View.VISIBLE
        cardStimulus.background = Ui.rounded(this, color(R.color.card_closed), 32f)
        tvCardHint.visibility = View.VISIBLE
        tvMemorize.visibility = View.GONE
        tvWord.visibility = View.GONE
        progressReveal.visibility = View.GONE
        tvRound.text = "Round $round of $TOTAL_ROUNDS · Remember the word and its color"

        answerButtons.forEach {
            it.backgroundTintList = null
            it.isEnabled = true
        }
    }

    private fun onCardTapped() {
        if (finished || phase != Phase.WAITING) return
        startTimerOnce()
        phase = Phase.SHOWING

        cardStimulus.background = Ui.rounded(this, color(R.color.ink), 32f)
        tvCardHint.visibility = View.GONE
        tvMemorize.visibility = View.VISIBLE

        tvWord.text = names[wordIndex].uppercase()
        tvWord.setTextColor(inkColors[inkIndex])
        tvWord.visibility = View.VISIBLE

        progressReveal.progress = 100
        progressReveal.visibility = View.VISIBLE
        val animator = ObjectAnimator.ofInt(progressReveal, "progress", 100, 0)
        animator.duration = REVEAL_MS
        animator.interpolator = LinearInterpolator()
        animator.start()
        revealAnimator = animator

        // بعد ثانيتين الكلمة تختفي ويظهر السؤال
        handler.postDelayed({ showQuestion() }, REVEAL_MS)
    }

    private fun showQuestion() {
        if (finished) return
        phase = Phase.QUESTION

        stimulusGroup.visibility = View.GONE
        questionGroup.visibility = View.VISIBLE

        if (questionType == QuestionType.INK) {
            tvQuestionTag.text = "ROUND $round · INK COLOR"
            tvQuestion.text = "What color was the word written in?"
            correctIndex = inkIndex
        } else {
            tvQuestionTag.text = "ROUND $round · THE WORD"
            tvQuestion.text = "Which word was shown?"
            correctIndex = wordIndex
        }
    }

    private fun onAnswer(index: Int) {
        if (finished || phase != Phase.QUESTION) return
        phase = Phase.FEEDBACK

        val correct = index == correctIndex
        addScore(if (correct) 1 else -1)

        tint(answerButtons[index], if (correct) R.color.dim_green else R.color.dim_red)
        if (!correct) {
            tint(answerButtons[correctIndex], R.color.dim_green)
        }

        handler.postDelayed({
            if (!finished) {
                if (round >= TOTAL_ROUNDS) finishGame(0L) else startRound()
            }
        }, FEEDBACK_MS)
    }

    private fun tint(button: Button, colorRes: Int) {
        button.backgroundTintList = ColorStateList.valueOf(color(colorRes))
    }

    override fun onDestroy() {
        revealAnimator?.cancel()
        super.onDestroy()
    }
}