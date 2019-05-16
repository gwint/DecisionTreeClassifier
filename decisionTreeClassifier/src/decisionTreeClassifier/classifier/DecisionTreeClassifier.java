package classifier;

import util.NDArray;
import util.Linkable;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.Random;
import util.Interval;
import util.Dataset;

public class DecisionTreeClassifier {
  private static final int TRAINING_SAMPLES = 0;
  private static final int TESTING_SAMPLES = 1;
  private Dataset dataset;
  private List<Integer> testingSamples;
  private Node trainedClassifier;
  private TrainingStrategy strategy;
  private int maxHeight;

  public DecisionTreeClassifier(TrainingStrategy strategyIn, int maxHeightIn) {
    if(strategyIn == null) {
      throw new IllegalArgumentException("Training strategy must not be null");
    }
    if(maxHeightIn < 1) {
      throw new IllegalArgumentException("Max height of trained classifier must be at least 1");
    }
    this.strategy = strategyIn;
    this.maxHeight = maxHeightIn;
  }

  /**
   * Returns the maximum height that the decision tree can grow to during
   * training.
   *
   * @return the maximum height that the decision tree can attain
   */
  public int getDesiredMaxHeight() {
    return this.maxHeight;
  }

  private void setDataset(Dataset datasetIn) {
    if(datasetIn == null) {
      throw new IllegalArgumentException("Training dataset containing training features must not be null");
    }
    this.dataset = datasetIn;
  }

  /**
   * Builds decision tree based on dataset and list of indices marking each
   * sample as either a training sample or a testing sample.
   *
   * @param dataset The dataset to use to construct the decision tree
   * @param trainingSamples The list of indices of samples in the dataset to
   *                        be used for training
   * @param testingSamples The list of indices of samples in the dataset to be
   *                       be used for testing
   */
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
    this.trainedClassifier = this.strategy.train(dataset,
                                                 trainingSamples,
                                                 this.getDesiredMaxHeight());
  }

  /**
   * Builds decision tree based on dataset where samples are randomly assigned
   * to training and testing sets based on the proportion that should appear
   * in the training set.
   *
   * @param dataset The dataset to use to construct the decision tree
   * @param proportion Proportion of samples from dataset to use for training
   */
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

  /**
   * Returns the dataset used to train the classifier
   *
   * @return The dataset used to train the classifier
   */
  public Dataset getDataset() {
    return this.dataset;
  }

  /**
   * Return the list of indices of samples from the dataset used for
   * testing.
   *
   * @return The indices from the dataset of the samples used for testing
   */
  public List<Integer> getTestingSamples() {
    return this.testingSamples;
  }

  /**
   * Makes predictions as to the target class of each sample of the testing
   * set contained within the classifier.
   *
   * @return A 1xn dimensional array (where n is the number of samples in the
   *         test set contained within the classifier) containing doubles
   *         representing the target class of each sample in the test set
   */
  public NDArray<Double> predict() {
    return this.predict(this.testingSamples);
  }

  /**
   * Makes predictions as to the target class of a sample provided by the user
   *
   * @param testSample A 1xm dimensional array (m is the number of features
   *        used to describe the sample)
   * @return A 1x1 dimensional array (where n is the number of samples in the
   *         test set contained within the classifier) containing doubles
   *         representing the target class of each sample in the test set
   */
  public NDArray<Double> predict(NDArray<Double> testSample) {
    NDArray<Double> prediction = new NDArray<>(1, 1);
    double predictedClass = this.getLabel(this.trainedClassifier, testSample);
    prediction.add(predictedClass, 0, 0);
    return prediction;
  }

  /**
   * Makes predictions as to the target class of a sample provided by the user
   *
   * @param testingSampleIndices A list of indices into the dataset used to
   *     train the classifier that will be used for testing
   * @return A 1x1 dimensional array (where n is the number of samples in the
   *         test set contained within the classifier) containing doubles
   *         representing the target class of each sample in the test set
   */
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

  private double getLabel(Node root, NDArray<Double> testSample) {
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
                           testSample.get(0, splitAttributeIdx.intValue());

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

    return this.getLabel(children.get(i), testSample);
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
