#ifndef TRAIN_STRAT_H
#define TRAIN_STRAT_H

#include "node.hpp"
#include "mytypes.hpp"

class TrainingStrategy {
    public:
        virtual Node* createModel(const my::features&, const my::classes&, int) const = 0;
};

#endif
