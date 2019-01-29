package visitors;

import classifier.DecisionTreeClassifier;

public interface ClfVisitorI {
  public void visit(DecisionTreeClassifier clf);
}
