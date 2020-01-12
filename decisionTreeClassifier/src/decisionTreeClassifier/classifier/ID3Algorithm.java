package classifier;

import util.NDArray;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.Queue;
import java.util.PriorityQueue;
import java.util.Comparator;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;

public class ID3Algorithm implements TrainingStrategy {
    private static final int NUM_DATA_PARTITIONS = 5;
    private static final int MIN_SAMPLES_FOR_SPLIT = 10;

    /**
     *  Creates a decision tree using using the training data passed to the
     *  method.
     *  @param features An NDArray containing the features for all available
     *                  samples.
     *  @param classes An NDArray containing the class label for all available
     *                 samples.
     *  @param maxClassifierHeight int The maximum height of the decision tree
     *                                 classifier to be created.
     *  @return A Linkable object representing the head of the newly created
     *          decision tree.
     */
    public Node createModel(NDArray features, NDArray classes, int maxClassifierHeight) {
        if(features == null || classes == null) {
            throw new IllegalArgumentException("Neither features nor classes may be null");
        }
        if(maxClassifierHeight < 1) {
            throw new IllegalArgumentException("Trained classifier cannot have desired height less than 1");
        }

        Node root = new Node(features, classes);

        this.trainHelper(root, maxClassifierHeight);

        return root;
    }

    /**
     *  A recursive helper method that helps with the building of the decision
     *  tree.  All the arguments are the same as those in train(...) but has
     *  another argument to represent the root of the tree to be created.
     */
    private static void trainHelper(Node treeRoot, int maximumTreeHeight) {
        if(treeRoot == null) {
            throw new IllegalArgumentException("Decision tree root must not be null");
        }

        // Stopping conditions:
        // 0) If max height for some node is reached, it becomes a leaf node.
        if(maximumTreeHeight == 1) {
            this.labelNode(treeRoot);
            return;
        }
        // 1) If all samples are the same, create leaf node and return.
        if(treeRoot.doIncludedSamplesAllHaveSameClass()) {
            this.labelNode(treeRoot);
            return;
        }
        // 2) If root has no samples, create leaf node w/ random label and
        //    return.
        if(treeRoot.getFeatures().length() == 0) {
            this.labelNode(treeRoot);
            return;
        }
        // 3) If no attributes left to use, create leaf node and return.
        if(Node.attributesAlreadyUsedToSplitANode.size() == this.features.get(0).get(0)) {
            this.labelNode(treeRoot);
            return;
        }
        // 4) If number of samples is below minimum for spltting, create leaf
        //    and return.
        if(treeRoot.getFeatures().length() < ID3Algorithm.MIN_SAMPLES_FOR_SPLIT) {
            this.labelNode(treeRoot);
            return;
        }

        int columnToUseToSplitSamples =
               this.findLowestEntropyFeature(treeRoot.getSampleIndices(),
                                             treeRoot.getUsedAttributes());

        treeRoot.setIndexOfFeatureToUseToSplitSamplesUp(columnToUseToSplitSamples);

        List<Integer> intervals = this.getIntervalsForFeature(treeRoot.getFeatures(),
                                                              columnToUseToSplitSamples,
                                                              5);

        List<List<NDArray>> lowestEntropyPartition =
                  this.splitSamplesAmongstIntervals(treeRoot.getFeatures(),
                                                    treeRoot.getClasses(),
                                                    intervals,
                                                    columnToUseToSplitSamples);

        List<Node> childNodes = this.createChildren(lowestEntropyPartition,
                                                    treeRoot);

        treeRoot.setChildren(childNodes);

        for(Node child : childNodes) {
            this.trainHelper(child, maxClassifierHeight-1);
        }
    }

    /**
     *  Create child nodes using partitioned data.
     *  @param partitions List<List<NDArray>> Feature and class NDArrays split
     *                                        into peices.
     *  @param parent Node The node that will be the parent of all created
     *                     nodes.
     *  @return A List of created child Node's.
     */
    private static List<Node> createChildren(List<List<NDArray>> partitions, Node parent) {
        List<Node> childNodes = new ArrayList<>();

        for(int i = 0; i < partitions.get(0).size(); i++) {
            NDArray childFeatures = partitions.get(0).get(i);
            NDArray childClasses = partitions.get(1).get(i);
            Node newNode = new Node(childFeatures, childClasses);
            newNode.setParent(parent);
            childNodes.add(newNode);
        }

        return childNodes;
    }

