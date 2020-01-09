package decisionTreeClassifier.driver;
import util.ProcessorI;
import java.io.File;
import java.io.IOException;
import util.FileProcessor;
import util.MyLogger;
import util.NDArray;
import classifier.DecisionTreeClassifier;
import classifier.ID3Algorithm;
import performance_evaluation.PerformanceMetricsCalculator;
import java.util.List;
import java.util.ArrayList;
import server.Server;

public class Driver {
    public static void main(String[] args) {
        if(args.length != 2 || args[0].equals("${arg0}") || args[1].equals("${arg1}")) {
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

        MyLogger.setDebugValue(0);

        NDArray features = NDArray.readCSV(featuresFile);
        NDArray classes = NDArray.readCSV(classesFile);

        DecisionTreeClassifier clf =
             new DecisionTreeClassifier(new ID3Algorithm(), 15);

        PerformanceMetricsCalculator metricsCalculator =
                                          new PerformanceMetricsCalculator();

        System.out.println(metricsCalculator.calculateAccuracy(clf,
                                                               features,
                                                               classes));
        System.out.println(metricsCalculator.performStratifiedKFoldCV(clf,
                                                                      features,
                                                                      classes,
                                                                      20));
        List<List<Integer>> confusionMatrix =
                metricsCalculator.getConfusionMatrix(clf, features, classes);

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
    }
}
