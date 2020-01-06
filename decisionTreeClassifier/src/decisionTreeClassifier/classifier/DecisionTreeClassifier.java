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
     *  @param features NDArray<Double> A n-dimensional array containing the feature data.
     *  @param classes NDArray<Double> A n-dimensional array containing the class coresponding
     *                                 to each training sample.
     *  @return A trained decision tree classifier.
     */
    public DecisionTreeClassifier train(NDArray<Double> features, NDArray<Double> classes) {
        if(features == null || classes == null) {
            throw new IllegalArgumentException("Neither feature nor class data may be null.");
        }

        this.decisionTree = this.strategy.createModel(features, classes);

        return this;
    }

    /**
     *  Predict class label associated with samples.
     *  @param features NDArray<Double> A n-dimensional array containing
     *                                  features that will be matched with a
     *                                  class label.
     *  @return A n-dimensional array containing the clas label associated
     *          with each sample provided as an argument.
     */
    public NDArray<Double> predict(NDArray<Double> features) {
        NDArray<Double> predictions = new NDArray<>(features.length(0));

        return predictions;
    }

    /**
     *  Get the class label associated with the sample whose features are
     *  provided as an argument.
     *  @param sampleFeatures NDArray<Double> A n-dimensional array containing
     *                                        a single sample's features.
     *  @return A double representing the class label associated with the class.
     */
    private double getLabel(NDArray<Double> sampleFeatures) {
        Node decisionTreeRoot = this.decisionTree;
        List<Node> children = root.getChildren();
        if(children == null) {
            return decisionTreeRoot.getClassNum();
        }

        return this.getLabelHelper(decisionTreeRoot, sampleFeatures);
    }

    /**
     */
    private double getLabelHelper(Node root, NDArray<Double> sampleFeatures) {
        List<Interval> intervals = root.getSplitAttributeIntervals();
        Integer splitAttributeIdx = root.getSplitAttributeIndex();

        if(splitAttributeIdx == null) {
            throw new IllegalStateException("Non-leaf node must have a non-null split attribute");
        }

        Double sampleSplitAttributeValue =
            this.dataset.getFeatureValue(sampleIdx, splitAttributeIdx.intValue());

        int i = 0;
        while(i < intervals.size()) {
            if(sampleSplitAttributeValue >= intervals.get(i).getStart() &&
                sampleSplitAttributeValue <= intervals.get(i).getEnd()) {
                break;
            }
            i++;
        }

        if(i == intervals.size()) {
            throw new IllegalStateException("Sample cannot be placed into interval");
        }

        return this.getLabelHelper(children.get(i), sampleIdx);
    }

    public static List<List<Integer>> getSplitSampleIndices(double trainingProportion, NDArray features, NDArray classes) {
        if(trainingProportion < 0) {
            throw new IllegalArgumentException("Proportion of data to be used for trainging must be non-negative");
        }

        int numSamples = features.length(0);
        int numTrainingSamples = (int) Math.round(trainingProportion * numSamples);

        List<List<Integer>> split = new ArrayList<>();

        List<Integer> trainingList = new ArrayList<>();
        List<Integer> testingList = new ArrayList<>();
        Set<Integer> trainingSet = new HashSet<>();
        Set<Integer> testSet = new HashSet<>();

        Random randNumGen = new Random();
        while(trainingSet.size() < numTrainingSamples) {
            int randInt = Math.abs(randNumGen.nextInt()) % numSamples;
            if(!trainingSet.contains(randInt)) {
                trainingSet.add(randInt);
            }
        }

        for(int sampleIdx = 0; sampleIdx < numSamples; sampleIdx++) {
            if(!trainingSet.contains(sampleIdx)) {
                testSet.add(sampleIdx);
            }
        }

        trainingList.addAll(trainingSet);
        testingList.addAll(testSet);

        split.add(trainingList);
        split.add(testingList);

        int numTestSamples = testingList.size();
        assert numTrainingSamples + numTestSamples == numSamples;

        return split;
    }
}
