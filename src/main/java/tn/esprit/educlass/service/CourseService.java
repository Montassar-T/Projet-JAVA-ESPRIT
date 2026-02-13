package tn.esprit.educlass.service;

import tn.esprit.educlass.mapper.ChapterMapper;
import tn.esprit.educlass.mapper.CourseMapper;
import tn.esprit.educlass.mapper.LessonMapper;
import tn.esprit.educlass.model.Chapter;
import tn.esprit.educlass.model.Course;
import tn.esprit.educlass.model.Lesson;
import tn.esprit.educlass.utlis.DataSource;

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
            INSERT INTO course (title, description, level, creation_date)
            VALUES (?, ?, ?, ?)
        """;

        try (PreparedStatement ps = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, course.getTitle());
            ps.setString(2, course.getDescription());
            ps.setInt(3, course.getLevel());
            ps.setTimestamp(4, new java.sql.Timestamp(new java.util.Date().getTime()));
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong(1);
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
            SET title = ?, description = ?, level = ?, creation_date = ?
            WHERE id = ?
        """;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, course.getTitle());
            ps.setString(2, course.getDescription());
            ps.setInt(3, course.getLevel());
            ps.setTimestamp(4, new java.sql.Timestamp(new java.util.Date().getTime()));
            ps.setLong(5, course.getId());
            ps.executeUpdate();
        }
    }

    public void deleteCourse(long id) throws SQLException {
        String sql = "DELETE FROM course WHERE id = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
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
                    return rs.getLong(1);
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
        }
    }

    public void deleteChapter(long id) throws SQLException {
        String sql = "DELETE FROM chapter WHERE id = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }

    /* =====================================================
       LESSON CRUD
       ===================================================== */

    public void createLesson(Lesson lesson) throws SQLException {
        String sql = """
            INSERT INTO lesson (title, content, duration_minutes, chapter_id)
            VALUES (?, ?, ?, ?)
        """;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, lesson.getTitle());
            ps.setString(2, lesson.getContent());
            ps.setInt(3, lesson.getDurationMinutes());
            ps.setLong(4, lesson.getChapter().getId());
            ps.executeUpdate();
        }
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

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, lesson.getTitle());
            ps.setString(2, lesson.getContent());
            ps.setInt(3, lesson.getDurationMinutes());
            ps.setLong(4, lesson.getId());
            ps.executeUpdate();
        }
    }

    public void deleteLesson(long id) throws SQLException {
        String sql = "DELETE FROM lesson WHERE id = ?";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }
}
