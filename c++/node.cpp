#include <cstdlib>
#include <iostream>
#include <vector>
#include <unordered_set>

#include "node.hpp"
#include "mytypes.hpp"

std::unordered_set<int> Node::attributesAlreadyUsedToSplitANode;

Node::Node(my::features* features, my::classes* classes) {
    if(features == NULL || classes == NULL) {
        std::cout << "Neither the features nor classes should be null" << std::endl;
        exit(1);
    }

    this->parent = NULL;
    this->label = Node::NO_LABEL_ASSIGNED;
    this->indexOfFeatureToUseToSplitSamplesUp = Node::NO_INDEX_ASSIGNED;
    this->features = features;
    this->classes = classes;
}

void Node::setIndexOfFeatureToUseToSplitSamplesUp(int index) {
    this->indexOfFeatureToUseToSplitSamplesUp = index;
    Node::attributesAlreadyUsedToSplitANode.insert(index);
}

int Node::getIndexOfFeatureToUseToSplitSamplesUp() {
    return this->indexOfFeatureToUseToSplitSamplesUp;
}

my::features* Node::getFeatures() {
    return this->features;
}

my::classes* Node::getClasses() {
    return this->classes;
}

void Node::setParent(Node* parentNode) {
    this->parent = parentNode;
}

void Node::setLabel(int labelIn) {
    this->label = labelIn;
}

int Node::getLabel() {
    return this->label;
}

Node* Node::getParent() {
    return this->parent;
}

bool Node::doIncludedSamplesAllHaveSameClass() {
    bool haveSameClass = false;
    int onlyClassPresent = -1;

    for(int i = 0; i < this->classes->size(); i++) {
        int currClass = this->classes->at(i);

        if(onlyClassPresent == -1) {
            onlyClassPresent = currClass;
        }

        if(onlyClassPresent != currClass) {
            haveSameClass = false;
            break;
        }
    }

    return haveSameClass;
}

std::vector<Node*> Node::getChildren() {
    return this->children;
}

void Node::setChildren(const std::vector<Node*>& children) {
    this->children = children;
}
