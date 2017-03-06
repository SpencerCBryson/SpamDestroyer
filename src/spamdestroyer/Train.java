package spamdestroyer;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by spencer on 27/02/17.
 */
public class Train {

    private int spamFileCount = 0;
    private int hamFileCount = 0;

    private HashMap<String,Integer> spamFreq = new HashMap<String,Integer>();
    private HashMap<String,Integer> hamFreq = new HashMap<String,Integer>();

    private File mainDir = null;

    private String output = null;

    public Train (File dir) {
        this.mainDir = dir;
    }
    
    public String frequencySpam()
    {
        File spamdir = new File(this.mainDir + "/train/spam");

        File[] spamdirlist = spamdir.listFiles();

        long start_time = System.currentTimeMillis();


        if(spamdirlist != null) {

            for (File file : spamdirlist) {
                try {
                    FileInputStream is = new FileInputStream(file);
                    InputStreamReader sr = new InputStreamReader(is,"UTF-8");
                    BufferedReader br = new BufferedReader(sr);

                    HashMap<String,Integer> bagOfWords = new HashMap<String,Integer>();

                    String line;

                    while((line = br.readLine()) != null) {
                        for (String word : line.split(" ")) {
                            word = word.toLowerCase();
                            if (bagOfWords.get(word) == null) {
                                bagOfWords.put(word, 1);
                            }

                        }
                    }

                    for (String key : bagOfWords.keySet()) {
                        if(this.spamFreq.get(key) == null) {
                            this.spamFreq.put(key,1);
                        } else {
                            this.spamFreq.put(key,this.spamFreq.get(key)+1);
                        }
                    }

                    bagOfWords = null;

                    spamFileCount++;

                    br.close();

                } catch (IOException e) {
                    e.printStackTrace();
                    return "SPAM DIRECTORY NOT FOUND! Please make sure the directory structure is correct.";
                }
            }
        } else {
            System.out.println("Directory not found.");
            this.spamFreq = null;
            return "SPAM DIRECTORY NOT FOUND! Please make sure the directory structure is correct.";
        }

        long end_time = System.currentTimeMillis();
        long total_time = end_time - start_time;

        String out = "Processed " + spamFileCount +" spam files in " + total_time + " ms.";
        System.out.println(out);

        return out;
    }


    public String frequencyHam()
    {
        ArrayList<File> dirList = new ArrayList<File>();

        dirList.add(0, new File(this.mainDir + "/train/ham"));
        dirList.add(1, new File(this.mainDir + "/train/ham2"));

        long start_time = System.currentTimeMillis();

        for(File dir : dirList ) {

            File[] hamDirList = dir.listFiles();

            if (hamDirList != null) {

                for (File file : hamDirList) {
                    try {
                        FileInputStream is = new FileInputStream(file);
                        InputStreamReader sr = new InputStreamReader(is, "UTF-8");
                        BufferedReader br = new BufferedReader(sr);

                        HashMap<String, Integer> bagOfWords = new HashMap<String, Integer>();

                        String line;

                        while ((line = br.readLine()) != null) {
                            for (String word : line.split(" ")) {
                                word = word.toLowerCase();
                                if (bagOfWords.get(word) == null) {
                                    bagOfWords.put(word, 1);
                                }

                            }
                        }

                        for (String key : bagOfWords.keySet()) {
                            if (this.hamFreq.get(key) == null) {
                                this.hamFreq.put(key, 1);
                            } else {
                                this.hamFreq.put(key, this.hamFreq.get(key) + 1);
                            }
                        }

                        hamFileCount++;

                        bagOfWords = null;

                        br.close();

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                System.out.println("Directory not found.");
                return "HAM DIRECTORY NOT FOUND! Please make sure the directory structure is correct.";
            }
        }

        long end_time = System.currentTimeMillis();
        long total_time = end_time - start_time;

        String out = "Processed " + hamFileCount +" ham files in " + total_time + " ms.";

        System.out.println(out);

        return out;
    }

    public HashMap<String,Double> bayes () {

        HashMap<String,Double> bayesMap = new HashMap<String,Double>();

        double spamProb = 0.0;
        double hamProb = 0.0;

        long start_time = System.currentTimeMillis();

        for(String word : this.spamFreq.keySet()) {
            spamProb = (this.spamFreq.get(word).doubleValue())/(spamFileCount);

            if(this.hamFreq.get(word) == null) {
                hamProb = 0.0;
            } else {
                hamProb = (this.hamFreq.get(word).doubleValue())/(hamFileCount);
            }

            double bayesProb = (spamProb)/(spamProb + hamProb);


            bayesMap.put(word,bayesProb);
        }

        long end_time = System.currentTimeMillis();
        long total_time = end_time - start_time;

        this.output = "Calculated probabilities for " + this.spamFreq.size() +" unique words in " + total_time + " ms.";
        System.out.println(this.output);

        this.hamFreq = null;
        this.spamFreq = null;

        System.gc();

        return bayesMap;
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
