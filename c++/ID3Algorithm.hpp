#ifndef ID3_ALGO_H
#define ID3_ALGO_H

#include <vector>

#include "TrainingStrategy.hpp"
#include "mytypes.hpp"

class ID3Algorithm : public TrainingStrategy {
    public:
        Node* createModel(const my::features&, const my::classes&, int) const override;

    private:
        static const int NUM_DATA_PARTITIONS = 5;
        static const int MIN_SAMPLES_FOR_SPLIT = 10;
        static void trainHelper(Node*, int);
        static std::vector<Node*> createChildren();
};

#endif
