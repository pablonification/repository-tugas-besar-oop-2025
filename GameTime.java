import java.util.Random;

public class GameTime implements Runnable {
    private int hour;
    private int minute;
    private int totalDays;
    private Season season;
    private Weather weather;

    private static final int DAYS_IN_SEASON = 10;
    private boolean running = true;
    private boolean timeSkip = false;
    private boolean weatherCheat = false;
    private Weather forcedWeather;
    private final Random rand = new Random();

    public GameTime() {
        this.hour = 6;
        this.minute = 0;
        this.totalDays = 1;
        this.season = Season.SPRING;
        generateWeather();
    }

    public void advanceTime(int minutes) {
        minute += minutes;
        while (minute >= 60) {
            minute -= 60;
            hour++;
        }
        while (hour >= 24) {
            nextDay();
        }
    }

    public void nextDay() {
        hour = 6;
        minute = 0;
        totalDays++;
        updateSeason();
        generateWeather();
    }

    private void updateSeason() {
        int seasonIndex = (totalDays - 1) / DAYS_IN_SEASON % 4;
        season = Season.values()[seasonIndex];
    }

    private void generateWeather() {
        if (weatherCheat) {
            weather = forcedWeather;
            return;
        }
        weather = rand.nextBoolean() ? Weather.SUNNY : Weather.RAINY;
    }

    public void setWeatherCheat(Weather w) {
        this.weatherCheat = true;
        this.forcedWeather = w;
    }

    public void disableWeatherCheat() {
        this.weatherCheat = false;
    }

    public void skipTimeTo(int hour, int minute) {
        this.hour = hour;
        this.minute = minute;
        timeSkip = true;
    }

    public boolean isDayTime() {
        return hour >= 6 && hour < 18;
    }

    public boolean isNightTime() {
        return !isDayTime();
    }

    public String getTimeString() {
        return String.format("%02d:%02d", hour, minute);
    }

    public int getCurrentDay() {
        return totalDays;
    }

    public Season getCurrentSeason() {
        return season;
    }

    public Weather getCurrentWeather() {
        return weather;
    }

    public void setTime(int hour, int minute) {
        this.hour = hour;
        this.minute = minute;
        while (this.minute >= 60) {
            this.minute -= 60;
            this.hour++;
        }
        while (this.hour >= 24) {
            nextDay();
        }
    }

    public void setSeason(Season season) {
        this.season = season;
    }

    public void setDay(int day) {
        if (day >= 1) {
            this.totalDays = day;
            updateSeason();
        }
    }

    public void setWeather(Weather weather) {
        this.weather = weather;
    }

    public void stopTime() {
        running = false;
    }

    @Override
    public void run() {
        while (running) {
            try {
                Thread.sleep(1000); // 1 detik real time
                if (!timeSkip) {
                    advanceTime(5); // 5 menit in-game setiap detik
                } else {
                    timeSkip = false;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

