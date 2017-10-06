package oucomp.web;

import java.net.URL;
import java.util.Hashtable;
import org.apache.commons.lang3.StringEscapeUtils;
import org.htmlparser.Node;
import org.htmlparser.beans.LinkBean;
import org.htmlparser.http.ConnectionManager;
import org.htmlparser.lexer.Lexer;
import org.htmlparser.lexer.Page;
import org.htmlparser.nodes.TagNode;
import org.htmlparser.nodes.TextNode;
import org.htmlparser.util.ParserException;
 
public class HypertextExtractor {


  private String proxyhost = null;
  private int proxyport = -1;

  private boolean convertEscape = true;
  private String enc = "ISO-8859-1";
  
  public HypertextExtractor() {
    this(null, -1);
  }
  public HypertextExtractor(String proxyhost, int proxyport) {
    this.proxyhost = proxyhost;
    this.proxyport = proxyport;
  }

  public void setEncoding(String enc) {
    this.enc = enc;
  }

  public HypertextDocument extractHypertext(URL url) throws Exception {
    ConnectionManager connector;
    StringBuffer buffer = new StringBuffer();
    HypertextDocument document = new HypertextDocument(url);    
    try {
      connector = Page.getConnectionManager();
      if (proxyhost != null) {
        connector.setProxyHost(proxyhost);
        connector.setProxyPort(proxyport);
      }
      Lexer lexer = new Lexer(connector.openConnection(url));
      process(lexer, document);
    } catch (ParserException pe) {
      System.err.println(pe.getMessage());
      throw pe;
    }
    return document;
  }

  private void process(Lexer lexer, HypertextDocument document) throws Exception {
    if (lexer == null) {
      throw new Exception("[WebpageExtractor] Extractor not ready");
    }
    boolean intext = true;
    boolean inhyperlink = false;
    String hyperlinkText = null;
    String hyperlinkURL = null;

    document.isFrame = false;
    
    Node node;
    lexer.reset();
    lexer.getPage().setEncoding(enc);
    while ((node = lexer.nextNode(false)) != null) {
      if (TextNode.class.isAssignableFrom(node.getClass())) {
        if (!intext) {
          continue;
        }
        TextNode textnode = (TextNode) node;
        String text = node.getText();
        if (convertEscape) {
          text = StringEscapeUtils.unescapeHtml4(text);
        }
        document.buffer.append(text);
        if (inhyperlink) {
          hyperlinkText = text;
        }
      } else if (TagNode.class.isAssignableFrom(node.getClass())) {
        TagNode tagnode = (TagNode) node;
        String tag = tagnode.getTagName();
        // get rid of javascript embedded in the body text section
        if (tag.equalsIgnoreCase("script")) {
          if (tagnode.isEndTag()) {
            intext = true;
          } else {
            intext = false;
          }
        } else if (tag.equalsIgnoreCase("frameset")) {
          document.isFrame = true;
        } else if (tag.equalsIgnoreCase("a")) {
          if (tagnode.isEndTag()) {
            inhyperlink = false;
            if (hyperlinkURL != null && hyperlinkText != null) {
              document.hyperlinkTable.put(hyperlinkURL, hyperlinkText);
              hyperlinkURL = hyperlinkText = null;
            }
          } else {
            inhyperlink = true;
            hyperlinkURL = tagnode.getAttribute("href");
          }
        }
      }
    }
  }

  public static void main(String args[]) throws Exception {
    String urlstr =
            "http://www.ouhk.edu.hk/~sctwww/computing2/info/StaffMain.htm";
    urlstr = "http://www.accusoft.com/resourcecenter/tutorials/dip/VQ/";
    urlstr =
            "http://news.wenxuecity.com/messages/200806/news-big5-633190.html";
    urlstr = "http://zh.wikipedia.org/w/index.php?title=%E9%A6%99%E6%B8%AF&variant=zh-hant&printable=yes";
    urlstr = "http://en.wikipedia.org/wiki/Electron";
    urlstr = "file:///D:/document/fulltime/programmingcontest/uhuntdata/Chapter1.txt";
    HypertextExtractor extractor = new HypertextExtractor();
    extractor.setEncoding("utf-8");
    HypertextDocument document = extractor.extractHypertext(new URL(urlstr));
    System.out.println(document);
  }
}
