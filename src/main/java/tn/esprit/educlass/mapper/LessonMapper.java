package tn.esprit.educlass.mapper;

import tn.esprit.educlass.model.Chapter;
import tn.esprit.educlass.model.Lesson;

import java.sql.ResultSet;
import java.sql.SQLException;

public class LessonMapper {

    public static Lesson map(ResultSet rs) throws SQLException {
        Lesson lesson = new Lesson();
        lesson.setId(rs.getLong("id"));
        lesson.setTitle(rs.getString("title"));
        lesson.setContent(rs.getString("content"));
        lesson.setDurationMinutes(rs.getInt("duration_minutes"));

        // Foreign key reference
        Chapter chapter = new Chapter();
        chapter.setId(rs.getLong("chapter_id"));
        lesson.setChapter(chapter);

        return lesson;
    }
}
