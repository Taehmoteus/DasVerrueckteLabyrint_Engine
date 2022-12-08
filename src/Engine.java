import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.util.Set;
import org.json.JSONObject;

/**
 * The Class contains the Engine
 *
 * @author tstoehr
 */
public class Engine {

  public boolean checkServerSocket = false;
  public BufferedWriter serverOutMessage;
  public BufferedReader serverInMessage;
  public String serverIP;
  public int serverPort;
  public String engineUsername;
  public Socket engineSocket;
  public String line;
  int timeIntervalCheckServerSocket = 100;


  /**
   * Constructor of the Engine
   *
   * @param ip       of the server
   * @param port     of the server
   * @param username of the engine
   * @author tstoehr
   */
  public Engine(String ip, int port, String username) {
    this.serverIP = ip;
    this.serverPort = port;
    this.engineUsername = username;
  }

  /**
   * Check if there is any chance to connect the server with the given ip and port
   *
   * @param ip   of the server
   * @param port of the server
   * @param time interval of checking the server
   * @author tstoehr
   */
  public void checkForServer(String ip, int port, int time) {
    //Loop three times for checking the connection to the server
    for (byte i = 0; i < 3; i++) {
      try {
        engineSocket = new Socket(ip, port);
        if (engineSocket.isConnected()) {
          System.out.println("Server socket connected");
          checkServerSocket = true;
          break;
        }
      } catch (UnknownHostException e) {
        System.out.println("Unknown Host");
      } catch (ConnectException e) {
        System.out.println("Error connection to Server");
      } catch (IOException e) {
        System.out.println("Unexpected Error: " + e);
      }
      try {
        //Sleep for time milliseconds and try again
        Thread.sleep(time);
      } catch (InterruptedException e) {
        System.out.println("Unexpected error: " + e);
      }
    }
    if (!checkServerSocket) {
      System.out.println("Cannot find an existing Server");
      System.exit(0);
    }
  }

  /**
   * The "main" methode of the engine
   *
   * @throws IOException if there is an error
   * @author tstoehr
   */
  public void run() throws IOException {
    checkForServer(serverIP, serverPort, timeIntervalCheckServerSocket);
    serverOutMessage = new BufferedWriter(new OutputStreamWriter(engineSocket.getOutputStream()));
    serverInMessage = new BufferedReader(new InputStreamReader(engineSocket.getInputStream()));
    //start new Thread for reading

    startReading();
    //TODO adjust the loop to automatically connect to the server
    while (engineSocket.isConnected()) {
      //Create a Scanner object
      Scanner myObj = new Scanner(System.in);
      System.out.println("Enter command: ");
      //Read user input
      String userName = myObj.nextLine();
      switch (userName) {
        case "exit":
          System.out.println("Exit program!");
          System.exit(-1);
          break;
        case "login":
          System.out.println("Login to Server: ");
          loginPlayer(engineUsername);
          break;
      }
      //TODO better solution for asking to input a command
      try {
        Thread.sleep(1000);
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
    }
    disconnect();
  }

  /**
   * Send the given message to the server
   *
   * @param message (JsonElement) which will send to the server
   * @author tstoehr
   */
  private void send(JsonElement message) {
    Thread sendThread = new Thread(() -> {
      try {
        serverOutMessage.write(message.toString());
        serverOutMessage.newLine();
        serverOutMessage.flush();
      } catch (IOException e) {
        e.printStackTrace();
      }

    });
    sendThread.start();
  }

  /**
   * Reading Thread for incoming messages from the server
   *
   * @author tstoehr
   */
  public void startReading() {
    Thread threadRead = new Thread(() -> {
      while (engineSocket.isConnected()) {
        try {
          String line = serverInMessage.readLine();
          //Without the if statement, the reader would read null
          if (line != null) {
            System.out.println(line);
            receiveMessage(new JSONObject(line));
          }
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      }
    });
    threadRead.start();
  }

  /**
   * The methode process the incoming messages
   *
   * @param message is the incoming message
   * @author tstoehr
   */
  public void receiveMessage(JSONObject message) {
    Set<String> keys = message.keySet();
    for (String key : keys) {
      switch (key) {
        case "response":
          JSONObject response = (JSONObject) message.get(key);
          String request = (String) response.get("request");
          switch (request) {
            case "login":
              JSONObject success = (JSONObject) message.get(key);
              String bool = (String) success.get("success");
              JSONObject error = (JSONObject) message.get(key);
              String errorMessage = (String) error.get("error");
              if (Boolean.getBoolean(bool)) {
                System.out.println("Login successfully");
              } else {
                System.out.println(errorMessage);
              }
          }
          break;
        default:
          System.out.println();
      }
    }
  }

  /**
   * The methode build the loginPlayer message
   *
   * @param username of the engine
   */
  public void loginPlayer(String username) {
    JsonObject jUsername = new JsonObject();
    jUsername.addProperty("username", username);
    JsonObject playerLogin = new JsonObject();
    playerLogin.add("loginPlayer", jUsername);
    send(playerLogin);
  }

  /**
   * This methode close the socket of the engine
   *
   * @author tstoehr
   */
  public void disconnect() {
    try {
      engineSocket.close();
    } catch (IOException e) {
      System.out.println("Unexpected error in the Connection: " + e);
    }
  }
}
