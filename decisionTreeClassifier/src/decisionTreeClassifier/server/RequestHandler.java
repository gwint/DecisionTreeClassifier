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
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;
import classifier.DecisionTreeClassifier;
import classifier.ID3Algorithm;

public class RequestHandler implements Runnable {
  private Socket clientConnection;
  private static Set<String> allowedMethods =
             new HashSet<>(Arrays.asList("GET", "POST", "OPTIONS"));

  private enum HTTPVerb {
    GET,
    PUT,
    DELETE,
    OPTIONS,
    POST,
    UNEXPECTED
  }

  public RequestHandler(Socket clientConnectionIn) {
    if(clientConnectionIn == null) {
      throw new IllegalArgumentException("Client connection socket must not be null");
    }
    this.clientConnection = clientConnectionIn;
  }

  private HTTPVerb convertStrToHTTPVerb(String httpVerb) {
    if(httpVerb == null) {
      throw new IllegalArgumentException("HTTP verb string to convert must not be null");
    }

    HTTPVerb verb = HTTPVerb.UNEXPECTED;
    if(httpVerb.equals("GET")) {
      verb = HTTPVerb.GET;
    }
    else if(httpVerb.equals("PUT")) {
      verb = HTTPVerb.PUT;
    }
    else if(httpVerb.equals("DELETE")) {
      verb = HTTPVerb.DELETE;
    }
    else if(httpVerb.equals("OPTIONS")) {
      verb = HTTPVerb.OPTIONS;
    }
    else if(httpVerb.equals("POST")) {
      verb = HTTPVerb.POST;
    }

    return verb;
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
                                         JSONArray allFeatures,
                                         JSONArray allClasses) {
    if(sampleIndices == null) {
      throw new IllegalArgumentException("Array of sample indices must not be null");
    }
    if(allFeatures == null) {
      throw new IllegalArgumentException("Array of feature data must not be null");
    }
    if(allClasses == null) {
      throw new IllegalArgumentException("Array of class labels must not be null");
    }

    Dataset dataset = this.getDataset(allFeatures, allClasses);
    int numSamples = allClasses.length();

    List<Integer> trainingSampleIndices = new ArrayList<>();
    List<Integer> testSampleIndices = new ArrayList<>();

    Set<Integer> testIndicesSet = new HashSet<>();

    for(int i = 0; i < sampleIndices.length(); i++) {
      testIndicesSet.add(sampleIndices.getInt(i));
    }

    for(int sampleIndex = 0; sampleIndex < numSamples; sampleIndex++) {
      if(testIndicesSet.contains(sampleIndex)) {
        testSampleIndices.add(sampleIndex);
      }
      else {
        trainingSampleIndices.add(sampleIndex);
      }
    }

    DecisionTreeClassifier clf =
               new DecisionTreeClassifier(new ID3Algorithm(), 15);

    clf.train(dataset, trainingSampleIndices, testSampleIndices);

    NDArray<Double> predictedClasses = clf.predict(testSampleIndices);

    JSONArray classesJsonArr = new JSONArray();

    for(int i = 0; i < predictedClasses.length(1); i++) {
      classesJsonArr.put(predictedClasses.get(0, i).doubleValue());
    }

    return classesJsonArr;
  }

  public Object getJSONValue(JSONObject jsonObj, String key) {
    if(jsonObj == null) {
      throw new IllegalArgumentException("JSON object cannot be null");
    }
    if(key == null) {
      throw new IllegalArgumentException("Key cannot be null");
    }

    try {
      return jsonObj.get(key);
    }
    catch(JSONException e) {
      return null;
    }
  }

  private static String getAllowedMethodsField() {
    String allowedOptions = "Allowed: ";
    Iterator<String> iter = RequestHandler.allowedMethods.iterator();
    while(iter.hasNext()) {
      String option = iter.next();
      allowedOptions = allowedOptions + option;
      if(iter.hasNext()) {
        allowedOptions = allowedOptions + ", ";
      }
    }
    return allowedOptions;
  }

  public void run() {
    InputStream datasetStream = null;
    try {
      datasetStream = this.clientConnection.getInputStream();
      String datasetString = "";

      BufferedReader requestReader =
             new BufferedReader(new InputStreamReader(datasetStream));

      StringBuilder continueResponse = null;

      Map<String, String> requestHeaderValues = new HashMap<>();

      String currentRequestLine = requestReader.readLine();
      while(currentRequestLine != null && currentRequestLine.length() > 0) {
        System.out.println(currentRequestLine);
        if(currentRequestLine.contains(":")) {
          String[] keyAndValueTuple = currentRequestLine.split(new String("[' ']?:[' ']?"));
          requestHeaderValues.put(keyAndValueTuple[0], keyAndValueTuple[1]);
        }
        else {
          String[] requestTypeInfo = currentRequestLine.split(new String(" "));
          requestHeaderValues.put("verb", requestTypeInfo[0]);
          requestHeaderValues.put("relativeUrl", requestTypeInfo[1]);
          requestHeaderValues.put("protocolVersion", requestTypeInfo[2]);
        }
        currentRequestLine = requestReader.readLine();
      }

      HTTPVerb httpVerb =
            convertStrToHTTPVerb(requestHeaderValues.get("verb"));
      OutputStream socketWriteStream = null;

      StringBuilder httpResponse = new StringBuilder();
      int numTrainingTuples = 0;

      if(httpVerb == HTTPVerb.POST) {
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
        socketWriteStream = this.clientConnection.getOutputStream();
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

        JSONArray testSamples =
                     (JSONArray) this.getJSONValue(jsonObj, "test_samples");
        if(testSamples != null && testSamples.length() > 0) {
          Integer numTrainingTuplesInteger =
                       (Integer) this.getJSONValue(jsonObj, "num_items");
          if(numTrainingTuplesInteger != null) {
            // Use user-provided dataset to make predictions
            numTrainingTuples = numTrainingTuplesInteger.intValue();

            JSONArray features =
                         (JSONArray) this.getJSONValue(jsonObj, "all_features");

            JSONArray classes =
                         (JSONArray) this.getJSONValue(jsonObj, "class_labels");
            if(features != null && classes != null &&
                                classes.length() == numTrainingTuples) {

              JSONArray testSampleClasses = this.getTestSampleClasses(testSamples,
                                                                      features,
                                                                      classes);
              System.out.println(testSampleClasses);
              httpResponse.append("HTTP/1.1 200 OK\n\n");
            }
            else {
              httpResponse.append("HTTP/1.1 406 Not Acceptable\n\n");
            }
          }
          else {
            httpResponse.append("HTTP/1.1 451 Unavailable For Legal Reasons\n\n");
          }
        }
        else {
          httpResponse.append("HTTP/1.1 406 Not Acceptable\n\n");
        }
      }
      else if(httpVerb == HTTPVerb.GET) {
        httpResponse.append("HTTP/1.1 200 OK\n\n");
        JSONObject datasetInfo = new JSONObject();
        datasetInfo.append("Number of Samples", numTrainingTuples);
        datasetInfo.append("Number of Properties per Sample", "");
        datasetInfo.append("Training Algorithm", "ID3");
        httpResponse.append(datasetInfo.toString() + "\n");
      }
      else if(httpVerb == HTTPVerb.OPTIONS) {
        httpResponse.append("HTTP/1.1 200 OK\n");
        httpResponse.append(this.getAllowedMethodsField() + "\n");
      }
      else {
        httpResponse.append("HTTP/1.1 405 Method Not Allowed\n");
        httpResponse.append(this.getAllowedMethodsField() + "\n");
      }

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

