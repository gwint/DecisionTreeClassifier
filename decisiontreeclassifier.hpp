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
        void train(const my::features&, const my::classes&);
        my::classes predict(const my::features&);
        static std::pair<my::training_data, my::testing_data>
        getTrainingAndTestSets(const my::features&, const my::classes&, double);
        ~DecisionTreeClassifier();
        DecisionTreeClassifier(const DecisionTreeClassifier&);
        Node* decisionTree;

    private:
        TrainingStrategy* strategy;
        int maxHeight;

        int getLabel(const my::single_sample_features&);
        int getLabelHelper(Node *, const my::single_sample_features&);
        TrainingStrategy* getStrategy();
};

#endif
