<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="en" data-bs-theme="light">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>NexusAuth - Sign In</title>

    <!-- Google Fonts: Outfit & JetBrains Mono -->
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=JetBrains+Mono:wght@400;500;600&family=Outfit:wght@300;400;500;600;700;800&display=swap" rel="stylesheet">

    <!-- Bootstrap 5 CSS -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet"
          integrity="sha384-QWTKZyjpPEjISv5WaRU9OFeRpok6YctnYmDr5pNlyT2bRjXh0JMhjY6hW+ALEwIH" crossorigin="anonymous">

    <!-- Bootstrap Icons -->
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">

    <!-- Custom Dark Aurora CSS -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body style="background: var(--bg-base); overflow-x: hidden;">

    <div class="split-login-container">
        <!-- Left Side: Brand Visual Hero Panel -->
        <div class="login-brand-panel">
            <!-- Brand Header -->
            <div class="d-flex align-items-center gap-3">
                <div class="brand-hexagon">
                    <i class="bi bi-shield-lock-fill"></i>
                </div>
                <div>
                    <div class="brand-title fs-4">NexusAuth</div>
                    <div class="brand-subtitle">Identity &amp; Access Governance</div>
                </div>
            </div>

            <!-- Central Hero Message -->
            <div class="my-auto py-5">
                <div class="badge-aurora badge-role-admin mb-3">
                    <i class="bi bi-cpu-fill"></i> Enterprise Edition v2.0
                </div>
                <h1 class="display-6 fw-bold mb-3" style="letter-spacing: -0.03em; color: var(--text-primary);">
                    Intelligent User Governance &amp; Session Security
                </h1>
                <p class="text-secondary fs-6 mb-4" style="max-width: 480px; line-height: 1.6;">
                    Production-grade user management powered by Jersey RESTful architecture, session-bound tokenless authentication, and real-time AJAX event synchronization.
                </p>

                <!-- Feature Highlights Grid -->
                <div class="row g-3 pt-2" style="max-width: 520px;">
                    <div class="col-6">
                        <div class="p-3 rounded-3" style="background: #ffffff; border: 1px solid var(--border-subtle); box-shadow: var(--shadow-card);">
                            <i class="bi bi-shield-check text-info fs-5 mb-2 d-block"></i>
                            <div class="fw-semibold text-dark small">Session-Bound Security</div>
                            <div class="text-muted small" style="font-size: 0.75rem;">Fixation-immune authentication</div>
                        </div>
                    </div>
                    <div class="col-6">
                        <div class="p-3 rounded-3" style="background: #ffffff; border: 1px solid var(--border-subtle); box-shadow: var(--shadow-card);">
                            <i class="bi bi-lightning-charge-fill text-warning fs-5 mb-2 d-block"></i>
                            <div class="fw-semibold text-dark small">AJAX Grid Sync</div>
                            <div class="text-muted small" style="font-size: 0.75rem;">Zero-reload state transitions</div>
                        </div>
                    </div>
                    <div class="col-6">
                        <div class="p-3 rounded-3" style="background: #ffffff; border: 1px solid var(--border-subtle); box-shadow: var(--shadow-card);">
                            <i class="bi bi-key-fill text-success fs-5 mb-2 d-block"></i>
                            <div class="fw-semibold text-dark small">BCrypt Encryption</div>
                            <div class="text-muted small" style="font-size: 0.75rem;">Salted cryptographic hashes</div>
                        </div>
                    </div>
                    <div class="col-6">
                        <div class="p-3 rounded-3" style="background: #ffffff; border: 1px solid var(--border-subtle); box-shadow: var(--shadow-card);">
                            <i class="bi bi-database-check text-primary fs-5 mb-2 d-block"></i>
                            <div class="fw-semibold text-dark small">PreparedStatements</div>
                            <div class="text-muted small" style="font-size: 0.75rem;">100% SQL injection immunity</div>
                        </div>
                    </div>
                </div>
            </div>

            <!-- Footer Compliance Info -->
            <div class="d-flex align-items-center justify-content-between text-muted small border-top pt-3" style="border-color: var(--border-subtle) !important;">
                <span><i class="bi bi-lock-fill text-success me-1"></i> HTTPS &amp; NoCache Guard Active</span>
                <span>Port 8080 &middot; Jersey JAX-RS</span>
            </div>
        </div>

        <!-- Right Side: Dark Glass Login Form -->
        <div class="login-form-panel">
            <div class="login-form-wrapper">
                <div class="mb-4">
                    <h3 class="fw-bold mb-2" style="letter-spacing: -0.02em; color: var(--text-primary);">Welcome Back</h3>
                    <p class="text-secondary small mb-0">Enter your credentials to access the administrative console.</p>
                </div>

                <!-- Session Expired Alert -->
                <% if ("true".equals(request.getParameter("sessionExpired"))) { %>
                    <div class="alert alert-warning d-flex align-items-center gap-2 small py-2 mb-3 bg-opacity-10 border border-warning" role="alert">
                        <i class="bi bi-exclamation-triangle-fill flex-shrink-0 text-warning"></i>
                        <div>Your session has expired. Please sign in again.</div>
                    </div>
                <% } %>

                <!-- Dynamic Error Alert -->
                <div id="loginAlert" class="alert alert-danger d-none align-items-center gap-2 small py-2 mb-3 bg-opacity-10 border border-danger" role="alert">
                    <i class="bi bi-exclamation-octagon-fill flex-shrink-0 text-danger"></i>
                    <div id="loginAlertText">Invalid credentials</div>
                </div>

                <!-- Form -->
                <form id="loginForm" novalidate>
                    <!-- Username -->
                    <div class="mb-3">
                        <label for="username" class="form-label small fw-semibold text-secondary">Username</label>
                        <div class="input-group">
                            <span class="input-group-text bg-transparent border-end-0 text-muted" style="border-color: var(--border-subtle);">
                                <i class="bi bi-person"></i>
                            </span>
                            <input type="text" class="form-control input-dark border-start-0 ps-0" id="username" name="username"
                                   placeholder="Username (e.g. admin)" required autofocus autocomplete="username">
                        </div>
                    </div>

                    <!-- Password -->
                    <div class="mb-4">
                        <div class="d-flex justify-content-between align-items-center mb-1">
                            <label for="password" class="form-label small fw-semibold text-secondary mb-0">Password</label>
                            <span class="text-muted small" style="font-size: 0.72rem;">BCrypt Protected</span>
                        </div>
                        <div class="input-group">
                            <span class="input-group-text bg-transparent border-end-0 text-muted" style="border-color: var(--border-subtle);">
                                <i class="bi bi-key"></i>
                            </span>
                            <input type="password" class="form-control input-dark border-start-0 border-end-0 px-0" id="password"
                                   name="password" placeholder="Password" required autocomplete="current-password">
                            <button class="btn btn-outline-secondary border-start-0 text-muted" type="button" id="togglePassword" style="border-color: var(--border-subtle);">
                                <i class="bi bi-eye" id="togglePasswordIcon"></i>
                            </button>
                        </div>
                    </div>

                    <!-- Submit Button -->
                    <button type="submit" class="btn btn-aurora w-100 py-2 d-flex align-items-center justify-content-center gap-2 mb-4" id="btnLogin">
                        <span id="btnLoginText">Sign In to Console</span>
                        <span id="btnLoginSpinner" class="spinner-border spinner-border-sm d-none" role="status" aria-hidden="true"></span>
                    </button>
                </form>

                <!-- Quick Fill Demo Accounts -->
                <div class="pt-3 border-top" style="border-color: var(--border-subtle) !important;">
                    <div class="text-muted small mb-2 fw-medium" style="font-size: 0.72rem; letter-spacing: 0.05em; text-transform: uppercase;">
                        Quick Demo Credentials (Click to Autofill)
                    </div>
                    <div class="d-flex gap-2 flex-wrap">
                        <span class="demo-chip" onclick="fillCredentials('admin', 'Admin@123')">
                            <i class="bi bi-star-fill text-warning"></i>Admin: <strong>admin</strong>
                        </span>
                        <span class="demo-chip" onclick="fillCredentials('john_doe', 'Manager@123')">
                            <i class="bi bi-briefcase-fill text-info"></i>Manager: <strong>john_doe</strong>
                        </span>
                        <span class="demo-chip" onclick="fillCredentials('jane_smith', 'User@123')">
                            <i class="bi bi-person-fill text-secondary"></i>User: <strong>jane_smith</strong>
                        </span>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <!-- jQuery 3.7.1 -->
    <script src="https://code.jquery.com/jquery-3.7.1.min.js"
            integrity="sha256-/JqT3SQfawRcv/BIHPThkBvs0OEvtFFmqPF/lYI/Cxo="
            crossorigin="anonymous"></script>

    <!-- Bootstrap 5.3.3 JS Bundle -->
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"
            integrity="sha384-YvpcrYf0tY3lHB60NNkmXc5s9fDVZLESaAA55NDzOxhy9GkcIdslK1eN7N6jIeHz"
            crossorigin="anonymous"></script>

    <script>
        const CONTEXT_PATH = "${pageContext.request.contextPath}";

        function fillCredentials(u, p) {
            $("#username").val(u);
            $("#password").val(p);
            $("#loginAlert").addClass("d-none");
            $("#username").focus();
        }

        $(document).ready(function() {
            // Password Show/Hide Toggle
            $("#togglePassword").on("click", function() {
                const passField = $("#password");
                const icon = $("#togglePasswordIcon");
                if (passField.attr("type") === "password") {
                    passField.attr("type", "text");
                    icon.removeClass("bi-eye").addClass("bi-eye-slash");
                } else {
                    passField.attr("type", "password");
                    icon.removeClass("bi-eye-slash").addClass("bi-eye");
                }
            });

            // Ajax Login Handler
            $("#loginForm").on("submit", function(e) {
                e.preventDefault();

                const username = $("#username").val().trim();
                const password = $("#password").val();
                const alertBox = $("#loginAlert");
                const alertText = $("#loginAlertText");
                const btn = $("#btnLogin");
                const btnText = $("#btnLoginText");
                const btnSpinner = $("#btnLoginSpinner");

                if (!username || !password) {
                    alertText.text("Please enter both username and password.");
                    alertBox.removeClass("d-none");
                    return;
                }

                // UI Loading State
                btn.prop("disabled", true);
                btnText.text("Verifying credentials...");
                btnSpinner.removeClass("d-none");
                alertBox.addClass("d-none");

                $.ajax({
                    url: CONTEXT_PATH + "/api/auth/login",
                    type: "POST",
                    contentType: "application/json; charset=utf-8",
                    data: JSON.stringify({
                        username: username,
                        password: password
                    }),
                    dataType: "json",
                    success: function(response) {
                        if (response && response.success) {
                            btnText.text("Authenticated! Entering console...");
                            btn.removeClass("btn-aurora").addClass("btn-success");
                            setTimeout(function() {
                                window.location.href = CONTEXT_PATH + "/home.jsp";
                            }, 450);
                        } else {
                            alertText.text(response.message || "Authentication failed.");
                            alertBox.removeClass("d-none");
                            btn.prop("disabled", false);
                            btnText.text("Sign In to Console");
                            btnSpinner.addClass("d-none");
                        }
                    },
                    error: function(xhr) {
                        let errMsg = "Invalid username or password.";
                        if (xhr.status === 404) {
                            errMsg = "API endpoint not found (404). Please restart the server with 'mvn clean compile exec:java'.";
                        } else if (xhr.status === 500) {
                            errMsg = "Server error (500). Please check console output.";
                        } else if (xhr.status === 0) {
                            errMsg = "Cannot connect to server. Please check if port 8080 is listening.";
                        }
                        try {
                            const res = JSON.parse(xhr.responseText);
                            if (res && res.message) {
                                errMsg = res.message;
                            }
                        } catch (e) {
                        }
                        alertText.text(errMsg);
                        alertBox.removeClass("d-none");
                        btn.prop("disabled", false);
                        btnText.text("Sign In to Console");
                        btnSpinner.addClass("d-none");
                    }
                });
            });
        });
    </script>
</body>
</html>
