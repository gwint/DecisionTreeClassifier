package classifier;

import util.Linkable;
import java.util.List;
import java.util.ArrayList;

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

  public void setAsLeaf() {
    this.isLeaf = true;
    this.label = this.getLabel();
  }

  public double getLabel() {
    return 0.0;
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
    String strForm = "";
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
