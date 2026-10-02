<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.usermanagement.model.User" %>
<%@ page import="com.usermanagement.service.AuthService" %>
<%
    User currentUser = (User) session.getAttribute(AuthService.SESSION_USER_KEY);
    if (currentUser == null) {
        response.sendRedirect(request.getContextPath() + "/login.jsp");
        return;
    }
    String currentUserRole = currentUser.getRole();
    boolean canManageUsers = "ADMIN".equalsIgnoreCase(currentUserRole) || "MANAGER".equalsIgnoreCase(currentUserRole);
    boolean isAdmin = "ADMIN".equalsIgnoreCase(currentUserRole);
%>
<jsp:include page="includes/header.jsp">
    <jsp:param name="title" value="NexusPortal - User Directory" />
</jsp:include>

<div class="app-shell">
    <!-- Sidebar + Topbar Component -->
    <jsp:include page="includes/navbar.jsp" />

    <!-- Main Workspace -->
    <main class="app-main">
        <div class="container-fluid px-3 px-lg-4 py-4">
            <!-- Header Section: Greeting & Actions -->
            <div class="d-flex flex-column flex-md-row justify-content-between align-items-start align-items-md-center gap-3 mb-4">
                <div>
                    <h2 class="fw-bold mb-1" style="letter-spacing: -0.02em; color: var(--text-primary);">Directory &amp; Access Controls</h2>
                    <p class="text-secondary small mb-0">
                        Administer corporate accounts, role assignments, and session states.
                    </p>
                </div>
                <div class="d-flex align-items-center gap-2">
                    <button type="button" class="btn btn-dark-outline d-flex align-items-center gap-2" id="btnRefreshGrid">
                        <i class="bi bi-arrow-repeat" id="refreshIcon"></i>
                        <span>Sync</span>
                    </button>
                    <% if (canManageUsers) { %>
                        <button type="button" class="btn btn-aurora d-flex align-items-center gap-2" data-bs-toggle="modal" data-bs-target="#addUserModal">
                            <i class="bi bi-plus-lg"></i>
                            <span>Create Account</span>
                        </button>
                    <% } %>
                </div>
            </div>

            <!-- Glowing KPI Statistics Cards -->
            <div class="row g-3 mb-4">
                <!-- Card 1: Total Users -->
                <div class="col-12 col-sm-6 col-xl-3">
                    <div class="kpi-card kpi-cyan">
                        <div class="d-flex align-items-center justify-content-between mb-2">
                            <span class="text-secondary small fw-medium" style="font-size: 0.75rem; letter-spacing: 0.05em; text-transform: uppercase;">
                                Total Accounts
                            </span>
                            <div class="kpi-icon-box kpi-icon-cyan">
                                <i class="bi bi-people-fill"></i>
                            </div>
                        </div>
                        <div class="kpi-value font-monospace" id="statTotalUsers">--</div>
                        <div class="text-muted small mt-2 d-flex align-items-center gap-1" style="font-size: 0.72rem;">
                            <i class="bi bi-check2 text-info"></i> Synchronized with MySQL
                        </div>
                    </div>
                </div>

                <!-- Card 2: Active Accounts -->
                <div class="col-12 col-sm-6 col-xl-3">
                    <div class="kpi-card kpi-emerald">
                        <div class="d-flex align-items-center justify-content-between mb-2">
                            <span class="text-secondary small fw-medium" style="font-size: 0.75rem; letter-spacing: 0.05em; text-transform: uppercase;">
                                Active Access
                            </span>
                            <div class="kpi-icon-box kpi-icon-emerald">
                                <i class="bi bi-shield-check"></i>
                            </div>
                        </div>
                        <div class="kpi-value font-monospace text-success" id="statActiveUsers" style="color: #16a34a;">--</div>
                        <div class="text-muted small mt-2 d-flex align-items-center gap-1" style="font-size: 0.72rem;">
                            <span class="pulse-dot" style="width: 6px; height: 6px;"></span> Can sign in to system
                        </div>
                    </div>
                </div>

                <!-- Card 3: Inactive Accounts -->
                <div class="col-12 col-sm-6 col-xl-3">
                    <div class="kpi-card kpi-rose">
                        <div class="d-flex align-items-center justify-content-between mb-2">
                            <span class="text-secondary small fw-medium" style="font-size: 0.75rem; letter-spacing: 0.05em; text-transform: uppercase;">
                                Suspended
                            </span>
                            <div class="kpi-icon-box kpi-icon-rose">
                                <i class="bi bi-slash-circle-fill"></i>
                            </div>
                        </div>
                        <div class="kpi-value font-monospace text-danger" id="statInactiveUsers">--</div>
                        <div class="text-muted small mt-2 d-flex align-items-center gap-1" style="font-size: 0.72rem;">
                            <i class="bi bi-lock-fill text-danger"></i> Access restricted
                        </div>
                    </div>
                </div>

                <!-- Card 4: Administrators -->
                <div class="col-12 col-sm-6 col-xl-3">
                    <div class="kpi-card kpi-amber">
                        <div class="d-flex align-items-center justify-content-between mb-2">
                            <span class="text-secondary small fw-medium" style="font-size: 0.75rem; letter-spacing: 0.05em; text-transform: uppercase;">
                                Admins
                            </span>
                            <div class="kpi-icon-box kpi-icon-amber">
                                <i class="bi bi-award-fill"></i>
                            </div>
                        </div>
                        <div class="kpi-value font-monospace" id="statAdmins" style="color: #d97706;">--</div>
                        <div class="text-muted small mt-2 d-flex align-items-center gap-1" style="font-size: 0.72rem;">
                            <i class="bi bi-key-fill text-warning"></i> Privileged governance
                        </div>
                    </div>
                </div>
            </div>

            <!-- Main Data Grid Panel -->
            <div class="data-panel">
                <!-- Overlay Loader -->
                <div id="gridLoadingOverlay" class="table-loading-overlay d-none">
                    <div class="spinner-border text-primary" role="status">
                        <span class="visually-hidden">Syncing records...</span>
                    </div>
                </div>

                <!-- Filter & Search Toolbar -->
                <div class="panel-header">
                    <div class="row g-2 align-items-center">
                        <!-- Search Input -->
                        <div class="col-12 col-md-4">
                            <div class="input-group">
                                <span class="input-group-text input-dark border-end-0 text-muted">
                                    <i class="bi bi-search"></i>
                                </span>
                                <input type="text" class="form-control input-dark border-start-0 ps-0" id="searchInput"
                                       placeholder="Search username, name, email...">
                                <button class="btn btn-dark-outline border-start-0 d-none" type="button" id="btnClearSearch">
                                    <i class="bi bi-x-lg text-muted"></i>
                                </button>
                            </div>
                        </div>

                        <!-- Role Filter -->
                        <div class="col-6 col-md-2">
                            <select class="form-select select-dark" id="roleFilter">
                                <option value="ALL">All Roles</option>
                                <option value="ADMIN">Admin</option>
                                <option value="MANAGER">Manager</option>
                                <option value="USER">User</option>
                            </select>
                        </div>

                        <!-- Status Filter -->
                        <div class="col-6 col-md-2">
                            <select class="form-select select-dark" id="statusFilter">
                                <option value="ALL">All Statuses</option>
                                <option value="ACTIVE">Active</option>
                                <option value="INACTIVE">Inactive</option>
                            </select>
                        </div>

                        <!-- Quick Filter Chips & Page Size -->
                        <div class="col-12 col-md-4 d-flex justify-content-md-end align-items-center gap-2 mt-2 mt-md-0">
                            <span class="text-muted small">Show:</span>
                            <select class="form-select select-dark w-auto" id="pageSizeSelect">
                                <option value="5">5</option>
                                <option value="10" selected>10</option>
                                <option value="25">25</option>
                                <option value="50">50</option>
                            </select>
                            <span class="text-muted small">records</span>
                        </div>
                    </div>
                </div>

                <!-- Table Content -->
                <div class="table-responsive">
                    <table class="table table-dark-custom">
                        <thead>
                            <tr>
                                <th class="sortable-th" data-sort="id" style="width: 80px;">
                                    ID <i class="bi bi-arrow-down-up" id="sortIcon_id"></i>
                                </th>
                                <th class="sortable-th" data-sort="full_name">
                                    Identity <i class="bi bi-arrow-down-up" id="sortIcon_full_name"></i>
                                </th>
                                <th class="sortable-th" data-sort="email">
                                    Email Address <i class="bi bi-arrow-down-up" id="sortIcon_email"></i>
                                </th>
                                <th class="sortable-th text-center" data-sort="role" style="width: 120px;">
                                    Role <i class="bi bi-arrow-down-up" id="sortIcon_role"></i>
                                </th>
                                <th class="sortable-th text-center" data-sort="status" style="width: 130px;">
                                    Status <i class="bi bi-arrow-down-up" id="sortIcon_status"></i>
                                </th>
                                <th class="sortable-th" data-sort="created_at" style="width: 170px;">
                                    Registered <i class="bi bi-arrow-down-up" id="sortIcon_created_at"></i>
                                </th>
                                <th class="text-end" style="width: 140px;">Operations</th>
                            </tr>
                        </thead>
                        <tbody id="userTableBody">
                            <!-- Populated dynamically via AJAX -->
                        </tbody>
                    </table>
                </div>

                <!-- Empty State -->
                <div id="emptyState" class="text-center py-5 d-none">
                    <i class="bi bi-filter-circle text-muted fs-1 mb-2 d-block"></i>
                    <h5 class="fw-semibold" style="color: var(--text-primary);">No Matching Accounts</h5>
                    <p class="text-muted small mb-0">Try clearing filters or adjusting your search keyword.</p>
                </div>

                <!-- Footer Pagination -->
                <div class="p-3 border-top d-flex flex-column flex-md-row justify-content-between align-items-center gap-3"
                     style="border-color: var(--border-subtle) !important; background: var(--bg-surface);">
                    <div class="text-muted small font-monospace" id="paginationInfo">
                        Showing 0 to 0 of 0 entries
                    </div>
                    <nav aria-label="Table pagination">
                        <ul class="pagination pagination-sm mb-0 gap-1" id="paginationControls">
                            <!-- Populated dynamically via AJAX -->
                        </ul>
                    </nav>
                </div>
            </div>
        </div>
    </main>
