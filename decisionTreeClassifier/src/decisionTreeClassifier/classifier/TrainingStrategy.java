package classifier;
import util.NDArray;
import util.Linkable;
import java.util.List;

public interface TrainingStrategy {
  public Linkable train(NDArray<Double> features, NDArray<Double> classes,
                        List<Integer> sampleIndices);
}
