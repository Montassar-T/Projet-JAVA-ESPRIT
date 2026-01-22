package tn.esprit.educlass.model;

public class Choice extends BaseEntity {

    private int id;
    private int questionId;
    private String text;
    private boolean isCorrect;

    public Choice() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getQuestionId() { return questionId; }
    public void setQuestionId(int questionId) { this.questionId = questionId; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public boolean isCorrect() { return isCorrect; }
    public void setCorrect(boolean correct) { isCorrect = correct; }
}

