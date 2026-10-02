package com.usermanagement.service;

import com.usermanagement.dao.ActivityLogDAO;
import com.usermanagement.dao.UserDAO;
import com.usermanagement.dao.UserDAOImpl;
import com.usermanagement.model.User;
import com.usermanagement.util.PasswordUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Service Layer for User management operations.
 * Implements input validation, business rule enforcement, and auditing.
 */
public class UserService {

    private final UserDAO userDAO;
    private final ActivityLogDAO activityLogDAO;

    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{3,30}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");

    public UserService() {
        this.userDAO = new UserDAOImpl();
        this.activityLogDAO = new ActivityLogDAO();
    }

    public UserService(UserDAO userDAO, ActivityLogDAO activityLogDAO) {
        this.userDAO = userDAO;
        this.activityLogDAO = activityLogDAO;
    }

    public User getUserById(int id) {
        return userDAO.findById(id);
    }

    public User getUserByUsername(String username) {
        return userDAO.findByUsername(username);
    }

    public List<User> getUsers(String search, String roleFilter, String statusFilter,
                               String sortBy, String sortOrder, int page, int pageSize) {
        return userDAO.findAll(search, roleFilter, statusFilter, sortBy, sortOrder, page, pageSize);
    }

    public int getTotalUsersCount(String search, String roleFilter, String statusFilter) {
        return userDAO.count(search, roleFilter, statusFilter);
    }

    public Map<String, Integer> getDashboardStatistics() {
        return userDAO.getStatistics();
    }

    /**
     * Validates and creates a new user.
     *
     * @param user           New user details
     * @param performerUsername Admin/Manager creating the user
     * @param clientIp       IP address of caller
     * @param validationErrors Output list to capture validation error messages
     * @return true if created successfully, false if validation or persistence failed
     */
    public boolean createUser(User user, String performerUsername, String clientIp, List<String> validationErrors) {
        validateUserData(user, true, validationErrors);

        if (!validationErrors.isEmpty()) {
            return false;
        }

        // Generate salt and hash the password using BCrypt
        String salt = PasswordUtil.generateRandomSalt();
        String hashedPassword = PasswordUtil.hashPassword(user.getPassword());

        user.setSalt(salt);
        user.setPasswordHash(hashedPassword);

        boolean success = userDAO.create(user);
        if (success) {
            activityLogDAO.log(user.getId(), performerUsername, "USER_CREATED",
                    "Created user: " + user.getUsername() + " (" + user.getRole() + ")", clientIp);
        } else {
            validationErrors.add("Database error occurred while creating user.");
        }
        return success;
    }

    /**
     * Validates and updates an existing user profile.
     *
     * @param user           User details to update
     * @param performerUsername Admin/Manager modifying the user
     * @param clientIp       IP address of caller
     * @param validationErrors Output list to capture validation error messages
     * @return true if updated successfully
     */
    public boolean updateUser(User user, String performerUsername, String clientIp, List<String> validationErrors) {
        User existing = userDAO.findById(user.getId());
        if (existing == null) {
            validationErrors.add("User with ID " + user.getId() + " not found.");
            return false;
        }

        validateUserData(user, false, validationErrors);

        if (!validationErrors.isEmpty()) {
            return false;
        }

        // If a new password is provided, update password hash as well
        if (user.getPassword() != null && !user.getPassword().trim().isEmpty()) {
            if (user.getPassword().length() < 6) {
                validationErrors.add("Password must be at least 6 characters long.");
                return false;
            }
            String newSalt = PasswordUtil.generateRandomSalt();
            String newHash = PasswordUtil.hashPassword(user.getPassword());
            userDAO.updatePassword(user.getId(), newHash, newSalt);
        }

        boolean success = userDAO.update(user);
        if (success) {
            activityLogDAO.log(user.getId(), performerUsername, "USER_UPDATED",
                    "Updated user: " + existing.getUsername(), clientIp);
        } else {
            validationErrors.add("Database error occurred while updating user.");
        }
        return success;
    }

