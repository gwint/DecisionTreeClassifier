#include <iostream>
#include <cstdlib>

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

    std::cout << featuresFileName << std::endl;
    std::cout << classesFileName << std::endl;

    my::features features;
    my::classes classes;

    DecisionTreeClassifier clf = DecisionTreeClassifier(new ID3Algorithm(), 15);
    clf = clf.train(&features, &classes);

    return 0;
}
