package com.example.buriktool

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.SeekBar
import android.widget.Spinner
import android.widget.TextView

class MainActivity : Activity() {

    private val games = listOf(
        "Free Fire MAX" to "com.dts.freefiremax",
        "Free Fire" to "com.dts.freefireth"
    )

    private lateinit var prefs: SharedPreferences
    private lateinit var status: TextView
    private lateinit var spinner: Spinner

    private val colorBg = Color.parseColor("#121218")
    private val colorCard = Color.parseColor("#1E1E2A")
    private val colorAccent = Color.parseColor("#FF8A3D")
    private val colorText = Color.parseColor("#F2F2F7")
    private val colorDim = Color.parseColor("#9A9AAE")
    private val colorBtn = Color.parseColor("#2C2C3A")

    private fun dp(v: Int): Int = (v * resources.displayMetrics.density).toInt()

    private fun rounded(color: Int, radiusDp: Int) = GradientDrawable().apply {
        setColor(color)
        cornerRadius = dp(radiusDp).toFloat()
    }

    private fun cardLayout() = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        background = rounded(colorCard, 16)
        setPadding(dp(16), dp(14), dp(16), dp(16))
    }

    private fun smallLabel(t: String) = TextView(this).apply {
        text = t
        textSize = 11f
        letterSpacing = 0.12f
        setTextColor(colorDim)
        typeface = Typeface.DEFAULT_BOLD
    }

    private fun styledButton(label: String, fill: Int, textColor: Int) = Button(this).apply {
        text = label
        isAllCaps = false
        textSize = 16f
        setTextColor(textColor)
        background = rounded(fill, 14)
        stateListAnimator = null
        minHeight = dp(52)
    }

    private fun LinearLayout.addWithMargin(v: View, topDp: Int = 0) {
        addView(
            v,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = dp(topDp) }
        )
    }

    private fun currentPkg(): String = games[spinner.selectedItemPosition].second

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        prefs = getSharedPreferences("buriq", Context.MODE_PRIVATE)
        val savedOffset = prefs.getInt("offset", 3).coerceIn(0, 4)
        val savedGame = prefs.getInt("game", 0).coerceIn(0, games.size - 1)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(48), dp(18), dp(28))
        }

        val title = TextView(this).apply {
            text = "Buriq Tools"
            textSize = 28f
            setTextColor(colorText)
            typeface = Typeface.DEFAULT_BOLD
        }
        val subtitle = TextView(this).apply {
            text = "Kontrol tekstur ANGLE (butuh root)"
            textSize = 13f
            setTextColor(colorDim)
        }
        root.addWithMargin(title)
        root.addWithMargin(subtitle, 2)

        // Kartu pilihan game
        val gameCard = cardLayout()
        gameCard.addView(smallLabel("GAME"))
        spinner = Spinner(this).apply {
            adapter = ArrayAdapter(
                this@MainActivity,
                android.R.layout.simple_spinner_dropdown_item,
                games.map { it.first }
            )
            setSelection(savedGame)
            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(p: AdapterView<*>?, v: View?, pos: Int, id: Long) {
                    prefs.edit().putInt("game", pos).apply()
                }
                override fun onNothingSelected(p: AdapterView<*>?) {}
            }
        }
        gameCard.addWithMargin(spinner, 4)
        root.addWithMargin(gameCard, 22)

        // Kartu offset
        val offsetCard = cardLayout()
        offsetCard.addView(smallLabel("OFFSET TEKSTUR"))
        val valueText = TextView(this).apply {
            text = savedOffset.toString()
            textSize = 48f
            gravity = Gravity.CENTER
            setTextColor(colorAccent)
            typeface = Typeface.DEFAULT_BOLD
        }
        val hint = TextView(this).apply {
            text = "0 = tajam   •   4 = paling burik"
            textSize = 12f
            gravity = Gravity.CENTER
            setTextColor(colorDim)
        }
        val seek = SeekBar(this).apply {
            max = 4
            progress = savedOffset
            progressTintList = ColorStateList.valueOf(colorAccent)
            thumbTintList = ColorStateList.valueOf(colorAccent)
        }
        seek.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(s: SeekBar?, p: Int, fromUser: Boolean) {
                valueText.text = p.toString()
                prefs.edit().putInt("offset", p).apply()
            }
            override fun onStartTrackingTouch(s: SeekBar?) {}
            override fun onStopTrackingTouch(s: SeekBar?) {}
        })
        offsetCard.addWithMargin(valueText, 4)
        offsetCard.addWithMargin(hint)
        offsetCard.addWithMargin(seek, 10)
        root.addWithMargin(offsetCard, 14)

        // Tombol
        val apply = styledButton("Terapkan", colorAccent, Color.parseColor("#1A1208"))
        val reset = styledButton("Reset ke normal", colorBtn, colorText)
        val refresh = styledButton("Cek status", colorBtn, colorText)
        root.addWithMargin(apply, 18)
        root.addWithMargin(reset, 10)
        root.addWithMargin(refresh, 10)

        // Kartu log
        val logCard = cardLayout()
        logCard.addView(smallLabel("LOG"))
        status = TextView(this).apply {
            text = "Belum ada aksi."
            textSize = 12.5f
            setTextColor(colorText)
            typeface = Typeface.MONOSPACE
            setTextIsSelectable(true)
        }
        logCard.addWithMargin(status, 6)
        root.addWithMargin(logCard, 18)

        apply.setOnClickListener {
            val pkg = currentPkg()
            val n = seek.progress
            val cmd = "settings put global angle_gl_driver_selection_pkgs $pkg && " +
                "settings put global angle_gl_driver_selection_values angle && " +
                "setprop debug.angle.feature_overrides_enabled " +
                "forceNearestFiltering:disableAnisotropicFiltering:forceTextureLodOffset$n && " +
                "am force-stop $pkg"
            su(cmd) { checkStatus("Diterapkan ke $pkg (offset $n). Buka game dari awal.") }
        }

        reset.setOnClickListener {
            val pkg = currentPkg()
            val cmd = "setprop debug.angle.feature_overrides_enabled '' ; " +
                "settings delete global angle_gl_driver_selection_pkgs ; " +
                "settings delete global angle_gl_driver_selection_values ; " +
                "am force-stop $pkg"
            su(cmd) { checkStatus("Direset ke normal.") }
        }

        refresh.setOnClickListener { checkStatus("Status:") }

        setContentView(ScrollView(this).apply {
            setBackgroundColor(colorBg)
            isFillViewport = true
            addView(root)
        })
    }

    private fun checkStatus(header: String) {
        val cmd = "settings list global | grep -i angle ; " +
            "echo prop=$(getprop debug.angle.feature_overrides_enabled)"
        su(cmd) { out -> status.text = "$header\n$out" }
    }

    private fun su(cmd: String, done: (String) -> Unit) {
        Thread {
            val out = try {
                val p = Runtime.getRuntime().exec(arrayOf("su", "-c", cmd))
                val o = p.inputStream.bufferedReader().readText()
                val e = p.errorStream.bufferedReader().readText()
                p.waitFor()
                (o + e).trim()
            } catch (ex: Exception) {
                "Gagal (root ditolak?): ${ex.message}"
            }
            runOnUiThread { done(out) }
        }.start()
    }
}
