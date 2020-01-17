all:	main

main:	driver.o decisiontreeclassifier.o node.o ID3Algorithm.o FileProcessor.o helper.o
	clang++ driver.o decisiontreeclassifier.o node.o ID3Algorithm.o FileProcessor.o helper.o -o main

driver.o:	driver.cpp
	clang++ -g -c driver.cpp

node.o:	node.cpp
	clang++ -g -c node.cpp

ID3Algorithm.o:	ID3Algorithm.cpp
	clang++ -g -c ID3Algorithm.cpp

decisiontreeclassifier.o:	decisiontreeclassifier.cpp
	clang++ -g -c decisiontreeclassifier.cpp

FileProcessor.o:	FileProcessor.cpp
	clang++ -g -c FileProcessor.cpp

helper.o:	helper.cpp
	clang++ -g -c helper.cpp

clean:
	rm *.o main
