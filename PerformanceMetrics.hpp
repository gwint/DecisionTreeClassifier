#ifndef PERF_METRICS_H
#define PERF_METRICS_H

#include <chrono>
#include <iostream>
#include <cstdio>

#include "mytypes.hpp"
#include "decisiontreeclassifier.hpp"

const int NUM_ITERS = 1;
const int NUM_FOLDS = 5;

template <typename T>
double
calculateAccuracy(DecisionTreeClassifier<T>,
                     const my::multiple_sample_features&,
                     const my::multiple_sample_classes&);

template <typename T>
double
performStratifiedKFoldCV(DecisionTreeClassifier<T>,
                                const my::multiple_sample_features&,
                                const my::multiple_sample_classes&);

template <typename T>
my::confusion_matrix
getConfusionMatrix(DecisionTreeClassifier<T>,
                     const my::multiple_sample_features&,
                     const my::multiple_sample_classes&);

void
printConfusionMatrix(const my::confusion_matrix&);

template <typename T>
double
getTrainingTime(DecisionTreeClassifier<T>,
                    const my::multiple_sample_features&,
                    const my::multiple_sample_classes&);

template <typename T>
double
calculateAccuracy(DecisionTreeClassifier<T> clf,
                  const my::multiple_sample_features& trainingFeatures,
                  const my::multiple_sample_classes& trainingLabels) {

    int totalCorrectPredictions = 0;
    int totalNumPredictions = 0;

    my::multiple_sample_classes predictions;

    for(int iteration = 0; iteration < NUM_ITERS; iteration++) {
        std::pair<my::training_data, my::testing_data> splitData =
            DecisionTreeClassifier<T>::getTrainingAndTestSets(trainingFeatures,
                                                              trainingLabels,
                                                              0.80);

        my::multiple_sample_features trainingFeatures =
                                            splitData.first.features;
        my::multiple_sample_classes trainingLabels =
                                            splitData.first.classes;
        my::multiple_sample_features testingFeatures =
                                            splitData.second.features;
        my::multiple_sample_classes testingLabels =
                                            splitData.second.classes;

        clf.train(trainingFeatures, trainingLabels);
        std::cout << clf << std::endl;
        predictions = clf.predict(testingFeatures);

        for(unsigned int i = 0; i < predictions.size(); i++) {
            if(predictions.at(i) == testingLabels.at(i)) {
                totalCorrectPredictions++;
            }
            totalNumPredictions++;
        }
    }

    if(predictions.empty()) {
        return 0.0;
    }


    return ((double) totalCorrectPredictions) / totalNumPredictions;
}

template <typename T>
double
performStratifiedKFoldCV(DecisionTreeClassifier<T> clf,
                              const my::multiple_sample_features& features,
                              const my::multiple_sample_classes& classes) {

    int totalNumCorrect = 0;
    int totalPredictionsMade = 0;

    int foldSize = features.size() / NUM_FOLDS;

    int excludeStart = 0;
    int excludeEnd = foldSize;


    for(int fold = 0; fold < NUM_FOLDS; fold++) {
        my::multiple_sample_features trainingFeatures;
        my::multiple_sample_classes trainingLabels;
        my::multiple_sample_features testingFeatures;
        my::multiple_sample_classes testingLabels;

        for(unsigned int sampleIndex = 0; sampleIndex < features.size(); sampleIndex++) {
            if(sampleIndex >= (unsigned) excludeStart && sampleIndex < (unsigned) excludeEnd) {
                testingFeatures.push_back(features.at(sampleIndex));
                testingLabels.push_back(classes.at(sampleIndex));
            }
            else {
                trainingFeatures.push_back(features.at(sampleIndex));
                trainingLabels.push_back(classes.at(sampleIndex));
            }
        }

        clf.train(trainingFeatures, trainingLabels);
        my::multiple_sample_classes predictions = clf.predict(testingFeatures);

        for(unsigned int i = 0; i < predictions.size(); i++) {
            if(predictions.at(i) == testingLabels.at(i)) {
                totalNumCorrect++;
            }
            totalPredictionsMade++;
        }
    }

    return ((double) totalNumCorrect) / totalPredictionsMade;
}

template <typename T>
my::confusion_matrix
getConfusionMatrix(DecisionTreeClassifier<T> clf, const my::multiple_sample_features& features, const my::multiple_sample_classes& classes) {
    my::confusion_matrix confusionMatrix;

    confusionMatrix.truePositive = 0;
    confusionMatrix.falsePositive = 0;
    confusionMatrix.trueNegative = 0;
    confusionMatrix.falseNegative = 0;

    std::pair<my::training_data, my::testing_data> trainAndTestSets =
            DecisionTreeClassifier<T>::getTrainingAndTestSets(features,
                                                              classes,
                                                              0.70);

    my::multiple_sample_features trainingFeatures =
                                         trainAndTestSets.first.features;
    my::multiple_sample_classes trainingLabels =
                                         trainAndTestSets.first.classes;
    my::multiple_sample_features testingFeatures =
                                         trainAndTestSets.second.features;
    my::multiple_sample_classes testingLabels =
                                         trainAndTestSets.second.classes;

    clf.train(trainingFeatures, trainingLabels);

    my::multiple_sample_classes predictions = clf.predict(testingFeatures);

    for(unsigned int i = 0; i < predictions.size(); i++) {
        int predictedClass = predictions.at(i);
        int actualClass = testingLabels.at(i);

        if(predictedClass == actualClass && actualClass == 0) {
            confusionMatrix.truePositive++;
        }
        else if(predictedClass != actualClass && actualClass == 0) {
            confusionMatrix.falsePositive++;
        }
        else if(predictedClass != actualClass && actualClass == 1) {
            confusionMatrix.falseNegative++;
        }
        else {
            confusionMatrix.trueNegative++;
        }
    }

    return confusionMatrix;
}

void
printConfusionMatrix(const my::confusion_matrix& matrix) {
    printf("TP: %d\tFP: %d\n", matrix.truePositive, matrix.falsePositive);
    printf("FN: %d\tTN: %d\n", matrix.falseNegative, matrix.trueNegative);

    double ppv = ((double) matrix.truePositive) / (matrix.truePositive + matrix.falsePositive);
    double npv = ((double) matrix.trueNegative) / (matrix.trueNegative + matrix.falseNegative);

    printf("PPV: %f\tNPV: %f\n", ppv, npv);
}

template <typename T>
double
getTrainingTime(DecisionTreeClassifier<T> clf,
                    const my::multiple_sample_features& features,
                    const my::multiple_sample_classes& classes) {


    std::pair<my::training_data, my::testing_data> splitData =
            DecisionTreeClassifier<T>::getTrainingAndTestSets(features,
                                                              classes,
                                                              0.70);

    my::multiple_sample_features trainingFeatures = splitData.first.features;
    my::multiple_sample_classes trainingLabels = splitData.first.classes;
    my::multiple_sample_features testingFeatures = splitData.second.features;
    my::multiple_sample_classes testingLabels = splitData.second.classes;

    for(int i = 0; i < 10; i++) {
        clf.train(trainingFeatures, trainingLabels);
    }

    std::chrono::high_resolution_clock::time_point t1, t2;

    t1 = std::chrono::high_resolution_clock::now();

    for(int i = 0; i < 2500; i++) {
        clf.train(trainingFeatures, trainingLabels);
    }

    t2 = std::chrono::high_resolution_clock::now();

    std::chrono::duration<double> timeSpan = std::chrono::duration_cast<std::chrono::duration<double>>(t2 - t1);

    return timeSpan.count() / 2500;
}

#endif
