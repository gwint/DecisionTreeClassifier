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
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.NoSuchElementException;
import util.NDArray;
import util.Dataset;

public class RequestHandler implements Runnable {
  private Socket clientConnection;

  public RequestHandler(Socket clientConnectionIn) {
    if(clientConnectionIn == null) {
      throw new IllegalArgumentException("Client connection socket must not be null");
    }
    this.clientConnection = clientConnectionIn;
  }

  private NDArray<Double> createNDArray(JSONArray arr, int numRows,
                                        int numCols) {
    if(arr == null) {
      throw new IllegalArgumentException("json array used to create ndarray must not be null");
    }
    if(numRows < 0 || numCols < 0) {
      throw new IllegalArgumentException("Desired dimensions for ndarray must be positive");
    }

    NDArray<Double> ndArr = new NDArray<>(numRows, numCols);
    int elementIndex = 0;

    for(int currRow = 0; currRow < numRows; currRow++) {
      for(int currCol = 0; currCol < numCols; currCol++) {
        try {
          ndArr.add(arr.getDouble(elementIndex), currRow, currCol);
          elementIndex++;
        }
        catch(JSONException e) {
          System.err.println("Mismatch between size of json array and dimensions of desired ndarray");
          System.exit(1);
        }
        finally {}
      }
    }

    return ndArr;
  }

  private Dataset getDataset(JSONArray features, JSONArray classes) {
    if(features == null) {
      throw new IllegalArgumentException("Features json array must not be null");
    }
    if(classes == null) {
      throw new IllegalArgumentException("classes json array must not be null");
    }

    int numSamples = classes.length();

    if(features.length() % numSamples != 0) {
      throw new IllegalArgumentException("Number of features and number of class labels does not match");
    }

    int numFeaturesPerSample = features.length() / numSamples;

    int desiredNumRows = numSamples;
    int desiredNumCols = numFeaturesPerSample;

    NDArray<Double> featuresNDArray = this.createNDArray(features,
                                                         desiredNumRows,
                                                         desiredNumCols);
    NDArray<Double> classLabelsNDArray = this.createNDArray(classes,
                                                            desiredNumRows,
                                                            1);
    return new Dataset(featuresNDArray, classLabelsNDArray);
  }

  private JSONArray getTestSampleClasses(JSONArray sampleIndices,
                                         JSONArray all_data,
                                         JSONArray all_classes) {
    if(sampleIndices == null) {
      throw new IllegalArgumentException("Array of sample indices must not be null");
    }
    if(all_data == null) {
      throw new IllegalArgumentException("Array of feature data must not be null");
    }
    if(all_classes == null) {
      throw new IllegalArgumentException("Array of class labels must not be null");
    }

    return new JSONArray();
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
    InputStream datasetStream = null;
    try {
      datasetStream = this.clientConnection.getInputStream();
      String datasetString = "";

      BufferedReader requestReader =
             new BufferedReader(new InputStreamReader(datasetStream));


      boolean jsonEncountered = false;
      StringBuilder continueResponse = null;

      Map<String, String> requestHeaderValues = new HashMap<>();

      String currentRequestLine = requestReader.readLine();
      while(currentRequestLine != null && currentRequestLine.length() > 0) {
        System.out.println(currentRequestLine);
        if(currentRequestLine.contains(":")) {
          String[] keyAndValueTuple = currentRequestLine.split(new String("[' ']?:[' ']?"));
          requestHeaderValues.put(keyAndValueTuple[0], keyAndValueTuple[1]);
        }
        currentRequestLine = requestReader.readLine();
      }
      System.out.println(requestHeaderValues);

      int bodySizeInBytes = -1;
      try {
        bodySizeInBytes =
               Integer.parseInt(requestHeaderValues.get("Content-Length"));
      }
      catch(NumberFormatException e) {
        System.err.println("Content-Length sent in http request could not be parse as an integer");
        System.exit(1);
      }
      finally {}

      continueResponse = new StringBuilder();
      continueResponse.append("HTTP/1.1 100 Continue\r\n\r\n");
      OutputStream socketWriteStream =
                             this.clientConnection.getOutputStream();
      socketWriteStream.write(continueResponse.toString().getBytes());
      socketWriteStream.flush();

      int numBytesRead = 0;
      int nextByte = datasetStream.read();
      while(numBytesRead < bodySizeInBytes) {
        datasetString =
              datasetString.concat(new String(new byte[]{(byte) nextByte}));
        numBytesRead++;
        if(numBytesRead < bodySizeInBytes) {
          nextByte = datasetStream.read();
        }
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
        socketWriteStream = this.clientConnection.getOutputStream();
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

