#include <unordered_map>
#include <iostream>
#include <algorithm>
#include <cmath>
#include <queue>
#include <functional>
#include <limits>

#include "ID3Algorithm.hpp"
#include "node.hpp"
#include "mytypes.hpp"

Node* ID3Algorithm::createModel(my::features* features,
                                my::classes* classes,
                                int maximumTreeHeight) {

    if(features == NULL || classes == NULL) {
        std::cerr << "Neither features nor classes may be null" << std::endl;
        exit(1);
    }

    Node* root = new Node(features, classes);

    ID3Algorithm::trainHelper(root, maximumTreeHeight);

    return NULL;
}

void ID3Algorithm::trainHelper(Node* treeRoot, int maximumTreeHeight) {
    if(treeRoot == NULL) {
        std::cout << "Decision tree root must not be NULL" << std::endl;
        exit(1);
    }

    if(maximumTreeHeight == 1) {
        ID3Algorithm::labelNode(treeRoot);
        return;
    }

    if(treeRoot->doIncludedSamplesAllHaveSameClass()) {
        ID3Algorithm::labelNode(treeRoot);
        return;
    }

    if(treeRoot->getFeatures()->size() == 0) {
        ID3Algorithm::labelNode(treeRoot);
        return;
    }

    if(treeRoot->getFeatures()->size() < ID3Algorithm::MIN_SAMPLES_FOR_SPLIT) {
        ID3Algorithm::labelNode(treeRoot);
        return;
    }

    int columnToUseToSplitSamples =
        ID3Algorithm::findLowestEntropyFeature(treeRoot->getFeatures(),
                                               treeRoot->getClasses());
    treeRoot->setIndexOfFeatureToUseToSplitSamplesUp(columnToUseToSplitSamples);

    my::intervals intervals =
                ID3Algorithm::getIntervalsForFeature(
                                  treeRoot->getFeatures(),
                                  columnToUseToSplitSamples,
                                  ID3Algorithm::NUM_DATA_PARTITIONS);

    std::vector<my::training_data> lowestEntropyPartition =
         ID3Algorithm::getPartitionedData(treeRoot->getFeatures(),
                                          treeRoot->getClasses(),
                                          intervals,
                                          columnToUseToSplitSamples);

    std::vector<Node*> children =
                ID3Algorithm::createChildren(lowestEntropyPartition,
                                             treeRoot);

    treeRoot->setChildren(children);

    for(Node* child : children) {
        ID3Algorithm::trainHelper(child, maximumTreeHeight-1);
    }
}

void ID3Algorithm::labelNode(Node* node) {
    if(node == NULL) {
        std::cerr << "Should not be trying to label a NULL node" << std::endl;
        exit(1);
    }

    my::features* features = node->getFeatures();
    if(features->size() == 0) {
        Node* parent = node->getParent();
        if(parent != NULL) {
            ID3Algorithm::labelNode(node->getParent());
            node->setLabel(node->getParent()->getLabel());
        }
    }
    else {
        my::classes* classes = node->getClasses();
        std::unordered_map<int, int> classCounts;
        for(int i = 0; i < classes->size(); i++) {
            int label = classes->at(i);
            if(classCounts.find(label) == classCounts.end()) {
                classCounts.insert(std::make_pair(label, 1));
            }
            else {
                classCounts[label]++;
            }
        }

        std::unordered_map<int, int>::iterator it = classCounts.begin();
        int mostFrequentLabel = it->first;
        int largestFrequency = it->second;
        for(std::unordered_map<int, int>::iterator it; it != classCounts.end(); it++) {
            if(it->second > largestFrequency) {
                mostFrequentLabel = it->first;
                largestFrequency = it->second;
            }
        }

        node->setLabel(mostFrequentLabel);
    }
}

double ID3Algorithm::getProportion(int targetLabel, const my::classes& classes) {
    int numSamplesWithLabel = 0;

    for(int i = 0; i < classes.size(); i++) {
        int label = classes.at(i);
        if(label == targetLabel) {
            numSamplesWithLabel++;
        }
    }

    return ((double) numSamplesWithLabel) / classes.size();
}

double
ID3Algorithm::getMinimumValueForGivenFeature(const my::features* features, int relevantColumnIndex) {
    double minimumFeatureValue = features->at(0).at(relevantColumnIndex);
    for(int sampleIndex = 1; sampleIndex < features->size(); sampleIndex++) {
        minimumFeatureValue = std::min(minimumFeatureValue, features->at(sampleIndex).at(relevantColumnIndex));
    }

    return minimumFeatureValue;
}