</div>

<!-- ===================================================================
     MODAL: Add New Account
     =================================================================== -->
<div class="modal fade" id="addUserModal" tabindex="-1" aria-labelledby="addUserModalLabel" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content modal-content-dark">
            <div class="modal-header modal-header-dark">
                <h5 class="modal-title fw-bold" style="color: var(--text-primary);" id="addUserModalLabel">
                    <i class="bi bi-person-plus-fill text-primary me-2"></i>Create New Account
                </h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
            </div>
            <form id="addUserForm" novalidate>
                <div class="modal-body p-4">
                    <div id="addModalAlert" class="alert alert-danger d-none small py-2 mb-3 bg-opacity-10 border border-danger"></div>

                    <div class="mb-3">
                        <label for="addUsername" class="form-label small fw-semibold text-secondary">Username *</label>
                        <input type="text" class="form-control input-dark" id="addUsername" name="username"
                               placeholder="e.g. jsmith (3-30 alphanumeric chars)" required autocomplete="off">
                    </div>

                    <div class="mb-3">
                        <label for="addFullName" class="form-label small fw-semibold text-secondary">Full Name *</label>
                        <input type="text" class="form-control input-dark" id="addFullName" name="fullName"
                               placeholder="e.g. Jane Smith" required autocomplete="off">
                    </div>

                    <div class="mb-3">
                        <label for="addEmail" class="form-label small fw-semibold text-secondary">Email Address *</label>
                        <input type="email" class="form-control input-dark" id="addEmail" name="email"
                               placeholder="e.g. jane.smith@enterprise.com" required autocomplete="off">
                    </div>

                    <div class="mb-3">
                        <label for="addPassword" class="form-label small fw-semibold text-secondary">Password *</label>
                        <input type="password" class="form-control input-dark" id="addPassword" name="password"
                               placeholder="Minimum 6 characters" required autocomplete="new-password">
                    </div>

                    <div class="row g-3">
                        <div class="col-6">
                            <label for="addRole" class="form-label small fw-semibold text-secondary">Role</label>
                            <select class="form-select select-dark" id="addRole" name="role">
                                <option value="USER" selected>USER</option>
                                <option value="MANAGER">MANAGER</option>
                                <% if (isAdmin) { %>
                                    <option value="ADMIN">ADMIN</option>
                                <% } %>
                            </select>
                        </div>
                        <div class="col-6">
                            <label for="addStatus" class="form-label small fw-semibold text-secondary">Status</label>
                            <select class="form-select select-dark" id="addStatus" name="status">
                                <option value="ACTIVE" selected>ACTIVE</option>
                                <option value="INACTIVE">INACTIVE</option>
                            </select>
                        </div>
                    </div>
                </div>
                <div class="modal-footer modal-footer-dark">
                    <button type="button" class="btn btn-dark-outline" data-bs-dismiss="modal">Cancel</button>
                    <button type="submit" class="btn btn-aurora d-flex align-items-center gap-2" id="btnSubmitAddUser">
                        <span id="btnAddText">Create Account</span>
                        <span id="btnAddSpinner" class="spinner-border spinner-border-sm d-none" role="status" aria-hidden="true"></span>
                    </button>
                </div>
            </form>
        </div>
    </div>
