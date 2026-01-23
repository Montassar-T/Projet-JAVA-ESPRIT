package tn.esprit.educlass.controller;

import tn.esprit.educlass.model.Notification;
import tn.esprit.educlass.service.NotificationService;

import java.sql.SQLException;
import java.util.List;

public class NotificationController {

    private final NotificationService service;

    public NotificationController(NotificationService service) {
        this.service = service;
    }

    public void createNotification(Notification notification) throws SQLException {
        service.createNotification(notification);
    }

    public Notification getNotificationById(long id) throws SQLException {
        return service.getNotificationById(id);
    }

    public List<Notification> getAllNotifications() throws SQLException {
        return service.getAllNotifications();
    }

    public List<Notification> getNotificationsByUser(int userId) throws SQLException {
        return service.getNotificationsByUser(userId);
    }

    public void updateNotification(Notification notification) throws SQLException {
        service.updateNotification(notification);
    }

    public void deleteNotification(long id) throws SQLException {
        service.deleteNotification(id);
    }

    public void markAsRead(long id) throws SQLException {
        service.markAsRead(id);
    }
}
