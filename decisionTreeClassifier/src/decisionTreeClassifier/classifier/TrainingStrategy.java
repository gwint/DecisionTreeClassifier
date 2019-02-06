package classifier;

import util.NDArray;
import java.util.List;
import util.Dataset;

public interface TrainingStrategy {
  public Node train(Dataset dataset, List<Integer> sampleIndices, int maxClassifierHeight);
}
