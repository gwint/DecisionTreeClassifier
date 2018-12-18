package classifier;
import util.NDArray;
import util.Linkable;

public interface TrainingStrategy {
  public Linkable train(NDArray features, NDArray classes);
}
