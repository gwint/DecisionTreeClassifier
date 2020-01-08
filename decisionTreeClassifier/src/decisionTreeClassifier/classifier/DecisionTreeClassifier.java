package classifier;

import util.NDArray;
import util.Linkable;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.Random;
import util.Interval;
import util.Dataset;

public class DecisionTreeClassifier {
    private Node decisionTree;
    private TrainingStrategy strategy;
    private int maxHeight;

    /**
     *  Creates a decision tree classifier that utilizes a particular strategy
     *  to construct a decision tree classifier with a particular max height.
     *  @param strategyIn TrainingStrategy Method by which the decision tree
     *                                     will be constructed.
     *  @param maxHeightIn int The maximum height the decision tree may reach.
     */
    public DecisionTreeClassifier(TrainingStrategy strategyIn, int maxHeightIn) {
        if(strategyIn == null) {
            throw new IllegalArgumentException("Training strategy must not be null");
        }
        if(maxHeightIn < 1) {
            throw new IllegalArgumentException("Max height of trained classifier must be at least 1");
        }
        this.strategy = strategyIn;
        this.maxHeight = maxHeightIn;
    }

    /**
     *  Constructs a decision tree using the provided training samples and ground
     *  truth classifications.
     *  @param features NDArray A n-dimensional array containing the feature data.
     *  @param classes NDArray A n-dimensional array containing the class coresponding
     *                         to each training sample.
     *  @return A trained decision tree classifier.
     */
    public DecisionTreeClassifier train(NDArray features, NDArray classes) {
        if(features == null || classes == null) {
            throw new IllegalArgumentException("Neither feature nor class data may be null.");
        }

        this.decisionTree = this.strategy.createModel(features, classes);

        return this;
    }

    /**
     *  Predict class label associated with samples.
     *  @param features NDArray A n-dimensional array containing
     *                          features that will be matched with a
     *                          class label.
     *  @return A n-dimensional array containing the clas label associated
     *          with each sample provided as an argument.
     */
    public NDArray predict(NDArray features) {
        NDArray predictions = new NDArray(features.length());

        return predictions;
    }

    /**
     *  Get the class label associated with the sample whose features are
     *  provided as an argument.
     *  @param sampleFeatures NDArray A n-dimensional array containing
     *                                a single sample's features.
     *  @return A double representing the class label associated with the class.
     */
    private double getLabel(NDArray sampleFeatures) {
        Node decisionTreeRoot = this.decisionTree;
        List<Node> children = root.getChildren();
        if(children == null) {
            return decisionTreeRoot.getClassLabel();
        }

        return this.getLabelHelper(decisionTreeRoot, sampleFeatures);
    }

    /**
     *  Helper method that recursively traverses decision tree to find a leaf
     *  that corresponds to the sample feauture's class label.
     *  @param root Node The starting position of the tree traversal.
     *  @param sampleFeatures NDArray The n-dimensional array containing
     *                                the feature information of the
     *                                sample in question.
     *  @return The class label associted with the sample in question.
     */
    private double getLabelHelper(Node root, NDArray sampleFeatures) {
        List<Integer> intervals = root.getIntervalsForFeatureUsedToSplitSamples();
        int indexToUseToSplitSamples = root.getIndexOfFeatureToUseToSplitSamplesUp();

        if(indexToUseToSplitSamples < 0) {
            throw new IllegalStateException("Non-leaf node must have a non-negative split attribute");
        }

        Double featureValueAtIndexUsedToSplitSamples =
                        (Double) sampleFeatures.get(indexToUseToSplitSamples);

        int childIndex = 0;
        for(int indexOfIntervalStart = 0; indexOfIntervalStart < intervals.size()-1; indexOfIntervalStart++) {
            if(featureValueAtIndexUsedToSplitSamples >= intervals.get(i) &&
               featureValueAtIndexUsedToSplitSamples <= intervals.get(i+1)) {
                break;
            }
            childIndex++;
        }

        if(childIndex == intervals.size()) {
            throw new IllegalStateException("For some reason, no interval could be found for the given sample");
        }

        return this.getLabelHelper(children.get(childIndex), sampleFeatures);
    }

    /**
     *  Splits a dataset consisting of sample features and corresponding
     *  class labels into training and test sets using a given proportion to
     *  determine the size of each.
     *  @param features NDArray A list containing 4 n-dimensional arrays: the
     *                          first two contain the training features and
     *                          classes, respectively and the second two
     *                          contain the test features and classes.
     */
    public static List<NDArray>
    getTrainingAndTestSets(NDArray allFeatures, NDArray allClasses, double trainingProportion) {
        if(trainingProportion < 0) {
            throw new IllegalArgumentException("Proportion of data to be used for trainging must be non-negative");
        }

        int numSamples = allFeatures.length();
        int numTrainingSamples = (int) Math.round(trainingProportion * numSamples);
        int numTestingSamples = numSamples - numTrainingSamples;

        int[] featureArrayDimensions = allFeatures.getDimensions();

        featureArrayDimensions[0] = numTrainingSamples;
        NDArray trainingFeatures = new NDArray(featureArrayDimensions);
        NDArray trainingClasses = new NDArray(numTrainingSamples);

        featureArrayDimensions[0] = numTestingSamples;
        NDArray testingFeatures = new NDArray(featureArrayDimensions);
        NDArray testingClasses = new NDArray(numTestingSamples);

        Set<Integer> indicesOfTrainingSamples = new HashSet<>();

        int nextTrainingSampleIndex = 0;
        Random randNumGen = new Random();
        while(indicesOfTrainingSamples.size() < numTrainingSamples) {
            int randSampleIndex = Math.abs(randNumGen.nextInt()) % numSamples;
            if(!indicesOfTrainingSamples.contains(randSampleIndex)) {
                indicesOfTrainingSamples.add(randSampleIndex);
                trainingFeatures.add(allFeatures.get(randSampleIndex), nextTrainingSampleIndex);
                trainingClasses.add(allClasses.get(randSampleIndex), nextTrainingSampleIndex);
                nextTrainingSampleIndex++;
            }
        }

        int nextTestingSampleIndex = 0;
        for(int sampleIdx = 0; sampleIdx < numSamples; sampleIdx++) {
            if(!indicesOfTrainingSamples.contains(sampleIdx)) {
                testingFeatures.add(allFeatures.get(sampleIdx), nextTestingSampleIndex);
                testingClasses.add(allClasses.get(sampleIdx), nextTestingSampleIndex);
                nextTestingSampleIndex++;
            }
        }

        return new ArrayList<NDArray>(Arrays.asList(trainingFeatures,
                                                    trainingClasses,
                                                    testingFeatures,
                                                    testingClasses));
    }
}
