package oucomp.textanalytics;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import org.apache.commons.io.FileUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;

public class DocumentSetFolderMerged extends DocumentSet {

  public void addFolder(String category, File folder) throws IOException {
    addFolder(category, folder, null);
  }

  public void addFolder(String category, File folder, FileFilter filter)
          throws IOException {
    if (!folder.isDirectory()) {
      throw new IOException("[DocumentSetFolder] Parameter folder is not a folder");
    }
    File fileArray[] = null;
    if (filter != null) {
      fileArray = folder.listFiles(filter);
    } else {
      fileArray = folder.listFiles();
    }
    Document doc = new Document();
    doc.add(new StringField(DOC_CATEGORY, category, Field.Store.YES));    
    for (File theFile : fileArray) {
      try {
        String content = FileUtils.readFileToString(theFile, "UTF-8");
        //System.out.println(content);
        doc.add(new TextField(DOC_CONTENT, content, Field.Store.NO));
      } catch (Exception ex) {
        System.err.println(ex);
      }
    }
    docList.add(doc);
  }
}
