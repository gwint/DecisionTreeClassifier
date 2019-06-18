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
    int portInt = 9090;

    if(args.length == 1) {
      String portString = args[0];
      try {
        portInt = Integer.parseInt(portString);
      }
      catch(NumberFormatException e) {
        System.err.println(e.getMessage());
        System.exit(1);
      }
      finally {}
    }

    Server.start(portInt);
  }
}
