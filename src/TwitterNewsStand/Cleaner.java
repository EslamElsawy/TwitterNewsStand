package TwitterNewsStand;


import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author mresl_000
 */
public class Cleaner {

    private HashMap<String, Tweet> mapPhase1;

    public Cleaner() {
        mapPhase1 = new HashMap<>();
    }

    public ArrayList<Tweet> cleanData(ArrayList<Tweet> inputTweets) {

        ArrayList<Tweet> tweets = kareemMethod(inputTweets);
        tweets = removeRetweets(tweets);

        return tweets;
    }

    private ArrayList<Tweet> removeRetweets(ArrayList<Tweet> inputTweets) {

//        System.out.println("Phase 2: Remove Retweets:\n"
//                + "=========================\n");
        ArrayList<Tweet> tweets = new ArrayList<>();

        //loop over tweets
        for (int i = 0; i < inputTweets.size(); i++) {

            Tweet currTweet = inputTweets.get(i);
            String text = currTweet.getOriginalText();
            Date date = currTweet.getDate();
            String user = currTweet.getAccountName();

            //Clear Text from: RT @User:
            if (text.length() >= 2) {
                String first2Chars = text.substring(0, 2);

                if (first2Chars.equals("RT")) {
                    //search for 2 spaces
                    int s = 0, c = 0;//s is the index and c is a counter for spaces
                    for (s = 0; s < text.length(); s++) {
                        if (text.charAt(s) == 32) {
                            c++;
                        }
                        if (c == 2) {
                            break;
                        }
                    }
                    text = text.substring(s + 1);
                }
            }
            
            if(text.isEmpty()){
                continue;
            }

            currTweet.setOriginalText(text.trim());

            if (mapPhase1.containsKey(text)) {
                
                Tweet oldTweet = mapPhase1.get(text);
                oldTweet.setRetweetCount(oldTweet.getRetweetCount() + 1);
                oldTweet.addRetweetUser(user);
                oldTweet.addRetweetDate(date);
                tweets.add(oldTweet);
                //System.out.println("RetweetDetected :" + text+":"+oldTweet.getTweetID()+":"+oldTweet.getRetweetCount());
            } else {
                mapPhase1.put(text, currTweet);
                tweets.add(currTweet);
            }
        }

        return tweets;
    }

    private ArrayList<Tweet> kareemMethod(ArrayList<Tweet> tweets) {

//        System.out.println("\nPhase 1: Remove mentions & urls:\n"
//                + "================================\n");
        String regex = ".*(https?://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]).*";
        for (int i = 0; i < tweets.size(); i++) {

            Pattern pattern = Pattern.compile(regex);
            String input = tweets.get(i).getOriginalText();
            String modifiedText = "";
            StringTokenizer strtok = new StringTokenizer(input, " \t\n\f\r");
            String token = "";

            while (strtok.hasMoreTokens()) {
                token = strtok.nextToken().trim();
                Matcher m = pattern.matcher(token);
                if (!m.matches() && !token.contains("@")) {
                    // TODO: not link then print to file
                    modifiedText += (token + " ");
                } else {
                    if (m.matches()) {
                        token = token.replace(m.group(1), "");
                        modifiedText += (token + " ");
                    }
                }
            }
            tweets.get(i).setOriginalText(modifiedText.trim());
        }

        return tweets;
    }

    private ArrayList<Tweet> postKareemCleaning(ArrayList<Tweet> inputTweets) throws FileNotFoundException, IOException {
        System.out.println("\nPhase 3: Test text uniqness after removing mentions and urls:\n"
                + "================================\n");
        ArrayList<Tweet> outputTweets = new ArrayList<>();
        HashMap<String, Integer> map = new HashMap<>();
        int uniqueCount = 0;

        for (int i = 0; i < inputTweets.size(); i++) {
            Tweet t = inputTweets.get(i);
            if (map.containsKey(t.getOriginalText())) {
                int index = map.get(t.getOriginalText());
                Tweet temp = outputTweets.get(index);

                //modify the retweet count.
                temp.setRetweetCount(temp.getRetweetCount() + t.getRetweetCount());

                //modify the retweet users
                ArrayList<String> retweetUsers = t.getRetweetUsers();
                for (int j = 0; j < retweetUsers.size(); j++) {
                    temp.addRetweetUser(retweetUsers.get(j));
                }

                //modify the retweets dates
                ArrayList<Date> retweetDates = t.getRetweetDates();
                for (int j = 0; j < retweetDates.size(); j++) {
                    temp.addRetweetDate(retweetDates.get(j));
                }

                outputTweets.set(index, temp);
            } else {
                map.put(t.getOriginalText(), uniqueCount);
                outputTweets.add(t);
                uniqueCount++;
            }
        }

        System.out.println("TweetsCount = " + inputTweets.size());
        System.out.println("UniqueCount = " + outputTweets.size());
        System.out.println("RetweetCount = " + (inputTweets.size() - outputTweets.size()));

        return outputTweets;
    }
}
