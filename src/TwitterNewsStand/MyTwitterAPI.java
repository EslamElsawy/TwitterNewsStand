package TwitterNewsStand;

import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;
import org.w3c.dom.*;
import org.xml.sax.*;
import twitter4j.HashtagEntity;
import twitter4j.HashtagEntity;
import twitter4j.MediaEntity;
import twitter4j.Query;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.QueryResult;
import twitter4j.ResponseList;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.TwitterFactory;
import twitter4j.URLEntity;
import twitter4j.URLEntity;
import twitter4j.User;
import twitter4j.User;
import twitter4j.UserMentionEntity;
import twitter4j.UserMentionEntity;
import twitter4j.conf.ConfigurationBuilder;

public class MyTwitterAPI {

    private Status lastStatus;
    private boolean firstLoop;
    private Cleaner cleaner;

    public MyTwitterAPI() {
        this.lastStatus = null;
        this.firstLoop = true;
        cleaner = new Cleaner();
    }

    public Twitter ConnectTwitter(String setOAuthConsumerKey,
            String setOAuthConsumerSecret,
            String setOAuthAccessToken,
            String setOAuthAccessTokenSecret) throws TwitterException {

        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setDebugEnabled(true)
                .setOAuthConsumerKey(setOAuthConsumerKey)
                .setOAuthConsumerSecret(setOAuthConsumerSecret)
                .setOAuthAccessToken(setOAuthAccessToken)
                .setOAuthAccessTokenSecret(setOAuthAccessTokenSecret);


        Twitter twitter = new TwitterFactory(cb.build()).getInstance();
        User user = twitter.verifyCredentials();
        System.out.println("Successfully verified credentials of " + user.getScreenName());
        return twitter;
    }

    public ArrayList<Tweet> GetQueryResult(Twitter twitter, String query) throws TwitterException {

        ArrayList<Tweet> tweets = new ArrayList<>();
        try {
            QueryResult result = twitter.search(new Query(query));
            List<Status> statuses = result.getTweets();

            for (Status status : statuses) {
                if (!firstLoop && lastStatus.getText().equals(status.getText())) {
                    break;
                }
                Tweet newTweet = CreateTweet(status);
                tweets.add(newTweet);
//                System.out.println(newTweet.getOriginalText());
            }

            firstLoop = false;
            lastStatus = statuses.get(0);
//            System.out.println("=======================================================================================");

        } catch (TwitterException te) {
            te.printStackTrace();
            System.out.println("Failed to verify credentials: " + te.getMessage());
            System.out.println("******Error:Reconnecting in 20 sec***********");
        }

//        System.out.println("Cleaned Tweets : \n=====================");
        ArrayList<Tweet> cleanedTweets = cleaner.cleanData(tweets);
        for (Tweet t : cleanedTweets) {
            System.out.println(t.getOriginalText());
        }
        System.out.println("=======================================================================================");

        return cleanedTweets;
    }

    public ArrayList<Tweet> GetHomeTimeLine(Twitter twitter) {

        ArrayList<Tweet> tweets = new ArrayList<>();
        try {
            ResponseList<Status> statuses = twitter.getHomeTimeline();

            for (Status status : statuses) {
                if (!firstLoop && lastStatus.getText().equals(status.getText())) {
                    break;
                }
                Tweet newTweet = CreateTweet(status);
                tweets.add(newTweet);
//                System.out.println(newTweet.getOriginalText());
            }

            firstLoop = false;
            lastStatus = statuses.get(0);
//            System.out.println("=======================================================================================");

        } catch (TwitterException te) {
            te.printStackTrace();
            System.out.println("Failed to verify credentials: " + te.getMessage());
            System.out.println("******Error:Reconnecting in 20 sec***********");
        }

//        System.out.println("Cleaned Tweets : \n=====================");
        ArrayList<Tweet> cleanedTweets = cleaner.cleanData(tweets);
        for (Tweet t : cleanedTweets) {
            System.out.println(t.getOriginalText());
        }
        System.out.println("=======================================================================================");

        return cleanedTweets;
    }

    private static Tweet CreateTweet(Status status) {

        //UserName
        String userName = status.getUser().getScreenName();

        //Text
        String text = status.getText();

        //CreatedAt
        Date date = status.getCreatedAt();

        //HashTags
        ArrayList<String> hashTagsArrayList = new ArrayList();
        HashtagEntity[] hashTagEntity = status.getHashtagEntities();
        for (int i = 0; i < hashTagEntity.length; i++) {
            hashTagsArrayList.add(hashTagEntity[i].getText());
        }

        //URLs
        ArrayList<String> urlsArrayList = new ArrayList();
        URLEntity[] urlEntity = status.getURLEntities();
        for (int i = 0; i < urlEntity.length; i++) {
            urlsArrayList.add(urlEntity[i].getURL().toString());
        }

        //Mentions
        ArrayList<String> mentionsArrayList = new ArrayList();
        UserMentionEntity[] userMentionEntity = status.getUserMentionEntities();
        for (int i = 0; i < userMentionEntity.length; i++) {
            mentionsArrayList.add(userMentionEntity[i].getScreenName());
        }

        //StatusID
        long tweetID = status.getId();

        //Create Tweet
        Tweet t = new Tweet(text, date, userName, tweetID, urlsArrayList, hashTagsArrayList, mentionsArrayList, 0,0);

        return t;
    }
}
