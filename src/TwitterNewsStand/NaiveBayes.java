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

public class NaiveBayes {

    HashMap<String, Integer> ArabicNewsDictionary = new HashMap<>();
    HashMap<String, Integer> ArabicJunksDictionary = new HashMap<>();
    HashMap<String, Integer> EnglishNewsDictionary = new HashMap<>();
    HashMap<String, Integer> EnglishJunksDictionary = new HashMap<>();
    HashMap<String, Double> LoadArabicNewsDictionary = new HashMap<>();
    HashMap<String, Double> LoadArabicJunksDictionary = new HashMap<>();
    HashMap<String, Double> LoadEnglishNewsDictionary = new HashMap<>();
    HashMap<String, Double> LoadEnglishJunksDictionary = new HashMap<>();
    ArrayList<Tweet> testList;
    int sumNewsCounter = 0, sumJunksCounter;
//    int newsC = 0, junksC = 0, TP = 0, FP = 0, TN = 0, FN = 0;

    public void NormalizeArabicTraining(BufferedReader trainingFile,
            boolean news) throws IOException {
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
            if (news) {
                for (String string : splitter) {
                    if (ArabicNewsDictionary.containsKey(string)) {
                        ArabicNewsDictionary.put(string,
                                ArabicNewsDictionary.get(string) + 1);
                    } else {
                        ArabicNewsDictionary.put(string, 1);
                    }

                }
//                newsC++;
            } else {
                for (String string : splitter) {
                    if (ArabicJunksDictionary.containsKey(string)) {
                        ArabicJunksDictionary.put(string,
                                ArabicJunksDictionary.get(string) + 1);
                    } else {
                        ArabicJunksDictionary.put(string, 1);
                    }

                }
//                junksC++;
            }

        }

    }

    public void setCounters() {
        Iterator<Entry<String, Integer>> it = ArabicNewsDictionary.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Integer> pairs = (Map.Entry<String, Integer>) it.next();
            sumNewsCounter += pairs.getValue();
        }
        it = ArabicJunksDictionary.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Integer> pairs = (Map.Entry<String, Integer>) it.next();
            sumJunksCounter += pairs.getValue();
        }
    }

    public void DictionaryFile(boolean Arabic, boolean news) throws IOException {
        BufferedWriter dictFile;
        Iterator<Entry<String, Integer>> it;
        if (Arabic && news) {
            dictFile = new BufferedWriter(new FileWriter(new File(
                    "NNADictionary.txt"), true));
            dictFile.write(sumNewsCounter + "");
            dictFile.newLine();
            it = ArabicNewsDictionary.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, Integer> pairs = (Map.Entry<String, Integer>) it.next();
                if (ArabicJunksDictionary.containsKey(pairs.getKey())) {
                    dictFile.write(pairs.getKey() + "	" + pairs.getValue()
                            + "	" + Math.sqrt((pairs.getValue() + 0.0) / (sumNewsCounter + 0.0)) + "	"
                            + (1 + Math.log((2.0) / (3.0))));
                } else {
                    dictFile.write(pairs.getKey() + "	" + pairs.getValue()
                            + "	" + Math.sqrt((pairs.getValue() + 0.0) / (sumNewsCounter + 0.0)) + "	"
                            + 1);
                }
                dictFile.newLine();
            }
        } else if (Arabic && !news) {
            dictFile = new BufferedWriter(new FileWriter(new File(
                    "NJADictionary.txt"), true));
            dictFile.write(sumJunksCounter + "");
            dictFile.newLine();
            it = ArabicJunksDictionary.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, Integer> pairs = (Map.Entry<String, Integer>) it.next();
                if (ArabicNewsDictionary.containsKey(pairs.getKey())) {
                    dictFile.write(pairs.getKey() + "	" + pairs.getValue()
                            + "	" + Math.sqrt((pairs.getValue() + 0.0) / (sumNewsCounter + 0.0)) + "	"
                            + (1 + Math.log((2.0) / (3.0))));
                } else {
                    dictFile.write(pairs.getKey() + "	" + pairs.getValue()
                            + "	" + Math.sqrt((pairs.getValue() + 0.0) / (sumNewsCounter + 0.0)) + "	"
                            + 1);
                }
                dictFile.newLine();
            }
        } else if (!Arabic && news) {
            dictFile = new BufferedWriter(new FileWriter(new File(
                    "NNEDictionary.txt"), true));
            it = EnglishNewsDictionary.entrySet().iterator();
        } else {
            dictFile = new BufferedWriter(new FileWriter(new File(
                    "NJEDictionary.txt"), true));
            it = EnglishJunksDictionary.entrySet().iterator();
        }

        dictFile.close();
    }

    public void loadDictionary(boolean Arabic, boolean news) throws IOException {
        BufferedReader loader;
        String[] splitter;
        String line;
        if (Arabic && news) {
            loader = new BufferedReader(new FileReader(
                    "NNADictionary.txt"));
            sumNewsCounter = Integer.parseInt(loader.readLine());
            while ((line = loader.readLine()) != null) {
                splitter = line.split("	");
                //TF
//                LoadArabicNewsDictionary.put(splitter[0], Double.parseDouble(splitter[2]));
                //TF*IDF
                LoadArabicNewsDictionary.put(splitter[0], (Double.parseDouble(splitter[2]) * (Double.parseDouble(splitter[3]))));
            }
        } else if (Arabic && !news) {
            loader = new BufferedReader(new FileReader(
                    "NJADictionary.txt"));
            sumJunksCounter = Integer.parseInt(loader.readLine());
            while ((line = loader.readLine()) != null) {
                splitter = line.split("	");
//                LoadArabicJunksDictionary.put(splitter[0], Double.parseDouble(splitter[2]));
                LoadArabicJunksDictionary.put(splitter[0], (Double.parseDouble(splitter[2]) * (Double.parseDouble(splitter[3]))));
            }
        } else if (!Arabic && news) {
            loader = new BufferedReader(new FileReader(
                    "NNEDictionary.txt"));
            while ((line = loader.readLine()) != null) {
                splitter = line.split("	");
                LoadEnglishNewsDictionary.put(splitter[0], Double.parseDouble(splitter[2]));
            }
        } else {
            loader = new BufferedReader(new FileReader(
                    "NJEDictionary.txt"));
            while ((line = loader.readLine()) != null) {
                splitter = line.split("	");
                LoadEnglishJunksDictionary.put(splitter[0], Double.parseDouble(splitter[2]));
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

    public void classify(boolean Arabic) throws IOException {
        String processed;
        String[] Splitter;
        double newsRec = 0.0, junkRec = 0.0;
        BufferedWriter out = new BufferedWriter(new FileWriter(new File(
                "NAClassifiedNews.txt"), true));
        BufferedWriter out2 = new BufferedWriter(new FileWriter(new File(
                "NAClassifiedJunks.txt"), true));
        for (int i = 0; i < testList.size(); i++) {
            newsRec = 0.0;
            junkRec = 0.0;
            processed = testList.get(i).getProcessedText();
            Splitter = processed.split(" ");
            for (String string : Splitter) {
                if (LoadArabicNewsDictionary.containsKey(string)) {
                    newsRec += -LoadArabicNewsDictionary.get(string) * (Math.log(LoadArabicNewsDictionary.get(string)) / Math.log(2));
                } else {
                    newsRec += -(1.0 / (sumNewsCounter + sumJunksCounter + 1.0)) * (Math.log(1.0 / (sumNewsCounter + sumJunksCounter + 1.0)) / Math.log(2));
                }

            }

            for (String string : Splitter) {
                if (LoadArabicJunksDictionary.containsKey(string)) {
                    junkRec += -LoadArabicJunksDictionary.get(string) * (Math.log(LoadArabicJunksDictionary.get(string)) / Math.log(2));
                } else {
                    junkRec += -(1.0 / (sumJunksCounter + sumNewsCounter + 1.0)) * (Math.log(1.0 / (sumJunksCounter + sumNewsCounter + 1.0)) / Math.log(2));
                }

            }

            if ((newsRec - junkRec) >= 0.001) {
//				out.write(testList.get(i).getDate().toString()+"	"+newsRec+"	"+junkRec);
//				out.newLine();
//				out.write(testList.get(i).getOriginalText());
//				out.newLine();
                out.write(testList.get(i).getProcessedText() + "	" + newsRec + "	" + junkRec);
//                            	out.write(testList.get(i).getProcessedText());
                out.newLine();
//                FN++;
//                TP++;

            } else {
//				out2.write(testList.get(i).getDate().toString()+"	"+newsRec+"	"+junkRec);
//				out2.newLine();
//				out2.write(testList.get(i).getOriginalText());
//				out2.newLine();
                out2.write(testList.get(i).getProcessedText() + "	" + newsRec + "	" + junkRec);
//                                out2.write(testList.get(i).getProcessedText());
                out2.newLine();
                testList.remove(i);
                i--;
//                FP++;
            }
        }
        out.close();
        out2.close();
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

    public void runMain(BufferedReader NewsTrainingFile,
            BufferedReader JunksTrainingFile, ArrayList<Tweet> testing) throws IOException {
        testList = testing;
        NormalizeArabicTraining(NewsTrainingFile, true);
        NormalizeArabicTraining(JunksTrainingFile, false);
        setCounters();
        DictionaryFile(true, true);
        DictionaryFile(true, false);
//        loadDictionary(true, true);
//        loadDictionary(true, false);
//        handleTest(true);
//        classify(true);
//        System.out.println((TP + 0.0) / (TP + FP + 0.0));
//        System.out.println((2314 + 0.0) / (2314 + FN + 0.0));
    }

    public void secondRunMain(ArrayList<Tweet> testing) throws IOException {
        testList = testing;
        loadDictionary(true, true);
        loadDictionary(true, false);
        handleTest(true);
        classify(true);

    }

    public static void main(String[] args) {
    }

    public void runMain2(BufferedReader NewsTrainingFile,
            BufferedReader JunksTrainingFile, ArrayList<Tweet> testing) throws IOException {
        testList = testing;
        NormalizeArabicTraining(NewsTrainingFile, true);
        NormalizeArabicTraining(JunksTrainingFile, false);
        setCounters();
        DictionaryFile(true, true);
        DictionaryFile(true, false);
        loadDictionary(true, true);
        loadDictionary(true, false);
        handleTest(true);
        classify(true);

    }
}