#include "PerformanceMetrics.hpp"
#include "mytypes.hpp"
#include "decisiontreeclassifier.hpp"

double
calculateAccuracy(DecisionTreeClassifier clf,
                  my::features& trainingFeatures,
                  my::classes& trainingLabels) {

    int totalCorrectPredictions = 0;
    int totalNumPredictions = 0;

    for(int iteration = 0; iteration < NUM_ITERS; iteration++) {
        std::pair<my::training_data, my::testing_data> splitData =
                 DecisionTreeClassifier::getTrainingAndTestSets(trainingFeatures, trainingLabels, 0.70);
        my::features trainingFeatures = splitData.first.first;
        my::classes trainingLabels = splitData.first.second;
        my::features testingFeatures = splitData.second.first;
        my::classes testingLabels = splitData.second.second;

        clf = clf.train(&trainingFeatures, &trainingLabels);
        my::classes predictions = clf.predict(testingFeatures);

        for(int i = 0; i < predictions.size(); i++) {
            if(predictions.at(i) == testingLabels.at(i)) {
                totalCorrectPredictions++;
            }
            totalNumPredictions++;
        }
    }

    return ((double) totalCorrectPredictions) / totalNumPredictions;
}

double
performStratifiedKFoldCV(DecisionTreeClassifier& clf,
                                my::features& features,
                                my::classes& classes) {

    int totalNumCorrect = 0;
    int totalPredictionsMade = 0;

    int foldSize = features.size() / NUM_FOLDS;

    int excludeStart = 0;
    int excludeEnd = foldSize;


    for(int fold = 0; fold < NUM_FOLDS; fold++) {
        my::features trainingFeatures;
        my::classes trainingLabels;
        my::features testingFeatures;
        my::classes testingLabels;

        for(int sampleIndex = 0; sampleIndex < features.size(); sampleIndex++) {
            if(sampleIndex >= excludeStart && sampleIndex < excludeEnd) {
                testingFeatures.push_back(features.at(sampleIndex));
                testingLabels.push_back(classes.at(sampleIndex));
            }
            else {
                trainingFeatures.push_back(features.at(sampleIndex));
                trainingLabels.push_back(classes.at(sampleIndex));
            }
        }

        clf = clf.train(&trainingFeatures, &trainingLabels);
        my::classes predictions = clf.predict(testingFeatures);

        for(int i = 0; i < predictions.size(); i++) {
            if(predictions.at(i) == testingLabels.at(i)) {
                totalNumCorrect++;
            }
            totalPredictionsMade++;
        }
    }

    return ((double) totalNumCorrect) / totalPredictionsMade;
}

my::confusion_matrix
getConfusionMatrix(DecisionTreeClassifier clf, my::features& features, my::classes& classes) {
    my::confusion_matrix confusionMatrix;
    confusionMatrix.first.first = 0;
    confusionMatrix.first.second = 0;
    confusionMatrix.second.first = 0;
    confusionMatrix.second.second = 0;

    std::pair<my::training_data, my::testing_data> trainAndTestSets =
                DecisionTreeClassifier::getTrainingAndTestSets(features, classes, 0.50);
    my::features trainingFeatures = trainAndTestSets.first.first;
    my::classes trainingLabels = trainAndTestSets.first.second;
    my::features testingFeatures = trainAndTestSets.second.first;
    my::classes testingLabels = trainAndTestSets.second.second;

    my::classes predictions = clf.predict(testingFeatures);

    for(int i = 0; i < predictions.size(); i++) {
        int predictedClass = predictions.at(i);
        int actualClass = testingLabels.at(i);

        if(predictedClass == actualClass && actualClass == 0) {
            confusionMatrix.first.first++;
        }
        else if(predictedClass != actualClass && actualClass == 0) {
            confusionMatrix.first.second++;
        }
        else if(predictedClass != actualClass && actualClass == 1) {
            confusionMatrix.second.first++;
        }
        else {
            confusionMatrix.second.second++;
        }
    }

    return confusionMatrix;
}
