package util;

import java.util.List;
import java.util.ArrayList;

public class NDArray<T> {
  private List<Integer> dimensions;
  private List<T> data;

  public NDArray(int... dims) {
    if(dims == null) {
      throw new IllegalArgumentException("N-Dimensional dimensions cannot be gathered from null array");
    }
    if(dims.length == 0) {
      throw new IllegalArgumentException("N-Dimensional array must have at least one dimension");
    }
    this.dimensions = new ArrayList<>();
    this.data = new ArrayList<>();

    for(int dim : dims) {
      this.dimensions.add(dim);
    }

    System.out.println(this.dimensions);
  }

  public T get(int ... indices) {
    if(indices == null) {
      throw new IllegalArgumentException("Indices must be provided to retrieve element");
    }
    if(indices.length != this.dimensions.size()) {
      throw new IllegalArgumentException(String.format("%d index values must be provided", this.dimensions.size()));
    }
    return null;
  }

  public void add(T value, int ... indices) {
  }

  public boolean isEmpty() {
    return this.data.size() == 0;
  }

  public int length(int axis) {
    return 0;
  }

  @Override
  public String toString() {
    return "Values at start + values at end stored in arrray";
  }

  @Override
  public int hashCode() {
    return 0;
  }

  @Override
  public void finalize() {}

  @Override
  public boolean equals(Object o) {
    return false;
  }
}
