package tn.esprit.educlass.utlis;

import tn.esprit.educlass.enums.Role;
import tn.esprit.educlass.model.Supervision;
import tn.esprit.educlass.model.User;
import tn.esprit.educlass.service.AdminService;

import java.sql.SQLException;

/**
 * Logs mutating actions (create, update, delete) by students and teachers
 * to the supervision table. GET/read actions are not logged.
 * <p>
 * Use either:
 * <ul>
 *   <li>{@link #logSuccess(String)} / {@link #logFailure(String)} after your code (manual)</li>
 *   <li>{@link #runSupervised(String, SupervisedAction)} to run a block and log success/failure automatically (decorator)</li>
 * </ul>
 */
public final class SupervisionLogger {

    private SupervisionLogger() {}

    /**
     * Action that can throw SQLException (e.g. service create/update/delete).
     * Used by {@link #runSupervised(String, SupervisedAction)}.
     */
    @FunctionalInterface
    public interface SupervisedAction {
        void run() throws SQLException;
    }

    /**
     * Decorator: runs the given action and logs to supervision on success or failure.
     * Only logs when current user is STUDENT or TEACHER. Rethrows any exception after logging failure.
     *
     * @param actionDescription short description (e.g. "Create course", "Delete mark")
     * @param action the mutating operation (insert/update/delete)
     * @throws SQLException if the action throws
     */
    public static void runSupervised(String actionDescription, SupervisedAction action) throws SQLException {
        try {
            action.run();
            logSuccess(actionDescription);
        } catch (SQLException e) {
            logFailure(actionDescription);
            throw e;
        } catch (Exception e) {
            logFailure(actionDescription);
            throw new SQLException("Supervised action failed", e);
        }
    }

    /**
     * Same as {@link #runSupervised(String, SupervisedAction)} but returns a value.
     * Use for methods that return boolean/int (e.g. success or generated id).
     */
    @FunctionalInterface
    public interface SupervisedCallable<T> {
        T call() throws SQLException;
    }

    public static <T> T runSupervised(String actionDescription, SupervisedCallable<T> action) throws SQLException {
        try {
            T result = action.call();
            logSuccess(actionDescription);
            return result;
        } catch (SQLException e) {
            logFailure(actionDescription);
            throw e;
        } catch (Exception e) {
            logFailure(actionDescription);
            throw new SQLException("Supervised action failed", e);
        }
    }

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
