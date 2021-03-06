package oucomp.summarize;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import org.apache.commons.collections4.comparators.ReverseComparator;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.AtomicReader;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.SlowCompositeReaderWrapper;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.Version;

public class SummarizerLucene {

  private Analyzer analyzer = new StandardAnalyzer();
  private int numSentences = 2;
  private float topTermCutoff;
  // these two values are used to implement a simple linear deboost. If 
  // a different algorithm is desired, these variables are likely to be
  // no longer required.
  private float sentenceDeboost;
  private float sentenceDeboostBase = 0.5F;
  private ParagraphTokenizer paragraphTokenizer;
  private SentenceTokenizer sentenceTokenizer;

  public void setAnalyzer(Analyzer analyzer) {
    this.analyzer = analyzer;
  }

  public void setNumSentences(int numSentences) {
    this.numSentences = numSentences;
  }

  /**
   * This value specifies where to cutoff the term list for query. The text is
   * loaded into an in-memory index, a sentence per Lucene Document. Then the
   * index is queried for terms and their associated frequency in the index. The
   * topTermCutoff is a ratio from 0 to 1 which specifies how far to go down the
   * frequency ordered list of terms. The terms considered have a frequency
   * greater than topTermCutoff * topFrequency.
   *
   * @param topTermCutoff a ratio specifying where the term list will be cut
   * off. Must be between 0 and 1. Default is to consider all terms if this
   * variable is not set, ie topTermCutoff == 0. But it is recommended to set an
   * appropriate value (such as 0.5).
   */
  public void setTopTermCutoff(float topTermCutoff) {
    if (topTermCutoff < 0.0F || topTermCutoff > 1.0F) {
      throw new IllegalArgumentException(
              "Invalid value: 0.0F <= topTermCutoff <= 1.0F");
    }
    this.topTermCutoff = topTermCutoff;
  }

  /**
   * Applies a index-time deboost to the sentences after the first one in all
   * the paragraphs after the first one. This attempts to model the
   * summarization heuristic that a summary can be generated by reading the
   * first paragraph (in full) of a document, followed by the first sentence in
   * every succeeding paragraph. The first paragraph is not deboosted at all.
   * For the second and succeeding paragraphs, the deboost is calculated as (1 -
   * sentence_pos * deboost) until the value reaches sentenceDeboostBase
   * (default 0.5) or less, and then no more deboosting occurs.
   *
   * @param sentenceDeboost the deboost value to set. Must be between 0 and 1.
   * Default is no deboosting, ie sentenceDeboost == 0.
   */
  public void setSentenceDeboost(float sentenceDeboost) {
    if (sentenceDeboost < 0.0F || sentenceDeboost > 1.0F) {
      throw new IllegalArgumentException(
              "Invalid value: 0.0F <= sentenceDeboost <= 1.0F");
    }
    this.sentenceDeboost = sentenceDeboost;
  }

  /**
   * This parameter is used in conjunction with sentenceDeboost. This value
   * defines the base until which deboosting will occur and then stop. Default
   * is set to 0.5 if not set. Must be between 0 and 1.
   *
   * @param sentenceDeboostBase the sentenceDeboostBase to set.
   */
  public void setSentenceDeboostBase(float sentenceDeboostBase) {
    if (sentenceDeboostBase < 0.0F || sentenceDeboostBase > 1.0F) {
      throw new IllegalArgumentException(
              "Invalid value: 0.0F <= sentenceDeboostBase <= 1.0F");
    }
    this.sentenceDeboostBase = sentenceDeboostBase;
  }

  /**
   * The init method pre-instantiates the Paragraph and Sentence tokenizers both
   * of which are based on ICU4J RuleBasedBreakIterators, so they are expensive
   * to set up, therefore we set them up once and reuse them.
   *
   * @throws Exception if one is thrown.
   */
  public void init() throws Exception {
    this.paragraphTokenizer = new ParagraphTokenizer();
    this.sentenceTokenizer = new SentenceTokenizer();
  }