    /**
     *  Identify the index of the feature that creates the lowest degree of
     *  entropy when samples are split into interval according to that feature.
     *  @param NDArray features N-dimensional array of sample feature values.
     *  @return The index of the feature leading to the lowest amount of
     *          entropy.
     */
    private static int findLowestEntropyFeature(NDArray features) {
        Map<Integer, Double> entropies = new HashMap<>();
        Queue<Integer> attributeIndicies = new PriorityQueue<>(new Comparator<Integer>() {
        public int compare(Integer idx1, Integer idx2) {
            return Double.compare(entropies.get(idx1.intValue()),
                                  entropies.get(idx2.intValue()));
            }
        });

        int numberOfFeatures = this.features.length();
        for(int featureIndex = 0; featureIndex < numberOfFeatures; featureIndex++) {
            if(!Node.attributesAlreadyUsedToSplitANode.contains(featureIndex)) {
                List<Integer> intervals =
                         this.getIntervalsForFeature(features, featureIndex);

                List<List<NDArray>> partitions =
                                this.splitSamplesAmongstIntervals(features,
                                                                  null,
                                                                  intervals,
                                                                  featureIndex);

                entropies.put(featureIndex, calcEntropy(partitions));
                attributeIndicies.add(featureIndex);
            }
        }

        return attributeIndicies.remove();
    }

    /**
     *  Calculates the entropy for a partitioning of samples.
     *  @param partitions List<List<NDArray>> A partitioning of samples.
     *  @return The entropy calculated for the given partition.
     */
    private static double calcEntropy(List<List<NDArray>> partitions) {
        double entropy = 0.0;

        for(List<Integer> sampleIndices : partitions) {
            for(int classLabel : new int[] {0, 1}) {
                double probability = this.getProportion(classLabel,
                                                sampleIndices);
                if(probability > 0) {
                    entropy += -probability * (Math.log10(probability) / Math.log10(2));
                }
            }
        }

        return entropy;
    }

    /**
     *  Calculates the percentage of samples that contain a particular label.
     *  @param label int An integer representing a class label.
     *  @param classes NDArray N-dimensional array of class labels.
     *  @return The percentage of values in an array that match a given value.
     */
    private static double getProportion(int label, NDArray classes) {
        int numSamplesWithLabel = 0;

        for(int i = 0; i < classes.length(); i++) {
            if(classes.get(i).equals(label)) {
                numSamplesWithLabel++;
            }
        }

        return ((double) numSamplesWithLabel) / sampleIndices.size();
    }

    /**
     */
    private static Map<Integer, Integer>
    getNumberOfSamplesPerInterval(NDArray features,
                                  List<Integer> intervals,
                                  int featureIndexUsedToSplitSamples) {

        Map<Integer, Integer> numberOfSamplesPerInterval = new HashMap<>();

        for(int sampleIndex = 0; sampleIndex < features.length(); sampleIndex++) {
            for(int intervalStartIndex = 0; intervalStartIndex < intervals.size(); intervalStartIndex++) {
                if(features.get(sampleIndex, featureIndexUsedToSplitSamples) < intervals.at(intervalStartIndex)) {
                    if(numberOfSamplesPerInterval.containsKey(intervalStartIndex)) {
                        int currentCount = numberOfSamplesPerInterval.get(intervalStartIndex);
                        numberOfSamplesPerInterval.put(intervalStartIndex, currentCount+1);
                    }
                    else {
                        numberOfSamplesPerInterval.put(intervalStartIndex, 1);
                    }
                    break;
                }
            }
        }

        return numberOfSamplesPerInterval;
    }

