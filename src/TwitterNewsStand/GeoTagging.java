package TwitterNewsStand;

import clustering.Cluster;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import info.bliki.api.User;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Vector;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author mresl_000
 */
public class GeoTagging {

    /**
     * @param args the command line arguments
     */
    public static int cacheHitCounter = 0;
    public static int requestWikiCounter = 0;
    public static HashMap<String, String> StructureVSTag = new HashMap<>();
    public static ArrayList<String> structureCount = new ArrayList<>();
    private MaxentTagger taggerPOS;
    private BufferedWriter outLog;
    private User wikiUser;
    private ArrayList<String> LocationDataBase;
    private ArrayList<Character> arabicCharacters;
    private String tahweelWord;
    private String wawLetter;
    private String ba2Letter;
    private HashMap<String, PriorityQueue<UserData>> BronzeMap;
    private ArrayList<String> wikiIndexersNamesPolitics;
    private ArrayList<String> wikiIndexersNamesSports;
    private String wikipediaDataSetDirectory;
    private ArrayList<ArrayList<String>> politicsTitlesDatabases;
    private ArrayList<ArrayList<String>> politicsLocationDatabases;
    private ArrayList<ArrayList<String>> sportsTitlesDatabases;
    private ArrayList<ArrayList<String>> sportsLocationDatabases;
    private NationalityWikiIndexer nationalityWikiIndexer;

    public GeoTagging() throws IOException, ClassNotFoundException {

        //****************POS tagger
        taggerPOS = new MaxentTagger("C:\\Forth Year\\Graduation project\\GeoTaggin paper\\Stanford\\stanfo"
                + "rd-postagger-full-2012-11-11\\stanford-postagger-f"
                + "ull-2012-11-11\\models\\arabic-accurate.tagger");


        //************Logging
        outLog = new BufferedWriter(new PrintWriter(new File("C:\\Users\\mresl_000\\Documents\\NetBean"
                + "sProjects\\DataBaseTwitter\\Statistics\\POS\\POSStanford2.txt")));

        //Wiki
        wikiUser = new User("eslamashrafelsawy", "eslam1990", "http://ar.wikipedia.org/w/api.php");
        wikiUser.login();

        //load locationdataset
        BufferedReader locReader = new BufferedReader(new InputStreamReader(new FileInputStream("LocationDataSet.txt"), "UTF-8"));
        LocationDataBase = new ArrayList<>();
        String locIn = "";
        while ((locIn = locReader.readLine()) != null) {
            LocationDataBase.add(normalizeText(locIn.trim()));
        }

        //load Arabic Characters
        arabicCharacters = new ArrayList<>();
        HashMap<Character, Character> map = initializeMap();
        for (Character c : map.values()) {
            arabicCharacters.add(c);
        }

        //load special characters and words
        BufferedReader in = null;
        try {
            FileInputStream fis = new FileInputStream("specialWords.txt");
            InputStreamReader isr = null;
            isr = new InputStreamReader(fis, "UTF-16");
            in = new BufferedReader(isr);
        } catch (Exception e) {
            System.out.println("Exception");
        }
        String[] splitted = in.readLine().split(" ");
        ba2Letter = splitted[0];
        tahweelWord = splitted[1];
        wawLetter = splitted[2];
        System.out.println(ba2Letter);
        System.out.println(tahweelWord);

        //bronze Map
        BronzeMap = new HashMap<>();

        //loading wikipedia indexes
        wikipediaDataSetDirectory = "C:\\Users\\mresl_000\\Doc"
                + "uments\\NetBeansProjects\\Twitter4jWikipedia\\WikipediaDataSet";
        wikiIndexersNamesPolitics = new ArrayList<>();
        wikiIndexersNamesSports = new ArrayList<>();
        File dir = new File(wikipediaDataSetDirectory);
        for (String indexFolderName : dir.list()) {
            //politics
            if (!indexFolderName.equals("سياسيون حسب الجنسية_حقوقيون_مدونون_رؤساءتحرير_محامون_اعلاميون_صحفيون_ناشطون_قضاة_شخصيات_دينية")
                    && !indexFolderName.equals("أحزاب حسب البلد")) {
                continue;
            }
            wikiIndexersNamesPolitics.add(indexFolderName);
        }
        for (String indexFolderName : dir.list()) {
            //Sports
            if (!indexFolderName.equals("رياضيون_حسب_الجنسية_لاعبوكرةقدم") && !indexFolderName.equals("نوادي")) {
                continue;
            }
            wikiIndexersNamesSports.add(indexFolderName);
        }


        //loading wikipedia titles databases politics
        System.out.println("Start politics Databases Loading");
        politicsTitlesDatabases = new ArrayList<ArrayList<String>>();
        politicsLocationDatabases = new ArrayList<ArrayList<String>>();
        for (String database : wikiIndexersNamesPolitics) {
            ArrayList<String> currTitleList = new ArrayList<>();
            ArrayList<String> currLocList = new ArrayList<>();
            File dir2 = new File(wikipediaDataSetDirectory + "\\" + database + "\\Titles");
            for (String fileName : dir2.list()) {
                //reading title
                BufferedReader titleInBuffer = new BufferedReader(
                        new InputStreamReader(new FileInputStream(
                        wikipediaDataSetDirectory + "\\" + database + "\\Titles\\" + fileName)));
                String title = titleInBuffer.readLine();
                currTitleList.add(title);

                //reading loc
                BufferedReader locBuffer = new BufferedReader(
                        new InputStreamReader(new FileInputStream(
                        wikipediaDataSetDirectory + "\\" + database + "\\Locations\\" + fileName)));
                String loc = locBuffer.readLine();
                currLocList.add(loc);

            }
            politicsTitlesDatabases.add(currTitleList);
            politicsLocationDatabases.add(currLocList);
        }
        System.out.println("End politics Databases Loading\n");

        //loading wikipedia titles databases sports
        System.out.println("Start sports Databases Loading");
        sportsTitlesDatabases = new ArrayList<ArrayList<String>>();
        sportsLocationDatabases = new ArrayList<ArrayList<String>>();
        for (String database : wikiIndexersNamesSports) {
            ArrayList<String> currTitleList = new ArrayList<>();
            ArrayList<String> currLocList = new ArrayList<>();
            File dir2 = new File(wikipediaDataSetDirectory + "\\" + database + "\\Titles");
            for (String fileName : dir2.list()) {
                //reading title
                BufferedReader titleInBuffer = new BufferedReader(
                        new InputStreamReader(new FileInputStream(
                        wikipediaDataSetDirectory + "\\" + database + "\\Titles\\" + fileName)));
                String title = titleInBuffer.readLine();
                currTitleList.add(title);

                //reading loc
                BufferedReader locBuffer = new BufferedReader(
                        new InputStreamReader(new FileInputStream(
                        wikipediaDataSetDirectory + "\\" + database + "\\Locations\\" + fileName)));
                String loc = locBuffer.readLine();
                currLocList.add(loc);

            }
            sportsTitlesDatabases.add(currTitleList);
            sportsLocationDatabases.add(currLocList);
        }
        System.out.println("End sports Databases Loading");

        //nationality wiki indexer
        nationalityWikiIndexer = new NationalityWikiIndexer("WikiIndex");

    }

