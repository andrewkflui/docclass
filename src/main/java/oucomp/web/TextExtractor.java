package oucomp.web;

import java.net.URL;
import org.apache.commons.lang3.StringEscapeUtils;
import org.htmlparser.Node;
import org.htmlparser.http.ConnectionManager;
import org.htmlparser.lexer.Lexer;
import org.htmlparser.lexer.Page;
import org.htmlparser.nodes.TagNode;
import org.htmlparser.nodes.TextNode;
import org.htmlparser.util.ParserException;
 
public class TextExtractor {
  private String proxyhost = null;
  private int proxyport = -1;

  private boolean convertEscape = true;
  private boolean removeLineBreak = true;
  private String enc = "UTF-8";

  public TextExtractor() {
    this(null, -1);
  }
  public TextExtractor(String proxyhost, int proxyport) {
    this.proxyhost = proxyhost;
    this.proxyport = proxyport;
  }

  public void setEncoding(String enc) {
    this.enc = enc;
  }
  
  public String extractText(URL url) throws Exception {
    StringBuffer content = extractText(url, null);
    return content.toString();
  }

  public StringBuffer extractText(URL url, StringBuffer buffer) throws Exception {
    ConnectionManager connector;
    if (buffer == null)
      buffer = new StringBuffer();
    try {
      connector = Page.getConnectionManager();
      if (proxyhost != null) {
        connector.setProxyHost(proxyhost);
        connector.setProxyPort(proxyport);
      }
      Lexer lexer = new Lexer(connector.openConnection(url));
      process(lexer, buffer);
    } catch (ParserException ex) {
      throw ex;
    }
    return buffer;
  }


  private void process(Lexer lexer, StringBuffer buffer) throws Exception {
    boolean intext = true;
    Node node;
    
    if (lexer == null) {
      throw new Exception("[URLTextExtractor] Extractor not ready");
    }

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
        if (removeLineBreak) {
          text = text.replaceAll("\r\n", "");
        }
        buffer.append(text);

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

        } else if (tag.equalsIgnoreCase("a")) {
          
        } else if (tag.equalsIgnoreCase("p")) {
          if (tagnode.isEndTag())
            buffer.append("\r\n");
        } else if (tag.startsWith("h") || tag.startsWith("H")) {
          if (tagnode.isEndTag())
            buffer.append("\r\n");
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
    urlstr = "file:///d:/course/MT260F-2008/Document/Web/Lecture1.htm";
        
    TextExtractor extractor = new TextExtractor();
    extractor.setEncoding("utf-8");
    String text = extractor.extractText(new URL(urlstr));
    System.out.println(text);
    /*
    DocumentBuilderText builder = new DocumentBuilderText();
    Section section = builder.buildSection("Lecture 1", text);
    //System.out.println(section);
    WordCounter wcounter = new WordCounter();
    section.serve(wcounter);
    System.out.println(wcounter);
     */
  }
}
