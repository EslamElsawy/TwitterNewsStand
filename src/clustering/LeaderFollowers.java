package clustering;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;
import TwitterNewsStand.Tweet;


public class LeaderFollowers {

    private double cos_threshold;
    private double merge_threshold;
    private double gauss_threshold;
    private Vector<Tweet> testset;
    private HashMap<Integer, Cluster> clusters;
    private HashMap<Long, Integer> twClusMapping;
//    private Vector<Cluster> clusters;
    private LuceneSimilarity sim;
    private static int id = 0;
    public static int count = 0;

    /**
     * Constructor for the leader followers algorithm
     *
     * @param cos_threshold
     * @param gauss_threshold
     */
    public LeaderFollowers(double cos_threshold, double gauss_threshold) {
        this.cos_threshold = cos_threshold;
        this.gauss_threshold = gauss_threshold;
        this.merge_threshold = cos_threshold - 0.1;
        this.testset = new Vector<Tweet>();
        this.clusters = new HashMap<Integer, Cluster>();
        this.twClusMapping = new HashMap<Long, Integer>();
        this.sim = new LuceneSimilarity();
    }

    /**
     * build all for online streams
     */
    public Vector<Pair<Tweet, Integer>> go_all(ArrayList<Tweet> tweets) {
        // If Retweeted (Update)
        System.out.println("Here");
        Vector<Pair<Tweet, Integer>> v = new Vector<Pair<Tweet, Integer>>();
        for(Tweet t : tweets) {
            Pair<Tweet, Integer> p = new Pair<Tweet, Integer>(t, twClusMapping.get(t.getTweetID()));
            if(twClusMapping.containsKey(t.getTweetID())) {
                System.out.println("Cluster ID = " + t.getCluID());
                p.setL(t);
                p.setR(t.getCluID());
                v.add(p);
                Vector<Tweet> set = clusters.get(t.getCluID()).getMembers();
                for(Tweet tw : set) {
                    if(tw.getTweetID() == t.getTweetID()) {
                        System.out.println("retweets = " + tw.getRetweetCount());
                        clusters.get(t.getCluID()).getMembers().get(set.indexOf(tw)).setRetweetCount(t.getRetweetCount());
                        clusters.get(t.getCluID()).getMembers().get(set.indexOf(tw)).setRetweetUsers(t.getRetweetUsers());
                        clusters.get(t.getCluID()).getMembers().get(set.indexOf(tw)).setRetweetDates(t.getRetweetDates());
                        System.out.println("retweets after = " + clusters.get(t.getCluID()).getMembers().get(set.indexOf(tw)));
                        break;
                    }
                }
                continue;
            }
            int id = go_tweet(t);
            p.setL(t);
            p.setR(id);
            v.add(p);
        }
        System.out.println("ID = " + id);
        System.out.println("Clusters NCount = " + clusters.size());
        return v;
    }
    
    /**
     * Cluster one tweet in the approach of Weighted similarity
     * @param t 
     */
    public int go_tweet(final Tweet t) {
        t.build_tweet_vector();
        double nearst_cluster = -1;
        int indexOfNearstCluster = -1;
        for (Integer idx : clusters.keySet()) {
            Cluster c = clusters.get(idx);
            long t_c = (long) Math.ceil(c.getMean());
            if (Math.abs(t_c - t.getDateLong()) > 86400000) {
                continue;
            }
            double sim = c.getCentroid().cosineSimilarity(t.getVector());
            if (sim > nearst_cluster) {
                nearst_cluster = sim;
                indexOfNearstCluster = idx;
            }
        }
        if (nearst_cluster < this.cos_threshold) {
            ++id;
            t.setCluID(id);
            Cluster c_j = new Cluster(t, id);
            this.clusters.put(id, c_j);
            indexOfNearstCluster = id;
        } else {
            t.setCluID(indexOfNearstCluster);
            this.clusters.get(indexOfNearstCluster).addmember(t);
        }
        twClusMapping.put(t.getTweetID(), indexOfNearstCluster);
        return indexOfNearstCluster;
    }

    /**
     * using lucene similarity with the tweet
     * @param t 
     */
    
