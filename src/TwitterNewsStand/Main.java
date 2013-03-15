package TwitterNewsStand;
import java.io.*;
import java.util.*;
import java.util.ArrayList;
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
public class Main {

    public static void main(String[] args) throws TwitterException, InterruptedException, IOException {

        String OAuthConsumerKey = "bxAim6s1TADOxux3umhiw";
        String OAuthConsumerSecret = "HGzBcOnhDczavZFEwjbMOf7zio9etaUNlfbgfceo";
        String OAuthAccessToken = "1234359457-DquIvFasGc5EN5r7WPE4SEAl4BRKJqspz4R2A4J";
        String OAuthAccessTokenSecret = "Xv557nOdyrLvOLcIldVhV6bGytHyLjnZMy2LS5pD6E";
        int tweet_count=0;
        MyTwitterAPI myTwitterAPI = new MyTwitterAPI();
        Twitter twitter = myTwitterAPI.ConnectTwitter(OAuthConsumerKey, OAuthConsumerSecret,
                OAuthAccessToken, OAuthAccessTokenSecret);
        NaiveBayes naiveBayesClassifier = new NaiveBayes();   
        LeaderFollowers lf = new LeaderFollowers(0.35, 0.4);
        CredibilityComponent credibility=new CredibilityComponent();
        NaiveBayesCategories bayesCategories=new NaiveBayesCategories();
       //Test Code
        
                    
        long file=0;
        while (true) {
            ArrayList<Tweet> tweets = myTwitterAPI.GetHomeTimeLine(twitter);
//            ArrayList<Tweet> tweets = myTwitterAPI.GetQueryResult(twitter, "عاجل");
//            ArrayList<Tweet> tweets = myTwitterAPI.GetQueryResult(twitter, "عن");
            naiveBayesClassifier.secondRunMain(tweets);            
            bayesCategories.secondRunMain(tweets);
            if(tweet_count > 0 && tweet_count % 1000 == 0) {
                lf.defragClusters_SingleLink();
            }
            tweet_count += tweets.size();
            // el tweetaya et7tt fi el cluster el folany
            Vector<Pair<Tweet, Integer>> v = lf.go_all(tweets);
            // tdeha el id tdek el cluster
            HashMap<Integer,Cluster> myclusters = lf.getClusters();
            credibility.SecondRun(v, myclusters, file);
            file++;
            Thread.sleep(60000);
        }
    }
}
