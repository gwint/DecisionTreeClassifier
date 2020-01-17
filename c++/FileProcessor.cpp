#include <string>
#include <fstream>
#include <iostream>

#include "FileProcessor.hpp"

FileProcessor::FileProcessor(std::string fileName) {
    this->fileName = fileName;
    this->inputStream.open(this->fileName.c_str());
    if(!this->inputStream.is_open()) {
        std::cerr << "Unable to open file for reading" << std::endl;
        exit(1);
    }
}

std::string FileProcessor::getFileName() {
    return this->fileName;
}

std::string FileProcessor::readNextLine() {
    std::string nextLine;
    std::getline(this->inputStream, nextLine);

    return nextLine;
}
