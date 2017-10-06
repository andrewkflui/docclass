package oucomp.rss;

/**
 * Required Libraries: jdom.jar and rome-0.9.jar
 */
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

public class RSSFeedRetriever {

  private String proxyhost = null;
  private int proxyport = -1;
  private String urlstr = null;
  private URL feedurl = null;

  public RSSFeedRetriever() {
    this(null, -1);
  }

  public RSSFeedRetriever(String proxyhost, int proxyport) {
    this.proxyhost = proxyhost;
    this.proxyport = proxyport;
  }

  public ArrayList<RSSFeedData> process(String urlstr, String encoding) throws Exception {
    if (proxyhost == null || proxyport < 0) {
      return process(new URL(urlstr), encoding);
    } else {
      return process(new URL("http", proxyhost, proxyport, urlstr), encoding);
    }
  }

  public ArrayList<RSSFeedData> process(URL feedurl, String encoding) throws Exception {
    this.feedurl = feedurl;
    SyndFeedInput input = new SyndFeedInput();
    SyndFeed feed = input.build(new XmlReader(feedurl));
    ArrayList<RSSFeedData> results = new ArrayList();
    List entries = feed.getEntries();
    for (int i = 0; i < entries.size(); i++) {
      SyndEntry entry = (SyndEntry) entries.get(i);
      RSSFeedData item = new RSSFeedData();
      String mimetype = entry.getDescription().getType();
      String dbencoding = "ISO-8859-1";
      item.title = entry.getTitle();
      item.title = new String(item.title.getBytes(encoding), dbencoding);
      item.publishDate = entry.getPublishedDate().getTime();
      item.uri = entry.getUri();
      item.link = entry.getLink();
      item.content = entry.getDescription().getValue();
      item.content = new String(item.content.getBytes(encoding), dbencoding);
      item.ingestDate = System.currentTimeMillis();
      item.mimetype = mimetype;
      results.add(item);
    }
    return results;
  }

  public static String readRaw(String rssurl) throws Exception {
    URL feedUrl = new URL(rssurl);
    SyndFeedInput input = new SyndFeedInput();
    URLConnection uc;
    uc = feedUrl.openConnection();
    uc.setRequestProperty("User-Agent", "FeedResearch");
    uc.connect();
    //System.out.println("Connection");
    //System.out.println(uc.getContentEncoding());
    //System.out.println(uc.getContentType());
    InputStream istream = uc.getInputStream();
    InputStreamReader reader = new InputStreamReader(istream);
    StringBuilder sb = new StringBuilder();
    char[] buffer = new char[256];
    while (true) {
      int len = reader.read(buffer);
      if (len == -1) {
        break;
      }
      String text = new String(buffer, len, 0);
      System.out.println("reading " + text);
      sb.append(buffer, len, 0);
    }
    return sb.toString();
  }

  public static void main(String args[]) throws Exception {
    /*
    WebpageTextExtractor extractor = new WebpageTextExtractor();
    String urlstr = "http://hk.news.yahoo.com/rss/news_hk_general_all.xml";
    urlstr = "http://www.rthk.org.hk/rthk/news/rss/c_expressnews.xml?20080630";
    RSSFeedRetriever retriever = new RSSFeedRetriever();
    ArrayList<RSSFeedData> list = retriever.process(urlstr, "big5");
    for (int i = 0; i < list.size(); i++) {
      RSSFeedData item = list.get(i);
      System.out.println(item);
    //String link = item.link;
    //extractor.process(link);
    //extractor.setEncoding("big5");
    //String text = extractor.extractText();
    // System.out.println(extractor.extractText());
    }
     */
  }
}
