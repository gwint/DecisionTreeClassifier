package classifier;

import util.Linkable;
import java.util.List;
import java.util.ArrayList;

public class Node implements Linkable, Cloneable {
  private static final int INVALID_NODE_ID = -1;
  private static final int NO_LABEL_ASSIGNED = -1;
  private static int nodeCount = 0;
  private int nodeId;
  private Node left;
  private Node right;
  private List<Integer> sampleIndices;
  private int label;

  public Node(CharSequence bNumberIn) {
    this.left = null;
    this.right = null;
    this.nodeId = Node.nodeCount++;
    this.sampleIndices = new ArrayList<>();
    this.label = Node.NO_LABEL_ASSIGNED;
  }

  public static int getNodeCount() {
    return Node.nodeCount;
  }

  /**
   Creates a dummy Node object that cannot be used for insertion or deletion.
   It is meant to represent the Node equivalent of null and avoid
   nullpointerexception when no input.txt is empty.
   @return A dummy node created via private constructor.
   */
  public static Node getDummyNode() {
    return new Node();
  }

  private Node() {
    this.nodeId = Node.INVALID_NODE_ID;
    this.left = null;
    this.right = null;
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

  public Node getLeftChild() {
    return this.left;
  }

  public Node getRightChild() {
    return this.right;
  }

  public void setLeftChild(Node aNodeIn) {
    if(aNodeIn == null) {
      throw new IllegalArgumentException("Cannot set left child to null.");
    }
    this.left = aNodeIn;
  }

  public void setRightChild(Node aNodeIn) {
    if(aNodeIn == null) {
      throw new IllegalArgumentException("Cannot set right child to null.");
    }
    this.right = aNodeIn;
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
