package util;

import java.util.List;
import java.util.ArrayList;
import util.ProcessorI;
import java.util.Arrays;

public class NDArray {
    private Object[] data;
    private boolean containsData;

    public NDArray(int... dims) {
        if(dims == null) {
            throw new IllegalArgumentException("N-Dimensional dimensions cannot be gathered from null array");
        }
        if(dims.length == 0) {
            throw new IllegalArgumentException("N-Dimensional array must have at least one dimension");
        }
        if(dims.length == 1) {
            this.containsData = true;
            this.data = new Object[dims[0]];
            return;
        }

        this.data = new Object[dims[0]];
        for(int i = 0; i < dims[0]; i++) {
            this.data[i] = new NDArray(Arrays.copyOfRange(dims, 1, dims.length));
        }

        this.containsData = false;
    }

    /**
     *  Retrieve an element at the specified location.
     *  @param indices... int's representing location of element to retrieve.
     *  @return The item retrieved from the ndarray.
     */
    public Object get(int ... indices) {
        if(indices == null) {
            throw new IllegalArgumentException("Indices must be provided to retrieve element");
        }
        if(indices.length > 1 && this.containsData) {
            throw new IllegalArgumentException("Too many index values provided");
        }
        if(indices[0] >= this.data.length) {
            throw new IllegalArgumentException(String.format("Invalid index value for axis: %d", indices[0]));
        }

        Object currArr = this;
        for(int index : indices) {
            System.out.println("object: " + currArr);
            currArr = ((NDArray) currArr).data[index];
        }

        System.out.println("type: " + currArr);
        return currArr;
    }

    /**
     *  Insert a value into an ndarray at a specified location.
     *  @param value T The value to insert.
     *  @param indices int... The location at which to insert the element.
     *  @return None.
     */
    public void add(Object value, int ... indices) {
        if(indices == null) {
            throw new IllegalArgumentException("Indices must be provided to retrieve element");
        }
        if(indices.length > 1 && this.containsData) {
            throw new IllegalArgumentException("Too many index values provided");
        }
        if(indices[0] >= this.data.length) {
            throw new IllegalArgumentException(String.format("Invalid index value for axis: %d", indices[0]));
        }

        if(indices.length == 1) {
            this.data[indices[0]] = value;
            return;
        }

        ((NDArray) this.data[indices[0]]).add(value, Arrays.copyOfRange(indices, 1, indices.length));
    }

    /**
     *  Parses a csv file and creates an n-dimensional array from the
     *  contents.
     *  @param fileName String Name of file to be read in.
     *  @return An n-dimensional array containing contents of file.
     */
    public static NDArray readCSV(String fileName) {
        FileProcessor processor = new FileProcessor(fileName);

        int numColumns = processor.readNextLine().split(new String(",")).length;

        int numRows = 1;
        while(processor.readNextLine() != null) {
            numRows++;
        }

        NDArray arr = new NDArray(numRows, numColumns);

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
    public int length() {
        return this.data.length;
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
