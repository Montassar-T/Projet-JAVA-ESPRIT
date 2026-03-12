package tn.esprit.educlass.model;

public class StudentResponse extends BaseEntity {

    private int id;
    private int studentId;
    private int questionId;
    private Integer choiceId; // null for OPEN questions
    private String answerText; // null for choice-based questions

    public StudentResponse() {}

    public StudentResponse(int studentId, int questionId, Integer choiceId, String answerText) {
        this.studentId = studentId;
        this.questionId = questionId;
        this.choiceId = choiceId;
        this.answerText = answerText;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }

    public int getQuestionId() { return questionId; }
    public void setQuestionId(int questionId) { this.questionId = questionId; }

    public Integer getChoiceId() { return choiceId; }
    public void setChoiceId(Integer choiceId) { this.choiceId = choiceId; }

    public String getAnswerText() { return answerText; }
    public void setAnswerText(String answerText) { this.answerText = answerText; }
}