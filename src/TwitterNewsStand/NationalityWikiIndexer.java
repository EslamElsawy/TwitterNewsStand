package TwitterNewsStand;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.ar.ArabicAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.store.*;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

/**
 *
 * @author Tarek
 */
public class NationalityWikiIndexer {

    private static Analyzer analyzer = new ArabicAnalyzer(Version.LUCENE_36);
    private IndexWriter writer;
    private IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_36,
            analyzer);
    private ArrayList<File> queue = new ArrayList<File>();
    public static String nationalityDirectory = "C:\\Users\\mresl_000\\Documents\\NetB"
            + "eansProjects\\Twitter4jWikipedia\\Nationality";

    public NationalityWikiIndexer(String indexDir) throws IOException {
        FSDirectory dir = FSDirectory.open(new File(indexDir));
        writer = new IndexWriter(dir, config);
    }

    public void indexFileOrDirectory() throws IOException {
        File titles = new File(nationalityDirectory + "\\Nationality");
        int originalNumDocs = writer.numDocs();
        for (File f : titles.listFiles()) {
            FileReader titleFile = null;
            FileReader articleFile = null;
            try {
                Document doc = new Document();
                titleFile = new FileReader(nationalityDirectory + "\\Country\\" + f.getName());
                articleFile = new FileReader(nationalityDirectory + "\\Nationality\\" + f.getName());
                doc.add(new Field("title", titleFile));
                doc.add(new Field("article", articleFile));
                doc.add(new Field("filename", f.getName(), Field.Store.YES, Field.Index.ANALYZED));
                writer.addDocument(doc);
                System.out.println("Added: " + f);
            } catch (Exception e) {
                System.out.println("Could not add: " + f);
            } finally {
                titleFile.close();
            }
        }
        int newNumDocs = writer.numDocs();
        System.out.println("");
        System.out.println("************************");
        System.out.println((newNumDocs - originalNumDocs) + " documents added.");
        System.out.println("************************");
        queue.clear();
        closeIndex();
    }
    // =============================================================

    public void closeIndex() throws IOException {
        writer.close();
    }
    //=============================================================

    public String[] searchArticle(String query, String indexLocation) throws IOException, ParseException {
        IndexReader reader = IndexReader.open(
                NIOFSDirectory.open(new File(indexLocation)), false);
        IndexSearcher searcher = new IndexSearcher(reader);
        String[] files = null;
        try {
            Query q = new QueryParser(Version.LUCENE_36, "article", analyzer).parse(query);
            TopScoreDocCollector collector = TopScoreDocCollector.create(10, true);
            searcher.search(q, collector);
            ScoreDoc[] hits = collector.topDocs().scoreDocs;
            files = new String[hits.length];
            for (int i = 0; i < hits.length; i++) {
                Document d = null;
                int docId = hits[i].doc;
                d = searcher.doc(docId);
                files[i] = d.get("filename");
            }
            return files;
        } catch (Exception e) {
            System.out.println("error in parsing in : " + query);
            return null;
        }

    }
    //=====================================================================

    public String[] searchTitle(String query, String indexLocation) throws IOException, ParseException {
        IndexReader reader = IndexReader.open(
                NIOFSDirectory.open(new File(indexLocation)), false);
        IndexSearcher searcher = new IndexSearcher(reader);
        String[] files = null;
        try {
            Query q = new QueryParser(Version.LUCENE_36, "title", analyzer).parse(query);
            TopScoreDocCollector collector = TopScoreDocCollector.create(10, true);
            searcher.search(q, collector);
            ScoreDoc[] hits = collector.topDocs().scoreDocs;
            files = new String[hits.length];
            for (int i = 0; i < hits.length; i++) {
                Document d = null;
                int docId = hits[i].doc;
                d = searcher.doc(docId);
                files[i] = d.get("filename");
            }
            return files;
        } catch (Exception e) {
            System.out.println("error in parsing in : " + query);
            return null;
        }

    }

    public static void main(String[] args) throws IOException, ParseException {
        String indexLoc = "WikiIndex";
        NationalityWikiIndexer wi = new NationalityWikiIndexer(indexLoc);
//        wi.indexFileOrDirectory();
//        String query = "كوريون جنوبيون";
//
//        String[] titles = wi.searchTitle(query, "WikiIndex");
//        System.out.println(Arrays.toString(titles));
//        String[] articles = wi.searchArticle(query, "WikiIndex");
//        System.out.println(Arrays.toString(articles));
//        System.out.println("Countries Match");
//        for (String s : titles) {
//            BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(nationalityDirectory + "\\Country\\" + s)));
//            System.out.println(in.readLine());
//        }
//        System.out.println("Nationalities Match");
//        for (String s : articles) {
//            BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(nationalityDirectory + "\\Nationality\\" + s)));
//            System.out.println(in.readLine());
//        }
    }
}
