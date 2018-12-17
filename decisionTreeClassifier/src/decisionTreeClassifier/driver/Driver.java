package decisionTreeClassifier.driver;
import util.ProcessorI;
import util.Results;
import util.StdoutDisplayInterface;
import java.io.File;
import java.io.IOException;
import util.FileProcessor;
import util.MyLogger;
import util.NDArray;

/**
 * @author Gregory Wint
 *
 */

/**
 * Class used to house the main method responsible for creating the objects
 * needed to calculate the sum of a collection of prime numbers.
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

    // Read in input filename
    CharSequence inputFilename = args[0];
    try {
      File testFile = new File(inputFilename.toString());
      if(!testFile.exists()) {
        System.err.printf("%s does not exist\n", inputFilename.toString());
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

    // Read in training data into NDArray
    NDArray trainingData = NDArray.readCSV(new FileProcessor(inputFilename));

    // Pass data to classifier

    // Train classifier

    // Predict

    // Display values for performance metrics

    NDArray<Integer> ndarr = new NDArray<>(3,3,3);
    System.out.println(ndarr.isEmpty());
    ndarr.add(10,0,2,2);
    System.out.println(ndarr.get(0,2,2));
  }
}
