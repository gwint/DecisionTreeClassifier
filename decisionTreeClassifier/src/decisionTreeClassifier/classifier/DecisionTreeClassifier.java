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
import util.Dataset;

public class DecisionTreeClassifier {
  private static final int TRAINING_SAMPLES = 0;
  private static final int TESTING_SAMPLES = 1;
  private Dataset dataset;
  private List<Integer> testingSamples;
  private Node trainedClassifier;
  private TrainingStrategy strategy;

  public DecisionTreeClassifier(TrainingStrategy strategyIn) {
    if(strategyIn == null) {
      throw new IllegalArgumentException("Training strategy must not be null");
    }
    this.strategy = strategyIn;
  }

  public void setDataset(Dataset datasetIn) {
    if(datasetIn == null) {
      throw new IllegalArgumentException("Training dataset containing training features must not be null");
    }
    this.dataset = datasetIn;
  }

  public void train(Dataset dataset,
                    List<Integer> trainingSamples,
                    List<Integer> testingSamples) {
    if(dataset == null) {
      throw new IllegalArgumentException("Training dataset must not be null");
    }
    if(trainingSamples == null) {
      throw new IllegalArgumentException("Training sample indices must not be null");
    }
    if(testingSamples == null) {
      throw new IllegalArgumentException("Testing sample indices must not be null");
    }

    this.setDataset(dataset);
    this.testingSamples = testingSamples;
    this.trainedClassifier = this.strategy.train(dataset, trainingSamples);
  }

  public void train(Dataset dataset, double proportion) {
    if(proportion < 0) {
      throw new IllegalArgumentException("Training proportion must be non-negative");
    }
    if(dataset == null) {
      throw new IllegalArgumentException("Training dataset must not be null");
    }

    List<List<Integer>> split = dataset.getSplitSampleIndices(proportion);
    this.train(dataset,
               split.get(DecisionTreeClassifier.TRAINING_SAMPLES),
               split.get(DecisionTreeClassifier.TESTING_SAMPLES));
  }

  public Dataset getDataset() {
    return this.dataset;
  }

  public List<Integer> getTestingSamples() {
    return this.testingSamples;
  }

  public NDArray<Double> predict() {
    return this.predict(this.testingSamples);
  }


  public NDArray<Double> predict(List<Integer> testingSampleIndices) {
    NDArray<Double> predictions =
                        new NDArray<>(1, testingSampleIndices.size());;
    int numPredictionsMade = 0;
    for(Integer testSampleIdx : testingSampleIndices) {
      if(testSampleIdx == null) {
        throw new IllegalStateException("Test sample indices should never be null");
      }
      double sampleLabel = this.getLabel(this.trainedClassifier,
                                         testSampleIdx.intValue());
      predictions.add(sampleLabel, 0, numPredictionsMade++);
    }
    return predictions;
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
       this.dataset.getFeatureValue(sampleIdx, splitAttributeIdx.intValue());

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
