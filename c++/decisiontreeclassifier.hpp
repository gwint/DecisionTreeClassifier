#ifndef DECISION_TREE_CLF_H
#define DECISION_TREE_CLF_H

#include <utility>
#include <vector>

#include "TrainingStrategy.hpp"
#include "mytypes.hpp"
#include "node.hpp"

class DecisionTreeClassifier {
    public:
        DecisionTreeClassifier(TrainingStrategy*, int);
        DecisionTreeClassifier train(my::features*, my::classes*);
        my::classes predict(const my::features&);
        static std::pair<my::training_data, my::testing_data>
        getTrainingAndTestSets(const my::features&, const my::classes&, double);

    private:
        Node* decisionTree;
        TrainingStrategy* strategy;
        int maxHeight;

        int getLabel(const my::single_sample_features&);
        int getLabelHelper(Node *, const my::single_sample_features&);
        TrainingStrategy* getStrategy();
};

#endif
