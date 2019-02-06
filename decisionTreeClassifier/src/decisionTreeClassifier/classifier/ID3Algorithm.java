package classifier;

import util.Linkable;
import util.NDArray;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.Queue;
import java.util.PriorityQueue;
import java.util.Comparator;
import util.Interval;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;
import visitors.LabelVisitor;
import visitors.VisitorI;
import util.Dataset;

public class ID3Algorithm implements TrainingStrategy {
  private static final int NUM_DATA_PARTITIONS = 5;
  private static final int MIN_SAMPLES_FOR_SPLIT = 10;
  private Dataset dataset;
  private List<Integer> trainingSampleIndices;

  public ID3Algorithm() {
    this.dataset = null;
  }

  private void setDataset(Dataset datasetIn) {
    if(datasetIn == null) {
      throw new IllegalArgumentException("Dataset must not be null");
    }
    this.dataset = datasetIn;
  }

  /**
   * Creates a decision tree using using the training data passed to the
   * method.
   * @param classes An NDArray containing the class label for all available
   *                samples.
   * @return A Linkable object representing the head of the newly created
   *         decision tree.
   */
  public Node train(Dataset dataset,
                    List<Integer> trainingSampleIndices,
                    int maxClassifierHeight) {

    if(trainingSampleIndices == null) {
      throw new IllegalArgumentException("List of training sample indices must not be null");
    }
    if(dataset == null) {
      throw new IllegalArgumentException("Dataset must not be null");
    }
    if(maxClassifierHeight < 1) {
      throw new IllegalArgumentException("Trained classifier cannot have desired height less than 1");
    }

    Node root = new Node(trainingSampleIndices,
                         dataset,
                         new HashSet<>());

    this.setDataset(dataset);
    this.trainHelper(root, maxClassifierHeight);
    return root;
  }

  /**
   * A recursive helper method that helps with the building of the decision
   * tree.  All the arguments are the same as those in train(...) but has
   * another argument to represent the root of the tree to be created.
   */
  private void trainHelper(Node treeRoot, int desiredHeight) {
    if(treeRoot == null) {
      throw new IllegalArgumentException("Decision tree root must not be null");
    }

    VisitorI labelAssigner = new LabelVisitor();

    // Stopping conditions:
    // 0) If max height for some node is reached, it becomes a leaf node.
    if(desiredHeight == 1) {
      treeRoot.accept(labelAssigner);
      return;
    }
    // 1) If all samples are the same, create leaf node and return.
    if(treeRoot.isHomogenous()) {
      treeRoot.accept(labelAssigner);
      return;
    }
    // 2) If root has no samples, create leaf node w/ random label and
    //    return.
    if(treeRoot.getSampleIndices().size() == 0) {
      treeRoot.accept(labelAssigner);
      return;
    }
    // 3) If no attributes left to use, create leaf node and return.
    if(treeRoot.getUsedAttributes().size() == this.dataset.getNumColumns()) {
      treeRoot.accept(labelAssigner);
      return;
    }
    // 4) If number of samples is below minimum for spltting, create leaf
    //    and return.
    if(treeRoot.getSampleIndices().size() <
       ID3Algorithm.MIN_SAMPLES_FOR_SPLIT) {
      treeRoot.accept(labelAssigner);
      return;
    }

    int splitFeatureIdx =
               this.findLowestEntropyFeature(treeRoot.getSampleIndices(),
                                             treeRoot.getUsedAttributes());

    treeRoot.setSplitAttribute(splitFeatureIdx);

    List<Interval> intervals =
          this.getAttributeIntervals(splitFeatureIdx);

    treeRoot.setSplitAttributeIntervals(intervals);

    List<Node> childNodes = this.createChildren(splitFeatureIdx,
                                                treeRoot,
                                                intervals);
    treeRoot.setChildren(childNodes);

    for(Node child : childNodes) {
      this.trainHelper(child, desiredHeight - 1);
    }
  }

  /**
   */
  private List<Node> createChildren(int lowestEntropyFeatureIdx,
                                    Node parent, List<Interval> intervals) {

    List<Node> childNodes = new ArrayList<>();

    List<List<Integer>> partitions =
                 this.partitionSamples(intervals,
                                       lowestEntropyFeatureIdx,
                                       parent.getSampleIndices());

    if(intervals.size() != partitions.size()) {
      throw new IllegalStateException("Number of partitions does not match the number of intervals when creating child nodes");
    }

    for(int i = 0; i < intervals.size(); i++) {
      Node newNode = new Node(partitions.get(i),
                              this.dataset,
                              new HashSet(parent.getUsedAttributes()));
      newNode.setParent(parent);
      childNodes.add(newNode);
    }

    return childNodes;
  }

