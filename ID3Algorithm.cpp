#include <unordered_map>
#include <iostream>
#include <algorithm>
#include <cmath>
#include <queue>
#include <functional>
#include <limits>
#include <cassert>
#include <numeric>

#include "ID3Algorithm.hpp"
#include "node.hpp"
#include "mytypes.hpp"

std::unordered_set<int> ID3Algorithm::unusedAttributeIndices;

ID3Algorithm::ID3Algorithm(int maxTreeHeight,
                           int numDataPartitions,
                           int minimumSamplesForSplit) {

    this->maxTreeHeight = maxTreeHeight;
    this->numDataPartitions = numDataPartitions;
    this->minimumSamplesForSplit = minimumSamplesForSplit;
}

Node*
ID3Algorithm::createModel(const my::multiple_sample_features& features,
                          const my::multiple_sample_classes& classes) {

    ID3Algorithm::populateUnusedAttributeIndices(features[0]->size());
    Node* root = new Node(features, classes);

    this->trainHelper(root, this->maxTreeHeight);

    return root;
}

void
ID3Algorithm::populateUnusedAttributeIndices(int numAttributes) {
    ID3Algorithm::unusedAttributeIndices.clear();
    auto insert = [](const int& index) {
        ID3Algorithm::unusedAttributeIndices.insert(index);
    };

    std::vector<int> indices(numAttributes);
    std::iota(indices.begin(), indices.end(), 0);
    std::for_each(indices.begin(), indices.end(), insert);
}

void
ID3Algorithm::trainHelper(Node* treeRoot, int maximumTreeHeight) {

    if(treeRoot == NULL) {
        std::cout << "Decision tree root must not be NULL" << std::endl;
        exit(1);
    }

    const my::multiple_sample_features& features = treeRoot->getFeatures();
    const my::multiple_sample_classes& classes = treeRoot->getClasses();

    auto shouldStopTrainingProcess = [&](void) {
        return maximumTreeHeight == 1 ||
               features.size() < this->minimumSamplesForSplit ||
               features.empty() ||
               treeRoot->doIncludedSamplesAllHaveSameClass() ||
               ID3Algorithm::unusedAttributeIndices.empty();
    };

    if(shouldStopTrainingProcess()) {
        this->labelNode(treeRoot);
        return;
    }

    int columnToUseToSplitSamples =
            this->findFeatureProvidingLargestInfoGain(features, classes);
    treeRoot->setIndexOfFeatureToUseToSplitSamplesUp(columnToUseToSplitSamples);

    my::intervals intervals = this->getIntervalsForFeature(
                                                   features,
                                                   columnToUseToSplitSamples);

    std::vector<my::training_data> maxInfoGainPartition =
         this->getPartitionedData(features,
                                  classes,
                                  intervals,
                                  columnToUseToSplitSamples);

    std::vector<Node*> children =
                this->createChildren(maxInfoGainPartition, treeRoot);

    treeRoot->setChildren(children);

    for(Node* child : children) {
        this->trainHelper(child, maximumTreeHeight-1);
    }
}

void
ID3Algorithm::labelNode(Node* node) {
    if(node == NULL) {
        std::cerr << "Should not be trying to label a NULL node" << std::endl;
        exit(1);
    }

    const my::multiple_sample_features& features = node->getFeatures();
    if(features.empty()) {
        Node* parent = node->getParent();
        if(parent != NULL) {
            this->labelNode(parent);
            node->setLabel(parent->getLabel());
        }
    }
    else {
        const my::multiple_sample_classes& classes = node->getClasses();
        std::unordered_map<int, int> classCounts;
        int numSamples = classes.size();

        int mostFrequentLabel = classes[0];
        int largestFrequency = 1;

        classCounts[mostFrequentLabel] = 1;

        for(int i = 1; i < numSamples; ++i) {
            int label = classes.at(i);
            ++classCounts[label];
            if(classCounts[label] > largestFrequency) {
                mostFrequentLabel = label;
                largestFrequency = classCounts[label];
            }
        }

        node->setLabel(mostFrequentLabel);
    }
}

double
ID3Algorithm::getProportion(int targetLabel, const my::multiple_sample_classes& classes) {
    int numSamples = classes.size();

    if(numSamples == 0) {
        return 0.0;
    }

    auto matchesTarget = [&, targetLabel](const int& label) {
        return label == targetLabel;
    };

    auto numSamplesWithTargetLabel =
                     count_if(classes.begin(), classes.end(), matchesTarget);

    return ((double) numSamplesWithTargetLabel) / numSamples;
}

double
ID3Algorithm::getMinimumValueForGivenFeature(const my::multiple_sample_features& features, int relevantColumnIndex) {
    my::single_sample_features* firstSampleFeatures = features[0];
    int numSamples = features.size();
    double minimumFeatureValue = firstSampleFeatures->at(relevantColumnIndex);
    for(int sampleIndex = 1; sampleIndex < numSamples; ++sampleIndex) {
        my::single_sample_features* sampleFeatures = features[sampleIndex];
        minimumFeatureValue = std::min(minimumFeatureValue, sampleFeatures->operator[](relevantColumnIndex));
    }

    return minimumFeatureValue;
}

