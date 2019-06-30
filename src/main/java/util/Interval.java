package util;

public class Interval {
  private double start;
  private double end;

  public Interval(double startIn, double endIn) {
    this.start = startIn;
    this.end = endIn;
  }

  public double getStart() {
    return this.start;
  }

  public double getEnd() {
    return this.end;
  }

  @Override
  public String toString() {
    return String.format("Start: %f, End: %f",
                         this.getStart(), this.getEnd());
  }
}
