#include <cstdlib>
#include <iostream>
#include <vector>
#include <unordered_set>

#include "node.hpp"
#include "mytypes.hpp"

Node::Node() {
    this->parent = NULL;
    this->label = Node::NO_LABEL_ASSIGNED;
    this->indexOfFeatureToUseToSplitSamplesUp = Node::NO_INDEX_ASSIGNED;
}

Node::Node(const my::multiple_sample_features& features, const my::multiple_sample_classes& classes) {
    this->parent = NULL;
    this->label = Node::NO_LABEL_ASSIGNED;
    this->indexOfFeatureToUseToSplitSamplesUp = Node::NO_INDEX_ASSIGNED;
    this->features = features;
    this->classes = classes;
}

void
Node::setIndexOfFeatureToUseToSplitSamplesUp(const unsigned int index) {
    this->indexOfFeatureToUseToSplitSamplesUp = index;
}

unsigned int
Node::getIndexOfFeatureToUseToSplitSamplesUp() const {
    return this->indexOfFeatureToUseToSplitSamplesUp;
}

const my::multiple_sample_features&
Node::getFeatures() const {
    return this->features;
}

const my::multiple_sample_classes&
Node::getClasses() const {
    return this->classes;
}

void
Node::setParent(Node* parentNode) {
    this->parent = parentNode;
}

void
Node::setLabel(int labelIn) {
    this->label = labelIn;
}

int
Node::getLabel() const {
    return this->label;
}

Node*
Node::getParent() {
    return this->parent;
}

void
Node::setFeatures(const my::multiple_sample_features& features) {
    this->features = features;
}

void
Node::setClasses(const my::multiple_sample_classes& classes) {
    this->classes = classes;
}


bool
Node::doIncludedSamplesAllHaveSameClass() {
    if(this->classes.empty()) {
        return false;
    }

    int numSamples = this->classes.size();
    int onlyClassPresent = this->classes[0];

    for(int i = 1; i < numSamples; i++) {
        int currClass = this->classes[i];

        if(onlyClassPresent != currClass) {
            return false;
        }
    }

    return true;
}

const std::vector<Node*>&
Node::getChildren() const {
    return this->children;
}

void
Node::setChildren(const std::vector<Node*>& children) {
    this->children = children;
}

bool
Node::isLeaf() const {
    return this->children.empty();
}

Node::~Node() {
    std::vector<Node*> children = this->getChildren();
    for(unsigned int i = 0; i < children.size(); i++) {
        delete children.at(i);
    }
}

Node&
Node::operator=(const Node& node) {
    this->parent = NULL;
    this->label = node.label;
    this->features = node.features;
    this->classes = node.classes;
    this->indexOfFeatureToUseToSplitSamplesUp =
                         node.indexOfFeatureToUseToSplitSamplesUp;

    for(unsigned int i = 0; i < node.children.size(); i++) {
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

    for(unsigned int i = 0; i < node.children.size(); i++) {
        Node oldNode = *node.children.at(i);
        Node* newNode = new Node(oldNode);
        newNode->setParent(this);
        this->children.push_back(newNode);
    }
}
