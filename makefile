all:	main

main:	driver.o node.o ID3Algorithm.o FileProcessor.o helper.o
	g++ driver.o node.o ID3Algorithm.o FileProcessor.o helper.o -o main -g

driver.o:	driver.cpp
	g++ -g -pedantic -Wall -c driver.cpp

node.o:	node.cpp
	g++ -g -pedantic -Wall -c node.cpp

ID3Algorithm.o:	ID3Algorithm.cpp
	g++ -g -pedantic -Wall -c ID3Algorithm.cpp

FileProcessor.o:	FileProcessor.cpp
	g++ -g -pedantic -Wall -c FileProcessor.cpp

helper.o:	helper.cpp
	g++ -g -pedantic -Wall -c helper.cpp

clean:
	rm *.o main
