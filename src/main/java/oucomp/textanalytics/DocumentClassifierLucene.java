package oucomp.textanalytics;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Iterator;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.ngram.NGramTokenizer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

public class DocumentClassifierLucene {

  public static final int ANALYZER_STANDARD = 1;
  public static final int ANALYZER_LOWERCASE = 2;
  public static final int ANALYZER_NGRAM = 3;
  public static final Version VERSION = Version.LUCENE_4_10_1;

  public static Analyzer createAnalyzer(int analyzerType) {
    Analyzer analyzer = null;
    switch (analyzerType) {
      case ANALYZER_STANDARD:
        analyzer = new StandardAnalyzer();
        break;
      case ANALYZER_LOWERCASE:
        analyzer = new Analyzer() {
          protected Analyzer.TokenStreamComponents createComponents(String fieldName, Reader reader) {
            Tokenizer source = new StandardTokenizer(reader);
            TokenStream filter = new LowerCaseFilter(source);
            return new Analyzer.TokenStreamComponents(source, filter);
          }
        };
        break;
      case ANALYZER_NGRAM:
        analyzer = new Analyzer() {
          protected Analyzer.TokenStreamComponents createComponents(String fieldName, Reader reader) {
            Tokenizer source = new NGramTokenizer(reader, 4, 4);
            return new Analyzer.TokenStreamComponents(source);
          }
        };
        break;
    }
    return analyzer;
  }

  public static void buildIndex(int analyzerType, DocumentSet docset, File indexDir)
          throws IOException, FileNotFoundException {
    buildIndex(createAnalyzer(analyzerType), docset, indexDir);
  }

  public static void buildIndex(Analyzer analyzer, DocumentSet docset, File indexDir)
          throws IOException, FileNotFoundException {

    Directory fsDir = FSDirectory.open(indexDir);
    IndexWriterConfig iwConf = new IndexWriterConfig(VERSION, analyzer);
    iwConf.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
    IndexWriter indexWriter = new IndexWriter(fsDir, iwConf);
    Iterator<Document> it = docset.iterator();
    while (it.hasNext()) {
      indexWriter.addDocument(it.next());
    }
    System.out.println("[DocumentClassifierLucene] Number of documents is " + indexWriter.numDocs());
    indexWriter.forceMerge(1);
    indexWriter.commit();
    indexWriter.close();
    System.out.println("[DocumentClassifierLucene] Index built at " + indexDir.getAbsolutePath());
  }

  public static ScoreDoc[] query(int analyzerType, String query, File indexDir)
          throws IOException, FileNotFoundException {
    return query(createAnalyzer(analyzerType), query, indexDir);
  }

  public static ScoreDoc[] query(Analyzer analyzer, String query, File indexDir)
          throws IOException, FileNotFoundException {
    Directory fsDir = FSDirectory.open(indexDir);
    DirectoryReader reader = DirectoryReader.open(fsDir);
    IndexSearcher searcher = new IndexSearcher(reader);
    BooleanQuery termsQuery = buildQuery(analyzer, query);
    TopDocs hits = searcher.search(termsQuery, 1);
    ScoreDoc[] scoreDocs = hits.scoreDocs;
    return scoreDocs;
  }

  public static ConfusionMatrix testIndexWithFolder(int analyzerType, DocumentSet testdocset, File indexDir)
          throws IOException, FileNotFoundException {
    return testIndexWithFolder(createAnalyzer(analyzerType), testdocset, indexDir);
  }

  public static ConfusionMatrix testIndexWithFolder(Analyzer analyzer, DocumentSet testdocset, File indexDir)
          throws IOException, FileNotFoundException {
    Directory fsDir = FSDirectory.open(indexDir);
    DirectoryReader reader = DirectoryReader.open(fsDir);
    IndexSearcher searcher = new IndexSearcher(reader);
    ConfusionMatrix confusionMatrix = new ConfusionMatrix();
    // iterates each test document
    Iterator<Document> it = testdocset.iterator();
    while (it.hasNext()) {
      Document querydoc = it.next();
      BooleanQuery termsQuery = buildQuery(analyzer, querydoc.get(DocumentSet.DOC_CONTENT));
      String queryCategory = querydoc.get(DocumentSet.DOC_CATEGORY);
      TopDocs hits = searcher.search(termsQuery, 1);
      ScoreDoc[] scoreDocs = hits.scoreDocs;
      //System.out.println(termsQuery.toString());
      for (int n = 0; n < scoreDocs.length; ++n) {
        ScoreDoc sd = scoreDocs[n];
        int docId = sd.doc;
        //System.out.println(n + ":" + docId + ":" + sd.score);
        Document doc = searcher.doc(docId);
        String resultCategory = doc.get(DocumentSet.DOC_CATEGORY);
        confusionMatrix.addValueToMatrix(queryCategory, resultCategory, 1);
      }
    }
    return confusionMatrix;
  }

  public static BooleanQuery buildQuery(int analyzerType, String text) throws IOException {
    return buildQuery(createAnalyzer(analyzerType), text);
  }

  public static BooleanQuery buildQuery(Analyzer analyzer, String text) throws IOException {
    BooleanQuery termsQuery = new BooleanQuery();
    Reader textReader = new StringReader(text);
    TokenStream tokStream = analyzer.tokenStream(DocumentSet.DOC_CONTENT, textReader);
    try {
      tokStream.reset();
      CharTermAttribute terms = tokStream.addAttribute(CharTermAttribute.class);
      int ct = 0;
      while (tokStream.incrementToken() && ct++ < 1024) {
        termsQuery.add(new TermQuery(new Term(DocumentSet.DOC_CONTENT, terms.toString())), BooleanClause.Occur.SHOULD);
      }
      tokStream.end();
    } finally {
      tokStream.close();
      textReader.close();
    }
    return termsQuery;
  }

  public static void main(String args[]) throws Exception {
    File datasetFolder = new File ("/Users/andrewlui/Documents/Development/corpus/Reuters21578-Apte-90Cat/");
    File trainFolder = new File(datasetFolder, "training");
    File testFolder = new File(datasetFolder, "test");
    File indexFolder = new File(datasetFolder, "index");
    String categoryArray[] = {"acq", "alum", "barley", "bop", "cocoa", "coffee", "copper", "cotton"};
    int analyzerType = ANALYZER_STANDARD;
    DocumentSetFolderMerged trainSet = new DocumentSetFolderMerged();
    DocumentSetFolder testSet = new DocumentSetFolder();
    for (int i = 0; i < categoryArray.length; i++) {
      trainSet.addFolder(categoryArray[i], new File(trainFolder, categoryArray[i]));
      testSet.addFolder(categoryArray[i], new File(testFolder, categoryArray[i]));
    }
    System.out.println("[DocumentClassifierLucene] Building Index");
    buildIndex(analyzerType, trainSet, indexFolder);
    System.out.println("[DocumentClassifierLucene] Testing Index");
    ConfusionMatrix confusionMatrix = testIndexWithFolder(analyzerType, testSet, indexFolder);
    confusionMatrix.printMatrix();
  }
}
