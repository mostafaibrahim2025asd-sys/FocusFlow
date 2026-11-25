package com.example.focusflow

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.Gravity
import android.widget.GridLayout
import android.widget.TextView

class CardMatchActivity : BaseGameActivity() {

    override val gameNumber = 1
    override val gameName = "Card Match"
    override val gameWeight = SessionManager.WEIGHT_CARD
    override val maxRawScore = TOTAL_PAIRS
    override val nextScreen: Class<*> = ColorRecallActivity::class.java

    companion object {
        private const val TOTAL_CARDS = 12
        private const val TOTAL_PAIRS = 6
        private const val MISMATCH_DELAY_MS = 800L
    }

    private val emojis = listOf("🦊", "🍉", "🚀", "🎈", "🐢", "🌙")
    private lateinit var cardEmojis: List<String>
    private val cardViews = mutableListOf<TextView>()
    private val matched = BooleanArray(TOTAL_CARDS)

    private var firstIndex = -1
    private var busy = false
    private var matchedPairs = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_card_match)
        setupHeader()

        // كل مرة بيتعمل خلط جديد للكروت
        cardEmojis = (emojis + emojis).shuffled()
        buildGrid()
    }

    private fun closedBg() = Ui.rounded(this, color(R.color.card_closed), 22f)
    private fun openBg() = Ui.rounded(this, color(R.color.surface), 22f)
    private fun matchedBg() =
        Ui.rounded(this, color(R.color.surface), 22f, color(R.color.match_green), 4f)
    private fun mismatchedBg() =
        Ui.rounded(this, color(R.color.surface), 22f, color(R.color.mismatch_red), 4f)

    private fun buildGrid() {
        val grid = findViewById<GridLayout>(R.id.gridCards)
        val margin = Ui.dp(this, 6f)
        val cardHeight = Ui.dp(this, 112f)

        for (i in 0 until TOTAL_CARDS) {
            val card = TextView(this)
            card.gravity = Gravity.CENTER
            card.textSize = 40f
            card.elevation = Ui.dp(this, 3f).toFloat()
            card.isClickable = true
            card.isFocusable = true
            card.contentDescription = "Closed card"
            card.background = closedBg()
            card.setOnClickListener { onCardClicked(i) }

            val params = GridLayout.LayoutParams(
                GridLayout.spec(i / 3),
                GridLayout.spec(i % 3, 1f)
            )
            params.width = 0
            params.height = cardHeight
            params.setMargins(margin, margin, margin, margin)

            grid.addView(card, params)
            cardViews.add(card)
        }
    }

    private fun onCardClicked(index: Int) {
        if (finished || busy || matched[index] || index == firstIndex) return
        startTimerOnce()

        showCard(index, openBg(), "")

        if (firstIndex == -1) {
            firstIndex = index
            return
        }

        val first = firstIndex
        firstIndex = -1

        if (cardEmojis[first] == cardEmojis[index]) {
            // تطابق: +1 وإطار أخضر
            matched[first] = true
            matched[index] = true
            showCard(first, matchedBg(), "matched")
            showCard(index, matchedBg(), "matched")
            addScore(1)
            matchedPairs++

            if (matchedPairs == TOTAL_PAIRS) {
                finishGame(800L)
            }
        } else {
            // عدم تطابق: -1 وإطار أحمر ثم يتقفلوا
            busy = true
            showCard(first, mismatchedBg(), "mismatched")
            showCard(index, mismatchedBg(), "mismatched")
            addScore(-1)

            handler.postDelayed({
                hideCard(first)
                hideCard(index)
                busy = false
            }, MISMATCH_DELAY_MS)
        }
    }

    private fun showCard(index: Int, bg: Drawable, state: String) {
        val card = cardViews[index]
        card.text = cardEmojis[index]
        card.background = bg
        card.contentDescription = (cardEmojis[index] + " " + state).trim()
    }

    private fun hideCard(index: Int) {
        val card = cardViews[index]
        card.text = ""
        card.background = closedBg()
        card.contentDescription = "Closed card"
    }
}