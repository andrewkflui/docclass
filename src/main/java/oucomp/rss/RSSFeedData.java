package oucomp.rss;

import java.util.Date;

public class RSSFeedData {

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
        sb.append("[Title] " + title + "\n");
        sb.append("[Content] " + content + "\n");
        sb.append("[Source] " + source + "\n");
        sb.append("[Mimetype] " + mimetype + "\n");        
        sb.append("[Published] " + new Date(publishDate) + "\n");
        sb.append("[Uri] " + uri + "\n");        
        sb.append("[Link] " + link + "\n");
        return sb.toString();
    }
    
}
