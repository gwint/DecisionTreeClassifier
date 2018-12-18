package util;

import java.util.List;
import java.util.ArrayList;
import util.ProcessorI;

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

    int totalNumElements = 1;

    for(int dim : dims) {
      totalNumElements *= dim;
      this.dimensions.add(dim);
    }

    for(int n = 0; n < totalNumElements; n++) {
      this.data.add(null);
    }
  }

  public T get(int ... indices) {
    if(indices == null) {
      throw new IllegalArgumentException("Indices must be provided to retrieve element");
    }
    if(indices.length != this.dimensions.size()) {
      throw new IllegalArgumentException(String.format("%d index values must be provided", this.dimensions.size()));
    }

    for(int i = 0; i < indices.length; i++) {
      if(indices[i] >= this.dimensions.get(i)) {
        throw new IllegalArgumentException(String.format("Invalid index value for axis %d: %d", i, indices[i]));
      }
    }

    int index1D = 0;
    int totalNumElements = this.data.size();
    for(int i = 0; i < indices.length; i++) {
      // a(x2*x3*x4*...*xn) + b(X3*x4*x5*...*xn) + ... + z
      totalNumElements /= this.dimensions.get(i);
      index1D += indices[i] * totalNumElements;
    }

    System.out.println(index1D);
    return this.data.get(index1D);
  }

  public void add(T value, int ... indices) {
    if(indices == null) {
      throw new IllegalArgumentException("Indices must be provided to retrieve element");
    }
    if(indices.length != this.dimensions.size()) {
      throw new IllegalArgumentException(String.format("%d index values must be provided", this.dimensions.size()));
    }

    for(int i = 0; i < indices.length; i++) {
      if(indices[i] >= this.dimensions.get(i)) {
        throw new IllegalArgumentException(String.format("Invalid index value for axis %d: %d", i, indices[i]));
      }
    }

    int index1D = 0;
    int totalNumElements = this.data.size();
    for(int i = 0; i < indices.length; i++) {
      // a(x2*x3*x4*...*xn) + b(X3*x4*x5*...*xn) + ... + z
      totalNumElements /= this.dimensions.get(i);
      index1D += indices[i] * totalNumElements;
    }

    this.data.add(index1D, value);
  }

  public static NDArray readCSV(FileProcessor processor) {
    FileProcessor processorCopy = (FileProcessor) processor.clone();

    int numColumns = processorCopy.readNextLine().split(new String(",")).length;

    int numRows = 1;
    while(processorCopy.readNextLine() != null) {
      numRows++;
    }

    NDArray arr = new NDArray<Double>(numRows, numColumns);

    int currRow = 0;
    int currCol = 0;
    String line = processor.readNextLine();
    while(line != null) {
      String[] pieces = line.split(new String(","));
      for(String data : pieces) {
        double value = Double.parseDouble(data);
        arr.add(value, currRow, currCol);
        currCol++;
        if(currCol > numColumns) {
          throw new IllegalStateException("Writing to column that doesn't exist in array");
        }
      }
      line = processor.readNextLine();
      currRow++;
      currCol = 0;
    }

    return arr;
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
