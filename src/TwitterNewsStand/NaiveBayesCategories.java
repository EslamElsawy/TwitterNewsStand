package TwitterNewsStand;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public class NaiveBayesCategories {

    HashMap<String, Integer> ArabicPoliticsDictionary = new HashMap<>();
    HashMap<String, Integer> ArabicSportsDictionary = new HashMap<>();
    HashMap<String, Integer> ArabicEconomyDictionary = new HashMap<>();
    HashMap<String, Integer> ArabicTechnologyDictionary = new HashMap<>();
    HashMap<String, Integer> ArabicWeatherDictionary = new HashMap<>();
    HashMap<String, Integer> ArabicArtDictionary = new HashMap<>();
    HashMap<String, Integer> EnglishNewsDictionary = new HashMap<>();
    HashMap<String, Integer> EnglishJunksDictionary = new HashMap<>();
    HashMap<String, Double> LoadArabicPoliticsDictionary = new HashMap<>();
    HashMap<String, Double> LoadArabicSportsDictionary = new HashMap<>();
    HashMap<String, Double> LoadArabicEconomyDictionary = new HashMap<>();
    HashMap<String, Double> LoadArabicTechnologyDictionary = new HashMap<>();
    HashMap<String, Double> LoadArabicWeatherDictionary = new HashMap<>();
    HashMap<String, Double> LoadArabicArtDictionary = new HashMap<>();
    HashMap<String, Double> LoadEnglishNewsDictionary = new HashMap<>();
    HashMap<String, Double> LoadEnglishJunksDictionary = new HashMap<>();
    ArrayList<Tweet> testList;
    double[] array = new double[6];

    int sumPoliticsCounter = 0, sumSportsCounter = 0, sumEconomyCounter = 0, sumTechnologyCounter = 0, sumWeatherCounter = 0, sumArtCounter = 0;

    public void NormalizeArabicTraining(BufferedReader trainingFile,
            int label) throws IOException {
        String OriginalText, temp;
        String[] splitter;
        while ((OriginalText = trainingFile.readLine()) != null) {
            temp = "";
            for (int i = 0; i < OriginalText.length(); i++) {
                if ((OriginalText.charAt(i) >= 1569 && OriginalText.charAt(i) <= 1594)
                        || (OriginalText.charAt(i) >= 1601 && OriginalText.charAt(i) <= 1610)) {
                    if (OriginalText.charAt(i) == 1570
                            || OriginalText.charAt(i) == 1571
                            || OriginalText.charAt(i) == 1573) {
                        temp += "ا";
                    } else if (OriginalText.charAt(i) == 1609) {
                        temp += "ي";
                    } else if (OriginalText.charAt(i) == 1577) {
                        temp += "ه";
                    } else {
                        temp += OriginalText.charAt(i);
                    }
                } else if (OriginalText.charAt(i) >= 1611
                        && OriginalText.charAt(i) <= 1630
                        || OriginalText.charAt(i) == 'ـ') {
                } else {
                    if (temp.length() > 0
                            && temp.charAt(temp.length() - 1) != ' ') {
                        temp += " ";
                    }
                }
            }
            splitter = temp.split(" ");
            if (label == 1) {
                for (String string : splitter) {
                    if (ArabicPoliticsDictionary.containsKey(string)) {
                        ArabicPoliticsDictionary.put(string,
                                ArabicPoliticsDictionary.get(string) + 1);
                    } else {
                        ArabicPoliticsDictionary.put(string, 1);
                    }

                }
            } else if (label == 2) {
                for (String string : splitter) {
                    if (ArabicSportsDictionary.containsKey(string)) {
                        ArabicSportsDictionary.put(string,
                                ArabicSportsDictionary.get(string) + 1);
                    } else {
                        ArabicSportsDictionary.put(string, 1);
                    }

                }

            } else if (label == 3) {
                for (String string : splitter) {
                    if (ArabicEconomyDictionary.containsKey(string)) {
                        ArabicEconomyDictionary.put(string,
                                ArabicEconomyDictionary.get(string) + 1);
                    } else {
                        ArabicEconomyDictionary.put(string, 1);
                    }

                }

            } else if (label == 4) {
                for (String string : splitter) {
                    if (ArabicTechnologyDictionary.containsKey(string)) {
                        ArabicTechnologyDictionary.put(string,
                                ArabicTechnologyDictionary.get(string) + 1);
                    } else {
                        ArabicTechnologyDictionary.put(string, 1);
                    }

                }

            } else if (label == 5) {
                for (String string : splitter) {
                    if (ArabicWeatherDictionary.containsKey(string)) {
                        ArabicWeatherDictionary.put(string,
                                ArabicWeatherDictionary.get(string) + 1);
                    } else {
                        ArabicWeatherDictionary.put(string, 1);
                    }

                }

            } else if (label == 6) {
                for (String string : splitter) {
                    if (ArabicArtDictionary.containsKey(string)) {
                        ArabicArtDictionary.put(string,
                                ArabicArtDictionary.get(string) + 1);
                    } else {
                        ArabicArtDictionary.put(string, 1);
                    }

                }

            }


        }

    }

    public void setCounters() {
        Iterator<Entry<String, Integer>> it = ArabicPoliticsDictionary.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Integer> pairs = (Map.Entry<String, Integer>) it.next();
            sumPoliticsCounter += pairs.getValue();
        }
        it = ArabicSportsDictionary.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Integer> pairs = (Map.Entry<String, Integer>) it.next();
            sumSportsCounter += pairs.getValue();
        }
        it = ArabicEconomyDictionary.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Integer> pairs = (Map.Entry<String, Integer>) it.next();
            sumEconomyCounter += pairs.getValue();
        }
        it = ArabicTechnologyDictionary.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Integer> pairs = (Map.Entry<String, Integer>) it.next();
            sumTechnologyCounter += pairs.getValue();
        }
        it = ArabicWeatherDictionary.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Integer> pairs = (Map.Entry<String, Integer>) it.next();
            sumWeatherCounter += pairs.getValue();
        }
        it = ArabicArtDictionary.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Integer> pairs = (Map.Entry<String, Integer>) it.next();
            sumArtCounter += pairs.getValue();
        }
    }

    public int numberOfrepeatition(String word) {
        int counter = 0;
        if (ArabicPoliticsDictionary.containsKey(word)) {
            counter++;
        }
        if (ArabicSportsDictionary.containsKey(word)) {
            counter++;
        }
        if (ArabicEconomyDictionary.containsKey(word)) {
            counter++;
        }
        if (ArabicTechnologyDictionary.containsKey(word)) {
            counter++;
        }
        if (ArabicWeatherDictionary.containsKey(word)) {
            counter++;
        }
        if (ArabicArtDictionary.containsKey(word)) {
            counter++;
        }

        return counter;
    }

    public void DictionaryFile(int label) throws IOException {
        BufferedWriter dictFile = null;
        Iterator<Entry<String, Integer>> it;
        int repeatation;
        if (label == 1) {
            dictFile = new BufferedWriter(new FileWriter(new File(
                    "Categories\\Dictionaries\\PoliticsDict.txt"), true));
            dictFile.write(sumPoliticsCounter + "");
            dictFile.newLine();
            it = ArabicPoliticsDictionary.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, Integer> pairs = (Map.Entry<String, Integer>) it.next();
                repeatation = numberOfrepeatition(pairs.getKey());
                dictFile.write(pairs.getKey() + "	" + pairs.getValue()
                        + "	" + Math.sqrt((pairs.getValue() + 0.0) / (sumPoliticsCounter + 0.0)) + "	"
                        + (1 + Math.log((6.0) / (repeatation + 1.0))));
                dictFile.newLine();
            }
        } else if (label == 2) {
            dictFile = new BufferedWriter(new FileWriter(new File(
                    "Categories\\Dictionaries\\SportsDict.txt"), true));
            dictFile.write(sumSportsCounter + "");
            dictFile.newLine();
            it = ArabicSportsDictionary.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, Integer> pairs = (Map.Entry<String, Integer>) it.next();
                repeatation = numberOfrepeatition(pairs.getKey());
                dictFile.write(pairs.getKey() + "	" + pairs.getValue()
                        + "	" + Math.sqrt((pairs.getValue() + 0.0) / (sumSportsCounter + 0.0)) + "	"
                        + (1 + Math.log((6.0) / (repeatation + 1.0))));
                dictFile.newLine();
            }
        } else if (label == 3) {
            dictFile = new BufferedWriter(new FileWriter(new File(
                    "Categories\\Dictionaries\\EconomyDict.txt"), true));
            dictFile.write(sumEconomyCounter + "");
            dictFile.newLine();
            it = ArabicEconomyDictionary.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, Integer> pairs = (Map.Entry<String, Integer>) it.next();
                repeatation = numberOfrepeatition(pairs.getKey());
                dictFile.write(pairs.getKey() + "	" + pairs.getValue()
                        + "	" + Math.sqrt((pairs.getValue() + 0.0) / (sumEconomyCounter + 0.0)) + "	"
                        + (1 + Math.log((6.0) / (repeatation + 1.0))));
                dictFile.newLine();
            }
        } else if (label == 4) {
            dictFile = new BufferedWriter(new FileWriter(new File(
                    "Categories\\Dictionaries\\TechnologyDict.txt"), true));
            dictFile.write(sumTechnologyCounter + "");
            dictFile.newLine();
            it = ArabicTechnologyDictionary.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, Integer> pairs = (Map.Entry<String, Integer>) it.next();
                repeatation = numberOfrepeatition(pairs.getKey());
                dictFile.write(pairs.getKey() + "	" + pairs.getValue()
                        + "	" + Math.sqrt((pairs.getValue() + 0.0) / (sumTechnologyCounter + 0.0)) + "	"
                        + (1 + Math.log((6.0) / (repeatation + 1.0))));
                dictFile.newLine();
            }
        } else if (label == 5) {
            dictFile = new BufferedWriter(new FileWriter(new File(
                    "Categories\\Dictionaries\\WeatherDict.txt"), true));
            dictFile.write(sumWeatherCounter + "");
            dictFile.newLine();
            it = ArabicWeatherDictionary.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, Integer> pairs = (Map.Entry<String, Integer>) it.next();
                repeatation = numberOfrepeatition(pairs.getKey());
                dictFile.write(pairs.getKey() + "	" + pairs.getValue()
                        + "	" + Math.sqrt((pairs.getValue() + 0.0) / (sumWeatherCounter + 0.0)) + "	"
                        + (1 + Math.log((6.0) / (repeatation + 1.0))));
                dictFile.newLine();
            }
        } else if (label == 6) {
            dictFile = new BufferedWriter(new FileWriter(new File(
                    "Categories\\Dictionaries\\ArtDict.txt"), true));
            dictFile.write(sumArtCounter + "");
            dictFile.newLine();
            it = ArabicArtDictionary.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, Integer> pairs = (Map.Entry<String, Integer>) it.next();
                repeatation = numberOfrepeatition(pairs.getKey());
                dictFile.write(pairs.getKey() + "	" + pairs.getValue()
                        + "	" + Math.sqrt((pairs.getValue() + 0.0) / (sumArtCounter + 0.0)) + "	"
                        + (1 + Math.log((6.0) / (repeatation + 1.0))));
                dictFile.newLine();
            }
        }
        dictFile.close();
    }

    public void loadDictionary(int label) throws IOException {
        BufferedReader loader = null;
        String[] splitter;
        String line;

        if (label == 1) {
            loader = new BufferedReader(new FileReader(
                    "Categories\\Dictionaries\\PoliticsDict.txt"));
            sumPoliticsCounter = Integer.parseInt(loader.readLine());
            while ((line = loader.readLine()) != null) {
                splitter = line.split("	");
                //TF*IDF
                LoadArabicPoliticsDictionary.put(splitter[0], (Double.parseDouble(splitter[2]) * (Double.parseDouble(splitter[3]))));
            }
        } else if (label == 2) {
            loader = new BufferedReader(new FileReader(
                    "Categories\\Dictionaries\\SportsDict.txt"));
            sumSportsCounter = Integer.parseInt(loader.readLine());
            while ((line = loader.readLine()) != null) {
                splitter = line.split("	");
                //TF*IDF
                LoadArabicSportsDictionary.put(splitter[0], (Double.parseDouble(splitter[2]) * (Double.parseDouble(splitter[3]))));
            }
        } else if (label == 3) {
            loader = new BufferedReader(new FileReader(
                    "Categories\\Dictionaries\\EconomyDict.txt"));
            sumEconomyCounter = Integer.parseInt(loader.readLine());
            while ((line = loader.readLine()) != null) {
                splitter = line.split("	");
                //TF*IDF
                LoadArabicEconomyDictionary.put(splitter[0], (Double.parseDouble(splitter[2]) * (Double.parseDouble(splitter[3]))));
            }
        } else if (label == 4) {
            loader = new BufferedReader(new FileReader(
                    "Categories\\Dictionaries\\TechnologyDict.txt"));
            sumTechnologyCounter = Integer.parseInt(loader.readLine());
            while ((line = loader.readLine()) != null) {
                splitter = line.split("	");
                //TF*IDF
                LoadArabicTechnologyDictionary.put(splitter[0], (Double.parseDouble(splitter[2]) * (Double.parseDouble(splitter[3]))));
            }
        } else if (label == 5) {
            loader = new BufferedReader(new FileReader(
                    "Categories\\Dictionaries\\WeatherDict.txt"));
            sumWeatherCounter = Integer.parseInt(loader.readLine());
            while ((line = loader.readLine()) != null) {
                splitter = line.split("	");
                //TF*IDF
                LoadArabicWeatherDictionary.put(splitter[0], (Double.parseDouble(splitter[2]) * (Double.parseDouble(splitter[3]))));
            }
        } else if (label == 6) {
            loader = new BufferedReader(new FileReader(
                    "Categories\\Dictionaries\\ArtDict.txt"));
            sumArtCounter = Integer.parseInt(loader.readLine());
            while ((line = loader.readLine()) != null) {
                splitter = line.split("	");
                //TF*IDF
                LoadArabicArtDictionary.put(splitter[0], (Double.parseDouble(splitter[2]) * (Double.parseDouble(splitter[3]))));
            }
        }

        loader.close();
    }

    public void NormalizeArabicTesting(Tweet tweet) {
        String OriginalText, temp = "";
        OriginalText = tweet.getOriginalText();
        for (int i = 0; i < OriginalText.length(); i++) {
            if ((OriginalText.charAt(i) >= 1569 && OriginalText.charAt(i) <= 1594)
                    || (OriginalText.charAt(i) >= 1601 && OriginalText.charAt(i) <= 1610)) {
                if (OriginalText.charAt(i) == 1570
                        || OriginalText.charAt(i) == 1571
                        || OriginalText.charAt(i) == 1573) {
                    temp += "ا";
                } else if (OriginalText.charAt(i) == 1609) {
                    temp += "ي";
                } else if (OriginalText.charAt(i) == 1577) {
                    temp += "ه";
                } else {
                    temp += OriginalText.charAt(i);
                }
            } else if (OriginalText.charAt(i) >= 1611
                    && OriginalText.charAt(i) <= 1630
                    || OriginalText.charAt(i) == 'ـ') {
            } else {
                if (temp.length() > 0 && temp.charAt(temp.length() - 1) != ' ') {
                    temp += " ";
                }
            }
        }
        if (temp.length() > 0) {
            tweet.setProcessedText(temp);
        }

    }

    public void NormalizeEnglishTesting(Tweet tweet) {
        String OriginalText, temp = "";
        OriginalText = tweet.getOriginalText();
        for (int i = 0; i < OriginalText.length(); i++) {
            if ((OriginalText.charAt(i) >= 65 && OriginalText.charAt(i) <= 90)
                    || (OriginalText.charAt(i) >= 97 && OriginalText.charAt(i) <= 122)) {
                temp += OriginalText.charAt(i);
            } else {
                if (temp.length() > 0 && temp.charAt(temp.length() - 1) != ' ') {
                    temp += " ";
                }
            }
        }
        if (temp.length() > 0) {
            tweet.setProcessedText(temp);
        }

    }

    public void classify() throws IOException {
        String processed;
        String[] Splitter;
        int index;
        double polRec = 0.0, sptRec = 0.0, ecnRec = 0.0, tecRec = 0.0, wthRec = 0.0, artRec = 0.0;
        BufferedWriter pol = new BufferedWriter(new FileWriter(new File(
                "Categories//Results//Pol.txt"), true));
        BufferedWriter spt = new BufferedWriter(new FileWriter(new File(
                "Categories//Results//Spt.txt"), true));
        BufferedWriter ecn = new BufferedWriter(new FileWriter(new File(
                "Categories//Results//Ecn.txt"), true));
        BufferedWriter tech = new BufferedWriter(new FileWriter(new File(
                "Categories//Results//Tech.txt"), true));
        BufferedWriter wth = new BufferedWriter(new FileWriter(new File(
                "Categories//Results//Weather.txt"), true));
        BufferedWriter art = new BufferedWriter(new FileWriter(new File(
                "Categories//Results//Art.txt"), true));
        int sum = sumPoliticsCounter + sumSportsCounter + sumEconomyCounter + sumTechnologyCounter + sumWeatherCounter + sumArtCounter;
        for (int i = 0; i < testList.size(); i++) {
            polRec = 0.0;
            sptRec = 0.0;
            ecnRec = 0.0;
            tecRec = 0.0;
            wthRec = 0.0;
            artRec = 0.0;
            processed = testList.get(i).getProcessedText();
            Splitter = processed.split(" ");
            for (String string : Splitter) {
                if (LoadArabicPoliticsDictionary.containsKey(string)) {
                    polRec += -LoadArabicPoliticsDictionary.get(string) * (Math.log(LoadArabicPoliticsDictionary.get(string)) / Math.log(2));
                } else {
                    polRec += -(1.0 / (sum + 1.0)) * (Math.log(1.0 / (sum + 1.0)) / Math.log(2));
                }
            }
            for (String string : Splitter) {
                if (LoadArabicSportsDictionary.containsKey(string)) {
                    sptRec += -LoadArabicSportsDictionary.get(string) * (Math.log(LoadArabicSportsDictionary.get(string)) / Math.log(2));
                } else {
                    sptRec += -(1.0 / (sum + 1.0)) * (Math.log(1.0 / (sum + 1.0)) / Math.log(2));
                }
            }
            for (String string : Splitter) {
                if (LoadArabicEconomyDictionary.containsKey(string)) {
                    ecnRec += -LoadArabicEconomyDictionary.get(string) * (Math.log(LoadArabicEconomyDictionary.get(string)) / Math.log(2));
                } else {
                    ecnRec += -(1.0 / (sum + 1.0)) * (Math.log(1.0 / (sum + 1.0)) / Math.log(2));
                }
            }
            for (String string : Splitter) {
                if (LoadArabicTechnologyDictionary.containsKey(string)) {
                    tecRec += -LoadArabicTechnologyDictionary.get(string) * (Math.log(LoadArabicTechnologyDictionary.get(string)) / Math.log(2));
                } else {
                    tecRec += -(1.0 / (sum + 1.0)) * (Math.log(1.0 / (sum + 1.0)) / Math.log(2));
                }
            }
            for (String string : Splitter) {
                if (LoadArabicWeatherDictionary.containsKey(string)) {
                    wthRec += -LoadArabicWeatherDictionary.get(string) * (Math.log(LoadArabicWeatherDictionary.get(string)) / Math.log(2));
                } else {
                    wthRec += -(1.0 / (sum + 1.0)) * (Math.log(1.0 / (sum + 1.0)) / Math.log(2));
                }
            }
            for (String string : Splitter) {
                if (LoadArabicArtDictionary.containsKey(string)) {
                    artRec += -LoadArabicArtDictionary.get(string) * (Math.log(LoadArabicArtDictionary.get(string)) / Math.log(2));
                } else {
                    artRec += -(1.0 / (sum + 1.0)) * (Math.log(1.0 / (sum + 1.0)) / Math.log(2));
                }
            }
            array[0] = polRec;
            array[1] = sptRec;
            array[2] = ecnRec;
            array[3] = tecRec;
            array[4] = wthRec;
            array[5] = artRec;
            index = maximum(array);
            if (index == 0) {
                pol.write(testList.get(i).getProcessedText());
                pol.newLine();
            } else if (index == 1) {
                spt.write(testList.get(i).getProcessedText());
                spt.newLine();
            } else if (index == 2) {
                ecn.write(testList.get(i).getProcessedText());
                ecn.newLine();
            } else if (index == 3) {
                tech.write(testList.get(i).getProcessedText());
                tech.newLine();
            } else if (index == 4) {
                wth.write(testList.get(i).getProcessedText());
                wth.newLine();
            } else if (index == 5) {
                art.write(testList.get(i).getProcessedText());
                art.newLine();
            }

        }
        pol.close();
        spt.close();
        ecn.close();
        tech.close();
        wth.close();
        art.close();
    }

    public int maximum(double[] array) {
        int max = 0;
        double val = array[0];
        for (int i = 1; i < array.length; i++) {
            if (val < array[i]) {
                val = array[i];
                max = i;
            }
        }
        return max;
    }

    public void handleTest(boolean Arabic) {
        if (Arabic) {
            for (int i = 0; i < testList.size(); i++) {
                NormalizeArabicTesting(testList.get(i));
                if (testList.get(i).getProcessedText() == null) {
                    testList.remove(i);
                    i--;
                }

            }
        } else {
            for (int i = 0; i < testList.size(); i++) {
                NormalizeEnglishTesting(testList.get(i));
                if (testList.get(i).getProcessedText() == null) {
                    testList.remove(i);
                    i--;
                }

            }
        }

    }

    public void runMain(BufferedReader PolTraining,
            BufferedReader SptTraining,BufferedReader EcnTraining,BufferedReader TechTraining,BufferedReader WthTraining,BufferedReader ArtTraining, ArrayList<Tweet> testing) throws IOException {
        testList = testing;
        NormalizeArabicTraining(PolTraining,1);
        NormalizeArabicTraining(SptTraining,2);
        NormalizeArabicTraining(EcnTraining,3);
        NormalizeArabicTraining(TechTraining,4);
        NormalizeArabicTraining(WthTraining,5);
        NormalizeArabicTraining(ArtTraining,6);
        setCounters();
        DictionaryFile(1);
        DictionaryFile(2);
        DictionaryFile(3);
        DictionaryFile(4);
        DictionaryFile(5);
        DictionaryFile(6);
    }

    public void secondRunMain(ArrayList<Tweet> testing) throws IOException {
        testList = testing;
        loadDictionary(1);
        loadDictionary(2);
        loadDictionary(3);
        loadDictionary(4);
        loadDictionary(5);
        loadDictionary(6);
        handleTest(true);
        classify();

    }

    public static void main(String[] args)throws IOException {
         NaiveBayesCategories bayesCategories=new NaiveBayesCategories();
//         BufferedReader art = new BufferedReader(new FileReader(
//                "Categories\\Training Set\\Art Training.txt"));
//         BufferedReader economy = new BufferedReader(new FileReader(
//                "Categories\\Training Set\\Economy Training.txt"));
//         BufferedReader pol = new BufferedReader(new FileReader(
//                "Categories\\Training Set\\Politics Training.txt"));
//         BufferedReader spt = new BufferedReader(new FileReader(
//                "Categories\\Training Set\\Sports Training.txt"));
//         BufferedReader tech = new BufferedReader(new FileReader(
//                "Categories\\Training Set\\Technology Training.txt"));
//         BufferedReader wth = new BufferedReader(new FileReader(
//                "Categories\\Training Set\\Weather Training.txt"));
//         bayesCategories.runMain(pol, spt, economy, tech, wth, art, null);
        bayesCategories.test();
         System.out.println("done");
         
    }
    
    public void test()throws IOException{
          BufferedReader NewTrainingFile3 = new BufferedReader(new FileReader(
                "Categories//Test.txt"));
        String line;
        ArrayList<Tweet> ar = new ArrayList<>();
        Tweet t;
        while ((line = NewTrainingFile3.readLine()) != null) {
            t = new Tweet(line, null, null, 0, null, null, null, 0, 0);
            ar.add(t);
        }
        NewTrainingFile3.close();
        secondRunMain(ar);
        
    }

}