  /**
   */
  private int findLowestEntropyFeature(List<Integer> sampleIndices,
                                       Set<Integer> usedAttributes) {

    Map<Integer, Double> entropies = new HashMap<>();
    Queue<Integer> attributeIndicies = new PriorityQueue<>(new Comparator<Integer>() {
      public int compare(Integer idx1, Integer idx2) {
        return Double.compare(entropies.get(idx1.intValue()),
                              entropies.get(idx2.intValue()));
      }
    });

    int numAttributes = this.dataset.getNumColumns();
    for(int attributeIndex = 0; attributeIndex < numAttributes;
        attributeIndex++) {
      if(!usedAttributes.contains(attributeIndex)) {
        List<Interval> intervals = this.getAttributeIntervals(attributeIndex);

        List<List<Integer>> partitions = this.partitionSamples(intervals,
                                                               attributeIndex,
                                                               sampleIndices);

        entropies.put(attributeIndex, calcEntropy(partitions));
        attributeIndicies.add(attributeIndex);
      }
    }
    return attributeIndicies.remove();
  }

  /**
   */
  private double calcEntropy(List<List<Integer>> partitions) {
    double entropy = 0.0;

    for(List<Integer> sampleIndices : partitions) {
      for(int classLabel : new int[] {0, 1}) {
        double probability = this.getProportion(classLabel,
                                                sampleIndices);
        if(probability > 0) {
          entropy += -probability * (Math.log10(probability) / Math.log10(2));
        }
      }
    }
    //System.out.println(String.format("Entropy for attribute: %f",
    //                                 entropy));
    return entropy;
  }

  /**
   */
  private double getProportion(int label, List<Integer> sampleIndices) {
    int numSamplesWithLabel = 0;

    for(Integer sampleIdx : sampleIndices) {
      Double classLabel = this.dataset.getClassLabel(sampleIdx.intValue());
      if(classLabel == null) {
        System.out.println("sample index value with null class label:" +
                           sampleIdx);
        throw new IllegalStateException("No class labels should be null");
      }
      if(classLabel.intValue() == label) {
        numSamplesWithLabel++;
      }
    }
    return ((double) numSamplesWithLabel) / sampleIndices.size();
  }

  /**
   */
  private List<Interval> getAttributeIntervals(int attributeIndex) {
    double minVal = 0;
    double maxVal = 0;

    List<Interval> intervals = new ArrayList<>();

    for(int sampleIdx = 0; sampleIdx < this.dataset.size(); sampleIdx++) {
      Double attrValue = this.dataset.getFeatureValue(sampleIdx,
                                                      attributeIndex);
      double associatedDoubleVal = attrValue.doubleValue();
      if(attrValue == null) {
        System.err.println("Attribute value Double object should not be null");
        System.err.println("Sample index on null: " + sampleIdx);
        System.err.println("Attributre index on null: " + attributeIndex);
        System.exit(1);
      }
      if(associatedDoubleVal < minVal) {
        minVal = attrValue;
      }
      if(associatedDoubleVal > maxVal) {
        maxVal = attrValue;
      }
    }

    double intervalSize = (maxVal - minVal) / ID3Algorithm.NUM_DATA_PARTITIONS;

    for(double start = minVal; start < maxVal; start += intervalSize) {
      intervals.add(new Interval(start, start + intervalSize));
    }
    return intervals;
  }

  private List<List<Integer>> partitionSamples(List<Interval> intervals,
                                               int attributeIndex,
                                               List<Integer> sampleIndices) {

    List<List<Integer>> partitions = new ArrayList<>();
    int numIntervals = intervals.size();
    for(int i = 0; i < numIntervals; i++) {
      partitions.add(new ArrayList<Integer>());
    }

    for(Integer sampleIndex : sampleIndices) {
      int sampleIndexAsInt = sampleIndex.intValue();
      Double attributeValue = this.dataset.getFeatureValue(sampleIndexAsInt,
                                                           attributeIndex);
      if(attributeValue == null) {
        System.err.println(
            String.format("Attribute at index %d should not be null",
                          attributeIndex));
        System.exit(1);
      }
      double attrValueAsDouble = attributeValue.doubleValue();

      for(int intervalIdx = 0; intervalIdx < numIntervals; intervalIdx++) {
        Interval interval = intervals.get(intervalIdx);
        if(interval == null) {
          System.err.println("Interval objects should never be null");
          System.exit(1);
        }
        if(attrValueAsDouble < interval.getEnd()) {
          partitions.get(intervalIdx).add(sampleIndex);
          break;
        }
      }
    }
    return partitions;
  }
}
