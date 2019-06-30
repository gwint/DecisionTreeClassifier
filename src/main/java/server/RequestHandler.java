package server;

import java.net.Socket;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.File;
import org.json.JSONObject;
import org.json.JSONException;
import org.json.JSONArray;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.NoSuchElementException;
import util.FileProcessor;
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
    HEAD,
    PUT,
    DELETE,
    OPTIONS,
    POST,
    UNEXPECTED
  }

  private static class BuiltInClassifier {
    private static DecisionTreeClassifier clf = null;

    private BuiltInClassifier() {
      CharSequence classesFile = "data/classes.txt";
      try {
        File testFile = new File(classesFile.toString());
        if(!testFile.exists()) {
          System.err.printf("%s does not exist\n", classesFile.toString());
          System.exit(1);
        }
      }
      catch(NullPointerException e) {
        System.err.println("Pathname to input file cannot be null");
        System.err.println(e);
        System.exit(1);
      }
      finally {}

      CharSequence dataFile = "data/data.txt";
      try {
        File testFile = new File(dataFile.toString());
        if(!testFile.exists()) {
          System.err.printf("%s does not exist\n", dataFile.toString());
          System.exit(1);
        }
      }
      catch(NullPointerException e) {
        System.err.println("Pathname to input file cannot be null");
        System.err.println(e);
        System.exit(1);
      }
      finally {}

      NDArray<Double> trainingData =
                       NDArray.readCSV(new FileProcessor(dataFile));
      NDArray<Double> trainingClasses =
                       NDArray.readCSV(new FileProcessor(classesFile));
      Dataset dataset = new Dataset(trainingData, trainingClasses);

      BuiltInClassifier.clf =
                new DecisionTreeClassifier(new ID3Algorithm(), 15);

      BuiltInClassifier.clf.train(dataset, 1);
    }

    public static DecisionTreeClassifier getBuiltInClassifier() {
      if(BuiltInClassifier.clf == null) {
        new BuiltInClassifier();
      }
      return BuiltInClassifier.clf;
    }
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

  private JSONArray getTestSampleClasses(NDArray<Double> testSample) {
    if(testSample == null) {
      throw new IllegalArgumentException("Test sample must not be null");
    }

    DecisionTreeClassifier clf =
                     RequestHandler.BuiltInClassifier.getBuiltInClassifier();

    NDArray<Double> prediction = clf.predict(testSample);

    JSONArray predictionJSONArr = new JSONArray();
    predictionJSONArr.put(prediction.get(0, 0));

    return predictionJSONArr;
  }

  private JSONArray getTestSampleClasses(NDArray testSample,
                                         JSONArray allFeatures,
                                         JSONArray allClasses) {
    if(testSample == null) {
      throw new IllegalArgumentException("Array of test sample must not be null");
    }
    if(allFeatures == null) {
      throw new IllegalArgumentException("Array of feature data must not be null");
    }
    if(allClasses == null) {
      throw new IllegalArgumentException("Array of class labels must not be null");
    }

    Dataset dataset = this.getDataset(allFeatures, allClasses);
    DecisionTreeClassifier clf =
               new DecisionTreeClassifier(new ID3Algorithm(), 15);
    clf.train(dataset, 1.0);

    NDArray<Double> predictedClasses = clf.predict(testSample);

    JSONArray classesJsonArr = new JSONArray();

    for(int i = 0; i < predictedClasses.length(1); i++) {
      classesJsonArr.put(predictedClasses.get(0, i).doubleValue());
    }

    return classesJsonArr;
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

      String headers = "";
      int b = datasetStream.read();
      int nextCharRead = datasetStream.read();
      int nextNextCharRead = datasetStream.read();
      while(!(b == (int)'\n' && nextCharRead == 13 &&
                                 nextNextCharRead == (int)'\n')) {
        headers = headers + new String(new byte[] {(byte)b});

        b = nextCharRead;
        nextCharRead = nextNextCharRead;
        nextNextCharRead = datasetStream.read();
      }
      String[] headerLines = headers.split(new String("\n"));

      Map<String, String> requestHeaderValues = new HashMap<>();

      for(int i = 0; i < headerLines.length; i++) {
        String currentRequestLine = headerLines[i].trim();
        if(currentRequestLine.contains(":")) {
          String[] keyAndValueTuple =
                     currentRequestLine.split(new String(":[' ']"));
          requestHeaderValues.put(keyAndValueTuple[0].toLowerCase(),
                                  keyAndValueTuple[1].toLowerCase());
        }
        else {
          String[] requestTypeInfo = currentRequestLine.split(new String(" "));
          requestHeaderValues.put("verb", requestTypeInfo[0]);
          requestHeaderValues.put("relativeUrl", requestTypeInfo[1]);
          requestHeaderValues.put("protocolVersion", requestTypeInfo[2]);
        }
      }

      StringBuilder continueResponse = null;

      HTTPVerb httpVerb =
            convertStrToHTTPVerb(requestHeaderValues.get("verb"));
      OutputStream socketWriteStream = null;

      StringBuilder httpResponse = new StringBuilder();
      int numTrainingTuples = 0;

      if(httpVerb == HTTPVerb.POST) {
        int bodySizeInBytes = -1;
        try {
          bodySizeInBytes =
               Integer.parseInt(requestHeaderValues.get("content-length"));
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

        Integer numTrainingTuplesInteger =
                       (Integer) this.getJSONValue(jsonObj, "num_items");
        JSONArray userProvidedTestTuples =
                  (JSONArray) this.getJSONValue(jsonObj, "test_tuples");

        if(userProvidedTestTuples != null) {
          NDArray<Double> sample =
                        new NDArray<>(1, userProvidedTestTuples.length());

          for(int i = 0; i < userProvidedTestTuples.length(); i++) {
            sample.add((Double) userProvidedTestTuples.get(i), 0, i);
          }

          JSONArray testSampleClass = this.getTestSampleClasses(sample);
          httpResponse.append("HTTP/1.1 200 OK\n\n");
          httpResponse.append(testSampleClass.toString());
        }
        else {
          httpResponse.append("HTTP/1.1 406 Not Acceptable\n\n");
        }
      }
      else if(httpVerb == HTTPVerb.GET) {
        httpResponse.append("HTTP/1.1 200 OK\n");
        httpResponse.append("Access-Control-Allow-Origin: *\n\n");
        JSONObject datasetInfo = new JSONObject();
        datasetInfo.append("Number of Samples", numTrainingTuples);
        datasetInfo.append("Number of Properties per Sample", "");
        datasetInfo.append("Training Algorithm", "ID3");
        httpResponse.append(datasetInfo.toString() + "\n");
      }
      else if(httpVerb == HTTPVerb.OPTIONS) {
        System.out.println("options");
        httpResponse.append("HTTP/1.1 200 OK\n\n");
        httpResponse.append(this.getAllowedMethodsField() + "\n");
      }
      else {
        httpResponse.append("HTTP/1.1 405 Method Not Allowed\n\n");
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

