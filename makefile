all:	main

main:	driver.o node.o ID3Algorithm.o FileProcessor.o helper.o
	g++ driver.o node.o ID3Algorithm.o FileProcessor.o helper.o -o main -g

driver.o:	driver.cpp
	g++ -g -pedantic -Wall -c driver.cpp -O3 -I../../aws-lambda-cpp/include

node.o:	node.cpp
	g++ -g -pedantic -Wall -c node.cpp -O3

ID3Algorithm.o:	ID3Algorithm.cpp
	g++ -g -pedantic -Wall -c ID3Algorithm.cpp -O3

FileProcessor.o:	FileProcessor.cpp
	g++ -g -pedantic -Wall -c FileProcessor.cpp -O3

helper.o:	helper.cpp
	g++ -g -pedantic -Wall -c helper.cpp -O3

clean:
	rm *.o main
