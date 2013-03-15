/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package TwitterNewsStand;

/**
 *
 * @author Ahmed
 */
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import org.apache.lucene.analysis.ar.ArabicAnalyzer;
import org.apache.lucene.analysis.ar.ArabicAnalyzer;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Query;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.store.*;
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
import org.apache.lucene.util.Version;

public class test {

    public static String getNormalizedTweet(String original)throws ParseException{
        ArabicAnalyzer analyzer = new ArabicAnalyzer(Version.LUCENE_36);
        Query q = new QueryParser(Version.LUCENE_36, "", analyzer).parse(original);
        return q.toString();
    }
// tesing
    public static void main(String[] args) throws IOException ,ParseException{
        BufferedReader NewTrainingFile = new BufferedReader(new FileReader(
                "nNews.txt"));
        BufferedReader NewTrainingFile2 = new BufferedReader(new FileReader(
                "Junks.txt"));
        BufferedReader NewTrainingFile3 = new BufferedReader(new FileReader(
                "Test.txt"));
        String line;
        ArrayList<Tweet> ar = new ArrayList<>();
        Tweet t;
        while ((line = NewTrainingFile3.readLine()) != null) {
            t = new Tweet(line, null, null, 0, null, null, null, 0, 0);
            ar.add(t);
        }
        NewTrainingFile3.close();
        NaiveBayes n = new NaiveBayes();
        n.runMain(NewTrainingFile, NewTrainingFile2, ar);
        
    }
}
