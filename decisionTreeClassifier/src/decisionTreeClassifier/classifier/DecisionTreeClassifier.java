package classifier;
import util.NDArray;
import util.Linkable;

public class DecisionTreeClassifier {
  private NDArray features;
  private NDArray classes;
  private Linkable trainedClassifier;

  public DecisionTreeClassifier(NDArray trainingFeaturesIn,
                                NDArray trainingClassesIn) {
    if(trainingFeaturesIn == null) {
      throw new IllegalArgumentException("Array containing training features must not be null");
    }
    if(trainingClassesIn == null) {
      throw new IllegalArgumentException("Array containing training classes must not be null");
    }
    this.features = trainingFeaturesIn;
    this.classes = trainingClassesIn;
    this.trainedClassifier = null;
  }

  public void train(TrainingStrategy strat) {
    if(strat == null) {
      throw new IllegalArgumentException("Training strategy must not be null");
    }
    this.trainedClassifier = strat.train(this.features, this.classes);
  }

  public int predict() {
    return 0;
  }
}
