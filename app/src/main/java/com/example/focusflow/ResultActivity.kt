package com.example.focusflow

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class ResultActivity : AppCompatActivity() {

    private class Level(
        val label: String,
        val bg: String,
        val fg: String,
        val message: String
    )

    // التصنيف حسب مجموع الجلسة من 100
    private fun levelFor(total: Int): Level = when {
        total < 50 -> Level(
            "Needs Practice", "#FBE3E0", "#9B2C22",
            "Keep training. Another session will help."
        )
        total < 70 -> Level(
            "Average", "#FFF1CC", "#7A5600",
            "You are getting there. Keep practicing."
        )
        total < 90 -> Level(
            "Very Good", "#E3F5EA", "#1B6B3F",
            "Great focus today. Keep your daily rhythm."
        )
        else -> Level(
            "Peak Focus", "#CFEFDD", "#11582F",
            "Outstanding! You are at peak focus."
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)

        val points = SessionManager.points
        val total = SessionManager.total()

        findViewById<TextView>(R.id.tvTotal).text = total.toString()

        val level = levelFor(total)
        val tvLevel = findViewById<TextView>(R.id.tvLevel)
        tvLevel.text = level.label
        tvLevel.setTextColor(Color.parseColor(level.fg))
        tvLevel.background = Ui.rounded(this, Color.parseColor(level.bg), 20f)
        findViewById<TextView>(R.id.tvMessage).text = level.message

        bindRow(R.id.pbCard, R.id.tvCardPts, points[0], SessionManager.WEIGHT_CARD)
        bindRow(R.id.pbColor, R.id.tvColorPts, points[1], SessionManager.WEIGHT_COLOR)
        bindRow(R.id.pbPattern, R.id.tvPatternPts, points[2], SessionManager.WEIGHT_PATTERN)

        // Play Again: جلسة جديدة (كروت متخلطة وألوان وأنماط جديدة)
        findViewById<Button>(R.id.btnPlayAgain).setOnClickListener {
            SessionManager.reset()
            startActivity(Intent(this, CardMatchActivity::class.java))
            finish()
        }

        findViewById<Button>(R.id.btnHome).setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }
    }

    private fun bindRow(barId: Int, textId: Int, points: Int, max: Int) {
        val bar = findViewById<ProgressBar>(barId)
        bar.max = max
        bar.progress = points
        findViewById<TextView>(textId).text = "$points/$max"
    }
}