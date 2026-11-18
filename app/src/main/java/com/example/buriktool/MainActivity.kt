package com.example.buriktool

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.Gravity
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.SeekBar
import android.widget.Spinner
import android.widget.TextView
import rikka.shizuku.Shizuku

class MainActivity : Activity() {

    private val games = listOf(
        "Free Fire MAX" to "com.dts.freefiremax",
        "Free Fire" to "com.dts.freefireth"
    )

    private lateinit var prefs: SharedPreferences
    private lateinit var status: TextView
    private lateinit var spinner: Spinner
    private lateinit var chip: TextView
    private lateinit var modeHint: TextView
    private lateinit var btnRoot: Button
    private lateinit var btnShizuku: Button
    private lateinit var autoLaunch: CheckBox

    private var mode = "root"
    private var rootOk: Boolean? = null
    private val ui = Handler(Looper.getMainLooper())

    private val colorBg = Color.parseColor("#121218")
    private val colorCard = Color.parseColor("#1E1E2A")
    private val colorAccent = Color.parseColor("#FF8A3D")
    private val colorText = Color.parseColor("#F2F2F7")
    private val colorDim = Color.parseColor("#9A9AAE")
    private val colorBtn = Color.parseColor("#2C2C3A")
    private val colorOnAccent = Color.parseColor("#1A1208")
    private val colorGreen = Color.parseColor("#3DDC84")
    private val colorRed = Color.parseColor("#FF5C5C")
    private val colorOrange = Color.parseColor("#FFB020")
    private val colorGray = Color.parseColor("#6E6E80")

    private val permListener =
        Shizuku.OnRequestPermissionResultListener { _, grantResult ->
            runOnUiThread {
                refreshUi()
                status.text = if (grantResult == PackageManager.PERMISSION_GRANTED)
                    "Izin Shizuku diberikan."
                else "Izin Shizuku ditolak."
            }
        }
    private val binderListener = Shizuku.OnBinderReceivedListener { runOnUiThread { refreshUi() } }
    private val deadListener = Shizuku.OnBinderDeadListener { runOnUiThread { refreshUi() } }

    // ---------- helper UI ----------

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

    private fun rowOfTwo(a: View, b: View): LinearLayout = LinearLayout(this).apply {
        orientation = LinearLayout.HORIZONTAL
        addView(a, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            .apply { marginEnd = dp(6) })
        addView(b, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            .apply { marginStart = dp(6) })
    }

    private fun currentPkg(): String = games[spinner.selectedItemPosition].second
    private fun currentGameName(): String = games[spinner.selectedItemPosition].first
    private fun modeLabel(): String = if (mode == "root") "Root" else "Shizuku"

    // ---------- status mode ----------

    // 0 = siap, 1 = belum ada izin, 2 = Shizuku belum berjalan
    private fun shizukuState(): Int = try {
        if (!Shizuku.pingBinder()) 2
        else if (Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED) 0
        else 1
    } catch (e: Throwable) {
        2
    }

