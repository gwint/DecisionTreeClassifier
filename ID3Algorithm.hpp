#ifndef ID3_ALGO_H
#define ID3_ALGO_H

#include <vector>

#include "mytypes.hpp"
#include "node.hpp"

class ID3Algorithm {
    public:
        ID3Algorithm() {}
        ID3Algorithm(unsigned int, unsigned int, unsigned int);
        Node* createModel(const my::multiple_sample_features&,
                          const my::multiple_sample_classes&);
        my::intervals getIntervalsForFeature(const my::multiple_sample_features&, int);

    private:
        unsigned int maxTreeHeight;
        unsigned int numDataPartitions;
        unsigned int minimumSamplesForSplit;

        void trainHelper(Node*, int);
        std::vector<Node*> createChildren(const std::vector<my::training_data>&, const Node*);
        void labelNode(Node*);
        double getProportion(int, const my::multiple_sample_classes&);
        double getMinimumValueForGivenFeature(const my::multiple_sample_features&, int);
        double getMaximumValueForGivenFeature(const my::multiple_sample_features&, int);
        std::vector<my::training_data> getPartitionedData(const my::multiple_sample_features&, const my::multiple_sample_classes&, const my::intervals&, int);
        double calculateEntropy(const my::multiple_sample_classes&);
        double calculateInformationGain(const std::vector<my::training_data>&, double);
        int findFeatureProvidingLargestInfoGain(const my::multiple_sample_features&, const my::multiple_sample_classes&);
        static std::unordered_set<int> unusedAttributeIndices;
        void populateUnusedAttributeIndices(int);
};

#endif
