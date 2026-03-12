package tn.esprit.educlass.utlis;

import tn.esprit.educlass.enums.Role;
import tn.esprit.educlass.model.Supervision;
import tn.esprit.educlass.model.User;
import tn.esprit.educlass.service.AdminService;

import java.sql.SQLException;

/**
 * Logs mutating actions (create, update, delete) by students and teachers
 * to the supervision table. GET/read actions are not logged.
 */
public final class SupervisionLogger {

    private SupervisionLogger() {}

    /**
     * Log an action to the supervision table.
     * Only logs when the current user is STUDENT or TEACHER (not ADMIN, not null).
     *
     * @param action short description of the action (e.g. "Create course", "Submit evaluation")
     * @param result "SUCCESS" or "FAILURE"
     */
    public static void log(String action, String result) {
        User currentUser = SessionManager.getCurrentUser();
        if (currentUser == null) return;
        Role role = currentUser.getRole();
        // Only log actions done by student or teacher
        if (role != Role.STUDENT && role != Role.TEACHER) return;

        Supervision s = new Supervision();
        s.setAction(action);
        s.setUser(currentUser.getFullName() + " (" + currentUser.getEmail() + ")");
        s.setType(role.name());
        s.setResult(result != null ? result : "SUCCESS");
        s.setTimestamp(new java.util.Date());

        try {
            AdminService adminService = new AdminService(DataSource.getInstance().getCon());
            adminService.registerAction(s);
        } catch (SQLException e) {
            System.err.println("SupervisionLogger: failed to log action '" + action + "': " + e.getMessage());
        }
    }

    /** Log a successful action. */
    public static void logSuccess(String action) {
        log(action, "SUCCESS");
    }

    /** Log a failed action. */
    public static void logFailure(String action) {
        log(action, "FAILURE");
    }
}
