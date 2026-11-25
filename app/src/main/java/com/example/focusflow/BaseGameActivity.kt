package com.example.focusflow

import android.content.Intent
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

abstract class BaseGameActivity : AppCompatActivity() {

    companion object {
        const val GAME_SECONDS = 180
    }


    protected abstract val gameNumber: Int
    protected abstract val gameName: String
    protected abstract val gameWeight: Int
    protected abstract val maxRawScore: Int
    protected abstract val nextScreen: Class<*>

    protected val handler = Handler(Looper.getMainLooper())
    protected var rawScore = 0
    protected var finished = false

    private var timer: CountDownTimer? = null
    private lateinit var tvTimer: TextView
    private lateinit var tvScore: TextView

    protected fun setupHeader() {
        tvTimer = findViewById(R.id.tvTimer)
        tvScore = findViewById(R.id.tvScore)

        findViewById<TextView>(R.id.tvGameLabel).text = "GAME $gameNumber OF 3"
        findViewById<TextView>(R.id.tvGameName).text = gameName

        val steps = listOf(R.id.step1, R.id.step2, R.id.step3)
        for (i in steps.indices) {
            val colorRes = if (i < gameNumber) R.color.accent else R.color.step_off
            findViewById<View>(steps[i]).background =
                Ui.rounded(this, color(colorRes), 3f)
        }

        showTime(GAME_SECONDS)
        showScore()
    }

    protected fun color(res: Int): Int = ContextCompat.getColor(this, res)

    protected fun startTimerOnce() {
        if (timer != null || finished) return
        timer = object : CountDownTimer(GAME_SECONDS * 1000L, 1000L) {
            override fun onTick(millisUntilFinished: Long) {
                showTime(((millisUntilFinished + 999) / 1000).toInt())
            }

            override fun onFinish() {
                showTime(0)
                finishGame(0L)
            }
        }
        timer?.start()
    }

    protected fun addScore(delta: Int) {
        rawScore += delta
        showScore()
    }

    protected fun finishGame(delayMs: Long) {
        if (finished) return
        finished = true
        timer?.cancel()

        val ratio = max(0, rawScore).toDouble() / maxRawScore
        val points = (gameWeight * min(1.0, ratio)).roundToInt()
        SessionManager.points[gameNumber - 1] = points

        handler.postDelayed({
            startActivity(Intent(this, nextScreen))
            finish()
        }, delayMs)
    }

    private fun showTime(seconds: Int) {
        val m = seconds / 60
        val s = (seconds % 60).toString().padStart(2, '0')
        tvTimer.text = "Time: $m:$s"
    }

    private fun showScore() {
        tvScore.text = "Score: $rawScore"
    }

    override fun onDestroy() {
        super.onDestroy()
        timer?.cancel()
        handler.removeCallbacksAndMessages(null)
    }
}