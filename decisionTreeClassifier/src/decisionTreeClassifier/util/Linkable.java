package util;
import util.ProcessorI;

public interface Linkable {
  public void insert(Linkable nodeToInsert);
  public void delete(CharSequence key, CharSequence value);
  public void delete(ProcessorI aProcessorIn);
  public Linkable find(CharSequence key);
  public boolean addClass(CharSequence classNameIn,
                          NotificationState isNeeded);
  public boolean removeClass(CharSequence classNameIn,
                             NotificationState isNeeded);
  public boolean isDummy();
  public void printNodes(Results resObj);
}
