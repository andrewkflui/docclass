package oucomp.summarize;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.en.PorterStemFilter;
import org.apache.lucene.analysis.standard.ClassicTokenizer;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.util.CharArraySet;

/**
 * Special purpose analyzer that uses a chain of PorterStemFilter, StopFilter,
 * LowercaseFilter and StandardFilter to wrap a StandardTokenizer. The
 * StopFilter uses a custom stop word set adapted from:
 * http://www.onjava.com/onjava/2003/01/15/examples/EnglishStopWords.txt For
 * ease of maintenance, we put these words in a flat file and import them on
 * analyzer construction.
 */
public class SummaryAnalyzer extends Analyzer {

  protected CharArraySet stopset = new CharArraySet(1000, true);

  public SummaryAnalyzer() throws IOException {
    InputStream restream = SummaryAnalyzer.class.getResourceAsStream("/oucomp/summarize/stopwords.txt");
    BufferedReader reader = new BufferedReader(new InputStreamReader(restream, "UTF-8"));
    while (true) {
      String stopword = reader.readLine();
      if (stopword == null) {
        break;
      }
      if (!stopword.startsWith("#")) {
        stopset.add(stopword);
      }
    }
    reader.close();
  }

  @Override
  protected TokenStreamComponents createComponents(String fieldName, Reader reader) {
    final Tokenizer source = new ClassicTokenizer(reader);
    TokenStream filterSet =
            new PorterStemFilter(
            new StopFilter(
            new LowerCaseFilter(
            new StandardFilter(source)), stopset));
    return new TokenStreamComponents(source, filterSet);
  }
}
