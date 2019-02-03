package visitors;

import classifier.Node;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;

public class LabelVisitor implements VisitorI {
  private static double NO_LABEL_ASSIGNED = Double.NaN;
  private double label;

  public LabelVisitor() {
    this.label = LabelVisitor.NO_LABEL_ASSIGNED;
  }

  public double getLabel() {
    if(label == LabelVisitor.NO_LABEL_ASSIGNED) {
      throw new IllegalStateException("label getter should not be called before a Node is visited");
    }
    return this.label;
  }

  public void setLabel(double newLabel) {
    this.label = newLabel;
  }

  public void visit(Node aNode) {
    List<Integer> sampleIndices = aNode.getSampleIndices();
    double classLabel = LabelVisitor.NO_LABEL_ASSIGNED;
    if(sampleIndices.size() == 0) {
      this.visit(aNode.getParent());
      if(aNode.getChildren() == null) {
        aNode.setLabel(this.getLabel());
      }
    }
    else {
      int numSamples = sampleIndices.size();
      Map<Double, Integer> classCounts = new HashMap<>();
      for(Integer sampleIdx : sampleIndices) {
        if(sampleIdx == null) {
          throw new IllegalArgumentException("List of sample indices should not be null");
        }
        int i = sampleIdx.intValue();
        Double label = aNode.getDataset().getClassLabel(sampleIdx);
        if(label == null) {
          throw new IllegalArgumentException("Every class label should be non-null");
        }

        if(!classCounts.containsKey(label)) {
          classCounts.put(label, 1);
        }
        else {
          classCounts.put(label, classCounts.get(label).intValue() + 1);
        }

        Set<Double> allLabels = classCounts.keySet();
        Iterator<Double> labelIter = allLabels.iterator();
        Iterator<Double> initializer = allLabels.iterator();
        classLabel = initializer.next();

        while(labelIter.hasNext()) {
          Double aLabel = labelIter.next();
          if(classCounts.get(aLabel) > classCounts.get(classLabel)) {
            classLabel = aLabel.doubleValue();
          }
        }
      }
      this.setLabel(classLabel);
      if(aNode.getChildren() == null) {
        aNode.setLabel(this.getLabel());
      }
    }
  }
}
