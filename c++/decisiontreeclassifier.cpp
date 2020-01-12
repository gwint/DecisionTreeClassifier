#include <iostream>
#include <cstdlib>

#include "decisiontreeclassifier.hpp"
#include "TrainingStrategy.hpp"

DecisionTreeClassifier::DecisionTreeClassifier(TrainingStrategy const * strat, int maxHeight) {
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

DecisionTreeClassifier DecisionTreeClassifier::train(const my::features& features, const my::classes& classes) {
    this->decisionTree = this->strategy->createModel(features, classes, this->maxHeight);

    return *this;
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
