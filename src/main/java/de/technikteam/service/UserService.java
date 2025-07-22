package de.technikteam.service;

import de.technikteam.dao.DatabaseManager;
import de.technikteam.dao.UserDAO;
import de.technikteam.model.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Service layer for handling complex business logic related to Users,
 * including transactional operations that span multiple DAO calls.
 */
public class UserService {
    private static final Logger logger = LogManager.getLogger(UserService.class);
    private final UserDAO userDAO;

    public UserService() {
        this.userDAO = new UserDAO();
    }

    /**
     * Creates a new user and assigns permissions in a single database transaction.
     *
     * @param user          The User object to create.
     * @param password      The plain-text password for the new user.
     * @param permissionIds An array of permission IDs to assign to the user.
     * @return The ID of the newly created user, or 0 on failure.
     */
    public int createUserWithPermissions(User user, String password, String[] permissionIds) {
        Connection conn = null;
        try {
            conn = DatabaseManager.getConnection();
            conn.setAutoCommit(false);

            int newUserId = userDAO.createUser(user, password, conn);
            if (newUserId > 0) {
                userDAO.updateUserPermissions(newUserId, permissionIds, conn);
                conn.commit();
                logger.info("Transaction for creating user '{}' committed successfully.", user.getUsername());
                return newUserId;
            } else {
                throw new SQLException("User creation returned an invalid ID.");
            }
        } catch (Exception e) {
            logger.error("Error in create user transaction for username '{}'. Rolling back.", user.getUsername(), e);
            if (conn != null) {
                try {
                    conn.rollback();
                    logger.info("Transaction rolled back successfully.");
                } catch (SQLException ex) {
                    logger.error("Failed to rollback transaction.", ex);
                }
            }
            return 0;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ex) {
                    logger.error("Failed to close connection.", ex);
                }
            }
        }
    }

    /**
     * Updates a user's profile information and their permissions in a single transaction.
     *
     * @param user           The User object with updated profile data.
     * @param permissionIds  The complete new set of permission IDs for the user.
     * @return true if both operations were successful, false otherwise.
     */
    public boolean updateUserWithPermissions(User user, String[] permissionIds) {
        Connection conn = null;
        boolean profileUpdated = false;
        boolean permissionsUpdated = false;
        try {
            conn = DatabaseManager.getConnection();
            conn.setAutoCommit(false);

            profileUpdated = userDAO.updateUser(user, conn);
            permissionsUpdated = userDAO.updateUserPermissions(user.getId(), permissionIds, conn);

            conn.commit();
            logger.info("Transaction for updating user '{}' committed successfully.", user.getUsername());
            return profileUpdated || permissionsUpdated;

        } catch (Exception e) {
            logger.error("Error in update user transaction for user '{}'. Rolling back.", user.getUsername(), e);
            if (conn != null) {
                try {
                    conn.rollback();
                    logger.info("Transaction rolled back successfully.");
                } catch (SQLException ex) {
                    logger.error("Failed to rollback transaction.", ex);
                }
            }
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ex) {
                    logger.error("Failed to close connection.", ex);
                }
            }
        }
    }
}