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
        course.setLevel(rs.getInt("level"));
        course.setTeacherId(rs.getLong("teacher_id"));
        if (rs.wasNull()) course.setTeacherId(null);
        course.setClassId(rs.getLong("school_class_id"));
        if (rs.wasNull()) course.setClassId(null);

        Date creationDate = rs.getTimestamp("creation_date");

        return course;
    }
}
