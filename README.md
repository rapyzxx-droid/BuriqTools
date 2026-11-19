# 🎮 BuriqTools

**Aplikasi Membuat Grafik MC Pada Game**

BuriqTools adalah aplikasi Android yang membantu kamu mengubah tampilan grafik game atau aplikasi sesuai keinginanmu. Kamu bebas mengatur sendiri tampilannya, termasuk membuatnya jadi **"buriq"** (pecah/blur/low-quality) supaya game terasa lebih ringan, unik, atau sekadar untuk seru-seruan.

> ⚠️ **Catatan:** Aplikasi ini masih dalam tahap pengembangan awal (v1.0.0). Beberapa fitur mungkin belum sempurna.

---

## 📖 Daftar Isi

- [Tentang Aplikasi](#-tentang-aplikasi)
- [Apa Itu Grafik "Buriq"?](#-apa-itu-grafik-buriq)
- [Fitur](#-fitur)
- [Cara Instal](#-cara-instal)
- [Cara Penggunaan](#-cara-penggunaan)
- [Tips](#-tips)
- [Pertanyaan Umum (FAQ)](#-pertanyaan-umum-faq)
- [Build dari Source Code](#-build-dari-source-code)
- [Kontribusi](#-kontribusi)
- [Disclaimer](#-disclaimer)

---

## 💡 Tentang Aplikasi

Tidak semua orang punya HP dengan spesifikasi tinggi, dan tidak semua orang suka grafik game yang itu-itu saja. BuriqTools dibuat supaya kamu bisa:

- Mengubah **angle/tampilan grafik** pada game atau aplikasi.
- **Mengedit sendiri** pengaturannya sesuai selera, tanpa ribet.
- Membuat tampilan jadi **buriq** untuk performa yang lebih ringan atau gaya yang khas.

Saat ini BuriqTools difokuskan dan diuji untuk game **Free Fire (FF)**.

Aplikasi ini dibuat dengan **Kotlin** (100%) dan ditujukan untuk perangkat Android.

---

## 🌫️ Apa Itu Grafik "Buriq"?

"Buriq" adalah istilah santai untuk tampilan yang **kurang tajam, pecah, atau blur**. Banyak orang sengaja membuat grafik game jadi buriq karena:

1. **Lebih ringan**: beban di HP berkurang, game bisa lebih lancar (FPS lebih stabil).
2. **Hemat baterai dan suhu**: HP tidak cepat panas.
3. **Gaya unik**: tampilan retro/lo-fi yang punya daya tarik tersendiri.
4. **Seru-seruan**: konten, meme, atau sekadar iseng.

Dengan BuriqTools, kamu yang menentukan seberapa buriq tampilannya.

---

## ✨ Fitur

- 🎨 **Ubah grafik game/aplikasi**: sesuaikan tampilan sesuai keinginan.
- ✏️ **Bisa diedit pengguna**: atur sendiri tingkat "buriq" dan pengaturan lainnya.
- 📱 **Ringan dan simpel**: tampilan mudah dipahami, cocok untuk pemula.
- 🔧 **Pengaturan fleksibel**: ubah, simpan, dan ubah lagi kapan saja.
- 🔓 **Mendukung Root**: untuk perangkat yang sudah di-root, akses penuh ke pengaturan.
- 🛡️ **Mendukung Shizuku**: tanpa root pun tetap bisa, cukup aktifkan Shizuku.
- 🆓 **Gratis dan open source**: kode terbuka untuk siapa saja.

> 📝 *Daftar fitur akan terus diperbarui seiring pengembangan.*

---

## 📲 Cara Instal

1. Buka halaman **[Releases](../../releases)** di repo ini.
2. Pilih versi terbaru (tanda **Latest**).
3. Unduh file **`app-debug.apk`** di bagian **Assets**.
4. Buka file APK yang sudah diunduh.
5. Jika muncul peringatan, izinkan **"Instal dari sumber tidak dikenal"** untuk browser atau file manager yang kamu pakai.
6. Tunggu proses instalasi selesai, lalu buka **BuriqTools**.

---

## 🕹️ Cara Penggunaan

1. **Buka BuriqTools** di HP kamu.
2. **Pilih game atau aplikasi** yang ingin diubah grafiknya.
3. **Atur tampilan** sesuai selera, termasuk tingkat buriq-nya.
4. **Simpan pengaturan** kamu.
5. **Jalankan game/aplikasi** dan lihat perubahannya.
6. Tidak cocok? Buka lagi BuriqTools dan **edit ulang** kapan saja.

---

## 🎯 Game yang Didukung

| Game | Status |
|------|--------|
| **Free Fire (FF)** | ✅ Fokus pengembangan dan sudah diuji |
| Game lainnya | 🔜 Belum diuji, dukungan akan ditambah di update berikutnya |

BuriqTools saat ini dikembangkan dan diuji dengan fokus pada **Free Fire**. Hasil di game lain bisa berbeda dan belum tentu berfungsi.

---

## 🔑 Mode Akses: Root atau Shizuku

BuriqTools membutuhkan izin khusus untuk mengubah pengaturan grafik. Kamu bisa memilih salah satu:

### Opsi 1: Root
Cocok untuk perangkat yang sudah di-root (misalnya dengan Magisk).
1. Buka BuriqTools.
2. Pilih mode **Root**.
3. Saat muncul permintaan akses superuser, tap **Izinkan/Grant**.

### Opsi 2: Shizuku (tanpa root)
Cocok untuk perangkat yang belum di-root.
1. Instal aplikasi **Shizuku** dari sumber resmi.
2. Aktifkan Shizuku, lewat **Wireless Debugging** (Android 11+) atau lewat ADB dari komputer.
3. Buka BuriqTools dan pilih mode **Shizuku**.
4. Saat muncul permintaan izin, tap **Izinkan/Allow**.

> 💡 Shizuku perlu diaktifkan ulang setiap HP restart (kecuali perangkat yang sudah di-root).

---

## 🚀 Rencana Pengembangan

BuriqTools akan terus diperbarui. Di update berikutnya akan hadir **fitur-fitur baru**. Pantau halaman **[Releases](../../releases)** untuk versi terbaru, dan beri ⭐ di repo ini supaya tidak ketinggalan.

Punya ide fitur? Sampaikan lewat tab **[Issues](../../issues)**.

---

## 🧠 Tips

- Mulai dari pengaturan ringan dulu, lalu naikkan pelan-pelan sampai ketemu yang pas.
- Catat pengaturan favoritmu supaya mudah diulang.
- Kalau terjadi masalah, kembalikan ke pengaturan awal.
- Pastikan kamu selalu memakai **versi terbaru** dari halaman Releases.

---

## ❓ Pertanyaan Umum (FAQ)

**Apakah aplikasi ini gratis?**
Ya, gratis dan open source.

**Kenapa pakai APK debug?**
Ini versi awal untuk pengujian. Versi rilis yang sudah ditandatangani akan menyusul.

**Apakah aman?**
Kode sumber terbuka, jadi siapa saja bisa memeriksanya. Unduh hanya dari halaman Releases resmi repo ini.

**Ada bug atau error, harus bagaimana?**
Buka tab **[Issues](../../issues)** dan ceritakan masalahmu, sertakan tipe HP, versi Android, dan screenshot kalau ada.

**Harus root?**
Tidak. Kamu bisa memakai Shizuku sebagai alternatif tanpa root.

**Bisa untuk semua game?**
Saat ini fokus dan pengujian ada di **Free Fire (FF)**. Game lain belum diuji, jadi hasilnya bisa berbeda tergantung game dan perangkat.

---

## 🛠️ Build dari Source Code

Kalau ingin membangun sendiri:

1. Pastikan sudah menginstal **Android Studio**.
2. Clone repo ini:
   ```bash
   git clone https://github.com/rapyzxx-droid/BuriqTools.git
   ```
3. Buka folder proyek di Android Studio dan tunggu **Gradle sync** selesai.
4. Jalankan lewat *Run*, atau buat APK lewat *Build → Build APK(s)*.
5. Hasil APK ada di `app/build/outputs/apk/`.

---

## 🤝 Kontribusi

Kontribusi sangat diterima! Caranya:

1. **Fork** repo ini.
2. Buat branch baru: `git checkout -b fitur-baru`
3. Commit perubahan: `git commit -m "Tambah fitur baru"`
4. Push ke branch: `git push origin fitur-baru`
5. Buka **Pull Request**.

Saran, ide fitur, dan laporan bug juga bisa dikirim lewat **Issues**.

---

## ⚠️ Disclaimer

- Aplikasi ini dibuat untuk tujuan **pribadi dan hiburan**.
- Gunakan dengan bijak dan patuhi aturan game atau aplikasi yang kamu pakai.
- Pengembang tidak bertanggung jawab atas masalah yang timbul akibat penggunaan aplikasi ini.

---

## 👤 Pembuat

Dibuat oleh **[rapyzxx-droid](https://github.com/rapyzxx-droid)**.

Kalau aplikasi ini bermanfaat, jangan lupa kasih ⭐ di repo ini!
