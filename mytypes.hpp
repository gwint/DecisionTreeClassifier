#ifndef MY_TYPES_H
#define MY_TYPES_H

#include <vector>
#include <utility>

namespace my {
    typedef std::vector<double> single_sample_features;
    typedef std::vector<std::vector<double>*> multiple_sample_features;
    typedef std::vector<int> multiple_sample_classes;
    typedef std::pair<double, double> interval;
    typedef std::vector<my::interval> intervals;

    typedef struct training_data {
        my::multiple_sample_features features;
        my::multiple_sample_classes classes;
    } training_data;

    typedef struct testing_data {
        my::multiple_sample_features features;
        my::multiple_sample_classes classes;
    } testing_data;

    typedef struct confusion_matrix {
        int truePositive;
        int falsePositive;
        int trueNegative;
        int falseNegative;
    } confusion_matrix;
}

#endif