    public static void main(String[] args) throws IOException, ClassNotFoundException, ParseException, Exception {
//
//        //Load Data
//        String inputFileName = "C:\\Users\\mresl_000\\Documents\\NetBeans"
//                + "Projects\\DataBaseTwitter\\Statistics\\ArabicNew"
//                + "sProviders\\All_ArabicNewsProviders_2.9.xml";
//        ArrayList<Tweet> tweets = new Cleaner().readAndCleanData(inputFileName);
//
//        //Create Tagger
//        GeoTagging myGeoTagger = new GeoTagging();
//
//        //Loop
//        Random generator = new Random();
//        int falseCounter = 0;
//        int trueCounter = 0;
//        for (int i = 0; i < 10000; i++) {
////            int rand = generator.nextInt(tweets.size());
////            System.out.println(i);
//
//            //GeoTagging
//            Tweet currT = tweets.get(i);
//            //Sports
////            if (!currT.getAccountName().equals("Yallakora") && !currT.getAccountName().equals("AljazeeraSports")) {
////                continue;
////            }
//            //politics
//            if (!currT.getAccountName().equals("Masrawy")) {
//                continue;
//            }
////            String category = "politics.txt";
//            String category = "sports.txt";
//            HashSet<String> results = myGeoTagger.geoTagTweet(currT, category);
//            if (results.size() == 0) {
//                falseCounter++;
//            } else {
//                trueCounter++;
//            }
//            //log results
//            myGeoTagger.getOutLog().append("***********************Final Results***************\n");
////            ArrayList<GeoTagSorter> sorterList = new ArrayList<>();
////            for (Map.Entry entry : results.entrySet()) {
////                sorterList.add(new GeoTagSorter(((GeoTagLabel) entry.getKey()).word,
////                        ((GeoTagLabel) entry.getKey()).label, (int) entry.getValue()));
////            }
////            Collections.sort(sorterList, new GeoTagSorterComparator());
////            for (GeoTagSorter e : sorterList) {
////                myGeoTagger.getOutLog().append((e.label == 1 ? "GOLD" : "SILVER") + " : "
////                        + (e).word + " : " + e.count + "\n");
////            }
//            myGeoTagger.getOutLog().append("====================END OF TWEET============================\n");
//
//        }//END of for loop
//        System.out.println("NotGeoTagged" + falseCounter);
//        System.out.println("GeoTagged" + trueCounter);
//        myGeoTagger.getOutLog().close();
//
//
//        //logging the important structures
//        PriorityQueue<UserData> pqStructures = getTopFive(structureCount);
//        //print top hundred
//        int hundered = 100;
//        while (!pqStructures.isEmpty() && hundered-- > 0) {
//            UserData u = pqStructures.poll();
//            System.out.println(u.userName + " : " + u.count);
//        }
//
//        //loggint the bronze Map
//        for (Map.Entry<String, PriorityQueue<UserData>> e : myGeoTagger.BronzeMap.entrySet()) {
//            System.out.println("->" + e.getKey() + " : " + e.getValue().peek().userName + " count : " + e.getValue().peek().count);
//        }
//        System.out.println("BronzeMapSize" + myGeoTagger.BronzeMap.size());
//
    }

    public BufferedWriter getOutLog() {
        return outLog;
    }

