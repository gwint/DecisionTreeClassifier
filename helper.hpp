#ifndef HELPER_H
#define HELPER_H

#include <string>

#include "mytypes.hpp"

my::multiple_sample_features readFeatures(std::string);
my::multiple_sample_classes readClasses(std::string);
std::string get(const std::string&);
my::training_data getData(const std::string&);

#endif
