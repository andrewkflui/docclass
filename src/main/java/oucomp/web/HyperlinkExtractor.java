package oucomp.web;

import java.net.URL;
import org.htmlparser.beans.LinkBean;

public class HyperlinkExtractor {

  private String proxyhost = null;
  private int proxyport = -1;

  private boolean convertEscape = true;
  private String enc = "ISO-8859-1";
  
  public HyperlinkExtractor() {
    this(null, -1);
  }
  public HyperlinkExtractor(String proxyhost, int proxyport) {
    this.proxyhost = proxyhost;
    this.proxyport = proxyport;
  }
  
  public URL[] extractLinks(String urlstr) {
    LinkBean lb = new LinkBean();
    lb.setURL(urlstr);
    URL list[] = lb.getLinks();
    return list;
  }
  
    public static void main(String args[]) throws Exception {
    //String urlstr = "http://en.wikipedia.org/wiki/Electron";
    String urlstr = "http://en.wikipedia.org/wiki/Electron";
    HyperlinkExtractor extractor = new HyperlinkExtractor();
    URL list[] = extractor.extractLinks(urlstr);
    for (int i=0; i<list.length; i++)
      System.out.println(list[i]);
  }
}