    public HashSet<String> geoTagTweet(Tweet tweet, String category) throws IOException, ParseException {
//        Comparator<GeoTagLabel> geoComparator = new GeoTagLabelComparator();
        HashMap<GeoTagLabel, Integer> geoTagMap = new HashMap<>();
        String text = tweet.getOriginalText();
        text = Clean(text, arabicCharacters).trim();
        if (text.isEmpty()) {
            return null;
        }

        outLog.append(text + "\n");

        //Tag Tweet
//        ArrayList<String> tweetNEOne = POSTagOne(taggerPOS, text, outLog, true, wawLetter);
        ArrayList<String> tweetNEOne = POSTagOne2(taggerPOS, text, outLog, wawLetter, ba2Letter);
        ArrayList<String> tweetAdj = POSTagAdj(taggerPOS, text, outLog, wawLetter, ba2Letter);
        //ArrayList<String> tweetNETwo = POSTagTwo(tagger, text, out, true);
        //ArrayList<String> tweetNEThree = POSTagThree(tagger, text, out, true);


        //print POS results
        outLog.append("\t\t****POS results****\n");
        //printList
        outLog.append("Selected Structures:\n");
        for (String s : tweetNEOne) {
            outLog.append(s + "\n");
        }
        //out.append("twos:\n");
//            for (String s : tweetNETwo) {
//                out.append(s + "\n");
//            }
//            out.append("threes:\n");
//            for (String s : tweetNEThree) {
//                out.append(s + "\n");
//            }

        ArrayList<ArrayList<String>> loop = new ArrayList<ArrayList<String>>();
        loop.add(tweetNEOne);
//            loop.add(tweetNETwo);
//            loop.add(tweetNEThree);

        //Array list containing all locations wiht trust 100%, gold, adj, exact matches queries.
        HashSet<String> sureLocations = new HashSet<>();

        //Phase One Gold Tag
        ArrayList<String> goldTags = new ArrayList<>();
        outLog.append("\t\t****Gold Tags*****\t\t\n");
        for (String s : tweetNEOne) {
            s = normalizeText(s);
            if (LocationDataBase.contains(s)) {
                goldTags.add(s);
                sureLocations.add(s);
                InsertInsideGeoTagMap(geoTagMap, new GeoTagLabel(s, GeoTagLabel.GOLD), 1);
                structureCount.add(StructureVSTag.get(s));
                outLog.append("Gold -> " + s + "\n");
            }
        }

        //updage bronze structure with gold tags
//        if (goldTags.isEmpty()) {
//            outLog.append("\t\t****Bronze Tags*****\t\t\n");
//            HashMap<String, String> bronzeRet = GetBronzeTags(tweetNEOne);
//            for (Map.Entry<String, String> e : bronzeRet.entrySet()) {
//                outLog.append("Bronze -> " + e.getKey() + " : " + e.getValue());
//            }
//        } else {
//            UpdateBronzeMap(tweetNEOne, goldTags);
//        }

        //Phase 2 adjectives
        outLog.append("\t\t****Adjectives results****\n");
        for (String adj : tweetAdj) {
            outLog.append("Query -> :" + adj + "\n");
            String[] titles = nationalityWikiIndexer.searchTitle(adj, "WikiIndex");
            String[] articles = nationalityWikiIndexer.searchArticle(adj, "WikiIndex");
//            System.out.println("Countries Match");
//            for (String s : titles) {
//                BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(nationalityDirectory + "\\Country\\" + s)));
//                System.out.println(in.readLine());
//            }
            outLog.append("Nationalities Match:");
            if (articles.length != 0) {
                String natFileName = articles[0];
                //get nationality
                BufferedReader inNat = new BufferedReader(new InputStreamReader(new FileInputStream("C:\\Users\\mresl_000\\Documents\\NetB"
                        + "eansProjects\\Twitter4jWikipedia\\Nationality" + "\\Nationality\\" + natFileName)));
                //get country
                BufferedReader inCount = new BufferedReader(new InputStreamReader(new FileInputStream("C:\\Users\\mresl_000\\Documents\\NetB"
                        + "eansProjects\\Twitter4jWikipedia\\Nationality" + "\\Country\\" + natFileName)));

                String currNat = inNat.readLine();
                String currCountr = inCount.readLine();
                outLog.append(currNat + ":" + currCountr);
                sureLocations.add(currCountr);
            }
            outLog.append("\n");
        }


        //go to wiki pedia
        outLog.append("\t\t****wikipedia results****\n");
        ArrayList<UserData> silverTags = new ArrayList<>();
        for (ArrayList<String> tempList : loop) {

            for (String queryWord : tempList) {
                queryWord = queryWord.trim();
                if (queryWord.isEmpty()) {
                    continue;
                }
                //skip  query if labeled as gold
                if (goldTags.contains(normalizeText(queryWord))) {
                    continue;
                }

                //use query
                ArrayList<String> queries = new ArrayList<>();
                queries.add(queryWord);
                for (String query : queries) {
                    outLog.append("\n==================="
                            + "==================================\n=\t\t->********Query: " + query + "********");

                    //loop through databases
                    ArrayList<String> wikiIndexerName = (category.equals("politics.txt")) ? wikiIndexersNamesPolitics : wikiIndexersNamesSports;
                    ArrayList<ArrayList<String>> titlesDatabases = (category.equals("politics.txt")) ? politicsTitlesDatabases : sportsTitlesDatabases;
                    ArrayList<ArrayList<String>> locationDatabases = (category.equals("politics.txt")) ? politicsLocationDatabases : sportsLocationDatabases;
                    ArrayList<DatabaseResult> allQueryMatches = new ArrayList<>();
                    for (int i = 0; i < wikiIndexerName.size(); i++) {
                        ArrayList<String> currDatabase = titlesDatabases.get(i);
                        ArrayList<String> currLocDatabase = locationDatabases.get(i);
                        String currDatabaseName = wikiIndexerName.get(i);
                        ArrayList<DatabaseResult> titlesMatch = getExactTitleMatches(query, currDatabase, currLocDatabase);
                        allQueryMatches.addAll(titlesMatch);
                        if (titlesMatch.isEmpty()) {
                            continue;
                        }
                        outLog.append("\nSearching In: " + currDatabaseName + "\n===============\n");

                        for (int c = 0; c < 10 && c < titlesMatch.size(); c++) {
                            DatabaseResult tit = titlesMatch.get(c);
                            outLog.append(tit.getTitle() + ":" + tit.getLocation() + ":" + tit.getWeight() + "\n");
                        }
                        outLog.append("\n");
                    }
                    outLog.append("\n");

                    //check exact match
                    outLog.append("\t\t*****Resolution Algorithm******\n");
                    if (allQueryMatches.size() == 1) {
                        outLog.append("Congrats Exact Match\n");
                        sureLocations.add(allQueryMatches.get(0).getLocation());
                    } else if (allQueryMatches.isEmpty()) { //no single matches at all
                        continue;
                    } else {//work on resolution algorithm
                        outLog.append("Step1:Resolve by weight\n");
                        ArrayList<DatabaseResult> resolvedByWeight = resolveByWeight(allQueryMatches);
                        if (resolvedByWeight.size() == 1) {
                            outLog.append("Succesful reolving by wieht\n");
                            DatabaseResult unique = resolvedByWeight.get(0);
                            outLog.append(unique.getTitle() + ":" + unique.getLocation() + ":" + unique.getWeight() + "\n");
                            sureLocations.add(unique.getLocation());
                        } else if (resolvedByWeight.isEmpty()) {
                            outLog.append("Error resolving by wieht returnSize=0\n");
                        } else {
                            //print resolved by weight
                            for (DatabaseResult currResult : resolvedByWeight) {
                                outLog.append(currResult.getTitle() + ":" + currResult.getLocation() + ":" + currResult.getWeight() + "\n");
                            }
                            //work on resolving by location
                            outLog.append("Step2:Resolve by Location\n");
                            ArrayList<DatabaseResult> resolvedByLocation = resolveByLocation(resolvedByWeight, sureLocations);
                            for (DatabaseResult currResult : resolvedByLocation) {
                                outLog.append(currResult.getTitle() + ":" + currResult.getLocation() + ":" + currResult.getWeight() + "\n");
                            }
                            if (resolvedByLocation.size() == 0) {
                                outLog.append("Error resolving by location-> size:0" + "\n");
                            } else if (resolvedByLocation.size() == 1) {
                                outLog.append("Succesful reolving by loc\n");
                                sureLocations.add(resolvedByLocation.get(0).getLocation());
                            } else {
                                outLog.append("Error resolving by location-> size:"
                                        + resolvedByLocation.size() + "\n");
                            }
                        }
                    }

                }
            }
        }
        //adding gold tags to important structures

        return sureLocations;
    }

    private ArrayList<DatabaseResult> resolveByLocation(ArrayList<DatabaseResult> inputList, HashSet<String> sureLocations) {
        if (sureLocations.isEmpty()) {
            return inputList;
        }
        ArrayList<DatabaseResult> ret = new ArrayList<DatabaseResult>();
        for (DatabaseResult db : inputList) {
            if (sureLocations.contains(db.getLocation())) {
                ret.add(db);
            }
        }
        return ret;
    }