  /**
   * This is the method that will be called by a client after setting up the
   * summarizer, configuring it appropriately by calling the setters, and
   * calling init() on it to instantiate its expensive objects.
   *
   * @param text the text to summarize. At this point, the text should be plain
   * text, converters ahead of this one in the chain should have done the
   * necessary things to remove HTML tags, etc.
   * @return the summary in the specified number of sentences.
   * @throws Exception if one is thrown.
   */
  public String summarize(String text) throws Exception {
    RAMDirectory ramdir = new RAMDirectory();
    buildIndex(ramdir, text);
    Query topTermQuery = computeTopTermQuery(ramdir);
    String[] sentences = searchIndex(ramdir, topTermQuery);
    return StringUtils.join(sentences, " ... ");
  }

  /**
   * Builds an in-memory index of the sentences in the text with the appropriate
   * document boosts if specified.
   *
   * @param ramdir the RAM Directory to use.
   * @param text the text to index.
   * @throws Exception if one is thrown.
   */
  private void buildIndex(Directory ramdir, String text) throws Exception {
    if (paragraphTokenizer == null || sentenceTokenizer == null) {
      throw new IllegalArgumentException(
              "Please call init() to instantiate tokenizers");
    }
    IndexWriterConfig iwConf = new IndexWriterConfig(Version.LATEST, analyzer);
    IndexWriter writer = new IndexWriter(ramdir, iwConf);
    paragraphTokenizer.setText(text);
    String paragraph = null;
    int pno = 0;
    while ((paragraph = paragraphTokenizer.nextParagraph()) != null) {
      System.out.println("paragraph: " + paragraph);
      sentenceTokenizer.setText(paragraph);
      String sentence = null;
      int sno = 0;
      while ((sentence = sentenceTokenizer.nextSentence()) != null) {
        System.out.println("sentence: " + sentence);
        Document doc = new Document();
        TextField field = new TextField("text", sentence, Store.YES);
        field.setBoost(computeDeboost(pno, sno));
        doc.add(field);
        writer.addDocument(doc);
        sno++;
      }
      pno++;
    }
    writer.commit();
    writer.close();
  }

  /**
   * Applies a linear deboost function to simulate the manual heuristic of
   * summarizing by skimming the first few sentences off a paragraph.
   *
   * @param paragraphNumber the paragraph number (0-based).
   * @param sentenceNumber the sentence number (0-based).
   * @return the deboost to apply to the current document.
   */
  private float computeDeboost(int paragraphNumber, int sentenceNumber) {
    if (paragraphNumber > 0) {
      if (sentenceNumber > 0) {
        float deboost = 1.0F - (sentenceNumber * sentenceDeboost);
        return (deboost < sentenceDeboostBase)
                ? sentenceDeboostBase : deboost;
      }
    }
    return 1.0F;
  }

