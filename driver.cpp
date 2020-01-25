#include <iostream>
#include <cstdlib>
#include <string>
#include <utility>

#include "decisiontreeclassifier.hpp"
#include "ID3Algorithm.hpp"
#include "mytypes.hpp"
#include "helper.hpp"
#include "PerformanceMetrics.hpp"
#include "TrainingStrategy.hpp"
#include "node.hpp"

int main(int argv, char** args) {
    if(argv != 3) {
        std::cout << "Usage: ./main <features file> <classes file>" << std::endl;
        exit(1);
    }

    char* featuresFileName = args[1];
    char* classesFileName = args[2];

    my::multiple_sample_features features = readFeatures(std::string(featuresFileName));
    my::multiple_sample_classes classes = readClasses(std::string(classesFileName));

    std::pair<my::training_data, my::testing_data> splitData =
            DecisionTreeClassifier::getTrainingAndTestSets(features,
                                                           classes,
                                                           0.7);

    TrainingStrategy* strategy = new ID3Algorithm();
    DecisionTreeClassifier clf = DecisionTreeClassifier(strategy, 15);

    double accuracy = calculateAccuracy(clf, features, classes);

    std::cout << "Accuracy: " << accuracy << std::endl;

    double kFoldAccuracy =
                 performStratifiedKFoldCV(clf, features, classes);

    std::cout << "K-Fold Accuracy: " << kFoldAccuracy << std::endl;

    my::confusion_matrix confusionMatrix =
                      getConfusionMatrix(clf, features, classes);

    printConfusionMatrix(confusionMatrix);

    delete strategy;

    for(my::single_sample_features* sampleFeaturePtr : features) {
        delete sampleFeaturePtr;
    }

    return 0;
}
