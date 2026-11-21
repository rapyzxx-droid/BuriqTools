# Laporan Perbaikan — Buriq Tools v1.5 → v1.6

Cakupan: hanya MainActivity.kt dan versi di app/build.gradle.kts. Fitur sensitivitas tidak disentuh.
Status: belum dibuild/diuji (sandbox tidak punya Android SDK dan jaringan). Sintaks diperiksa manual (tanda kurung seimbang). Tolong build lewat GitHub Actions dan tes di perangkat.

## Yang ditambal

1. Verifikasi hasil (sebelumnya selalu bilang "Diterapkan")
   - Setelah Terapkan/Reset, aplikasi membaca balik settings dan properti dari perangkat, lalu membandingkan dengan yang diharapkan.
   - Baris pertama log kini "✓ terverifikasi" atau "⚠ perlu dicek", disertai daftar masalahnya.
   - Menampilkan info bila paket com.android.angle tidak terdeteksi (hanya info, bukan kegagalan) dan catatan bahwa ANGLE hanya untuk OpenGL ES, bukan Vulkan.

2. Status basi setelah reboot
   - Aplikasi menyimpan pkg dan fitur yang terakhir diterapkan. Saat dibuka, jika properti debug.angle sudah kosong/berbeda, muncul peringatan agar Terapkan ulang atau Reset.

3. Peringatan palsu "properti tidak ter-set lewat Shizuku"
   - Hilang. Properti kosong hanya dianggap masalah bila memang ada fitur yang diharapkan.

4. Reflection Shizuku
   - Method newProcess di-cache dan ada pesan error jelas bila tidak ditemukan.
   - BELUM dimigrasi ke UserService (perlu AIDL dan tidak bisa saya uji di sini). Risiko putus di versi Shizuku mendatang masih ada.

5. readAll bisa menggantung
   - stdout dan stderr kini dibaca bersamaan di dua thread, dengan batas waktu 30 detik (60 detik untuk cek root karena menunggu dialog izin). Proses yang macet di-destroy dan dilaporkan "[waktu habis ...]".

6. Race condition kecil
   - rootOk diberi @Volatile.

## Tambahan kecil
- Tombol ditekan berulang tidak lagi memicu perintah ganda (flag busy).
- Terapkan ditolak dengan pesan jelas bila game belum terpasang.
- Callback Handler dibersihkan di onDestroy.
- exec menangkap Throwable, jadi UI tidak terkunci bila terjadi error tak terduga.
- Versi dinaikkan: versionCode 7, versionName 1.6.

## Koreksi atas analisis saya sebelumnya
- Saya sempat menyebut "prompt root tiap dibuka" sebagai celah. Itu tidak benar: prompt hanya muncul di perangkat yang memang di-root, jadi tidak diubah.

## Tidak diubah (disengaja)
- debug.keystore dengan password default tetap ada. Wajar untuk debug, jangan dipakai untuk rilis.
- build.yml, manifest, dan resource tidak berubah.
- Risiko kebijakan: tweak tekstur di game online bisa melanggar aturan Garena.

---
# v1.7 — Panel pengaturan
- Ketuk di mana saja di luar panel pengaturan sekarang menutup panel (sebelumnya harus ketuk ikon gear lagi).
- Ketukan di dalam panel (Reset, Terapkan, slider, toggle, Mode) TIDAK menutup panel.
- Cara kerja: lapisan transparan (scrim) di belakang panel; panel menyerap sentuhannya sendiri.
- Versi: versionCode 8, versionName 1.7.