    /**
     */
    private static List<List<NDArray>>
    splitSamplesAmongstIntervals(NDArray allFeatures,
                                 NDArray allClasses,
                                 List<Integer> intervals,
                                 int indexOfFeatureUsedToSplitSamples) {

        List<List<NDArray>> partitions = new ArrayList<>();
        partitions.add(new ArrayList<NDArray>());
        partitions.add(new ArrayList<NDArray>());

        Map<Integer, Integer> numberOfSamplesPerInterval =
                      this.getNumberOfSamplesPerInterval(allFeatures,
                                                         intervals,
                                                         indexOfFeatureUsedToSplitSamples);

        Map<Integer, Integer> nextIndexPerArray = new HashMap<>();

        for(int i = 0; i < intervals.size(); i++) {
            int numSamplesInInteval = numberOfSamplesPerInteval.get(i);
            int numFeatures = features.get(0).length();
            partitions.get(0).add(new NDArray(numSamplesInInterval, numFeatures));
            partitions.get(1).add(new NDArray(numSamplesInInterval));
            nextIndexPerArray.put(i, 0);
        }

        for(int sampleIndex = 0; sampleIndex < allFeatures.length(); sampleIndex++) {
            double featureValue = this.allFeatures.get(sampleIndex).(indexOfFeatureUsedToSplitSamples);

            for(int intervalIdx = 0; intervalIdx < intervals.size(); intervalIdx++) {
                if(featureValue <= intervals.get(intervalIdx)) {
                    int insertionPosition = nextIndexPerArray.put(intervalIdx);
                    partitions.get(0).get(intervalIdx).add(allFeatures.get(sampleIndex), insertionPosition);
                    if(classes != null) {
                        partitions.get(1).get(intervalIdx).add(allClasses.get(sampleIndex), insertionPosition);
                    }
                    nextIndexPerArray.put(intervalIdx, insertionPosition + 1);
                    break;
                }
            }
        }

        return partitions;
    }

    public static List<Integer>
    getIntervalsForFeature(NDArray features, int featureColumnIndex, int numIntervals) {
        List<Integer> intervalStartingValues = new ArrayList<>();
        double minimumFeatureValue = this.getMinimumValueForGivenFeature(featureColumnIndex);
        double maximumFeatureValue = this.getMaximumValueForGivenFeature(featureColumnIndex);

        double intervalSize = (maximumFeatureValue - minimumFeatureValue) / numIntervals;

        for(int numIntervalStartsComputed = 0; numIntervalStartsComputed < numIntervals; numIntervalStartsComputed++) {
            intervalStartingValues.add(minimumFeatureValue + (intervalSize * numIntervalStartsComputed));
        }

        return intervalStartingValues;
    }

    private static double getMinimumValueForGivenFeature(NDArray features, int relevantColumnIndex) {
        double minimumFeatureValue = this.features.get(0, relevantColumnIndex);
        for(int sampleIndex = 1; sampleIndex < this.features.length(); sampleIndex++) {
            minimumFeatureValue = Math.min(minimumFeatureValue, this.features.get(sampleIndex, relevantColumnIndex));
        }

        return minimumFeatureValue;
    }

    private static double getMaximumValueForGivenFeature(NDArray features, int relevantColumnIndex) {
        double maximumFeatureValue = this.features.get(0, relevantColumnIndex);
        for(int sampleIndex = 1; sampleIndex < this.features.length(); sampleIndex++) {
            maximumFeatureValue = Math.max(maximumFeatureValue, this.features.get(sampleIndex, relevantColumnIndex));
        }

        return maximumFeatureValue;
    }

    public static void labelNode(Node aNode) {
        List<Integer> sampleIndices = aNode.getSampleIndices();
        double classLabel = LabelVisitor.NO_LABEL_ASSIGNED;
        if(sampleIndices.size() == 0) {
            this.visit(aNode.getParent());
            if(aNode.getChildren() == null) {
                aNode.setLabel(this.getLabel());
            }
        }
        else {
            int numSamples = sampleIndices.size();
            Map<Double, Integer> classCounts = new HashMap<>();
            for(Integer sampleIdx : sampleIndices) {
                if(sampleIdx == null) {
                    throw new IllegalArgumentException("List of sample indices should not be null");
                }
                int i = sampleIdx.intValue();
                Double label = aNode.getDataset().getClassLabel(sampleIdx);
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
            this.setLabel(classLabel);
            if(aNode.getChildren() == null) {
                aNode.setLabel(this.getLabel());
            }
        }
    }
}
