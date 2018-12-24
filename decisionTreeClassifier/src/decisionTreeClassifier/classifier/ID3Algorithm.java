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

public class ID3Algorithm implements TrainingStrategy {
  private static final int NUM_DATA_PARTITIONS = 2;
  private List<Integer> usedAttributes;

  public ID3Algorithm() {
    this.usedAttributes = new ArrayList<>();
  }

  /**
   */
  public Linkable train(NDArray<Double> features, NDArray<Double> classes,
                        List<Integer> sampleIndices) {
    Node root = new Node(sampleIndices);
    return this.trainHelper(features, classes, sampleIndices, root);
  }

  private Linkable trainHelper(NDArray<Double> features,
                               NDArray<Double> classes,
                               List<Integer> sampleIndices, Node treeRoot) {

    // 1) Calculate entropy of every attribute a of the data set
    // 2) Partition set S into subsets using attribute providing minimum
    //    entropy
    int splitFeatureIdx = this.findLowestEntropyFeature(sampleIndices,
                                                        features, classes);
    // 3) Make a decision tree node containing the attribute
    List<Node> childNodes = this.createChildren(splitFeatureIdx, features,
                                                sampleIndices);
    // 4) Recur on subsets using remaining attributes

    System.out.println(splitFeatureIdx);
    return null;
  }

  /**
   */
  private List<Node> createChildren(int lowestEntropyFeatureIdx,
                                    NDArray<Double> features,
                                    List<Integer> sampleIndices) {

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
      childNodes.add(new Node(partitions.get(i)));
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