  /**
   * Computes a term frequency map for the index at the specified location.
   * Builds a Boolean OR query out of the "most frequent" terms in the index and
   * returns it. "Most Frequent" is defined as the terms whose frequencies are
   * greater than or equal to the topTermCutoff * the frequency of the top term,
   * where the topTermCutoff is number between 0 and 1.
   *
   * @param ramdir the directory where the index is created.
   * @return a Boolean OR query.
   * @throws Exception if one is thrown.
   */
  private Query computeTopTermQuery(Directory ramdir) throws Exception {
    final Map<String, Integer> frequencyMap = new HashMap();
    List<String> termlist = new ArrayList();
    DirectoryReader reader = DirectoryReader.open(ramdir);
    AtomicReader wrapper = SlowCompositeReaderWrapper.wrap(reader);
    Terms terms = wrapper.terms("text");
    TermsEnum termenum = terms.iterator(TermsEnum.EMPTY);
    while (true) {
      BytesRef termText = termenum.next();
      if (termText == null) {
        break;
      }

      int frequency = termenum.docFreq();
      System.out.println("termtext: " + new String(termText.bytes, termText.offset, termText.length) + " " + frequency);      
      String termTextStr = new String(termText.bytes, termText.offset, termText.length);
      frequencyMap.put(termTextStr, frequency);
      termlist.add(termTextStr);
    }
    reader.close();
    // sort the term map by frequency descending
    Collections.sort(termlist, new ReverseComparator<String>(
            new ByValueComparator<String, Integer>(frequencyMap)));
    // retrieve the top terms based on topTermCutoff
    List<String> topTerms = new ArrayList();
    float topFreq = -1.0F;
    for (String term : termlist) {
      if (topFreq < 0.0F) {
        // first term, capture the value
        topFreq = (float) frequencyMap.get(term);
        topTerms.add(term);
       System.out.println("topterm: " + term + " " + topFreq);          
      } else {
        // not the first term, compute the ratio and discard if below
        // topTermCutoff score
        float ratio = (float) ((float) frequencyMap.get(term) / topFreq);
        if (ratio >= topTermCutoff) {
          topTerms.add(term);
        } else {
          break;
        }
      }
    }
    StringBuilder termBuf = new StringBuilder();
    BooleanQuery q = new BooleanQuery();
    for (String topTerm : topTerms) {
      termBuf.append(topTerm).append("(").append(frequencyMap.get(topTerm)).append(");");
      q.add(new TermQuery(new Term("text", topTerm)), Occur.SHOULD);
    }
    System.out.println(">>> top terms: " + termBuf.toString());
    System.out.println(">>> query: " + q.toString());
    return q;
  }

  /**
   * Executes the query against the specified index, and returns a bounded
   * collection of sentences ordered by document id (so the sentence ordering is
   * preserved in the collection).
   *
   * @param ramdir the directory location of the index.
   * @param query the Boolean OR query computed from the top terms.
   * @return an array of sentences.
   * @throws Exception if one is thrown.
   */
  private String[] searchIndex(Directory ramdir, Query query)
          throws Exception {
    SortedMap<Integer, String> sentenceMap = new TreeMap<Integer, String>();
    DirectoryReader reader = DirectoryReader.open(ramdir);
    IndexSearcher searcher = new IndexSearcher(reader);
    Explanation explaination = searcher.explain(query, numSentences);
    System.out.println("Explanation: " + explaination);
    TopDocs topDocs = searcher.search(query, numSentences);
    for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
      
      int docId = scoreDoc.doc;
      Document doc = searcher.doc(docId);
      sentenceMap.put(scoreDoc.doc, StringUtils.chomp(doc.get("text")));
    }
    return sentenceMap.values().toArray(new String[0]);
  }

  public static void testLuceneSummarizer(File fileArray[]) throws Exception {
    for (File testFile : fileArray) {
      String text = FileUtils.readFileToString(testFile, "UTF-8");
      SummarizerLucene summarizer = new SummarizerLucene();
      summarizer.setAnalyzer(new SummaryAnalyzer());
      summarizer.setNumSentences(2);
      summarizer.setTopTermCutoff(0.5F);
      summarizer.setSentenceDeboost(0.2F);
      summarizer.init();
      System.out.println("Input: " + testFile);
      String summary = summarizer.summarize(text);
      System.out.println(">>> Summary (from LuceneSummarizer): " + summary);
    }
  }

  public static void main(String args[]) throws Exception {
    List<File> fileList = new ArrayList();
    //fileList.add(new File("/Users/andrewlui/Documents/Development/corpus/operatingsystems.txt"));
    fileList.add(new File("../../corpus/operatingsystems.txt"));
    testLuceneSummarizer((File[]) fileList.toArray(new File[0]));

  }
}

class ByValueComparator<K, V extends Comparable<? super V>> implements Comparator<K> {

  private Map<K, V> freqMap = new HashMap<K, V>();

  public ByValueComparator(Map<K, V> freqMap) {
    this.freqMap = freqMap;
  }

  public int compare(K k1, K k2) {
    return freqMap.get(k1).compareTo(freqMap.get(k2));
  }
}