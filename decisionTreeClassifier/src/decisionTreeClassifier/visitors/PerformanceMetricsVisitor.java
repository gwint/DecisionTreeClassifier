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

  private void calculateAccuracy(DecisionTreeClassifier clf) {
    if(clf == null) {
      throw new IllegalArgumentException("Classifier from which statistics are to be collected must not be null");
    }

    int totalCorrectPredictions = 0;
    int totalNumPredictions = 0;

    for(int numIterations = 0;
        numIterations < PerformanceMetricsVisitor.NUM_ITERS;
        numIterations++) {
      clf.train(new ID3Algorithm(), 0.3);
      clf.predict();

      NDArray<Double> predictedClasses = clf.getPredictedClasses();
      NDArray<Double> actualClasses = clf.getClasses();
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
    this.setAccuracy(Double.valueOf(((double) totalCorrectPredictions) /
                                    totalNumPredictions));
  }

  private Map<Double, Set<Integer>>
  getClassMembership(DecisionTreeClassifier clf) {
    Map<Double, Set<Integer>> classMembership = new HashMap<>();
    NDArray<Double> allClasses = clf.getClasses();

    for(int sampleIdx = 0; sampleIdx < allClasses.length(0); sampleIdx++) {
      Double classLabel = allClasses.get(sampleIdx, 0);
      if(classMembership.containsKey(classLabel)) {
        classMembership.get(classLabel).add(sampleIdx);
      }
      else {
        Set<Integer> indicesOfSamplesWClass = new HashSet<>();
        indicesOfSamplesWClass.add(sampleIdx);
        classMembership.put(classLabel, indicesOfSamplesWClass);
      }
    }
    return classMembership;
  }

  private List<Set<Integer>>
  getKFolds(Map<Double, Set<Integer>> classMembership, int numFolds) {
    if(classMembership == null) {
      throw new IllegalArgumentException("Class Membership cannot be null");
    }

    List<Set<Integer>> folds = new ArrayList<>();

    for(int i = 0; i < numFolds; i++) {
      folds.add(new HashSet<>());
    }

    int foldIndex = 0;

    for(Double classLabel : classMembership.keySet()) {
      Set<Integer> sampleIndices = classMembership.get(classLabel);
      Iterator<Integer> sampleIndexIterator = sampleIndices.iterator();
      while(sampleIndexIterator.hasNext()) {
        folds.get(foldIndex % numFolds).add(sampleIndexIterator.next());
        foldIndex++;
      }
    }
    return folds;
  }

  private void performStratifiedKFoldCV(DecisionTreeClassifier clf,
                                        int numFolds) {
    if(clf == null) {
      throw new IllegalArgumentException("Classifier from which statistics are to be collected must not be null");
    }

    Map<Double, Set<Integer>> classMembership = this.getClassMembership(clf);
    List<Set<Integer>> folds = this.getKFolds(classMembership, numFolds);
  }

  public void visit(DecisionTreeClassifier clf) {
    if(clf == null) {
      throw new IllegalArgumentException("Classifier from which statistics are to be collected must not be null");
    }
    this.calculateAccuracy(clf);
    this.performStratifiedKFoldCV(clf, 10);
  }

  public String toString() {
    return String.format("Accuracy: %s", this.getAccuracy());
  }
}
