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

  public double getPrecision() {
    return 0.0;
  }

  public double getRecall() {
    return 0.0;
  }

  private int getNumCorrectPredictions(NDArray<Double> predictions,
                                       NDArray<Double> actualClasses,
                                       List<Integer> testSampleIndices) {
    if(predictions == null) {
      throw new IllegalArgumentException("Array of predicted classes must not be null");
    }
    if(actualClasses == null) {
      throw new IllegalArgumentException("Array of actual classes must not be null");
    }
    if(testSampleIndices == null) {
      throw new IllegalArgumentException("List of test sample indices must not be null");
    }
    if(predictions.length(1) != testSampleIndices.size()) {
      throw new IllegalStateException("Number of predictions does not match number of actual class labels");
    }

    int totalCorrectPredictions = 0;

    for(int i = 0; i < testSampleIndices.size(); i++) {
      Integer testSampleIdx = testSampleIndices.get(i);
      if(testSampleIdx == null) {
        throw new IllegalStateException("Test Sample index must not be null");
      }
      Double predictedClass = predictions.get(0, i);
      Double actualClass = actualClasses.get(testSampleIdx.intValue(), 0);
      if(predictedClass.equals(actualClass)) {
        totalCorrectPredictions++;
      }
    }
    return totalCorrectPredictions;
  }

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

      clf.train(dataset, 0.90);

      NDArray<Double> predictedClasses = clf.predict();
      NDArray<Double> actualClasses = dataset.getClasses();
      List<Integer> testSampleIndices = clf.getTestingSamples();

      totalCorrectPredictions +=
                this.getNumCorrectPredictions(predictedClasses,
                                              actualClasses,
                                              testSampleIndices);
      totalNumPredictions += testSampleIndices.size();
    }

    System.out.println("Num correct: " + totalCorrectPredictions);
    return ((double) totalCorrectPredictions) / totalNumPredictions;
  }

  public double performStratifiedKFoldCV(DecisionTreeClassifier clf,
                                         Dataset dataset,
                                         int numFolds) {
    if(clf == null) {
      throw new IllegalArgumentException("Classifier from which statistics are to be collected must not be null");
    }
    if(numFolds < 2) {
      throw new IllegalArgumentException("Number of folds must be at least 2");
    }
    if(dataset == null) {
      throw new IllegalArgumentException("Dataset must not be null");
    }

    int totalNumCorrect = 0;
    int totalPredictionsMade = 0;

    List<Set<Integer>> folds = dataset.getKFolds(numFolds);
    for(int leaveOutFoldIdx = 0; leaveOutFoldIdx < folds.size();
        leaveOutFoldIdx++) {
      List<Integer> testingSampleIndices =
                         new ArrayList<>(folds.get(leaveOutFoldIdx));
      List<Integer> trainingSampleIndices = new ArrayList<>();
      for(int foldIdx = 0; foldIdx < folds.size(); foldIdx++) {
        if(foldIdx != leaveOutFoldIdx) {
          trainingSampleIndices.addAll(folds.get(foldIdx));
        }
      }
      clf.train(dataset, trainingSampleIndices, testingSampleIndices);
      NDArray<Double> predictions = clf.predict();
      totalNumCorrect += this.getNumCorrectPredictions(predictions,
                                                       dataset.getClasses(),
                                                       testingSampleIndices);
      totalPredictionsMade += testingSampleIndices.size();
    }

    System.out.println("Num correct: " + totalNumCorrect);
    return ((double) totalNumCorrect) / totalPredictionsMade;
  }
}
