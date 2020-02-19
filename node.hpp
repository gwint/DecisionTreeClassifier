#ifndef NODE_H
#define NODE_H

#include <unordered_set>
#include <vector>

#include "mytypes.hpp"

class Node {
    public:
        Node();
        Node(const my::multiple_sample_features&, const my::multiple_sample_classes&);
        void setIndexOfFeatureToUseToSplitSamplesUp(const unsigned int);
        unsigned int getIndexOfFeatureToUseToSplitSamplesUp() const;
        const my::multiple_sample_features& getFeatures() const;
        const my::multiple_sample_classes& getClasses() const;
        void setParent(Node*);
        Node* getParent();
        void setLabel(const int);
        int getLabel() const;
        void setFeatures(const my::multiple_sample_features&);
        void setClasses(const my::multiple_sample_classes&);
        const std::vector<Node*>& getChildren() const;
        void setChildren(const std::vector<Node*>&);
        bool doIncludedSamplesAllHaveSameClass();
        bool isLeaf() const;
        ~Node();
        Node(const Node&);
        Node& operator=(const Node&);

    private:
        static const int NO_LABEL_ASSIGNED = -1;
        static const int NO_INDEX_ASSIGNED = -1;

        std::vector<Node*> children;
        int label;
        unsigned int indexOfFeatureToUseToSplitSamplesUp;
        Node* parent;
        my::multiple_sample_features features;
        my::multiple_sample_classes classes;
};

#endif
