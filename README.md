# Spakbor Hills - Tugas Besar IF2010 Pemrograman Berorientasi Objek STI

Selamat datang di repositori Spakbor Hills! Project ini adalah implementasi permainan yang menerapkan spesifikasi dari Tugas Besar mata kuliah IF2010 - Pemrograman Berorientasi Objek. Tujuan utama Project ini adalah menerapkan konsep-konsep fundamental OOP dan desain perangkat lunak yang baik dalam pengembangan sebuah aplikasi game.

Dokumen ini bertujuan untuk digunakan sebagai panduan dalam proses setup, build, testing, dan kontribusi pada Project ini.

**Daftar Isi:**

1.  [Deskripsi Project](#1-deskripsi-Project)
2.  [Prerequisites](#2-prerequisites)
3.  [Setup Project](#3-setup-Project)
4.  [Struktur Project](#4-struktur-Project)
5.  [Build Project](#5-build-Project)
6.  [Running Project](#6-running-Project)
7.  [Running Test](#7-running-test)
8.  [Prinsip & Desain](#8-prinsip--desain)
9.  [Dependensi Utama](#9-dependensi-utama)
10. [Anggota Kelompok](#10-anggota-kelompok)

---

## 1. Deskripsi Project

**Spakbor Hills** adalah game simulasi pertanian yang terinspirasi dari game klasik seperti Stardew Valley. Pemain akan mengelola sebuah pertanian virtual, berinteraksi dengan penduduk desa (NPC), menanam tanaman, memancing, memasak, dan mencapai tujuan tertentu dalam permainan. Game ini dikembangkan menggunakan bahasa Java dengan fokus pada penerapan prinsip-prinsip Pemrograman Berorientasi Objek (OOP) seperti *Inheritance*, *Polymorphism*, *Encapsulation*, *Abstraction*, serta konsep *Generics*, *Exceptions*, dan *Concurrency*.

---

## 2. Prerequisites

Pastikan perangkat Anda telah terinstall software berikut:

*   **Java Development Kit (JDK):** Versi `21`.
*   **Git:** Untuk version control.
*   **Gradle:** Build automation tool yang kita gunakan.
    *   *Wrapper* (`gradlew` atau `gradlew.bat`) sudah termasuk dalam repo, sehingga instalasi Gradle manual **tidak diperlukan**. Cukup pastikan JDK terpasang dengan benar.

---

## 3. Setup Project

1.  **Clone Repository:**
    ```bash
    git clone https://github.com/pablonification/repository-tugas-besar-oop-2025.git
    cd repository-tugas-besar-oop-2025
    ```
2.  **Import ke IDE:**
    *   Buka IDE.
    *   Import Project sebagai Project Gradle. IDE biasanya akan otomatis mendeteksi file `build.gradle` dan mengkonfigurasi dependensi serta struktur Project. Tunggu hingga proses sinkronisasi Gradle selesai.

---

## 4. Struktur Project

Berikut adalah gambaran umum struktur direktori Project:

```
.
├── .git/               # Direktori internal Git
├── .gitignore          # File yang diabaikan Git
├── build/              # Output build (Gradle) - JANGAN DI-COMMIT
├── gradle/             # File-file Gradle wrapper
├── src/
│   ├── main/
│   │   ├── java/       # Kode sumber utama aplikasi
│   │   │   └── com/
│   │   │       └── spakborhills/
│   │   │           ├── Main.java       # Entry point aplikasi
│   │   │           ├── model/          # Kelas-kelas entitas (Player, NPC, Item, Farm, Tile, etc.)
│   │   │           ├── view/           # Kelas-kelas untuk display GUI (GamePanel, GameFrame)
│   │   │           ├── controller/     # Kelas-kelas untuk logika game, flow, & input handling
│   │   │           └── util/           # Kelas-kelas utility (TimeUtil, Randomizer, etc.)
│   │   └── resources/  # File resource (assets, musik, gambar)
│   └── test/
│       ├── java/       # Kode sumber untuk testing (JUnit)
│       │   └── com/
│       │       └── spakborhills/
│       │           └── ...             # Test classes mengikuti struktur main
│       └── resources/  # File resource untuk testing
├── build.gradle        # File konfigurasi build Gradle
├── gradlew             # Gradle wrapper script (Linux/macOS)
├── gradlew.bat         # Gradle wrapper script (Windows)
└── README.md           # Dokumentasi ini
```

---

## 5. Build Project

Gunakan perintah berikut di terminal dari *root* direktori Project untuk mengkompilasi kode dan membuat *executable JAR*:

```bash
# Untuk Linux/macOS
./gradlew build

# Untuk Windows
gradlew.bat build
```

Perintah ini akan menjalankan kompilasi, menjalankan test (jika dikonfigurasi), dan mem-package aplikasi. Output JAR biasanya berada di `build/libs/nama-Project-versi.jar`. JAR yang dihasilkan akan berisi semua dependensi yang diperlukan (*fat JAR*).

---

## 6. Running Project

Setelah berhasil melakukan build, atau untuk menjalankan langsung dari source code menggunakan Gradle:

```bash
# Untuk Linux/macOS
./gradlew run

# Untuk Windows
gradlew.bat run
```

Perintah `run` akan mengkompilasi kode jika perlu dan menjalankan `Main.java`.

---

## 7. Running Test

Untuk menjalankan unit test yang telah dibuat menggunakan JUnit:

```bash
# Untuk Linux/macOS
./gradlew test

# Untuk Windows
gradlew.bat test
```

Laporan hasil test akan digenerate oleh Gradle dan biasanya dapat ditemukan di `build/reports/tests/test/index.html`. Buka file HTML ini di browser untuk melihat detail hasil test.

---

## 8. Prinsip & Desain

Kami telah menerapkan praktik rekayasa perangkat lunak yang baik dalam Project ini:

*   **SOLID:** Menerapkan kelima prinsip SOLID untuk menghasilkan kode yang modular, fleksibel, dan mudah dipelihara.
*   **Design Patterns:** Implementasi Design Pattern yang digunakan:
    *   `MVC (Model-View-Controller)` - Digunakan untuk memisahkan logika game (Model), tampilan (View), dan kontrol input (Controller)
    *   `State Pattern` - Digunakan untuk mengelola state game (IN_GAME, PAUSE_MENU, STORE_UI, dll)
    *   `Observer Pattern` - Digunakan untuk sistem event handling dan notifikasi antar komponen
    *   `Factory Pattern` - Digunakan untuk pembuatan objek Item dan GameObject
    *   `Singleton Pattern` - Digunakan untuk manajemen game state dan resources
    
*   **Logging:** Menggunakan `SLF4J` sebagai API logging dengan implementasi `Logback` untuk debugging dan pemantauan.
*   **Exception Handling:** Menerapkan exception handling yang tepat untuk menangani kondisi error yang mungkin terjadi.
*   **Concurrency:** Menerapkan konsep concurrency pada sistem waktu game dan animasi.

---

## 9. Dependensi Utama

Dependensi utama Project ini dikelola oleh Gradle dan didefinisikan dalam file `build.gradle`:

*   **Java Development Kit (JDK):** `24`
*   **Gradle:** Build Tool (via Wrapper)
*   **JUnit 5 (Jupiter Engine):** Framework untuk Unit Testing
*   **SLF4J API:** Abstraksi Logging
*   **Logback Classic:** Implementasi Logging
*   **Java Sound API:** Untuk sistem audio game

---

## 10. Anggota Kelompok

Project ini dikembangkan oleh Kelompok `2` - K4:

*   `A. Nurul Aqeela Amin` - `18223019`
*   `Arqila Surya Putra` - `18223047`
*   `Ali Syauqie` - `18223045`
*   `Daffa Athalla Rajasa` - `18223053`

---