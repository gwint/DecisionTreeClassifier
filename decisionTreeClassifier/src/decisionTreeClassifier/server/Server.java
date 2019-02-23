package decisionTreeClassifier.server;

import java.net.Socket;
import java.net.ServerSocket;
import java.io.IOException;

public class Server {
  public static void start() throws IOException {
    ServerSocket serverSocket = new ServerSocket(0);
    System.out.println("Waiting for http requests..." +
                       serverSocket.getLocalPort());
    while(true) {
    }
  }

  public static void main(String[] args) throws IOException {
    Server.start();
  }
}
