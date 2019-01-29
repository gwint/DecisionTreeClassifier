package classifier;

import util.NDArray;
import util.Linkable;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;
import util.Interval;

public class DecisionTreeClassifier {
  private NDArray<Double> features;
  private NDArray<Double> classes;
  private List<Integer> trainingSamples;
  private List<Integer> testingSamples;
  private Node trainedClassifier;
  private NDArray<Double> predictedClasses;

  public DecisionTreeClassifier(NDArray<Double> allFeaturesIn,
                                NDArray<Double> allClassesIn) {
    if(allFeaturesIn == null) {
      throw new IllegalArgumentException("Array containing training features must not be null");
    }
    if(allClassesIn == null) {
      throw new IllegalArgumentException("Array containing training classes must not be null");
    }

    this.features = allFeaturesIn;
    this.classes = allClassesIn;
    this.trainedClassifier = null;
    this.trainingSamples = new ArrayList<>();
    this.testingSamples = new ArrayList<>();
    this.predictedClasses = null;

    if(this.features.length(0) != this.classes.length(0)) {
      throw new IllegalArgumentException("Mismatch between number of samples and the number of classes");
    }
  }

  public void train(TrainingStrategy strat, double proportion) {
    if(strat == null) {
      throw new IllegalArgumentException("Training strategy must not be null");
    }
    this.splitData(proportion);
    this.predictedClasses = new NDArray<>(1, this.trainingSamples.size());
    this.trainedClassifier = strat.train(this.features, this.classes,
                                         this.trainingSamples);
  }

  public void predict(NDArray features) {
  }

  public void predict() {
    int numPredictionsMade = 0;
    for(Integer testSampleIdx : this.testingSamples) {
      if(testSampleIdx == null) {
        throw new IllegalStateException("Test sample indices should never be null");
      }
      double sampleLabel = this.getLabel(this.trainedClassifier,
                                         testSampleIdx.intValue());
      System.out.println(sampleLabel);
      this.predictedClasses.add(sampleLabel, 0, numPredictionsMade++);
    }
  }

  private void splitData(double trainingProportion) {
    if(trainingProportion < 0) {
      throw new IllegalArgumentException("Proportion of data to be used for trainging must be non-negative");
    }

    int numSamples = this.features.length(0);
    int numTrainingSamples = (int) Math.round(trainingProportion * numSamples);

    Random randNumGen = new Random(0);
    while(this.trainingSamples.size() < numTrainingSamples) {
      int randInt = Math.abs(randNumGen.nextInt()) % numSamples;
      if(!this.trainingSamples.contains(randInt)) {
        this.trainingSamples.add(randInt);
      }
    }

    for(int sampleIdx = 0; sampleIdx < numSamples; sampleIdx++) {
      if(!this.trainingSamples.contains(sampleIdx)) {
        this.testingSamples.add(sampleIdx);
      }
    }
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
