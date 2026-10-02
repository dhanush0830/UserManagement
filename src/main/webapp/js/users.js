/**
 * NexusPortal - User Data Grid Controller
 * Dark Aurora UI & AJAX CRUD Orchestrator
 */

(function ($) {
    'use strict';

    // Grid State
    const state = {
        search: '',
        role: 'ALL',
        status: 'ALL',
        sortBy: 'id',
        sortOrder: 'ASC',
        page: 1,
        pageSize: 10,
        totalCount: 0,
        totalPages: 1
    };

    const ctx = window.APP_CONTEXT || '';
    let searchDebounceTimer = null;

    $(document).ready(function () {
        loadUsers();
        loadStats();
        bindEventHandlers();
    });

    function bindEventHandlers() {
        // Sync / Refresh button
        $('#btnRefreshGrid').on('click', function () {
            const icon = $('#refreshIcon');
            icon.addClass('spin-animation');
            loadStats();
            loadUsers(function () {
                icon.removeClass('spin-animation');
                showToast('Grid synchronized with database', 'info');
            });
        });

        // Search Input with 300ms debounce
        $('#searchInput').on('input', function () {
            const val = $(this).val().trim();
            $('#btnClearSearch').toggleClass('d-none', val.length === 0);

            clearTimeout(searchDebounceTimer);
            searchDebounceTimer = setTimeout(function () {
                state.search = val;
                state.page = 1;
                loadUsers();
            }, 300);
        });

        $('#btnClearSearch').on('click', function () {
            $('#searchInput').val('').trigger('input').focus();
        });

        // Role Filter Dropdown
        $('#roleFilter').on('change', function () {
            state.role = $(this).val();
            state.page = 1;
            loadUsers();
        });

        // Status Filter Dropdown
        $('#statusFilter').on('change', function () {
            state.status = $(this).val();
            state.page = 1;
            loadUsers();
        });

        // Page Size Selector
        $('#pageSizeSelect').on('change', function () {
            state.pageSize = parseInt($(this).val(), 10) || 10;
            state.page = 1;
            loadUsers();
        });

        // Column Sorting
        $('.sortable-th').on('click', function () {
            const column = $(this).data('sort');
            if (state.sortBy === column) {
                state.sortOrder = (state.sortOrder === 'ASC') ? 'DESC' : 'ASC';
            } else {
                state.sortBy = column;
                state.sortOrder = 'ASC';
            }
            updateSortIcons();
            loadUsers();
        });

        // Add User Form (AJAX POST)
        $('#addUserForm').on('submit', function (e) {
            e.preventDefault();
            handleAddUser();
        });

        // Edit User Form (AJAX PUT)
        $('#editUserForm').on('submit', function (e) {
            e.preventDefault();
            handleEditUser();
        });

        // Delete Confirm (AJAX DELETE)
        $('#btnConfirmDelete').on('click', function () {
            handleDeleteUser();
        });

        // Modal Resets
        $('#addUserModal').on('hidden.bs.modal', function () {
            const form = $('#addUserForm')[0];
            if (form) form.reset();
            $('#addModalAlert').addClass('d-none').empty();
        });

        $('#editUserModal').on('hidden.bs.modal', function () {
            $('#editModalAlert').addClass('d-none').empty();
        });
    }

    // Load Users via AJAX (GET /api/users)
    function loadUsers(callback) {
        $('#gridLoadingOverlay').removeClass('d-none');

        const params = {
            search: state.search,
            role: state.role,
            status: state.status,
            sortBy: state.sortBy,
            sortOrder: state.sortOrder,
            page: state.page,
            pageSize: state.pageSize
        };

        $.ajax({
            url: ctx + '/api/users',
            type: 'GET',
            data: params,
            dataType: 'json',
            success: function (response) {
                if (response && response.success && response.data) {
                    const data = response.data;
                    state.totalCount = data.totalCount;
                    state.totalPages = data.totalPages;
                    renderTable(data.users || []);
                    renderPagination(data);
                } else {
                    showToast(response.message || 'Error loading records', 'danger');
                }
            },
            error: function () {
                showToast('Unable to connect to service.', 'danger');
            },
            complete: function () {
                $('#gridLoadingOverlay').addClass('d-none');
                if (typeof callback === 'function') callback();
            }
        });
    }

    // Load KPI Stats (GET /api/users/stats)
    function loadStats() {
        $.ajax({
            url: ctx + '/api/users/stats',
            type: 'GET',
            dataType: 'json',
            success: function (response) {
                if (response && response.success && response.data) {
                    const s = response.data;
                    $('#statTotalUsers').text(s.total ?? 0);
                    $('#statActiveUsers').text(s.active ?? 0);
                    $('#statInactiveUsers').text(s.inactive ?? 0);
                    $('#statAdmins').text(s.admins ?? 0);
                }
            }
        });
    }

    // Render Table in Dark Aurora Style
    function renderTable(users) {
        const tbody = $('#userTableBody');
        tbody.empty();

        if (!users || users.length === 0) {
            $('#emptyState').removeClass('d-none');
            return;
        }

        $('#emptyState').addClass('d-none');

        const currentUser = window.CURRENT_USER || {};
        const isAdmin = currentUser.isAdmin === true;
        const canManage = currentUser.canManage === true;

        users.forEach(function (user) {
            const initial = escapeHtml((user.fullName || 'U').charAt(0).toUpperCase());
            const safeUsername = escapeHtml(user.username);
            const safeFullName = escapeHtml(user.fullName);
            const safeEmail = escapeHtml(user.email);
            const role = escapeHtml(user.role || 'USER');
            const status = (user.status || 'ACTIVE').toUpperCase();
            const isSelf = (currentUser.id === user.id);
            const isTargetAdmin = ('admin' === safeUsername.toLowerCase());

            const roleBadgeClass = 'badge-role-' + role.toLowerCase();
            const statusBadgeClass = (status === 'ACTIVE') ? 'badge-active' : 'badge-inactive';
            const dotColor = (status === 'ACTIVE') ? 'var(--accent)' : 'var(--danger)';
            const formattedDate = formatDate(user.createdAt);

            // Operation Buttons
            let actionButtonsHtml = '<div class="d-flex justify-content-end gap-1">';

            // View Details
            actionButtonsHtml += `
                <button type="button" class="btn-action-icon"
                        title="View Details" onclick="viewUserDetails(${user.id})">
                    <i class="bi bi-eye"></i>
                </button>
            `;

            // Edit
            if (canManage) {
                actionButtonsHtml += `
                    <button type="button" class="btn-action-icon btn-edit"
                            title="Edit Account" onclick="openEditModal(${user.id})">
                        <i class="bi bi-pencil"></i>
                    </button>
                `;
            }

            // Status Toggle
            if (canManage && !isTargetAdmin) {
                const nextStatus = (status === 'ACTIVE') ? 'INACTIVE' : 'ACTIVE';
                const toggleTitle = (status === 'ACTIVE') ? 'Deactivate' : 'Activate';
                const toggleIcon = (status === 'ACTIVE') ? 'bi-toggle-on text-success' : 'bi-toggle-off text-muted';

                actionButtonsHtml += `
                    <button type="button" class="btn-action-icon"
                            title="${toggleTitle}" onclick="toggleUserStatus(${user.id}, '${nextStatus}')">
                        <i class="bi ${toggleIcon}"></i>
                    </button>
                `;
            }

            // Delete (Admin only)
            if (isAdmin && !isSelf && !isTargetAdmin) {
                actionButtonsHtml += `
                    <button type="button" class="btn-action-icon btn-delete"
                            title="Delete Account" onclick="openDeleteModal(${user.id}, '${safeUsername}')">
                        <i class="bi bi-trash3"></i>
                    </button>
                `;
            }

            actionButtonsHtml += '</div>';

            const row = `
                <tr>
                    <td class="font-monospace text-secondary">#${user.id}</td>
                    <td>
                        <div class="user-identity">
                            <div class="user-glyph">${initial}</div>
                            <div>
                                <div class="fw-semibold text-white">${safeFullName}</div>
                                <div class="text-muted small">@${safeUsername}</div>
                            </div>
                        </div>
                    </td>
                    <td>
                        <span class="font-monospace text-secondary small">${safeEmail}</span>
                    </td>
                    <td class="text-center">
                        <span class="badge-aurora ${roleBadgeClass}">${role}</span>
                    </td>
                    <td class="text-center">
                        <span class="badge-aurora ${statusBadgeClass}">
                            <span class="pulse-dot" style="width: 6px; height: 6px; background: ${dotColor};"></span>
                            ${status}
                        </span>
                    </td>
                    <td>
                        <span class="font-monospace text-secondary small">${formattedDate}</span>
                    </td>
                    <td class="text-end">
                        ${actionButtonsHtml}
                    </td>
                </tr>
            `;

            tbody.append(row);
        });
    }

    // Render Pagination Controls
    function renderPagination(data) {
        const total = data.totalCount || 0;
        const page = data.page || 1;
        const pageSize = data.pageSize || 10;
        const totalPages = data.totalPages || 1;

        const startRecord = (total === 0) ? 0 : (page - 1) * pageSize + 1;
        const endRecord = Math.min(page * pageSize, total);
        $('#paginationInfo').text(`Showing ${startRecord} to ${endRecord} of ${total} accounts`);

        const controls = $('#paginationControls');
        controls.empty();

        if (totalPages <= 1) return;

        const prevDisabled = (page <= 1) ? 'disabled opacity-25' : '';
        controls.append(`
            <li class="page-item ${prevDisabled}">
                <a class="page-link input-dark border-0 rounded" href="javascript:void(0)" onclick="goToPage(${page - 1})">
                    <i class="bi bi-chevron-left"></i>
                </a>
            </li>
        `);

        const maxPagesToShow = 5;
        let startPage = Math.max(1, page - 2);
        let endPage = Math.min(totalPages, startPage + maxPagesToShow - 1);

        if (endPage - startPage < maxPagesToShow - 1) {
            startPage = Math.max(1, endPage - maxPagesToShow + 1);
        }

        for (let p = startPage; p <= endPage; p++) {
            const activeClass = (p === page) ? 'btn-aurora text-white' : 'input-dark text-secondary';
            controls.append(`
                <li class="page-item">
                    <a class="page-link border-0 rounded ${activeClass}" href="javascript:void(0)" onclick="goToPage(${p})">${p}</a>
                </li>
            `);
        }

        const nextDisabled = (page >= totalPages) ? 'disabled opacity-25' : '';
        controls.append(`
            <li class="page-item ${nextDisabled}">
                <a class="page-link input-dark border-0 rounded" href="javascript:void(0)" onclick="goToPage(${page + 1})">
                    <i class="bi bi-chevron-right"></i>
                </a>
            </li>
        `);
    }

    window.goToPage = function (p) {
        if (p < 1 || p > state.totalPages || p === state.page) return;
        state.page = p;
        loadUsers();
    };

    // Add User (POST /api/users)
    function handleAddUser() {
        const username = $('#addUsername').val().trim();
        const fullName = $('#addFullName').val().trim();
        const email = $('#addEmail').val().trim();
        const password = $('#addPassword').val();
        const role = $('#addRole').val();
        const status = $('#addStatus').val();

        const alertBox = $('#addModalAlert');
        const btn = $('#btnSubmitAddUser');
        const btnText = $('#btnAddText');
        const btnSpinner = $('#btnAddSpinner');

        const errors = [];
        if (!username || username.length < 3) errors.push('Username must contain at least 3 characters.');
        if (!fullName) errors.push('Full Name is required.');
        if (!email || !email.includes('@')) errors.push('Valid email is required.');
        if (!password || password.length < 6) errors.push('Password must be at least 6 characters.');

        if (errors.length > 0) {
            alertBox.removeClass('d-none').html(errors.join('<br>'));
            return;
        }

        alertBox.addClass('d-none').empty();
        btn.prop('disabled', true);
        btnText.text('Creating...');
        btnSpinner.removeClass('d-none');

        const payload = {
            username: username,
            fullName: fullName,
            email: email,
            password: password,
            role: role,
            status: status
        };

        $.ajax({
            url: ctx + '/api/users',
            type: 'POST',
            contentType: 'application/json; charset=utf-8',
            data: JSON.stringify(payload),
            dataType: 'json',
            success: function (response) {
                if (response && response.success) {
                    const modalEl = document.getElementById('addUserModal');
                    const modal = bootstrap.Modal.getInstance(modalEl);
                    if (modal) modal.hide();

                    showToast(`Account @${username} provisioned successfully!`, 'success');
                    loadStats();
                    loadUsers();
                } else {
                    const msg = (response && response.errors) ? response.errors.join('<br>') : (response.message || 'Creation failed');
                    alertBox.removeClass('d-none').html(msg);
                }
            },
            error: function (xhr) {
                let errHtml = 'Failed to provision account.';
                try {
                    const res = JSON.parse(xhr.responseText);
                    if (res.errors && res.errors.length) errHtml = res.errors.join('<br>');
                    else if (res.message) errHtml = res.message;
                } catch (e) {
                }
                alertBox.removeClass('d-none').html(errHtml);
            },
            complete: function () {
                btn.prop('disabled', false);
                btnText.text('Create Account');
                btnSpinner.addClass('d-none');
            }
        });
    }

    // Open Edit Modal (GET /api/users/{id})
    window.openEditModal = function (id) {
        const modalEl = document.getElementById('editUserModal');
        const modal = new bootstrap.Modal(modalEl);

        $.ajax({
            url: ctx + '/api/users/' + id,
            type: 'GET',
            dataType: 'json',
            success: function (response) {
                if (response && response.success && response.data) {
                    const u = response.data;
                    $('#editUserId').val(u.id);
                    $('#editUsername').val(u.username);
                    $('#editFullName').val(u.fullName);
                    $('#editEmail').val(u.email);
                    $('#editPassword').val('');
                    $('#editRole').val(u.role);
                    $('#editStatus').val(u.status);

                    modal.show();
                } else {
                    showToast('Failed to retrieve account profile.', 'danger');
                }
            },
            error: function () {
                showToast('Unable to fetch account details.', 'danger');
            }
        });
    };

    // Edit User (PUT /api/users/{id})
    function handleEditUser() {
        const id = $('#editUserId').val();
        const fullName = $('#editFullName').val().trim();
        const email = $('#editEmail').val().trim();
        const password = $('#editPassword').val();
        const role = $('#editRole').val();
        const status = $('#editStatus').val();

        const alertBox = $('#editModalAlert');
        const btn = $('#btnSubmitEditUser');
        const btnText = $('#btnEditText');
        const btnSpinner = $('#btnEditSpinner');

        if (!fullName || !email) {
            alertBox.removeClass('d-none').text('Full name and email are mandatory.');
            return;
        }

        alertBox.addClass('d-none').empty();
        btn.prop('disabled', true);
        btnText.text('Saving...');
        btnSpinner.removeClass('d-none');

        const payload = {
            id: parseInt(id, 10),
            fullName: fullName,
            email: email,
            role: role,
            status: status
        };

        if (password && password.trim().length > 0) {
            payload.password = password;
        }

        $.ajax({
            url: ctx + '/api/users/' + id,
            type: 'PUT',
            contentType: 'application/json; charset=utf-8',
            data: JSON.stringify(payload),
            dataType: 'json',
            success: function (response) {
                if (response && response.success) {
                    const modalEl = document.getElementById('editUserModal');
                    const modal = bootstrap.Modal.getInstance(modalEl);
                    if (modal) modal.hide();

                    showToast('Account profile modified successfully!', 'success');
                    loadStats();
                    loadUsers();
                } else {
                    const msg = (response && response.errors) ? response.errors.join('<br>') : (response.message || 'Update failed');
                    alertBox.removeClass('d-none').html(msg);
                }
            },
            error: function (xhr) {
                let errHtml = 'Failed to update account.';
                try {
                    const res = JSON.parse(xhr.responseText);
                    if (res.errors && res.errors.length) errHtml = res.errors.join('<br>');
                    else if (res.message) errHtml = res.message;
                } catch (e) {
                }
                alertBox.removeClass('d-none').html(errHtml);
            },
            complete: function () {
                btn.prop('disabled', false);
                btnText.text('Save Changes');
                btnSpinner.addClass('d-none');
            }
        });
    }

    // Toggle Status (PUT /api/users/{id}/status)
    window.toggleUserStatus = function (id, targetStatus) {
        $.ajax({
            url: ctx + '/api/users/' + id + '/status',
            type: 'PUT',
            contentType: 'application/json; charset=utf-8',
            data: JSON.stringify({ status: targetStatus }),
            dataType: 'json',
            success: function (response) {
                if (response && response.success) {
                    showToast(`Status changed to ${targetStatus}`, 'success');
                    loadStats();
                    loadUsers();
                } else {
                    showToast(response.message || 'Status transition failed', 'danger');
                }
            },
            error: function () {
                showToast('Status update failed.', 'danger');
            }
        });
    };

    // Open Delete Modal
    window.openDeleteModal = function (id, username) {
        $('#deleteTargetId').val(id);
        $('#deleteTargetUsername').text('@' + username);

        const modalEl = document.getElementById('deleteUserModal');
        const modal = new bootstrap.Modal(modalEl);
        modal.show();
    };

    // Delete User (DELETE /api/users/{id})
    function handleDeleteUser() {
        const id = $('#deleteTargetId').val();
        const btn = $('#btnConfirmDelete');
        const btnText = $('#btnDeleteText');
        const btnSpinner = $('#btnDeleteSpinner');

        btn.prop('disabled', true);
        btnText.text('Deleting...');
        btnSpinner.removeClass('d-none');

        $.ajax({
            url: ctx + '/api/users/' + id,
            type: 'DELETE',
            dataType: 'json',
            success: function (response) {
                if (response && response.success) {
                    const modalEl = document.getElementById('deleteUserModal');
                    const modal = bootstrap.Modal.getInstance(modalEl);
                    if (modal) modal.hide();

                    showToast('Account removed from directory.', 'success');
                    loadStats();
                    loadUsers();
                } else {
                    showToast(response.message || 'Deletion failed', 'danger');
                }
            },
            error: function (xhr) {
                let msg = 'Failed to delete account.';
                try {
                    const res = JSON.parse(xhr.responseText);
                    if (res && res.message) msg = res.message;
                } catch (e) {
                }
                showToast(msg, 'danger');
            },
            complete: function () {
                btn.prop('disabled', false);
                btnText.text('Delete');
                btnSpinner.addClass('d-none');
            }
        });
    }

    // View User Details
    window.viewUserDetails = function (id) {
        $.ajax({
            url: ctx + '/api/users/' + id,
            type: 'GET',
            dataType: 'json',
            success: function (response) {
                if (response && response.success && response.data) {
                    const u = response.data;
                    showToast(`User #${u.id}: ${u.fullName} (@${u.username}) | Role: ${u.role} | Status: ${u.status}`, 'info');
                }
            }
        });
    };

    // Header Sort Icons
    function updateSortIcons() {
        $('.sortable-th i').attr('class', 'bi bi-arrow-down-up text-muted');
        const activeIcon = $('#sortIcon_' + state.sortBy);
        if (state.sortOrder === 'ASC') {
            activeIcon.attr('class', 'bi bi-arrow-up text-info fw-bold');
        } else {
            activeIcon.attr('class', 'bi bi-arrow-down text-info fw-bold');
        }
    }

    function formatDate(raw) {
        if (!raw) return 'N/A';
        try {
            const date = new Date(raw);
            if (isNaN(date.getTime())) return raw;
            return date.toLocaleDateString('en-US', {
                year: 'numeric',
                month: 'short',
                day: 'numeric'
            });
        } catch (e) {
            return raw;
        }
    }

    function escapeHtml(str) {
        if (!str) return '';
        return String(str)
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;')
            .replace(/'/g, '&#039;');
    }

})(jQuery);
