package visitors;

import java.util.List;
import util.NDArray;
import classifier.Node;

public class HomogenousVisitor implements VisitorI {
  private Boolean isHomogenous;

  public HomogenousVisitor() {
    this.isHomogenous = null;
  }

  private Boolean getHomogeneityFlag() {
    return this.isHomogenous;
  }

  private void setHomogeneityFlag(Boolean flagValue) {
    this.isHomogenous = flagValue;
  }

  public boolean isNodeHomogenous() {
    Boolean homogeneityFlag = this.getHomogeneityFlag();

    if(homogeneityFlag == null) {
      throw new IllegalStateException("Homogeneity flag must be set before its value is determined");
    }
    return homogeneityFlag.booleanValue();
  }

  public void visit(Node aNode) {
    List<Integer> sampleIndices = aNode.getSampleIndices();
    NDArray<Double> classes = aNode.getClasses();
    NDArray<Double> features = aNode.getFeatures();

    if(sampleIndices == null) {
      throw new IllegalArgumentException("List of sample indices must not be null");
    }
    if(classes == null) {
      throw new IllegalArgumentException("NDArray being checked for homegeneity must not be null");
    }

    boolean areIdentical = false;
    Double onlyClassPresent = null;
    for(int sampleIndexIndex = 0; sampleIndexIndex < sampleIndices.size() - 1;
        sampleIndexIndex++) {
      Integer indexObj = sampleIndices.get(sampleIndexIndex);
      if(indexObj == null) {
        throw new IllegalStateException("No sample Index should be null");
      }
      int i = indexObj.intValue();

      if(onlyClassPresent == null) {
        onlyClassPresent = classes.get(i, 0);
      }

      Double elem = classes.get(i, 0);
      if(elem == null) {
        throw new IllegalStateException("No class label should be null");
      }
      if(!elem.equals(onlyClassPresent)) {
        areIdentical = false;
        break;
      }
    }
    this.setHomogeneityFlag(Boolean.valueOf(areIdentical));
  }
}
