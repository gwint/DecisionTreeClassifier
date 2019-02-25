package server;

import java.net.Socket;
import java.io.InputStream;
import java.io.IOException;

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
    // classify using decision tree classifier
    // return json response to client
    InputStream datasetStream = null;
    try {
      datasetStream = this.clientConnection.getInputStream();
      // read json from socket representing dataset and samples to classify
      for(int i = 0; i < 4; i++) {
        System.out.println((char) datasetStream.read());
      }
      try {
        this.clientConnection.close();
      }
      catch(IOException e) {
        System.err.println(e.getMessage());
        System.exit(1);
      }
      finally {}
    }
    catch(IOException e) {
      System.err.println(e.getMessage());
      System.exit(1);
    }
    finally {}
  }
}

