// public class Time {
//     private int day; 
//     private int hour; 
//     private int minute; 

//     private static final int MINUTES_PER_DAY = 24 * 60;
//     private static final int REAL_SECOND_TO_GAME_MINUTE = 5;

//     public Time() {
//         this.day = 1;
//         this.hour = 6;
//         this.minute = 0;
//     }

//     public void advanceTime(int minutes) {
//         int totalMinutes = hour * 60 + minute + minutes;

//         while (totalMinutes >= MINUTES_PER_DAY) {
//             totalMinutes -= MINUTES_PER_DAY;
//             day++;
//         }

//         hour = totalMinutes / 60;
//         minute = totalMinutes % 60;
//     }

//     public void timeMorning() {
//         // Waktu pagi default: jam 06.00
//         hour = 6;
//         minute = 0;
//         day++;
//     }

//     public boolean isDaytime() {
//         int totalMinutes = hour * 60 + minute;
//         return totalMinutes >= 360 && totalMinutes < 1080; // 06.00 - 17.59
//     }

//     public String getFormatTime() {
//         return String.format("%02d:%02d", hour, minute);
//     }

//     public int getDay() {
//         return day;
//     }

//     // Untuk debugging atau tampilan CLI
//     public void printTimeInfo() {
//         System.out.println("Hari ke-" + day + ", Waktu: " + getFormatTime() + " (" + (isDaytime() ? "Siang" : "Malam") + ")");
//     }
// }
