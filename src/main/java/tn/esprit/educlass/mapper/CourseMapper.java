package tn.esprit.educlass.mapper;

import tn.esprit.educlass.model.Course;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

public class CourseMapper {

    public static Course map(ResultSet rs) throws SQLException {
        Course course = new Course();
        course.setId(rs.getLong("id"));
        course.setTitle(rs.getString("title"));
        course.setDescription(rs.getString("description"));

        Date creationDate = rs.getTimestamp("creation_date");

        return course;
    }
}
