package spamdestroyer;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.HashMap;

public class Main extends Application {

    File mainDirectory = null;
    Label dirLbl = new Label("no dir selected");
    String log = "";


    @Override
    public void start(Stage primaryStage) throws Exception{

        primaryStage.setTitle("Spam Destroyer");

        TableView<Email> table = new TableView<Email>();

        BorderPane layout = new BorderPane();

        GridPane topArea = new GridPane();
        topArea.setPadding(new Insets(10, 10, 10, 10));
        topArea.setVgap(10);
        topArea.setHgap(10);

        TextArea console = new TextArea();
        console.setStyle("-fx-text-fill: #000080;");
        console.setEditable(false);
        console.setText("Open a directory containing the appropriate training and testing data sets.");
        this.dirLbl.setStyle("-fx-text-fill: #ff0000;");

        TableColumn<Email,String> fileCol = new TableColumn<>("File");
        fileCol.setCellValueFactory(new PropertyValueFactory<>("file"));
        TableColumn<Email,String> actualCol = new TableColumn<>("Actual");
        actualCol.setCellValueFactory(new PropertyValueFactory<>("actual"));
        TableColumn<Email,String> guessCol = new TableColumn<>("Guess");
        guessCol.setCellValueFactory(new PropertyValueFactory<>("guess"));
        TableColumn<Email,String> probCol = new TableColumn<>("Probability");
        probCol.setCellValueFactory(new PropertyValueFactory<>("spamProbability"));

        Button openButton = new Button("Open Train/Test Set");
        Button runButton = new Button("Destroy Spam");
        runButton.setDisable(true);

        openButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {
                DirectoryChooser directoryChooser = new DirectoryChooser();
                directoryChooser.setInitialDirectory(new File("."));
                mainDirectory = directoryChooser.showDialog(primaryStage);

                System.out.println(mainDirectory);

                if(mainDirectory != null) {
                    dirLbl.setText(mainDirectory.getName());
                    dirLbl.setStyle("-fx-text-fill: #008000;");
                    runButton.setDisable(false);
                    console.setText("Press 'Destroy Spam' to start the process.");
                }
            }
        });


        ProgressIndicator pi = new ProgressIndicator(-1);
        pi.setVisible(false);
        runButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent e) {
                if(mainDirectory != null) {
                    openButton.setDisable(true);
                    runButton.setDisable(true);
                    pi.setVisible(true);

                    topArea.add(pi,3,1);

                    // Launch new thread so we can update the UI during the calculations

                    Task task = new Task() {

                        @Override
                        protected Object call() throws Exception {
                            log += "Learning training dataset...\n";
                            updateMessage(log);
                            Train trainingSet = new Train(mainDirectory);

                            log += trainingSet.frequencySpam()+"\n";
                            updateMessage(log);
                            if(trainingSet.getSpamFreq() == null) {
                                log += "ABORTING!";
                                updateMessage(log);
                                return null;
                            }

                            log += trainingSet.frequencyHam()+"\n";
                            updateMessage(log);
                            if(trainingSet.getHamFreq() == null) {
                                log += "ABORTING!";
                                updateMessage(log);
                                return null;
                            }

                            HashMap<String,Double> bayesMap = trainingSet.bayes();
                            log += trainingSet.getOutput()+"\n";
                            updateMessage(log);

                            log += "Analyzing test set...\n";
                            updateMessage(log);

                            Test spamSet = new Test(mainDirectory + "/test/spam",bayesMap);
                            ObservableList<Email> spamList = spamSet.testSpam();
                            log += spamSet.getOutput() +"\n";
                            updateMessage(log);

                            Test hamSet = new Test(mainDirectory + "/test/ham",bayesMap);
                            ObservableList<Email> hamList = hamSet.testSpam();
                            log += hamSet.getOutput()+"\n";
                            updateMessage(log);

                            spamList.addAll(hamList);

                            int correct = 0;
                            int spamGuess = 0;
                            int spamPosGuess = 0;
                            int total = spamList.size();

                            for(Email email : spamList) {
                                if(email.isCorrect())
                                    correct++;

                                if(email.markedSpam())
                                    spamGuess++;

                                if(email.markedSpam() && email.isCorrect())
                                    spamPosGuess++;
                            }

                            double accuracy = ((double)correct)/total;
                            double precision = ((double)spamPosGuess)/spamGuess;

                            log += "\nPredicted " + correct + "/" + total + " emails correctly.\n";
                            updateMessage(log);
                            log += "Accuracy: " + accuracy +" | " + "Precision: " + precision+"\n";
                            updateMessage(log);

                            Platform.runLater(new Runnable() {
                                @Override public void run() {
                                    table.getColumns().add(fileCol);
                                    table.getColumns().add(actualCol);
                                    table.getColumns().add(guessCol);
                                    table.getColumns().add(probCol);
                                    table.setItems(spamList);
                                    pi.setProgress(1.0);
                                }
                            });
                            return null;
                        }
                    };

                    new Thread(task).start();
                    console.textProperty().bind(task.messageProperty());

                }
            }
        });

        topArea.add(openButton, 1, 1);
        topArea.add(runButton, 2, 1);
        topArea.add(dirLbl,1,2);

        layout.setCenter(table);
        layout.setTop(topArea);
        layout.setBottom(console);
        primaryStage.setScene(new Scene(layout, 600, 600));
        primaryStage.show();

    }


    public static void main(String[] args) {
        launch(args);
    }
}
