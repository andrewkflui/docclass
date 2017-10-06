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

public class FolderHelper {

  public static boolean deleteRecursive(File folder) {
    if (folder == null)
      return false;
    boolean failed = false;
    File[] files = folder.listFiles();
    for (int i=0; i<files.length; i++) {
      if (files[i].isDirectory()) {
        if (!deleteRecursive(files[i]))
          failed = true;
      }
      if (!files[i].delete())
        failed = true;
    }
    if (!folder.delete())
      failed = true;
    return !failed;
  }
}