package util;

import java.util.List;
import java.util.ArrayList;
import util.ProcessorI;
import java.util.Arrays;

public class NDArray<T> {
    private List<Integer> dimensions;
    private Object[] data;

    public NDArray(int... dims) {
        if(dims == null) {
            throw new IllegalArgumentException("N-Dimensional dimensions cannot be gathered from null array");
        }
        if(dims.length == 0) {
            throw new IllegalArgumentException("N-Dimensional array must have at least one dimension");
        }
        this.dimensions = new ArrayList<>();

        int totalNumElements = 1;

        for(int dim : dims) {
            totalNumElements *= dim;
            this.dimensions.add(dim);
        }

        this.data = new Object[totalNumElements];
    }

    /**
     *  Retrieve an element at the specified location.
     *  @param indices... int's representing location of element to retrieve.
     *  @return The item retrieved from the ndarray.
     */
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
        int totalNumElements = this.data.length;
        for(int i = 0; i < indices.length; i++) {
            // a(x2*x3*x4*...*xn) + b(X3*x4*x5*...*xn) + ... + z
            totalNumElements /= this.dimensions.get(i);
            index1D += indices[i] * totalNumElements;
        }

        return (T) this.data[index1D];
    }

    /**
     *  Insert a value into an ndarray at a specified location.
     *  @param value T The value to insert.
     *  @param indices int... The location at which to insert the element.
     *  @return None.
     */
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
        int totalNumElements = this.data.length;
        for(int i = 0; i < indices.length; i++) {
            // a(x2*x3*x4*...*xn) + b(X3*x4*x5*...*xn) + ... + z
            totalNumElements /= this.dimensions.get(i);
            index1D += indices[i] * totalNumElements;
        }

        this.data[index1D] = value;
    }

    /**
     *  Parses a csv file and creates an n-dimensional array from the
     *  contents.
     *  @param fileName String Name of file to be read in.
     *  @return An n-dimensional array containing contents of file.
     */
    public static NDArray<Double> readCSV(String fileName) {
        FileProcessor processor = new FileProcessor(fileName);

        int numColumns = processor.readNextLine().split(new String(",")).length;

        int numRows = 1;
        while(processor.readNextLine() != null) {
            numRows++;
        }

        NDArray<Double> arr = new NDArray<Double>(numRows, numColumns);

        int currRow = 0;
        int currCol = 0;
        processor = new FileProcessor(fileName);
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

    /**
     *  Find the length of the ndarray along some axis.
     *  @param axis int An zero-indexed int representing the axis for which the
     *                  length is desired.
     *  @return The length of the ndarray along the axis in question.
     */
    public int length(int axis) {
        return this.dimensions.get(axis);
    }

    @Override
    public String toString() {
        return "Values at start + values at end stored in arrray";
    }

    @Override
    public boolean equals(Object o) {
        return Arrays.equals(((NDArray) o).data, this.data);
    }
}
