package oucomp.datasource;

import java.util.Date;

public class RSSDocument {

  public String title = null;
  public String content = null;
  public String source = null;
  public String mimetype = null;
  public long publishDate = 0;
  public long ingestDate = 0;
  public String uri = null;
  public String link = null;

  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("[Title] ").append(title).append("\n");
    sb.append("[Content] ").append(content).append("\n");
    sb.append("[Source] ").append(source).append("\n");
    sb.append("[Mimetype] ").append(mimetype).append("\n");
    sb.append("[Published] ").append(new Date(publishDate)).append("\n");
    sb.append("[Uri] ").append(uri).append("\n");
    sb.append("[Link] ").append(link).append("\n");
    return sb.toString();
  }
}
