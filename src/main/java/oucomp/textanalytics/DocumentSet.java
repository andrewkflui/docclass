package oucomp.textanalytics;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.lucene.document.Document;

public class DocumentSet {
  public static final String DOC_CATEGORY = "category";
  public static final String DOC_CONTENT = "content";  
  
  protected List<Document> docList = new ArrayList();
  
  public Iterator<Document> iterator() {
    return docList.iterator();
  }
  
  public int size() {
    return docList.size();
  }
  
  public Document getDocument(int index) {
    if (index < 0 || index >= docList.size())
      return null;
    return docList.get(index);
  }
}
