/**
 * Library required: htmlparser.jar
 */
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


public class MarkupExtractor {

  private String proxyhost = null;
  private int proxyport = -1;

  private boolean convertEscape = true;
  private String enc = "ISO-8859-1";

  public MarkupExtractor() {
    this(null, -1);
  }

  public MarkupExtractor(String proxyhost, int proxyport) {
    this.proxyhost = proxyhost;
    this.proxyport = proxyport;
  }

  public void setEncoding(String enc) {
    this.enc = enc;
  }

  public MarkupDocument extractMarkup(String urlstr) throws Exception {
    return extractMarkup(new URL(urlstr));
  }

  public MarkupDocument extractMarkup(URL url) throws Exception {
    ConnectionManager connector;
    MarkupDocument doc = new MarkupDocument();
    try {
      connector = Page.getConnectionManager();
      if (proxyhost != null) {
        connector.setProxyHost(proxyhost);
        connector.setProxyPort(proxyport);
      }
      Lexer lexer = new Lexer(connector.openConnection(url));
      process(lexer, doc);
    } catch (ParserException ex) {
      System.err.println(ex.getMessage());
      throw ex;
    }
    return doc;
  }

  private void process(Lexer lexer, MarkupDocument document) throws Exception {
    boolean intext = true;
    String text = null;

    boolean inparatext = false;
    String paratext = null;
    boolean inheader = false;
    String headertext = null;
    
    MarkupBlock markupBlock = null;

    boolean inhyperlink = false;
    String hyperlinkText = null;
    String hyperlinkURL = null;
    String hyperlinkHtml = null;

    Node node;
    lexer.reset();
    lexer.getPage().setEncoding(enc);
    while ((node = lexer.nextNode(false)) != null) {
      if (TextNode.class.isAssignableFrom(node.getClass())) {
        if (!intext) {
          continue;
        }
        TextNode textnode = (TextNode) node;
        text = textnode.getText();
        if (convertEscape) {
           text = StringEscapeUtils.unescapeHtml4(text);
        }
        if (inheader) {
          headertext = text;
        } 
        if (inhyperlink) {
          hyperlinkText = text;
                    if (markupBlock != null)
          markupBlock.text.append(text);
        } else if (inparatext) {
          markupBlock.text.append(text);
          markupBlock.htmltext.append(textnode.toHtml());
        }
      } else if (TagNode.class.isAssignableFrom(node.getClass())) {
        TagNode tagnode = (TagNode) node;
        String tag = tagnode.getTagName().toLowerCase();
        if (!tagnode.isEndTag()) {
          //System.out.println("Tag Start: " + tag);
          //System.out.println(tagnode.toHtml());
          // a header tag 
          if (tag.startsWith("h") && tag.length() == 2) {
            markupBlock = new MarkupBlock(tag);
            inheader = true;
          } else if (tag.equalsIgnoreCase("p")) {
            markupBlock = new MarkupBlock(tag);
            markupBlock.htmltext.append("<p>");
            inparatext = true;
          } else if (tag.equalsIgnoreCase("a")) {
            inhyperlink = true;
            hyperlinkURL = tagnode.getAttribute("href");
            hyperlinkHtml = tagnode.toHtml();
          } else if (tag.equalsIgnoreCase("b") || tag.equalsIgnoreCase("i")) {
              if (inparatext) {
                markupBlock.htmltext.append("<" + tag + ">");
              }            
          }
        } else {
          //System.out.println("Tag End: " + tag); 
          //System.out.println(tagnode.toHtml());
          // a header tag
          if (tag.equals("title")) {
            document.setTitle(text);
          } else if (tag.startsWith("h") && tag.length() == 2) {
            headertext = headertext.replaceAll("\r\n", "").trim();
            markupBlock.text.append(headertext);
            document.addMarkupBlock(markupBlock);
            markupBlock = null;
            inheader = false;
          } else if (tag.equalsIgnoreCase("p")) {
            inparatext = false;
            markupBlock.htmltext.append("</p>");
            document.addMarkupBlock(markupBlock);
          } else if (tag.equalsIgnoreCase("a")) {
            inhyperlink = false;
            if (hyperlinkURL != null && hyperlinkText != null) {
              if (inparatext) {
                markupBlock.addHyperlink(hyperlinkText, hyperlinkURL, hyperlinkHtml);
                markupBlock.htmltext.append(hyperlinkHtml + hyperlinkText + "</a>");
              }
              hyperlinkURL = hyperlinkText = hyperlinkHtml = null;
            }
          } else if (tag.equalsIgnoreCase("b") || tag.equalsIgnoreCase("i")) {
              if (inparatext) {
                markupBlock.htmltext.append("</" + tag + ">");
              }            
          }
        }
        if (tag.equalsIgnoreCase("meta")) {
          document.addMetaTag(node.toHtml());
        }
      }
    }
  }

  public static void main(String args[]) throws Exception {
    //String urlstr = "file:///d:/course/MT260F-2008/Document/Web/Lecture1.htm";
    String urlstr = "file:///D:/document/fulltime/programmingcontest/uhuntdata/Chapter1.txt";
    MarkupExtractor extractor = new MarkupExtractor();
    extractor.setEncoding("utf8");    
    MarkupDocument doc = extractor.extractMarkup(urlstr);

    int count = doc.getMarkupBlockCount();
    for (int i=0; i<count; i++) {
      MarkupBlock block = doc.getMarkupBlock(i);
      System.out.println(block.getHtml());      
    }
  }
}
