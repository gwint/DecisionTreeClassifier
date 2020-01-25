#include <string>
#include <sstream>

#include "helper.hpp"
#include "mytypes.hpp"
#include "FileProcessor.hpp"

my::multiple_sample_features readFeatures(std::string featuresFileName) {
    my::multiple_sample_features features;
    FileProcessor processor(featuresFileName);

    std::string currLine = processor.readNextLine();
    while(currLine.length() > 0) {
        std::stringstream lineParser(currLine);
        std::string val;
        my::single_sample_features* sampleFeatures = new my::single_sample_features;
        while(std::getline(lineParser, val, ',')) {
            sampleFeatures->push_back(std::stod(val, NULL));
        }
        features.push_back(sampleFeatures);

        currLine = processor.readNextLine();
    }

    return features;
}

my::multiple_sample_classes readClasses(std::string classesFileName) {
    my::multiple_sample_classes classes;
    FileProcessor processor(classesFileName);

    std::string currLine = processor.readNextLine();
    while(currLine.length() > 0) {
        classes.push_back(std::stoi(currLine, NULL));
        currLine = processor.readNextLine();
    }

    return classes;
}

