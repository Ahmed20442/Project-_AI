package com.example.expertsystem;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.jpl7.*;

import java.util.ArrayList;
import java.util.List;

public class ExpertSystemGUI extends Application {
    private VBox chatBox;
    private HBox answerButtons;
    private TextArea resultArea;
    private ProgressIndicator progressIndicator;
    private static String answer = null;
    private static String pendingSymptom = null;
    private static ExpertSystemGUI instance;
    private TextField patientNameField;

    @Override
    public void start(Stage primaryStage) {
        Label title = new Label("🩺 Medical Expert System");
        title.setStyle("-fx-font-size: 30px; -fx-font-weight: bold; -fx-text-fill: #004d40;");

        // === Name Input ===
        Label patientNameLabel = new Label("👤 Patient Name:");
        patientNameLabel.setStyle("-fx-font-size: 15px; -fx-text-fill: #006064;");

        patientNameField = new TextField();
        patientNameField.setPromptText("Enter patient name...");
        patientNameField.setStyle("-fx-font-size: 15px; -fx-padding: 10px; -fx-border-color: #4dd0e1; -fx-border-radius: 8px; -fx-background-radius: 8px;");

        // === Buttons ===
        Button diagnoseButton = new Button("🔍 Start Diagnose");
        diagnoseButton.setStyle("-fx-background-color: #0288d1; -fx-text-fill: white; -fx-font-size: 15px; -fx-padding: 10px 20px; -fx-background-radius: 10px;");
        diagnoseButton.setOnAction(e -> new Thread(this::runDiagnosis).start());

        Button resetButton = new Button("♻ Reset");
        resetButton.setStyle("-fx-background-color: #d32f2f; -fx-text-fill: white; -fx-font-size: 15px; -fx-padding: 10px 20px; -fx-background-radius: 10px;");
        resetButton.setOnAction(e -> resetSystem());

        // === Yes/No Buttons ===
        Button yesBtn = new Button("✅ Yes");
        yesBtn.setStyle("-fx-background-color: #388e3c; -fx-text-fill: white;");
        yesBtn.setOnAction(e -> handleAnswer("yes"));

        Button noBtn = new Button("❌ No");
        noBtn.setStyle("-fx-background-color: #d32f2f; -fx-text-fill: white;");
        noBtn.setOnAction(e -> handleAnswer("no"));

        answerButtons = new HBox(10, yesBtn, noBtn);
        answerButtons.setAlignment(Pos.CENTER);
        answerButtons.setVisible(false);

        progressIndicator = new ProgressIndicator();
        progressIndicator.setVisible(false);

        HBox nameBox = new HBox(10, patientNameLabel, patientNameField);
        nameBox.setAlignment(Pos.CENTER);

        // === Result Area ===
        resultArea = new TextArea();
        resultArea.setEditable(false);
        resultArea.setWrapText(true);
        resultArea.setStyle("-fx-font-size: 14px; -fx-padding: 10px; -fx-background-color: #e8f5e9; -fx-border-color: #81c784; -fx-border-radius: 8px;");
        resultArea.setPrefHeight(600);
        resultArea.setPrefWidth(330);

        // === Chat Box ===
        chatBox = new VBox(10);
        chatBox.setStyle("-fx-background-color: #f1f8e9; -fx-padding: 10px; -fx-border-color: #c5e1a5; -fx-border-radius: 10px;");
        chatBox.setPrefHeight(600);
        chatBox.setPrefWidth(450);  // Make chatBox width larger than resultArea

        HBox chatAndResultBox = new HBox(20, chatBox, resultArea);
        chatAndResultBox.setAlignment(Pos.CENTER);
        chatAndResultBox.setPrefHeight(600);
        chatAndResultBox.setPrefWidth(750);  // Adjusting container width

        // === Main Layout ===
        VBox layout = new VBox(20);
        layout.setAlignment(Pos.CENTER);
        layout.setStyle("-fx-padding: 30px; -fx-background-color: linear-gradient(to bottom, #e0f7fa, #ffffff);");
        layout.getChildren().addAll(title, nameBox, diagnoseButton, resetButton, progressIndicator, chatAndResultBox, answerButtons);

        // Add ScrollPane to make the layout scrollable
        ScrollPane scrollPane = new ScrollPane(layout);
        scrollPane.setFitToHeight(true);
        scrollPane.setFitToWidth(true);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS); // Always show vertical scrollbar
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED); // Show horizontal scrollbar only when needed

        Scene scene = new Scene(scrollPane, 750, 550);
        primaryStage.setTitle("Medical Expert System");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void runDiagnosis() {
        String patientName = patientNameField.getText().trim();
        if (patientName.isEmpty()) {
            Platform.runLater(() -> resultArea.setText("Please enter a patient name."));
            return;
        }
        Platform.runLater(() -> {
            resultArea.clear();
            chatBox.getChildren().clear();
            progressIndicator.setVisible(true);
        });
        new Thread(() -> {
            try {
                new Query("consult('project.pl')").hasSolution();
                new Query("retractall(yes(_))").hasSolution();
                new Query("retractall(no(_))").hasSolution();
                List<String> symptomsToAsk = getUnansweredSymptoms();
                for (String symptom : symptomsToAsk) {
                    ask(symptom);
                    if ("yes".equals(answer)) {
                        new Query("assertz(yes(" + symptom + "))").hasSolution();
                    } else {
                        new Query("assertz(no(" + symptom + "))").hasSolution();
                    }
                }
                Query diagnosisQuery = new Query("diagnose_results(Results)");
                if (diagnosisQuery.hasSolution()) {
                    Term results = diagnosisQuery.oneSolution().get("Results");
                    Platform.runLater(() -> resultArea.setText(patientName + " You probably have :\n" + formatResults(results)));
                }
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> resultArea.setText("Error: " + e.getMessage()));
            } finally {
                Platform.runLater(() -> progressIndicator.setVisible(false));
            }
        }).start();
    }
    private List<String> getUnansweredSymptoms() {
        List<String> symptoms = new ArrayList<>();
        Query q = new Query("ask_all_symptoms(Symptoms)");
        Term list = q.oneSolution().get("Symptoms");
        for (Term t : list.toTermArray()) {
            symptoms.add(t.name());
        }
        return symptoms;
    }
    public static synchronized void ask(String symptom) {
        answer = null;
        ExpertSystemGUI instance = getInstance();
        Platform.runLater(() -> {
            pendingSymptom = symptom;
            Label question = new Label("🤔 Do you have " + symptom.replace("_", " ") + "?");
            question.setStyle("-fx-font-size: 14px; -fx-text-fill: #37474f;");
            instance.chatBox.getChildren().add(question);
            instance.answerButtons.setVisible(true);
        });
        try {
            synchronized (ExpertSystemGUI.class) {
                while (answer == null) {
                    ExpertSystemGUI.class.wait();
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void handleAnswer(String userAnswer) {
        Label userReply = new Label("🗨 " + userAnswer.toUpperCase());
        userReply.setStyle("-fx-font-size: 14px; -fx-background-color: #d1ffd6; -fx-padding: 8px 12px; -fx-background-radius: 10px;");
        HBox replyContainer = new HBox(userReply);
        replyContainer.setAlignment(Pos.CENTER_RIGHT);
        replyContainer.setPadding(new Insets(5, 10, 5, 10));
        chatBox.getChildren().add(replyContainer);
        answerButtons.setVisible(false);
        synchronized (ExpertSystemGUI.class) {
            answer = userAnswer;
            ExpertSystemGUI.class.notify();
        }
    }
    private void resetSystem() {
        Platform.runLater(() -> {
            patientNameField.clear();
            resultArea.clear();
            chatBox.getChildren().clear();
            progressIndicator.setVisible(false);
        });
        new Query("retractall(diagnosis(_))").hasSolution();
        new Query("retractall(yes(_))").hasSolution();
        new Query("retractall(no(_))").hasSolution();
        new Query("retractall(patient(_))").hasSolution();
    }
    private String formatResults(Term results) {
        StringBuilder sb = new StringBuilder();
        double maxProb = -1;
        String mostLikelyDisease = "";

        // Iterate over the results to filter out diseases with zero or near-zero probability
        for (Term result : results.toTermArray()) {
            String disease = result.arg(1).name();
            double prob = result.arg(2).doubleValue();

            // Only process diseases with probability > 0.01% (exclude 0.00% and values close to zero)
            if (prob > 0.01) {
                sb.append(String.format("• %s: %.2f%%\n", disease, prob));

                // Update the most likely disease if the current one has a higher probability
                if (prob > maxProb) {
                    maxProb = prob;
                    mostLikelyDisease = disease;
                }
            }
        }

        // If we have a disease with a non-zero probability, highlight it as the most likely
        if (!mostLikelyDisease.isEmpty()) {
            sb.append("\n🔍 Most likely diagnosis: ").append(mostLikelyDisease)
                    .append(String.format(" (%.2f%%)", maxProb));
        } else {
            sb.append("❗ No matching diagnosis with sufficient probability.");
        }

        return sb.toString();
    }

    public static void main(String[] args) {
        launch(args);
    }
    @Override
    public void init() {
        instance = this;
    }
    public static ExpertSystemGUI getInstance() {
        return instance;
    }
}
