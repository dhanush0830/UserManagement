<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.usermanagement.model.User" %>
<%@ page import="com.usermanagement.service.AuthService" %>
<%@ page import="com.usermanagement.util.DBConnectionManager" %>
<%
    User loggedInUser = (User) session.getAttribute(AuthService.SESSION_USER_KEY);
    String userRole = (loggedInUser != null) ? loggedInUser.getRole() : "USER";
    String userFullName = (loggedInUser != null) ? loggedInUser.getFullName() : "User";
    String userInitial = (userFullName != null && !userFullName.isEmpty()) ? userFullName.substring(0, 1).toUpperCase() : "U";
    String activeDbType = DBConnectionManager.getInstance().getActiveDatabaseType();
%>
<!-- App Sidebar -->
<aside class="app-sidebar" id="appSidebar">
    <!-- Brand -->
    <div class="sidebar-brand">
        <div class="brand-hexagon">
            <i class="bi bi-shield-check"></i>
        </div>
        <div>
            <div class="brand-title">NexusPortal</div>
            <div class="brand-subtitle">Governance Console</div>
        </div>
    </div>

    <!-- Navigation List -->
    <nav class="sidebar-nav">
        <div class="nav-section-label">Identity Management</div>
        <a href="${pageContext.request.contextPath}/home.jsp" class="sidebar-link active">
            <i class="bi bi-people-fill"></i>
            <span>User Directory</span>
        </a>
        <a href="javascript:void(0)" class="sidebar-link" onclick="showToast('Audit trails recording live into activity_logs table.', 'info')">
            <i class="bi bi-activity"></i>
            <span>Security Audits</span>
        </a>

        <div class="nav-section-label mt-3">Infrastructure</div>
        <div class="p-3 mx-2 rounded-3" style="background: #f8fafc; border: 1px solid var(--border-subtle);">
            <div class="d-flex align-items-center justify-content-between mb-1">
                <span class="text-muted small" style="font-size: 0.72rem;">PERSISTENCE</span>
                <span class="badge bg-success-subtle text-success py-0" style="font-size: 0.65rem;">CONNECTED</span>
            </div>
            <div class="fw-semibold text-primary small text-truncate">
                <i class="bi bi-database me-1 text-primary"></i><%= activeDbType %>
            </div>
            <div class="text-muted small mt-1" style="font-size: 0.7rem;">HikariCP Active Pool</div>
        </div>
    </nav>

    <!-- Sidebar User Footer -->
    <div class="sidebar-footer">
        <div class="d-flex align-items-center gap-2">
            <div class="user-avatar-bubble" style="width: 34px; height: 34px; font-size: 0.8rem;"><%= userInitial %></div>
            <div class="flex-grow-1 overflow-hidden">
                <div class="text-dark fw-semibold small text-truncate"><%= userFullName %></div>
                <div class="text-muted small text-truncate" style="font-size: 0.72rem;">@<%= (loggedInUser != null) ? loggedInUser.getUsername() : "" %></div>
            </div>
            <a href="javascript:void(0)" class="btn-action-icon btn-delete" title="Sign Out" id="sidebarLogoutBtn">
                <i class="bi bi-box-arrow-right"></i>
            </a>
        </div>
    </div>
</aside>

<!-- Topbar Navigation -->
<header class="app-topbar">
    <div class="d-flex align-items-center gap-3">
        <!-- Mobile Sidebar Toggler -->
        <button class="btn btn-dark-outline d-lg-none py-1 px-2" type="button" id="sidebarToggle">
            <i class="bi bi-list fs-5"></i>
        </button>

        <!-- Page Breadcrumb / Status -->
        <div class="d-flex align-items-center gap-2">
            <span class="text-muted small">Console</span>
            <i class="bi bi-chevron-right text-muted" style="font-size: 0.7rem;"></i>
            <span class="text-dark small fw-medium">User Directory &amp; RBAC</span>
        </div>
    </div>

    <!-- Right Controls -->
    <div class="d-flex align-items-center gap-3">
        <!-- Live Session Indicator with Pulse Dot -->
        <div class="session-live-pill d-none d-sm-inline-flex" title="Session automatically terminates after 30 minutes of inactivity">
            <span class="pulse-dot"></span>
            <span>Session: <strong id="sessionTimerText" class="text-primary font-monospace">30:00</strong></span>
        </div>

        <!-- Role Badge -->
        <span class="badge-aurora badge-role-<%= userRole.toLowerCase() %> d-none d-md-inline-flex">
            <%= userRole %>
        </span>

        <!-- User Menu Dropdown -->
        <div class="dropdown">
            <button class="btn p-0 border-0 dropdown-toggle d-flex align-items-center gap-2"
                    type="button" id="topUserDropdown" data-bs-toggle="dropdown" aria-expanded="false">
                <div class="user-avatar-bubble"><%= userInitial %></div>
            </button>
            <ul class="dropdown-menu dropdown-menu-end modal-content-dark shadow-lg border mt-2 py-2" aria-labelledby="topUserDropdown">
                <li class="px-3 py-2 border-bottom" style="border-color: var(--border-subtle) !important;">
                    <div class="fw-bold text-dark small"><%= userFullName %></div>
                    <div class="text-muted small">@<%= (loggedInUser != null) ? loggedInUser.getUsername() : "" %></div>
                    <div class="text-muted small font-monospace" style="font-size: 0.7rem;"><%= (loggedInUser != null) ? loggedInUser.getEmail() : "" %></div>
                </li>
                <li>
                    <a class="dropdown-item py-2 small d-flex align-items-center gap-2" href="javascript:void(0)" onclick="viewMyProfile()">
                        <i class="bi bi-person-circle text-info"></i> Account Profile
                    </a>
                </li>
                <li><hr class="dropdown-divider my-1" style="border-color: var(--border-subtle);"></li>
                <li>
                    <a class="dropdown-item py-2 small text-danger d-flex align-items-center gap-2" href="javascript:void(0)" id="navLogoutBtn">
                        <i class="bi bi-power"></i> Terminate Session
                    </a>
                </li>
            </ul>
        </div>
    </div>
</header>
