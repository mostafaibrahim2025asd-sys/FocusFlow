package com.example.focusflow

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.TextView
import kotlin.math.min
import kotlin.random.Random

class PatternEchoActivity : BaseGameActivity() {

    override val gameNumber = 3
    override val gameName = "Pattern Echo"
    override val gameWeight = SessionManager.WEIGHT_PATTERN
    override val maxRawScore = PATTERN_LENGTHS.sum()
    override val nextScreen: Class<*> = ResultActivity::class.java

    companion object {
        // طول النمط في كل جولة (6 جولات = 24 ضغطة كحد أقصى)
        private val PATTERN_LENGTHS = intArrayOf(3, 3, 4, 4, 5, 5)
        private const val FLASH_MS = 600L      // مدة إضاءة المربع
        private const val STEP_MS = 900L       // الفاصل بين مربع والتاني
        private const val LEAD_IN_MS = 700L    // انتظار قبل أول مربع
    }

    private enum class Phase { READY, WATCH, INPUT, BETWEEN }

    private val colorNames = listOf("Red", "Blue", "Green", "Yellow")
    private val fullColors by lazy {
        listOf(R.color.c_red, R.color.c_blue, R.color.c_green, R.color.c_yellow)
            .map { color(it) }
    }
    private val dimColors by lazy {
        listOf(R.color.dim_red, R.color.dim_blue, R.color.dim_green, R.color.dim_yellow)
            .map { color(it) }
    }

    private val squares = mutableListOf<View>()
    private var pattern: List<Int> = emptyList()
    private var inputIndex = 0
    private var roundIndex = 0
    private var phase = Phase.READY

    private lateinit var tvInstruction: TextView
    private lateinit var tvStatus: TextView
    private lateinit var tvRoundInfo: TextView
    private lateinit var dotsContainer: LinearLayout
    private lateinit var btnStart: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pattern_echo)
        setupHeader()

        tvInstruction = findViewById(R.id.tvInstruction)
        tvStatus = findViewById(R.id.tvStatus)
        tvRoundInfo = findViewById(R.id.tvRoundInfo)
        dotsContainer = findViewById(R.id.dotsContainer)
        btnStart = findViewById(R.id.btnStart)

        tvInstruction.text =
            "Tap Start. The squares light up in order. Then repeat the same order."

        buildSquares()
        showResting()
        showRoundInfo()
        buildDots(PATTERN_LENGTHS[0])
        setStatus("Ready", false)

        btnStart.setOnClickListener { onStartClicked() }
    }

    private fun buildSquares() {
        val grid = findViewById<GridLayout>(R.id.gridSquares)
        val margin = Ui.dp(this, 8f)
        val screenWidth = resources.displayMetrics.widthPixels
        val size = (screenWidth - Ui.dp(this, 40f) - margin * 4) / 2

        for (i in 0 until 4) {
            val square = View(this)
            square.isClickable = true
            square.isFocusable = true
            square.contentDescription = colorNames[i] + " square"
            square.setOnClickListener { onSquareTapped(i, it) }

            val params = GridLayout.LayoutParams(
                GridLayout.spec(i / 2),
                GridLayout.spec(i % 2)
            )
            params.width = size
            params.height = size
            params.setMargins(margin, margin, margin, margin)

            grid.addView(square, params)
            squares.add(square)
        }
    }

    private fun setSquare(i: Int, colorInt: Int, scale: Float) {
        squares[i].background = Ui.rounded(this, colorInt, 28f)
        squares[i].scaleX = scale
        squares[i].scaleY = scale
    }

    private fun showResting() {
        for (i in 0 until 4) setSquare(i, dimColors[i], 1f)
    }

    private fun showActive() {
        for (i in 0 until 4) setSquare(i, fullColors[i], 1f)
    }

    private fun onStartClicked() {
        if (finished || phase != Phase.READY) return
        startTimerOnce()
        btnStart.visibility = View.INVISIBLE
        beginWatch()
    }

    // مرحلة المشاهدة: المربعات تنوّر بالترتيب
    private fun beginWatch() {
        phase = Phase.WATCH
        val length = PATTERN_LENGTHS[roundIndex]
        pattern = List(length) { Random.nextInt(4) }
        inputIndex = 0

        showRoundInfo()
        buildDots(length)
        setStatus("Watch carefully", false)
        tvInstruction.text = "Watch carefully. Do not tap yet."
        showResting()

        pattern.forEachIndexed { k, squareIndex ->
            val on = LEAD_IN_MS + k * STEP_MS
            handler.postDelayed({
                if (!finished) {
                    setSquare(squareIndex, fullColors[squareIndex], 1.06f)
                    updateDots(k + 1)
                }
            }, on)
            handler.postDelayed({
                if (!finished) setSquare(squareIndex, dimColors[squareIndex], 1f)
            }, on + FLASH_MS)
        }

        handler.postDelayed({
            if (!finished) beginInput()
        }, LEAD_IN_MS + length * STEP_MS)
    }

    // مرحلة اللاعب: لازم يكرر نفس الترتيب
    private fun beginInput() {
        phase = Phase.INPUT
        inputIndex = 0
        updateDots(0)
        setStatus("Your turn", true)
        tvInstruction.text = "Repeat the same order. Correct tap +1, wrong tap −1."
        showActive()
    }

    private fun onSquareTapped(index: Int, view: View) {
        if (finished || phase != Phase.INPUT) return

        view.animate().scaleX(0.94f).scaleY(0.94f).setDuration(80).withEndAction {
            view.animate().scaleX(1f).scaleY(1f).setDuration(80).start()
        }.start()

        if (pattern[inputIndex] == index) {
            addScore(1)
            inputIndex++
            updateDots(inputIndex)
            if (inputIndex == pattern.size) endRound(true)
        } else {
            addScore(-1)
            endRound(false)
        }
    }

    private fun endRound(success: Boolean) {
        phase = Phase.BETWEEN
        setStatus(if (success) "Great!" else "Missed it", false)
        roundIndex++

        handler.postDelayed({
            if (!finished) {
                showResting()
                if (roundIndex >= PATTERN_LENGTHS.size) finishGame(0L) else beginWatch()
            }
        }, 900L)
    }

    private fun showRoundInfo() {
        val i = min(roundIndex, PATTERN_LENGTHS.size - 1)
        tvRoundInfo.text =
            "Round ${i + 1} of ${PATTERN_LENGTHS.size} · Pattern of ${PATTERN_LENGTHS[i]}"
    }

    private fun setStatus(text: String, strong: Boolean) {
        tvStatus.text = text
        if (strong) {
            tvStatus.background = Ui.rounded(this, color(R.color.primary), 18f)
            tvStatus.setTextColor(color(R.color.white))
        } else {
            tvStatus.background = Ui.rounded(this, color(R.color.track), 18f)
            tvStatus.setTextColor(color(R.color.primary))
        }
    }

    private fun buildDots(count: Int) {
        dotsContainer.removeAllViews()
        repeat(count) {
            val dot = View(this)
            val params = LinearLayout.LayoutParams(Ui.dp(this, 12f), Ui.dp(this, 12f))
            params.marginStart = Ui.dp(this, 8f)
            dot.layoutParams = params
            dot.background = Ui.oval(color(R.color.step_off))
            dotsContainer.addView(dot)
        }
    }

    private fun updateDots(filled: Int) {
        for (i in 0 until dotsContainer.childCount) {
            val c = if (i < filled) R.color.accent else R.color.step_off
            dotsContainer.getChildAt(i).background = Ui.oval(color(c))
        }
    }
}