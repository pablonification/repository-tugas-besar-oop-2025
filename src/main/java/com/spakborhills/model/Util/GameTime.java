package com.spakborhills.model.Util; // Atau package yang sesuai

import com.spakborhills.model.Enum.Season;
import com.spakborhills.model.Enum.Weather;
import java.util.Random;

/**
 * Mengelola waktu, hari, musim, dan cuaca dalam game.
 * Berdasarkan spesifikasi Halaman 11 (atribut Farm) dan Halaman 33 (detail Waktu, Musim, Cuaca).
 */
public class GameTime {

    public static final int MINUTES_IN_HOUR = 60;
    public static final int HOURS_IN_DAY = 24; // 0-23
    public static final int DAYS_IN_SEASON = 10; 
    public static final int START_HOUR = 6; // Game dimulai jam 06:00
    public static final int DAY_START_MINUTE = 0;
    public static final int NIGHT_START_HOUR = 18; // Malam dimulai jam 18:00 (Halaman 33)
    public static final int PASS_OUT_HOUR = 2; // Jam 02:00 pagi, pemain otomatis tidur (Halaman 26)

    private int minute; // 0-59
    private int hour;   // 0-23 (06:00 - 05:59 siklusnya)
    private int dayOfMonth; // Hari dalam musim (1-10)
    private Season currentSeason;
    private Weather currentWeather;
    private int currentYear;
    private int totalDaysPlayed;

    private int rainyDaysThisSeason;
    private final Random randomGenerator;

    /**
     * Konstruktor untuk GameTime.
     * Menginisialisasi waktu ke awal permainan (Hari 1, Musim Semi, jam 06:00, Cerah).
     */
    public GameTime() {
        this.minute = DAY_START_MINUTE;
        this.hour = START_HOUR;
        this.dayOfMonth = 1;
        this.currentSeason = Season.SPRING;
        this.currentWeather = Weather.SUNNY; // Cuaca awal default
        this.currentYear = 1;
        this.totalDaysPlayed = 1; // Mulai dari hari pertama
        this.randomGenerator = new Random();
        this.rainyDaysThisSeason = 0;
        determineInitialWeather();
        System.out.println("GameTime diinisialisasi: Hari " + dayOfMonth + " " + currentSeason +
                           ", " + String.format("%02d:%02d", hour, minute) + ", Cuaca: " + currentWeather);
    }

    public int getMinute() { return minute; }
    public int getHour() { return hour; }
    public int getCurrentDay() { return dayOfMonth; } 
    public Season getCurrentSeason() { return currentSeason; }
    public Weather getCurrentWeather() { return currentWeather; }
    public int getTotalDaysPlayed() { return totalDaysPlayed; } 
    public int getCurrentYear() { return currentYear; }

    private void determineInitialWeather(){
        // Cuaca awal bisa SUNNY, atau langsung terapkan logika acak
        // Untuk memastikan hari pertama bisa hujan jika beruntung:
        this.currentWeather = determineNextWeather();
        if (this.currentWeather == Weather.RAINY) {
            this.rainyDaysThisSeason++;
        }
    }

    /**
     * Mengembalikan representasi string dari waktu saat ini (HH:MM).
     * @return String waktu dalam format HH:MM.
     */
    public String getTimeString() {
        return String.format("%02d:%02d", hour, minute);
    }

    /**
     * Memeriksa apakah saat ini adalah waktu siang (06:00 - 17:59).
     * Sesuai spesifikasi Halaman 33.
     * @return true jika siang hari, false jika tidak.
     */
    public boolean isDayTime() {
        return hour >= START_HOUR && hour < NIGHT_START_HOUR;
    }

    /**
     * Memeriksa apakah saat ini adalah waktu malam (18:00 - 05:59).
     * Sesuai spesifikasi Halaman 33.
     * @return true jika malam hari, false jika tidak.
     */
    public boolean isNightTime() {
        // Malam adalah dari jam 18:00 hingga 05:59 keesokan harinya
        return hour >= NIGHT_START_HOUR || hour < START_HOUR;
    }

    /**
     * Memeriksa apakah waktu sudah melewati batas tidur paksa (yaitu, 02:00 pagi).
     * Sesuai spesifikasi Halaman 26.
     * @return true jika sudah melewati batas waktu tidur.
     */
    public boolean isPastBedtime() {
        return hour >= PASS_OUT_HOUR && hour < START_HOUR; // Antara jam 2 pagi sampai sebelum jam 6 pagi
    }

    // Cheats setter
    /**
     * Mengatur cuaca saat ini. Biasanya dipanggil oleh nextDay() atau event khusus.
     * @param weather Cuaca baru.
     */
    public void setWeather(Weather weather) {
        if (weather != null) {
            this.currentWeather = weather;
            System.out.println("Cuaca diubah menjadi: " + this.currentWeather);
        }
    }

    /**
     * Mengatur musim saat ini secara manual (untuk cheat).
     * Juga mereset rainyDaysThisSeason karena musim berubah.
     * @param newSeason Musim baru.
     */
    public void setSeason(Season newSeason) {
        if (newSeason != null && newSeason != Season.ANY) {
            this.currentSeason = newSeason;
            this.rainyDaysThisSeason = 0; // Reset counter hujan saat musim diubah manual
            // Pertimbangkan apakah dayOfMonth juga perlu direset ke 1 atau dibiarkan
            // Untuk cheat, mungkin lebih baik membiarkannya agar tidak terlalu disruptif
            // atau tambahkan parameter lain jika perlu kontrol lebih.
            System.out.println("CHEAT: Musim diubah menjadi: " + this.currentSeason);
        } else {
            System.err.println("CHEAT: Gagal mengubah musim ke nilai yang tidak valid: " + newSeason);
        }
    }

