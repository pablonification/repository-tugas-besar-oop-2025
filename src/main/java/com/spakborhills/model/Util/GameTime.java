// STUB

package com.spakborhills.model.Util;

import com.spakborhills.model.Enum.Season;
import com.spakborhills.model.Enum.Weather;
import java.util.Random; // Untuk cuaca acak sederhana

public class GameTime {
    private int day;
    private Season season;
    private Weather weather;
    // Tambahkan atribut jam/menit jika perlu testing waktu spesifik
    private int hour;
    // private int minute;

    public GameTime() {
        this.day = 1;
        this.season = Season.SPRING;
        this.weather = Weather.SUNNY; // Cuaca awal
        // this.hour = 6;
        // this.minute = 0;
        System.out.println("GameTime diinisialisasi: Hari 1, Spring, Sunny");
    }

    public int getCurrentDay() { return day; }

    public Season getCurrentSeason() { return season; }
    public Weather getCurrentWeather() { return weather; }
    public int getHour() { return hour; }
    // public int getMinute() { return minute; }

    // Stub: Logika sebenarnya lebih kompleks (ganti musim, tahun, cuaca acak)
    public void nextDay() {
        day++;
        // Logika ganti musim sederhana
        if (day > 10) { // Asumsi 10 hari per musim
            day = 1;
            switch (season) {
                case SPRING: season = Season.SUMMER; break;
                case SUMMER: season = Season.FALL; break;
                case FALL:   season = Season.WINTER; break;
                case WINTER: season = Season.SPRING; break; // Kembali ke Spring
                default: season = Season.SPRING; // Safety
            }
        }
        // Cuaca acak sederhana (bisa lebih kompleks sesuai spek)
        this.weather = (new Random().nextInt(5) == 0) ? Weather.RAINY : Weather.SUNNY; // 1/5 kemungkinan hujan
        System.out.println("Stub: Maju ke hari berikutnya.");
    }

    // Stub: Logika advance waktu dalam hari
    public void advance(int minutes) {
        System.out.println("Stub: Waktu maju " + minutes + " menit.");
        // Implementasi detail nanti
    }
}