double
ID3Algorithm::getMaximumValueForGivenFeature(const my::multiple_sample_features& features, int relevantColumnIndex) {
    my::single_sample_features* firstSampleFeatures = features[0];
    double maximumFeatureValue = firstSampleFeatures->at(relevantColumnIndex);

    int numSamples = features.size();
    for(int sampleIndex = 1; sampleIndex < numSamples; ++sampleIndex) {
        maximumFeatureValue = std::max(maximumFeatureValue, features[sampleIndex]->operator[](relevantColumnIndex));
    }

    return maximumFeatureValue;
}

my::intervals
ID3Algorithm::getIntervalsForFeature(
                             const my::multiple_sample_features& features,
                             int featureColumnIndex) {

    std::vector<my::interval> intervals;

    const double minimumFeatureValue =
             this->getMinimumValueForGivenFeature(features, featureColumnIndex);
    const double maximumFeatureValue =
             this->getMaximumValueForGivenFeature(features, featureColumnIndex);

    const double intervalSize =
      (maximumFeatureValue - minimumFeatureValue) / (this->numDataPartitions-2);

    intervals.push_back(std::make_pair(-std::numeric_limits<double>::max(), minimumFeatureValue));

    for(int numIntervalsMade = 0; numIntervalsMade < this->numDataPartitions-2; ++numIntervalsMade) {
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

    for(int sampleIndex = 0; sampleIndex < numSamples; ++sampleIndex) {
        double featureVal = features[sampleIndex]->operator[](indexOfFeatureUsedToSplitSamples);
        for(int intervalIndex = 0; intervalIndex < numIntervals; ++intervalIndex) {
            if(featureVal <= intervals.at(intervalIndex).first) {
                partitionedData[intervalIndex].features.push_back(features[sampleIndex]);
                partitionedData[intervalIndex].classes.push_back(classes[sampleIndex]);
                break;
            }
        }
    }

    return partitionedData;
}

double
ID3Algorithm::calculateEntropy(const my::multiple_sample_classes& classes) {
    double class1Proportion = ID3Algorithm::getProportion(0, classes);
    double class2Proportion = ID3Algorithm::getProportion(1, classes);

    if(class1Proportion == 0.0 || class2Proportion == 0.0) {
        return 0.0;
    }

    return (-class1Proportion * (log10(class1Proportion) / log10(2))) -
           (class2Proportion * (log10(class2Proportion) / log10(2)));
}

int
ID3Algorithm::findFeatureProvidingLargestInfoGain(
                                const my::multiple_sample_features& features,
                                const my::multiple_sample_classes& classes) {
    int maxInformationGainFeature =
             *(ID3Algorithm::unusedAttributeIndices.begin());

    double entropy = ID3Algorithm::calculateEntropy(classes);

    auto intervals = this->getIntervalsForFeature(features,
                                                  maxInformationGainFeature);

    std::vector<my::training_data> partitionedData =
                this->getPartitionedData(features,
                                         classes,
                                         intervals,
                                         maxInformationGainFeature);

    double maxInformationGain =
              this->calculateInformationGain(partitionedData, entropy);

    auto attrIt = ID3Algorithm::unusedAttributeIndices.begin();
    while(attrIt != ID3Algorithm::unusedAttributeIndices.end()) {
        intervals = this->getIntervalsForFeature(features, *attrIt);

        partitionedData = this->getPartitionedData(features,
                                                   classes,
                                                   intervals,
                                                   *attrIt);

        double resultantInformationGain =
              this->calculateInformationGain(partitionedData, entropy);

        if(resultantInformationGain > maxInformationGain) {
            maxInformationGain = resultantInformationGain;
            maxInformationGainFeature = *attrIt;
        }

        ++attrIt;
    }

    ID3Algorithm::unusedAttributeIndices.erase(maxInformationGainFeature);

    return maxInformationGainFeature;
}

double
ID3Algorithm::calculateInformationGain(const std::vector<my::training_data>& partitionedData, double entropy) {
    double totalPartitionEntropy = 0.0;
    auto datasetSize = partitionedData.size();

    if(datasetSize == 0) {
        return 0.0;
    }

    int totalNumberOfSamplesInParent = 0;

    for(unsigned int i = 0; i < datasetSize; ++i) {
        my::multiple_sample_classes classes = partitionedData.at(i).classes;

        const int partitionSize = classes.size();
        double partitionEntropy = this->calculateEntropy(classes);

        totalPartitionEntropy += (partitionSize * partitionEntropy);
        totalNumberOfSamplesInParent += partitionSize;
    }

    return entropy - (totalPartitionEntropy / totalNumberOfSamplesInParent);
}

std::vector<Node*>
ID3Algorithm::createChildren(const std::vector<my::training_data>& partitionedData, const Node* parent) {
    std::vector<Node*> children;

    for(auto trainingDataIter = partitionedData.begin();
             trainingDataIter != partitionedData.end(); ++trainingDataIter) {
        auto childNode =
               new Node(trainingDataIter->features, trainingDataIter->classes);
        children.push_back(childNode);
    }

    return children;
}