    /**
     * Memajukan waktu game sebanyak menit tertentu.
     * Akan menangani pergantian jam dan hari jika diperlukan.
     * Sesuai spesifikasi Halaman 33: "1 detik di dunia nyata = 5 menit di dunia permainan".
     * Controller akan memanggil ini berdasarkan tick game.
     *
     * @param minutesToAdd Jumlah menit yang akan ditambahkan.
     * @return true jika penambahan menit ini menyebabkan pergantian hari, false jika tidak.
     */
    public void advance(int minutesToAdd) {
        if (minutesToAdd <= 0) return;

        this.minute += minutesToAdd;
        // boolean dayChanged = false;

        while (this.minute >= MINUTES_IN_HOUR) {
            this.minute -= MINUTES_IN_HOUR;
            this.hour++;
            if (this.hour >= HOURS_IN_DAY) {
                this.hour = 0; // Kembali ke jam 00:00
            }
        }
        // System.out.println("Waktu maju ke: " + getTimeString()); // Feedback
        // return dayChanged;
    }

    /**
     * Memproses transisi ke hari berikutnya.
     * Mereset jam, menambah hari, menangani pergantian musim/tahun, dan menentukan cuaca baru.
     */
    public void nextDay() {
        this.totalDaysPlayed++;
        this.dayOfMonth++;

        if (this.dayOfMonth > DAYS_IN_SEASON) {
            this.dayOfMonth = 1;
            this.rainyDaysThisSeason = 0;
            // Ganti musim
            switch (this.currentSeason) {
                case SPRING: this.currentSeason = Season.SUMMER; break;
                case SUMMER: this.currentSeason = Season.FALL; break;
                case FALL:   this.currentSeason = Season.WINTER; break;
                case WINTER: 
                    this.currentSeason = Season.SPRING;
                    this.currentYear++;
                    System.out.println("Selamat Tahun Baru! Memasuki Tahun ke-" + this.currentYear);
                    break;
                case ANY: 
                    System.err.println("ERROR: Musim ANY tidak valid untuk musim aktif.");
                    this.currentSeason = Season.SPRING; // Fallback
                    break;
            }
            System.out.println("Musim berganti menjadi: " + this.currentSeason);
        }

        // Reset waktu ke awal hari
        this.hour = START_HOUR;
        this.minute = DAY_START_MINUTE;

        this.currentWeather = determineNextWeather();
        if (this.currentWeather == Weather.RAINY) {
            this.rainyDaysThisSeason++;
        }

        System.out.println("Memulai Hari Baru: Tahun " + currentYear + ", Hari " + dayOfMonth + " " + currentSeason +
                           ", " + getTimeString() + ", Cuaca: " + currentWeather +
                           " (Hujan musim ini: " + rainyDaysThisSeason + ")");

        // Tentukan cuaca untuk hari baru (Halaman 33)
        // "Dalam satu season, Rainy Day minimal terjadi 2 kali."
        // Logika ini bisa lebih kompleks. Untuk sekarang, acak sederhana.
        // Jika ingin memastikan minimal 2 hari hujan, perlu state tambahan untuk melacak.
        // int rainChance = randomGenerator.nextInt(5); // 0-4, jadi 1/5 = 20% kemungkinan hujan
        // if (rainChance == 0) { // Atau logika lain untuk memastikan minimal 2 hari hujan per musim
        //     this.currentWeather = Weather.RAINY;
        // } else {
        //     this.currentWeather = Weather.SUNNY;
        // }
        // System.out.println("Memulai hari baru: Hari " + dayOfMonth + " " + currentSeason + ", " + getTimeString() + ", Cuaca: " + currentWeather);
    }

    /**
     * Menentukan cuaca untuk hari berikutnya berdasarkan aturan spesifikasi.
     * "Dalam satu season, Rainy Day minimal terjadi 2 kali." (Halaman 33)
     * @return Weather untuk hari berikutnya.
     */
    private Weather determineNextWeather() {
        int daysRemainingInSeason = DAYS_IN_SEASON - this.dayOfMonth + 1; // +1 karena hari ini belum selesai

        // Jika sisa hari sedikit dan target hujan belum tercapai, paksa hujan
        if (this.rainyDaysThisSeason < 2) {
            if (daysRemainingInSeason <= (2 - this.rainyDaysThisSeason)) {
                // Contoh: sisa 1 hari, belum hujan sama sekali (2-0=2), 1 <= 2 -> paksa hujan
                // Contoh: sisa 1 hari, sudah hujan 1x (2-1=1), 1 <= 1 -> paksa hujan
                // Contoh: sisa 2 hari, belum hujan sama sekali (2-0=2), 2 <= 2 -> paksa hujan
                System.out.println("  (Logika Cuaca: Memastikan minimal 2 hari hujan per musim)");
                return Weather.RAINY;
            }
        }

        // Jika tidak ada paksaan, gunakan probabilitas acak
        // Probabilitas hujan bisa disesuaikan, misal 20% (1 dari 5)
        int rainChanceRoll = randomGenerator.nextInt(5); // Angka 0-4
        if (rainChanceRoll == 0) { // 20% chance
            return Weather.RAINY;
        } else {
            return Weather.SUNNY;
        }
    }

}