    private fun refreshUi() {
        val color: Int
        val label: String
        if (mode == "root") {
            label = "ROOT"
            color = when (rootOk) {
                true -> colorGreen
                false -> colorRed
                null -> colorGray
            }
            modeHint.text = when (rootOk) {
                true -> "Perintah dijalankan lewat su. Root aktif."
                false -> "Root tidak tersedia atau ditolak. Cek izin di manajer root."
                null -> "Perintah dijalankan lewat su. Memeriksa root..."
            }
        } else {
            label = "SHIZUKU"
            val st = shizukuState()
            color = when (st) {
                0 -> colorGreen
                1 -> colorOrange
                else -> colorRed
            }
            modeHint.text = when (st) {
                0 -> "Shizuku aktif dan izin sudah diberikan."
                1 -> "Izin belum diberikan. Ketuk Terapkan untuk memintanya."
                else -> "Shizuku belum berjalan. Buka aplikasi Shizuku lalu nyalakan."
            }
        }
        val s = SpannableString("●  $label")
        s.setSpan(ForegroundColorSpan(color), 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        chip.text = s

        fun style(b: Button, selected: Boolean) {
            b.background = rounded(if (selected) colorAccent else colorBtn, 12)
            b.setTextColor(if (selected) colorOnAccent else colorText)
        }
        style(btnRoot, mode == "root")
        style(btnShizuku, mode == "shizuku")
    }

    private fun checkRoot() {
        Thread {
            val out = try {
                runProcess(arrayOf("su", "-c", "id"))
            } catch (e: Exception) {
                ""
            }
            rootOk = out.contains("uid=0")
            runOnUiThread { refreshUi() }
        }.start()
    }

    private fun setMode(m: String) {
        mode = m
        prefs.edit().putString("mode", m).apply()
        if (m == "root") {
            rootOk = null
            checkRoot()
        } else if (shizukuState() == 1) {
            try {
                Shizuku.requestPermission(1001)
            } catch (e: Throwable) {
            }
        }
        refreshUi()
    }

    // Mengembalikan true kalau mode yang dipilih siap dipakai.
    private fun ensureReady(): Boolean {
        if (mode != "shizuku") return true
        when (shizukuState()) {
            2 -> {
                status.text = "Shizuku belum berjalan. Buka aplikasi Shizuku dan nyalakan dulu."
                return false
            }
            1 -> {
                try {
                    Shizuku.requestPermission(1001)
                } catch (e: Throwable) {
                }
                status.text = "Meminta izin Shizuku. Ketuk Izinkan, lalu tekan tombol lagi."
                return false
            }
        }
        return true
    }

    // ---------- eksekusi perintah ----------

    private fun readAll(p: Process): String {
        val o = p.inputStream.bufferedReader().readText()
        val e = p.errorStream.bufferedReader().readText()
        p.waitFor()
        return (o + e).trim()
    }

    private fun runProcess(args: Array<String>): String {
        val p = Runtime.getRuntime().exec(args)
        return readAll(p)
    }

    // Shizuku.newProcess bersifat private di API 13, jadi dipanggil lewat reflection.
    private fun runShizuku(cmd: String): String {
        val m = Shizuku::class.java.getDeclaredMethod(
            "newProcess",
            Array<String>::class.java,
            Array<String>::class.java,
            String::class.java
        )
        m.isAccessible = true
        val p = m.invoke(null, arrayOf("sh", "-c", cmd), null, null) as Process
        return readAll(p)
    }

    private fun exec(cmd: String, done: (String) -> Unit) {
        val useRoot = mode == "root"
        Thread {
            val out = try {
                if (useRoot) runProcess(arrayOf("su", "-c", cmd)) else runShizuku(cmd)
            } catch (ex: Exception) {
                val why = ex.cause?.message ?: ex.message
                "Gagal (${if (useRoot) "root" else "Shizuku"}): $why"
            }
            runOnUiThread { done(out) }
        }.start()
    }

    private fun checkStatus(header: String, extra: String = "", afterApply: Boolean = false) {
        val cmd = "settings list global | grep -i angle ; " +
            "echo prop=$(getprop debug.angle.feature_overrides_enabled)"
        exec(cmd) { out ->
            var t = header
            if (extra.isNotBlank()) t += "\n$extra"
            t += "\n$out"
            if (afterApply && mode == "shizuku" && out.trim().endsWith("prop=")) {
                t += "\nCatatan: properti debug.angle tidak ter-set lewat Shizuku. Coba mode Root."
            }
            status.text = t
        }
    }

    private fun launchGame(silent: Boolean) {
        val pkg = currentPkg()
        val intent = packageManager.getLaunchIntentForPackage(pkg)
        if (intent == null) {
            val msg = "Game $pkg tidak ditemukan atau belum terpasang."
            status.text = if (silent) status.text.toString() + "\n" + msg else msg
            return
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        if (!silent) status.text = "Membuka ${currentGameName()}..."
        startActivity(intent)
    }

    // ---------- lifecycle ----------

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        prefs = getSharedPreferences("buriq", Context.MODE_PRIVATE)
        val savedOffset = prefs.getInt("offset", 3).coerceIn(0, 4)
        val savedGame = prefs.getInt("game", 0).coerceIn(0, games.size - 1)
        val savedAuto = prefs.getBoolean("autolaunch", false)
        mode = prefs.getString("mode", "root") ?: "root"

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(44), dp(18), dp(28))
        }

