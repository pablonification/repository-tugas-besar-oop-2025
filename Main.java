public class Main {
    public static void main(String[] args) {
        GameTime gameTime = new GameTime();
        Thread timThread = new Thread(gameTime);
        timThread.start();
        GameCLI cli = new GameCLI(gameTime);
        cli.startCheatMenu();
    }
}