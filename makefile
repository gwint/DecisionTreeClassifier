all:	main

main:	driver.o decisiontreeclassifier.o node.o ID3Algorithm.o FileProcessor.o helper.o PerformanceMetrics.o
	clang++ driver.o decisiontreeclassifier.o node.o ID3Algorithm.o FileProcessor.o helper.o PerformanceMetrics.o -o main -pg

driver.o:	driver.cpp
	clang++ -pg -c driver.cpp -O3

node.o:	node.cpp
	clang++ -pg -c node.cpp -O3

ID3Algorithm.o:	ID3Algorithm.cpp
	clang++ -pg -c ID3Algorithm.cpp -O3

decisiontreeclassifier.o:	decisiontreeclassifier.cpp
	clang++ -pg -c decisiontreeclassifier.cpp -O3

FileProcessor.o:	FileProcessor.cpp
	clang++ -pg -c FileProcessor.cpp -O3

helper.o:	helper.cpp
	clang++ -pg -c helper.cpp -O3

PerformanceMetrics.o:	PerformanceMetrics.cpp
	clang++ -pg -c PerformanceMetrics.cpp -O3

clean:
	rm *.o main
