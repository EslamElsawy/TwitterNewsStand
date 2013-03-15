package TwitterNewsStand;

import clustering.Cluster;
import clustering.Pair;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

import org.apache.lucene.analysis.ar.ArabicAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.NIOFSDirectory;
import org.apache.lucene.util.Version;

public class CategoriesClassifier {

    private static ArabicAnalyzer analyzer = new ArabicAnalyzer(Version.LUCENE_36);
    private IndexWriter writer;
    private IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_36,
            analyzer);
    private ArrayList<File> queue = new ArrayList<File>();
    private String indexLocation;

    public CategoriesClassifier(String indexDir) throws IOException {
        indexLocation = indexDir;
        FSDirectory dir = FSDirectory.open(new File(indexDir));
        writer = new IndexWriter(dir, config);
    }

    // ============================================================
    public void indexFileOrDirectory(String fileName) throws IOException {
        addFiles(new File(fileName));
        int originalNumDocs = writer.numDocs();
        for (File f : queue) {
            FileReader fr = null;
            try {
                Document doc = new Document();
                fr = new FileReader(f);
                doc.add(new Field("contents", fr));
                doc.add(new Field("path", f.getPath(), Field.Store.YES, Field.Index.ANALYZED));
                doc.add(new Field("filename", f.getName(), Field.Store.YES, Field.Index.ANALYZED));
                writer.addDocument(doc);
                System.out.println("Added: " + f);
            } catch (Exception e) {
                System.out.println("Could not add: " + f);
            } finally {
                fr.close();
            }
        }
        int newNumDocs = writer.numDocs();
        System.out.println("");
        System.out.println("************************");
        System.out.println((newNumDocs - originalNumDocs) + " documents added.");
        System.out.println("************************");
        queue.clear();
    }

    // =============================================================
    private void addFiles(File file) {
        if (!file.exists()) {
            System.out.println(file + " does not exist.");
        }
        if (file.isDirectory()) {
            for (File f : file.listFiles()) {
                addFiles(f);
            }
        } else {
            queue.add(file);
        }
    }

    public void closeIndex() throws IOException {
        writer.close();
    }

    // =============================================================
    public String search(String query)
            throws IOException, ParseException {
        // query = query.replaceAll("[^\u0600-\u06FF\\s0-9]", "");
        IndexReader reader = IndexReader.open(
                NIOFSDirectory.open(new File(indexLocation)), false);
        IndexSearcher searcher = new IndexSearcher(reader);
        try {
            Query q = new QueryParser(Version.LUCENE_36, "contents", analyzer).parse(query);
            TopScoreDocCollector collector = TopScoreDocCollector.create(1,
                    true);
            searcher.search(q, collector);
            ScoreDoc[] hits = collector.topDocs().scoreDocs;
            Document d = null;
            int docId = hits[0].doc;
            d = searcher.doc(docId);
            return d != null ? d.get("filename") : "junk";
        } catch (Exception e) {
            // out.println("error in parsing in : " + query);
            return "junk";
        }
    }

    public String getClusterType(int[] a) {
        int index = 0, max = 0;
        for (int i = 0; i < a.length; i++) {
            if (a[i] > max) {
                max = a[i];
                index = i;
            }
        }
        return index == 0 ? "economy" : index == 1 ? "health"
                : index == 2 ? "politics" : index == 3 ? "sports"
                : index == 4 ? "technology" : index == 5 ? "weather"
                : index == 6 ? "art" : "cars";
    }

    public void labelCluster(Vector<Pair<Tweet, Integer>> v, HashMap<Integer, Cluster> myclusters) throws IOException, ParseException {
        for (Pair<Tweet, Integer> p : v) {
            Cluster c = myclusters.get(p.getR());
            int[] cate = new int[8];
            for (int i = 0; i < c.getMembers().size(); i++) {
                Tweet tweet = c.getMembers().get(i);
                if (tweet.getCategory() == null) {
                    String type = this.search(tweet.getProcessedText());
                    tweet.setCategory(type);
                }
                String type = tweet.getCategory();
                if (type.equals("economy.txt")) {
                    cate[0]++;
                } else if (type.equals("health.txt")) {
                    cate[1]++;
                } else if (type.equals("politics.txt")) {
                    cate[2]++;
                } else if (type.equals("sports.txt")) {
                    cate[3]++;
                } else if (type.equals("technology.txt")) {
                    cate[4]++;
                } else if (type.equals("weather.txt")) {
                    cate[5]++;
                } else if (type.equals("art.txt")) {
                    cate[6]++;
                } else if (type.equals("cars.txt")) {
                    cate[7]++;
                }
            }
            String ctype = this.getClusterType(cate);
            c.setCategory(ctype);
        }
    }

    public static void main(String[] args) throws IOException, ParseException {

        // TODO Auto-generated method stub String indexLoc =
        String indexLoc = "Category_index2";
        CategoriesClassifier c = null;
        try {
            c = new CategoriesClassifier(indexLoc);
        } catch (Exception ex) {
            System.out.println("Cannot create index..." + ex.getMessage());
            System.exit(-1);
        }
        c.indexFileOrDirectory("Categories\\Training Set");
        c.closeIndex();

//        BufferedReader br = new BufferedReader(new InputStreamReader(
//                new FileInputStream("clusters.txt")));
//        BufferedWriter outNews = new BufferedWriter(new FileWriter(
//                "ArabicClusters3.txt", true));
//
//        String s = br.readLine();
//        int[] cate = new int[8];
//        while (s != null) {
//            if (!s.startsWith("---------------")) {
//                // s = s.replaceAll("[^a-zA-Z\\s]+", "");
//                // s = s.toLowerCase();
//                String type = c.search(s, indexLoc, null);
//                if (type.equals("economy.txt")) {
//                    cate[0]++;
//                } else if (type.equals("health.txt")) {
//                    cate[1]++;
//                } else if (type.equals("politics.txt")) {
//                    cate[2]++;
//                } else if (type.equals("sports.txt")) {
//                    cate[3]++;
//                } else if (type.equals("technology.txt")) {
//                    cate[4]++;
//                } else if (type.equals("weather.txt")) {
//                    cate[5]++;
//                } else if (type.equals("art.txt")) {
//                    cate[6]++;
//                } else if (type.equals("cars.txt")) {
//                    cate[7]++;
//                }
//                outNews.append(type + " : " + s + "\n");
//                outNews.flush();
//            } else {
//                outNews.append("*************" + getClusterType(cate)
//                        + "****************\n");
//                outNews.append(s + "\n");
//                cate = new int[8];
//            }
//            s = br.readLine();
//        }
//        outNews.close();

    }
}
