#include <cstdlib>
#include <iostream>
#include <vector>
#include <unordered_set>

#include "node.hpp"
#include "mytypes.hpp"

int Node::NUM_NODES = 0;

std::unordered_set<int> Node::attributesAlreadyUsedToSplitANode;

Node::Node(const my::multiple_sample_features& features, const my::multiple_sample_classes& classes) {
    this->parent = NULL;
    this->label = Node::NO_LABEL_ASSIGNED;
    this->indexOfFeatureToUseToSplitSamplesUp = Node::NO_INDEX_ASSIGNED;
    this->features = features;
    this->classes = classes;
    Node::NUM_NODES++;
}

void Node::setIndexOfFeatureToUseToSplitSamplesUp(int index) {
    this->indexOfFeatureToUseToSplitSamplesUp = index;
    Node::attributesAlreadyUsedToSplitANode.insert(index);
}

int Node::getIndexOfFeatureToUseToSplitSamplesUp() {
    return this->indexOfFeatureToUseToSplitSamplesUp;
}

my::multiple_sample_features Node::getFeatures() {
    return this->features;
}

my::multiple_sample_classes Node::getClasses() {
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

    for(int i = 0; i < this->classes.size(); i++) {
        int currClass = this->classes.at(i);

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

bool Node::isLeaf() {
    return this->children.empty();
}

Node::~Node() {
    std::vector<Node*> children = this->getChildren();
    for(int i = 0; i < children.size(); i++) {
        delete children.at(i);
    }
    Node::NUM_NODES--;
}

Node& Node::operator=(const Node& node) {
    this->parent = NULL;
    this->label = node.label;
    this->features = node.features;
    this->classes = node.classes;
    this->indexOfFeatureToUseToSplitSamplesUp =
                         node.indexOfFeatureToUseToSplitSamplesUp;

    for(int i = 0; i < node.children.size(); i++) {
        Node oldNode = *node.children.at(i);
        Node* newNode = new Node(oldNode);
        newNode->setParent(this);
        this->children.push_back(newNode);
    }

    return *this;
}

Node::Node(const Node& node) {
    this->parent = NULL;
    this->label = node.label;
    this->features = node.features;
    this->classes = node.classes;
    this->indexOfFeatureToUseToSplitSamplesUp =
                         node.indexOfFeatureToUseToSplitSamplesUp;

    for(int i = 0; i < node.children.size(); i++) {
        Node oldNode = *node.children.at(i);
        Node* newNode = new Node(oldNode);
        newNode->setParent(this);
        this->children.push_back(newNode);
    }
}
