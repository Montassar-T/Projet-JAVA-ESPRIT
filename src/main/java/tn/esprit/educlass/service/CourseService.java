package tn.esprit.educlass.service;

import tn.esprit.educlass.mapper.ChapterMapper;
import tn.esprit.educlass.mapper.CourseMapper;
import tn.esprit.educlass.mapper.LessonMapper;
import tn.esprit.educlass.model.Chapter;
import tn.esprit.educlass.model.Course;
import tn.esprit.educlass.model.Lesson;
import tn.esprit.educlass.utlis.DataSource;
import tn.esprit.educlass.utlis.SupervisionLogger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CourseService {

    private final Connection connection;

    public CourseService() {
        this.connection = DataSource.getInstance().getCon();
    }

    /* =====================================================
       COURSE CRUD
       ===================================================== */

    public long createCourse(Course course) throws SQLException {
        String sql = """
            INSERT INTO course (title, description, level, teacher_id, school_class_id, creation_date)
            VALUES (?, ?, ?, ?, ?, ?)
        """;

        try (PreparedStatement ps = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, course.getTitle());
            ps.setString(2, course.getDescription());
            ps.setInt(3, course.getLevel());
            if (course.getTeacherId() != null) ps.setLong(4, course.getTeacherId());
            else ps.setNull(4, java.sql.Types.BIGINT);
            if (course.getClassId() != null) ps.setLong(5, course.getClassId());
            else ps.setNull(5, java.sql.Types.BIGINT);
            ps.setTimestamp(6, new java.sql.Timestamp(new java.util.Date().getTime()));
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    long id = rs.getLong(1);
                    SupervisionLogger.logSuccess("Create course: " + course.getTitle());
                    return id;
                }
            }
        }
        return -1;
    }

    public Course getCourseById(long id) throws SQLException {
        String sql = "SELECT * FROM course WHERE id = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? CourseMapper.map(rs) : null;
            }
        }
    }

    public List<Course> getAllCourses() throws SQLException {
        String sql = "SELECT * FROM course";
        List<Course> courses = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                courses.add(CourseMapper.map(rs));
            }
        }
        return courses;
    }

    public List<Course> getCoursesByLevel(int level) throws SQLException {
        String sql = "SELECT * FROM course WHERE level = ?";
        List<Course> courses = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, level);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    courses.add(CourseMapper.map(rs));
                }
            }
        }
        return courses;
    }

    public void updateCourse(Course course) throws SQLException {
        String sql = """
            UPDATE course
            SET title = ?, description = ?, level = ?, teacher_id = ?, school_class_id = ?, creation_date = ?
            WHERE id = ?
        """;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, course.getTitle());
            ps.setString(2, course.getDescription());
            ps.setInt(3, course.getLevel());
            if (course.getTeacherId() != null) ps.setLong(4, course.getTeacherId());
            else ps.setNull(4, java.sql.Types.BIGINT);
            if (course.getClassId() != null) ps.setLong(5, course.getClassId());
            else ps.setNull(5, java.sql.Types.BIGINT);
            ps.setTimestamp(6, new java.sql.Timestamp(new java.util.Date().getTime()));
            ps.setLong(7, course.getId());
            ps.executeUpdate();
            SupervisionLogger.logSuccess("Update course: " + course.getTitle());
        }
    }

    public void deleteCourse(long id) throws SQLException {
        String sql = "DELETE FROM course WHERE id = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
            SupervisionLogger.logSuccess("Delete course id=" + id);
        }
    }

    /* =====================================================
       CHAPTER CRUD
       ===================================================== */

    public long createChapter(Chapter chapter) throws SQLException {
        String sql = """
            INSERT INTO chapter (title, order_index, course_id)
            VALUES (?, ?, ?)
        """;

        try (PreparedStatement ps = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, chapter.getTitle());
            ps.setInt(2, chapter.getOrderIndex());
            ps.setLong(3, chapter.getCourse().getId());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    long id = rs.getLong(1);
                    SupervisionLogger.logSuccess("Create chapter: " + chapter.getTitle());
                    return id;
                }
            }
        }
        return -1;
    }

    public Chapter getChapterById(long id) throws SQLException {
        String sql = "SELECT * FROM chapter WHERE id = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? ChapterMapper.map(rs) : null;
            }
        }
    }

    public List<Chapter> getChaptersByCourse(long courseId) throws SQLException {
        String sql = "SELECT * FROM chapter WHERE course_id = ? ORDER BY order_index";
        List<Chapter> chapters = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, courseId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    chapters.add(ChapterMapper.map(rs));
                }
            }
        }
        return chapters;
    }

    public void updateChapter(Chapter chapter) throws SQLException {
        String sql = """
            UPDATE chapter
            SET title = ?, order_index = ?
            WHERE id = ?
        """;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, chapter.getTitle());
            ps.setInt(2, chapter.getOrderIndex());
            ps.setLong(3, chapter.getId());
            ps.executeUpdate();
            SupervisionLogger.logSuccess("Update chapter: " + chapter.getTitle());
        }
    }

    public void deleteChapter(long id) throws SQLException {
        String sql = "DELETE FROM chapter WHERE id = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
            SupervisionLogger.logSuccess("Delete chapter id=" + id);
        }
    }

    /* =====================================================
       LESSON CRUD
       ===================================================== */

    public void createLesson(Lesson lesson) throws SQLException {
        String lessonSql = """
            INSERT INTO lesson (title, content, pdf_path, duration_minutes, chapter_id)
            VALUES (?, ?, ?, ?, ?)
        """;

        connection.setAutoCommit(false);
        try (PreparedStatement ps = connection.prepareStatement(lessonSql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, lesson.getTitle());
            ps.setString(2, lesson.getContent());
            ps.setString(3, lesson.getPdfPath());
            ps.setInt(4, lesson.getDurationMinutes());
            ps.setLong(5, lesson.getChapter().getId());
            ps.executeUpdate();

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    long lessonId = generatedKeys.getLong(1);
                    lesson.setId(lessonId);
                    if (lesson.getPdfData() != null) {
                        saveLessonAttachment(lessonId, lesson.getPdfData());
                    }
                    SupervisionLogger.logSuccess("Create lesson: " + lesson.getTitle());
                }
            }
            connection.commit();
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(true);
        }
    }

    private void saveLessonAttachment(long lessonId, byte[] pdfData) throws SQLException {
        String sql = "INSERT INTO lesson_attachment (lesson_id, pdf_content) VALUES (?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, lessonId);
            ps.setBytes(2, pdfData);
            ps.executeUpdate();
        }
    }

    private void updateLessonAttachment(long lessonId, byte[] pdfData) throws SQLException {
        // Delete existing attachment if any and insert new one
        String deleteSql = "DELETE FROM lesson_attachment WHERE lesson_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(deleteSql)) {
            ps.setLong(1, lessonId);
            ps.executeUpdate();
        }
        if (pdfData != null) {
            saveLessonAttachment(lessonId, pdfData);
        }
    }

    public byte[] getLessonPdfData(long lessonId) throws SQLException {
        String sql = "SELECT pdf_content FROM lesson_attachment WHERE lesson_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, lessonId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getBytes("pdf_content");
                }
            }
        }
        return null;
    }

    public Lesson getLessonById(long id) throws SQLException {
        String sql = "SELECT * FROM lesson WHERE id = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? LessonMapper.map(rs) : null;
            }
        }
    }

    public List<Lesson> getAllLessons() throws SQLException {
        String sql = "SELECT * FROM lesson";
        List<Lesson> lessons = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lessons.add(LessonMapper.map(rs));
            }
        }
        return lessons;
    }

    public List<Lesson> getLessonsByChapter(long chapterId) throws SQLException {
        String sql = "SELECT * FROM lesson WHERE chapter_id = ?";
        List<Lesson> lessons = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, chapterId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lessons.add(LessonMapper.map(rs));
                }
            }
        }
        return lessons;
    }

    public void updateLesson(Lesson lesson) throws SQLException {
        String sql = """
            UPDATE lesson
            SET title = ?, content = ?, duration_minutes = ?
            WHERE id = ?
        """;

        connection.setAutoCommit(false);
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, lesson.getTitle());
            ps.setString(2, lesson.getContent());
            ps.setInt(3, lesson.getDurationMinutes());
            ps.setLong(4, lesson.getId());
            ps.executeUpdate();

            if (lesson.getPdfData() != null) {
                updateLessonAttachment(lesson.getId(), lesson.getPdfData());
            }
            connection.commit();
            SupervisionLogger.logSuccess("Update lesson: " + lesson.getTitle());
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(true);
        }
    }

    public void deleteLesson(long id) throws SQLException {
        String sql = "DELETE FROM lesson WHERE id = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
            SupervisionLogger.logSuccess("Delete lesson id=" + id);
        }
    }
}
