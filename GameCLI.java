import java.util.Scanner;

public class GameCLI{
    private GameTime gameTime;

    public GameCLI(GameTime gameTime){
        this.gameTime = gameTime;
    }

    public void startCheatMenu(){
        Scanner scanner = new Scanner(System.in);
        while(true){
            System.out.println("\n===== Cheat Menu ====");
            System.out.println("1. Set Time");
            System.out.println("2. Set Season");
            System.out.println("3. Set Weather");
            System.out.println("4. Set Day");
            System.out.println("5. View Current Time info");
            System.out.println("6. Exit Cheat Menu");
            System.out.println("Choose");
            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1:
                    System.out.println("enter hour (0-23): ");
                    int hour = scanner.nextInt();
                    System.out.println("Enter minute (0-59): ");
                    int minute = scanner.nextInt();
                    gameTime.setTime(hour, minute);
                    break;
                
                case 2:
                    System.out.println(" Enter season(SPRING,SUMMER,FALL,WINTER): ");
                    String seasonString = scanner.nextLine().toUpperCase();
                    try{
                        gameTime.setSeason(Season.valueOf(seasonString));
                    } catch(IllegalArgumentException e) {
                         System.out.println("Invalid season.");
                    }
                    break;
                case 3:
                    System.out.println("Enter weather (SUNNY, RAINY): ");
                    String weatheString = scanner.nextLine().toUpperCase();
                    try {
                        gameTime.setWeather(Weather.valueOf(weatheString));
                    } catch (IllegalArgumentException e) {
                        System.out.println("Invalid Weather.");
                    }
                    break;
                case 4:
                    System.out.println("Enter day: ");
                    int day = scanner.nextInt();
                    if (day >= 1) {
                        gameTime.setDay(day);
                    } else {
                        System.out.println("Invalid day.");
                    }
                    break;
                case 5:
                    System.out.println("\n--- Current Time Info ---");
                    System.out.println("Time    : " + gameTime.getTimeString());
                    System.out.println("Day     : " + gameTime.getCurrentDay());
                    System.out.println("Season  : " + gameTime.getCurrentSeason());
                    System.out.println("Weather : " + gameTime.getCurrentWeather());
                    break;
                case 6:
                    return;
                default:
                    System.out.println("Invalid choice.");
            }
        }
    }
}