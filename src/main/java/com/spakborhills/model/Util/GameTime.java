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
    public static final int HOURS_IN_DAY = 24; 
    public static final int DAYS_IN_SEASON = 10; 
    public static final int START_HOUR = 6; 
    public static final int DAY_START_MINUTE = 0;
    public static final int NIGHT_START_HOUR = 18; 
    public static final int PASS_OUT_HOUR = 2; 

    private int minute; 
    private int hour;   
    private int dayOfMonth; 
    private Season currentSeason;
    private Weather currentWeather;
    private int currentYear;
    private int totalDaysPlayed;

    private int rainyDaysThisSeason;
    private final Random randomGenerator;
    private boolean isPaused; 

    /**
     * Konstruktor untuk GameTime.
     * Menginisialisasi waktu ke awal permainan (Hari 1, Musim Semi, jam 06:00, Cerah).
     */
    public GameTime() {
        this.minute = DAY_START_MINUTE;
        this.hour = START_HOUR;
        this.dayOfMonth = 1;
        this.currentSeason = Season.SPRING;
        this.currentWeather = Weather.SUNNY; 
        this.currentYear = 1;
        this.totalDaysPlayed = 1; 
        this.randomGenerator = new Random();
        this.rainyDaysThisSeason = 0;
        this.isPaused = false; 
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

    public void setDayOfMonth(int dayOfMonth) { 
        if (dayOfMonth >= 1 && dayOfMonth <= DAYS_IN_SEASON) { 
            this.dayOfMonth = dayOfMonth;
        }
    }

    public void setYear(int year) { 
        if (year > 0) { 
            this.currentYear = year;
        }
    }

    private void determineInitialWeather(){
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
        return hour >= NIGHT_START_HOUR || hour < START_HOUR;
    }

    /**
     * Memeriksa apakah waktu sudah melewati batas tidur paksa (yaitu, 02:00 pagi).
     * Sesuai spesifikasi Halaman 26.
     * @return true jika sudah melewati batas waktu tidur.
     */
    public boolean isPastBedtime() {
        return hour >= PASS_OUT_HOUR && hour < START_HOUR; 
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
            this.rainyDaysThisSeason = 0;
            System.out.println("CHEAT: Musim diubah menjadi: " + this.currentSeason);
        } else {
            System.err.println("CHEAT: Gagal mengubah musim ke nilai yang tidak valid: " + newSeason);
        }
    }

    /**
     * Sets the game time directly (for cheats).
     * @param newHour The hour to set (0-23).
     * @param newMinute The minute to set (0-59).
     */
    public void setTime(int newHour, int newMinute) {
        if (newHour >= 0 && newHour < HOURS_IN_DAY && newMinute >= 0 && newMinute < MINUTES_IN_HOUR) {
            this.hour = newHour;
            this.minute = newMinute;
            System.out.println("CHEAT: Waktu diubah menjadi: " + getTimeString());
        } else {
            System.err.println("CHEAT: Gagal mengubah waktu ke nilai yang tidak valid: " + newHour + ":" + newMinute);
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
        if (isPaused || minutesToAdd <= 0) return; 

        this.minute += minutesToAdd;

        while (this.minute >= MINUTES_IN_HOUR) {
            this.minute -= MINUTES_IN_HOUR;
            this.hour++;
            if (this.hour >= HOURS_IN_DAY) {
                this.hour = 0; 
            }
        }
    }

    /**
     * Sets the pause state of the game time.
     * When paused, the advance() method will not progress time.
     * @param paused true to pause time, false to resume.
     */
    public void setPaused(boolean paused) {
        this.isPaused = paused;
        if (isPaused) {
            System.out.println("GameTime Paused");
        } else {
            System.out.println("GameTime Resumed");
        }
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
                    this.currentSeason = Season.SPRING; 
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
    }

    /**
     * Menentukan cuaca untuk hari berikutnya berdasarkan aturan spesifikasi.
     * "Dalam satu season, Rainy Day minimal terjadi 2 kali." (Halaman 33)
     * @return Weather untuk hari berikutnya.
     */
    private Weather determineNextWeather() {
        int daysRemainingInSeason = DAYS_IN_SEASON - this.dayOfMonth + 1;

        if (this.rainyDaysThisSeason < 2) {
            if (daysRemainingInSeason <= (2 - this.rainyDaysThisSeason)) {
                System.out.println("  (Logika Cuaca: Memastikan minimal 2 hari hujan per musim)");
                return Weather.RAINY;
            }
        }

        // Jika tidak ada paksaan, gunakan probabilitas acak
        // Probabilitas hujan bisa disesuaikan, misal 20% (1 dari 5)
        int rainChanceRoll = randomGenerator.nextInt(5); 
        if (rainChanceRoll == 0) { 
            return Weather.RAINY;
        } else {
            return Weather.SUNNY;
        }
    }
}
