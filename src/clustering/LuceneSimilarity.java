package clustering;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Vector;

import org.apache.lucene.analysis.ar.ArabicAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similar.MoreLikeThis;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;
import TwitterNewsStand.Tweet;

public class LuceneSimilarity {

    private final Directory idxDir;

    /**
     * Constructor for the Lucence's similarity
     */
    public LuceneSimilarity() {
        this.idxDir = new RAMDirectory();
    }

    /**
     * return the index and the score of similarity for the nearest cluster
     * @param clusters
     * @param testSet
     * @return 
     */
    @SuppressWarnings("deprecation")
    public double[] getNearestCluster(final Vector<Cluster> clusters, final Vector<Tweet> testSet) {
        double[] idx_sim = new double[2];
        writeIndex(clusters);
        IndexReader indexReader = null;
        try {
            indexReader = IndexReader.open(idxDir, false);
            MoreLikeThis moreLikeThis = new MoreLikeThis(indexReader);
            // Lower the frequency since content is short
            moreLikeThis.setMinTermFreq(1);
            moreLikeThis.setMinDocFreq(1);

            for (int i = 0; i < testSet.size(); i++) {
                Reader reader = new StringReader(testSet.get(i).getProcessedText());
                Query query = moreLikeThis.like(reader, "contents");
                IndexSearcher searcher = new IndexSearcher(indexReader);
                TopDocs topDocs = searcher.search(query, 1);
                ScoreDoc[] hits = topDocs.scoreDocs;
                int docId = hits[0].doc;
                idx_sim[1] = (double) hits[0].score;
                Document d = searcher.doc(docId);
                int j = Integer.parseInt(d.get("id"));
                idx_sim[0] = j;
                closeSearcher(searcher);
            }
            closeIndexReader(indexReader);
        } catch (CorruptIndexException e) {
            e.printStackTrace();
            closeIndexReader(indexReader);
        } catch (IOException e) {
            e.printStackTrace();
            closeIndexReader(indexReader);
        }
        return idx_sim;
    }

    /**
     *  write the index of this cluster to the memo
     * @param clusters 
     */
    public void writeIndex(Vector<Cluster> clusters) {
        IndexWriter writer = null;
        try {
            writer = getIndexWriter();
            for (int i = 0; i < clusters.size(); i++) {
                writer.addDocument(addContentToDoc(clusters.get(i), i));
            }
            closeIndexWriter(writer);
        } catch (CorruptIndexException e) {
            e.printStackTrace();
            closeIndexWriter(writer);
        } catch (IOException e) {
            e.printStackTrace();
            closeIndexWriter(writer);
        }
    }

    /**
     * 
     * @return
     * @throws IOException 
     */
    private IndexWriter getIndexWriter() throws IOException {
        IndexWriterConfig indexWriterConfig = new IndexWriterConfig(
                Version.LUCENE_36, new ArabicAnalyzer(Version.LUCENE_36));
        return new IndexWriter(idxDir, indexWriterConfig);
    }

    /**
     * 
     * @param content
     * @param index
     * @return 
     */
    private Document addContentToDoc(Cluster content, int index) {
        Document doc = new Document();
        doc.add(new Field("id", index + "", Field.Store.YES, Field.Index.ANALYZED));
        for (Tweet t : content.getMembers()) {
            doc.add(new Field("contents", t.getProcessedText(), Field.Store.YES, Field.Index.ANALYZED));
        }
        return doc;
    }

    /**
     * 
     * @param writer 
     */
    private void closeIndexWriter(IndexWriter writer) {
        if (writer != null) {
            try {
                writer.close();
            } catch (CorruptIndexException e1) {
                e1.printStackTrace();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

    /**
     * 
     * @param reader 
     */
    private void closeIndexReader(IndexReader reader) {
        if (reader != null) {
            try {
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 
     * @param searcher 
     */
    private void closeSearcher(IndexSearcher searcher) {
        if (searcher != null) {
            try {
                searcher.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
