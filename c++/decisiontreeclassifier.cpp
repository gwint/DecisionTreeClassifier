#include <iostream>
#include <cstdlib>
#include <cmath>

#include "decisiontreeclassifier.hpp"
#include "TrainingStrategy.hpp"

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

    return predictions;
}

int DecisionTreeClassifier::getLabel(const my::single_sample_features& features) {
    Node* decisionTreeRoot = this->decisionTree;
    std::vector<Node*> children = decisionTreeRoot->getChildren();
    if(children.empty()) {
        return decisionTreeRoot->getLabel();
    }

    return this->getLabelHelper(decisionTreeRoot, features);
}



int DecisionTreeClassifier::getLabelHelper(Node* root, const my::single_sample_features& features) {
    return 0;
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
