/**
 * NexusPortal - Global Client Application Utilities
 * Dark Aurora UI & Session Interceptors
 */

// Toast notification helper with Dark Aurora Styling
function showToast(message, type = 'success') {
    const toastEl = document.getElementById('liveToast');
    const toastText = document.getElementById('toastText');
    const toastIcon = document.getElementById('toastIcon');

    if (!toastEl || !toastText) return;

    toastText.textContent = message;

    // Reset styles
    toastEl.className = 'toast align-items-center text-white border-0 shadow-lg modal-content-dark';
    if (type === 'success') {
        toastEl.style.borderColor = 'rgba(16, 185, 129, 0.4)';
        toastIcon.className = 'bi bi-check-circle-fill fs-5 text-success';
    } else if (type === 'danger' || type === 'error') {
        toastEl.style.borderColor = 'rgba(244, 63, 94, 0.4)';
        toastIcon.className = 'bi bi-exclamation-octagon-fill fs-5 text-danger';
    } else if (type === 'warning') {
        toastEl.style.borderColor = 'rgba(245, 158, 11, 0.4)';
        toastIcon.className = 'bi bi-exclamation-triangle-fill fs-5 text-warning';
    } else {
        toastEl.style.borderColor = 'rgba(6, 182, 212, 0.4)';
        toastIcon.className = 'bi bi-info-circle-fill fs-5 text-info';
    }

    const toast = new bootstrap.Toast(toastEl, { delay: 4000 });
    toast.show();
}

// Global Ajax Setup: Attach X-Requested-With header & handle 401 Session Expiration
$.ajaxSetup({
    headers: {
        'X-Requested-With': 'XMLHttpRequest'
    },
    error: function (xhr) {
        if (xhr.status === 401) {
            showToast('Session expired. Redirecting to authentication...', 'warning');
            setTimeout(function () {
                window.location.href = (window.APP_CONTEXT || '') + '/login.jsp?sessionExpired=true';
            }, 1000);
        }
    }
});

// Session Inactivity Countdown Timer (30 minutes)
(function initSessionTimer() {
    let timeLeft = 30 * 60; // 30 minutes in seconds
    const timerDisplay = $('#sessionTimerText');

    if (timerDisplay.length === 0) return;

    const interval = setInterval(function () {
        timeLeft--;
        if (timeLeft <= 0) {
            clearInterval(interval);
            timerDisplay.text('00:00');
            showToast('Session terminated due to inactivity.', 'danger');
            setTimeout(function () {
                window.location.href = (window.APP_CONTEXT || '') + '/login.jsp?sessionExpired=true';
            }, 1000);
            return;
        }

        const minutes = Math.floor(timeLeft / 60);
        const seconds = timeLeft % 60;
        const formatted = String(minutes).padStart(2, '0') + ':' + String(seconds).padStart(2, '0');
        timerDisplay.text(formatted);

        // Flash red when < 2 minutes remain
        if (timeLeft < 120) {
            timerDisplay.addClass('text-danger fw-bold');
        }
    }, 1000);
})();

// Mobile Sidebar Toggle and Logout Listeners
$(document).ready(function () {
    // Mobile sidebar toggle
    $('#sidebarToggle').on('click', function () {
        $('#appSidebar').toggleClass('show');
    });

    // Close sidebar when clicking outside on mobile
    $(document).on('click', function (e) {
        if ($(window).width() < 992) {
            if (!$(e.target).closest('#appSidebar, #sidebarToggle').length) {
                $('#appSidebar').removeClass('show');
            }
        }
    });

    // Logout Button Listeners (both topbar and sidebar)
    $('#navLogoutBtn, #sidebarLogoutBtn').on('click', function (e) {
        e.preventDefault();
        const ctx = window.APP_CONTEXT || '';

        $.ajax({
            url: ctx + '/api/auth/logout',
            type: 'POST',
            contentType: 'application/json',
            success: function () {
                showToast('Signed out successfully.', 'info');
                setTimeout(function () {
                    window.location.href = ctx + '/login.jsp';
                }, 400);
            },
            error: function () {
                window.location.href = ctx + '/login.jsp';
            }
        });
    });
});

// View Profile modal helper
function viewMyProfile() {
    if (window.CURRENT_USER) {
        showToast('Logged in as @' + window.CURRENT_USER.username + ' (' + window.CURRENT_USER.role + ')', 'info');
    }
}
