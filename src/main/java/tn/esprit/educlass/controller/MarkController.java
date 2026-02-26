package tn.esprit.educlass.controller;

import tn.esprit.educlass.model.Mark;
import tn.esprit.educlass.service.MarkService;

import java.sql.SQLException;
import java.util.List;

public class MarkController {

    private final MarkService markService;

    public MarkController() {
        this.markService = new MarkService();
    }

    public boolean add(Mark m) throws SQLException {
        return markService.ajouter(m);
    }

    public boolean update(Mark m) throws SQLException {
        return markService.modifier(m);
    }

    public boolean delete(Mark m) throws SQLException {
        return markService.supprimer(m);
    }

    public List<Mark> list() throws SQLException {
        return markService.afficher();
    }

    public Mark getById(int id) throws SQLException {
        return markService.findById(id);
    }

    public List<Mark> getByStudentId(int studentId) throws SQLException {
        return markService.findByStudentId(studentId);
    }

    public List<Mark> getByExamId(int examId) throws SQLException {
        return markService.findByExamId(examId);
    }

    public Mark getByStudentAndExam(int studentId, int examId) throws SQLException {
        return markService.findByStudentAndExam(studentId, examId);
    }
}
