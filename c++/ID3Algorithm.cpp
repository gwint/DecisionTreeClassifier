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

Node* ID3Algorithm::createModel(const my::features& features,
                                const my::classes& classes,
                                int maximumTreeHeight) const {
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

    // Waiting to write other methods before continuing...
}

void ID3Algorithm::labelNode(Node* node) {
    my::features* features = node->getFeatures();
    if(features->size() == 0) {
        ID3Algorithm::labelNode(node->getParent());
        node->setLabel(node->getParent()->getLabel());
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

    for(int label : classes) {
        if(label == targetLabel) {
            numSamplesWithLabel++;
        }
    }

    return ((double) numSamplesWithLabel) / classes.size();
}

double
ID3Algorithm::getMinimumValueForGivenFeature(const my::features& features, int relevantColumnIndex) {
    double minimumFeatureValue = features.at(0).at(relevantColumnIndex);
    for(int sampleIndex = 1; sampleIndex < features.size(); sampleIndex++) {
        minimumFeatureValue = std::min(minimumFeatureValue, features.at(sampleIndex).at(relevantColumnIndex));
    }

    return minimumFeatureValue;
}

double
ID3Algorithm::getMaximumValueForGivenFeature(const my::features& features, int relevantColumnIndex) {
    double maximumFeatureValue = features.at(0).at(relevantColumnIndex);
    for(int sampleIndex = 1; sampleIndex < features.size(); sampleIndex++) {
        maximumFeatureValue = std::max(maximumFeatureValue, features.at(sampleIndex).at(relevantColumnIndex));
    }

    return maximumFeatureValue;
}

my::intervals
ID3Algorithm::getIntervalsForFeature(const my::features& features, int featureColumnIndex, int numIntervals) {
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
ID3Algorithm::getPartitionedData(const my::features& features,
                                 const my::classes& classes,
                                 const my::intervals& intervals,
                                 int indexOfFeatureUsedToSplitSamples) {
    std::vector<my::training_data> partitionedData(intervals.size());

    for(int sampleIndex = 0; sampleIndex < features.size(); sampleIndex++) {
        double featureVal = features.at(sampleIndex).at(indexOfFeatureUsedToSplitSamples);
        for(int intervalIndex = 0; intervalIndex < intervals.size(); intervalIndex++) {
            if(featureVal <= intervals.at(intervalIndex).first) {
                partitionedData.at(intervalIndex).first.push_back(features.at(sampleIndex));
                partitionedData.at(intervalIndex).second.push_back(classes.at(sampleIndex));
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
ID3Algorithm::findLowestEntropyFeature(const my::features& features) {
    int minEntropyFeature = 0;
    double minEntropy = std::numeric_limits<double>::lowest();

    for(int featureIndex = 0; featureIndex < features.size(); featureIndex++) {
        if(Node::attributesAlreadyUsedToSplitANode.find(featureIndex) ==
           Node::attributesAlreadyUsedToSplitANode.end()) {
            my::intervals intervals =
                 ID3Algorithm::getIntervalsForFeature(
                                features,
                                featureIndex,
                                ID3Algorithm::MIN_SAMPLES_FOR_SPLIT);


        }
    }

    return minEntropyFeature;
}

my::partitionedDataset
ID3Algorithm::splitSamplesAmongstIntervals(const my::features& features,
                                           const my::classes& classes,
                                           const my::intervals& intervals,
                                           int indexOfFeatureUsedToSplitSamples) {
    my::partitionedDataset partitionedData =
          std::make_pair(std::vector<my::features>(intervals.size()),
                         std::vector<my::classes>(intervals.size()));

    for(int sampleIndex = 0; sampleIndex < features.size(); sampleIndex++) {
        double featureVal = features.at(sampleIndex).at(indexOfFeatureUsedToSplitSamples);

        for(int intervalIndex = 0; intervalIndex < intervals.size(); intervalIndex++) {
            my::interval currInterval = intervals.at(intervalIndex);
            if(featureVal < currInterval.first) {
                partitionedData.first.at(intervalIndex).push_back(features.at(sampleIndex));
                partitionedData.second.at(intervalIndex).push_back(classes.at(sampleIndex));
                break;
            }
        }
    }

    return partitionedData;
}
