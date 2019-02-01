package classifier;

import util.NDArray;
import util.Linkable;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.Random;
import util.Interval;
import visitors.ClfVisitorI;

public class DecisionTreeClassifier {
  private NDArray<Double> features;
  private NDArray<Double> classes;
  private List<Integer> trainingSamples;
  private List<Integer> testingSamples;
  private Node trainedClassifier;
  private NDArray<Double> predictedClasses;
  private TrainingStrategy strategy;

  public DecisionTreeClassifier(TrainingStrategy strategyIn) {
    if(strategyIn == null) {
      throw new IllegalArgumentException("Training strategy must not be null");
    }
    this.strategy = strategyIn;
  }

  public void setFeatures(NDArray<Double> featuresIn) {
    if(featuresIn == null) {
      throw new IllegalArgumentException("Array containing training features must not be null");
    }
    this.features = featuresIn;
  }

  public void setClasses(NDArray<Double> classesIn) {
    if(classesIn == null) {
      throw new IllegalArgumentException("Array containing training classes must not be null");
    }
    this.classes = classesIn;
  }

  public void train(NDArray<Double> featuresIn,
                    NDArray<Double> classesIn,
                    double proportion) {
    if(proportion < 0) {
      throw new IllegalArgumentException("Training proportion must be non-negative");
    }
    if(features == null) {
      throw new IllegalArgumentException("Features array must not be null");
    }
    if(classes == null) {
      throw new IllegalArgumentException("Classes array must not be null");
    }

    this.setFeatures(featuresIn);
    this.setClasses(classesIn);

    this.trainedClassifier = null;
    this.trainingSamples = new ArrayList<>();
    this.testingSamples = new ArrayList<>();
    this.predictedClasses = null;

    int numTestSamples = (int) ((1-proportion) * this.features.length(0));

    this.splitData(proportion);
    this.predictedClasses = new NDArray<>(1, numTestSamples);
    this.trainedClassifier = this.strategy.train(this.features,
                                                 this.classes,
                                                 this.trainingSamples);
  }

  public List<Integer> getTestingSamples() {
    return this.testingSamples;
  }

  public NDArray<Double> getClasses() {
    return this.classes;
  }

  public NDArray<Double> getPredictedClasses() {
    return this.predictedClasses;
  }

  public void predict(NDArray features) {
  }

  public NDArray<Double> predict() {
    NDArray<Double> predictions = null;
    int numPredictionsMade = 0;
    for(Integer testSampleIdx : this.testingSamples) {
      if(testSampleIdx == null) {
        throw new IllegalStateException("Test sample indices should never be null");
      }
      double sampleLabel = this.getLabel(this.trainedClassifier,
                                         testSampleIdx.intValue());
      this.predictedClasses.add(sampleLabel, 0, numPredictionsMade++);
    }
    return predictions;
  }

  /** Will be removed **/
  private void splitData(double trainingProportion) {
    if(trainingProportion < 0) {
      throw new IllegalArgumentException("Proportion of data to be used for trainging must be non-negative");
    }

    int numSamples = this.features.length(0);
    int numTrainingSamples = (int) Math.round(trainingProportion * numSamples);

    Set<Integer> testSet = new HashSet<>();
    Set<Integer> trainingSet = new HashSet<>();

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

    this.trainingSamples.addAll(trainingSet);
    this.testingSamples.addAll(testSet);

    int numTestSamples = this.testingSamples.size();
    System.out.println("# training samples: " + numTrainingSamples);
    System.out.println("# testing samples: " + numTestSamples);
    assert numTrainingSamples + numTestSamples == numSamples;
  }

  private double getLabel(Node root, int sampleIdx) {
    List<Node> children = root.getChildren();
    if(children == null) {
      return root.getClassNum();
    }

    List<Interval> intervals = root.getSplitAttributeIntervals();
    Integer splitAttributeIdx = root.getSplitAttributeIndex();
    if(splitAttributeIdx == null) {
      throw new IllegalStateException("Non-leaf node must have a non-null split attribute");
    }
    Double sampleSplitAttributeValue =
              this.features.get(sampleIdx, splitAttributeIdx.intValue());
    int i = 0;
    while(i < intervals.size()) {
      if(sampleSplitAttributeValue >= intervals.get(i).getStart() &&
         sampleSplitAttributeValue <= intervals.get(i).getEnd()) {
        break;
      }
      i++;
    }

    if(i == intervals.size()) {
      throw new IllegalStateException("Sample cannot be placed into interval");
    }

    return this.getLabel(children.get(i), sampleIdx);
  }

  @Override
  public String toString() {
    return "";
  }
}
