package oucomp.helper.io;
/***************************************************************
 *
 * Helper Utilities
 * Written by Dr. Andrew Kwok-Fai LUI
 * On 21/7/2010
 *
 * Copyright Andrew Kwok-Fai LUI 2010
 *
 */
import java.io.*;

public class LogHelper {

  private static String defaultFilename = "defaultlog.txt";

  private static PrintWriter writer = null;

  private static void setDefaultFilename() {
    try {
      if (writer != null) {
        writer.close();
      }
      writer = new PrintWriter(new FileOutputStream(defaultFilename, true));
    } catch (Exception ex) {

    }
  }

  public static void setFilename(String filename) {
    try {
      if (writer != null) {
        writer.close();
      }
      if (filename == null)
        setDefaultFilename();
      else
        writer = new PrintWriter(new FileOutputStream(filename, true));
    } catch (Exception ex) {
      setDefaultFilename();
    }
  }

  public static void close() {
    if (writer != null)
      writer.close();
    writer = null;
  }

  public static void print(String message, int level) {
    if (writer == null)  {
      setFilename(null);
      if (writer == null)
        return;
    }
    writer.print(message);
  }

  public static void println(String message, int level) {
    print(message, level);
    print("\n", level);
  }

  public static void println(String message) {
    println(message, 0);
  }
}