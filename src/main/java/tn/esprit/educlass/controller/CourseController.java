package tn.esprit.educlass.controller;

import javafx.event.ActionEvent;
import tn.esprit.educlass.model.Chapter;
import tn.esprit.educlass.model.Course;
import tn.esprit.educlass.model.Lesson;
import tn.esprit.educlass.service.CourseService;

import java.sql.SQLException;
import java.util.List;

public class CourseController {

    private CourseService service = new CourseService();

    /* =====================================================
       COURSE ENDPOINTS
       ===================================================== */

    public void createCourse(Course course) throws SQLException {
        service.createCourse(course);
    }

    public Course getCourseById(long id) throws SQLException {
        return service.getCourseById(id);
    }

    public List<Course> getAllCourses() throws SQLException {
        return service.getAllCourses();
    }

    public void updateCourse(Course course) throws SQLException {
        service.updateCourse(course);
    }

    public void deleteCourse(long id) throws SQLException {
        service.deleteCourse(id);
    }

    /* =====================================================
       CHAPTER ENDPOINTS
       ===================================================== */

    public void createChapter(Chapter chapter) throws SQLException {
        service.createChapter(chapter);
    }

    public Chapter getChapterById(long id) throws SQLException {
        return service.getChapterById(id);
    }

    public List<Chapter> getChaptersByCourse(long courseId) throws SQLException {
        return service.getChaptersByCourse(courseId);
    }

    public void updateChapter(Chapter chapter) throws SQLException {
        service.updateChapter(chapter);
    }

    public void deleteChapter(long id) throws SQLException {
        service.deleteChapter(id);
    }

    /* =====================================================
       LESSON ENDPOINTS
       ===================================================== */

    public void createLesson(Lesson lesson) throws SQLException {
        service.createLesson(lesson);
    }

    public Lesson getLessonById(long id) throws SQLException {
        return service.getLessonById(id);
    }

    public List<Lesson> getLessonsByChapter(long chapterId) throws SQLException {
        return service.getLessonsByChapter(chapterId);
    }

    public void updateLesson(Lesson lesson) throws SQLException {
        service.updateLesson(lesson);
    }

    public void deleteLesson(long id) throws SQLException {
        service.deleteLesson(id);
    }

    public void onSearch(ActionEvent actionEvent) {
    }

    public void onApplyFilters(ActionEvent actionEvent) {
    }

    public void onResetFilters(ActionEvent actionEvent) {
    }


}