    public void mergeClusters(BufferedWriter bw) throws IOException {
        for(int i = 0; i < this.clusters.size(); i++) {
            TweetVector c1 = this.clusters.get(i).getCentroid();
            for(int j = i + 1; j < this.clusters.size(); j++) {
                TweetVector c2 = this.clusters.get(j).getCentroid();
                double sim = c1.cosineSimilarity(c2);
                if (sim >= cos_threshold) {
//                    System.out.printf("Cluster %d, is merged with Cluster %d\n", i, j);
                    bw.write("Cluster " + i + " is merged with Cluster " + j + " \n");
                }
            }
        }
    }
    
    public HashMap<Integer, Cluster> getClusters() {
        return this.clusters;
    }
    
    public void defragClusters_SingleLink() {
        for(Integer i : clusters.keySet()) {
            TweetVector c1 = this.clusters.get(i).getCentroid();
            double max_sim = 0.0;
            int max_index = -1;
            for(Integer j : clusters.keySet()) {
                if(i == j)
                    continue;
                TweetVector c2 = this.clusters.get(j).getCentroid();
                double sim = c1.cosineSimilarity(c2);
                if(sim > max_sim) {
                    max_sim = sim;
                    max_index = j;
                }
            }
            if(max_sim >= gauss_threshold) {
                this.clusters.get(i).addMembers(this.clusters.get(max_index).getMembers());
                this.clusters.remove(max_index);
            }
        }
    }
    
    public void defragClusters_Update() {
        Vector<Integer> keySet = new Vector<Integer>(clusters.keySet());
        for(int i = 0; i < keySet.size(); i++) {
            Cluster master = clusters.get(keySet.get(i));
            TweetVector tv_master = master.getCentroid();
            for(int j = i + 1; j < keySet.size(); j++) {
                Cluster slave = clusters.get(keySet.get(j));
                TweetVector tv_slave = slave.getCentroid();
                double sim = tv_master.cosineSimilarity(tv_slave);
                if(sim >= gauss_threshold) {
                    for(Tweet tweet : slave.getMembers()) {
                        this.clusters.get(i).addmember(tweet);
                    }
                    clusters.remove(j);
                }
            }
        }
    }
    
    public void defragClusters() {
        Vector<Integer> keySet = new Vector<Integer>(clusters.keySet());
        for (int i = 0; i < keySet.size(); i++) {
            Cluster master = clusters.get(keySet.get(i));
            TweetVector tv_master = master.getCentroid();
            for (int j = i + 1; j < keySet.size(); j++) {
                Cluster slave = clusters.get(keySet.get(j));
                TweetVector tv_slave = slave.getCentroid();
                double sim = tv_master.cosineSimilarity(tv_slave);
                if (sim >= gauss_threshold) {
                    this.clusters.get(i).addMembers(slave.getMembers());
                    clusters.remove(j);
                }
            }
        }
    }
    
    
//    public static void main(String[] args) throws FileNotFoundException, IOException, ParseException {
//        BufferedReader in = new BufferedReader(new FileReader("ClassifiedNews.txt"));
//        BufferedWriter bw = new BufferedWriter(new FileWriter("ClusteredNews.out"));
//        LeaderFollowers lf = new LeaderFollowers(0.35, 0);
//        String time = "";
//        String orig = "";
//        String stem = "";
//        int i = 1;
//        while ((time = in.readLine()) != null) {
//            String s = new String(time.getBytes("ASCII"));
//            Date d;
//            d = new SimpleDateFormat("EE MMM dd hh:mm:ss z yyyy", Locale.ENGLISH).parse(s);
//            orig = in.readLine();
//            stem = in.readLine();
//            Tweet t = new Tweet(orig, stem, d, 1);
//            lf.go_tweet(t);
//            i++;
////            if(i % 1000 == 0) {
////                BufferedWriter bw_x = new BufferedWriter(new FileWriter("English Testing" + i / 1000));
////                lf.mergeClusters(bw_x);
////                bw_x.close();
////            }
//        }
//        i = 1;
//        System.out.println(lf.clusters.size());
//        for(Cluster c : lf.clusters) {
//            bw.write("C " + i + "  = " + c.getMembers().size() + "\n");
//            bw.write("Cluster " + i + "\n");
//            i++;
//            for(Tweet t : c.getMembers())
//                bw.write(t.getDate() + " " + t.getOriginal() + "\n");
//            bw.write("------------------------------------------------------------------------------\n");
//        }
//    }
    
}
