package server;

import java.net.Socket;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import org.json.JSONObject;
import org.json.JSONException;
import org.json.JSONArray;

public class RequestHandler implements Runnable {
  private Socket clientConnection;

  public RequestHandler(Socket clientConnectionIn) {
    if(clientConnectionIn == null) {
      throw new IllegalArgumentException("Client connection socket must not be null");
    }
    this.clientConnection = clientConnectionIn;
  }

  public Object getJSONValue(JSONObject jsonObj, String key) {
    if(jsonObj == null) {
      throw new IllegalArgumentException("JSON object cannot be null");
    }
    if(key == null) {
      throw new IllegalArgumentException("Key cannot be null");
    }

    Object associatedObj = null;
    try {
      associatedObj = jsonObj.get(key);
    }
    catch(JSONException e) {
      System.err.println(String.format("Unable to find key in json object: %s",
                                       key));
      System.exit(1);
    }
    finally {}

    return associatedObj;
  }

  public void run() {
    System.out.println("Now handling classifier request...");

    InputStream datasetStream = null;
    try {
      datasetStream = this.clientConnection.getInputStream();
      String datasetString = "";

      int byteRead = datasetStream.read();
      boolean jsonEncountered = false;

      while(byteRead > -1) {
        if(byteRead == (byte)'{') {
          jsonEncountered = true;
        }
        if(jsonEncountered) {
          datasetString =
              datasetString.concat(new String(new byte[] {(byte) byteRead}));
        }
        byteRead = datasetStream.read();
      }

      JSONObject jsonObj = new JSONObject(datasetString);

      Integer numTrainingTuplesInteger =
                     (Integer) this.getJSONValue(jsonObj, "num_items");
      int numTrainingTuples = numTrainingTuplesInteger.intValue();

      System.out.println("# training tuples: " + numTrainingTuples);

      JSONArray features =
                     (JSONArray) this.getJSONValue(jsonObj, "all_features");

      JSONArray classes =
                     (JSONArray) this.getJSONValue(jsonObj, "class_labels");

      JSONArray testSamples =
                     (JSONArray) this.getJSONValue(jsonObj, "test_samples");

      System.out.println("test_samples: " + testSamples);

      if(testSamples.length() == 0) {
        throw new IllegalStateException("User must request a class label for at least one sample");
      }

      if(classes.length() != numTrainingTuples) {
        throw new IllegalStateException("Number of classes provided does not match stated tuple count");
      }

      // Classifiy test samples and respond to client

      StringBuilder httpResponse = new StringBuilder();
      httpResponse.append("HTTP/1.1 200 OK\n\n");

      try {
        OutputStream socketWriteStream =
                           this.clientConnection.getOutputStream();
        System.out.println(httpResponse);
        socketWriteStream.write(httpResponse.toString().getBytes());
        socketWriteStream.flush();
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

