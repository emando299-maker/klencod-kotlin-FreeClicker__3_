package com.example.freeclicker

import android.app.Activity
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.provider.Settings
import android.text.InputType
import android.view.Gravity
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.*

class MainActivity : Activity() {
    private val GOLD = 0xFFD4AF37.toInt()
    private val BG = 0xFF0B0B0B.toInt()
    private val CARD = 0xFF161616.toInt()
    private val MUTED = 0xFFB8B8B8.toInt()
    private val MAX_MS = 24L * 3600 * 1000
    private lateinit var root: LinearLayout
    private lateinit var status: TextView
    private lateinit var showSwitch: Switch

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()

    private fun card(title: String): LinearLayout {
        val c = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(16), dp(18), dp(18))
            background = GradientDrawable().apply { cornerRadius = dp(18).toFloat(); setColor(CARD); setStroke(dp(1), 0x66D4AF37) }
            layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT).apply { topMargin = dp(14) }
        }
        c.addView(TextView(this).apply {
            text = title.uppercase(); setTextColor(GOLD); textSize = 12f; letterSpacing = 0.15f
            typeface = Typeface.DEFAULT_BOLD; setPadding(0, 0, 0, dp(10))
        })
        root.addView(c); return c
    }

    private fun text(t: String, size: Float = 14f, color: Int = MUTED) =
        TextView(this).apply { text = t; textSize = size; setTextColor(color); setPadding(0, dp(4), 0, dp(4)) }

    private fun goldBtn(t: String, filled: Boolean = true, f: () -> Unit) = Button(this).apply {
        text = t; isAllCaps = false; typeface = Typeface.DEFAULT_BOLD; stateListAnimator = null
        setTextColor(if (filled) BG else GOLD)
        background = GradientDrawable().apply { cornerRadius = dp(14).toFloat(); setColor(if (filled) GOLD else CARD); setStroke(dp(1), GOLD) }
        layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, dp(48)).apply { topMargin = dp(10) }
        setOnClickListener { f() }
    }

    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        val prefs = getSharedPreferences("cfg", MODE_PRIVATE)
        root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setBackgroundColor(BG); setPadding(dp(20), dp(24), dp(20), dp(32)) }

        root.addView(TextView(this).apply {
            text = "FREE CLICKER"; textSize = 28f; setTextColor(GOLD); typeface = Typeface.DEFAULT_BOLD; letterSpacing = 0.12f
        })
        root.addView(text("Auto tap & swipe  •  No ads  •  Everything free"))

        // Status + setup
        val setup = card("Setup")
        status = text("", 15f, GOLD).also { setup.addView(it) }
        setup.addView(text("Turn on Free Clicker in Accessibility, open your target app, then use the floating panel: ＋ add point, ▶ start."))
        setup.addView(goldBtn("Open Accessibility settings") { startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) })

        // Interval
        val iv = card("Click interval  (0 – 24 h)")
        val cur = prefs.getLong("interval", 500L)
        fun field(hnt: String, v: Long) = EditText(this).apply {
            setText(v.toString()); hint = hnt; gravity = Gravity.CENTER; textSize = 18f
            setTextColor(0xFFFFFFFF.toInt()); setHintTextColor(MUTED)
            backgroundTintList = ColorStateList.valueOf(GOLD)
            inputType = InputType.TYPE_CLASS_NUMBER
            layoutParams = LinearLayout.LayoutParams(0, WRAP_CONTENT, 1f).apply { setMargins(dp(4), 0, dp(4), 0) }
        }
        val fh = field("h", cur / 3600000); val fm = field("m", cur % 3600000 / 60000)
        val fs = field("s", cur % 60000 / 1000); val fms = field("ms", cur % 1000)
        iv.addView(LinearLayout(this).apply { listOf(fh, fm, fs, fms).forEach { addView(it) } })
        iv.addView(text("hours   |   minutes   |   seconds   |   ms").apply { gravity = Gravity.CENTER; textSize = 11f })
        iv.addView(goldBtn("Save interval") {
            fun n(e: EditText) = e.text.toString().toLongOrNull() ?: 0L
            val total = (n(fh) * 3600000 + n(fm) * 60000 + n(fs) * 1000 + n(fms)).coerceIn(0, MAX_MS)
            prefs.edit().putLong("interval", total).apply()
            fh.setText((total / 3600000).toString()); fm.setText((total % 3600000 / 60000).toString())
            fs.setText((total % 60000 / 1000).toString()); fms.setText((total % 1000).toString())
            Toast.makeText(this, "Saved ($total ms)", Toast.LENGTH_SHORT).show()
        })

        // Point size
        val ps = card("Point size")
        val sizeLabel = text("", 15f, 0xFFFFFFFF.toInt()).also { ps.addView(it) }
        val sb = SeekBar(this).apply {
            max = 52; progress = prefs.getInt("pointDp", 32) - 12
            progressTintList = ColorStateList.valueOf(GOLD); thumbTintList = ColorStateList.valueOf(GOLD)
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(s: SeekBar?, p: Int, u: Boolean) {
                    sizeLabel.text = "${p + 12} dp"; prefs.edit().putInt("pointDp", p + 12).apply()
                }
                override fun onStartTrackingTouch(s: SeekBar?) {}
                override fun onStopTrackingTouch(s: SeekBar?) {}
            })
        }
        sizeLabel.text = "${sb.progress + 12} dp"
        ps.addView(sb)

        // Panel
        val pn = card("Floating panel")
        showSwitch = Switch(this).apply {
            text = "Show floating panel"; setTextColor(0xFFFFFFFF.toInt())
            thumbTintList = ColorStateList.valueOf(GOLD); trackTintList = ColorStateList.valueOf(0x66D4AF37)
            isChecked = prefs.getBoolean("panelVisible", true)
            setOnCheckedChangeListener { _, c -> prefs.edit().putBoolean("panelVisible", c).apply() }
        }
        pn.addView(showSwitch)
        pn.addView(text("Also available as a Quick Settings tile (\"Clicker panel\"), so you can show or hide it from any app."))
        pn.addView(Switch(this).apply {
            text = "Compact panel"; setTextColor(0xFFFFFFFF.toInt())
            thumbTintList = ColorStateList.valueOf(GOLD); trackTintList = ColorStateList.valueOf(0x66D4AF37)
            isChecked = prefs.getBoolean("compact", false)
            setOnCheckedChangeListener { _, c -> prefs.edit().putBoolean("compact", c).apply() }
        })
        pn.addView(text("Keeps only the drag handle, 👁 and ▶/■."))

        // Close
        val cl = card("Exit")
        cl.addView(text("Stops the clicker, removes the floating panel and turns the accessibility service off."))
        cl.addView(goldBtn("Close Free Clicker", filled = false) {
            ClickerService.instance?.shutdown()
            finishAndRemoveTask()
        })

        setContentView(ScrollView(this).apply { setBackgroundColor(BG); addView(root) })
    }

    override fun onResume() {
        super.onResume()
        val on = Settings.Secure.getString(contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)?.contains(packageName) == true
        status.text = if (on) "●  Service active" else "●  Service inactive"
        status.setTextColor(if (on) GOLD else 0xFFCF6679.toInt())
        showSwitch.isChecked = getSharedPreferences("cfg", MODE_PRIVATE).getBoolean("panelVisible", true)
    }
}