package oucomp.datasource;

import java.net.URL;
import org.htmlparser.Node;
import org.htmlparser.http.ConnectionManager;
import org.htmlparser.lexer.Lexer;
import org.htmlparser.lexer.Page;
import org.htmlparser.nodes.TagNode;
import org.htmlparser.nodes.TextNode;
import org.htmlparser.util.ParserException;

public class WebDataSource extends GenericDataSource {

  public HypertextDocument readHypertext(String urlstr) throws Exception {
    return readHypertext(urlstr, null);
  }

  public HypertextDocument readHypertext(String urlstr, String encoding) throws Exception {
    URL url = createURL(urlstr);
    ConnectionManager connector;
    HypertextDocument document = new HypertextDocument();
    document.setUrl(url);
    try {
      connector = Page.getConnectionManager();
      if (proxyhost != null) {
        connector.setProxyHost(proxyhost);
        connector.setProxyPort(proxyport);
      }
      Lexer lexer = new Lexer(connector.openConnection(url));
      boolean intext = true;
      boolean inhyperlink = false;
      String hyperlinkText = null;
      String hyperlinkURL = null;
      boolean isFrame = false;
      Node node;
      lexer.reset();
      if (encoding != null) {
        lexer.getPage().setEncoding(encoding);
      }
      while ((node = lexer.nextNode(false)) != null) {
        if (TextNode.class.isAssignableFrom(node.getClass())) {
          if (!intext) {
            continue;
          }
          TextNode textnode = (TextNode) node;
          String text = textnode.getText();
          document.addText(text);
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
            isFrame = true;
          } else if (tag.equalsIgnoreCase("a")) {
            if (tagnode.isEndTag()) {
              inhyperlink = false;
              if (hyperlinkURL != null && hyperlinkText != null) {
                document.addHyperlink(hyperlinkURL, hyperlinkText);
                hyperlinkURL = hyperlinkText = null;
              }
            } else {
              inhyperlink = true;
              hyperlinkURL = tagnode.getAttribute("href");
            }
          }
        }
      }
      document.setContainFrame(isFrame);

    } catch (ParserException pe) {
      System.err.println(pe.getMessage());
      throw pe;
    }
    return document;
  }

  public MarkupDocument readMarkupHypertext(String urlstr) throws Exception {
    return readMarkupHypertext(urlstr, null);
  }

  public MarkupDocument readMarkupHypertext(String urlstr, String encoding) throws Exception {
    URL url = createURL(urlstr);
    ConnectionManager connector;

    MarkupDocument document = new MarkupDocument();
    //document.setUrl(url);
    try {
      connector = Page.getConnectionManager();
      if (proxyhost != null) {
        connector.setProxyHost(proxyhost);
        connector.setProxyPort(proxyport);
      }
      Lexer lexer = new Lexer(connector.openConnection(url));
      boolean intext = true;
      String text = null;
      boolean inparatext = false;
      boolean inheader = false;
      String headertext = "";

      MarkupBlock markupBlock = null;
      boolean inhyperlink = false;
      String hyperlinkText = null;

      Node node;
      lexer.reset();
      if (encoding != null) {
        lexer.getPage().setEncoding(encoding);
      }
      while ((node = lexer.nextNode(false)) != null) {
        if (TextNode.class.isAssignableFrom(node.getClass())) {
          if (!intext) {
            continue;
          }
          TextNode textnode = (TextNode) node;
          text = textnode.getText();
          if (inheader) {
            headertext = text;
          }
          if (inhyperlink) {
            hyperlinkText = text;
            if (markupBlock != null) {
              markupBlock.textBuffer.append(text);
            }
          } else if (inparatext) {
            markupBlock.addText(text);
            markupBlock.addHtmlText(textnode.toHtml());
          }
        } else if (TagNode.class.isAssignableFrom(node.getClass())) {
          String hyperlinkURL = "";
          String hyperlinkHtml = "";
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
              markupBlock.htmltextBuffer.append("<p>");
              inparatext = true;
            } else if (tag.equalsIgnoreCase("a")) {
              markupBlock = new MarkupBlock(tag);
              inhyperlink = true;
              hyperlinkURL = tagnode.getAttribute("href");
              hyperlinkHtml = tagnode.toHtml();
            } else if (tag.equalsIgnoreCase("b") || tag.equalsIgnoreCase("i") 
                    || tag.equalsIgnoreCase("u") || tag.equalsIgnoreCase("em")) {
              if (inparatext) {
                markupBlock.addHtmlText("<" + tag + ">");
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
              markupBlock.addText(headertext);
              document.addMarkupBlock(markupBlock);
              markupBlock = null;
              inheader = false;
            } else if (tag.equalsIgnoreCase("p")) {
              inparatext = false;
              markupBlock.addHtmlText("</p>");
              document.addMarkupBlock(markupBlock);
            } else if (tag.equalsIgnoreCase("a")) {
              inhyperlink = false;
              if (hyperlinkURL != null && hyperlinkText != null) {
                if (inparatext) {
                  markupBlock.addHyperlink(hyperlinkText, hyperlinkURL, hyperlinkHtml);
                  markupBlock.addHtmlText(hyperlinkHtml + hyperlinkText + "</a>");
                }
                hyperlinkURL = hyperlinkText = hyperlinkHtml = null;
              }
            } else if (tag.equalsIgnoreCase("b") || tag.equalsIgnoreCase("i")) {
              if (inparatext) {
                markupBlock.addHtmlText("</" + tag + ">");
              }
            }
          }
          if (tag.equalsIgnoreCase("meta")) {
            document.addMetaTag(node.toHtml());
          }
        }
      }
    } catch (ParserException pe) {
      System.err.println(pe.getMessage());
      throw pe;
    }
    return document;
  }

  public static void testHypertextReader() throws Exception {
    WebDataSource dataSource = new WebDataSource();
    String urlstr = "http://www.rthk.org.hk/";
    System.out.println(dataSource.readHypertext(urlstr, "utf8"));
  }

  public static void testMarkupHypertextReader() throws Exception {
    WebDataSource dataSource = new WebDataSource();
    String urlstr = "http://rthk.hk/text/chi/news/rss.htm";
    System.out.println(dataSource.readMarkupHypertext(urlstr, "big5"));
  }

  public static void main(String args[]) throws Exception {
    testHypertextReader();
    //testMarkupHypertextReader();
  }
}
