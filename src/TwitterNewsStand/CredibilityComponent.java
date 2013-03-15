package TwitterNewsStand;

import clustering.*;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;

public class CredibilityComponent {

    HashMap<String, Double> userRecords = new HashMap<>();
    HashMap<String, Integer> userContributions = new HashMap<>();
    HashMap<Long, Double> TweetScore = new HashMap<>();

    public CredibilityComponent() {
        loadSeeders();
    }

    public double getLinksWeight(Tweet tweet) {
        if (tweet.getLinks().size() != 0) {
            /*
             * here we need to make a similarity comparisons between link
             * content and tweet content
             */
            return 1.0;
        } else {
            return 0.0;
        }
    }

    public double getRetweetsNumberWeight(Tweet tweet) {
        if (tweet.getRetweetCount() >= 4) {
            return 1.0;
        } else {
            return (tweet.getRetweetCount() + 0.0) / 4.0;
        }
    }

    public double getSizeOfClusterWeight(int sizeOfCluster) {
        if (sizeOfCluster >= 2) {
            return 1.0;
        } else {
            return (sizeOfCluster + 0.0) / 2;
        }
    }

    public double getNewsSeedersWeight(Vector<Tweet> tweets) {
        double weight = 0;
        for (int i = 0; i < tweets.size(); i++) {
            /*
             * here we can check the presence of news seeders
             */
            if (tweets.get(i).getLinks().size() != 0) {
                weight += (1.0 / (tweets.size() + 0.0));
            }
        }
        return weight;
    }

    public void test(Vector<Tweet> tweets) throws IOException {
        BufferedWriter out = new BufferedWriter(new FileWriter(new File(
                "credibility.txt"), true));
        double val;
        for (int i = 0; i < tweets.size(); i++) {
            val = checkTweetCredibility(tweets.get(i), 0, tweets);
            out.write(tweets.get(i).getOriginalText() + " ,cre= " + val
                    + (val >= 0.5 ? ",Y" : ",N"));

            out.newLine();
            if (tweets.get(i).getLinks().size() != 0) {
                out.write("links = true, retweets= "
                        + tweets.get(i).getRetweetCount());
            } else {
                out.write("links = false, retweets= "
                        + tweets.get(i).getRetweetCount());
            }
            out.newLine();
        }
        Iterator<Entry<String, Double>> it;
        it = userRecords.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Double> pairs = (Map.Entry<String, Double>) it.next();
            out.write(pairs.getKey() + ":" + pairs.getValue());
            out.newLine();
        }
        out.close();
    }

//    public void test2(ArrayList<ArrayList<Tweet>> clusters,long file) throws IOException {
//        String name="cred "+file+".txt";
//        BufferedWriter out = new BufferedWriter(new FileWriter(new File(
//                name), true));
//        double val, clustCred = 0;
//        for (ArrayList<Tweet> list : clusters) {
//            clustCred = 0;
//            out.write("=====================================");
//            out.newLine();
//            for (int i = 0; i < list.size(); i++) {
//                val = checkTweetCredibility(list.get(i), list.size(), list);
//                clustCred += val;
//                out.write(list.get(i).getOriginalText() + " ,cre= " + val
//                        + (val >= 2.5 ? ",Y" : ",N"));
//                out.newLine();
//            }
//            if (clustCred / list.size() >= 2.5) {
//                out.write("Credible Cluster");
//            } else {
//                out.write("Uncredible Cluster");
//            }
//            out.newLine();
//        }
////        Iterator<Entry<String, Double>> it;
////        it = userRecords.entrySet().iterator();
////        while (it.hasNext()) {
////            Map.Entry<String, Double> pairs = (Map.Entry<String, Double>) it.next();
////            out.write(pairs.getKey() + ":" + pairs.getValue());
////            out.newLine();
////        }
////
//        out.close();
//    }
    public void checkClusterCredibility(Vector<Tweet> tweets) {
        double credibiltyMeasure = 0.0;
        double temp;
        for (int i = 0; i < tweets.size(); i++) {
            temp = checkTweetCredibility(tweets.get(i), tweets.size(), tweets);
            accumulateUserBehavior(tweets.get(i).getAccountName(), temp);
            for (int j = 0; tweets.get(i).getRetweetUsers() != null
                    && j < tweets.get(i).getRetweetUsers().size(); j++) {
                accumulateUserBehavior(tweets.get(i).getRetweetUsers().get(j),
                        temp);
            }
            if (temp >= 2.5) {
                credibiltyMeasure += temp;
            }
        }
        // if (credibiltyMeasure / (tweets.size() + 0.0) >= 2.5)
        // return true;
        // return false;

    }

    public double checkTweetCredibility(Tweet tweet, int sizeOfCluster,
            Vector<Tweet> tweets) {
        double temp = getLinksWeight(tweet) + getRetweetsNumberWeight(tweet)
                + getSizeOfClusterWeight(sizeOfCluster)
                + getNewsSeedersWeight(tweets) + checkUserTrustability(tweet);
        accumulateUserBehavior(tweet.getAccountName(), temp);
        for (int j = 0; tweet.getRetweetUsers() != null
                && j < tweet.getRetweetUsers().size(); j++) {
            accumulateUserBehavior(tweet.getRetweetUsers().get(j), temp);
        }
        return temp;

        // double temp = getLinksWeight(tweet) + getRetweetsNumberWeight(tweet)
        // + checkUserTrustability(tweet);
        // ;

    }

    public double checkUserTrustability(Tweet tweet) {
        if (userRecords.containsKey(tweet.getAccountName())) {
            return userRecords.get(tweet.getAccountName());
        }
        return 0;
    }

    public void accumulateUserBehavior(String userName,
            double credibilityMeasure) {
        if (credibilityMeasure >= 2.5) {
            if (!userRecords.containsKey(userName)) {
                userRecords.put(userName, 1.0);
                userContributions.put(userName, 1);
            } else {
                int trustedTweets = (int) (userRecords.get(userName) * userRecords.get(userName));
                userContributions.put(userName,
                        (userContributions.get(userName) + 1));
                if ((trustedTweets + 1.0) / (userContributions.get(userName) + 0.0) >= 1.0) {
                    userRecords.put(userName, 1.0);
                } else {
                    userRecords.put(userName, (trustedTweets + 1.0)
                            / (userContributions.get(userName) + 0.0));
                }
            }
        } else {
            if (!userRecords.containsKey(userName)) {
                userRecords.put(userName, 0.0);
                userContributions.put(userName, 1);
            } else {
                int trustedTweets = (int) (userRecords.get(userName) * userRecords.get(userName));
                userContributions.put(userName,
                        (userContributions.get(userName) + 1));
                if (trustedTweets > 0) {
                    userRecords.put(userName, (trustedTweets - 1.0)
                            / (userContributions.get(userName) + 0.0));
                } else {
                    userRecords.put(userName, 0.0);
                }
            }
        }

    }

    public void loadSeeders() {
        try {
            BufferedReader loader = new BufferedReader(new FileReader(
                    "TrustedSeeders.txt"));
            String line;
            String[] splitter;
            while ((line = loader.readLine()) != null) {
                splitter = line.split(" ");
                userRecords.put(splitter[0], Double.parseDouble(splitter[1]));
                userContributions.put(splitter[0], 0);
            }
        } catch (Exception e) {
        }
    }

