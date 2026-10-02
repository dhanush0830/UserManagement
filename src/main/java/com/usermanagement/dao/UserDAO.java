package com.usermanagement.dao;

import com.usermanagement.model.User;

import java.util.List;
import java.util.Map;

/**
 * Data Access Object interface for User entities.
 */
public interface UserDAO {

    /**
     * Finds a user by primary key ID.
     */
    User findById(int id);

    /**
     * Finds a user by unique username (used during authentication).
     */
    User findByUsername(String username);

    /**
     * Finds a user by unique email address.
     */
    User findByEmail(String email);

    /**
     * Checks if a username is already taken by another user.
     */
    boolean isUsernameTaken(String username, int excludeUserId);

    /**
     * Checks if an email is already taken by another user.
     */
    boolean isEmailTaken(String email, int excludeUserId);

    /**
     * Retrieves paginated, filtered, and sorted user records.
     *
     * @param search       Keyword search matching username, full_name, or email
     * @param roleFilter   Filter by role (or null/empty for all)
     * @param statusFilter Filter by status (or null/empty for all)
     * @param sortBy       Column name to sort by (whitelisted)
     * @param sortOrder    'ASC' or 'DESC'
     * @param page         1-based page number
     * @param pageSize     Number of records per page
     * @return List of matching User instances
     */
    List<User> findAll(String search, String roleFilter, String statusFilter,
                       String sortBy, String sortOrder, int page, int pageSize);

    /**
     * Counts the total number of records matching the given filters.
     */
    int count(String search, String roleFilter, String statusFilter);

    /**
     * Creates a new user record.
     *
     * @param user User entity to insert
     * @return true if inserted successfully
     */
    boolean create(User user);

    /**
     * Updates an existing user's profile details.
     *
     * @param user User entity with updated fields
     * @return true if updated successfully
     */
    boolean update(User user);

    /**
     * Updates a user's password.
     */
    boolean updatePassword(int id, String passwordHash, String salt);

    /**
     * Updates a user's status (ACTIVE / INACTIVE).
     */
    boolean updateStatus(int id, String status);

    /**
     * Deletes a user by primary key ID.
     */
    boolean delete(int id);

    /**
     * Retrieves high-level dashboard metrics (Total, Active, Inactive, Admins).
     */
    Map<String, Integer> getStatistics();
}
