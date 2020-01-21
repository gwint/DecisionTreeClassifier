#ifndef PERF_METRICS_H
#define PERF_METRICS_H

#include "mytypes.hpp"
#include "decisiontreeclassifier.hpp"

const int NUM_ITERS = 10;
const int NUM_FOLDS = 10;

double
calculateAccuracy(DecisionTreeClassifier,
                     const my::features&,
                     const my::classes&);

double
performStratifiedKFoldCV(DecisionTreeClassifier,
                                const my::features&,
                                const my::classes&);

my::confusion_matrix
getConfusionMatrix(DecisionTreeClassifier,
                     const my::features&,
                     const my::classes&);

void
printConfusionMatrix(const my::confusion_matrix&);

#endif