        // Label mode di pojok kiri atas
        chip = TextView(this).apply {
            textSize = 13f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(colorText)
            background = rounded(colorCard, 20)
            setPadding(dp(14), dp(6), dp(14), dp(6))
        }
        root.addView(
            chip,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        val title = TextView(this).apply {
            text = "Buriq Tools"
            textSize = 28f
            setTextColor(colorText)
            typeface = Typeface.DEFAULT_BOLD
        }
        val subtitle = TextView(this).apply {
            text = "Kontrol tekstur ANGLE"
            textSize = 13f
            setTextColor(colorDim)
        }
        root.addWithMargin(title, 14)
        root.addWithMargin(subtitle, 2)

        // Kartu mode
        val modeCard = cardLayout()
        modeCard.addView(smallLabel("MODE"))
        btnRoot = styledButton("Root", colorAccent, colorOnAccent).apply { minHeight = dp(44) }
        btnShizuku = styledButton("Shizuku", colorBtn, colorText).apply { minHeight = dp(44) }
        btnRoot.setOnClickListener { setMode("root") }
        btnShizuku.setOnClickListener { setMode("shizuku") }
        modeCard.addWithMargin(rowOfTwo(btnRoot, btnShizuku), 8)
        modeHint = TextView(this).apply {
            textSize = 12f
            setTextColor(colorDim)
        }
        modeCard.addWithMargin(modeHint, 8)
        root.addWithMargin(modeCard, 18)

        // Kartu game
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
        root.addWithMargin(gameCard, 14)

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
        autoLaunch = CheckBox(this).apply {
            text = "Buka game otomatis setelah Terapkan"
            textSize = 13f
            setTextColor(colorText)
            isChecked = savedAuto
            buttonTintList = ColorStateList.valueOf(colorAccent)
            setOnCheckedChangeListener { _, checked ->
                prefs.edit().putBoolean("autolaunch", checked).apply()
            }
        }
        offsetCard.addWithMargin(valueText, 4)
        offsetCard.addWithMargin(hint)
        offsetCard.addWithMargin(seek, 10)
        offsetCard.addWithMargin(autoLaunch, 12)
        root.addWithMargin(offsetCard, 14)

        // Tombol aksi
        val apply = styledButton("Terapkan", colorAccent, colorOnAccent)
        val launch = styledButton("Buka game", colorBtn, colorText)
        val reset = styledButton("Reset", colorBtn, colorText)
        val refresh = styledButton("Cek status", colorBtn, colorText)
        root.addWithMargin(apply, 18)
        root.addWithMargin(launch, 10)
        root.addWithMargin(rowOfTwo(reset, refresh), 10)

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
            if (!ensureReady()) return@setOnClickListener
            val pkg = currentPkg()
            val n = seek.progress
            val cmd = "settings put global angle_gl_driver_selection_pkgs $pkg && " +
                "settings put global angle_gl_driver_selection_values angle && " +
                "setprop debug.angle.feature_overrides_enabled " +
                "forceNearestFiltering:disableAnisotropicFiltering:forceTextureLodOffset$n && " +
                "am force-stop $pkg"
            exec(cmd) { out ->
                checkStatus(
                    "Diterapkan ke $pkg (offset $n) via ${modeLabel()}.",
                    out,
                    afterApply = true
                )
                if (autoLaunch.isChecked) {
                    ui.postDelayed({ launchGame(true) }, 700)
                }
            }
        }

        launch.setOnClickListener { launchGame(false) }

        reset.setOnClickListener {
            if (!ensureReady()) return@setOnClickListener
            val pkg = currentPkg()
            val cmd = "setprop debug.angle.feature_overrides_enabled '' ; " +
                "settings delete global angle_gl_driver_selection_pkgs ; " +
                "settings delete global angle_gl_driver_selection_values ; " +
                "am force-stop $pkg"
            exec(cmd) { out -> checkStatus("Direset ke normal via ${modeLabel()}.", out) }
        }

        refresh.setOnClickListener {
            if (!ensureReady()) return@setOnClickListener
            checkStatus("Status (${modeLabel()}):")
        }

        setContentView(ScrollView(this).apply {
            setBackgroundColor(colorBg)
            isFillViewport = true
            addView(root)
        })

        try {
            Shizuku.addRequestPermissionResultListener(permListener)
            Shizuku.addBinderReceivedListenerSticky(binderListener)
            Shizuku.addBinderDeadListener(deadListener)
        } catch (e: Throwable) {
        }

        refreshUi()
        if (mode == "root") checkRoot()
    }

    override fun onResume() {
        super.onResume()
        if (::chip.isInitialized) refreshUi()
    }

    override fun onDestroy() {
        try {
            Shizuku.removeRequestPermissionResultListener(permListener)
            Shizuku.removeBinderReceivedListener(binderListener)
            Shizuku.removeBinderDeadListener(deadListener)
        } catch (e: Throwable) {
        }
        super.onDestroy()
    }
}
