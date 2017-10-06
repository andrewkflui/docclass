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

public class SerializableHelper {

  /**
   * Method for loading the review records from a serialized object file.
   */
  public static synchronized Object load(String file) {
    return load(new File(file));
  }

  public static synchronized Object load(File fileref) {
    try {
      if (!fileref.exists()) {
        fileref = new File(fileref.getCanonicalPath() + "~");
        if (!fileref.exists()) {
          return null;
        }
      }
      ObjectInputStream objectStream = new ObjectInputStream(
              new FileInputStream(fileref));
      Object obj = objectStream.readObject();
      objectStream.close();
      return obj;
    } catch (Exception ex) {
      System.err.println("[SerializedFileHelper] Error: " + ex);
    }
    return null;
  }

  /**
   * Private method for saving the review records to a serialized object file.
   */
  public static synchronized boolean save(String file, Object obj) {
    return save(new File(file), obj);
  }

  public static synchronized boolean save(File fileref, Object obj) {
    try {
      // create a backup file first
      if (fileref.exists()) {
        String canonicalPath = fileref.getCanonicalPath();
        File backup = new File(canonicalPath + "~");
        fileref.renameTo(backup);
        fileref = new File(canonicalPath);
      }
      ObjectOutputStream objectStream = new ObjectOutputStream(
              new FileOutputStream(fileref));
      objectStream.writeObject(obj);
      objectStream.close();
      return true;
    } catch (Exception ex) {
      System.err.println("[SerializedFileHelper] Error: " + ex);
    }
    return false;
  }

  public static synchronized boolean write(OutputStream ostream, Object obj) {
    try {
      ObjectOutputStream objectStream = new ObjectOutputStream(ostream);
      objectStream.writeObject(obj);
    } catch (Exception ex) {
      System.err.println("[SerializedFileHelper] Error: " + ex);
    }
    return false;
  }
}

