#ifndef ID3_ALGO_H
#define ID3_ALGO_H

#include <vector>

#include "TrainingStrategy.hpp"
#include "mytypes.hpp"
#include "node.hpp"

class ID3Algorithm : public TrainingStrategy {
    public:
        Node* createModel(const my::features&, const my::classes&, int) const override;

    private:
        static const int NUM_DATA_PARTITIONS = 5;
        static const int MIN_SAMPLES_FOR_SPLIT = 10;
        static void trainHelper(Node*, int);
        static std::vector<Node*> createChildren();
        static void labelNode(Node*);
        static double getProportion(int, const my::classes&);
        static my::intervals getIntervalsForFeature(const my::features&, int, int);
        static double getMinimumValueForGivenFeature(const my::features&, int);
        static double getMaximumValueForGivenFeature(const my::features&, int);
        static std::vector<my::training_data> getPartitionedData(const my::features&, const my::classes&, const my::intervals&, int);
        static double calculateEntropy(const std::vector<my::training_data>&);
        static int findLowestEntropyFeature(const my::features&);
        static my::partitionedDataset splitSamplesAmongstIntervals(const my::features&, const my::classes&, const my::intervals&, int);
};

#endif
