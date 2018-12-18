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

    NDArray trainingData = NDArray.readCSV(new FileProcessor(featuresFile));
    NDArray trainingClasses = NDArray.readCSV(new FileProcessor(classesFile));

    DecisionTreeClassifier clf = new DecisionTreeClassifier(trainingData,
                                                            trainingClasses);

    clf.train(new ID3Algorithm(), 0.7);

    clf.predict();

    // Calculate performance metrics and display results

  }
}
