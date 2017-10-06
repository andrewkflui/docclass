package oucomp.web;

import java.util.ArrayList;

public class MarkupDocument {

  private String title = null;
  private ArrayList<String> metatagList = new ArrayList<String>();
  private ArrayList<MarkupBlock> blockList = new ArrayList<MarkupBlock>();
  
  public MarkupDocument() {
  }
  
  public MarkupDocument(String title) {
    this.title = title;
  }
  
  public void setTitle(String title) {
    this.title = title;
  }
  
  public void addMetaTag(String html) {
    metatagList.add(html);
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
  
}
