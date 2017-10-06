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

public class StreamHelper {

  static byte buffer[] = new byte[256];

  public static void dumpStream(InputStream istream) {
    synchronized (buffer) {
      while (true) {
        try {
          int count = istream.read(buffer, 0, 256);
          if (count == -1) {
            return;
          }
          System.out.write(buffer, 0, count);
          System.out.flush();
        } catch (IOException ex) {
          return;
        }
      } // while
    }
  }

}