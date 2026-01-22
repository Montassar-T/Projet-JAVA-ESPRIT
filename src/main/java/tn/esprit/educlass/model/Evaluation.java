package tn.esprit.educlass.model;

import tn.esprit.educlass.enums.EvaluationType;

import java.util.Date;

public class Evaluation extends BaseEntity {

    private int id;
    private String title;
    private String description;
    private EvaluationType type;
    private int teacherId;
    private int duration; // in minutes
    private Date dueDate;
    private String status;

    public Evaluation() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public EvaluationType getType() { return type; }
    public void setType(EvaluationType type) { this.type = type; }

    public int getTeacherId() { return teacherId; }
    public void setTeacherId(int teacherId) { this.teacherId = teacherId; }

    public int getDuration() { return duration; }
    public void setDuration(int duration) { this.duration = duration; }

    public Date getDueDate() { return dueDate; }
    public void setDueDate(Date dueDate) { this.dueDate = dueDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
