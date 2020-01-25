#ifndef PERF_METRICS_H
#define PERF_METRICS_H

#include "mytypes.hpp"
#include "decisiontreeclassifier.hpp"

const int NUM_ITERS = 10;
const int NUM_FOLDS = 5;

double
calculateAccuracy(DecisionTreeClassifier,
                     const my::multiple_sample_features&,
                     const my::multiple_sample_classes&);

double
performStratifiedKFoldCV(DecisionTreeClassifier,
                                const my::multiple_sample_features&,
                                const my::multiple_sample_classes&);

my::confusion_matrix
getConfusionMatrix(DecisionTreeClassifier,
                     const my::multiple_sample_features&,
                     const my::multiple_sample_classes&);

void
printConfusionMatrix(const my::confusion_matrix&);

#endif