    private ArrayList<DatabaseResult> resolveByWeight(ArrayList<DatabaseResult> inputList) {
        ArrayList<DatabaseResult> ret = new ArrayList<DatabaseResult>();
        double max = 0;
        for (DatabaseResult db : inputList) {
            max = Math.max(max, db.getWeight());
        }
        for (DatabaseResult db : inputList) {
            if (Math.abs(db.getWeight() - max) < 0.0001) {
                ret.add(db);
            }
        }

        return ret;
    }

    private ArrayList<DatabaseResult> getExactTitleMatches(String query, ArrayList<String> currDatabase, ArrayList<String> currLocDatabase) {
        ArrayList<DatabaseResult> match = new ArrayList<>();
        String[] splitQuery = query.trim().split(" +");
        int queryLength = splitQuery.length;

        for (int m = 0; m < currDatabase.size(); m++) {
            String record = currDatabase.get(m);
            String loc = currLocDatabase.get(m);
            String[] splitRecord = record.trim().split(" +");
            int recordLength = splitRecord.length;
            for (int i = 0; i + queryLength <= recordLength; i++) {
                int k = 0;
                boolean isMatch = true;
                for (int j = i; j < i + queryLength; j++, k++) {
                    if (!splitQuery[k].equals(splitRecord[j])) {
                        isMatch = false;
                        break;
                    }
                }
                double wieght = (splitQuery.length * 1.0) / (splitRecord.length * 1.0);
                if (isMatch) {
                    match.add(new DatabaseResult(0, record, loc, wieght));
                }
            }
        }

        return match;
    }

    private static HashMap<Character, Character> initializeMap() {

        HashMap<Character, Character> map = new HashMap();
        map.put('\'', '\u0621');
        map.put('|', '\u0622');
        map.put('O', '\u0623');
        map.put('W', '\u0624');
        map.put('I', '\u0625');
        map.put('}', '\u0626');
        map.put('A', '\u0627');
        map.put('b', '\u0628');
        map.put('p', '\u0629');
        map.put('t', '\u062A');


        map.put('v', '\u062B');
        map.put('j', '\u062C');
        map.put('H', '\u062D');
        map.put('x', '\u062E');
        map.put('d', '\u062F');
        map.put('*', '\u0630');
        map.put('r', '\u0631');
        map.put('z', '\u0632');
        map.put('s', '\u0633');


        map.put('$', '\u0634');
        map.put('S', '\u0635');
        map.put('D', '\u0636');
        map.put('T', '\u0637');
        map.put('Z', '\u0638');
        map.put('E', '\u0639');
        map.put('g', '\u063A');
        map.put('_', '\u0640');
        map.put('f', '\u0641');


        map.put('q', '\u0642');
        map.put('k', '\u0643');
        map.put('l', '\u0644');
        map.put('m', '\u0645');
        map.put('n', '\u0646');
        map.put('h', '\u0647');
        map.put('w', '\u0648');
        map.put('Y', '\u0649');
        map.put('y', '\u064A');
        map.put('F', '\u064B');
        map.put('N', '\u064C');
        map.put('K', '\u064D');
        map.put('a', '\u064E');
        map.put('u', '\u064F');

        map.put('i', '\u0650');
        map.put('~', '\u0651');
        map.put('o', '\u0652');
        map.put('`', '\u0670');
        map.put('{', '\u0671');
        map.put('P', '\u067E');
        map.put('J', '\u0686');
        map.put('V', '\u06A4');
        map.put('G', '\u06AF');
        map.put(' ', ' ');
        map.put('<', '\u0625');
        map.put('>', '\u0623');
        map.put('&', '\u0624');
        return map;
    }

    private static String Clean(String input, ArrayList<Character> arabicChars) {
        String ret = "";
        for (int i = 0; i < input.length(); i++) {
            if (arabicChars.contains(input.charAt(i))) {
                ret += input.charAt(i);
            } else {
                ret += ' ';
            }
        }

        String[] splitted = ret.split(" +");
        String ret2 = "";
        for (int i = 0; i < splitted.length; i++) {
            ret2 += splitted[i] + " ";
        }

        return ret2.trim();
    }

//    public static String getWikiArticle(String query, User user, String tahweel) {
//        StringBuilder sb = new StringBuilder("");
//        String[] listOfTitleStrings = {query};
//        List<Page> listOfPages = user.queryContent(listOfTitleStrings);
//
//        for (Page page : listOfPages) {
//
//            MyWikiModel wikiModel = new MyWikiModel("${image}", "${title}");
//            String currentContent = page.getCurrentContent();
//
//            if (currentContent.startsWith("#" + tahweel)) {
//                String newq = "";
//                int rIndex = currentContent.indexOf(tahweel);
//                while (currentContent.charAt(rIndex) != '[' && rIndex < currentContent.length()) {
//                    rIndex++;
//                }
//                rIndex += 2;
//                while (currentContent.charAt(rIndex) != ']' && rIndex < currentContent.length()) {
//                    newq += currentContent.charAt(rIndex);
//                    rIndex++;
//                }
//                System.out.println("Redirection from: " + query + " to: " + newq);
//                return getWikiArticle(newq, user, tahweel);
//            }
//            String html = wikiModel.render(new MyHtmlConverter(true, true), currentContent);
//            sb.append(html + "\n");
//        }
//
//        return query + "," + sb.toString();
//    }
    private static String saveArticle(String newQuery, String article) throws IOException {
        BufferedWriter out = new BufferedWriter(new FileWriter(new File("C:\\Users\\mresl_000\\Documents\\NetBeans"
                + "Projects\\DataBaseTwitter\\Statistics\\POS\\Wiki\\" + newQuery + ".txt")));
        out.append(article);
        out.close();
        return article;
    }

    private static String searchDownloadedWiki(String query) throws FileNotFoundException, IOException {

        File dir = new File("C:\\Users\\mresl_000\\Documents\\NetBeansProjects\\DataBaseTwitter\\Statistics\\POS\\Wiki");
        for (File file : dir.listFiles()) {
            if (file.getName().toLowerCase().equals((query.toLowerCase() + ".txt"))) {
                BufferedReader in = new BufferedReader(new FileReader(new File("C:\\Users\\mresl_000\\Doc"
                        + "uments\\NetBeansProjects\\DataBaseTwitter\\Statistics\\POS\\Wiki\\" + file.getName())));
                String article = "";
                String temp = in.readLine();
                while ((temp) != null) {
                    article += (temp + "\n");
                    temp = in.readLine();
                }
                return article;
            }
        }
        return null;
    }

