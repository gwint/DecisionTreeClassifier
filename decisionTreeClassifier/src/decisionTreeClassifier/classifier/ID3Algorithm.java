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
import java.util.Iterator;
import java.util.Set;

public class ID3Algorithm implements TrainingStrategy {
  private static final int NUM_DATA_PARTITIONS = 2;
  private static final int MIN_SAMPLES_FOR_SPLIT = 10;
  private List<Integer> usedAttributes;
  private NDArray<Double> features;
  private NDArray<Double> classes;
  private List<Integer> trainingSampleIndices;

  public ID3Algorithm() {
    this.features = null;
    this.classes = null;
    this.usedAttributes = new ArrayList<>();
  }

  private void setFeatures(NDArray<Double> featuresIn) {
    if(featuresIn == null) {
      throw new IllegalArgumentException("NDArray containing features must not be null");
    }
    this.features = featuresIn;
  }

  private void setClasses(NDArray<Double> classesIn) {
    if(classesIn == null) {
      throw new IllegalArgumentException("NDArray containing classes must not be null");
    }
    this.classes = classesIn;
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
  public Linkable train(NDArray<Double> features,
                        NDArray<Double> classes,
                        List<Integer> trainingSampleIndices) {
    this.setFeatures(features);
    this.setClasses(classes);
    Node root = new Node(trainingSampleIndices, null);
    this.trainHelper(trainingSampleIndices, root);
    return root;
  }

  /**
   * A recursive helper method that helps with the building of the decision
   * tree.  All the arguments are the same as those in train(...) but has
   * another argument to represent the root of the tree to be created.
   */
  private void trainHelper(List<Integer> trainingSampleIndices,
                           Node treeRoot) {

    if(features == null) {
      throw new IllegalArgumentException("Features NDArray must not be null");
    }
    if(classes == null) {
      throw new IllegalArgumentException("Classes NDArray must not be null");
    }
    if(trainingSampleIndices == null) {
      throw new IllegalArgumentException("List of sample indices must not be null");
    }
    if(treeRoot == null) {
      throw new IllegalArgumentException("Decision tree root must not be null");
    }
    if(this.features == null) {
      throw new IllegalStateException("Features should be set to a non-null object before this method is called");
    }
    if(this.classes == null) {
      throw new IllegalStateException("Classes should be set to a non-null object before this method is called");
    }

    // Stopping conditions:
      // 1) If all samples are the same, create leaf node and return.
      if(this.areAllClassesIdentical(trainingSampleIndices)) {
        System.out.println("All samples have the same class label");
        treeRoot.assignLabel(this.getLabel(treeRoot.getSampleIndices()));
        treeRoot.setAsLeaf();
        return;
      }
      // 2) If root has no samples, create leaf node w/ random label and
      //    return.
      if(treeRoot.getSampleIndices().size() == 0) {
        System.out.println("Node contains no samples");
        treeRoot.assignLabel(this.getLabel(treeRoot.getSampleIndices()));
        treeRoot.setAsLeaf();
        return;
      }
      // 3) If no attributes left to use, create leaf node and return.
      if(this.usedAttributes.size() == features.length(1)) {
        System.out.println("All attributes have been used already");
        treeRoot.assignLabel(this.getLabel(treeRoot.getSampleIndices()));
        treeRoot.setAsLeaf();
        return;
      }
      // 4) If number of samples is below minimum for spltting, create leaf
      //    and return.
      if(treeRoot.getSampleIndices().size() <
         ID3Algorithm.MIN_SAMPLES_FOR_SPLIT) {
        System.out.println("Node contains less than the minimum amount needed for a split to occur");
        treeRoot.assignLabel(this.getLabel(treeRoot.getSampleIndices()));
        treeRoot.setAsLeaf();
        return;
      }

    int splitFeatureIdx =
               this.findLowestEntropyFeature(trainingSampleIndices);

    this.retireAttribute(splitFeatureIdx);

    List<Node> childNodes = this.createChildren(splitFeatureIdx,
                                                trainingSampleIndices,
                                                treeRoot);
    treeRoot.setChildren(childNodes);

    for(Node child : childNodes) {
      this.trainHelper(child.getSampleIndices(), child);
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
  private boolean areAllClassesIdentical(List<Integer> sampleIndices) {
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
      Double elem = this.classes.get(i, 0);
      Double nextElem = this.classes.get(i+1, 0);
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

  private double getLabel(List<Integer> sampleIndices) {
    double classLabel = 0.0;
    if(sampleIndices.size() == 0) {
      classLabel = this.parent.getLabel(classes);
    }
    else {
      int numSamples = sampleIndices.size();
      Map<Double, Integer> classCounts = new HashMap<>();
      for(Integer sampleIdx : sampleIndices) {
        if(sampleIdx == null) {
          throw new IllegalArgumentException("List of sample indices should not be null");
        }
        int i = sampleIdx.intValue();
        Double label = classes.get(sampleIdx, 0);
        if(label == null) {
          throw new IllegalArgumentException("Every class label should be non-null");
        }

        if(!classCounts.containsKey(label)) {
          classCounts.put(label, 1);
        }
        else {
          classCounts.put(label, classCounts.get(label).intValue() + 1);
        }

        Set<Double> allLabels = classCounts.keySet();
        Iterator<Double> labelIter = allLabels.iterator();
        Iterator<Double> initializer = allLabels.iterator();
        classLabel = initializer.next();

        while(labelIter.hasNext()) {
          Double aLabel = labelIter.next();
          if(classCounts.get(aLabel) > classCounts.get(classLabel)) {
            classLabel = aLabel.doubleValue();
          }
        }
      }
    }

    return 0.0;
  }

  /**
   */
  private List<Node> createChildren(int lowestEntropyFeatureIdx,
                                    List<Integer> sampleIndices,
                                    Node parent) {

    List<Node> childNodes = new ArrayList<>();

    List<Interval> intervals =
          this.getAttributeIntervals(lowestEntropyFeatureIdx);

    List<List<Integer>> partitions =
                 this.partitionSamples(intervals,
                                       lowestEntropyFeatureIdx,
                                       sampleIndices);

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
  private int findLowestEntropyFeature(List<Integer> sampleIndices) {

    Map<Integer, Double> entropies = new HashMap<>();
    Queue<Integer> attributeIndicies = new PriorityQueue<>(new Comparator<Integer>() {
      public int compare(Integer idx1, Integer idx2) {
        return Double.compare(entropies.get(idx1.intValue()),
                              entropies.get(idx2.intValue()));
      }
    });

    int numAttributes = this.features.length(1);
    for(int attributeIndex = 0; attributeIndex < numAttributes;
        attributeIndex++) {
      if(!this.usedAttributes.contains(attributeIndex)) {
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

    assert partitions.size() == 2;

    for(List<Integer> sampleIndices : partitions) {
      for(int classLabel : new int[] {0, 1}) {
        double probability = this.getProportion(classLabel,
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
  private double getProportion(int label, List<Integer> sampleIndices) {
    int numSamplesWithLabel = 0;

    for(Integer sampleIdx : sampleIndices) {
      Double classLabel = this.classes.get(sampleIdx.intValue(), 0);
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

    for(int sampleIdx = 0; sampleIdx < features.length(0); sampleIdx++) {
      Double attrValue = this.features.get(sampleIdx, attributeIndex);
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
      Double attributeValue = this.features.get(sampleIndexAsInt,
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
