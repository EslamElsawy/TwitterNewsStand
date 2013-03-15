package TwitterNewsStand;

import clustering.TweetVector;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;

public class Tweet implements Comparable<Tweet> {

    private String originalText;
    private String processedText;
    private Date date;
    private String accountName;
    private long tweetID;
    private ArrayList<String> links;
    private ArrayList<String> hashTags;
    private ArrayList<String> userMentions;
    private int retweetCount;
    private ArrayList<String> retweetUsers;
    private ArrayList<Date> retweetDates;
    private TweetVector tf_vector;
    private int tweet_type;
    private String label;
    private int clusterID;
    private double credibility = 0;
    private String category;
    private HashSet<String> geoTag;
    private HashSet<String> nameEntities;

    public Tweet(String stemmed, String label) {
        this.originalText = stemmed;
        this.processedText = stemmed;
        this.date = new Date(1353856143000L);
        this.tf_vector = new TweetVector(0);
        this.tweet_type = 0;
        this.label = label;
        geoTag=new HashSet<>();
        nameEntities=new HashSet<>();
    }

    public String getLabel() {
        return this.label;
    }

    public void setCluID(int id) {
        this.clusterID = id;
    }

    public int getCluID() {
        return this.clusterID;
    }

    public Tweet(String text, Date date, String user, long tweetID, ArrayList<String> links, ArrayList<String> hashTags, ArrayList<String> userMentions, int retweetCount, int tweet_type) {
        this.originalText = text;
        this.tweet_type = tweet_type;
        this.tf_vector = new TweetVector(this.tweet_type);
        this.date = date;
        this.accountName = user;
        this.tweetID = tweetID;
        this.links = links;
        this.hashTags = hashTags;
        this.userMentions = userMentions;
        this.retweetCount = retweetCount;
        retweetUsers = new ArrayList<>();
        retweetDates = new ArrayList<>();
        geoTag=new HashSet<>();
        nameEntities=new HashSet<>();
    }

    /**
     * used to sort tweets by date ... older first the newer
     * @param t
     * @return 
     */
    @Override
    public int compareTo(final Tweet t) {
        return (this.getDateLong() < t.getDateLong() ? -1 : (this.getDateLong() == t.getDateLong() ? 0 : 1));
    }

    public void build_tweet_vector() {
        this.tf_vector.build_vector(processedText);
    }

    public int getType() {
        return this.tweet_type;
    }

    public TweetVector getVector() {
        return this.tf_vector;
    }

    public ArrayList<String> getRetweetUsers() {
        return retweetUsers;
    }

    public void setRetweetUsers(ArrayList<String> retweetUsers) {
        this.retweetUsers = retweetUsers;
    }

    public ArrayList<Date> getRetweetDates() {
        return retweetDates;
    }

    public void setRetweetDates(ArrayList<Date> retweetDates) {
        this.retweetDates = retweetDates;
    }

    public String getOriginalText() {
        return originalText;
    }

    public void setOriginalText(String originalText) {
        this.originalText = originalText;
    }

    public String getProcessedText() {
        return processedText;
    }

    public void setProcessedText(String processedText) {
        this.processedText = processedText;
    }

    public long getDateLong() {
        return date.getTime();
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public long getTweetID() {
        return tweetID;
    }

    public void setTweetID(long tweetID) {
        this.tweetID = tweetID;
    }

    public ArrayList<String> getLinks() {
        return links;
    }

    public void setLinks(ArrayList<String> links) {
        this.links = links;
    }

    public ArrayList<String> getHashTags() {
        return hashTags;
    }

    public void setHashTags(ArrayList<String> hashTags) {
        this.hashTags = hashTags;
    }

    public ArrayList<String> getUserMentions() {
        return userMentions;
    }

    public void setUserMentions(ArrayList<String> userMentions) {
        this.userMentions = userMentions;
    }
    

    public int getRetweetCount() {
        return retweetCount;
    }

    public void setRetweetCount(int retweetCount) {
        this.retweetCount = retweetCount;
    }

    void addRetweetDate(Date date) {
        retweetDates.add(date);
    }

    void addRetweetUser(String user) {
        retweetUsers.add(user);
    }

    public void setCredibility(double cred) {
        this.credibility = cred;
    }

    public double getCredibility() {
        return credibility;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public HashSet<String> getGeoTag() {
        return geoTag;
    }

    public void setGeoTag(HashSet<String> geoTag) {
        this.geoTag = geoTag;
    }

    public HashSet<String> getNameEntities() {
        return nameEntities;
    }

    public void setNameEntities(HashSet<String> nameEntities) {
        this.nameEntities = nameEntities;
    }

   
    
    
}
