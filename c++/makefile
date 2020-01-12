all:	main

main:	driver.o decisiontreeclassifier.o node.o ID3Algorithm.o
	clang++ driver.o decisiontreeclassifier.o node.o ID3Algorithm.o -o main

driver.o:	driver.cpp
	clang++ -c driver.cpp

node.o:	node.cpp
	clang++ -c node.cpp

ID3Algorithm.o:	ID3Algorithm.cpp
	clang++ -c ID3Algorithm.cpp

decisiontreeclassifier.o:	decisiontreeclassifier.cpp
	clang++ -c decisiontreeclassifier.cpp

clean:
	rm *.o main
