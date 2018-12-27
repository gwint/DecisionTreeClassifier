package classifier;
import util.Linkable;
import util.NDArray;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Queue;
import java.util.PriorityQueue;
import java.util.Comparator;
import util.Interval;
import java.util.Arrays;

public class ID3Algorithm implements TrainingStrategy {
  private static final int NUM_DATA_PARTITIONS = 2;
  private static final int MIN_SAMPLES_FOR_SPLIT = 10;
  private List<Integer> usedAttributes;

  public ID3Algorithm() {
    this.usedAttributes = new ArrayList<>();
  }

  /**
   * Creates a decision tree using using the training data passed to the
   * method.
   * @param features An NDArray containing the features for all available
   *                 samples.
   * @param classes An NDArray containing the class label for all available
   *                samples.
   * @return A Linkable object representing the head of the newly created
   *         decision tree.
   */
  public Linkable train(NDArray<Double> features, NDArray<Double> classes,
                        List<Integer> sampleIndices) {
    Node root = new Node(sampleIndices, null);
    this.trainHelper(features, classes, sampleIndices, root);
    return root;
  }

  /**
   * A recursive helper method that helps with the building of the decision
   * tree.  All the arguments are the same as those in train(...) but has
   * another argument to represent the root of the tree to be created.
   */
  private void trainHelper(NDArray<Double> features,
                           NDArray<Double> classes,
                           List<Integer> sampleIndices, Node treeRoot) {

    if(features == null) {
      throw new IllegalArgumentException("Features NDArray must not be null");
    }
    if(classes == null) {
      throw new IllegalArgumentException("Classes NDArray must not be null");
    }
    if(sampleIndices == null) {
      throw new IllegalArgumentException("List of sample indices must not be null");
    }
    if(treeRoot == null) {
      throw new IllegalArgumentException("Decision tree root must not be null");
    }

    // Stopping conditions
      // 1) If all samples are the same, create leaf node and return.
      if(this.areAllClassesIdentical(sampleIndices, classes)) {
        System.out.println("All samples have the same class label");
        treeRoot.setAsLeaf(classes);
        return;
      }
      // 2) If root has no samples, create leaf node w/ random label and
      //    return.
      if(treeRoot.getSampleIndices().size() == 0) {
        System.out.println("Node contains no samples");
        treeRoot.setAsLeaf(classes);
        return;
      }
      // 3) If no attributes left to use, create leaf node and return.
      if(this.usedAttributes.size() == features.length(1)) {
        System.out.println("All attributes have been used already");
        treeRoot.setAsLeaf(classes);
        return;
      }
      // 4) If number of samples is below minimum for spltting, create leaf
      //    and return.
      if(treeRoot.getSampleIndices().size() <
         ID3Algorithm.MIN_SAMPLES_FOR_SPLIT) {
        System.out.println("Node contains less than the minimum amount needed for a split to occur");
        treeRoot.setAsLeaf(classes);
        return;
      }

    // 1) Calculate entropy of every attribute a of the data set
    // 2) Partition set S into subsets using attribute providing minimum
    //    entropy
    int splitFeatureIdx = this.findLowestEntropyFeature(sampleIndices,
                                                        features, classes);
    this.retireAttribute(splitFeatureIdx);
    // 3) Make a decision tree node containing the attribute
    List<Node> childNodes = this.createChildren(splitFeatureIdx, features,
                                                sampleIndices, treeRoot);
    treeRoot.setChildren(childNodes);
    // 4) Recur on subsets using remaining attributes
    for(Node child : childNodes) {
      this.trainHelper(features, classes, child.getSampleIndices(), child);
    }

    System.out.println(splitFeatureIdx);
  }

  /**
   * Helper method used to determine if all the samples in a node have the
   * same class.
   * @param classes NDArray containing classes for all available samples.
   * @param sampleIndices List containing indices of relevant samples.
   * @return true if all samples in the node have the same class label, and
   *         false otherwise.
   */
  private boolean areAllClassesIdentical(List<Integer> sampleIndices,
                                         NDArray<Double> classes) {
    if(sampleIndices == null) {
      throw new IllegalArgumentException("List of sample indices must not be null");
    }
    if(classes == null) {
      throw new IllegalArgumentException("NDArray being checked for homegeneity must not be null");
    }

    boolean areIdentical = sampleIndices.size() > 0;
    for(Integer indexObj : sampleIndices) {
      if(indexObj == null) {
        throw new IllegalStateException("No sample Index should be null");
      }
      int i = indexObj.intValue();
      Double elem = classes.get(i, 0);
      Double nextElem = classes.get(i+1, 0);
      if(elem == null || nextElem == null) {
        throw new IllegalStateException("No class label should be null");
      }
      if(!elem.equals(nextElem)) {
        areIdentical = false;
        break;
      }
    }
    return areIdentical;
  }

