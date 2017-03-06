package spamdestroyer;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * The main functionality of this class is to train the algorithm to
 * identify the probability of 'spamliness' for each word based on a provided
 * spam/ham data set.
 */
public class Train {

    private int spamFileCount = 0;
    private int hamFileCount = 0;

    private HashMap<String,Integer> spamFreq = new HashMap<>();
    private HashMap<String,Integer> hamFreq = new HashMap<>();

    private File mainDir = null;
    private String output = null;

    public Train (File dir) {
        this.mainDir = dir;
    }

    /*
     * Creates a bag of words model from a given training set of spam, and counts the
     * occurrence of each word in spam files.
     */
    public String frequencySpam()
    {
        File spamdir = new File(mainDir + "/train/spam");
        File[] spamdirlist = spamdir.listFiles();

        long start_time = System.currentTimeMillis();

        if(spamdirlist != null) {
            for (File file : spamdirlist)
                try {
                    // Create the bag of words
                    HashMap<String,Integer> bagOfWords = bagOfWords(file);

                    for (String key : bagOfWords.keySet()) {
                        if (spamFreq.get(key) == null) {
                            spamFreq.put(key, 1);
                        } else {
                            spamFreq.put(key, spamFreq.get(key) + 1);
                        }
                    }

                    spamFileCount++;

                } catch (IOException e) {
                    e.printStackTrace();
                    return "SPAM DIRECTORY NOT FOUND! Please make sure the directory structure is correct.";
                }
        } else {
            System.out.println("Directory not found.");
            spamFreq = null;
            return "SPAM DIRECTORY NOT FOUND! Please make sure the directory structure is correct.";
        }

        long end_time = System.currentTimeMillis();
        long total_time = end_time - start_time;

        String out = "Processed " + spamFileCount +" spam files in " + total_time + " ms.";
        System.out.println(out);

        return out;
    }


    /*
     * Creates a bag of words model from a given training set of 'ham'(non-spam), and counts the
     * occurrence of each word in 'ham' files.
     */
    public String frequencyHam()
    {
        ArrayList<File> dirList = new ArrayList<>();

        dirList.add(0, new File(mainDir + "/train/ham"));
        dirList.add(1, new File(mainDir + "/train/ham2"));

        long start_time = System.currentTimeMillis();

        for(File dir : dirList ) {
            File[] hamDirList = dir.listFiles();

            if (hamDirList != null) {
                for (File file : hamDirList) {
                    try {
                        // Create the bag of words
                        HashMap<String,Integer> bagOfWords = bagOfWords(file);

                        // Add or update occurrence of word in global ham map
                        for (String key : bagOfWords.keySet()) {
                            if (hamFreq.get(key) == null) {
                                hamFreq.put(key, 1);
                            } else {
                                hamFreq.put(key, hamFreq.get(key) + 1);
                            }
                        }

                        hamFileCount++;

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                System.out.println("Directory not found.");
                return "HAM DIRECTORY NOT FOUND! Please make sure the directory " +
                        "structure is correct.";
            }
        }

        long end_time = System.currentTimeMillis();
        long total_time = end_time - start_time;

        String out = "Processed " + hamFileCount +" ham files in " + total_time + " ms.";

        System.out.println(out);

        return out;
    }

    /*
     * Calculates the Bayesian probability that a given word is spam, based on the training
     * data set.
     */
    public HashMap<String,Double> bayes () {

        HashMap<String,Double> bayesMap = new HashMap<>();

        double spamProb;
        double hamProb;

        long start_time = System.currentTimeMillis();

        for(String word : spamFreq.keySet()) {
            spamProb = (spamFreq.get(word).doubleValue())/(spamFileCount);

            if(hamFreq.get(word) == null) {
                hamProb = 0.0;
            } else {
                hamProb = (hamFreq.get(word).doubleValue())/(hamFileCount);
            }

            double bayesProb = (spamProb)/(spamProb + hamProb);


            bayesMap.put(word,bayesProb);
        }

        long end_time = System.currentTimeMillis();
        long total_time = end_time - start_time;

        output = "Calculated probabilities for " + spamFreq.size() +
                " unique words in " + total_time + " ms.";

        System.out.println(output);

        hamFreq = null;
        spamFreq = null;

        System.gc();

        return bayesMap;
    }

    /*
     *  Creates a bag of words model from a given file
     */
    private HashMap<String,Integer> bagOfWords(File file) throws IOException {
        HashMap<String, Integer> bagOfWords = new HashMap<>();
        FileInputStream is = new FileInputStream(file);
        InputStreamReader sr = new InputStreamReader(is, "UTF-8");
        BufferedReader br = new BufferedReader(sr);
        String line;

        while ((line = br.readLine()) != null) {
            for (String word : line.split(" ")) {
                word = word.toLowerCase(); // force every word to lowercase
                bagOfWords.putIfAbsent(word, 1);
            }
        }

        br.close();

        return bagOfWords;
    }

    public int getSpamFileCount() {
        return spamFileCount;
    }

    public int getHamFileCount() {
        return hamFileCount;
    }

    public HashMap<String, Integer> getSpamFreq() {
        return spamFreq;
    }

    public HashMap<String, Integer> getHamFreq() {
        return hamFreq;
    }

    public String getOutput() {
        return output;
    }
}
