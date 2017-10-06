package oucomp.datasource;

import java.net.URL;
import java.util.Date;

public class HtmlDocument {

  public final static int STATUS_UNKNOWN = 0;
  public final static int STATUS_OK = 1;
  public final static int STATUS_UNKNOWNHOST = 2;
  public final static int STATUS_TIMEOUT = 3;
  public final static int STATUS_FILENOTFOUND = 4;
  public final static int STATUS_IOERROR = 5;
  public final static int STATUS_MALFORMEDURL = 6;
  private URL url = null;
  private StringBuilder content = new StringBuilder();
  private long contentLength = -1;
  private long lastModified;
  private String contentType;
  private String encodingType;
  private int status;

  public URL getUrl() {
    return url;
  }

  public void setUrl(URL url) {
    this.url = url;
  }

  public String getContent() {
    return content.toString();
  }

  public void addContent(String text) {
    this.content.append(text);
  }

  public long getContentLength() {
    return contentLength;
  }

  public void setContentLength(long contentLength) {
    this.contentLength = contentLength;
  }

  public long getLastModified() {
    return lastModified;
  }

  public void setLastModified(long lastModified) {
    this.lastModified = lastModified;
  }

  public String getContentType() {
    return contentType;
  }

  public void setContentType(String contentType) {
    this.contentType = contentType;
  }

  public String getEncodingType() {
    return encodingType;
  }

  public void setEncodingType(String encodingType) {
    this.encodingType = encodingType;
  }

  public int getStatus() {
    return status;
  }

  public void setStatus(int status) {
    this.status = status;
  }
  
  public String getStatusString() {
    switch (status) {
      case STATUS_UNKNOWN:
        return "UNKNOWN";
      case STATUS_OK:
        return "OK";
      case STATUS_UNKNOWNHOST:
        return "UNKNOWN HOST";
      case STATUS_TIMEOUT:
        return "TIMEOUT";
      case STATUS_FILENOTFOUND:
        return "FILE NOT FOUND";
      case STATUS_IOERROR:
        return "IO ERROR";
    }
    return "";
  }

  @Override
  public String toString() {
    StringBuilder buffer = new StringBuilder();
    buffer.append(url).append('\n');
    buffer.append("Status: ");
    buffer.append(getStatusString()).append('\n');
    buffer.append(contentType).append('\n');
    buffer.append(contentLength).append('\n');
    buffer.append(new Date(lastModified)).append('\n');
    buffer.append(content);
    return buffer.toString();
  }
  
}
