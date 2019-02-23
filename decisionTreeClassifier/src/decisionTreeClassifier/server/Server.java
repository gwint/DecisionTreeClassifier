package decisionTreeClassifier.server;

import java.net.Socket;
import java.net.ServerSocket;
import java.io.IOException;

public class Server {
  public static void start() {
    ServerSocket serverSocket = null;
    try {
      serverSocket = new ServerSocket(0);
    }
    catch(IOException e) {
      System.err.println(e.getMessage());
      System.exit(1);
    }
    finally {}

    System.out.println("Waiting for http requests..." +
                       serverSocket.getLocalPort());
    while(true) {
      try {
        serverSocket.accept();
        // Launch thread to handle request
      }
      catch(IOException e) {
        System.err.println(e.getMessage());
        System.exit(1);
      }
      finally {}
    }
  }

  public static void main(String[] args) {
    // parse command line arguments
    Server.start();
  }
}
