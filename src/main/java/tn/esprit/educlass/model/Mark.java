package tn.esprit.educlass.model;

import java.math.BigDecimal;

public class Mark extends BaseEntity {

    private int id;
    private int studentId;
    private int examId;
    private BigDecimal mark;

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
}
