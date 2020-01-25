#include <iostream>
#include <cstdlib>
#include <cmath>

#include "decisiontreeclassifier.hpp"
#include "TrainingStrategy.hpp"
#include "ID3Algorithm.hpp"

DecisionTreeClassifier::DecisionTreeClassifier(TrainingStrategy* strat, int maxHeight) {
    if(strat == NULL) {
        std::cout << "Training strategy must not be null" << std::endl;
        exit(1);
    }
    if(maxHeight < 1) {
        std::cout << "Maximum height of decision tree must be at least 1" << std::endl;
        exit(1);
    }

    this->strategy = strat;
    this->maxHeight = maxHeight;
    this->decisionTree = NULL;
}

void DecisionTreeClassifier::train(const my::multiple_sample_features& features, const my::multiple_sample_classes& classes) {
    if(this->decisionTree != NULL) {
        delete this->decisionTree;
    }

    TrainingStrategy* strategy = this->getStrategy();
    this->decisionTree = strategy->createModel(features, classes, this->maxHeight);
}

TrainingStrategy* DecisionTreeClassifier::getStrategy() {
    return this->strategy;
}

my::multiple_sample_classes DecisionTreeClassifier::predict(const my::multiple_sample_features& features) {
    my::multiple_sample_classes predictions;

    if(this->decisionTree == NULL) {
        return predictions;
    }

    for(my::single_sample_features* sampleFeatures : features) {
        predictions.push_back(this->getLabel(sampleFeatures));
    }

    return predictions;
}

int DecisionTreeClassifier::getLabel(my::single_sample_features* features) {
    Node* decisionTreeRoot = this->decisionTree;
    std::vector<Node*> children = decisionTreeRoot->getChildren();
    if(children.empty()) {
        return decisionTreeRoot->getLabel();
    }

    return this->getLabelHelper(decisionTreeRoot, features);
}



int DecisionTreeClassifier::getLabelHelper(Node* root, my::single_sample_features* features) {
    if(root == NULL) {
        std::cerr << "Node should not be null" << std::endl;
        exit(1);
    }
    if(root->isLeaf()) {
        return root->getLabel();
    }

    int indexUsedToSplitSamples =
                     root->getIndexOfFeatureToUseToSplitSamplesUp();
    my::intervals intervals =
         ID3Algorithm::getIntervalsForFeature(
                                  root->getFeatures(),
                                  indexUsedToSplitSamples,
                                  ID3Algorithm::NUM_DATA_PARTITIONS);

    double featureValueAtIndexUsedToSplitSamples =
                                features->operator[](indexUsedToSplitSamples);
    int childIndex = 0;
    for(my::interval interval : intervals) {
        if(featureValueAtIndexUsedToSplitSamples >= interval.first &&
           featureValueAtIndexUsedToSplitSamples <= interval.second) {
            break;
        }

        childIndex++;
    }

    std::vector<Node*> children = root->getChildren();
    Node* relevantChild = children[childIndex];

    return this->getLabelHelper(relevantChild, features);
}

std::pair<my::training_data, my::testing_data>
DecisionTreeClassifier::getTrainingAndTestSets(const my::multiple_sample_features& features, const my::multiple_sample_classes& classes, double trainingProportion) {
    if(trainingProportion < 0) {
        std::cout << "Training proportion must be non-negative" << std::endl;
        exit(1);
    }

    srand(100);

    std::pair<my::training_data, my::testing_data> splitData;
    int numSamples = features.size();
    int numTrainingSamples = ceil(trainingProportion * numSamples);

    my::multiple_sample_features trainingFeatures;
    my::multiple_sample_features testingFeatures;
    my::multiple_sample_classes trainingLabels;
    my::multiple_sample_classes testingLabels;

    std::unordered_set<int> usedSampleIndices;

    while(usedSampleIndices.size() < numTrainingSamples) {
        int randSampleIndex = rand() % numSamples;
        if(usedSampleIndices.find(randSampleIndex) == usedSampleIndices.end()) {
            usedSampleIndices.insert(randSampleIndex);
            trainingFeatures.push_back(features[randSampleIndex]);
            trainingLabels.push_back(classes[randSampleIndex]);
        }
    }

    for(int sampleIndex = 0; sampleIndex < numSamples; sampleIndex++) {
        if(usedSampleIndices.find(sampleIndex) == usedSampleIndices.end()) {
            testingFeatures.push_back(features[sampleIndex]);
            testingLabels.push_back(classes[sampleIndex]);
        }
    }

    my::training_data trainingData;
    my::testing_data testingData;

    trainingData.first = trainingFeatures;
    trainingData.second = trainingLabels;

    testingData.first = testingFeatures;
    testingData.second = testingLabels;

    splitData.first = trainingData;
    splitData.second = testingData;

    return splitData;
}

DecisionTreeClassifier::~DecisionTreeClassifier() {
    if(this->decisionTree != NULL) {
        delete this->decisionTree;
    }
}

DecisionTreeClassifier::DecisionTreeClassifier(const DecisionTreeClassifier& clf) {
    this->strategy = clf.strategy;
    this->maxHeight = clf.maxHeight;

    this->decisionTree = NULL;
    if(clf.decisionTree != NULL) {
        Node oldTreeRoot = *clf.decisionTree;
        this->decisionTree = new Node(oldTreeRoot);
    }
}
