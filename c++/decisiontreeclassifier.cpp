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
}

DecisionTreeClassifier DecisionTreeClassifier::train(my::features* features, my::classes* classes) {
    TrainingStrategy* strategy = this->getStrategy();
    this->decisionTree = strategy->createModel(features, classes, this->maxHeight);

    return *this;
}

TrainingStrategy* DecisionTreeClassifier::getStrategy() {
    return this->strategy;
}

my::classes DecisionTreeClassifier::predict(const my::features& features) {
    my::classes predictions;
    for(my::single_sample_features sampleFeatures : features) {
        predictions.push_back(this->getLabel(sampleFeatures));
    }

    return predictions;
}

int DecisionTreeClassifier::getLabel(const my::single_sample_features& features) {
    Node* decisionTreeRoot = this->decisionTree;
    std::cout << "root: " << decisionTreeRoot << std::endl;
    std::vector<Node*> children = decisionTreeRoot->getChildren();
    if(children.empty()) {
        return decisionTreeRoot->getLabel();
    }

    return this->getLabelHelper(decisionTreeRoot, features);
}



int DecisionTreeClassifier::getLabelHelper(Node* root, const my::single_sample_features& features) {
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
                                features.at(indexUsedToSplitSamples);
    int childIndex = 0;
    for(my::interval interval : intervals) {
        std::cout << "feature: " << featureValueAtIndexUsedToSplitSamples << std::endl;
        std::cout << "start: " << interval.first << std::endl;
        std::cout << "end: " << interval.second << std::endl;
        if(featureValueAtIndexUsedToSplitSamples >= interval.first &&
           featureValueAtIndexUsedToSplitSamples <= interval.second) {
            std::cout << "in break" << std::endl;
            break;
        }

        childIndex++;
    }

    std::vector<Node*> children = root->getChildren();
    std::cout << "children size: " << children.size() << std::endl;
    std::cout << "child index: " << childIndex << std::endl;
    Node* relevantChild = children.at(childIndex);

    return this->getLabelHelper(relevantChild, features);
}

std::pair<my::training_data, my::testing_data>
DecisionTreeClassifier::getTrainingAndTestSets(const my::features& features, const my::classes& classes, double trainingProportion) {
    if(trainingProportion < 0) {
        std::cout << "Training proportion must be non-negative" << std::endl;
        exit(1);
    }

    srand(100);

    std::pair<my::training_data, my::testing_data> splitData;
    int numSamples = features.size();
    int numTrainingSamples = ceil(trainingProportion * numSamples);

    my::features trainingFeatures;
    my::features testingFeatures;
    my::classes trainingLabels;
    my::classes testingLabels;

    std::unordered_set<int> usedSampleIndices;

    while(usedSampleIndices.size() < numTrainingSamples) {
        int randSampleIndex = rand() % numSamples;
        if(usedSampleIndices.find(randSampleIndex) == usedSampleIndices.end()) {
            usedSampleIndices.insert(randSampleIndex);
            trainingFeatures.push_back(features.at(randSampleIndex));
            trainingLabels.push_back(classes.at(randSampleIndex));
        }
    }

    for(int sampleIndex = 0; sampleIndex < numSamples; sampleIndex++) {
        if(usedSampleIndices.find(sampleIndex) == usedSampleIndices.end()) {
            testingFeatures.push_back(features.at(sampleIndex));
            testingLabels.push_back(classes.at(sampleIndex));
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
