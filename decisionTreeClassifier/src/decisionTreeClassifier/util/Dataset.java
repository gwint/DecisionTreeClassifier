package util;

import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.Random;
import java.util.Iterator;

public class Dataset {
  private NDArray<Double> features;
  private NDArray<Double> classes;

  public Dataset(NDArray<Double> featuresIn,
                 NDArray<Double> classesIn) {
    if(featuresIn == null) {
      throw new IllegalArgumentException("Features array must not be null");
    }
    if(classesIn == null) {
      throw new IllegalArgumentException("Classes array must not be null");
    }
    this.features = featuresIn;
    this.classes = classesIn;
  }

  public NDArray<Double> getClasses() {
    return this.classes;
  }

  public NDArray<Double> getFeatures() {
    return this.features;
  }

  public List<List<Integer>> getSplitSampleIndices(double trainingProportion) {
    if(trainingProportion < 0) {
      throw new IllegalArgumentException("Proportion of data to be used for trainging must be non-negative");
    }

    int numSamples = this.features.length(0);
    int numTrainingSamples = (int) Math.round(trainingProportion * numSamples);

    List<List<Integer>> split = new ArrayList<>();

    List<Integer> trainingList = new ArrayList<>();
    List<Integer> testingList = new ArrayList<>();
    Set<Integer> trainingSet = new HashSet<>();
    Set<Integer> testSet = new HashSet<>();

    Random randNumGen = new Random();
    while(trainingSet.size() < numTrainingSamples) {
      int randInt = Math.abs(randNumGen.nextInt()) % numSamples;
      if(!trainingSet.contains(randInt)) {
        trainingSet.add(randInt);
      }
    }

    for(int sampleIdx = 0; sampleIdx < numSamples; sampleIdx++) {
      if(!trainingSet.contains(sampleIdx)) {
        testSet.add(sampleIdx);
      }
    }

    trainingList.addAll(trainingSet);
    testingList.addAll(testSet);

    split.add(trainingList);
    split.add(testingList);

    int numTestSamples = testingList.size();
    assert numTrainingSamples + numTestSamples == numSamples;

    return split;
  }

  private Map<Double, Set<Integer>>
  getClassMembership() {
    Map<Double, Set<Integer>> classMembership = new HashMap<>();
    NDArray<Double> allClasses = this.classes;

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

  public List<Set<Integer>>
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

  public Double getFeatureValue(int sampleIndex, int featureIndex) {
    if(sampleIndex < 0 || sampleIndex >= this.features.length(0)) {
      throw new IllegalArgumentException("Sample index is invalid: " + sampleIndex);
    }
    if(featureIndex < 0 || featureIndex >= this.features.length(1)) {
      throw new IllegalArgumentException("Feature index is invalid: " + featureIndex);
    }
    return this.features.get(sampleIndex, featureIndex);
  }

  public Double getClassLabel(int sampleIndex) {
    if(sampleIndex < 0 || sampleIndex >= this.classes.length(0)) {
      throw new IllegalArgumentException("Sample index is invalid: " + sampleIndex);
    }
    return this.classes.get(sampleIndex, 0);
  }

  public int size() {
    return this.features.length(0);
  }

  public int getNumColumns() {
    return this.features.length(1);
  }
}
