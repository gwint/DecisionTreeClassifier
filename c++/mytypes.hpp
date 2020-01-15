#ifndef MY_TYPES_H
#define MY_TYPES_H

#include <vector>
#include <utility>

namespace my {
    typedef std::vector<std::vector<double>> features;
    typedef std::vector<int> classes;
    typedef std::vector<double> single_sample_features;
    typedef std::pair<my::features, my::classes> training_data;
    typedef std::pair<my::features, my::classes> testing_data;
    typedef std::pair<double, double> interval;
    typedef std::vector<my::interval> intervals;
    typedef std::pair<std::vector<my::features>, std::vector<my::classes>> partitionedDataset;
};

#endif
