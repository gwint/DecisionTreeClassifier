all:	main

main:	driver.o decisiontreeclassifier.o node.o ID3Algorithm.o FileProcessor.o helper.o PerformanceMetrics.o
	clang++ driver.o decisiontreeclassifier.o node.o ID3Algorithm.o FileProcessor.o helper.o PerformanceMetrics.o -o main -pg

driver.o:	driver.cpp
	clang++ -pg -c driver.cpp

node.o:	node.cpp
	clang++ -pg -c node.cpp

ID3Algorithm.o:	ID3Algorithm.cpp
	clang++ -pg -c ID3Algorithm.cpp

decisiontreeclassifier.o:	decisiontreeclassifier.cpp
	clang++ -pg -c decisiontreeclassifier.cpp

FileProcessor.o:	FileProcessor.cpp
	clang++ -pg -c FileProcessor.cpp

helper.o:	helper.cpp
	clang++ -pg -c helper.cpp

PerformanceMetrics.o:	PerformanceMetrics.cpp
	clang++ -pg -c PerformanceMetrics.cpp

clean:
	rm *.o main
