package aws;

import com.amazonaws.services.lambda.runtime.Context;
import java.net.Socket;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.File;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import org.json.JSONObject;
import org.json.JSONException;
import org.json.JSONArray;
import util.FileProcessor;
import util.NDArray;
import util.Dataset;
import classifier.DecisionTreeClassifier;
import classifier.ID3Algorithm;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.HashSet;

public class RequestHandler {

    private static class BuiltInClassifier {
        private static DecisionTreeClassifier clf = null;

        private BuiltInClassifier() {
            CharSequence classesFile = "/classes.txt";
            CharSequence dataFile = "/data.txt";

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

    public String handleRequest(Map<String, List<Double> > input, Context context) {
        context.getLogger().log("Input: " + input.toString());

        int numColumns = input.get("test_tuples").size();
        NDArray<Double> sample = new NDArray<>(1, numColumns);

        for(int i = 0; i < numColumns; i++) {
            sample.add((Double) input.get("test_tuples").get(i), 0, i);
        }
        JSONArray testSampleClass = getTestSampleClasses(sample);
        return testSampleClass.toString();
    }
}
