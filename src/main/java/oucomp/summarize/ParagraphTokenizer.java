package oucomp.summarize;

import java.util.regex.Pattern;

public class ParagraphTokenizer {

  private String paraArray[] = null;
  private int index = 0;

  public ParagraphTokenizer() throws Exception {
    super();
  }

  public void setText(String text) {
    paraArray = Pattern.compile("(?<=(\r\n|\r|\n))([ \\t]*$)+", Pattern.MULTILINE).split(text);
    index = 0;
  }

  public String nextParagraph() {
    while (true) {
      if (paraArray == null || index >= paraArray.length) {
        return null;
      }
      String result = paraArray[index++].trim();
      if (result.length() > 0)
        return result;
    }
  }
}