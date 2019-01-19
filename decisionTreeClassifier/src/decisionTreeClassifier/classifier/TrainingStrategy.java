package classifier;
import util.NDArray;
import java.util.List;

public interface TrainingStrategy {
  public Node train(NDArray<Double> features, NDArray<Double> classes,
                        List<Integer> sampleIndices);
}
