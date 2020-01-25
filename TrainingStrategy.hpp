#ifndef TRAIN_STRAT_H
#define TRAIN_STRAT_H

#include "node.hpp"
#include "mytypes.hpp"

class TrainingStrategy {
    public:
        virtual Node* createModel(const my::multiple_sample_features&, const my::multiple_sample_classes&, int) = 0;
        virtual ~TrainingStrategy() {}
};

#endif
