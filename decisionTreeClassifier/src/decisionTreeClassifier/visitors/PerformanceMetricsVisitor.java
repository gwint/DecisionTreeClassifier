package visitors;

import classifier.DecisionTreeClassifier;
import classifier.ID3Algorithm;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import util.NDArray;
import util.Dataset;

public class PerformanceMetricsVisitor {
  private static final int NUM_ITERS = 100;

  public double calculateAccuracy(DecisionTreeClassifier clf,
                                   Dataset dataset) {
    if(clf == null) {
      throw new IllegalArgumentException("Classifier from which statistics are to be collected must not be null");
    }

    int totalCorrectPredictions = 0;
    int totalNumPredictions = 0;

    for(int numIterations = 0;
        numIterations < PerformanceMetricsVisitor.NUM_ITERS;
        numIterations++) {

      clf.train(dataset, 0.3);

      NDArray<Double> predictedClasses = clf.predict();
      NDArray<Double> actualClasses = dataset.getClasses();
      List<Integer> testSampleIndices = clf.getTestingSamples();

      for(int i = 0; i < testSampleIndices.size(); i++) {
        Integer testSampleIdx = testSampleIndices.get(i);
        if(testSampleIdx == null) {
          throw new IllegalStateException("Test Sample index must not be null");
        }
        Double predictedClass = predictedClasses.get(0, i);
        Double actualClass = actualClasses.get(testSampleIdx.intValue(), 0);
        if(predictedClass.equals(actualClass)) {
          totalCorrectPredictions++;
        }
      }
      totalNumPredictions += testSampleIndices.size();
    }

    System.out.println("Num correct: " + totalCorrectPredictions);
    return ((double) totalCorrectPredictions) / totalNumPredictions;
  }

  private void performStratifiedKFoldCV(DecisionTreeClassifier clf,
                                        Dataset dataset,
                                        int numFolds) {
    if(clf == null) {
      throw new IllegalArgumentException("Classifier from which statistics are to be collected must not be null");
    }

    //Map<Double, Set<Integer>> classMembership = this.getClassMembership(clf);
    //List<Set<Integer>> folds = this.getKFolds(classMembership, numFolds);
  }
}
