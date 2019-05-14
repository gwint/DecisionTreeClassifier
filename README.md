<h1 align="center">
    Predicting Tumor Malignancy With a Decision Tree Classifier
    <br>
    <img src="https://www.researchgate.net/profile/Simone_Ludwig/publication/321707228/figure/fig2/AS:588683954647044@1517364600325/Decision-tree-obtained-from-FDT-classifier-for-the-Ovarian-cancer-data-set.png" alt="Raft logo" width="200">
</h1>

A decision tree created using the ID3 training algorithm.

Data Set: Wisconsin Breast Cancer Diagnostic Data 
Data Set URL: http://archive.ics.uci.edu/ml/machine-learning-databases/breast-cancer-wisconsin/wdbc.data 

## How to run:   

First compile using:   
ant -buildfile /api/DecisionTreeClassifier/decisionTreeClassifier/src/build.xml all

Then start server using:
ant -buildfile api/DecisionTreeClassifier/decisionTreeClassifier/src/build.xml runServer -Darg0=9000

*You'll probably need sudo*
