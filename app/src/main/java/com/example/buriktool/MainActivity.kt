package com.example.buriktool

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Outline
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.RippleDrawable
import android.os.BatteryManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.SpannableString
import android.text.Spanned
import android.text.TextUtils
import android.text.style.ForegroundColorSpan
import android.view.Gravity
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import android.view.ViewOutlineProvider
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.SeekBar
import android.widget.TextClock
import android.widget.TextView
import rikka.shizuku.Shizuku

class MainActivity : Activity() {

    private val games = listOf(
        "Free Fire MAX" to "com.dts.freefiremax",
        "Free Fire" to "com.dts.freefireth"
    )

    private lateinit var prefs: SharedPreferences
    private lateinit var status: TextView
    private lateinit var statusLine: TextView
    private lateinit var chip: TextView
    private lateinit var batteryText: TextView
    private lateinit var tempText: TextView
    private lateinit var modeTileTitle: TextView
    private lateinit var modeTileSub: TextView
    private lateinit var offsetSeek: SeekBar
    private lateinit var logPanel: LinearLayout
    private val gameCards = mutableListOf<LinearLayout>()

    private var selectedGame = 0
    private var mode = "root"
    private var rootOk: Boolean? = null
    private val ui = Handler(Looper.getMainLooper())

    private val colorText = Color.parseColor("#F2F7F5")
    private val colorDim = Color.parseColor("#9DB5AD")
    private val colorAccent = Color.parseColor("#4FD6A0")
    private val colorAccentLight = Color.parseColor("#8CF5CC")
    private val colorOnAccent = Color.parseColor("#06241A")
    private val colorGreen = Color.parseColor("#3DDC84")
    private val colorRed = Color.parseColor("#FF5C5C")
    private val colorOrange = Color.parseColor("#FFB020")
    private val colorGray = Color.parseColor("#6E7F79")

    private val tick = object : Runnable {
        override fun run() {
            updateBattery()
            ui.postDelayed(this, 10000)
        }
    }

    private val permListener =
        Shizuku.OnRequestPermissionResultListener { _, grantResult ->
            runOnUiThread {
                refreshUi()
                setLog(
                    if (grantResult == PackageManager.PERMISSION_GRANTED) "Izin Shizuku diberikan."
                    else "Izin Shizuku ditolak."
                )
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

    // Kotak "kaca": isi putih transparan + garis tepi tipis
    private fun glass(
        fillAlpha: Int = 36,
        strokeAlpha: Int = 80,
        radiusDp: Int = 12,
        tint: Int = Color.WHITE
    ) = GradientDrawable().apply {
        setColor(Color.argb(fillAlpha, Color.red(tint), Color.green(tint), Color.blue(tint)))
        cornerRadius = dp(radiusDp).toFloat()
        setStroke(dp(1), Color.argb(strokeAlpha, Color.red(tint), Color.green(tint), Color.blue(tint)))
    }

    private fun ripple(radiusDp: Int) = RippleDrawable(
        ColorStateList.valueOf(Color.argb(70, 255, 255, 255)),
        null,
        rounded(Color.WHITE, radiusDp)
    )

    private fun smallLabel(t: String) = TextView(this).apply {
        text = t
        textSize = 11f
        letterSpacing = 0.12f
        setTextColor(colorDim)
        typeface = Typeface.DEFAULT_BOLD
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
            .apply { marginEnd = dp(5) })
        addView(b, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            .apply { marginStart = dp(5) })
    }

    private fun makeTile(
        title: String,
        sub: String,
        onClick: () -> Unit
    ): Triple<LinearLayout, TextView, TextView> {
        val t = TextView(this).apply {
            text = title
            textSize = 14f
            setTextColor(colorText)
            typeface = Typeface.DEFAULT_BOLD
        }
        val s = TextView(this).apply {
            text = sub
            textSize = 10.5f
            setTextColor(colorDim)
            maxLines = 1
            ellipsize = TextUtils.TruncateAt.END
        }
        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_VERTICAL
            background = glass(36, 80, 12)
            foreground = ripple(12)
            minimumHeight = dp(44)
            setPadding(dp(12), dp(4), dp(12), dp(4))
            isClickable = true
            setOnClickListener { onClick() }
            addView(t)
            addView(s)
        }
        return Triple(box, t, s)
    }

