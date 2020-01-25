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
        void train(const my::multiple_sample_features&, const my::multiple_sample_classes&);
        my::multiple_sample_classes predict(const my::multiple_sample_features&);
        static std::pair<my::training_data, my::testing_data>
        getTrainingAndTestSets(const my::multiple_sample_features&, const my::multiple_sample_classes&, double);
        ~DecisionTreeClassifier();
        DecisionTreeClassifier(const DecisionTreeClassifier&);
        Node* decisionTree;

    private:
        TrainingStrategy* strategy;
        int maxHeight;

        int getLabel(my::single_sample_features*);
        int getLabelHelper(Node *, my::single_sample_features*);
        TrainingStrategy* getStrategy();
};

#endif
