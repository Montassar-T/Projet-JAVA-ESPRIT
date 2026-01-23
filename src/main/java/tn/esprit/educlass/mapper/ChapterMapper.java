package tn.esprit.educlass.mapper;

import tn.esprit.educlass.model.Chapter;
import tn.esprit.educlass.model.Course;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ChapterMapper {

    public static Chapter map(ResultSet rs) throws SQLException {
        Chapter chapter = new Chapter();
        chapter.setId(rs.getLong("id"));
        chapter.setTitle(rs.getString("title"));
        chapter.setOrderIndex(rs.getInt("order_index"));

        // Foreign key reference
        Course course = new Course();
        course.setId(rs.getLong("course_id"));
        chapter.setCourse(course);

        return chapter;
    }
}
