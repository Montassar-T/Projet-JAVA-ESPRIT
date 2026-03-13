package tn.esprit.educlass.model;

import java.math.BigDecimal;

public class Mark extends BaseEntity {

    private int id;
    private int studentId;
    private int examId;
    private BigDecimal mark;
    // Student has requested a double correction
    private boolean reviewRequested;
    // Teacher has processed the request
    private boolean reviewResolved;

    public Mark() {}

    public Mark(int studentId, int examId, BigDecimal mark) {
        this.studentId = studentId;
        this.examId = examId;
        this.mark = mark;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getStudentId() {
        return studentId;
    }

    public void setStudentId(int studentId) {
        this.studentId = studentId;
    }

    public int getExamId() {
        return examId;
    }

    public void setExamId(int examId) {
        this.examId = examId;
    }

    public BigDecimal getMark() {
        return mark;
    }

    public void setMark(BigDecimal mark) {
        this.mark = mark;
    }

    public boolean isReviewRequested() {
        return reviewRequested;
    }

    public void setReviewRequested(boolean reviewRequested) {
        this.reviewRequested = reviewRequested;
    }

    public boolean isReviewResolved() {
        return reviewResolved;
    }

    public void setReviewResolved(boolean reviewResolved) {
        this.reviewResolved = reviewResolved;
    }
}
