package oucomp.summarize;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;

public class SentenceTokenizer {

  private String sentenceArray[] = null;
  private int index = 0;
  SentenceDetectorME sentenceDetector;

  public SentenceTokenizer() throws Exception {
    super();
    InputStream modelIn = SentenceTokenizer.class.getResourceAsStream("/oucomp/summarize/en-sent.bin");
    try {
      SentenceModel model = new SentenceModel(modelIn);
      sentenceDetector = new SentenceDetectorME(model);
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      try {
        modelIn.close();
      } catch (IOException e) {
      }
    }
  }

  public void setText(String text) {
    sentenceArray = sentenceDetector.sentDetect(text);
    this.index = 0;
  }

  public String nextSentence() {
    while (true) {
      if (sentenceArray == null || index >= sentenceArray.length) {
        return null;
      }
      String result = sentenceArray[index++].trim();
      if (result.length() > 0) {
        return result;
      }
    }

  }
}
