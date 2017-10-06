package oucomp.datasource;

import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import org.htmlparser.Node;
import org.htmlparser.http.ConnectionManager;
import org.htmlparser.lexer.Lexer;
import org.htmlparser.lexer.Page;
import org.htmlparser.nodes.TagNode;
import org.htmlparser.nodes.TextNode;
import org.htmlparser.util.ParserException;

public class GenericDataSource {

  protected String proxyhost = null;
  protected int proxyport;

  public GenericDataSource() {
    this(null, -1);
  }

  public GenericDataSource(String proxyhost, int proxyport) {
    this.proxyhost = proxyhost;
    this.proxyport = proxyport;
  }

  protected URL createURL(String urlstr) {
    try {
      if (proxyhost == null || proxyport < 0) {
        return new URL(urlstr);
      } else {
        return new URL("http", proxyhost, proxyport, urlstr);
      }
    } catch (MalformedURLException ex) {
      ex.printStackTrace(System.err);
      return null;
    }
  }

  public List<RSSDocument> readRSS(String feedurl) throws IOException, FeedException {
    URL url = createURL(feedurl);
    SyndFeedInput input = new SyndFeedInput();
    SyndFeed feed = input.build(new XmlReader(url));
    ArrayList<RSSDocument> results = new ArrayList();
    List entries = feed.getEntries();
    for (int i = 0; i < entries.size(); i++) {
      SyndEntry entry = (SyndEntry) entries.get(i);
      RSSDocument item = new RSSDocument();
      String mimetype = entry.getDescription().getType();
      item.title = entry.getTitle();
      //item.title = new String(item.title.getBytes(encoding), dbencoding);
      item.publishDate = entry.getPublishedDate().getTime();
      item.uri = entry.getUri();
      item.link = entry.getLink();
      item.content = entry.getDescription().getValue();
      //item.content = new String(item.content.getBytes(encoding), dbencoding);
      item.ingestDate = System.currentTimeMillis();
      item.mimetype = mimetype;
      results.add(item);
    }
    return results;
  }

  public String readRaw(String urlstr) throws Exception {
    URL url = createURL(urlstr);
    URLConnection uc;
    uc = url.openConnection();
    uc.setRequestProperty("User-Agent", "FeedResearch");
    uc.connect();
    InputStream istream = uc.getInputStream();
    InputStreamReader reader = new InputStreamReader(istream);
    StringBuilder sb = new StringBuilder();
    char[] buffer = new char[16384];
    while (true) {
      int len = reader.read(buffer);
      if (len == -1) {
        break;
      }
      String text = new String(buffer, 0, len);
      sb.append(text);
    }
    return sb.toString();
  }

  public HtmlDocument readHtml(String urlstr) throws Exception {
    return readHtml(urlstr, false);
  }

  public HtmlDocument readHtml(String urlstr, boolean metaOnly) throws Exception {
    URL url = createURL(urlstr);
    HtmlDocument document = new HtmlDocument();
    document.setUrl(url);
    try {
      URLConnection connection = url.openConnection();
      connection.connect();
      document.setContentType(connection.getContentType());
      document.setLastModified(connection.getLastModified());
      document.setEncodingType(connection.getContentEncoding());
      document.setContentLength(connection.getContentLength());
      BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
      long contentLength = 0;
      if (!metaOnly) {
        char[] buffer = new char[2048];
        int len;
        while ((len = reader.read(buffer, 0, 2048)) != -1) {
          document.addContent(new String(buffer, 0, len));
          contentLength += len;
        }
      }
      document.setContentLength(contentLength);
      reader.close();
      document.setStatus(HtmlDocument.STATUS_OK);
    } catch (UnknownHostException ex) {
      document.setStatus(HtmlDocument.STATUS_UNKNOWNHOST);
    } catch (SocketTimeoutException ex) {
      document.setStatus(HtmlDocument.STATUS_TIMEOUT);
    } catch (FileNotFoundException ex) {
      document.setStatus(HtmlDocument.STATUS_FILENOTFOUND);
    } catch (IOException ex) {
      document.setStatus(HtmlDocument.STATUS_IOERROR);
    }
    return document;
  }

  public static void testRSSReader() throws Exception {
    GenericDataSource dataSource = new GenericDataSource();
    String urlstr = "http://www.rthk.org.hk/rthk/news/rss/c_expressnews.xml";
    List<RSSDocument> list = dataSource.readRSS(urlstr);
    for (RSSDocument data : list) {
      System.out.println(data);
    }
  }

  public static void testRawReader() throws Exception {
    GenericDataSource dataSource = new GenericDataSource();
    String urlstr = "http://www.rthk.org.hk/";
    System.out.println(dataSource.readRaw(urlstr));
  }

  public static void testHtmlReader() throws Exception {
    GenericDataSource dataSource = new GenericDataSource();
    String urlstr = "http://www.rthk.org.hk/";
    System.out.println(dataSource.readHtml(urlstr));
  }

  public static void main(String args[]) throws Exception {
    //testRSSReader();
    //testRawReader();
    testHtmlReader();
  }
}