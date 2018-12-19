package classifier;
import util.Linkable;
import util.NDArray;
import java.util.List;
import java.util.ArrayList;

public class ID3Algorithm implements TrainingStrategy {
  private List<Integer> usedAttributes;

  public ID3Algorithm() {
    this.usedAttributes = new ArrayList<>();
  }

  public Linkable train(NDArray features, NDArray classes,
                        List<Integer> sampleIndices) {
    Node root = null;
    return this.trainHelper(features, classes, sampleIndices, root);
  }

  private Linkable trainHelper(NDArray features, NDArray classes,
                                List<Integer> sampleIndices, Node treeRoot) {
    /*
     1) Calculate entropy of every attribute a of the data set
     2) Partition set S into subsets using attribute providing minimum entropy / maximum information gain
     3) Make a decision tree node containing the attribute
     4) Recur on subsets using remaining attributes
    */
    return null;
  }

  private double calcEntropy(int attributeIndex, List<Integer> sampleIndices) {
    double entropy = 0.0;

    for(Integer sampleIdx : sampleIndices) {
      double probability = 1.0;
      entropy += -probability * (Math.log10(probability) / Math.log10(2));
    }
    return entropy;
  }

  private double getProportion(int label, NDArray classes,
                               List<Integer> sampleIndices) {
    int numSamplesWithLabel = 0;

    for(Integer sampleIdx : sampleIndices) {
      if(((int) classes.get(sampleIdx.intValue())) == label) {
        numSamplesWithLabel++;
      }
    }
    return ((double) numSamplesWithLabel) / sampleIndices.size();
  }
}
