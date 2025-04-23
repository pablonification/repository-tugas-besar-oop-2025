# Spakbor Hills - Tugas Besar IF2010 Pemrograman Berorientasi Objek STI

Dokumen ini bertujuan untuk digunakan sebagai panduan dalam proses setup, build, testing, dan kontribusi pada proyek ini.

**Daftar Isi:**

1.  [Prerequisites](#1-prerequisites)
2.  [Setup Proyek](#2-setup-proyek)
3.  [Struktur Proyek](#3-struktur-proyek)
4.  [Build Proyek](#4-build-proyek)
5.  [Menjalankan Proyek](#5-menjalankan-proyek)
6.  [Menjalankan Test](#6-menjalankan-test)
7.  [Prinsip & Desain](#7-prinsip--desain)
8.  [Dependensi Utama](#8-dependensi-utama)
9.  [Konfigurasi (Jika Ada)](#9-konfigurasi-jika-ada)
10. [Tim Pengembang](#10-tim-pengembang)

---

## 1. Prerequisites

Pastikan perangkat Anda telah terinstall software berikut:

*   **Java Development Kit (JDK):** Versi `24`.
*   **Git:** Untuk version control.
*   **Gradle:** Build automation tool yang kita gunakan.
    *   Wrapper (`gradlew` atau `gradlew.bat`) sudah termasuk dalam repo, tidak perlu install manual.

---

## 2. Setup Proyek

1.  **Clone Repository:**
    ```bash
    git clone https://github.com/pablonification/repository-tugas-besar-oop-2025.git
    cd repository-tugas-besar-oop-2025
    ```
2.  **Import ke IDE:**
    *   Buka IDE favorit Anda (IntelliJ IDEA, Eclipse, VS Code).
    *   Import proyek sebagai proyek Gradle. IDE biasanya akan otomatis mendeteksi dan mengkonfigurasi berdasarkan file `build.gradle`.

---

## 3. Struktur Proyek

Berikut adalah gambaran umum struktur direktori proyek:

```
.
├── .git/               # Direktori internal Git
├── .gitignore          # File yang diabaikan Git
├── build/              # Output build (Gradle) - JANGAN DI-COMMIT
├── gradle/             # Gradle wrapper files (jika pakai Gradle)
├── src/
│   ├── main/
│   │   ├── java/       # Kode sumber utama aplikasi
│   │   │   └── com/
│   │   │       └── spakborhills/
│   │   │           ├── Main.java       # Entry point aplikasi
│   │   │           ├── model/          # Kelas-kelas entitas (Player, NPC, Item, etc.)
│   │   │           ├── view/           # Kelas-kelas untuk display CLI
│   │   │           ├── controller/     # Kelas-kelas untuk logika game & flow
│   │   │           ├── util/           # Kelas-kelas utility
│   │   │           └── exception/      # Custom exceptions
│   │   └── resources/  # File resource non-kode (jika ada, misal: data awal, config)
│   └── test/
│       ├── java/       # Kode sumber untuk testing (JUnit)
│       │   └── com/
│       │       └── spakborhills/
│       │           └── ...             # Test classes mengikuti struktur main
│       └── resources/  # File resource untuk testing
├── build.gradle        # File konfigurasi build Gradle (jika pakai Gradle)
├── gradlew             # Gradle wrapper script (Linux/macOS)
├── gradlew.bat         # Gradle wrapper script (Windows)
└── README.md           # Dokumentasi ini
```

---

## 4. Build Proyek

Gunakan perintah berikut di terminal dari *root* direktori proyek untuk mengkompilasi kode dan membuat *executable JAR*:

*   **Jika menggunakan Gradle:**
    ```bash
    # Untuk Linux/macOS
    ./gradlew build

    # Untuk Windows
    gradlew.bat build
    ```
    Output JAR biasanya berada di `build/libs/`.

*   **Jika menggunakan Maven:**
    ```bash
    mvn package
    ```
    Output JAR biasanya berada di `target/`.

JAR yang dihasilkan akan berisi semua dependensi yang diperlukan.

---

## 5. Menjalankan Proyek

Setelah berhasil melakukan build, jalankan game menggunakan perintah berikut:

```bash
gradlew run
```

---

## 6. Menjalankan Test

Untuk menjalankan unit test yang telah dibuat:

*   **Jika menggunakan Gradle:**
    ```bash
    # Untuk Linux/macOS
    ./gradlew test

    # Untuk Windows
    gradlew.bat test
    ```
    Laporan test biasanya berada di `build/reports/tests/test/index.html`.

*   **Jika menggunakan Maven:**
    ```bash
    mvn test
    ```
    Laporan test biasanya berada di `target/surefire-reports/`.

---

## 7. Prinsip & Desain

*   **SOLID:** Kami berusaha menerapkan kelima prinsip SOLID dalam desain kode.
<!-- *   **Design Patterns:** Sesuai spesifikasi, kami wajib mengimplementasikan **minimal 3** Design Pattern. Pattern yang telah diimplementasikan:
    *   `Creational Pattern` - Digunakan pada `[Bagian/Kelas terkait]` untuk `[Tujuan Penggunaan]`
    *   `[Nama Pattern 2]` - Digunakan pada `[Bagian/Kelas terkait]` untuk `[Tujuan Penggunaan]`
    *   `[Nama Pattern 3]` - Digunakan pada `[Bagian/Kelas terkait]` untuk `[Tujuan Penggunaan]`
    *   `[Tambahkan pattern lain jika ada]` -->
    <!-- Tim: Update daftar ini seiring implementasi pattern -->
*   **Logging:** Kami menggunakan `SLF4J dengan Logback` untuk logging. Level logging utama adalah INFO, gunakan DEBUG untuk detail development.
*   **Exception Handling:** Gunakan exception handling yang sesuai. Buat custom exception jika diperlukan untuk kasus spesifik game.

---

## 8. Dependensi Utama

*   **Java Development Kit (JDK):** `[Versi 21]`
*   **Gradle:** Build Tool
*   **JUnit 5:** Framework untuk Unit Testing
*   **SLF4J API, Logback/Log4j2 Core:** Framework Logging
*   `[Tambahkan dependensi lain jika ada]`

<!-- Tim: Pastikan daftar dependensi ini sesuai dengan file build.gradle atau pom.xml -->

---

## 9. Konfigurasi (Jika Ada)

<!-- *   `[Jelaskan jika ada file konfigurasi eksternal, misal: untuk save/load, data game awal, dll.]`
*   `[Jelaskan jika ada environment variable yang perlu di-set.]` -->
*   *(Saat ini belum ada konfigurasi eksternal yang signifikan)*

<!-- Tim: Update bagian ini jika ada penambahan fitur yang memerlukan konfigurasi -->

---

## 10. Tim Pengembang

*   `[A. Nurul Aqeela Amin]`
*   `[Arqila Surya Putra]`
*   `[Ali Syauqie]`
*   `[Daffa Athalla Rajasa]`

---

*Dokumen ini terakhir diperbarui pada: [19 April 2025]*