//    public void SecondRun(Vector<Pair<Tweet, Integer>> v, HashMap<Integer, Cluster> myclusters, long file) throws IOException {
//        String name = "cred " + file + ".txt";
//        BufferedWriter out = new BufferedWriter(new FileWriter(new File(
//                name), true));
//        double val, clustCred = 0;
//        Cluster tempClus;
//        for (Pair<Tweet, Integer> tweet : v) {
//            clustCred = 0;
//            out.write("=====================================");
//            out.newLine();
//            tempClus = myclusters.get(tweet.getR());
//            val = checkTweetCredibility(tweet.getL(), tempClus.getMembers().size(), tempClus.getMembers());
//            TweetScore.put(tweet.getL().getTweetID(), val);
//            for (int i = 0; i < tempClus.getMembers().size(); i++) {
//                if (TweetScore.containsKey(tempClus.getMembers().get(i).getTweetID())) {
//                    val = TweetScore.get(tempClus.getMembers().get(i).getTweetID());
//                }
//                else{
//                    val=checkTweetCredibility(tempClus.getMembers().get(i), tempClus.getMembers().size(), tempClus.getMembers());
//                    TweetScore.put(tempClus.getMembers().get(i).getTweetID(), val);
//                 }
//
//                clustCred += val;
//                out.write(tempClus.getMembers().get(i).getOriginalText() + " ,cre= " + val
//                        + (val >= 2.5 ? ",Y" : ",N"));
//                out.newLine();
//            }
//            if (clustCred / tempClus.getMembers().size() >= 2.5) {
//                out.write("Credible Cluster");
//            } else {
//                out.write("Uncredible Cluster");
//            }
//            out.newLine();
//
//
//        }
//        out.close();
//    }
    public void SecondRun(Vector<Pair<Tweet, Integer>> v, HashMap<Integer, Cluster> myclusters, long file) throws IOException {
        String name = "cred " + file + ".txt";
        BufferedWriter out = new BufferedWriter(new FileWriter(new File(
                name), true));
        double val, clustCred = 0;
        Cluster tempClus;
        for (Pair<Tweet, Integer> tweet : v) {
            clustCred = 0;
            tempClus = myclusters.get(tweet.getR());
            val = checkTweetCredibility(tweet.getL(), tempClus.getMembers().size(), tempClus.getMembers());
            TweetScore.put(tweet.getL().getTweetID(), val);
            for (int i = 0; i < tempClus.getMembers().size(); i++) {
                if (!TweetScore.containsKey(tempClus.getMembers().get(i).getTweetID())) {
                    val = checkTweetCredibility(tempClus.getMembers().get(i), tempClus.getMembers().size(), tempClus.getMembers());
                    TweetScore.put(tempClus.getMembers().get(i).getTweetID(), val);

                }
            }
        }
        Iterator<Entry<Integer, Cluster>> it = myclusters.entrySet().iterator();
        Map.Entry<Integer, Cluster> pairs;
        while (it.hasNext()) {
            pairs = (Map.Entry<Integer, Cluster>) it.next();
            tempClus = pairs.getValue();
            clustCred = 0;
            out.write("=============================");
            out.newLine();
            for (Tweet tweet : tempClus.getMembers()) {
                val = TweetScore.get(tweet.getTweetID());
                clustCred += val;
                out.write(tweet.getOriginalText() + " ,cre= " + val
                        + (val >= 2.5 ? ",Y, ret= "+tweet.getRetweetCount() : ",N, ret="+tweet.getRetweetCount()));
                out.newLine();
            }
            if (clustCred / tempClus.getMembers().size() >= 1.25) {
                out.write("Credible Cluster");
            } else {
                out.write("Uncredible Cluster");
            }
            out.newLine();
        }
        out.close();
    }
}