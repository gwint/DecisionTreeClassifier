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
    /*
     1) Calculate entropy of every attribute a of the data set
     2) Partition set S into subsets using attribute providing minimum entropy / maximum information gain
     3) Make a decision tree node containing the attribute
     4) Recur on subsets using remaining attributes
    */
    int splitFeatureIdx = this.findLowestEntropyFeature(sampleIndices,
                                                        features, classes);
    return null;
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
        System.out.println(partitions);

        // Calculate the entropy for each partition and sum them

        System.out.println(intervals);
        System.exit(1);
        entropies.put(attributeIndex,
                      calcEntropy(attributeIndex, sampleIndices,
                                  features, classes));
        attributeIndicies.add(attributeIndex);
      }
    }

    System.out.println(entropies);
    return attributeIndicies.remove();
  }

  /**
   * TODO: Should be calculating entropy for this attribute after splitting
   */
  private double calcEntropy(int attributeIndex, List<Integer> sampleIndices,
                             NDArray<Double> features,
                             NDArray<Double> classes) {
    double entropy = 0.0;

    for(int classLabel : new int[] {0, 1}) {
      double probability = this.getProportion(classLabel, classes,
                                              sampleIndices);
      entropy += -probability * (Math.log10(probability) / Math.log10(2));
      System.out.println("entropy value: " + entropy);
    }
    return entropy;
  }

  /**
   */
  private double getProportion(int label, NDArray<Double> classes,
                               List<Integer> sampleIndices) {
    int numSamplesWithLabel = 0;
    System.out.println("num rows in classes: " + classes.length(0));
    System.out.println("random class label: " + classes.get(14,0));

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

    System.out.println("Now entering getAttributeIntervals()");

    System.out.println(features.get(52,0));
    List<Interval> intervals = new ArrayList<>();

    for(int sampleIdx = 0; sampleIdx < features.length(0); sampleIdx++) {
      Double attrValue = features.get(sampleIdx, attributeIndex);
      double associatedDoubleVal = attrValue.doubleValue();
      if(attrValue == null) {
        System.out.println("Sample index on null: " + sampleIdx);
        System.out.println("Attributre index on null: " + attributeIndex);
      }
      if(associatedDoubleVal < minVal) {
        minVal = attrValue;
      }
      if(associatedDoubleVal > maxVal) {
        maxVal = attrValue;
      }
    }

    System.out.println("max: " + maxVal);
    System.out.println("min: " + minVal);

    double intervalSize = (maxVal - minVal) / ID3Algorithm.NUM_DATA_PARTITIONS;

    for(double start = minVal; start < maxVal; start += intervalSize) {
      intervals.add(new Interval(start, start + intervalSize));
    }

    System.out.println("Now leaving getAttributeIntervals()");

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
