package decisionTreeClassifier.driver;
import util.ProcessorI;
import util.Results;
import util.StdoutDisplayInterface;
import java.io.File;
import java.io.IOException;
import util.FileProcessor;
import util.MyLogger;
import util.NDArray;
import classifier.DecisionTreeClassifier;
import classifier.ID3Algorithm;
import performance_evaluation.PerformanceMetricsCalculator;
import util.Dataset;
import java.util.List;
import java.util.ArrayList;
import server.Server;

/**
 * @author Gregory Wint
 *
 */

public class Driver {
  /**
   * Grabs names of the file containing a list of integers, the
   * number of threads to use while calculating the sum of the primes in
   * that list, and the debug value to use during execution.
   * @return No return value.
   */
  public static void main(String[] args) {

    /*
     * As the build.xml specifies the arguments as argX, in case the
     * argument value is not given java takes the default value specified in
     * build.xml. To avoid that, below condition is used
     */
    if(args.length != 3 ||
       args[0].equals("${arg0}") ||
       args[1].equals("${arg1}") ||
       args[2].equals("${arg2}")) {
      System.err.println(
       "Error: Incorrect number of arguments. Program accepts 3 arguments.");
      System.exit(0);
    }

    // Read in name of file containing features
    CharSequence featuresFile = args[0];
    try {
      File testFile = new File(featuresFile.toString());
      if(!testFile.exists()) {
        System.err.printf("%s does not exist\n", featuresFile.toString());
        System.exit(1);
      }
    }
    catch(NullPointerException e) {
      System.err.println("Pathname to input file cannot be null");
      System.err.println(e);
      System.exit(1);
    }
    finally {}

    // Read in name of file containing classes
    CharSequence classesFile = args[1];
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

    // Read in debug level
    CharSequence debugValueStr = args[2];
    int debugValue = -1;

    try {
      debugValue = Integer.parseInt(debugValueStr.toString());
      if(debugValue < 0 || debugValue > 4) {
        throw new IllegalArgumentException("The debug value must be an integer falling between 0 and 4, inclusive.");
      }
    }
    catch(NumberFormatException e) {
      System.err.println("The debug value must be an integer");
      System.err.println(e);
      System.exit(1);
    }
    finally {}

    MyLogger.setDebugValue(debugValue);

    NDArray<Double> trainingData =
                       NDArray.readCSV(new FileProcessor(featuresFile));
    NDArray<Double> trainingClasses =
                       NDArray.readCSV(new FileProcessor(classesFile));
    Dataset dataset = new Dataset(trainingData, trainingClasses);

    DecisionTreeClassifier clf =
             new DecisionTreeClassifier(new ID3Algorithm(), 15);

    PerformanceMetricsCalculator metricsCalculator =
                                          new PerformanceMetricsCalculator();

    System.out.println(metricsCalculator.calculateAccuracy(clf, dataset));
    System.out.println(metricsCalculator.performStratifiedKFoldCV(clf,
                                                                  dataset,
                                                                  20));
    List<List<Integer>> confusionMatrix =
                metricsCalculator.getConfusionMatrix(clf, dataset);

    System.out.println(confusionMatrix);

    int numTruePos = confusionMatrix.get(metricsCalculator.TRUE_POS_ROW)
                                    .get(metricsCalculator.TRUE_POS_COL);
    int numFalsePos = confusionMatrix.get(metricsCalculator.FALSE_POS_ROW)
                                    .get(metricsCalculator.FALSE_POS_COL);
    double positivePredictiveValue =
                       ((double) numTruePos) / (numTruePos + numFalsePos);
    System.out.println("PPV: " + positivePredictiveValue);

    int numTrueNeg = confusionMatrix.get(metricsCalculator.TRUE_NEG_ROW)
                                    .get(metricsCalculator.TRUE_NEG_COL);
    int numFalseNeg = confusionMatrix.get(metricsCalculator.FALSE_NEG_ROW)
                                    .get(metricsCalculator.FALSE_NEG_COL);
    double negativePredictiveValue =
                       ((double) numTrueNeg) / (numTrueNeg + numFalseNeg);
    System.out.println("NPV: " + negativePredictiveValue);

    Server.start();
  }
}
