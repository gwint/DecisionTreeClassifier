#ifndef TRAIN_STRAT_H
#define TRAIN_STRAT_H

#include "node.hpp"
#include "mytypes.hpp"

class TrainingStrategy {
    public:
        virtual Node* createModel(my::features*, my::classes*, int) = 0;
};

#endif
