package clustering;

import java.util.HashMap;
import java.util.Vector;
import TwitterNewsStand.Tweet;
import java.util.HashSet;

public class Cluster {

    private double mean;
    private double variance;
    private int prev_size;
    private Vector<Tweet> members;
    private TweetVector centroid;
    private HashMap<String, Integer> map;
    private boolean master;
    private int id;
    private double credibility;
    private String category;
    private HashSet<String> geoTag;
    private HashSet<String> nameEntities;

    /**
     * Constructor for the cluster
     * @param centroid 
     */
    public Cluster(Tweet centroid, int id) {
        this.members = new Vector<Tweet>();
        this.members.add(centroid);
        this.mean = centroid.getDateLong();
        this.id = id;
        this.variance = 0.0;
        this.prev_size = 1;
        this.centroid = new TweetVector(centroid.getType());
        this.map = new HashMap<String, Integer>();
        this.centroid.build_vector(centroid.getProcessedText());
        this.master = true;
        geoTag=new HashSet<>();
        nameEntities=new HashSet<>();
    }

    /**
     * 
     * @param t 
     */
    public void addmember(Tweet t) {
        this.members.add(t);
        this.centroid.build_vector(t.getProcessedText());
        updateVariance();
    }

    public int getID() {
        return id;
    }

    /**
     * update value of mean
     */
    private void updateMean() {
        double tmp = 0.0;
        for (Tweet t : members) {
            tmp += t.getDateLong();
        }
        tmp = tmp / (double) members.size();
        this.mean = tmp;
    }

    /**
     * update value of variance
     */
    private void updateVariance() {
        double variance = 0.0;
        updateMean();
        for (Tweet t : members) {
            variance += ((t.getDateLong() - this.mean) * (t.getDateLong() - this.mean));
        }
        variance /= members.size();
        this.variance = variance;
    }

    public Vector<Tweet> getMembers() {
        return this.members;
    }

    public double getMean() {
        return this.mean;
    }

    public TweetVector getCentroid() {
        return this.centroid;
    }

    public int preSize() {
        return this.prev_size;
    }

    public void setSize(int size) {
        this.prev_size = size;
    }

    public double getGrowth() {
        return (double) (this.members.size() - this.prev_size) / (double) this.prev_size;
    }

    public void updateMap(String label) {
        Integer oldCount = map.get(label);
        map.put(label, oldCount == null ? 1 : oldCount + 1);
    }

    public String getMax() {
        Integer max = 0;
        String label = "";
        for (String s : map.keySet()) {
            Integer x = map.get(s);
            if (x > max) {
                max = x;
                label = s;
            }
        }
        return label;
    }

    public void setMaster(boolean master) {
        this.master = master;
    }

    public void addMembers(Vector<Tweet> clus) {
        for (Tweet t : clus) {
            this.members.add(t);
        }
    }

    public boolean isMaster() {
        return this.master;
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
