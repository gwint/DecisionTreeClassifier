package classifier;

import util.NDArray;
import util.Linkable;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;

public class DecisionTreeClassifier {
  private NDArray features;
  private NDArray classes;
  private List<Integer> trainingSamples;
  private List<Integer> testingSamples;
  private Linkable trainedClassifier;

  public DecisionTreeClassifier(NDArray<Double> allFeaturesIn,
                                NDArray<Double> allClassesIn) {
    if(allFeaturesIn == null) {
      throw new IllegalArgumentException("Array containing training features must not be null");
    }
    if(allClassesIn == null) {
      throw new IllegalArgumentException("Array containing training classes must not be null");
    }

    this.features = allFeaturesIn;
    this.classes = allClassesIn;
    this.trainedClassifier = null;
    this.trainingSamples = new ArrayList<>();
    this.testingSamples = new ArrayList<>();

    if(this.features.length(0) != this.classes.length(0)) {
      throw new IllegalArgumentException("Mismatch between number of samples and the number of classes");
    }
  }

  public void train(TrainingStrategy strat, double proportion) {
    if(strat == null) {
      throw new IllegalArgumentException("Training strategy must not be null");
    }
    this.splitData(proportion);
    this.trainedClassifier = strat.train(this.features, this.classes,
                                         this.trainingSamples);
  }

  public void predict(NDArray features) {
  }

  public void predict() {
  }

  private void splitData(double trainingProportion) {
    if(trainingProportion < 0) {
      throw new IllegalArgumentException("Proportion of data to be used for trainging must be non-negative");
    }

    int numSamples = this.features.length(0);
    int numTrainingSamples = (int) Math.round(trainingProportion * numSamples);

    Random randNumGen = new Random(0);
    while(this.trainingSamples.size() < numTrainingSamples) {
      int randInt = Math.abs(randNumGen.nextInt()) % numSamples;
      if(!this.trainingSamples.contains(randInt)) {
        this.trainingSamples.add(randInt);
      }
    }

    for(int sampleIdx = 0; sampleIdx < numSamples; sampleIdx++) {
      if(!this.trainingSamples.contains(sampleIdx)) {
        this.testingSamples.add(sampleIdx);
      }
    }
  }

  @Override
  public String toString() {
    return "";
  }
}
