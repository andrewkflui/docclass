package oucomp.web;

import java.util.ArrayList;

public class MarkupBlock {

  String tag = null;
  StringBuffer text = new StringBuffer();
  StringBuffer htmltext = new StringBuffer();
  ArrayList<Hyperlink> hyperlinkList = new ArrayList<Hyperlink>();

  MarkupBlock(String tag) {
     this.tag = tag; 
  }
  
  public String getText() {
    return text.toString();
  }

  public String getHtml() {
    return htmltext.toString();
  }
  
  public String getTag() {
    return tag;
  }

  public int getHyperlinkCount() {
    return hyperlinkList.size();
  }

  public String getHyperlinkText(int index) {
    return hyperlinkList.get(index).hyperlinkText;
  }

  public String getHyperlinkURL(int index) {
    return hyperlinkList.get(index).hyperlinkURL;
  }

  public String getHyperlinkHtml(int index) {
    return hyperlinkList.get(index).hyperlinkHtml;
  }
  
  public void addHyperlink(String hyperlinkText, String hyperlinkURL, String hyperlinkHtml) {
    hyperlinkList.add(new Hyperlink(hyperlinkText, hyperlinkURL, hyperlinkHtml));
  }

  class Hyperlink {

    String hyperlinkText;
    String hyperlinkURL;
    String hyperlinkHtml;

    Hyperlink(String hyperlinkText, String hyperlinkURL, String hyperlinkHtml) {
      this.hyperlinkText = hyperlinkText;
      this.hyperlinkURL = hyperlinkURL;
      this.hyperlinkHtml = hyperlinkHtml;
    }
  }
}