    /**
     * Quick status toggle (e.g. ACTIVE -> INACTIVE or vice versa).
     */
    public boolean toggleUserStatus(int id, String newStatus, String performerUsername, String clientIp) {
        User existing = userDAO.findById(id);
        if (existing == null) {
            return false;
        }

        String targetStatus = "ACTIVE".equalsIgnoreCase(newStatus) ? "ACTIVE" : "INACTIVE";
        boolean success = userDAO.updateStatus(id, targetStatus);
        if (success) {
            activityLogDAO.log(id, performerUsername, "STATUS_CHANGED",
                    "Changed status of " + existing.getUsername() + " to " + targetStatus, clientIp);
        }
        return success;
    }

    /**
     * Deletes a user by ID. Prevents deleting the last admin or self-deletion of current admin.
     */
    public boolean deleteUser(int id, String currentLoggedInUsername, String clientIp, List<String> errorMessages) {
        User targetUser = userDAO.findById(id);
        if (targetUser == null) {
            errorMessages.add("User not found.");
            return false;
        }

        // Prevent self-deletion
        if (targetUser.getUsername().equalsIgnoreCase(currentLoggedInUsername)) {
            errorMessages.add("Security violation: You cannot delete your own logged-in account.");
            return false;
        }

        // Prevent deleting the primary admin account
        if ("admin".equalsIgnoreCase(targetUser.getUsername())) {
            errorMessages.add("The default system administrator account cannot be deleted.");
            return false;
        }

        boolean success = userDAO.delete(id);
        if (success) {
            activityLogDAO.log(id, currentLoggedInUsername, "USER_DELETED",
                    "Deleted user: " + targetUser.getUsername(), clientIp);
        } else {
            errorMessages.add("Database error occurred while deleting user.");
        }
        return success;
    }

    private void validateUserData(User user, boolean isCreation, List<String> errors) {
        if (user == null) {
            errors.add("User payload is required.");
            return;
        }

        // Full Name Validation
        if (user.getFullName() == null || user.getFullName().trim().isEmpty()) {
            errors.add("Full Name is required.");
        } else if (user.getFullName().trim().length() > 100) {
            errors.add("Full Name cannot exceed 100 characters.");
        }

        // Username Validation (only on creation or if provided)
        if (isCreation) {
            if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
                errors.add("Username is required.");
            } else if (!USERNAME_PATTERN.matcher(user.getUsername()).matches()) {
                errors.add("Username must be 3-30 alphanumeric characters or underscores.");
            } else if (userDAO.isUsernameTaken(user.getUsername(), 0)) {
                errors.add("Username '" + user.getUsername() + "' is already taken.");
            }
        }

        // Email Validation
        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            errors.add("Email address is required.");
        } else if (!EMAIL_PATTERN.matcher(user.getEmail()).matches()) {
            errors.add("Invalid email format.");
        } else if (userDAO.isEmailTaken(user.getEmail(), isCreation ? 0 : user.getId())) {
            errors.add("Email '" + user.getEmail() + "' is already registered to another user.");
        }

        // Password Validation (Mandatory for creation, optional for updates)
        if (isCreation) {
            if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
                errors.add("Password is required for new users.");
            } else if (user.getPassword().length() < 6) {
                errors.add("Password must be at least 6 characters long.");
            }
        }

        // Role Validation
        String role = user.getRole();
        if (role == null || role.trim().isEmpty()) {
            user.setRole("USER");
        } else if (!role.equals("ADMIN") && !role.equals("MANAGER") && !role.equals("USER")) {
            errors.add("Invalid role specified. Must be ADMIN, MANAGER, or USER.");
        }

        // Status Validation
        String status = user.getStatus();
        if (status == null || status.trim().isEmpty()) {
            user.setStatus("ACTIVE");
        } else if (!status.equals("ACTIVE") && !status.equals("INACTIVE")) {
            errors.add("Invalid status specified. Must be ACTIVE or INACTIVE.");
        }
    }
}
