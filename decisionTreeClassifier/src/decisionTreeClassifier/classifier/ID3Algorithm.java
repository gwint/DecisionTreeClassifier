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

public class ID3Algorithm implements TrainingStrategy {
  private List<Integer> usedAttributes;

  public ID3Algorithm() {
    this.usedAttributes = new ArrayList<>();
  }

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
        entropies.put(attributeIndex,
                      calcEntropy(attributeIndex, sampleIndices,
                                  features, classes));
        attributeIndicies.add(attributeIndex);
      }
    }

    return attributeIndicies.remove();
  }

  private double calcEntropy(int attributeIndex, List<Integer> sampleIndices,
                             NDArray<Double> features,
                             NDArray<Double> classes) {
    double entropy = 0.0;

    for(int classLabel : new int[] {0, 1}) {
      double probability = this.getProportion(classLabel, classes,
                                              sampleIndices);
      entropy += -probability * (Math.log10(probability) / Math.log10(2));
    }
    return entropy;
  }

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
}
