package visitors;

import classifier.DecisionTreeClassifier;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import util.NDArray;

public class PerformanceMetricsVisitor implements ClfVisitorI {
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
    int correctPredictions = 0;

    if(trainedClf == null) {
      throw new IllegalArgumentException("Classifier from which statistics are to be collected must not be null");
    }

    NDArray<Double> predictedClasses = trainedClf.getPredictedClasses();
    NDArray<Double> actualClasses = trainedClf.getClasses();
    List<Integer> testSampleIndices = trainedClf.getTestingSamples();

    System.out.println("predictedClasses size: " + actualClasses.length(0));

    for(int i = 0; i < testSampleIndices.size(); i++) {
      Integer testSampleIdx = testSampleIndices.get(i);
      if(testSampleIdx == null) {
        throw new IllegalStateException("Test Sample index must not be null");
      }
      Double predictedClass = predictedClasses.get(0, i);
      Double actualClass = actualClasses.get(testSampleIdx.intValue(), 0);
      if(predictedClass.equals(actualClass)) {
        correctPredictions++;
      }
    }
    System.out.println("Num correct: " + correctPredictions);
    this.setAccuracy(Double.valueOf(((double) correctPredictions) /
                                    testSampleIndices.size()));
  }

  public String toString() {
    return String.format("Accuracy: %s", this.getAccuracy());
  }
}
