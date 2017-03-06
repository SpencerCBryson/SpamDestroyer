package spamdestroyer;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by spencer on 27/02/17.
 */
public class Test {
    private ObservableList<Email> spamResults = FXCollections.observableArrayList();
    private HashMap<String,Double> bayesMap;
    private File dir;
    private String output;

    public Test(String dir, HashMap<String,Double> bayesMap) {
        this.dir = new File(dir);
        this.bayesMap = bayesMap;
    }

    public ObservableList<Email> testSpam() {

        long start_time = System.currentTimeMillis();

        File[] dirList = this.dir.listFiles();

        for(File file : dirList) {
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

                double n = 0.0;

                for(String word : bagOfWords.keySet()) {
                    if(this.bayesMap.get(word) != null) {
                        double bayesProb = this.bayesMap.get(word);
                        if(bayesProb > 0 && bayesProb < 1)
                            n += (Math.log(1.0 - bayesProb) - Math.log(bayesProb));
                    }
                }

                bagOfWords = null;

                double overallSpam = 1.0/(1.0+Math.pow(Math.E,n));

                if(overallSpam > 0.5) {
                    Email spam = new Email(file,this.dir.getName(),"spam",overallSpam);
                    this.spamResults.add(spam);
                } else {
                    Email ham = new Email(file,this.dir.getName(),"ham",overallSpam);
                    this.spamResults.add(ham);
                }

            } catch (IOException e){
                e.printStackTrace();
            }
        }

        double correct = 0.0;
        double total = this.spamResults.size();

        for(Email email : this.spamResults) {
            if(email.getGuess().equals(email.getActual()))
                correct++;
        }

        double accuracy = correct/total;

        long end_time = System.currentTimeMillis();
        long total_time = end_time - start_time;

        this.output = "Processed " + dirList.length +" test files in " + total_time + " ms.";
        System.out.println(this.output);

        System.out.println("Predicted " + correct + "/" + total + " (" + accuracy +"%) spam files correctly.");

        System.gc();

        return this.spamResults;
    }

    public String getOutput() {
        return output;
    }
}
