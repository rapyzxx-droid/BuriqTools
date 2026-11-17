package com.example.buriktool

import android.app.Activity
import android.os.Bundle
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

    private lateinit var status: TextView
    private lateinit var spinner: Spinner

    private fun currentPkg(): String = games[spinner.selectedItemPosition].second

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val pad = (16 * resources.displayMetrics.density).toInt()
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(pad, pad * 2, pad, pad)
        }

        val title = TextView(this).apply {
            text = "Buriq Tools"
            textSize = 22f
        }

        spinner = Spinner(this).apply {
            adapter = ArrayAdapter(
                this@MainActivity,
                android.R.layout.simple_spinner_dropdown_item,
                games.map { it.first }
            )
        }

        val label = TextView(this).apply { text = "Offset tekstur: 3 (4 = paling burik)" }
        val seek = SeekBar(this).apply { max = 4; progress = 3 }
        seek.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(s: SeekBar?, p: Int, fromUser: Boolean) {
                label.text = "Offset tekstur: $p (4 = paling burik)"
            }
            override fun onStartTrackingTouch(s: SeekBar?) {}
            override fun onStopTrackingTouch(s: SeekBar?) {}
        })

        val apply = Button(this).apply { text = "Terapkan" }
        val reset = Button(this).apply { text = "Reset ke normal" }
        val refresh = Button(this).apply { text = "Cek status" }

        status = TextView(this).apply { text = "Belum ada aksi." }

        apply.setOnClickListener {
            val pkg = currentPkg()
            val n = seek.progress
            val cmd = "settings put global angle_gl_driver_selection_pkgs $pkg && " +
                "settings put global angle_gl_driver_selection_values angle && " +
                "setprop debug.angle.feature_overrides_enabled " +
                "forceNearestFiltering:disableAnisotropicFiltering:forceTextureLodOffset$n && " +
                "am force-stop $pkg"
            su(cmd) { checkStatus("Diterapkan ke $pkg. Buka game dari awal.") }
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

        listOf(title, spinner, label, seek, apply, reset, refresh, status).forEach { root.addView(it) }
        setContentView(ScrollView(this).apply { addView(root) })
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
