#ifndef DECISION_TREE_CLF_H
#define DECISION_TREE_CLF_H

#include <utility>
#include <vector>

#include "TrainingStrategy.hpp"
#include "mytypes.hpp"
#include "node.hpp"

class DecisionTreeClassifier {
    public:
        DecisionTreeClassifier(TrainingStrategy const *, int);
        DecisionTreeClassifier train(const my::features&, const my::classes&);
        my::classes predict(const my::features&);

    private:
        Node* decisionTree;
        TrainingStrategy const * strategy;
        int maxHeight;

        int getLabel(const my::single_sample_features&);
        int getLabelHelper(Node *, const my::single_sample_features&);
        static std::pair<my::features, my::classes>
        getTrainingAndTestSets(const my::features&, const my::classes&);
};

#endif