    private static ArrayList<String> POSTagOne(MaxentTagger tagger, String text, BufferedWriter out, boolean print, String waw) throws IOException {
        ArrayList<String> tweetNE = new ArrayList<>();
        String output = tagger.tagString(text);
        String[] tokenVsTags = output.split(" ");
        String prevTag = "";
        String prevWord = "";
        for (int j = 0; j < tokenVsTags.length; j++) {

            String[] tokens = tokenVsTags[j].split("/");
            if (print) {
                out.append("-> Token: " + tokens[0] + "\t\tTag: " + tokens[1] + "\n");
            }
            if (/*tokens[1].equals("NNS") || tokens[1].equals("DTNNS") ||*/tokens[1].equals("NNP")
                    || tokens[1].equals("DTNNP") /*|| tokens[1].equals("NN") || tokens[1].equals("DTNN")*/) {
                if (tokens[0].startsWith(waw)) {
                    tweetNE.add(prevWord.trim());
                    prevWord = tokens[0].substring(1);
                } else {
                    prevWord = prevWord + " " + tokens[0];
                }
            } else {
                if (prevTag.equals("NNP") || prevTag.equals("DTNNP")) {
                    tweetNE.add(prevWord.trim());
                    prevWord = "";
                }
//                if (tokens[1].equals("DTJJ")) {
//                    tweetNE.add(tokens[0]);
//                }
            }
            prevTag = tokens[1];
        }
        if (prevTag.equals("NNP") || prevTag.equals("DTNNP")) {
            tweetNE.add(prevWord.trim());
            prevWord = "";
        }
        return tweetNE;
    }

    private static ArrayList<String> POSTagOne2(MaxentTagger tagger, String text, BufferedWriter out, String waw, String ba2) throws IOException {
        ArrayList<String> tweetNE = new ArrayList<>();
        String output = tagger.tagString(text);
        String[] tokenVsTags = output.split(" ");
        ArrayList<String> nounTags = new ArrayList<>();
        nounTags.add("NN");
        nounTags.add("DTNN");
        nounTags.add("NNS");
        nounTags.add("DTNNS");
        nounTags.add("NNP");
        nounTags.add("DTNNP");
        nounTags.add("NNPS");
        nounTags.add("DTNNPS");
        nounTags.add("JJ");
        nounTags.add("DTJJ");
        String[] words = new String[tokenVsTags.length];
        String[] tags = new String[tokenVsTags.length];
        for (int i = 0; i < tokenVsTags.length; i++) {
            String[] tempArray = tokenVsTags[i].split("/");
            words[i] = tempArray[0].trim();
            tags[i] = tempArray[1].trim();
        }

        for (int j = 0; j < tokenVsTags.length; j++) {
            String currWord = words[j];
            String currTag = tags[j];

            out.append("-> Token: " + currWord + "\t\tTag: " + currTag + "\n");

            if (!nounTags.contains(currTag)) {
                continue;
            }

            for (int k = j; k < tokenVsTags.length && k - j <= 1; k++) {
                if (!nounTags.contains(tags[k])) {
                    break;
                }
                String properName = "";
                String properNameTag = "";
                boolean allNN = true;
                boolean allDTNN = true;
                boolean allJJ = true;
                boolean allDTJJ = true;
                boolean allNNS = true;
                boolean allDTNNS = true;
                for (int i = j; i <= k; i++) {
                    if (!tags[i].equals("NN")) {
                        allNN = false;
                    }
                    if (!tags[i].equals("DTNN")) {
                        allDTNN = false;
                    }
                    if (!tags[i].equals("DTJJ")) {
                        allDTJJ = false;
                    }
                    if (!tags[i].equals("JJ")) {
                        allJJ = false;
                    }
                    if (!tags[i].equals("DTNNS")) {
                        allDTNNS = false;
                    }
                    if (!tags[i].equals("NNS")) {
                        allNNS = false;
                    }

                    properName = properName + " " + words[i];
                    properNameTag = properNameTag + " " + tags[i];
                }

                boolean accepted = !allNN && !allDTNN && !allDTJJ && !allJJ && !allDTNNS && !allNNS;

                if (accepted) {
                    tweetNE.add(properName.trim());
                    StructureVSTag.put(properName.trim(), properNameTag.trim());

                    if (properName.trim().startsWith(waw) || properName.trim().startsWith(ba2)) {
                        tweetNE.add(properName.trim().substring(1));
                        StructureVSTag.put(properName.trim().substring(1), properNameTag.trim());
                    }
//                if(tags[k].equals("JJ") || tags[k].equals("DTJJ")){
//                    break;
//                }
                }
            }
        }

        return tweetNE;
    }

    private static ArrayList<String> POSTagTwo(MaxentTagger tagger, String text, BufferedWriter out, boolean print) throws IOException {
        ArrayList<String> tweetNE = new ArrayList<>();
        String output = tagger.tagString(text);
        String[] tokenVsTags = output.split(" ");

        ArrayList<String> patternsToMatch = new ArrayList<>();
        patternsToMatch.add("NN DTNN");//meda alta7rir
        patternsToMatch.add("NNS DTNN");//kowat al2sad
        patternsToMatch.add("DTNN DTNN");//algam3a alamrekeay
        patternsToMatch.add("DTNN DTJJ");//alkada2 al2dary
        patternsToMatch.add("NN DTJJ");//kasr aleta7edaya 
        out.append("\n");
        for (int j = 0; j < tokenVsTags.length - 1; j++) {
            String[] tokensOne = tokenVsTags[j].split("/");
            String[] tokensTwo = tokenVsTags[j + 1].split("/");
            String token1 = tokensOne[0].trim();
            String tag1 = tokensOne[1].trim();
            String token2 = tokensTwo[0].trim();
            String tag2 = tokensTwo[1].trim();

            String match = "";
            match = MatchPattern(patternsToMatch, tag1, tag2);
            if (!match.isEmpty()) {
                out.append("-> Token: " + token1 + " " + token2 + "\t\tTag: " + match + "\n");
                tweetNE.add(token1 + " " + token2);
            }
        }

        return tweetNE;
    }

