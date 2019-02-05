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
  public static final int TRUE_POS_ROW = 0;
  public static final int TRUE_POS_COL = 0;
  public static final int FALSE_NEG_ROW = 0;
  public static final int FALSE_NEG_COL = 1;
  public static final int FALSE_POS_ROW = 1;
  public static final int FALSE_POS_COL = 0;
  public static final int TRUE_NEG_ROW = 1;
  public static final int TRUE_NEG_COL = 1;

  public List<List<Integer>> getConfusionMatrix(DecisionTreeClassifier clf,
                                                Dataset dataset) {
    List<List<Integer>> confusionMatrix = new ArrayList<>();
    confusionMatrix.add(new ArrayList<>());
    confusionMatrix.add(new ArrayList<>());

    confusionMatrix.get(0).add(0);
    confusionMatrix.get(0).add(0);
    confusionMatrix.get(1).add(0);
    confusionMatrix.get(1).add(0);

    Double positiveClass = 0.0;
    Double negativeClass = 1.0;

    for(int numIterations = 0;
        numIterations < PerformanceMetricsVisitor.NUM_ITERS;
        numIterations++) {

      clf.train(dataset, 0.70);

      NDArray<Double> predictedClasses = clf.predict();
      NDArray<Double> actualClasses = dataset.getClasses();
      List<Integer> testSampleIndices = clf.getTestingSamples();

      int predictionIdx = 0;
      int newCount = 0;
      for(Integer testSampleIdx = 0; testSampleIdx < testSampleIndices.size();
          testSampleIdx++) {
        double predictedClass =
                 predictedClasses.get(0, predictionIdx).doubleValue();
        double actualClass =
                 actualClasses.get(testSampleIdx.intValue(), 0).doubleValue();

        if(predictedClass == positiveClass && actualClass == positiveClass) {
          newCount =
            confusionMatrix.get(this.TRUE_POS_ROW)
                           .get(this.TRUE_POS_COL)
                           .intValue() + 1;
          confusionMatrix.get(this.TRUE_POS_ROW)
                         .set(this.TRUE_POS_COL, newCount);
        }
        else if(predictedClass == positiveClass &&
                actualClass == negativeClass) {
          newCount =
            confusionMatrix.get(this.FALSE_POS_ROW)
                           .get(this.FALSE_POS_COL)
                           .intValue() + 1;
          confusionMatrix.get(this.FALSE_POS_ROW)
                         .set(this.FALSE_POS_COL, newCount);
        }
        else if(predictedClass == negativeClass &&
                actualClass == positiveClass) {
          newCount =
            confusionMatrix.get(this.FALSE_NEG_ROW)
                           .get(this.FALSE_NEG_COL)
                           .intValue() + 1;
          confusionMatrix.get(this.FALSE_NEG_ROW)
                         .set(this.FALSE_NEG_COL, newCount);
        }
        else {
          newCount =
            confusionMatrix.get(this.TRUE_NEG_ROW)
                           .get(this.TRUE_NEG_COL)
                           .intValue() + 1;
          confusionMatrix.get(this.TRUE_NEG_ROW)
                         .set(this.TRUE_NEG_COL, newCount);
        }
      }
    }

    return confusionMatrix;
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
