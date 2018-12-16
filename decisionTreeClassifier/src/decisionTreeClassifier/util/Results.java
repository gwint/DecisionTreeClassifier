package util;
import java.util.List;
import java.util.ArrayList;
import java.util.Queue;
import java.util.ArrayDeque;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class Results implements FileDisplayInterface, StdoutDisplayInterface {
  private boolean fileNameProvided;
  private boolean shouldPrintNull;
  private BufferedWriter writer;
  private int id;
  private static int instanceCount = 0;

  public Results(boolean nullAllowed) {
    MyLogger.writeMessage("Now entering Results constructor",
                          MyLogger.DebugLevel.CONSTRUCTOR);

    this.fileNameProvided = true;
    this.shouldPrintNull = nullAllowed;
    this.writer = null;
    this.id = Results.instanceCount;
    Results.instanceCount++;
  }

  public Results(CharSequence fileName, boolean nullAllowed) {
    MyLogger.writeMessage("Now entering Results constructor",
                          MyLogger.DebugLevel.CONSTRUCTOR);

    if(fileName == null) {
      throw new IllegalArgumentException("Invalid file name");
    }
    this.fileNameProvided = true;
    FileWriter fWriter = null;
    try {
      fWriter = new FileWriter(fileName.toString());
      this.writer = new BufferedWriter(fWriter);
      this.shouldPrintNull = nullAllowed;
    }
    catch(IOException e) {
      MyLogger.writeError(String.format("%s does not exist\n", fileName));
      System.exit(1);
    }
  }

  private int getId() {
    return this.id;
  }

  /**
   Determines whether or not Results object created using a filename.
   @return true if a filename was provided at object creation, false otherwise.
   */
  private boolean getFileProvidedStatus() {
    return this.fileNameProvided;
  }

  /**
   Determines whether or not Results object should print null.
   @return true if it should print null, false otherwise.
   */
  private boolean getNullAllowedStatus() {
    return this.shouldPrintNull;
  }

  /**
   * Writes String representation of an object to a stdout.
   * @return No return value.
   */
  public void writeToStdout(Object o) {
    if((o == null && this.shouldPrintNull) ||
       o != null) {
      MyLogger.writeMessage(o.toString(), MyLogger.DebugLevel.NONE);
    }
  }

  /**
   * Writes String representation of an object to a file.
   * @return No return value.
   */
  public void writeToFile(Object o) {
    if(!this.fileNameProvided) {
      throw new UnsupportedOperationException("Cannot write to file if no filename provided");
    }
    try {
      if(o == null && this.shouldPrintNull) {
        this.writer.write("null\n");
      }
      else if(o != null){
        String toStrVal = o.toString();
        if(toStrVal.length() > 1) {
          this.writer.write(o.toString() + "\n");
        }
      }
      this.writer.flush();
    }
    catch(IOException e) {
      MyLogger.writeError("Error while writing to file");
      System.exit(1);
    }
  }

  @Override
  public int hashCode() {
    return this.getId();
  }

  @Override
  public String toString() {
    return String.format("Results object- Null Allowed: %s, Filename provided:",
                         Boolean.valueOf(this.getNullAllowedStatus()),
                         Boolean.valueOf(this.getFileProvidedStatus()));
  }

  /**
   * Placeholder implementation of equals(): it is not needed for this
   * assignment.
   * @return false as a placeholder value.
   */
  @Override
  public boolean equals(Object o) {
    return false;
  }

  @Override
  public void finalize() {
    try {
      this.writer.close();
    }
    catch(IOException e) {
      MyLogger.writeMessage("Unable to close writer",
                            MyLogger.DebugLevel.NONE);
      System.exit(1);
    }
  }
}
