#include <string>
#include <sstream>
#include <cstdlib>
#include <iostream>

#include "helper.hpp"
#include "mytypes.hpp"
#include "FileProcessor.hpp"

my::multiple_sample_features readFeatures(const std::string& featuresFileName) {
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

my::multiple_sample_classes readClasses(const std::string& classesFileName) {
    my::multiple_sample_classes classes;
    FileProcessor processor(classesFileName);

    std::string currLine = processor.readNextLine();
    while(currLine.length() > 0) {
        classes.push_back(std::stoi(currLine, NULL));
        currLine = processor.readNextLine();
    }

    return classes;
}

std::string get(const std::string& url) {
    char buffer[128];
    const std::string command = std::string("wget ") + url;
    std::string result = "";
    FILE* pipe = popen(command.c_str(), "r");
    if(pipe == NULL) {
        std::cerr << "popen() call failed!\n";
        exit(1);
    }

    try {
        while(fgets(buffer, sizeof buffer, pipe) != NULL) {
            result += buffer;
        }
    }
    catch(...) {
        pclose(pipe);
        throw;
    }

    pclose(pipe);

    return result;
}

my::training_data getData(const std::string& url) {
    my::training_data data;

    const std::string urlContents = get(url);
    std::string currentLine;
    while(std::getline(std::stringstream(urlContents), currentLine)) {
        my::single_sample_features* sampleFeatures = new my::single_sample_features;
        std::string val;
        while(std::getline(std::stringstream(currentLine), val, ',')) {
            if(val.find("\n") != std::string::npos) {
                data.classes.push_back(std::stoi(val));
            }
            else {
                sampleFeatures->push_back(std::stod(val));
            }
        }
        data.features.push_back(sampleFeatures);
    }

    return data;
}
