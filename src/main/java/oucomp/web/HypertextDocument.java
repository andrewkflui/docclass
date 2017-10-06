package oucomp.web;

import java.net.URL;
import java.util.Enumeration;
import java.util.Hashtable;

public class HypertextDocument {
  URL url = null;
  StringBuffer buffer = new StringBuffer();
  boolean isFrame = false;
  Hashtable<String, String> hyperlinkTable = new Hashtable<String, String>();

  HypertextDocument(URL url) {
    this.url = url;
  }
  
  public String getText() {
    return buffer.toString();
  }

  public StringBuffer getText(StringBuffer inbuffer) {
    inbuffer.append(buffer);
    return inbuffer;
  }

  public Hashtable<String, String> getHyperlinks() {
    return hyperlinkTable;
  }

  public boolean containsFrame() {
    return isFrame;
  }
  
  public String toString() {
    StringBuffer sb = new StringBuffer();  
    sb.append("[Frame]: " + isFrame);
    sb.append("[Text]: " + buffer.toString());
    sb.append("[Hypertexts]: \n");
    Enumeration<String> e = hyperlinkTable.keys();
    while (e.hasMoreElements()) {
      String key = e.nextElement();
      sb.append(key + " " + hyperlinkTable.get(key) + "\n");
    }
    return sb.toString();
  }
}
