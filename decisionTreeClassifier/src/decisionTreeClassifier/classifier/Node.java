package classifier;

import util.Linkable;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.Iterator;
import util.NDArray;

public class Node implements Linkable, Cloneable {
  private static final int INVALID_NODE_ID = -1;
  private static final int NO_LABEL_ASSIGNED = -1;
  private static int nodeCount = 0;
  private int nodeId;
  private List<Node> children;
  private List<Integer> sampleIndices;
  private double label;
  private Integer splitAttributeIndex;
  private boolean isLeaf;
  private Node parent;

  public Node(List<Integer> sampleIndicesIn, Integer attributeIndex) {
    if(sampleIndicesIn == null) {
      throw new IllegalArgumentException("List of sample indices must not be null");
    }

    this.isLeaf = false;
    this.sampleIndices = sampleIndicesIn;
    this.children = null;
    this.nodeId = Node.nodeCount++;
    this.label = Node.NO_LABEL_ASSIGNED;
    this.splitAttributeIndex = attributeIndex;
  }

  public static int getNodeCount() {
    return Node.nodeCount;
  }

  public boolean isLeaf() {
    return this.isLeaf;
  }

  public void setParent(Node parentNode) {
    this.parent = parentNode;
  }

  /**
   Creates a dummy Node object that cannot be used for insertion or deletion.
   It is meant to represent the Node equivalent of null and avoid
   nullpointerexception when no input.txt is empty.
   @return A dummy node created via private constructor.
   */
  public static Node getLeafNode(double classLabel) {
    return new Node(classLabel);
  }

  public void assignLabel(double labelIn) {
    this.label = labelIn;
  }

  public void setAsLeaf() {
    this.isLeaf = true;
  }

  public double getLabel(NDArray<Double> classes) {
    double classLabel = (double) Node.NO_LABEL_ASSIGNED;
    if(this.getSampleIndices().size() == 0) {
      classLabel = this.parent.getLabel(classes);
    }
    else {
      int numSamples = this.getSampleIndices().size();
      Map<Double, Integer> classCounts = new HashMap<>();
      for(Integer sampleIdx : this.getSampleIndices()) {
        if(sampleIdx == null) {
          throw new IllegalArgumentException("List of sample indices should not contain any null elements");
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

    return classLabel;
  }

  public List<Integer> getSampleIndices() {
    if(this.sampleIndices == null) {
      throw new UnsupportedOperationException("Cannot get the sample indices of a leaf node");
    }
    return this.sampleIndices;
  }

  private Node(double classLabel) {
    this.label = classLabel;
    this.isLeaf = true;
    this.nodeId = Node.INVALID_NODE_ID;
    this.children = null;
  }

  @Override
  public String toString() {
    String strForm = String.format("Node Type: %s \n",
                                   (this.isLeaf) ? "Leaf" : "Non-Leaf");
    return strForm;
  }

  @Override
  public void finalize() {}

  @Override
  public int hashCode() {
    return Node.getNodeCount();
  }

  /**
   Two Node objects are equal if they have the same b-number and contain
   the same classes in the same order.
   @return true if Nodes are equal, false otherwise.
   */
  @Override
  public boolean equals(Object aNode) {
    return false;
  }

  public List<Node> getChildren() {
    return this.children;
  }

  public void setChildren(List<Node> childNodes) {
    if(childNodes == null) {
      throw new IllegalArgumentException("Cannot set list of child nodes to null.");
    }
    this.children = childNodes;
  }

  /**
   Determines if Node is valid (meaning it has a valid b-number) or if it is
   a dummy Node.
   @return true if this Node has a valid 3 digit b-number, false otherwise
   */
  public boolean isDummy() {
    return this.nodeId == Node.INVALID_NODE_ID;
  }
}
