package clustering;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Stack;
import java.util.Vector;
import TwitterNewsStand.Tweet;

public class Evaluation {
    public static void main(String[] args) throws IOException {
        BufferedReader in = new BufferedReader(new FileReader("Defrag_4.out"));
        Vector<Cluster> v = new Vector<Cluster>();
        Vector<Vector<Tweet>> t = new Vector<Vector<Tweet>>();
        HashMap<String, Integer> map = new HashMap<String, Integer>();
        Vector<HashMap<String, Integer>> vm = new Vector<HashMap<String, Integer>>();
        String s = "";
        int i = 0, j = 0;
        Vector<Tweet> vector = new Vector<Tweet>();
        vm.add(new HashMap<String, Integer>());
        while((s = in.readLine()) != null) {
            if(s.equals("------------------------------------------------------------------------------")) {
//                System.out.println(vector.size());
                t.add(vector);
                vector = new Vector<Tweet>();
                vm.add(new HashMap<String, Integer>());
                continue;
            }
            i++;
            String label = s.substring(0, s.indexOf(" "));
            String str_tw = (s.substring(s.indexOf(" ") + 1)).trim();
            vector.add(new Tweet(str_tw, label));
            Integer oldCount = map.get(label);
            Integer oldCount_Internal = vm.get(vm.size() - 1).get(label);
            map.put(label, oldCount == null ? 1 : oldCount + 1);
            vm.get(vm.size() - 1).put(label, oldCount_Internal == null ? 1 : oldCount_Internal + 1);
        }
        t.add(vector);
        
        // Building Clusters
        int index = 0;
        for(Vector<Tweet> vt : t) {
            v.add(new Cluster(vt.firstElement(), index));
            for(int idx = 1; idx < vt.size(); idx++) {
                v.get(index).addmember(vt.get(idx));
            }
            index++;
        }
        
        System.out.println(v.size());
        //De-Fragmentation
        Vector<Cluster> vc = new Vector<Cluster>();
        
//        for(i = 0; i < v.size(); i++) {
//            Cluster master = v.get(i);
//            TweetVector tv_master = master.getCentroid();
//            for(j = i + 1; j < v.size(); j++) {
//                Cluster slave = v.get(j);
//                TweetVector tv_slave = slave.getCentroid();
//                double sim = tv_master.cosineSimilarity(tv_slave);
//                if(sim >= 0.35) {
//                    master.addMembers(slave.getMembers());
//                    v.remove(j);
//                }
//            }
//            vc.add(master);
//        }
        

//        for(i = 0; i < v.size(); i++) {
//            Cluster master = v.get(i);
//            TweetVector tv_master = master.getCentroid();
//            for(j = i + 1; j < v.size(); j++) {
//                Cluster slave = v.get(j);
//                TweetVector tv_slave = slave.getCentroid();
//                double sim = tv_master.cosineSimilarity(tv_slave);
//                if(sim >= 0.32) {
//                    for(Tweet tweet : slave.getMembers()) {
//                        master.addmember(tweet);
//                    }
//                    v.remove(j);
//                }
//            }
//            vc.add(master);
//        }
        
        System.out.println(vc.size());
        
        Vector<Vector<Tweet>> newClusters = new Vector<Vector<Tweet>>();
        for(Cluster clus : vc) {
            newClusters.add(clus.getMembers());
        }
        
        // percision
        double tp;
        double fn;
        double fp;
        
//        for(String x : map.keySet()) {
//            System.out.println(x + " " + map.get(x));
//        }
      
/**
 i
 * عدد ال elements اللي جوه Cluster موجود دلوقتي
 j
* و عدد ال elements اللي ليها نفس ال Class 
* k
و عدد ال elements اللي من نفس ال Class جوا نفس ال cluster
 * 
 * 
 * TP = max(k within the cluster)

FN = i - TP

FP = j - TP
* 
* TP = max(k in within cluster)
J = number of elements in class

that has maximum K
 * 
 */
        BufferedWriter bw = new BufferedWriter(new FileWriter("F-Measure4_1_After.out"));
        index = 0;
        double avg = 0.0;
        for(Vector<Tweet> tv : t) {
            i = tv.size();
            String maxLabel = "";
            int k = 0;
            for (String str : vm.get(index).keySet()) {
                int x = vm.get(index).get(str);
                if (x > k) {
                    k = x;
                    maxLabel = str;
                }
            }
            j = map.get(maxLabel);
            tp = k;
            fn = i - tp;
            fp = j - tp;
            double precision = tp / (tp + fp);
            double recall = tp / (tp + fn);
            double fmeasure = (2 * precision * recall) / (precision + recall);
            index++;
            avg += (fmeasure);
            bw.append("F-Measure = " + fmeasure + "\n");
        }
        bw.append("Overall F-Measure = " + (avg / vm.size()) + "\n");
        bw.close();
        
        
//        System.out.println(i);
//        Collections.shuffle(t);
//        LeaderFollowers lf = new LeaderFollowers(0.35, 0);
//        for(Tweet tw : t) {
//            lf.go_tweet(tw);
//        }
//        BufferedWriter bw = new BufferedWriter(new FileWriter("Purity.out"));
//        i = 1;
//        BufferedWriter bw2 = new BufferedWriter(new FileWriter("Defrag_4.out"));
////        System.out.println(lf.getClusters().size());
//        for(Cluster c : vc) {
//            for(Tweet tw : c.getMembers())
//                bw2.write(tw.getLabel() + " " + tw.getOriginal() + "\n");
//            bw2.write("------------------------------------------------------------------------------\n");
//        }
//        bw2.close();
//        double overAll = 0.0;
//        for(Vector<Tweet> vt : t) {
//            if(!vt.isEmpty()) {
//                double pur = purity(vt, vt.firstElement().getLabel());
//                j = map.get(vt.firstElement().getLabel());
//                double[] arr = tp2(vt, vt.firstElement().getLabel(), j);
//                tp = arr[0];
//                fn = arr[1];
//                fp = arr[2];
//                double f = (2 * fn * fp) / (fn + fp);
//                overAll += ((double)vt.size() / (double)i) * pur;
//                bw.write(pur + "   " + f + "\n");
//            }
//        }
//        bw.write("Over All Purity = " + overAll + "\n");
//        System.err.println(overAll);
//        bw.close();
    }
    
    public static double purity(Vector<Tweet> t, String label) {
        double size = t.size();
        double tp = 0.0;
        for(Tweet tw : t) {
            if(tw.getLabel().equals(label)) {
                tp++;
            }
        }
        return tp / size;
    }
    
    public static double[] tp2(Vector<Tweet> t, String label, int actual) {
        double size = t.size();
        double[] tp = new double[3];
        for(Tweet tw : t) {
            if(tw.getLabel().equals(label)) {
                tp[0]++;
            }
        }
        tp[1] = tp[0] / t.size();
        tp[2] = tp[0] / actual;
        return tp;
    }
}
