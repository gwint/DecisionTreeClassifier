#ifndef NODE_H
#define NODE_H

#include <unordered_set>
#include <vector>

#include "mytypes.hpp"

class Node {
    public:
        static std::unordered_set<int> attributesAlreadyUsedToSplitANode;
        Node(const my::multiple_sample_features&, const my::multiple_sample_classes&);
        void setIndexOfFeatureToUseToSplitSamplesUp(int);
        int getIndexOfFeatureToUseToSplitSamplesUp();
        my::multiple_sample_features getFeatures();
        my::multiple_sample_classes getClasses();
        void setParent(Node*);
        Node* getParent();
        void setLabel(int);
        int getLabel();
        std::vector<Node*> getChildren();
        void setChildren(const std::vector<Node*>&);
        bool doIncludedSamplesAllHaveSameClass();
        bool isLeaf();
        ~Node();
        Node(const Node&);
        Node& operator=(const Node&);
        static int NUM_NODES;

    private:
        static const int NO_LABEL_ASSIGNED = -1;
        static const int NO_INDEX_ASSIGNED = -1;

        std::vector<Node*> children;
        int label;
        int indexOfFeatureToUseToSplitSamplesUp;
        Node* parent;
        my::multiple_sample_features features;
        my::multiple_sample_classes classes;
};

#endif