double
ID3Algorithm::getMaximumValueForGivenFeature(const my::features* features, int relevantColumnIndex) {
    double maximumFeatureValue = features->at(0).at(relevantColumnIndex);
    for(int sampleIndex = 1; sampleIndex < features->size(); sampleIndex++) {
        maximumFeatureValue = std::max(maximumFeatureValue, features->at(sampleIndex).at(relevantColumnIndex));
    }

    return maximumFeatureValue;
}

my::intervals
ID3Algorithm::getIntervalsForFeature(const my::features* features, int featureColumnIndex, int numIntervals) {
    std::vector<my::interval> intervals;

    double minimumFeatureValue =
             ID3Algorithm::getMinimumValueForGivenFeature(features, featureColumnIndex);
    double maximumFeatureValue =
             ID3Algorithm::getMaximumValueForGivenFeature(features, featureColumnIndex);

    double intervalSize =
         (maximumFeatureValue - minimumFeatureValue) / numIntervals;

    for(int numIntervalsMade = 0; numIntervalsMade < numIntervals-1; numIntervalsMade++) {
        int start = minimumFeatureValue + (intervalSize * numIntervalsMade);
        int end = start + intervalSize;
        intervals.push_back(std::make_pair(start, end));
    }

    return intervals;
}

std::vector<my::training_data>
ID3Algorithm::getPartitionedData(const my::features* features,
                                 const my::classes* classes,
                                 const my::intervals& intervals,
                                 int indexOfFeatureUsedToSplitSamples) {
    std::vector<my::training_data> partitionedData(intervals.size());

    for(int sampleIndex = 0; sampleIndex < features->size(); sampleIndex++) {
        double featureVal = features->at(sampleIndex).at(indexOfFeatureUsedToSplitSamples);
        for(int intervalIndex = 0; intervalIndex < intervals.size(); intervalIndex++) {
            if(featureVal <= intervals.at(intervalIndex).first) {
                partitionedData.at(intervalIndex).first.push_back(features->at(sampleIndex));
                partitionedData.at(intervalIndex).second.push_back(classes->at(sampleIndex));
                break;
            }
        }
    }

    return partitionedData;
}

double
ID3Algorithm::calculateEntropy(const std::vector<my::training_data>& partitionedData) {
    double entropy = 0.0;
    int labels[] = {0, 1};
    for(int i = 0; i < partitionedData.size(); i++) {
        for(int label : labels) {
            double probability =
                 ID3Algorithm::getProportion(label, partitionedData.at(i).second);

            if(probability > 0) {
                entropy += -probability * log10(probability) / log10(2);
            }
        }
    }

    return entropy;
}

int
ID3Algorithm::findLowestEntropyFeature(const my::features* features, const my::classes* classes) {
    int minEntropyFeature = 0;
    double minEntropy = std::numeric_limits<double>::max();

    for(int featureIndex = 0; featureIndex < features->size(); featureIndex++) {
        if(Node::attributesAlreadyUsedToSplitANode.find(featureIndex) ==
           Node::attributesAlreadyUsedToSplitANode.end()) {
            my::intervals intervals =
                 ID3Algorithm::getIntervalsForFeature(
                                features,
                                featureIndex,
                                ID3Algorithm::MIN_SAMPLES_FOR_SPLIT);


            std::vector<my::training_data> partitionedData =
                     ID3Algorithm::getPartitionedData(features,
                                                      classes,
                                                      intervals,
                                                      featureIndex);

            double resultingEntropy =
                     ID3Algorithm::calculateEntropy(partitionedData);

            if(resultingEntropy < minEntropy) {
                minEntropy = resultingEntropy;
                minEntropyFeature = featureIndex;
            }
        }
    }

    return minEntropyFeature;
}

std::vector<Node*>
ID3Algorithm::createChildren(const std::vector<my::training_data>& partitionedData, const Node* parent) {
    std::vector<Node*> children;
    for(int i = 0; i < partitionedData.size(); i++) {
        my::features* features = new my::features(partitionedData.at(i).first);
        my::classes* classes = new my::classes(partitionedData.at(i).second);
        children.push_back(new Node(features, classes));
    }

    return children;
}
