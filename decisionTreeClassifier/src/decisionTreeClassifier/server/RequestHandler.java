package server;

import java.net.Socket;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.BufferedReader;
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

      BufferedReader requestReader =
             new BufferedReader(new InputStreamReader(datasetStream));

      String currentRequestLine = requestReader.readLine();

      boolean jsonEncountered = false;
      StringBuilder continueResponse = null;

      while(currentRequestLine != null) {
        System.out.println(currentRequestLine);
        if(currentRequestLine.length() == 0 && continueResponse == null) {
          System.out.println("Time to send 100-continue response to client");
          continueResponse = new StringBuilder();
          continueResponse.append("HTTP/1.1 100 Continue\r\n\r\n");
          OutputStream socketWriteStream =
                             this.clientConnection.getOutputStream();
          System.out.println(continueResponse);
          socketWriteStream.write(continueResponse.toString().getBytes());
          socketWriteStream.flush();
        }
        else if(currentRequestLine.length() > 0 && currentRequestLine.charAt(0) == '{') {
          jsonEncountered = true;
        }

        if(jsonEncountered) {
          datasetString =
              datasetString.concat(currentRequestLine);
        }
        System.out.println("Attempting to read again");
        currentRequestLine = requestReader.readLine();
      }

      System.out.println("Done reading");

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

