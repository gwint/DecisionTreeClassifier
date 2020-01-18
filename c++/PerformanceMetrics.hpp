#ifndef PERF_METRICS_H
#define PERF_METRICS_H

#include "mytypes.hpp"

double calculateAccuracy();
double performStratifiedKFoldCV();
my::confusion_matrix getConfusionMatrix();

#endif
