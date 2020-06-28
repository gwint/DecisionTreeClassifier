#ifndef DECISION_TREE_CLF_H
#define DECISION_TREE_CLF_H

#include <cmath>
#include <utility>
#include <vector>
#include <stack>
#include <queue>
#include <unordered_set>

#include "mytypes.hpp"
#include "node.hpp"
#include "ID3Algorithm.hpp"

extern std::vector<Node*> nodesToDelete;

template <typename T>
class DecisionTreeClassifier {
    public:
        DecisionTreeClassifier();
        void train(const my::multiple_sample_features&, const my::multiple_sample_classes&);
        my::multiple_sample_classes predict(const my::multiple_sample_features&);
        static std::pair<my::training_data, my::testing_data>
        getTrainingAndTestSets(const my::multiple_sample_features&, const my::multiple_sample_classes&, double);
        ~DecisionTreeClassifier();
        DecisionTreeClassifier(const DecisionTreeClassifier<T>&);
        Node* decisionTree;
        DecisionTreeClassifier<T>& setMaxTreeHeight(const unsigned int);
        DecisionTreeClassifier<T>& setNumDataPartitions(const unsigned int);
        DecisionTreeClassifier<T>& setMinimumSamplesForSplit(const unsigned int);

        template <typename K>
        friend std::ostream& operator<<(std::ostream&, DecisionTreeClassifier<K> const&);

    private:
        T strategy;
        unsigned int maxHeight;
        unsigned int numDataPartitions;
        unsigned int minimumSamplesForSplit;
        int getLabel(my::single_sample_features*);
        int getLabelHelper(const Node * const, my::single_sample_features*);
};

template <typename T>
std::ostream&
operator<<(std::ostream& o, DecisionTreeClassifier<T> const& clf) {
    if(clf.decisionTree == NULL) {
        return o;
    }

    unsigned int maxNumChildren = clf.numDataPartitions;

    unsigned int nodeCount = 0;
    unsigned int level = 0;

    std::queue<Node*> nodes;
    nodes.push(clf.decisionTree);

    std::vector<Node*> children;

    while(!nodes.empty()) {
        Node* currNode = nodes.front();
        nodes.pop();

        std::cout << "Node #" << nodeCount << ", Level " << level;

        std::vector<Node*> currNodeChildren;
        if(currNode != NULL) {
            std::cout << *currNode << std::endl;
            currNodeChildren = currNode->getChildren();
            std::cout << ", Label " << currNode->getLabel() << std::endl;
        }
        else {
            std::cout << std::endl;
            std::cout << "null" << std::endl;
        }

        for(unsigned int i = 0; i < maxNumChildren; i++) {
            if(i < currNodeChildren.size()) {
                children.push_back(currNodeChildren[i]);
            }
            else {
                children.push_back(NULL);
            }
        }

        if(nodes.empty()) {
            bool nonNullFound = false;
            for(Node* child : children) {
                nodes.push(child);
                if(child != NULL) {
                    nonNullFound = true;
                }
            }
            children.clear();
            level++;

            if(!nonNullFound) {
                std::queue<Node*> empty;
                std::swap(nodes, empty);
            }
        }

        nodeCount++;
    }

    return o;
}

template <typename T>
DecisionTreeClassifier<T>::DecisionTreeClassifier() {
    this->maxHeight = 10;
    this->numDataPartitions = 11;
    this->minimumSamplesForSplit = 10;
    this->decisionTree = NULL;
}

template <typename T>
DecisionTreeClassifier<T>&
DecisionTreeClassifier<T>::setMaxTreeHeight(const unsigned int maxTreeHeight) {
    this->maxHeight = maxTreeHeight;
    return *this;
}

template <typename T>
DecisionTreeClassifier<T>&
DecisionTreeClassifier<T>::setNumDataPartitions(const unsigned int numDataPartitions) {
    this->numDataPartitions = numDataPartitions;
    return *this;
}

template <typename T>
DecisionTreeClassifier<T>&
DecisionTreeClassifier<T>::setMinimumSamplesForSplit(const unsigned int minimumNumSamples) {
    this->minimumSamplesForSplit = minimumNumSamples;
    return *this;
}

template <typename T>
void
DecisionTreeClassifier<T>::train(const my::multiple_sample_features& features,
                                 const my::multiple_sample_classes& classes) {

    if(this->decisionTree != NULL) {
        nodesToDelete.push_back(this->decisionTree);
    }

    this->strategy = T(this->maxHeight,
                       this->numDataPartitions,
                       this->minimumSamplesForSplit);

    this->decisionTree = this->strategy.createModel(features, classes);
}

template <typename T>
my::multiple_sample_classes
DecisionTreeClassifier<T>::predict(const my::multiple_sample_features& features) {
    my::multiple_sample_classes predictions;

    if(this->decisionTree == NULL) {
        return predictions;
    }

    for(my::single_sample_features* sampleFeatures : features) {
        predictions.push_back(this->getLabel(sampleFeatures));
    }

    return predictions;
}

template <typename T>
int
DecisionTreeClassifier<T>::getLabel(my::single_sample_features* features) {
    const Node* const decisionTreeRoot = this->decisionTree;
    const std::vector<Node*>& children = decisionTreeRoot->getChildren();
    if(children.empty()) {
        return decisionTreeRoot->getLabel();
    }

    return this->getLabelHelper(decisionTreeRoot, features);
}


template <typename T>
int
DecisionTreeClassifier<T>::getLabelHelper(const Node* const root, my::single_sample_features* features) {
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
         this->strategy.getIntervalsForFeature(
                                  root->getFeatures(),
                                  indexUsedToSplitSamples);

    double featureValueAtIndexUsedToSplitSamples =
                                features->at(indexUsedToSplitSamples);
    int childIndex = 0;
    for(const my::interval& interval : intervals) {

        if(featureValueAtIndexUsedToSplitSamples >= interval.first &&
           featureValueAtIndexUsedToSplitSamples <= interval.second) {
            break;
        }

        childIndex++;
    }

    const std::vector<Node*>& children = root->getChildren();
    Node* relevantChild = children.at(childIndex);

    return this->getLabelHelper(relevantChild, features);
}

template <typename T>
std::pair<my::training_data, my::testing_data>
DecisionTreeClassifier<T>::getTrainingAndTestSets(const my::multiple_sample_features& features, const my::multiple_sample_classes& classes, double trainingProportion) {
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

    while(usedSampleIndices.size() < (unsigned) numTrainingSamples) {
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

    trainingData.features = trainingFeatures;
    trainingData.classes = trainingLabels;

    testingData.features = testingFeatures;
    testingData.classes = testingLabels;

    splitData.first = trainingData;
    splitData.second = testingData;

    return splitData;
}

template <typename T>
DecisionTreeClassifier<T>::~DecisionTreeClassifier() {
    if(this->decisionTree != NULL) {
        delete this->decisionTree;
    }
}

template <typename T>
DecisionTreeClassifier<T>::DecisionTreeClassifier(
                            const DecisionTreeClassifier<T>& clf) {
    this->maxHeight = clf.maxHeight;
    this->numDataPartitions = clf.numDataPartitions;
    this->minimumSamplesForSplit = clf.minimumSamplesForSplit;
    this->decisionTree = NULL;
    this->strategy = clf.strategy;
    if(clf.decisionTree != NULL) {
        this->decisionTree = new Node(*clf.decisionTree);
    }
}

#endif
