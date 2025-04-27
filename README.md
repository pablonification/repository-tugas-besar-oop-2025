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
<!-- 8.  [Prinsip & Desain](#8-prinsip--desain) -->
8.  [Dependensi Utama](#8-dependensi-utama)
<!-- 10. [Konfigurasi (Jika Ada)](#10-konfigurasi-jika-ada) -->
9. [Anggota Kelompok](#9-anggota-kelompok)

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
│   │   │           ├── view/           # Kelas-kelas untuk display CLI (misal: MapView, MenuView)
│   │   │           ├── controller/     # Kelas-kelas untuk logika game, flow, & input handling
│   │   │           ├── util/           # Kelas-kelas utility (misal: TimeUtil, Randomizer)
│   │   │           └── exception/      # Custom exceptions (misal: InsufficientEnergyException)
│   │   └── resources/  # File resource non-kode (jika ada, misal: data item, resep)
│   └── test/
│       ├── java/       # Kode sumber untuk testing (JUnit)
│       │   └── com/
│       │       └── spakborhills/
│       │           └── ...             # Test classes mengikuti struktur main
│       └── resources/  # File resource untuk testing (jika perlu)
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

Perintah ini akan menjalankan kompilasi, menjalankan test (jika dikonfigurasi), dan mem-package aplikasi. Output JAR biasanya berada di `build/libs/nama-Project-versi.jar`. JAR yang dihasilkan akan berisi semua dependensi yang diperlukan (*fat JAR* jika dikonfigurasi demikian di `build.gradle`).

---

## 6. Running Project

Setelah berhasil melakukan build, atau untuk menjalankan langsung dari source code menggunakan Gradle:

```bash
# Untuk Linux/macOS
./gradlew run

# Untuk Windows
gradlew.bat run
```

Perintah `run` akan mengkompilasi kode jika perlu dan menjalankan `Main.java` yang telah ditentukan di `build.gradle`.

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

<!-- ## 8. Prinsip & Desain

Kami berkomitmen untuk menerapkan praktik rekayasa perangkat lunak yang baik dalam Project ini:

*   **SOLID:** Berusaha menerapkan kelima prinsip SOLID (Single Responsibility, Open/Closed, Liskov Substitution, Interface Segregation, Dependency Inversion) untuk menghasilkan kode yang modular, fleksibel, dan mudah dipelihara.
*   **Design Patterns:** Sesuai spesifikasi, kami wajib mengimplementasikan **minimal 3** Design Pattern. Pattern yang direncanakan/telah diimplementasikan:
    *   `[Nama Pattern 1]` - *[Status: Direncanakan/Implementasi]* - Digunakan pada `[Bagian/Kelas terkait]` untuk `[Tujuan Penggunaan, misal: mengelola state game, membuat objek item]`
    *   `[Nama Pattern 2]` - *[Status: Direncanakan/Implementasi]* - Digunakan pada `[Bagian/Kelas terkait]` untuk `[Tujuan Penggunaan]`
    *   `[Nama Pattern 3]` - *[Status: Direncanakan/Implementasi]* - Digunakan pada `[Bagian/Kelas terkait]` untuk `[Tujuan Penggunaan]`
    *   *(Tambahkan pattern lain jika ada)* -->
    
<!-- *   **Logging:** Menggunakan `SLF4J` sebagai API logging dengan implementasi `Logback` (atau `Log4j2`). Ini membantu dalam proses debugging dan pemantauan. Konfigurasi logging ada di `src/main/resources/logback.xml` (atau file konfigurasi yang sesuai).
*   **Exception Handling:** Menerapkan exception handling yang tepat untuk menangani kondisi error yang mungkin terjadi (misal: input tidak valid, energi tidak cukup). Custom exceptions dibuat di package `exception` untuk kasus-kasus spesifik game.
*   **Concurrency:** Menerapkan konsep concurrency sesuai kebutuhan spesifikasi, terutama pada aksi-aksi yang berjalan paralel dengan waktu game (jika ada, seperti memasak). Penggunaan `synchronized`, `Lock`, atau struktur data concurrent akan didokumentasikan di kode. -->

---

## 8. Dependensi Utama

Dependensi utama Project ini dikelola oleh Gradle dan didefinisikan dalam file `build.gradle`:

*   **Java Development Kit (JDK):** `21` (atau versi yang disepakati tim)
*   **Gradle:** Build Tool (via Wrapper)
*   **JUnit 5 (Jupiter Engine):** Framework untuk Unit Testing
*   **SLF4J API:** Abstraksi Logging
*   **Logback Classic / Log4j2 Core:** Implementasi Logging
<!-- *   `[Tambahkan dependensi lain jika ada, misal: library JSON untuk save/load seperti Jackson atau Gson]` -->

<!-- Tim: Pastikan daftar dependensi ini selalu sinkron dengan file build.gradle -->

<!-- ---

## 10. Konfigurasi (Jika Ada)

*   *(Saat ini belum ada konfigurasi eksternal yang signifikan yang perlu diatur oleh pengguna akhir untuk menjalankan game dasar).*
*   *(Fitur save/load mungkin akan menggunakan file di lokasi tertentu, akan didokumentasikan di sini jika sudah diimplementasikan).* -->

<!-- Tim: Update bagian ini jika ada penambahan fitur yang memerlukan konfigurasi (misal: path file save, level logging default) -->

---

## 9. Anggota Kelompok

Project ini dikembangkan oleh Kelompok `2` - K4:

*   `A. Nurul Aqeela Amin` - `18223019`
*   `Arqila Surya Putra` - `18223047`
*   `Ali Syauqie` - `18223045`
*   `Daffa Athalla Rajasa` - `18223053`

---