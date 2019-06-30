package util;

public class MyLogger{

    public static enum DebugLevel { NONE, SHOW_AVGS, SHOW_TRANSITIONS,
                                    SHOW_RANGES, CONSTRUCTOR };

    private static DebugLevel debugLevel;

    /**
     Sets debug value for MyLogger.
     @param levelIn New debug value
     @return No value.
     */
    public static void setDebugValue (int levelIn) {
	switch (levelIn) {
        case 4: debugLevel = DebugLevel.CONSTRUCTOR; break;
        case 3: debugLevel = DebugLevel.SHOW_RANGES; break;
	case 2: debugLevel = DebugLevel.SHOW_TRANSITIONS; break;
	case 1: debugLevel = DebugLevel.SHOW_AVGS; break;
	default: debugLevel = DebugLevel.NONE; break;
	}
    }

    /**
     Sets debug value for MyLogger.
     @return No value.
     */
    public static void setDebugValue (DebugLevel levelIn) {
	debugLevel = levelIn;
    }

    /**
     * Writes a message to stdout that is seen depending on the
     * current debug level of MyLogger.
     * @return No value.
     */
    public static void writeMessage(CharSequence message,
                                    DebugLevel levelIn ) {
	if (levelIn == debugLevel)
	    System.out.println(message);
    }

    /**
     * Writes a message to stderr that is seen depending on the
     * current debug level of MyLogger.
     * @return No value.
     */
    public static void writeError(CharSequence message) {
      if(message == null) {
        throw new IllegalArgumentException("Error message cannot be null");
      }
      System.err.println(message);
    }

    @Override
    public String toString() {
	return "The debug level has been set to the following " + debugLevel;
    }

    @Override
    public void finalize() {}

    /**
     Placeholder implementation of hashCode.
     @return 0 as placeholder value.
     */
    @Override
    public int hashCode() {
      return 0;
    }
}