    private fun currentPkg(): String = games[selectedGame].second
    private fun currentGameName(): String = games[selectedGame].first
    private fun modeLabel(): String = if (mode == "root") "Root" else "Shizuku"

    private fun setLog(t: String) {
        status.text = t
        statusLine.text = t.lines().firstOrNull() ?: ""
    }

    private fun updateBattery() {
        val i = registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED)) ?: return
        val level = i.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = i.getIntExtra(BatteryManager.EXTRA_SCALE, 100)
        val temp = i.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) / 10
        val pct = if (level >= 0 && scale > 0) level * 100 / scale else 0
        batteryText.text = "$pct%"
        tempText.text = "$temp°C"
    }

    // ---------- daftar game (ikon + nama package) ----------

    private fun isInstalled(pkg: String): Boolean = try {
        packageManager.getApplicationInfo(pkg, 0)
        true
    } catch (e: Exception) {
        false
    }

    private fun iconOf(pkg: String): Drawable? = try {
        packageManager.getApplicationIcon(pkg)
    } catch (e: Exception) {
        getDrawable(android.R.drawable.sym_def_app_icon)
    }

    private fun buildGameCard(index: Int): LinearLayout {
        val (name, pkg) = games[index]
        val installed = isInstalled(pkg)

        val icon = ImageView(this).apply {
            setImageDrawable(iconOf(pkg))
            scaleType = ImageView.ScaleType.FIT_CENTER
            outlineProvider = object : ViewOutlineProvider() {
                override fun getOutline(v: View, o: Outline) {
                    o.setRoundRect(0, 0, v.width, v.height, dp(12).toFloat())
                }
            }
            clipToOutline = true
        }

        val texts = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        texts.addView(TextView(this).apply {
            text = name
            textSize = 16f
            setTextColor(colorText)
            typeface = Typeface.DEFAULT_BOLD
        })
        texts.addView(TextView(this).apply {
            text = pkg
            textSize = 10.5f
            setTextColor(colorDim)
            typeface = Typeface.MONOSPACE
            maxLines = 1
            ellipsize = TextUtils.TruncateAt.END
        })
        texts.addView(TextView(this).apply {
            text = if (installed) "Terpasang" else "Belum terpasang"
            textSize = 10.5f
            setTextColor(if (installed) colorGreen else colorRed)
        })

        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            foreground = ripple(14)
            setPadding(dp(12), dp(10), dp(12), dp(10))
            isClickable = true
            addView(icon, LinearLayout.LayoutParams(dp(48), dp(48)))
            addView(
                texts,
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { marginStart = dp(12) }
            )
            setOnClickListener { selectGame(index) }
        }
    }

    private fun selectGame(i: Int) {
        selectedGame = i
        prefs.edit().putInt("game", i).apply()
        refreshGameCards()
    }

    private fun refreshGameCards() {
        gameCards.forEachIndexed { i, v ->
            val sel = i == selectedGame
            v.background = if (sel) glass(60, 190, 14, colorAccent) else glass(14, 40, 14)
            v.alpha = if (sel) 1f else 0.72f
        }
    }

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
        val sub: String
        if (mode == "root") {
            label = "ROOT"
            color = when (rootOk) {
                true -> colorGreen
                false -> colorRed
                null -> colorGray
            }
            sub = when (rootOk) {
                true -> "root aktif"
                false -> "root ditolak"
                null -> "memeriksa..."
            }
        } else {
            label = "SHIZUKU"
            val st = shizukuState()
            color = when (st) {
                0 -> colorGreen
                1 -> colorOrange
                else -> colorRed
            }
            sub = when (st) {
                0 -> "siap dipakai"
                1 -> "izin belum diberikan"
                else -> "belum berjalan"
            }
        }
        val s = SpannableString("●  $label")
        s.setSpan(ForegroundColorSpan(color), 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        chip.text = s

        modeTileTitle.text = "Mode: ${modeLabel()}"
        modeTileSub.text = sub
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
                setLog("Shizuku belum berjalan. Buka aplikasi Shizuku dan nyalakan dulu.")
                return false
            }
            1 -> {
                try {
                    Shizuku.requestPermission(1001)
                } catch (e: Throwable) {
                }
                setLog("Meminta izin Shizuku. Ketuk Izinkan, lalu tekan tombol lagi.")
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
            setLog(t)
        }
    }

    private fun applyOffset(launchAfter: Boolean) {
        if (!ensureReady()) return
        val pkg = currentPkg()
        val n = offsetSeek.progress
        val cmd = "settings put global angle_gl_driver_selection_pkgs $pkg && " +
            "settings put global angle_gl_driver_selection_values angle && " +
            "setprop debug.angle.feature_overrides_enabled " +
            "forceNearestFiltering:disableAnisotropicFiltering:forceTextureLodOffset$n && " +
            "am force-stop $pkg"
        exec(cmd) { out ->
            checkStatus("Diterapkan ke $pkg (offset $n) via ${modeLabel()}.", out, afterApply = true)
            if (launchAfter) ui.postDelayed({ launchGame(true) }, 700)
        }
    }

    private fun resetAll() {
        if (!ensureReady()) return
        val pkg = currentPkg()
        val cmd = "setprop debug.angle.feature_overrides_enabled '' ; " +
            "settings delete global angle_gl_driver_selection_pkgs ; " +
            "settings delete global angle_gl_driver_selection_values ; " +
            "am force-stop $pkg"
        exec(cmd) { out -> checkStatus("Direset ke normal via ${modeLabel()}.", out) }
    }

    private fun launchGame(silent: Boolean) {
        val pkg = currentPkg()
        val intent = packageManager.getLaunchIntentForPackage(pkg)
        if (intent == null) {
            val msg = "Game $pkg tidak ditemukan atau belum terpasang."
            setLog(if (silent) status.text.toString() + "\n" + msg else msg)
            return
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        if (!silent) setLog("Membuka ${currentGameName()}...")
        startActivity(intent)
    }

    // ---------- lifecycle ----------

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        prefs = getSharedPreferences("buriq", Context.MODE_PRIVATE)
        val savedOffset = prefs.getInt("offset", 3).coerceIn(0, 4)
        selectedGame = prefs.getInt("game", 0).coerceIn(0, games.size - 1)
        mode = prefs.getString("mode", "root") ?: "root"

        status = TextView(this).apply {
            text = "Belum ada aksi."
            textSize = 12f
            setTextColor(colorText)
            typeface = Typeface.MONOSPACE
            setTextIsSelectable(true)
        }
        statusLine = TextView(this).apply {
            text = "Tekstur: 0 = tajam  •  4 = paling burik"
            textSize = 11f
            setTextColor(colorDim)
            typeface = Typeface.MONOSPACE
            maxLines = 1
            ellipsize = TextUtils.TruncateAt.END
        }

        // ===== Bar atas: label mode (kiri), jam, baterai, suhu =====
        chip = TextView(this).apply {
            textSize = 13f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(colorText)
            background = glass(40, 90, 20)
            setPadding(dp(14), dp(5), dp(14), dp(5))
        }
        val clock = TextClock(this).apply {
            format24Hour = "HH:mm"
            format12Hour = "HH:mm"
            textSize = 15f
            setTextColor(colorText)
            typeface = Typeface.DEFAULT_BOLD
        }
        batteryText = TextView(this).apply {
            textSize = 13f
            setTextColor(colorText)
        }
        tempText = TextView(this).apply {
            textSize = 13f
            setTextColor(colorText)
        }
        val brand = TextView(this).apply {
            text = "BURIQ TOOLS"
            textSize = 11f
            letterSpacing = 0.2f
            setTextColor(colorDim)
            typeface = Typeface.DEFAULT_BOLD
        }
        val topBar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        topBar.addView(chip)
        for (v in listOf(clock, batteryText, tempText)) {
            topBar.addView(
                v,
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { marginStart = dp(14) }
            )
        }
        topBar.addView(View(this), LinearLayout.LayoutParams(0, 1, 1f))
        topBar.addView(brand)

        // ===== Panel kiri: daftar game =====
        val left = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_VERTICAL
        }
        for (i in games.indices) {
            val card = buildGameCard(i)
            gameCards.add(card)
            left.addWithMargin(card, if (i == 0) 0 else 10)
        }

        // ===== Panel kanan (tengah): tile + tombol Mulai =====
        val right = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_VERTICAL
        }
        val (launchBox, _, _) = makeTile("Buka game", "luncurkan") { launchGame(false) }
        val (statusBox, _, _) = makeTile("Cek status", "baca ANGLE") {
            if (ensureReady()) checkStatus("Status (${modeLabel()}):")
        }
        val (logBox, _, _) = makeTile("Log", "lihat detail") {
            logPanel.visibility = if (logPanel.visibility == View.VISIBLE) View.GONE else View.VISIBLE
        }
        right.addView(statusLine)
        right.addWithMargin(rowOfTwo(launchBox, statusBox), 6)
        right.addWithMargin(logBox, 6)

        val start = TextView(this).apply {
            text = "▶  Mulai"
            textSize = 20f
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            setTextColor(colorOnAccent)
            background = GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                intArrayOf(colorAccentLight, colorAccent)
            ).apply { cornerRadius = dp(12).toFloat() }
            foreground = ripple(12)
            minHeight = dp(52)
            isClickable = true
            setOnClickListener { applyOffset(true) }
        }
        right.addWithMargin(start, 8)

        // ===== Bar bawah tengah: mode, tekstur, reset, terapkan =====
        val valueText = TextView(this).apply {
            text = savedOffset.toString()
            textSize = 20f
            setTextColor(colorAccent)
            typeface = Typeface.DEFAULT_BOLD
        }
        offsetSeek = SeekBar(this).apply {
            max = 4
            progress = savedOffset
            progressTintList = ColorStateList.valueOf(colorAccent)
            thumbTintList = ColorStateList.valueOf(colorAccent)
        }
        offsetSeek.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(s: SeekBar?, p: Int, fromUser: Boolean) {
                valueText.text = p.toString()
                prefs.edit().putInt("offset", p).apply()
            }
            override fun onStartTrackingTouch(s: SeekBar?) {}
            override fun onStopTrackingTouch(s: SeekBar?) {}
        })
        val texHeader = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        texHeader.addView(
            smallLabel("TEKSTUR"),
            LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        )
        texHeader.addView(valueText)
        val texBlock = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(8), dp(0), dp(8), dp(0))
        }
        texBlock.addView(texHeader)
        texBlock.addView(offsetSeek)

        val (modeBox, modeT, modeS) = makeTile("Mode", "") {
            setMode(if (mode == "root") "shizuku" else "root")
        }
        modeTileTitle = modeT
        modeTileSub = modeS
        modeBox.minimumWidth = dp(124)
        val (resetBox, _, _) = makeTile("Reset", "ke normal") { resetAll() }
        resetBox.minimumWidth = dp(84)
        val (applyBox, applyT, applyS) = makeTile("Terapkan", "tanpa buka game") { applyOffset(false) }
        applyBox.minimumWidth = dp(116)
        applyBox.background = rounded(colorAccent, 12)
        applyT.setTextColor(colorOnAccent)
        applyS.setTextColor(colorOnAccent)

        fun lp(w: Int, ms: Int = 0, me: Int = 0) = LinearLayout.LayoutParams(
            w, LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            marginStart = dp(ms)
            marginEnd = dp(me)
        }

        val bottomBar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            background = glass(40, 110, 28)
            setPadding(dp(10), dp(6), dp(10), dp(6))
        }
        bottomBar.addView(modeBox, lp(LinearLayout.LayoutParams.WRAP_CONTENT))
        bottomBar.addView(texBlock, lp(dp(190), 8, 8))
        bottomBar.addView(resetBox, lp(LinearLayout.LayoutParams.WRAP_CONTENT))
        bottomBar.addView(applyBox, lp(LinearLayout.LayoutParams.WRAP_CONTENT, 6, 0))

        // ===== Panel log (muncul saat tile Log diketuk) =====
        val logHeader = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        logHeader.addView(
            smallLabel("LOG"),
            LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        )
        logHeader.addView(TextView(this).apply {
            text = "Tutup"
            textSize = 13f
            setTextColor(colorAccent)
            typeface = Typeface.DEFAULT_BOLD
            setPadding(dp(10), dp(4), dp(10), dp(4))
            isClickable = true
            setOnClickListener { logPanel.visibility = View.GONE }
        })
        val logScroll = ScrollView(this).apply { addView(status) }
        logPanel = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#F2101C19"))
                cornerRadius = dp(16).toFloat()
                setStroke(dp(1), Color.argb(90, 255, 255, 255))
            }
            setPadding(dp(16), dp(12), dp(16), dp(14))
            visibility = View.GONE
            isClickable = true
        }
        logPanel.addView(logHeader)
        logPanel.addView(
            logScroll,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            ).apply { topMargin = dp(6) }
        )

        // ===== Susun layout landscape =====
        val middle = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        middle.addView(
            ScrollView(this).apply {
                isVerticalScrollBarEnabled = false
                isFillViewport = true
                addView(left)
            },
            LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 5.2f)
                .apply { marginEnd = dp(16) }
        )
        middle.addView(
            ScrollView(this).apply {
                isVerticalScrollBarEnabled = false
                isFillViewport = true
                addView(right)
            },
            LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 3.8f)
        )

        val column = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(26), dp(8), dp(26), dp(10))
        }
        column.addView(
            topBar,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )
        column.addView(
            middle,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            ).apply { topMargin = dp(4) }
        )
        column.addView(
            bottomBar,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.CENTER_HORIZONTAL
                topMargin = dp(6)
            }
        )

        val rootFrame = FrameLayout(this).apply {
            background = GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                intArrayOf(
                    Color.parseColor("#0B1614"),
                    Color.parseColor("#12302A"),
                    Color.parseColor("#0B1B1F")
                )
            )
        }
        rootFrame.addView(
            column,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        )
        rootFrame.addView(
            logPanel,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            ).apply { setMargins(dp(80), dp(50), dp(80), dp(20)) }
        )

        window.attributes = window.attributes.apply {
            layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }
        // Beri jarak dari area kamera (cutout) supaya isi tidak tertutup.
        rootFrame.setOnApplyWindowInsetsListener { _, insets ->
            val cut = insets.displayCutout
            val l = maxOf(dp(26), (cut?.safeInsetLeft ?: 0) + dp(8))
            val r = maxOf(dp(26), (cut?.safeInsetRight ?: 0) + dp(8))
            column.setPadding(l, dp(8), r, dp(10))
            insets
        }
        setContentView(rootFrame)
        hideSystemBars()

        try {
            Shizuku.addRequestPermissionResultListener(permListener)
            Shizuku.addBinderReceivedListenerSticky(binderListener)
            Shizuku.addBinderDeadListener(deadListener)
        } catch (e: Throwable) {
        }

        refreshGameCards()
        refreshUi()
        updateBattery()
        if (mode == "root") checkRoot()
    }

    // Sembunyikan status bar & navigation bar; muncul sementara kalau digeser dari tepi layar.
    private fun hideSystemBars() {
        if (Build.VERSION.SDK_INT >= 30) {
            window.setDecorFitsSystemWindows(false)
            window.insetsController?.let {
                it.hide(WindowInsets.Type.systemBars())
                it.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                )
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) hideSystemBars()
    }

    override fun onResume() {
        super.onResume()
        if (::chip.isInitialized) refreshUi()
        ui.post(tick)
    }

    override fun onPause() {
        ui.removeCallbacks(tick)
        super.onPause()
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
