#ifndef ID3_ALGO_H
#define ID3_ALGO_H

#include <vector>

#include "TrainingStrategy.hpp"
#include "mytypes.hpp"
#include "node.hpp"

class ID3Algorithm : public TrainingStrategy {
    public:
        Node* createModel(const my::multiple_sample_features&, const my::multiple_sample_classes&, int) override;
        static const int NUM_DATA_PARTITIONS = 12;
        static my::intervals getIntervalsForFeature(const my::multiple_sample_features&, int, int);

    private:
        static const int MIN_SAMPLES_FOR_SPLIT = 10;
        static void trainHelper(Node*, int);
        static std::vector<Node*> createChildren(const std::vector<my::training_data>&, const Node*);
        static void labelNode(Node*);
        static double getProportion(int, const my::multiple_sample_classes&);
        static double getMinimumValueForGivenFeature(const my::multiple_sample_features&, int);
        static double getMaximumValueForGivenFeature(const my::multiple_sample_features&, int);
        static std::vector<my::training_data> getPartitionedData(const my::multiple_sample_features&, const my::multiple_sample_classes&, const my::intervals&, int);
        static double calculateEntropy(const my::multiple_sample_classes&);
        static double calculateInformationGain(const std::vector<my::training_data>&, double);
        static int findFeatureProvidingLargestInfoGain(const my::multiple_sample_features&, const my::multiple_sample_classes&);
};

#endif
