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
public class FileCleaner {

    public static ArrayList<Tweet> readAndCleanData(String inputFileName) throws IOException, ParseException {

        ArrayList<Tweet> tweets = removeRetweets(inputFileName);
        tweets = kareemMethod(tweets);
        tweets = postKareemCleaning(tweets);

        return tweets;
    }

    private static ArrayList<Tweet> removeRetweets(String inputFileName) throws IOException, ParseException {

        int tweetsCount = 0;
        int uniqueCount = 0;
        ArrayList<Tweet> tweets = new ArrayList<>();
        DateFormat df = new SimpleDateFormat("EEE MMM dd kk:mm:ss z yyyy", Locale.ENGLISH);
        HashMap<String, Integer> map = new HashMap<>();

        Document dom = null;
        // Make an  instance of the DocumentBuilderFactory
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            // use the factory to take an instance of the document builder
            DocumentBuilder db = dbf.newDocumentBuilder();
            // parse using the builder to get the DOM mapping of the    
            // XML file
            dom = db.parse(inputFileName);
            Element doc = dom.getDocumentElement();

            NodeList nl = doc.getElementsByTagName("Tweet");
            tweetsCount += nl.getLength();
            //loop over tweets
            for (int i = 0; i < nl.getLength(); i++) {
                NodeList childNodes = nl.item(i).getChildNodes();
                String text = null, user = null;
                Date date = null;
                long id = 0;
                ArrayList<String> links = new ArrayList<String>();
                ArrayList<String> mentions = new ArrayList<String>();
                ArrayList<String> hashTags = new ArrayList<String>();

                //loop over children
                for (int k = 0; k < childNodes.getLength(); k++) {
                    Node child = childNodes.item(k);
                    switch (child.getNodeName()) {
                        case "Text":
                            text = child.getFirstChild().getNodeValue();
                            break;
                        case "CreatedAt":
                            String time = child.getFirstChild().getNodeValue();
                            date = df.parse(time);
                            break;
                        case "User":
                            user = child.getFirstChild().getNodeValue();
                            break;
                        case "StatusID":
                            id = Long.parseLong(child.getFirstChild().getNodeValue());
                            break;
                        case "URLs":
                            NodeList urlsNodeList = child.getChildNodes();
                            for (int l = 0; l < urlsNodeList.getLength(); l++) {
                                Node urlNode = urlsNodeList.item(l);
                                if (urlNode.getNodeName().equals("URL")) {
                                    String s = urlNode.getFirstChild().getNodeValue();
                                    links.add(s);
                                }
                            }
                            break;
                        case "UserMentions":
                            NodeList userMentions = child.getChildNodes();
                            for (int l = 0; l < userMentions.getLength(); l++) {
                                Node userMentionNode = userMentions.item(l);
                                if (userMentionNode.getNodeName().equals("UserMention")) {
                                    String s = userMentionNode.getFirstChild().getNodeValue();
                                    mentions.add(s);
                                }
                            }
                            break;
                        case "HashTags":
                            NodeList hashTagsNodeList = child.getChildNodes();
                            for (int l = 0; l < hashTagsNodeList.getLength(); l++) {
                                Node hashTagNode = hashTagsNodeList.item(l);
                                if (hashTagNode.getNodeName().equals("HashTag")) {
                                    String s = hashTagNode.getFirstChild().getNodeValue();
                                    hashTags.add(s);
                                }
                            }
                            break;
                    }
                }

                //Clear Text from: RT @User:
                String first2Chars = text.substring(0, 2);
                boolean RT = false;
                if (first2Chars.equals("RT")) {
                    RT = true;
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

                if (map.containsKey(text)) {
                    int index = map.get(text);
                    Tweet t = tweets.get(index);
                    t.setRetweetCount(t.getRetweetCount() + 1);
                    t.addRetweetUser(user);
                    t.addRetweetDate(date);
                    tweets.set(index, t);
                } else {
                    map.put(text, uniqueCount);
                    tweets.add(new Tweet(text, date, user, id, links, hashTags, mentions, 0,0));
                    uniqueCount++;
                }
            }
        } catch (ParserConfigurationException pce) {
            System.out.println(pce.getMessage());
        } catch (SAXException se) {
            System.out.println(se.getMessage());
        } catch (IOException ioe) {
            System.err.println(ioe.getMessage());
        }

        System.out.println("Phase 1: Remove Retweets:\n"
                + "=========================\n");
        System.out.println("TweetsCount = " + tweetsCount);
        System.out.println("UniqueCount = " + uniqueCount);
        System.out.println("RetweetCount = " + (tweetsCount - uniqueCount));

        return tweets;
    }

    public static ArrayList<Tweet> kareemMethod(ArrayList<Tweet> tweets) throws FileNotFoundException, IOException {

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

        System.out.println("\nPhase 2: Remove mentions & urls:\n"
                + "================================\n");

        return tweets;
    }

    public static ArrayList<Tweet> postKareemCleaning(ArrayList<Tweet> inputTweets) throws FileNotFoundException, IOException {
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
