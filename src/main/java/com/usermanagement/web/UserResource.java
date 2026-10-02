package com.usermanagement.web;

import com.usermanagement.model.ApiResponse;
import com.usermanagement.model.User;
import com.usermanagement.service.AuthService;
import com.usermanagement.service.UserService;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * JAX-RS REST Web Service Endpoint for complete User CRUD operations.
 */
@Path("/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserResource {

    private final UserService userService = new UserService();

    /**
     * Retrieves paginated, sorted, and filtered users list.
     */
    @GET
    public Response getUsers(
            @QueryParam("search") String search,
            @QueryParam("role") String role,
            @QueryParam("status") String status,
            @QueryParam("sortBy") @DefaultValue("id") String sortBy,
            @QueryParam("sortOrder") @DefaultValue("ASC") String sortOrder,
            @QueryParam("page") @DefaultValue("1") int page,
            @QueryParam("pageSize") @DefaultValue("10") int pageSize) {

        List<User> users = userService.getUsers(search, role, status, sortBy, sortOrder, page, pageSize);
        int totalCount = userService.getTotalUsersCount(search, role, status);
        int totalPages = (int) Math.ceil((double) totalCount / pageSize);

        Map<String, Object> result = new HashMap<>();
        result.put("users", users);
        result.put("totalCount", totalCount);
        result.put("page", page);
        result.put("pageSize", pageSize);
        result.put("totalPages", Math.max(1, totalPages));

        return Response.ok(ApiResponse.success("Users retrieved successfully", result)).build();
    }

    /**
     * Retrieves dashboard summary metrics.
     */
    @GET
    @Path("/stats")
    public Response getStatistics() {
        Map<String, Integer> stats = userService.getDashboardStatistics();
        return Response.ok(ApiResponse.success("Statistics retrieved successfully", stats)).build();
    }

    /**
     * Retrieves a single user by primary key ID.
     */
    @GET
    @Path("/{id}")
    public Response getUserById(@PathParam("id") int id) {
        User user = userService.getUserById(id);
        if (user == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(ApiResponse.error("User with ID " + id + " was not found."))
                    .build();
        }
        return Response.ok(ApiResponse.success("User retrieved successfully", user)).build();
    }

    /**
     * Creates a new user record.
     */
    @POST
    public Response createUser(User newUser, @Context HttpServletRequest request) {
        User currentUser = AuthService.getCurrentUser(request);
        String performerUsername = (currentUser != null) ? currentUser.getUsername() : "SYSTEM";
        String clientIp = AuthService.getClientIp(request);

        // RBAC Check: Only ADMIN and MANAGER roles can create accounts
        if (currentUser != null && !"ADMIN".equalsIgnoreCase(currentUser.getRole())
                && !"MANAGER".equalsIgnoreCase(currentUser.getRole())) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity(ApiResponse.error("Permission denied. Only Admins and Managers can create users."))
                    .build();
        }

        List<String> validationErrors = new ArrayList<>();
        boolean success = userService.createUser(newUser, performerUsername, clientIp, validationErrors);

        if (!success) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(ApiResponse.error("Failed to create user", validationErrors))
                    .build();
        }

        return Response.status(Response.Status.CREATED)
                .entity(ApiResponse.success("User created successfully!", newUser))
                .build();
    }

    /**
     * Updates an existing user profile.
     */
    @PUT
    @Path("/{id}")
    public Response updateUser(@PathParam("id") int id, User updatedUser, @Context HttpServletRequest request) {
        User currentUser = AuthService.getCurrentUser(request);
        String performerUsername = (currentUser != null) ? currentUser.getUsername() : "SYSTEM";
        String clientIp = AuthService.getClientIp(request);

        if (updatedUser == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(ApiResponse.error("User details are required."))
                    .build();
        }

        // Set ID from path parameter
        updatedUser.setId(id);

        List<String> validationErrors = new ArrayList<>();
        boolean success = userService.updateUser(updatedUser, performerUsername, clientIp, validationErrors);

        if (!success) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(ApiResponse.error("Failed to update user", validationErrors))
                    .build();
        }

        return Response.ok(ApiResponse.success("User updated successfully!", updatedUser)).build();
    }

    /**
     * Deletes a user by ID.
     */
    @DELETE
    @Path("/{id}")
    public Response deleteUser(@PathParam("id") int id, @Context HttpServletRequest request) {
        User currentUser = AuthService.getCurrentUser(request);
        String currentLoggedInUsername = (currentUser != null) ? currentUser.getUsername() : "";
        String clientIp = AuthService.getClientIp(request);

        // Only ADMINs are allowed to delete users
        if (currentUser != null && !"ADMIN".equalsIgnoreCase(currentUser.getRole())) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity(ApiResponse.error("Permission denied. Only Admins can delete users."))
                    .build();
        }

        List<String> errorMessages = new ArrayList<>();
        boolean success = userService.deleteUser(id, currentLoggedInUsername, clientIp, errorMessages);

        if (!success) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(ApiResponse.error("Failed to delete user", errorMessages))
                    .build();
        }

        return Response.ok(ApiResponse.success("User deleted successfully.")).build();
    }

    /**
     * Quick status toggle endpoint.
     */
    @PUT
    @Path("/{id}/status")
    public Response updateStatus(@PathParam("id") int id, Map<String, String> body, @Context HttpServletRequest request) {
        String newStatus = body != null ? body.get("status") : null;
        if (newStatus == null || (!newStatus.equalsIgnoreCase("ACTIVE") && !newStatus.equalsIgnoreCase("INACTIVE"))) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(ApiResponse.error("Invalid status value. Must be 'ACTIVE' or 'INACTIVE'."))
                    .build();
        }

        User currentUser = AuthService.getCurrentUser(request);
        String performerUsername = (currentUser != null) ? currentUser.getUsername() : "SYSTEM";
        String clientIp = AuthService.getClientIp(request);

        boolean success = userService.toggleUserStatus(id, newStatus, performerUsername, clientIp);
        if (!success) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(ApiResponse.error("Failed to update user status."))
                    .build();
        }

        return Response.ok(ApiResponse.success("Status updated to " + newStatus.toUpperCase())).build();
    }
}
