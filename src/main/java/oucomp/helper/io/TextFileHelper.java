package oucomp.helper.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

public class TextFileHelper {

  public static String read(File textFile, String encoding) throws Exception {
    BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(textFile), encoding));
    StringBuilder builder = new StringBuilder();
    while (true) {
      String line = reader.readLine();
      if (line == null) {
        break;
      }
      builder.append(line);
    }
    reader.close();
    return builder.toString();
  }

  public static String read(File textFile, String encoding, boolean keepEOL) throws Exception {
    BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(textFile), encoding));
    StringBuilder builder = new StringBuilder();
    while (true) {
      String line = reader.readLine();
      if (line == null) {
        break;
      }
      builder.append(line);
      if (keepEOL)
        builder.append("\n");
    }
    reader.close();
    return builder.toString();
  }

  public static void write(File textFile, String encoding, String content) throws Exception {
    PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(textFile), encoding));
    writer.write(content);
    writer.close();
  }

  public static File[] listFiles(File folder, String suffix) throws Exception {
    return folder.listFiles(new TextFileFilter(suffix));
  }

  static class TextFileFilter implements FileFilter {
    private String suffix = ".txt";
    public TextFileFilter(String suffix) {
      this.suffix = suffix;
    }
    public boolean accept(File pathname) {
      if (!pathname.isFile())
        return false;
      if (!pathname.getName().endsWith(suffix))
        return false;
      return true;
    }
  }
}
