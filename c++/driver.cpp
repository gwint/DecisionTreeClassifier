#include <iostream>
#include <cstdlib>
#include <string>
#include <utility>

#include "decisiontreeclassifier.hpp"
#include "ID3Algorithm.hpp"
#include "mytypes.hpp"
#include "helper.hpp"

int main(int argv, char** args) {
    if(argv != 3) {
        std::cout << "Usage: ./main <features file> <classes file>" << std::endl;
        exit(1);
    }

    char* featuresFileName = args[1];
    char* classesFileName = args[2];

    my::features features = readFeatures(std::string(featuresFileName));
    my::classes classes = readClasses(std::string(classesFileName));

    std::pair<my::training_data, my::testing_data> splitData =
            DecisionTreeClassifier::getTrainingAndTestSets(features,
                                                           classes,
                                                           0.7);

    my::features trainingFeatures = splitData.first.first;
    my::classes trainingClasses = splitData.first.second;
    my::features testingFeatures = splitData.second.first;
    my::classes testingClasses = splitData.second.second;

    DecisionTreeClassifier clf = DecisionTreeClassifier(new ID3Algorithm(), 15);
    clf = clf.train(&trainingFeatures, &trainingClasses);

    my::classes predictions = clf.predict(testingFeatures);

    return 0;
}
