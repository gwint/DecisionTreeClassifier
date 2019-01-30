package visitors;

import classifier.DecisionTreeClassifier;
import classifier.ID3Algorithm;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import util.NDArray;

public class PerformanceMetricsVisitor implements ClfVisitorI {
  private static final int NUM_ITERS = 100;
  private Double accuracy;

  public PerformanceMetricsVisitor() {
    this.accuracy = null;
  }

  public double getAccuracy() {
    if(this.accuracy == null) {
      throw new IllegalArgumentException("Accuracy must be calculated before its value can be queried");
    }
    return this.accuracy.doubleValue();
  }

  private void setAccuracy(Double accuracyIn) {
    if(accuracyIn == null) {
      throw new IllegalArgumentException("Accuracy must not be null");
    }
    this.accuracy = accuracyIn;
  }

  public void visit(DecisionTreeClassifier trainedClf) {
    int totalCorrectPredictions = 0;
    int totalNumPredictions = 0;

    if(trainedClf == null) {
      throw new IllegalArgumentException("Classifier from which statistics are to be collected must not be null");
    }

    for(int numIterations = 0;
        numIterations < PerformanceMetricsVisitor.NUM_ITERS;
        numIterations++) {
      trainedClf.train(new ID3Algorithm(), 0.3);
      trainedClf.predict();

      NDArray<Double> predictedClasses = trainedClf.getPredictedClasses();
      NDArray<Double> actualClasses = trainedClf.getClasses();
      List<Integer> testSampleIndices = trainedClf.getTestingSamples();

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
    this.setAccuracy(Double.valueOf(((double) totalCorrectPredictions) /
                                    totalNumPredictions));
  }

  public String toString() {
    return String.format("Accuracy: %s", this.getAccuracy());
  }
}
