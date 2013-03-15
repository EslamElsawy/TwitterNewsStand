package clustering;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.ar.ArabicAnalyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.util.Version;

public class TweetVector {

    private Map<String, Integer> wordsMap;
    private Analyzer analizer;
    private QueryParser parser;
    private String parse_option;
    
    /**
     * Constructor for the tweet vector using type either Arabic or English
     * @param type 
     */
    public TweetVector(int type) {
        this.wordsMap = new HashMap<String, Integer>();
        if(type == 0) {
            this.analizer = new ArabicAnalyzer(Version.LUCENE_36);
            this.parser = new QueryParser(Version.LUCENE_36, "", analizer);
            this.parse_option = "[^\u0600-\u06FF]+";
        } else if(type == 1) {
            this.analizer = new EnglishAnalyzer(Version.LUCENE_36);
            this.parser = new QueryParser(Version.LUCENE_36, "", analizer);
            this.parse_option = "[^a-zA-Z]+";
        }
    }

    /**
     * Building the actual tweet vector for the stemmed tweet to be used in
     * similarity measures later
     *
     * @param stemmed_tweet
     */
    public void build_vector(final String stemmed_tweet) {
        try {
            String parsed_tweet = this.parser.parse(stemmed_tweet).toString();
            for (String w : parsed_tweet.split(parse_option)) {
                this.updateCount(w);
            }
        } catch (ParseException ex) {
            System.out.println("Error ya Teet");
        }
    }
    
    /**
     * upgrades the count of one word (term) in the built vector for a certain
     * tweet
     *
     * @param term
     */
    public void updateCount(final String term) {
        Integer oldCount = wordsMap.get(term);
        wordsMap.put(term, oldCount == null ? 1 : oldCount + 1);
    }

    /**
     * calculates the cosine similarity of one tweet against the other one
     *
     * @param otherVector
     * @return
     */
    public double cosineSimilarity(final TweetVector otherVector) {
        double innerProduct = 0;
        for (String w : this.wordsMap.keySet()) {
            innerProduct += this.getCount(w) * otherVector.getCount(w);
        }
        return innerProduct / (this.getNorm() * otherVector.getNorm());
    }

    /**
     * Calculate the norm of this tweet vector to be used in the cosine
     * similarity
     *
     * @return
     */
    public double getNorm() {
        double sum = 0;
        for (Integer count : wordsMap.values()) {
            sum += count * count;
        }
        return Math.sqrt(sum);
    }

    /**
     * get count of such word in the vector map
     *
     * @param word
     * @return
     */
    public int getCount(final String word) {
        return wordsMap.containsKey(word) ? wordsMap.get(word) : 0;
    }
}
