package TwitterNewsStand;

import java.io.*;
import java.util.*;
import java.util.ArrayList;
import org.apache.lucene.queryParser.ParseException;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import clustering.*;
import twitter4j.Twitter;
import twitter4j.TwitterException;
/*
 * To change this template, choose Tools | Templates and open the template in
 * the editor.
 */

/**
 *
 * @author mresl_000
 */
public class NewMain {

    public static void main(String[] args) throws TwitterException, InterruptedException, IOException, ParseException, ClassNotFoundException, java.text.ParseException {

        String OAuthConsumerKey = "IaUXT1xdN8YKHid8nOumgw";
        String OAuthConsumerSecret = "N1vCxXoq0uCL4BxUF2OwyRcFeImHLXtjNPZWQFiavA";
        String OAuthAccessToken = "1234410656-eBBUPY0SLOxqK7Evq8b3WDvvvKMnPYOjZqXojRE";
        String OAuthAccessTokenSecret = "Tqg6SmrwnOHw8lIx5V1arVzvRNIVW0mSby3cdOD98Y";
        int tweet_count = 0;
        MyTwitterAPI myTwitterAPI = new MyTwitterAPI();
        Twitter twitter = myTwitterAPI.ConnectTwitter(OAuthConsumerKey, OAuthConsumerSecret,
                OAuthAccessToken, OAuthAccessTokenSecret);
        NaiveBayes naiveBayesClassifier = new NaiveBayes();
        LeaderFollowers lf = new LeaderFollowers(0.35, 0.4);
        CredibilityComp credibility = new CredibilityComp();
        CategoriesClassifier cc = new CategoriesClassifier("Category_index2");
        NaiveBayesCategories bayesCategories = new NaiveBayesCategories();
        GeoTagging myGeoTagger = new GeoTagging();

        //Test Code                                    
        while (true) {

            BufferedWriter bw = new BufferedWriter(new FileWriter("clustering.out"));
            long startTime = System.currentTimeMillis();

            //choose your input : 1- xml File   2 - real time
            
            //1 - xml File 
//            String inputFileName = "C:\\Users\\mresl_000\\Documents\\NetBeans"
//                    + "Projects\\DataBaseTwitter\\Statistics\\ArabicNew"
//                    + "sProviders\\All_ArabicNewsProviders_2.9.xml";
//
//            ArrayList<Tweet> tweets = new FileCleaner().readAndCleanData(inputFileName);
//            ArrayList<Tweet> temp = new ArrayList<>();
//            for (int i = 0; i < 5000; i++) {
//                temp.add(tweets.get(i));
//            }
//            tweets = temp;

            //2 - real time streams.
            ArrayList<Tweet> tweets = myTwitterAPI.GetHomeTimeLine(twitter);
            tweets.addAll(myTwitterAPI.GetQueryResult(twitter, "عاجل"));


            System.out.println("Start of classification");
            naiveBayesClassifier.secondRunMain(tweets);
            bayesCategories.secondRunMain(tweets);

            System.out.println("Start of clustering");
            if (tweet_count > 0 && tweet_count % 1000 == 0) {
                lf.defragClusters_Update();
            }
            tweet_count += tweets.size();
            // el tweetaya et7tt fi el cluster el folany
            Vector<Pair<Tweet, Integer>> v = lf.go_all(tweets);
            // tdeha el id tdek el cluster
            HashMap<Integer, Cluster> myclusters = lf.getClusters();

            System.out.println("Start of Credability");
            credibility.SecondRun(v, myclusters);

            System.out.println("Start of Category Labelling");
            cc.labelCluster(v, myclusters);
            
            /*Don't uncoment this code because it needs the wikipedia database.
             System.out.println("Start of Geo Tagging");
             //Geotagging code
             myGeoTagger.geoTagTweets(tweets);
             myGeoTagger.geoTagClusters(myclusters);

             System.out.println("Start of NER");
             //Name Entities Extarction
             myGeoTagger.extarctTweetsNameEntities(tweets);
             myGeoTagger.extractClustersNameEntities(myclusters);
             */

            //printing code
            int notGeoTaggedCount = 0;
            for (Cluster c : myclusters.values()) {
//                System.out.println("Cluster:"+c.getID()+" Size:"+c.getGeoTag().size());
                if (c.getGeoTag().isEmpty()) {
                    notGeoTaggedCount++;
                }
                bw.append("ID: " + c.getID() + " ,SIZE = " + c.getMembers().size() + "\n");
                for (Tweet t : c.getMembers()) {
                    bw.append(t.getOriginalText() + " Retweets: " + t.getRetweetCount() + "\n");
                }
                bw.append("Category Label : " + c.getCategory() + "\n");
                bw.append("Credability : " + c.getCredibility() + "\n");
                bw.append("Geo Tags : " + c.getGeoTag().toString() + "\n");
                bw.append("Name Entities : " + c.getNameEntities().toString() + "\n");
                bw.append("========================================================\n");
                bw.flush();
            }

            //recording the time.
            System.out.println("Not GeoTagged: " + notGeoTaggedCount);
            long endTime = System.currentTimeMillis();
            System.out.println("That took " + (endTime - startTime) + " milliseconds");

            Thread.sleep(20000);
        }
    }
}
