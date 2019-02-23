package server;

import java.net.Socket;

public class RequestHandler implements Runnable {
  private Socket clientConnection;

  public RequestHandler(Socket clientConnectionIn) {
    if(clientConnectionIn == null) {
      throw new IllegalArgumentException("Client connection socket must not be null");
    }
    this.clientConnection = clientConnectionIn;
  }
  public void run() {
    System.out.println("Now handling classifier request...");
    // read json from socket representing dataset and samples to classify
    // classify using decision tree classifier
    // return json response to client
  }
}

