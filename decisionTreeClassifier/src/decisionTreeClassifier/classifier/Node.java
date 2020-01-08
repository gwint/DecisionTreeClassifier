package classifier;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;
import util.NDArray;
import visitors.VisitorI;

public class Node {
    private static final int NO_LABEL_ASSIGNED = -1;
    private static final int NO_INDEX_ASSIGNED = -1;
    private static Set<Integer> attributesAlreadyUsedToSplitANode;

    private List<Node> children;
    private int label;
    private int indexOfFeatureToUseToSplitSamplesUp;
    private Node parent;

    private NDArray features;
    private NDArray classes;

    static {
        Node.attributesAlreadyUsedToSplitANode = new HashSet<>();
    }

    /**
     *  Creates a Node that will form part of a decision tree classifier.
     *  @param allFeatures NDArray An array containing the features to be used
     *                             during creation of the decision tree classifier.
     *  @param allClasses NDArray An array containing the class label corresponding
     *                            to each of the samples in the features array.
     */
    public Node(NDArray allFeatures, NDArray allClasses) {
        if(allFeatures == null || allClasses == null) {
            throw new IllegalArgumentException("Neither the features nor classes used to create a Node may be null");
        }

        this.parent = null;
        this.children = new ArrayList<>();
        this.label = Node.NO_LABEL_ASSIGNED;
        this.indexOfFeatureToUseToSplitSamplesUp = Node.NO_INDEX_ASSIGNED;
        this.features = allFeatures;
        this.classes = allClasses;
    }

    /**
     *  Note that a particular feature will be used to spawn child Node's
     *  that descend from this Node.  The usage of this index value is made
     *  aware to all Nodes via inclusion in a static container.
     *  @param index int An integer representing the column index that should
     *                   be used to create child Nodes.
     *  @return None.
     */
    public void setIndexOfFeatureToUseToSplitSamplesUp(int index) {
        this.indexOfFeatureToUseToSplitSamplesUp = index;
        Node.attributesAlreadyUsedToSplitANode.add(index);
    }

    public int getIndexOfFeatureToUseToSplitSamplesUp() {
        return this.indexOfFeatureToUseToSplitSamplesUp;
    }

    public void setParent(Node parentNode) {
        this.parent = parentNode;
    }

    public void setLabel(double labelIn) {
        this.label = labelIn;
    }

    public double getClassLabel() {
        return this.label;
    }

    public Node getParent() {
        return this.parent;
    }

    /**
     *  Determines whether all samples included in this Node have the same
     *  class.
     *  @return A boolean that is true if all samples included in this Node
     *          have the same class label and false otherwise.
     */
    public boolean doIncludedSamplesAllHaveSameClass() {
        boolean haveSameClass = false;
        Double onlyClassPresent = null;

        for(int i = 0; i < this.classes.length; i++) {
            Double currClass = (Double) this.classes.get(i);

            if(onlyClassPresent == null) {
                onlyClassPresent = currClass;
            }

            if(!onlyClassPresent.equals(currClass)) {
                haveSameClass = false;
                break;
            }
        }

        return haveSameClass;
    }

    public List<Integer> getIntervalsForFeatureUsedToSplitSamples() {
        return null;
    }

    public List<Node> getChildren() {
        return this.children;
    }
}
