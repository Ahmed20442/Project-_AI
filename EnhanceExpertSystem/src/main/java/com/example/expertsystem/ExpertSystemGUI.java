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
    private static String answer = null;
    private String pendingSymptom;
    private TextArea resultArea;
    private TextField nameInput;
    private VBox chatBox;
    private HBox answerButtons;
    private ProgressIndicator progressIndicator;
    private ComboBox<String> diseaseComboBox;

    private static ExpertSystemGUI instance;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Medical Expert System");

        Label title = new Label("🩺 Medical Expert System");
        title.setStyle("-fx-font-size: 30px; -fx-font-weight: bold; -fx-text-fill: #004d40;");

        Label nameLabel = new Label("👤 Patient Name:");
        nameLabel.setStyle("-fx-font-size: 15px; -fx-text-fill: #006064;");

        nameInput = new TextField();
        nameInput.setPromptText("Enter patient name...");
        nameInput.setStyle("-fx-font-size: 15px; -fx-padding: 10px; -fx-border-color: #4dd0e1; -fx-border-radius: 8px; -fx-background-radius: 8px;");

        HBox nameBox = new HBox(10, nameLabel, nameInput);
        nameBox.setAlignment(Pos.CENTER);

        Button diagnoseButton = new Button("🔍 Start Diagnose");
        diagnoseButton.setStyle("-fx-background-color: #0288d1; -fx-text-fill: white; -fx-font-size: 15px; -fx-padding: 10px 20px; -fx-background-radius: 10px;");
        diagnoseButton.setOnAction(e -> new Thread(this::runDiagnosis).start());

        Button resetButton = new Button("♻️ Reset");
        resetButton.setStyle("-fx-background-color: #d32f2f; -fx-text-fill: white; -fx-font-size: 15px; -fx-padding: 10px 20px; -fx-background-radius: 10px;");
        resetButton.setOnAction(e -> resetSystem());

        Button showSymptomsButton = new Button("📋 Show Symptoms");
        showSymptomsButton.setStyle("-fx-background-color: #00796b; -fx-text-fill: white; -fx-font-size: 15px; -fx-padding: 10px 20px; -fx-background-radius: 10px;");

        HBox buttons = new HBox(15, diagnoseButton, resetButton, showSymptomsButton);
        buttons.setAlignment(Pos.CENTER);

        diseaseComboBox = new ComboBox<>();
        diseaseComboBox.setPromptText("Select Disease");
        diseaseComboBox.setStyle("-fx-font-size: 15px; -fx-padding: 8px; -fx-border-color: #4dd0e1; -fx-border-radius: 8px; -fx-background-radius: 8px;");
        loadDiseases();

        showSymptomsButton.setOnAction(e -> {
            String selectedDisease = diseaseComboBox.getValue();
            if (selectedDisease != null && !selectedDisease.trim().isEmpty()) {
                showDiseaseSymptoms(selectedDisease);
            } else {
                resultArea.setText("⚠️ Please select a valid disease.");
            }
        });

        resultArea = new TextArea();
        resultArea.setEditable(false);
        resultArea.setWrapText(true);
        resultArea.setStyle("-fx-font-size: 14px; -fx-padding: 10px; -fx-background-color: #e8f5e9; -fx-border-color: #81c784; -fx-border-radius: 8px;");
        resultArea.setPrefHeight(600);
        resultArea.setPrefWidth(330);

        chatBox = new VBox(10);
        chatBox.setStyle("-fx-background-color: #f1f8e9; -fx-padding: 10px; -fx-border-color: #c5e1a5; -fx-border-radius: 10px;");
        chatBox.setPrefHeight(600);
        chatBox.setPrefWidth(330);

        HBox chatAndResultBox = new HBox(20, chatBox, resultArea);
        chatAndResultBox.setAlignment(Pos.CENTER);
        chatAndResultBox.setPrefHeight(600);
        chatAndResultBox.setPrefWidth(450);

        progressIndicator = new ProgressIndicator();
        progressIndicator.setVisible(false);

        Button yesBtn = new Button("✅ Yes");
        yesBtn.setStyle("-fx-background-color: #388e3c; -fx-text-fill: white;");
        yesBtn.setOnAction(e -> handleAnswer("yes"));

        Button noBtn = new Button("❌ No");
        noBtn.setStyle("-fx-background-color: #d32f2f; -fx-text-fill: white;");
        noBtn.setOnAction(e -> handleAnswer("no"));

        answerButtons = new HBox(10, yesBtn, noBtn);
        answerButtons.setAlignment(Pos.CENTER);
        answerButtons.setVisible(false);

        VBox layout = new VBox(20);
        layout.setAlignment(Pos.CENTER);
        layout.setStyle("-fx-padding: 30px; -fx-background-color: linear-gradient(to bottom, #e0f7fa, #ffffff);");
        layout.getChildren().addAll(title, nameBox, diseaseComboBox, buttons, progressIndicator, chatAndResultBox, answerButtons);

        ScrollPane scrollPane = new ScrollPane(layout);
        scrollPane.setFitToHeight(true);
        scrollPane.setFitToWidth(true);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        Scene scene = new Scene(scrollPane, 750, 650);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void runDiagnosis() {
        Platform.runLater(() -> {
            resultArea.clear();
            progressIndicator.setVisible(true);
        });

        try {
            Query consultQuery = new Query("consult('project.pl')");
            if (!consultQuery.hasSolution()) {
                Platform.runLater(() -> resultArea.setText("⚠️ Failed to consult Prolog file."));
                return;
            }

            String patientName = nameInput.getText().trim();
            if (!patientName.isEmpty()) {
                new Query("assertz(patient('" + patientName + "'))").hasSolution();
            }

            // استدعاء Prolog للحصول على السؤال التالي

            Platform.runLater(() -> {
                // عرض السؤال المسترجع من Prolog
                String questionFromProlog = askQuestionFromProlog();
                Label symptomQuestion = new Label("❓ " + questionFromProlog);
                symptomQuestion.setStyle("-fx-font-size: 14px; -fx-background-color: #c5e1a5; -fx-padding: 8px 12px; -fx-background-radius: 10px;");
                HBox questionBox = new HBox(symptomQuestion);
                questionBox.setAlignment(Pos.CENTER_LEFT);
                questionBox.setPadding(new Insets(5, 10, 5, 10));
                chatBox.getChildren().add(questionBox);

                // إظهار الأزرار للإجابة (نعم / لا)
                answerButtons.setVisible(true);
            });



            synchronized (ExpertSystemGUI.class) {
                ExpertSystemGUI.class.wait(); // Wait for the answer from the user
            }

            Query diagnosisQuery = new Query("diagnose_results(Disease)");
            if (diagnosisQuery.hasSolution()) {
                String disease = diagnosisQuery.oneSolution().get("Disease").toString();
                Platform.runLater(() -> {
                    resultArea.setText("✅ Diagnosis: " + disease.replace("_", " "));
                });
            } else {
                Platform.runLater(() -> resultArea.setText("❌ No diagnosis found."));
            }
        } catch (Exception e) {
            Platform.runLater(() -> resultArea.setText("💥 Error during diagnosis: " + e.getMessage()));
        } finally {
            Platform.runLater(() -> progressIndicator.setVisible(false));
        }
    }

    private String askQuestionFromProlog() {
        String question = "";
        try {
            // استعلام Prolog لاسترجاع الأعراض
            Query query = new Query("findall(S, symptom(S), Symptoms).");
            if (query.hasMoreSolutions()) {
                // استخراج البيانات بشكل صحيح
                Object symptomsObj = query.nextSolution().get("Symptoms");
                if (symptomsObj instanceof org.jpl7.Compound) {
                    // تحويل الكائن إلى قائمة من الأعراض
                    List<String> symptoms = extractSymptoms(symptomsObj);
                    if (!symptoms.isEmpty()) {
                        question = "Do you have " + symptoms.get(0) + "?";  // استرجاع السؤال من الأعراض
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return question;
    }

    private List<String> extractSymptoms(Object symptomsObj) {
        List<String> symptoms = new ArrayList<>();
        if (symptomsObj instanceof org.jpl7.Compound) {
            org.jpl7.Compound compound = (org.jpl7.Compound) symptomsObj;
            for (int i = 0; i < compound.arity(); i++) {
                // الحصول على العناصر من compound باستخدام arity و الحصول على القيم
                Object item = compound.arg(i + 1);
                if (item instanceof org.jpl7.Atom) {
                    symptoms.add(((org.jpl7.Atom) item).name());
                }
            }
        }
        return symptoms;
    }




    private void showDiseaseSymptoms(String diseaseName) {
        if (diseaseName.isEmpty()) {
            resultArea.setText("Please select a disease.");
            return;
        }

        try {
            Query consultQuery = new Query("consult('project.pl')");
            if (!consultQuery.hasSolution()) {
                resultArea.setText("Failed to load Prolog file.");
                return;
            }

            String symptomQueryStr = String.format("findall(S, symptom('%s', S), Symptoms)", diseaseName);
            Query symptomQuery = new Query(symptomQueryStr);

            String treatmentQueryStr = String.format("treatment('%s', Treatment)", diseaseName);
            Query treatmentQuery = new Query(treatmentQueryStr);

            StringBuilder output = new StringBuilder("Disease: " + diseaseName + "\n");
            if (symptomQuery.hasSolution()) {
                Term symptoms = symptomQuery.oneSolution().get("Symptoms");
                String symptomsText = symptoms.toString()
                        .replace("[", "\n ▸ ")
                        .replace("]", "")
                        .replace(",", ",\n ▸");
                output.append("\nSymptoms:").append(symptomsText);
            } else {
                output.append("\nNo symptoms found.");
            }

            if (treatmentQuery.hasSolution()) {
                Term treatment = treatmentQuery.oneSolution().get("Treatment");
                output.append("\n\nTreatment: ").append(treatment.toString().replace("_", " "));
            } else {
                output.append("\n\nNo treatment information found.");
            }

            resultArea.setText(output.toString());

        } catch (Exception e) {
            resultArea.setText("Error retrieving data: " + e.getMessage());
        }
    }

    private void resetSystem() {
        nameInput.clear();
        resultArea.clear();
        chatBox.getChildren().clear();
        progressIndicator.setVisible(false);
        new Query("retractall(diagnosis(_))").hasSolution();
        new Query("retractall(yes(_))").hasSolution();
        new Query("retractall(no(_))").hasSolution();
        new Query("retractall(patient(_))").hasSolution();
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
            if (pendingSymptom != null) {
                if (userAnswer.equals("yes")) {
                    new Query("assertz(yes(" + pendingSymptom + "))").hasSolution();
                } else {
                    new Query("assertz(no(" + pendingSymptom + "))").hasSolution();
                }
            }
            pendingSymptom = null;
            ExpertSystemGUI.class.notify();
        }
    }

    private void loadDiseases() {
        try {
            Query consult = new Query("consult('project.pl')");
            if (!consult.hasSolution()) return;

            Query diseaseQuery = new Query("findall(D, symptom(D, _), L), sort(L, Diseases)");
            if (diseaseQuery.hasSolution()) {
                Term diseasesTerm = diseaseQuery.oneSolution().get("Diseases");
                Term[] diseaseList = diseasesTerm.listToTermArray();
                for (Term disease : diseaseList) {
                    diseaseComboBox.getItems().add(disease.toString());
                }
            }
        } catch (Exception e) {
            System.out.println("Error loading diseases: " + e.getMessage());
        }
    }

    @Override
    public void init() {
        instance = this;
    }

    public static ExpertSystemGUI getInstance() {
        return instance;
    }
}