</div>

<!-- ===================================================================
     MODAL: Update Account
     =================================================================== -->
<div class="modal fade" id="editUserModal" tabindex="-1" aria-labelledby="editUserModalLabel" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered">
        <div class="modal-content modal-content-dark">
            <div class="modal-header modal-header-dark">
                <h5 class="modal-title fw-bold" style="color: var(--text-primary);" id="editUserModalLabel">
                    <i class="bi bi-pencil-square text-primary me-2"></i>Edit Account
                </h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
            </div>
            <form id="editUserForm" novalidate>
                <input type="hidden" id="editUserId" name="id">
                <div class="modal-body p-4">
                    <div id="editModalAlert" class="alert alert-danger d-none small py-2 mb-3 bg-opacity-10 border border-danger"></div>

                    <div class="mb-3">
                        <label for="editUsername" class="form-label small fw-semibold text-secondary">Username (Immutable)</label>
                        <input type="text" class="form-control input-dark opacity-50" id="editUsername" readonly disabled>
                    </div>

                    <div class="mb-3">
                        <label for="editFullName" class="form-label small fw-semibold text-secondary">Full Name *</label>
                        <input type="text" class="form-control input-dark" id="editFullName" name="fullName" required>
                    </div>

                    <div class="mb-3">
                        <label for="editEmail" class="form-label small fw-semibold text-secondary">Email Address *</label>
                        <input type="email" class="form-control input-dark" id="editEmail" name="email" required>
                    </div>

                    <div class="mb-3">
                        <label for="editPassword" class="form-label small fw-semibold text-secondary">
                            Reset Password <span class="text-muted fw-normal">(Optional)</span>
                        </label>
                        <input type="password" class="form-control input-dark" id="editPassword" name="password"
                               placeholder="Leave blank to retain current password" autocomplete="new-password">
                    </div>

                    <div class="row g-3">
                        <div class="col-6">
                            <label for="editRole" class="form-label small fw-semibold text-secondary">Role</label>
                            <select class="form-select select-dark" id="editRole" name="role">
                                <option value="USER">USER</option>
                                <option value="MANAGER">MANAGER</option>
                                <% if (isAdmin) { %>
                                    <option value="ADMIN">ADMIN</option>
                                <% } %>
                            </select>
                        </div>
                        <div class="col-6">
                            <label for="editStatus" class="form-label small fw-semibold text-secondary">Status</label>
                            <select class="form-select select-dark" id="editStatus" name="status">
                                <option value="ACTIVE">ACTIVE</option>
                                <option value="INACTIVE">INACTIVE</option>
                            </select>
                        </div>
                    </div>
                </div>
                <div class="modal-footer modal-footer-dark">
                    <button type="button" class="btn btn-dark-outline" data-bs-dismiss="modal">Cancel</button>
                    <button type="submit" class="btn btn-aurora d-flex align-items-center gap-2" id="btnSubmitEditUser">
                        <span id="btnEditText">Save Changes</span>
                        <span id="btnEditSpinner" class="spinner-border spinner-border-sm d-none" role="status" aria-hidden="true"></span>
                    </button>
                </div>
            </form>
        </div>
    </div>
