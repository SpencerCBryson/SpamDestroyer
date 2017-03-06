package spamdestroyer;

import java.io.File;

/**
 * Created by spencer on 27/02/17.
 */
public class Email {
    private File file;
    private String actual;
    private String guess;
    private double spamProbability;

    public Email(File file, String actual, String guess, double spamProbability) {
        this.file = file;
        this.actual = actual;
        this.guess = guess;
        this.spamProbability = spamProbability;
    }

    public boolean isCorrect()
    {
        return this.actual.equals(this.guess);
    }

    public boolean markedSpam()
    {
        return this.guess.equals("spam");
    }

    public String getFile() {
        return file.getName();
    }

    public String getActual() {
        return actual;
    }

    public String getGuess() {
        return guess;
    }

    public double getSpamProbability() {
        return spamProbability;
    }
}