    private static ArrayList<String> POSTagThree(MaxentTagger tagger, String text, BufferedWriter out, boolean print) throws IOException {
        ArrayList<String> tweetNE = new ArrayList<>();
        String output = tagger.tagString(text);
        String[] tokenVsTags = output.split(" ");

        ArrayList<String> patternsToMatch = new ArrayList<>();
        patternsToMatch.add("NN DTNN DTJJ");//gabhet alankaz alwatany
        patternsToMatch.add("NNS DTNN DTJJ");//legan altanseek almahaleya
        patternsToMatch.add("DTNN DTNN DTJJ");
        patternsToMatch.add("NN NNP DTJJ");// 7ezb masr alkaweya
        out.append("\n");
        for (int j = 0; j < tokenVsTags.length - 2; j++) {
            String[] tokensOne = tokenVsTags[j].split("/");
            String[] tokensTwo = tokenVsTags[j + 1].split("/");
            String[] tokensThree = tokenVsTags[j + 2].split("/");
            String token1 = tokensOne[0].trim();
            String tag1 = tokensOne[1].trim();
            String token2 = tokensTwo[0].trim();
            String tag2 = tokensTwo[1].trim();
            String token3 = tokensThree[0].trim();
            String tag3 = tokensThree[1].trim();

            String match = "";
            match = MatchPattern(patternsToMatch, tag1, tag2, tag3);
            if (!match.isEmpty()) {
                out.append("-> Token: " + token1 + " " + token2 + " " + token3 + "\t\tTag: " + match + "\n");
                tweetNE.add(token1 + " " + token2 + " " + token3);
            }
        }

        return tweetNE;
    }

    private static PriorityQueue<UserData> getTopFive(ArrayList<String> articleNE) {

        HashMap<String, Integer> uniqueMap = new HashMap<>();

        for (String s : articleNE) {
            if (uniqueMap.containsKey(s)) {
                int temp = uniqueMap.remove(s);
                uniqueMap.put(s, temp + 1);
            } else {
                uniqueMap.put(s, 1);
            }
        }


        Comparator<UserData> comparator = new userComparator();
        PriorityQueue<UserData> pq = new PriorityQueue<>(10000, comparator);
        //output the hash map to priority queue
        Iterator it = uniqueMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pairs = (Map.Entry) it.next();
            UserData u = new UserData((String) pairs.getKey(), (Integer) pairs.getValue());
            pq.add(u);
            it.remove(); // avoids a ConcurrentModificationException
        }

