#include <unordered_map>
#include <iostream>
#include <algorithm>
#include <cmath>
#include <queue>
#include <functional>
#include <limits>
#include <cassert>

#include "ID3Algorithm.hpp"
#include "node.hpp"
#include "mytypes.hpp"

Node* ID3Algorithm::createModel(const my::multiple_sample_features& features,
                                const my::multiple_sample_classes& classes,
                                int maximumTreeHeight) {

    Node* root = new Node(features, classes);

    ID3Algorithm::trainHelper(root, maximumTreeHeight);

    return root;
}

void ID3Algorithm::trainHelper(Node* treeRoot, int maximumTreeHeight) {
    if(treeRoot == NULL) {
        std::cout << "Decision tree root must not be NULL" << std::endl;
        exit(1);
    }

    my::multiple_sample_features features = treeRoot->getFeatures();
    my::multiple_sample_classes classes = treeRoot->getClasses();

    if(maximumTreeHeight == 1) {
        ID3Algorithm::labelNode(treeRoot);
        return;
    }

    if(features.size() < ID3Algorithm::MIN_SAMPLES_FOR_SPLIT) {
        ID3Algorithm::labelNode(treeRoot);
        return;
    }

    if(features.empty()) {
        ID3Algorithm::labelNode(treeRoot);
        return;
    }

    if(treeRoot->doIncludedSamplesAllHaveSameClass()) {
        ID3Algorithm::labelNode(treeRoot);
        return;
    }

    int columnToUseToSplitSamples =
        ID3Algorithm::findLowestEntropyFeature(features, classes);
    treeRoot->setIndexOfFeatureToUseToSplitSamplesUp(columnToUseToSplitSamples);

    my::intervals intervals =
                ID3Algorithm::getIntervalsForFeature(
                                  features,
                                  columnToUseToSplitSamples,
                                  ID3Algorithm::NUM_DATA_PARTITIONS);

    std::vector<my::training_data> lowestEntropyPartition =
         ID3Algorithm::getPartitionedData(features,
                                          classes,
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

    my::multiple_sample_features features = node->getFeatures();
    if(features.empty()) {
        Node* parent = node->getParent();
        if(parent != NULL) {
            ID3Algorithm::labelNode(parent);
            node->setLabel(parent->getLabel());
        }
    }
    else {
        my::multiple_sample_classes classes = node->getClasses();
        std::unordered_map<int, int> classCounts;
        int numSamples = classes.size();

        int mostFrequentLabel = classes[0];
        int largestFrequency = 1;

        classCounts[mostFrequentLabel] = largestFrequency;

        for(int i = 1; i < numSamples; i++) {
            int label = classes[i];
            if(classCounts.find(label) == classCounts.end()) {
                classCounts[label] = 1;
            }
            else {
                classCounts[label]++;
                if(classCounts[label] > largestFrequency) {
                    mostFrequentLabel = label;
                    largestFrequency = classCounts[label];
                }
            }
        }

        node->setLabel(mostFrequentLabel);
    }
}

double ID3Algorithm::getProportion(int targetLabel, const my::multiple_sample_classes& classes) {
    int numSamplesWithLabel = 0;
    int numSamples = classes.size();

    for(int i = 0; i < numSamples; i++) {
        int label = classes[i];
        if(label == targetLabel) {
            numSamplesWithLabel++;
        }
    }

    return ((double) numSamplesWithLabel) / numSamples;
}

double
ID3Algorithm::getMinimumValueForGivenFeature(const my::multiple_sample_features& features, int relevantColumnIndex) {
    my::single_sample_features* firstSampleFeatures = features[0];

    int numSamples = features.size();
    double minimumFeatureValue = firstSampleFeatures->operator[](relevantColumnIndex);
    for(int sampleIndex = 1; sampleIndex < numSamples; sampleIndex++) {
        minimumFeatureValue = std::min(minimumFeatureValue, features[sampleIndex]->operator[](relevantColumnIndex));
    }

    return minimumFeatureValue;
}

double
ID3Algorithm::getMaximumValueForGivenFeature(const my::multiple_sample_features& features, int relevantColumnIndex) {
    my::single_sample_features* firstSampleFeatures = features[0];
    double maximumFeatureValue = firstSampleFeatures->operator[](relevantColumnIndex);

    int numSamples = features.size();
    for(int sampleIndex = 1; sampleIndex < numSamples; sampleIndex++) {
        maximumFeatureValue = std::max(maximumFeatureValue, features[sampleIndex]->operator[](relevantColumnIndex));
    }

    return maximumFeatureValue;
}

my::intervals
ID3Algorithm::getIntervalsForFeature(const my::multiple_sample_features& features, int featureColumnIndex, int numIntervals) {
    std::vector<my::interval> intervals;

    double minimumFeatureValue =
             ID3Algorithm::getMinimumValueForGivenFeature(features, featureColumnIndex);
    double maximumFeatureValue =
             ID3Algorithm::getMaximumValueForGivenFeature(features, featureColumnIndex);

    double intervalSize =
      (maximumFeatureValue - minimumFeatureValue) / (numIntervals-2);

    intervals.push_back(std::make_pair(-std::numeric_limits<double>::max(), minimumFeatureValue));

    for(int numIntervalsMade = 0; numIntervalsMade < numIntervals-2; numIntervalsMade++) {
        double start = minimumFeatureValue + (intervalSize * numIntervalsMade);
        double end = start + intervalSize;
        intervals.push_back(std::make_pair(start, end));
    }

    intervals.push_back(std::make_pair(maximumFeatureValue, std::numeric_limits<double>::max()));

    return intervals;
}

std::vector<my::training_data>
ID3Algorithm::getPartitionedData(const my::multiple_sample_features& features,
                                 const my::multiple_sample_classes& classes,
                                 const my::intervals& intervals,
                                 int indexOfFeatureUsedToSplitSamples) {
    int numIntervals = intervals.size();
    int numSamples = features.size();

    std::vector<my::training_data> partitionedData(numIntervals);

    for(int sampleIndex = 0; sampleIndex < numSamples; sampleIndex++) {
        double featureVal = features[sampleIndex]->operator[](indexOfFeatureUsedToSplitSamples);
        for(int intervalIndex = 0; intervalIndex < numIntervals; intervalIndex++) {
            if(featureVal <= intervals[intervalIndex].first) {
                partitionedData[intervalIndex].first.push_back(features[sampleIndex]);
                partitionedData[intervalIndex].second.push_back(classes[sampleIndex]);
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
                 ID3Algorithm::getProportion(label, partitionedData[i].second);

            if(probability > 0) {
                entropy += -probability * log10(probability) / log10(2);
            }
        }
    }

    return entropy;
}

int
ID3Algorithm::findLowestEntropyFeature(const my::multiple_sample_features& features, const my::multiple_sample_classes& classes) {
    int minEntropyFeature = 0;
    double minEntropy = std::numeric_limits<double>::max();
    int numColumns = features[0]->size();

    for(int featureIndex = 0; featureIndex < numColumns; featureIndex++) {
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
    int numPartitions = partitionedData.size();

    for(int i = 0; i < numPartitions; i++) {
        my::multiple_sample_features features = partitionedData[i].first;
        my::multiple_sample_classes classes = partitionedData[i].second;
        children.push_back(new Node(features, classes));
    }

    return children;
}
