package oucomp.datasource;

import java.util.ArrayList;

public class MarkupDocument {

  private String title = null;
  private ArrayList<String> metatagList = new ArrayList();
  private ArrayList<MarkupBlock> blockList = new ArrayList();

  public MarkupDocument() {
  }

  public MarkupDocument(String title) {
    this.title = title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getTitle() {
    return title;
  }

  public void addMetaTag(String html) {
    metatagList.add(html);
  }

  public ArrayList<String> getMetatagList() {
    return metatagList;
  }

  public void addMarkupBlock(MarkupBlock block) {
    blockList.add(block);
  }

  public int getMarkupBlockCount() {
    return blockList.size();
  }

  public MarkupBlock getMarkupBlock(int index) {
    return blockList.get(index);
  }

  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("[Title] ").append(title).append('\n');
    for (String meta : metatagList) {
      sb.append("[Meta] ").append(meta).append('\n');
    }
    for (MarkupBlock block : blockList) {
      sb.append("[MarkupBlock] ").append(block).append('\n');
    }
    return sb.toString();
  }
}
