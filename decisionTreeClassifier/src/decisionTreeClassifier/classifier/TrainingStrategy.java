package classifier;
import util.NDArray;
import util.Linkable;
import java.util.List;

public interface TrainingStrategy {
  public Linkable train(NDArray features, NDArray classes,
                        List<Integer> sampleIndices);
}
