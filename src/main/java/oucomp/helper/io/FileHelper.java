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
import java.io.File;

public class FileHelper {

  public synchronized static File createFile(File folder, String filename) {
    int index = filename.lastIndexOf('.');
    String prefix = filename.substring(0, index);
    String suffix = filename.substring(index + 1, filename.length());
    int count = 0;
    while (true) {
      String newfilename;
      if (index <= 0)
        newfilename = filename + System.currentTimeMillis() + count;
      else
        newfilename = prefix + System.currentTimeMillis() + count + "." + suffix;
      File newfile = new File(folder, newfilename);
      if (!newfile.exists()) {
        return newfile;
      }
      count++;
    }
  }
  
  public static void main(String args[]) throws Exception {
    File folder = new File("d:/development/ecserver/data/scoreboard");
    File newfile = FileHelper.createFile(folder, "abc.xls");
    System.out.println(newfile.getAbsolutePath());
  }
}
