# DecisionTreeClassifier
A decision tree created using the ID3 training algorithm.

Data Set: Wisconsin Breast Cancer Diagnostic Data 
Data Set URL: http://archive.ics.uci.edu/ml/machine-learning-databases/breast-cancer-wisconsin/wdbc.data 

## How to run:   

First compile using:   
ant -buildfile /api/DecisionTreeClassifier/decisionTreeClassifier/src/build.xml all

Then start server using:
ant -buildfile api/DecisionTreeClassifier/decisionTreeClassifier/src/build.xml runServer -Darg0=9000

*You'll probably need sudo*
