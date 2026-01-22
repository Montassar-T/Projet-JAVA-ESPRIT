package tn.esprit.educlass.model;

import tn.esprit.educlass.enums.QuestionType;

public class Question extends BaseEntity {

    private int id;
    private int evaluationId;
    private String text;
    private QuestionType questionType;
    private double points;

    public Question() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getEvaluationId() { return evaluationId; }
    public void setEvaluationId(int evaluationId) { this.evaluationId = evaluationId; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public QuestionType getQuestionType() { return questionType; }
    public void setQuestionType(QuestionType questionType) { this.questionType = questionType; }

    public double getPoints() { return points; }
    public void setPoints(double points) { this.points = points; }
}
