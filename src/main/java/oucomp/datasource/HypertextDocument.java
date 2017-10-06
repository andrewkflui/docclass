package oucomp.datasource;

import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;

public class HypertextDocument {
  private URL url = null;
  private StringBuilder buffer = new StringBuilder();
  private boolean containFrame = false;
  private HashMap<String, String> hyperlinkTable = new HashMap();

  public URL getUrl() {
    return url;
  }

  public void setUrl(URL url) {
    this.url = url;
  }

  public String getText() {
    return buffer.toString();
  }

  public StringBuffer getText(StringBuffer inbuffer) {
    inbuffer.append(buffer);
    return inbuffer;
  }
  
  public void addText(String text) {
    buffer.append(text);
  }

  public HashMap<String, String> getHyperlinks() {
    return hyperlinkTable;
  }
  
  public void addHyperlink(String url, String text) {
    hyperlinkTable.put(url, text);
  }

  public boolean isContainFrame() {
    return containFrame;
  }

  public void setContainFrame(boolean containFrame) {
    this.containFrame = containFrame;
  }
   

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("[Frame]: ").append(containFrame);
    sb.append("[Text]: ").append(buffer.toString());
    sb.append("[Hypertexts]: \n");
    Iterator<String> it = hyperlinkTable.keySet().iterator();
    while (it.hasNext()) {
      String key = it.next();
      sb.append(key).append(" ").append(hyperlinkTable.get(key)).append("\n");
    }
    return sb.toString();
  }
}
