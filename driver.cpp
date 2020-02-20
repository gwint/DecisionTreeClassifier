#include <iostream>
#include <cstdlib>
#include <string>
#include <utility>
#include <thread>
#include <stack>

#include <aws/lambda-runtime/runtime.h>

#include "decisiontreeclassifier.hpp"
#include "ID3Algorithm.hpp"
#include "mytypes.hpp"
#include "helper.hpp"
#include "PerformanceMetrics.hpp"
#include "node.hpp"

std::vector<Node*> nodesToDelete;
std::stack<Node> nodeBucket;

using aws::lambda_runtime::invocation_response;
using aws::lambda_runtime::invocation_request;

invocation_response handler(invocation_request const& request) {
    return invocation_response::success(request.payload, "application/json");
}

int main(int argv, char** args) {
    run_handler(handler);
    return 0;

    if(argv != 3) {
        std::cout << "Usage: ./main <features file> <classes file>" << std::endl;
        exit(1);
    }

    char* featuresFileName = args[1];
    char* classesFileName = args[2];

    my::multiple_sample_features features = readFeatures(std::string(featuresFileName));
    my::multiple_sample_classes classes = readClasses(std::string(classesFileName));

    for(int i = 0; i < 1000000; i++) {
        nodeBucket.push(Node());
    }

    std::pair<my::training_data, my::testing_data> splitData =
            DecisionTreeClassifier<ID3Algorithm>::getTrainingAndTestSets(features,
                                                           classes,
                                                           0.8);

    DecisionTreeClassifier<ID3Algorithm> clf =
                 DecisionTreeClassifier<ID3Algorithm>()
                                              .setMaxTreeHeight(11)
                                              .setNumDataPartitions(13)
                                              .setMinimumSamplesForSplit(10);

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

/*
    for(Node* node : nodesToDelete) {
        delete node;
    }
*/
    return 0;
}
