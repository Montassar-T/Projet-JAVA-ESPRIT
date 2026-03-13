package tn.esprit.educlass.service;

import tn.esprit.educlass.enums.QuestionType;
import tn.esprit.educlass.mapper.MarkMapper;
import tn.esprit.educlass.model.*;
import tn.esprit.educlass.utlis.DataSource;
import tn.esprit.educlass.utlis.SupervisionLogger;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MarkService {

    private Connection con;

    public MarkService() {
        this.con = DataSource.getInstance().getCon();
    }

    // CREATE
    public boolean ajouter(Mark m) throws SQLException {
        String sql = "INSERT INTO marks (student_id, exam_id, mark, review_requested, review_resolved) VALUES (?,?,?,?,?)";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, m.getStudentId());
        ps.setInt(2, m.getExamId());
        ps.setBigDecimal(3, m.getMark());
        ps.setBoolean(4, m.isReviewRequested());
        ps.setBoolean(5, m.isReviewResolved());
        boolean success = ps.executeUpdate() > 0;
        ps.close();
        if (success) SupervisionLogger.logSuccess("Create mark student=" + m.getStudentId() + " exam=" + m.getExamId());
        return success;
    }

    // UPDATE
    public boolean modifier(Mark m) throws SQLException {
        String sql = "UPDATE marks SET student_id=?, exam_id=?, mark=?, review_requested=?, review_resolved=? WHERE id=?";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, m.getStudentId());
        ps.setInt(2, m.getExamId());
        ps.setBigDecimal(3, m.getMark());
        ps.setBoolean(4, m.isReviewRequested());
        ps.setBoolean(5, m.isReviewResolved());
        ps.setInt(6, m.getId());
        boolean success = ps.executeUpdate() > 0;
        ps.close();
        if (success) SupervisionLogger.logSuccess("Update mark id=" + m.getId());
        return success;
    }

    // DELETE
    public boolean supprimer(Mark m) throws SQLException {
        String sql = "DELETE FROM marks WHERE id=?";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, m.getId());
        boolean success = ps.executeUpdate() > 0;
        ps.close();
        if (success) SupervisionLogger.logSuccess("Delete mark id=" + m.getId());
        return success;
    }

    // LIST ALL MARKS
    public List<Mark> afficher() throws SQLException {
        List<Mark> marks = new ArrayList<>();
        String sql = "SELECT * FROM marks";
        Statement st = con.createStatement();
        ResultSet rs = st.executeQuery(sql);
        while (rs.next()) {
            marks.add(MarkMapper.map(rs));
        }
        rs.close();
        st.close();
        return marks;
    }

    // FIND BY ID
    public Mark findById(int id) throws SQLException {
        String sql = "SELECT * FROM marks WHERE id=?";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, id);
        ResultSet rs = ps.executeQuery();
        Mark m = null;
        if (rs.next()) {
            m = MarkMapper.map(rs);
        }
        rs.close();
        ps.close();
        return m;
    }

    // FIND MARKS BY STUDENT ID
    public List<Mark> findByStudentId(int studentId) throws SQLException {
        List<Mark> marks = new ArrayList<>();
        String sql = "SELECT * FROM marks WHERE student_id=?";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, studentId);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            marks.add(MarkMapper.map(rs));
        }
        rs.close();
        ps.close();
        return marks;
    }

    // FIND MARKS BY EXAM ID
    public List<Mark> findByExamId(int examId) throws SQLException {
        List<Mark> marks = new ArrayList<>();
        String sql = "SELECT * FROM marks WHERE exam_id=?";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, examId);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            marks.add(MarkMapper.map(rs));
        }
        rs.close();
        ps.close();
        return marks;
    }

    // FIND MARK BY STUDENT AND EXAM (unique constraint)
    public Mark findByStudentAndExam(int studentId, int examId) throws SQLException {
        String sql = "SELECT * FROM marks WHERE student_id=? AND exam_id=?";
        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, studentId);
        ps.setInt(2, examId);
        ResultSet rs = ps.executeQuery();
        Mark m = null;
        if (rs.next()) {
            m = MarkMapper.map(rs);
        }
        rs.close();
        ps.close();
        return m;
    }

    /**
     * Automatically calculates the mark for a student on an evaluation
     * based on their responses compared to correct choices.
     *
     * For SINGLE_CHOICE: full points if the selected choice is correct.
     * For MULTIPLE_CHOICE: full points if ALL correct choices are selected and NO incorrect ones.
     * For OPEN: 0 points (requires manual grading).
     *
     * The final mark is normalized to /20.
     *
     * @return the calculated mark (0-20 scale), or null if no auto-gradable questions exist
     */
    public BigDecimal calculateMark(int studentId, int evaluationId) throws SQLException {
        QuestionService questionService = new QuestionService();
        ChoiceService choiceService = new ChoiceService();
        StudentResponseService responseService = new StudentResponseService();

        List<Question> questions = questionService.findByEvaluation(evaluationId);
        if (questions.isEmpty()) return null;

        double totalPoints = 0;     // max possible points (excluding OPEN questions)
        double earnedPoints = 0;    // student's earned points

        for (Question question : questions) {
            // Skip OPEN questions — they need manual grading
            if (question.getQuestionType() == QuestionType.OPEN) {
                continue;
            }

            totalPoints += question.getPoints();

            List<Choice> allChoices = choiceService.findByQuestion(question.getId());
            List<Choice> correctChoices = allChoices.stream()
                    .filter(Choice::isCorrect)
                    .collect(Collectors.toList());

            List<StudentResponse> studentResponses = responseService
                    .findByStudentAndQuestion(studentId, question.getId());

            List<Integer> selectedChoiceIds = studentResponses.stream()
                    .filter(r -> r.getChoiceId() != null)
                    .map(StudentResponse::getChoiceId)
                    .collect(Collectors.toList());

            if (question.getQuestionType() == QuestionType.SINGLE_CHOICE) {
                // Full points if the single selected choice is the correct one
                if (selectedChoiceIds.size() == 1) {
                    boolean isCorrect = correctChoices.stream()
                            .anyMatch(c -> c.getId() == selectedChoiceIds.get(0));
                    if (isCorrect) {
                        earnedPoints += question.getPoints();
                    }
                }
            } else if (question.getQuestionType() == QuestionType.MULTIPLE_CHOICE) {
                // Full points only if student selected ALL correct choices and NO incorrect ones
                List<Integer> correctIds = correctChoices.stream()
                        .map(Choice::getId)
                        .collect(Collectors.toList());

                boolean allCorrectSelected = selectedChoiceIds.containsAll(correctIds);
                boolean noIncorrectSelected = correctIds.containsAll(selectedChoiceIds);

                if (allCorrectSelected && noIncorrectSelected) {
                    earnedPoints += question.getPoints();
                }
            }
        }

        // Avoid division by zero
        if (totalPoints == 0) return BigDecimal.ZERO;

        // Normalize to /20 scale
        double normalized = (earnedPoints / totalPoints) * 20.0;
        return BigDecimal.valueOf(normalized).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Calculates and saves (or updates) the mark for a student on an evaluation.
     * @return the saved Mark object, or null on failure
     */
    public Mark calculateAndSaveMark(int studentId, int evaluationId) throws SQLException {
        BigDecimal calculatedMark = calculateMark(studentId, evaluationId);
        if (calculatedMark == null) {
            calculatedMark = BigDecimal.ZERO;
        }

        // Check if mark already exists
        Mark existing = findByStudentAndExam(studentId, evaluationId);
        if (existing != null) {
            existing.setMark(calculatedMark);
            modifier(existing);
            return existing;
        } else {
            Mark m = new Mark(studentId, evaluationId, calculatedMark);
            // default: no review requested/resolved
            m.setReviewRequested(false);
            m.setReviewResolved(false);
            ajouter(m);
            return m;
        }
    }
}
