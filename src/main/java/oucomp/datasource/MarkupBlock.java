package oucomp.datasource;

import java.util.ArrayList;
import java.util.List;

public class MarkupBlock {

  String tag = null;
  StringBuilder textBuffer = new StringBuilder();
  StringBuilder htmltextBuffer = new StringBuilder();
  List<Hyperlink> hyperlinkList = new ArrayList();

  MarkupBlock(String tag) {
    this.tag = tag;
  }

  public String getText() {
    return textBuffer.toString();
  }

  public String getHtmlText() {
    return htmltextBuffer.toString();
  }

  public void addText(String text) {
    textBuffer.append(text);
  }

  public void addHtmlText(String html) {
    htmltextBuffer.append(html);
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

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("\t[Tag] ").append(tag).append('\n');
    sb.append("\t[Text] ").append(textBuffer).append('\n');
    sb.append("\t[Html] ").append(htmltextBuffer).append('\n');
    sb.append("\t[Hyperlink] ");
    for (Hyperlink link : hyperlinkList) {
      sb.append("\t\tText: ").append(link.hyperlinkText).append('\n');
      sb.append("\t\tURL: ").append(link.hyperlinkURL).append('\n');
      sb.append("\t\tHtml: ").append(link.hyperlinkHtml).append('\n');
    }
    return sb.toString();
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
