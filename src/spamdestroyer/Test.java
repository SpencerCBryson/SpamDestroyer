package spamdestroyer;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.*;
import java.util.HashMap;

/**
 * Tests the spam filter against a given test set, using a pre-trained bayesian classifier.
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

        File[] dirList = dir.listFiles();

        for(File file : dirList) {
            try {
                FileInputStream is = new FileInputStream(file);
                InputStreamReader sr = new InputStreamReader(is,"UTF-8");
                BufferedReader br = new BufferedReader(sr);

                HashMap<String,Integer> bagOfWords = new HashMap<>();

                String line;

                while((line = br.readLine()) != null) {
                    for (String word : line.split(" ")) {
                        word = word.toLowerCase(); // Force all words to lowercase to increase accuracy
                        bagOfWords.putIfAbsent(word, 1);
                    }
                }

                double n = 0.0;

                for(String word : bagOfWords.keySet()) {
                    if(bayesMap.get(word) != null) {
                        double bayesProb = bayesMap.get(word);
                        // Omit anything 1 and over to improve accuracy
                        if(bayesProb < 1)
                            n += (Math.log(1.0 - bayesProb) - Math.log(bayesProb));
                    }
                }

                double overallSpam = 1.0/(1.0+Math.pow(Math.E,n));

                if(overallSpam > 0.5) {
                    Email spam = new Email(file,dir.getName(),"spam",overallSpam);
                    spamResults.add(spam);
                } else {
                    Email ham = new Email(file,dir.getName(),"ham",overallSpam);
                    spamResults.add(ham);
                }

            } catch (IOException e){
                e.printStackTrace();
            }
        }

        double correct = 0.0;
        double total = spamResults.size();

        for(Email email : spamResults) {
            if(email.getGuess().equals(email.getActual()))
                correct++;
        }

        double accuracy = correct/total;

        long end_time = System.currentTimeMillis();
        long total_time = end_time - start_time;

        output = "Processed " + dirList.length +" test files in " + total_time + " ms.";
        System.out.println(output);

        System.out.println("Predicted " + correct + "/" + total + " (" + accuracy +"%) spam files correctly.");

        System.gc();

        return spamResults;
    }

    public String getOutput() {
        return output;
    }
}
