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
import java.util.Enumeration;
import java.util.zip.*;

public class ZipHelper {

  private static byte[] buffer = new byte[256];

  public static synchronized void dumpZipEntry(InputStream istream, String name, File folder)
    throws IOException {

    File outFile = new File(folder, name);
    BufferedOutputStream ostream = new BufferedOutputStream(new FileOutputStream(outFile));
    int count;
    while ((count = istream.read(buffer, 0, 256)) != -1) {
      ostream.write(buffer, 0, count);
    }
    ostream.close();
  }

  public static synchronized void dumpZipFile(ZipFile zipFile, File folder)
    throws IOException {

    Enumeration e = zipFile.entries();
    while (e.hasMoreElements()) {
      ZipEntry entry = (ZipEntry)e.nextElement();
      File zipName = new File(entry.getName());
      InputStream istream = zipFile.getInputStream(entry);
      dumpZipEntry(istream, zipName.getName(), folder);
      istream.close();
    }
  }
}