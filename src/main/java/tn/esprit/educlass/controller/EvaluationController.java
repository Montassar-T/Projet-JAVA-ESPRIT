package tn.esprit.educlass.controller;

import tn.esprit.educlass.model.Evaluation;
import tn.esprit.educlass.service.EvaluationService;

import java.sql.SQLException;
import java.util.List;

public class EvaluationController {

    private final EvaluationService evaluationService;

    public EvaluationController() {
        this.evaluationService = new EvaluationService();
    }

    public boolean add(Evaluation e) throws SQLException {
        return evaluationService.ajouter(e);
    }

    public boolean update(Evaluation e) throws SQLException {
        return evaluationService.modifier(e);
    }

    public boolean delete(Evaluation e) throws SQLException {
        return evaluationService.supprimer(e);
    }

    public List<Evaluation> list() throws SQLException {
        return evaluationService.afficher();
    }

    public Evaluation getById(int id) throws SQLException {
        return evaluationService.findById(id);
    }

    public boolean publish(int evaluationId) throws SQLException {
        return evaluationService.publish(evaluationId);
    }
}

