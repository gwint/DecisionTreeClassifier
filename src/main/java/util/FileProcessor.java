package util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class FileProcessor implements ProcessorI, Cloneable {
  private BufferedReader reader;
  private CharSequence fileName;
  private int id;
  private static int instanceCount = 0;

  public FileProcessor(CharSequence fileNameIn) {
      MyLogger.writeMessage("Now entering FileProcessor constructor",
                            MyLogger.DebugLevel.CONSTRUCTOR);

      if(fileNameIn == null) {
          throw new IllegalArgumentException("Cannot create file processor using null file name.");
      }

      this.fileName = fileNameIn;

      try {
          InputStream inputStream = FileProcessor.class.getResourceAsStream(fileNameIn.toString());
          this.reader = new BufferedReader(new InputStreamReader(inputStream));
      }
      catch(Exception e) {
          e.printStackTrace();
          System.exit(1);
      }
      finally {}

      this.id = FileProcessor.instanceCount;
      FileProcessor.instanceCount++;
  }

  @Override
  protected Object clone() {
    return new FileProcessor(this.getFileName());
  }

  private int getId() {
    return this.id;
  }

  public CharSequence getFileName() {
    return this.fileName;
  }

  /**
   * Grabs the next line from the file being read and returns it as a
   * String.
   * @return The next line from the file being read, or null if the
   * end of the file has been reached.
   */
  public String readNextLine() {
    String nextLine = null;
    try {
      nextLine = this.reader.readLine();
    }
    catch(IOException e) {
      MyLogger.writeError(
           "Error occured while reading from input file.\nNow exiting...\n");
      MyLogger.writeError(e.toString());
      System.exit(1);
    }
    finally {}

    return nextLine;
  }

  @Override
  public void finalize() {
    try {
      this.reader.close();
    }
    catch(IOException e) {
      MyLogger.writeError("Unable to close reader");
      System.exit(1);
    }
  }

  @Override
  public int hashCode() {
    return this.getId();
  }

  @Override
  public String toString() {
    return String.format("File Processor object using file with name %s",
                         this.fileName.toString());
  }

  /**
   Determines if two FileProcessor objects are equal.
   @return true if the same filename is associated with each, false otherwise.
   */
  @Override
  public boolean equals(Object aFileProcessorIn) {
    if(aFileProcessorIn == null) {
      throw new IllegalArgumentException("Cannot compare against null file processor");
    }
    if(!(aFileProcessorIn instanceof FileProcessor)) {
      throw new ClassCastException("Cannot cast object to FileProcessor in order to carry out comparison");
    }
    FileProcessor otherFileProcessor = (FileProcessor) aFileProcessorIn;
    return this.getFileName().toString().equals(otherFileProcessor.toString());
  }
}
