package com.usermanagement.web;

import com.usermanagement.model.ApiResponse;
import com.usermanagement.model.User;
import com.usermanagement.service.AuthService;
import com.usermanagement.service.UserService;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * REST Endpoint for Authentication and Session Lifecycle.
 */
@Path("/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthResource {

    private final AuthService authService = new AuthService();
    private final UserService userService = new UserService();

    public static class LoginRequest {
        private String username;
        private String password;

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    public static class RegisterRequest {
        private String username;
        private String password;
        private String fullName;
        private String email;

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
    }

    /**
     * Authenticates user and establishes HTTP Session.
     */
    @POST
    @Path("/login")
    public Response login(LoginRequest request, @Context HttpServletRequest httpRequest) {
        if (request == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(ApiResponse.error("Request body is required."))
                    .build();
        }

        try {
            User user = authService.login(request.getUsername(), request.getPassword(), httpRequest);
            return Response.ok(ApiResponse.success("Authentication successful! Welcome, " + user.getFullName(), user))
                    .build();
        } catch (AuthService.AuthenticationException e) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(ApiResponse.error(e.getMessage()))
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(ApiResponse.error("Server error: " + (e.getMessage() != null ? e.getMessage() : "Unknown error during login.")))
                    .build();
        }
    }

    /**
     * Invalidates active session and logs out user.
     */
    @POST
    @Path("/logout")
    public Response logout(@Context HttpServletRequest httpRequest) {
        authService.logout(httpRequest);
        return Response.ok(ApiResponse.success("Logged out successfully.")).build();
    }

    /**
     * Self-registration: creates a new USER-role account.
     */
    @POST
    @Path("/register")
    public Response register(RegisterRequest request, @Context HttpServletRequest httpRequest) {
        if (request == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(ApiResponse.error("Request body is required."))
                    .build();
        }
        User newUser = new User();
        newUser.setUsername(request.getUsername());
        newUser.setPassword(request.getPassword());
        newUser.setFullName(request.getFullName());
        newUser.setEmail(request.getEmail());
        newUser.setRole("USER");
        newUser.setStatus("ACTIVE");

        java.util.List<String> errors = new java.util.ArrayList<>();
        String clientIp = AuthService.getClientIp(httpRequest);
        boolean created = userService.createUser(newUser, "SELF_REGISTER", clientIp, errors);
        if (created) {
            return Response.status(Response.Status.CREATED)
                    .entity(ApiResponse.success("Account created successfully! You can now sign in."))
                    .build();
        } else {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(ApiResponse.error(errors.isEmpty() ? "Registration failed." : String.join(" ", errors)))
                    .build();
        }
    }

    /**
     * Returns details of currently logged-in user from active session.
     */
    @GET
    @Path("/me")
    public Response getCurrentUser(@Context HttpServletRequest httpRequest) {
        User user = AuthService.getCurrentUser(httpRequest);
        if (user == null) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(ApiResponse.error("Not authenticated."))
                    .build();
        }
        return Response.ok(ApiResponse.success("Current user profile", user)).build();
    }
}
