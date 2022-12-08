import java.io.IOException;
import java.util.Random;
import java.util.Scanner;

/**
 * The class creates the engine
 *
 * @author tstoehr
 */
public class Connection {

  public static Engine engine;

  /**
   * Create a name for the engine, creates it and runs it
   *
   * @param args input arguments from the console
   * @author tstoehr
   */
  public static void main(String[] args) {
    //Create a random Number between 0 and 1000000 for the signature of the engine
    Scanner inputArguments = new Scanner(System.in);
    System.out.println("Enter IP: ");
    String serverIp = inputArguments.nextLine();
    System.out.println("Enter Port: ");
    String serverPort = inputArguments.nextLine();
    System.out.println("Enter Username: ");
    String engineUsername = inputArguments.nextLine();

    engine = new Engine(serverIp, Integer.parseInt(serverPort), engineUsername);
    //TODO change input variables
    try {
      engine.run();
    } catch (IOException e) {
      System.out.println("Unexpected error: " + e);
      System.exit(0);
    }
  }
}
