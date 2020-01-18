#ifndef PERF_METRICS_H
#define PERF_METRICS_H

#include "mytypes.hpp"
#include "decisiontreeclassifier.hpp"

const int NUM_ITERS = 10;
const int NUM_FOLDS = 10;

double calculateAccuracy(DecisionTreeClassifier,
                         my::features&,
                         my::classes&);

double performStratifiedKFoldCV(DecisionTreeClassifier&,
                                my::features&,
                                my::classes&);

my::confusion_matrix
getConfusionMatrix(DecisionTreeClassifier,
                     my::features&,
                     my::classes&);

#endif