</div>

<!-- ===================================================================
     MODAL: Delete Confirmation
     =================================================================== -->
<div class="modal fade" id="deleteUserModal" tabindex="-1" aria-labelledby="deleteUserModalLabel" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered modal-sm">
        <div class="modal-content modal-content-dark">
            <div class="modal-body text-center p-4">
                <i class="bi bi-exclamation-triangle-fill text-danger fs-1 mb-2 d-block"></i>
                <h5 class="fw-bold mb-2" style="color: var(--text-primary);">Confirm Deletion</h5>
                <p class="text-secondary small mb-3">
                    Are you sure you want to permanently delete account <strong id="deleteTargetUsername" class="text-dark"></strong>?
                </p>
                <input type="hidden" id="deleteTargetId">
                <div class="d-flex justify-content-center gap-2">
                    <button type="button" class="btn btn-dark-outline px-3" data-bs-dismiss="modal">Cancel</button>
                    <button type="button" class="btn btn-danger px-3 d-flex align-items-center gap-2" id="btnConfirmDelete">
                        <span id="btnDeleteText">Delete</span>
                        <span id="btnDeleteSpinner" class="spinner-border spinner-border-sm d-none" role="status" aria-hidden="true"></span>
                    </button>
                </div>
            </div>
        </div>
    </div>
</div>

<script>
    window.CURRENT_USER = {
        id: <%= currentUser.getId() %>,
        username: "<%= currentUser.getUsername() %>",
        role: "<%= currentUser.getRole() %>",
        canManage: <%= canManageUsers %>,
        isAdmin: <%= isAdmin %>
    };
</script>

<jsp:include page="includes/footer.jsp" />

<script src="${pageContext.request.contextPath}/js/users.js"></script>
