package util;
import myTree.Node;
import myTree.ElementInfo;
import util.Linkable;
import java.util.Scanner;
import java.util.List;
import java.util.ArrayList;
import myTree.SubjectI;
import util.NotificationState;

public class TreeBuilder implements Builder {
  private ProcessorI treeInfoProcessor;
  private List<Linkable> backupHeads;
  private int numBackups;
  private int nextBackupIdx;

  public TreeBuilder(ProcessorI treeInfoProcessorIn, int numBackupsIn) {
    if(treeInfoProcessorIn == null) {
      throw new IllegalArgumentException("Processor cannot be null.");
    }
    if(numBackupsIn < 0) {
      throw new IllegalArgumentException("Number of backups must be non-negative.");
    }
    this.numBackups = numBackupsIn;
    this.treeInfoProcessor = treeInfoProcessorIn;
    this.backupHeads = new ArrayList<>();
    this.nextBackupIdx = 0;
  }

  /**
   Creates a tree using information provided by the information file
   processor used to create this builder.
   @return A Linkable object representing the root of the newly created tree.
   */
  public Linkable build() {
    Linkable root = Node.getDummyNode();
    CharSequence elementInfo = this.getProcessor().readNext();
    while(elementInfo != null) {
      ElementInfo insertionInfo = new ElementInfo(elementInfo);
      CharSequence bNumber = insertionInfo.getBNumber();
      CharSequence className = insertionInfo.getClassName();

      if(root.isDummy()) {
        root = new Node(bNumber);
        this.backupHeads =
          this.createBackupHeads(root, this.getNumBackups());
      }

      Linkable nodeContainingBNum = root.find(bNumber);
      if(nodeContainingBNum != null) {
        ((Node) nodeContainingBNum).addClass(className,
                                             NotificationState.NEEDED);
      }
      else {
        Linkable newNode = new Node(bNumber);
        newNode.addClass(className, NotificationState.NOT_NEEDED);
        root.insert(newNode);
        this.insertClonesIntoBackups(newNode);
      }
      elementInfo = this.getProcessor().readNext();
    }
    return root;
  }

  @Override
  public String toString() {
    return "TreeBuilder object";
  }

  @Override
  public void finalize() {}

  @Override
  public boolean equals(Object aTreeIn) {
    return false;
  }

  /**
   Placeholder implementation of hashCode.
   @return 0 in all cases.
   */
  @Override
  public int hashCode() {
    return 0;
  }

  public ProcessorI getProcessor() {
    return this.treeInfoProcessor;
  }

  public int getNumBackups() {
    return this.numBackups;
  }

  private void addBackupHead(Linkable aNode) {
    if(aNode == null) {
      throw new IllegalArgumentException("Head of backup cannot be null");
    }
    this.backupHeads.add(aNode);
  }

  /**
   Inserts a node into all data structures whose root is contained
   in the list of backups.
   @return No value.
   */
  private void insertClonesIntoBackups(Linkable nodeToClone) {
    if(nodeToClone == null) {
      throw new IllegalArgumentException("Cannot clone null node.");
    }

    for(Linkable backupRoot : this.getBackupHeads()) {
      backupRoot.insert(this.createClone(nodeToClone));
    }
  }

  /**
   Creates a clone of the Linkable provided and registers that clone as an
   observer of the original node.
   @return A Linkable object that is the clone of the original Linkable object.
   */
  private Linkable createClone(Linkable originalNode) {
    if(originalNode == null) {
      throw new IllegalArgumentException("Cannot create a clone of a null object.");
    }
    if(!(originalNode instanceof Cloneable)) {
      throw new IllegalArgumentException("Cannot clone item that does not implement cloneable");
    }

    Linkable backupNode = null;
    try {
      backupNode = (Node) ((Node) originalNode).clone();
    }
    catch(CloneNotSupportedException e) {
      System.err.println("Unable to clone original node");
      System.exit(1);
    }
    finally {}
    ((SubjectI) originalNode).registerObserver(backupNode);
    return backupNode;
  }

  /**
   Creates a list of Linkable objects representing the roots of the backups
   of the data structure that will be created via the build() method.
   @return A list of Linkable objects representing the roots of the backups
   of the data structure that will be created via the build() method.
   */
  private List<Linkable> createBackupHeads(Linkable root, int numBackups) {
    if(root == null) {
      throw new IllegalArgumentException("Cannot create backups of a null root.");
    }
    if(numBackups < 0) {
      throw new IllegalArgumentException("Desired number of backups must be non-negative.");
    }
    List<Linkable> backupHeads = new ArrayList<>();
    for(int count = 0; count < numBackups; count++) {
      Linkable backupNode = null;
      try {
        backupNode = (Node) ((Node) root).clone();
      }
      catch(CloneNotSupportedException e) {
        System.err.println("Unable to clone original node");
        System.exit(1);
      }
      finally {}
      ((SubjectI) root).registerObserver(backupNode);
      backupHeads.add(backupNode);
    }
    return backupHeads;
  }

  public List<Linkable> getBackupHeads() {
    return this.backupHeads;
  }

  /**
   Retrieves the head of one of the backup trees.
   @Return The head of one of the backup trees, is guranteed to be unique
   with each call, or null if no more backups remain.
   */
  public Node getNextBackup() {
    Node nextBackup = Node.getDummyNode();
    if(this.nextBackupIdx < this.getNumBackups() &&
       this.getBackupHeads().size() > 0) {
      nextBackup = (Node) this.getBackupHeads().get(this.nextBackupIdx);
      this.nextBackupIdx++;
    }
    return nextBackup;
  }
}
