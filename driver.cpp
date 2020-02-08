#include <iostream>
#include <cstdlib>
#include <string>
#include <utility>

#include "decisiontreeclassifier.hpp"
#include "ID3Algorithm.hpp"
#include "mytypes.hpp"
#include "helper.hpp"
#include "PerformanceMetrics.hpp"
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
            DecisionTreeClassifier<ID3Algorithm>::getTrainingAndTestSets(features,
                                                           classes,
                                                           0.8);

    DecisionTreeClassifier<ID3Algorithm> clf(10, 3, 2);

    double accuracy = calculateAccuracy<ID3Algorithm>(clf, features, classes);

    std::cout << "Accuracy: " << accuracy << std::endl;

    double kFoldAccuracy =
             performStratifiedKFoldCV<ID3Algorithm>(clf, features, classes);

    std::cout << "K-Fold Accuracy: " << kFoldAccuracy << std::endl;

    my::confusion_matrix confusionMatrix =
                   getConfusionMatrix<ID3Algorithm>(clf, features, classes);

    printConfusionMatrix(confusionMatrix);

    double averageTrainingTime =
                     getTrainingTime<ID3Algorithm>(clf, features, classes);

    std::cout << "Average training time = " << averageTrainingTime << "s" << std::endl;

    for(my::single_sample_features* sampleFeaturePtr : features) {
        delete sampleFeaturePtr;
    }

    return 0;
}