        return pq;

    }

    private static String MatchPattern(ArrayList<String> patternsToMatch, String tag1, String tag2) {
        for (String p : patternsToMatch) {
            String[] split = p.split(" ");
            if (tag1.equals(split[0].trim()) && tag2.equals(split[1].trim())) {
                return p;
            }
        }
        return "";
    }

    private static String MatchPattern(ArrayList<String> patternsToMatch, String tag1, String tag2, String tag3) {
        for (String p : patternsToMatch) {
            String[] split = p.split(" ");
            if (tag1.equals(split[0].trim()) && tag2.equals(split[1].trim()) && tag3.equals(split[2].trim())) {
                return p;
            }
        }
        return "";
    }

    private static ArrayList<String> findLocations(String article, ArrayList<String> locationDataSet, String waw, String ba2) {
        String[] split = article.split(" ");
        ArrayList<String> map = new ArrayList<>();
        for (int i = 0; i < split.length; i++) {
            String query = split[i];
            query = normalizeText(query);
            if (locationDataSet.contains((query))) {
                map.add(query);
                continue;
            }
            if (query.startsWith(waw) || query.startsWith(ba2)) {
                query = query.substring(1);
                if (locationDataSet.contains((query))) {
                    map.add(query);
                }
            }
        }
        for (int i = 0; i < split.length - 1; i++) {
            String query = split[i] + " " + split[i + 1];
            query = normalizeText(query);
            if (locationDataSet.contains((query))) {
                map.add(query);
                continue;
            }
            if (query.startsWith(waw) || query.startsWith(ba2)) {
                query = query.substring(1);
                if (locationDataSet.contains((query))) {
                    map.add(query);
                }
            }
        }
        return map;
    }

    private static PriorityQueue<UserData> getTopFive2(ArrayList<UserData> silverTags) {
        HashMap<String, Integer> uniqueMap = new HashMap<>();

        for (UserData u : silverTags) {
            if (uniqueMap.containsKey(u.userName)) {
                int temp = uniqueMap.remove(u.userName);
                uniqueMap.put(u.userName, temp + u.count);
            } else {
                uniqueMap.put(u.userName, u.count);
            }
        }


        Comparator<UserData> comparator = new userComparator();
        PriorityQueue<UserData> pq = new PriorityQueue<>(10000, comparator);
        //output the hash map to priority queue
        Iterator it = uniqueMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pairs = (Map.Entry) it.next();
            UserData u = new UserData((String) pairs.getKey(), (Integer) pairs.getValue());
            pq.add(u);
            it.remove(); // avoids a ConcurrentModificationException
        }

        return pq;
    }

    private void InsertInsideGeoTagMap(HashMap<GeoTagLabel, Integer> geoTagMap, GeoTagLabel geoTagLabel, int count) {
        if (geoTagMap.containsKey(geoTagLabel)) {
            int temp = geoTagMap.remove(geoTagLabel);
            geoTagMap.put(geoTagLabel, temp + count);
        } else {
            geoTagMap.put(geoTagLabel, count);
        }
    }

    private void UpdateBronzeMap(ArrayList<String> tweetNEOne, ArrayList<String> goldTags) {
        Comparator<UserData> comparator = new userComparator();
        for (String ne : tweetNEOne) {
            PriorityQueue<UserData> pq;
            if (BronzeMap.containsKey(ne)) {
                pq = BronzeMap.remove(ne);
            } else {
                pq = new PriorityQueue<>(1000, comparator);
            }
            HashMap<String, Integer> tempMap = new HashMap();
            while (!pq.isEmpty()) {
                tempMap.put(pq.peek().userName, pq.peek().count);
                pq.poll();
            }
            for (String tag : goldTags) {
                if (tempMap.containsKey(tag)) {
                    int temp = tempMap.remove(tag);
                    tempMap.put(tag, temp + 1);
                } else {
                    tempMap.put(tag, 1);
                }
            }
            for (Map.Entry<String, Integer> e : tempMap.entrySet()) {
                pq.add(new UserData(e.getKey(), e.getValue()));
            }
            BronzeMap.put(ne, pq);
        }
    }

    private HashMap<String, String> GetBronzeTags(ArrayList<String> tweetNEOne) {
        HashMap<String, String> ret = new HashMap<>();
        for (String s : tweetNEOne) {
            if (BronzeMap.containsKey(s)) {
                ret.put(s, BronzeMap.get(s).peek().userName);
            }
        }
        return ret;
    }

    private String[] GetArticlesNames(String[] pageIds, String wiName) throws FileNotFoundException, IOException {
        String[] ret = new String[pageIds.length];
        int i = 0;
        for (String id : pageIds) {
            BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(wikipediaDataSetDirectory + "\\" + wiName + "\\Titles\\" + id)));
            ret[i] = in.readLine();
            i++;
        }
        return ret;
    }

    private ArrayList<String> POSTagAdj(MaxentTagger tagger, String text, BufferedWriter out, String waw, String ba2) {
        ArrayList<String> tweetNE = new ArrayList<>();
        String output = tagger.tagString(text);
        String[] tokenVsTags = output.split(" ");

        String[] words = new String[tokenVsTags.length];
        String[] tags = new String[tokenVsTags.length];
        for (int i = 0; i < tokenVsTags.length; i++) {
            String[] tempArray = tokenVsTags[i].split("/");
            words[i] = tempArray[0].trim();
            tags[i] = tempArray[1].trim();
            if (tags[i].equals("JJ") || tags[i].equals("DTJJ")) {
                tweetNE.add(words[i]);
            }
        }

        return tweetNE;
    }

    void geoTagTweets(ArrayList<Tweet> tweets) throws IOException, ParseException {
        for (Tweet t : tweets) {
            String cat = t.getCategory();
            HashSet<String> geoTags = geoTagTweet(t, cat);
            t.setGeoTag(geoTags);
        }
    }

    void geoTagClusters(HashMap<Integer, Cluster> myclusters) {

        for (Cluster c : myclusters.values()) {
            HashSet<String> clusterTags = new HashSet<>();
            Vector<Tweet> tweets = c.getMembers();
            for (Tweet t : tweets) {
                HashSet<String> tags = t.getGeoTag();
                Iterator<String> itr = tags.iterator();
                while (itr.hasNext()) {
                    clusterTags.add(itr.next());
                }
            }
            c.setGeoTag(clusterTags);
        }

    }

    void extarctTweetsNameEntities(ArrayList<Tweet> tweets) throws IOException, ParseException {
        for (Tweet t : tweets) {
            String cat = t.getCategory();
            HashSet<String> nameEntities = NERTweet(t, cat);
            t.setNameEntities(nameEntities);
        }
    }

    private HashSet<String> NERTweet(Tweet tweet, String category) throws IOException, ParseException {
        String text = tweet.getOriginalText();
        text = Clean(text, arabicCharacters).trim();
        if (text.isEmpty()) {
            return null;
        }

        //Tag Tweet
        ArrayList<String> tweetNEOne = POSTagOne2(taggerPOS, text, outLog, wawLetter, ba2Letter);
        ArrayList<String> tweetAdj = POSTagAdj(taggerPOS, text, outLog, wawLetter, ba2Letter);

        ArrayList<ArrayList<String>> loop = new ArrayList<ArrayList<String>>();
        loop.add(tweetNEOne);

        //Array list containing all locations wiht trust 100%, gold, adj, exact matches queries.
        HashSet<String> returnNameEntities = new HashSet<>();
        HashSet<String> sureLocations = new HashSet<>();

        //Phase One Gold Tag
        ArrayList<String> goldTags = new ArrayList<>();
        outLog.append("\t\t****Gold Tags*****\t\t\n");
        for (String s : tweetNEOne) {
            s = normalizeText(s);
            if (LocationDataBase.contains(s)) {
                goldTags.add(s);
                returnNameEntities.add(s);
                sureLocations.add(s);
                structureCount.add(StructureVSTag.get(s));
                outLog.append("Gold -> " + s + "\n");
            }
        }


        //Phase 2 adjectives
        outLog.append("\t\t****Adjectives results****\n");
        for (String adj : tweetAdj) {
            outLog.append("Query -> :" + adj + "\n");
            String[] titles = nationalityWikiIndexer.searchTitle(adj, "WikiIndex");
            String[] articles = nationalityWikiIndexer.searchArticle(adj, "WikiIndex");
//            System.out.println("Countries Match");
//            for (String s : titles) {
//                BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(nationalityDirectory + "\\Country\\" + s)));
//                System.out.println(in.readLine());
//            }
            outLog.append("Nationalities Match:");
            if (articles.length != 0) {
                String natFileName = articles[0];
                //get nationality
                BufferedReader inNat = new BufferedReader(new InputStreamReader(new FileInputStream("C:\\Users\\mresl_000\\Documents\\NetB"
                        + "eansProjects\\Twitter4jWikipedia\\Nationality" + "\\Nationality\\" + natFileName)));
                //get country
                BufferedReader inCount = new BufferedReader(new InputStreamReader(new FileInputStream("C:\\Users\\mresl_000\\Documents\\NetB"
                        + "eansProjects\\Twitter4jWikipedia\\Nationality" + "\\Country\\" + natFileName)));

                String currNat = inNat.readLine();
                String currCountr = inCount.readLine();
                outLog.append(currNat + ":" + currCountr);
                returnNameEntities.add(currCountr);
                sureLocations.add(currCountr);
            }
            outLog.append("\n");
        }


        //go to wiki pedia
        outLog.append("\t\t****wikipedia results****\n");
        for (ArrayList<String> tempList : loop) {

            for (String queryWord : tempList) {
                queryWord = queryWord.trim();
                if (queryWord.isEmpty()) {
                    continue;
                }
                //skip  query if labeled as gold
                if (goldTags.contains(normalizeText(queryWord))) {
                    continue;
                }

                //use query
                ArrayList<String> queries = new ArrayList<>();
                queries.add(queryWord);
                for (String query : queries) {
                    outLog.append("\n==================="
                            + "==================================\n=\t\t->********Query: " + query + "********");

                    //loop through databases
                    ArrayList<String> wikiIndexerName = (category.equals("politics.txt")) ? wikiIndexersNamesPolitics : wikiIndexersNamesSports;
                    ArrayList<ArrayList<String>> titlesDatabases = (category.equals("politics.txt")) ? politicsTitlesDatabases : sportsTitlesDatabases;
                    ArrayList<ArrayList<String>> locationDatabases = (category.equals("politics.txt")) ? politicsLocationDatabases : sportsLocationDatabases;
                    ArrayList<DatabaseResult> allQueryMatches = new ArrayList<>();
                    for (int i = 0; i < wikiIndexerName.size(); i++) {
                        ArrayList<String> currDatabase = titlesDatabases.get(i);
                        ArrayList<String> currLocDatabase = locationDatabases.get(i);
                        String currDatabaseName = wikiIndexerName.get(i);
                        ArrayList<DatabaseResult> titlesMatch = getExactTitleMatches(query, currDatabase, currLocDatabase);
                        allQueryMatches.addAll(titlesMatch);
                        if (titlesMatch.isEmpty()) {
                            continue;
                        }
                        outLog.append("\nSearching In: " + currDatabaseName + "\n===============\n");

                        for (int c = 0; c < 10 && c < titlesMatch.size(); c++) {
                            DatabaseResult tit = titlesMatch.get(c);
                            outLog.append(tit.getTitle() + ":" + tit.getLocation() + ":" + tit.getWeight() + "\n");
                        }
                        outLog.append("\n");
                    }
                    outLog.append("\n");

                    //check exact match
                    outLog.append("\t\t*****Resolution Algorithm******\n");
                    if (allQueryMatches.size() == 1) {
                        outLog.append("Congrats Exact Match\n");
                        returnNameEntities.add(allQueryMatches.get(0).getTitle());
                        sureLocations.add(allQueryMatches.get(0).getLocation());
                    } else if (allQueryMatches.isEmpty()) { //no single matches at all
                        continue;
                    } else {//work on resolution algorithm
                        outLog.append("Step1:Resolve by weight\n");
                        ArrayList<DatabaseResult> resolvedByWeight = resolveByWeight(allQueryMatches);
                        if (resolvedByWeight.size() == 1) {
                            outLog.append("Succesful reolving by wieht\n");
                            DatabaseResult unique = resolvedByWeight.get(0);
                            outLog.append(unique.getTitle() + ":" + unique.getLocation() + ":" + unique.getWeight() + "\n");
                            returnNameEntities.add(unique.getTitle());
                            sureLocations.add(unique.getLocation());
                        } else if (resolvedByWeight.isEmpty()) {
                            outLog.append("Error resolving by wieht returnSize=0\n");
                        } else {
                            //print resolved by weight
                            for (DatabaseResult currResult : resolvedByWeight) {
                                outLog.append(currResult.getTitle() + ":" + currResult.getLocation() + ":" + currResult.getWeight() + "\n");
                            }
                            //work on resolving by location
                            outLog.append("Step2:Resolve by Location\n");
                            ArrayList<DatabaseResult> resolvedByLocation = resolveByLocation(resolvedByWeight, sureLocations);
                            for (DatabaseResult currResult : resolvedByLocation) {
                                outLog.append(currResult.getTitle() + ":" + currResult.getLocation() + ":" + currResult.getWeight() + "\n");
                            }
                            if (resolvedByLocation.size() == 0) {
                                outLog.append("Error resolving by location-> size:0" + "\n");
                            } else if (resolvedByLocation.size() == 1) {
                                outLog.append("Succesful reolving by loc\n");
                                returnNameEntities.add(resolvedByLocation.get(0).getTitle());
                                sureLocations.add(resolvedByLocation.get(0).getLocation());
                            } else {
                                outLog.append("Error resolving by location-> size:"
                                        + resolvedByLocation.size() + "\n");
                            }
                        }
                    }

                }
            }
        }
        //adding gold tags to important structures

        return returnNameEntities;
    }

    void extractClustersNameEntities(HashMap<Integer, Cluster> myclusters) {
        for (Cluster c : myclusters.values()) {
            HashSet<String> clusterNameEntities = new HashSet<>();
            Vector<Tweet> tweets = c.getMembers();
            for (Tweet t : tweets) {
                HashSet<String> nameEntities = t.getNameEntities();
                Iterator<String> itr = nameEntities.iterator();
                while (itr.hasNext()) {
                    clusterNameEntities.add(itr.next());
                }
            }
            c.setNameEntities(clusterNameEntities);
        }
    }

    public static class userComparator implements Comparator<UserData> {

        @Override
        public int compare(UserData x, UserData y) {
            // Assume neither string is null. Real code should
            // probably be more robust
            if (x.count < y.count) {
                return 1;
            }
            if (x.count > y.count) {
                return -1;
            }
            return 0;
        }
    }

    public static class UserData {

        public String userName;
        public int count;

        public UserData(String name, int c) {
            userName = name;
            count = c;
        }
    }

    public class GeoTagLabel {

        public String word;
        public int label;
        static final int GOLD = 1;
        static final int SILVER = 2;
        static final int BRONZE = 3;

        public GeoTagLabel(String w, int l) {
            word = w;
            label = l;
        }

        @Override
        public int hashCode() {
            return label + 31 * this.word.hashCode();
        }

        @Override
        public boolean equals(Object y) {
            if (y == null || !(y instanceof GeoTagLabel)) {
                return false;
            }
            GeoTagLabel yy = GeoTagLabel.class.cast(y);
            return this.word.equals(yy.word) && this.label == yy.label;
        }
    }

    public static String normalizeText(String OriginalText) {

        String temp = "";
        for (int i = 0; i < OriginalText.length(); i++) {
            char currChar = OriginalText.charAt(i);
            if ((currChar >= 1569 && currChar <= 1594)
                    || (currChar >= 1601 && OriginalText
                    .charAt(i) <= 1610)) {
                if (currChar == 1570
                        || currChar == 1571
                        || currChar == 1573) {
                    temp += "ا";
                } else if (currChar == 1609) {
                    temp += "ي";
                } else if (currChar == 1577) {
                    temp += "ه";
                } else {
                    temp += currChar;
                }
            } else if (currChar >= 1611
                    && currChar <= 1630
                    || currChar == 'ـ') {
            } else {
                if (temp.length() > 0
                        && temp.charAt(temp.length() - 1) != ' ') {
                    temp += " ";
                }
            }
        }
        return temp.trim();
    }

    public static class GeoTagSorter {

        public String word;
        public int label;
        public int count;

        public GeoTagSorter(String w, int l, int c) {
            word = w;
            label = l;
            count = c;
        }
    }

    public static class GeoTagSorterComparator implements Comparator<GeoTagSorter> {

        @Override
        public int compare(GeoTagSorter x, GeoTagSorter y) {
            if (x.label < y.label) {
                return -1;
            }
            if (x.label > y.label) {
                return 1;
            }
            if (x.count > y.count) {
                return -1;
            }
            if (x.count < y.count) {
                return 1;
            }
            return 0;
        }
    }
//    public static class BronzeStructure implements Comparator<BronzeStructure> {
//
//
//        private int count;
//        private PriorityQueue<UserData> pq;
//
//        public BronzeStructure( int count, PriorityQueue<UserData> pq) {
//
//            this.count = count;
//            this.pq = pq;
//        }
//
//        @Override
//        public int compare(BronzeStructure x, BronzeStructure y) {
//            if (x.count > y.count) {
//                return -1;
//            } else if (x.count < y.count) {
//                return 1;
//            } else {
//                return 0;
//            }
//        }
//    }

    public class DatabaseResult {

        int id;
        String title;
        String location;
        double weight;

        public DatabaseResult(int id, String title, String location, double weight) {
            this.id = id;
            this.title = title;
            this.location = location;
            this.weight = weight;
        }

        public int getId() {
            return id;
        }

        public String getTitle() {
            return title;
        }

        public String getLocation() {
            return location;
        }

        public double getWeight() {
            return weight;
        }
    }
}
