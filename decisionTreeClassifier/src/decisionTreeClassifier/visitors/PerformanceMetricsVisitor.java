package visitors;

import classifier.Node;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;

public class PerformanceMetricsVisitor implements VisitorI {
  private Double accuracy;

  public PerformanceMetricsVisitor() {
    this.accuracy = null;
  }

  public double getAccuracy() {
    if(this.accuracy == null) {
      throw new IllegalArgumentException("Accuracy must be calculated before its value can be queried");
    }
    return this.accuracy.doubleValue();
  }

  private void setAccuracy(Double accuracyIn) {
    if(accuracyIn == null) {
      throw new IllegalArgumentException("Accuracy must not be null");
    }
    this.accuracy = accuracyIn;
  }

  public void visit(Node aNode) {
  }
}