  private void retireAttribute(int attributeIndex) {
    if(this.usedAttributes.contains(attributeIndex)) {
      throw new IllegalArgumentException(String.format(
             "Cannot retire previously retired attribute: %d",
             attributeIndex));
    }
    this.usedAttributes.add(attributeIndex);
  }

  /**
   */
  private List<Node> createChildren(int lowestEntropyFeatureIdx,
                                    NDArray<Double> features,
                                    List<Integer> sampleIndices,
                                    Node parent) {

    List<Node> childNodes = new ArrayList<>();

    List<Interval> intervals =
          this.getAttributeIntervals(lowestEntropyFeatureIdx, features);

    List<List<Integer>> partitions =
                 this.partitionSamples(intervals, lowestEntropyFeatureIdx,
                                       features, sampleIndices);

    if(intervals.size() != partitions.size()) {
      throw new IllegalStateException("Number of partitions does not match the number of intervals when creating child nodes");
    }

    for(int i = 0; i < intervals.size(); i++) {
      Node newNode = new Node(partitions.get(i), lowestEntropyFeatureIdx);
      newNode.setParent(parent);
      childNodes.add(newNode);
    }

    return childNodes;
  }

  /**
   */
  private int findLowestEntropyFeature(List<Integer> sampleIndices,
                                       NDArray<Double> features,
                                       NDArray<Double> classes) {

    Map<Integer, Double> entropies = new HashMap<>();
    Queue<Integer> attributeIndicies = new PriorityQueue<>(new Comparator<Integer>() {
      public int compare(Integer idx1, Integer idx2) {
        return Double.compare(entropies.get(idx1.intValue()),
                              entropies.get(idx2.intValue()));
      }
    });

    int numAttributes = features.length(1);
    for(int attributeIndex = 0; attributeIndex < numAttributes;
        attributeIndex++) {
      if(!this.usedAttributes.contains(attributeIndex)) {
        List<Interval> intervals = this.getAttributeIntervals(attributeIndex,
                                                              features);

        List<List<Integer>> partitions = this.partitionSamples(intervals,
                                                               attributeIndex,
                                                               features,
                                                               sampleIndices);

        entropies.put(attributeIndex, calcEntropy(partitions, classes));
        attributeIndicies.add(attributeIndex);
      }
    }
    return attributeIndicies.remove();
  }

  /**
   */
  private double calcEntropy(List<List<Integer>> partitions,
                             NDArray<Double> classes) {
    double entropy = 0.0;

    assert partitions.size() == 2;

    for(List<Integer> sampleIndices : partitions) {
      for(int classLabel : new int[] {0, 1}) {
        double probability = this.getProportion(classLabel, classes,
                                                sampleIndices);
        if(probability > 0) {
          entropy += -probability * (Math.log10(probability) / Math.log10(2));
        }
      }
    }
    System.out.println(String.format("Entropy for attribute: %f",
                                     entropy));
    return entropy;
  }

  /**
   */
  private double getProportion(int label, NDArray<Double> classes,
                               List<Integer> sampleIndices) {
    int numSamplesWithLabel = 0;

    for(Integer sampleIdx : sampleIndices) {
      Double classLabel = classes.get(sampleIdx.intValue(), 0);
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
  private List<Interval> getAttributeIntervals(int attributeIndex,
                                               NDArray<Double> features) {
    double minVal = 0;
    double maxVal = 0;

    List<Interval> intervals = new ArrayList<>();

    for(int sampleIdx = 0; sampleIdx < features.length(0); sampleIdx++) {
      Double attrValue = features.get(sampleIdx, attributeIndex);
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
                                               NDArray<Double> features,
                                               List<Integer> sampleIndices) {

    List<List<Integer>> partitions = new ArrayList<>();
    int numIntervals = intervals.size();
    for(int i = 0; i < numIntervals; i++) {
      partitions.add(new ArrayList<Integer>());
    }

    for(Integer sampleIndex : sampleIndices) {
      int sampleIndexAsInt = sampleIndex.intValue();
      Double attributeValue = features.get(sampleIndexAsInt, attributeIndex);
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
