#ifndef FILE_PROC_H
#define FILE_PROC_H

#include <string>
#include <fstream>

class FileProcessor {
    public:
        FileProcessor(std::string);
        std::string readNextLine();
        std::string getFileName();

    private:
        std::string fileName;
        std::ifstream inputStream;
};

#endif
