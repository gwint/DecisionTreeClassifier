package server;

import java.net.Socket;
import java.net.ServerSocket;
import java.io.IOException;

public class Server {
  public static void start(int portNum) {
    if(portNum < 1024) {
      throw new IllegalArgumentException("Invalid port number");
    }

    ServerSocket serverSocket = null;
    try {
      serverSocket = new ServerSocket(portNum);
    }
    catch(IOException e) {
      System.err.println(e.getMessage());
      System.exit(1);
    }
    finally {}

    while(true) {
      try {
        Socket conn = serverSocket.accept();
        (new Thread(new RequestHandler(conn))).start();
      }
      catch(IOException e) {
        System.err.println(e.getMessage());
        System.exit(1);
      }
      finally {}
    }
  }

  public static void main(String[] args) {
    String host = args[0];
    System.out.println("Server runnning on "  + host);

    String portString = args[1];
    int portInt = -1;
    try {
      portInt = Integer.parseInt(portString);
    }
    catch(NumberFormatException e) {
      System.err.println(e.getMessage());
      System.exit(1);
    }
    finally {}

    System.out.println("Server listening on port " + portInt);

    Server.start(portInt);
  }
}
