<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>NexusAuth - Create Account</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=JetBrains+Mono:wght@400;500;600&family=Outfit:wght@300;400;500;600;700;800&display=swap" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet" integrity="sha384-QWTKZyjpPEjISv5WaRU9OFeRpok6YctnYmDr5pNlyT2bRjXh0JMhjY6hW+ALEwIH" crossorigin="anonymous">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <style>
        .strength-bar{height:4px;border-radius:2px;transition:all .3s;background:var(--border-subtle,#e2e8f0);}
        .strength-bar.weak{background:#ef4444;width:33%;}
        .strength-bar.medium{background:#f59e0b;width:66%;}
        .strength-bar.strong{background:#10b981;width:100%;}
        .register-step{display:none;} .register-step.active{display:block;}
        .step-indicator{display:flex;gap:8px;margin-bottom:1.5rem;}
        .step-dot{width:8px;height:8px;border-radius:50%;background:var(--border-subtle,#e2e8f0);transition:all .3s;}
        .step-dot.active{background:#6366f1;transform:scale(1.4);}
        .step-dot.done{background:#10b981;}
    </style>
</head>
<body style="background:var(--bg-base);overflow-x:hidden;">
<div class="split-login-container">

    <!-- Left Brand Panel -->
    <div class="login-brand-panel">
        <div class="d-flex align-items-center gap-3">
            <div class="brand-hexagon"><i class="bi bi-shield-lock-fill"></i></div>
            <div>
                <div class="brand-title fs-4">NexusAuth</div>
                <div class="brand-subtitle">Identity &amp; Access Governance</div>
            </div>
        </div>
        <div class="my-auto py-5">
            <div class="badge-aurora badge-role-user mb-3">
                <i class="bi bi-person-plus-fill"></i> Self-Registration Portal
            </div>
            <h1 class="display-6 fw-bold mb-3" style="letter-spacing:-0.03em;color:var(--text-primary);">
                Join the Enterprise Platform
            </h1>
            <p class="text-secondary fs-6 mb-4" style="max-width:480px;line-height:1.6;">
                Create your account in seconds. Passwords are protected with BCrypt salted hashing and stored securely in MySQL with HikariCP connection pooling.
            </p>
            <div class="row g-3 pt-2" style="max-width:520px;">
                <div class="col-6">
                    <div class="p-3 rounded-3" style="background:#fff;border:1px solid var(--border-subtle);box-shadow:var(--shadow-card);">
                        <i class="bi bi-person-badge-fill text-primary fs-5 mb-2 d-block"></i>
                        <div class="fw-semibold text-dark small">Instant Access</div>
                        <div class="text-muted small" style="font-size:.75rem;">Active immediately</div>
                    </div>
                </div>
                <div class="col-6">
                    <div class="p-3 rounded-3" style="background:#fff;border:1px solid var(--border-subtle);box-shadow:var(--shadow-card);">
                        <i class="bi bi-key-fill text-success fs-5 mb-2 d-block"></i>
                        <div class="fw-semibold text-dark small">BCrypt Security</div>
                        <div class="text-muted small" style="font-size:.75rem;">Salted hashes</div>
                    </div>
                </div>
                <div class="col-6">
                    <div class="p-3 rounded-3" style="background:#fff;border:1px solid var(--border-subtle);box-shadow:var(--shadow-card);">
                        <i class="bi bi-shield-check text-info fs-5 mb-2 d-block"></i>
                        <div class="fw-semibold text-dark small">SQL Injection Safe</div>
                        <div class="text-muted small" style="font-size:.75rem;">PreparedStatement only</div>
                    </div>
                </div>
                <div class="col-6">
                    <div class="p-3 rounded-3" style="background:#fff;border:1px solid var(--border-subtle);box-shadow:var(--shadow-card);">
                        <i class="bi bi-database-check text-warning fs-5 mb-2 d-block"></i>
                        <div class="fw-semibold text-dark small">MySQL Backed</div>
                        <div class="text-muted small" style="font-size:.75rem;">HikariCP pool</div>
                    </div>
                </div>
            </div>
        </div>
        <div class="d-flex align-items-center justify-content-between text-muted small border-top pt-3" style="border-color:var(--border-subtle) !important;">
            <span><i class="bi bi-lock-fill text-success me-1"></i> HTTPS &amp; NoCache Guard Active</span>
            <span>Port 8080 &middot; Jersey JAX-RS</span>
        </div>
    </div>

    <!-- Right Form Panel -->
    <div class="login-form-panel">
        <div class="login-form-wrapper">
            <div class="mb-4">
                <h3 class="fw-bold mb-1" style="letter-spacing:-0.02em;color:var(--text-primary);">Create Account</h3>
                <p class="text-secondary small mb-0">Fill in your details to get started.</p>
            </div>

            <!-- Step dots -->
            <div class="step-indicator">
                <div class="step-dot active" id="dot1"></div>
                <div class="step-dot" id="dot2"></div>
            </div>

            <!-- Alert -->
            <div id="regAlert" class="alert d-none align-items-center gap-2 small py-2 mb-3" role="alert">
                <i class="bi flex-shrink-0" id="regAlertIcon"></i>
                <div id="regAlertText"></div>
            </div>

            <!-- Success -->
            <div id="successPanel" class="d-none text-center py-4">
                <div class="mb-3" style="font-size:3rem;">&#127881;</div>
                <h5 class="fw-bold mb-2" style="color:var(--text-primary);">Account Created!</h5>
                <p class="text-secondary small mb-4">Your account is active. Redirecting to login...</p>
                <div class="spinner-border text-success" role="status" style="width:1.5rem;height:1.5rem;">
                    <span class="visually-hidden">Loading...</span>
                </div>
            </div>

            <form id="registerForm" novalidate autocomplete="off">
                <!-- Step 1 -->
                <div class="register-step active" id="step1">
                    <div class="mb-3">
                        <label for="fullName" class="form-label small fw-semibold text-secondary">Full Name</label>
                        <div class="input-group">
                            <span class="input-group-text bg-transparent border-end-0 text-muted" style="border-color:var(--border-subtle);">
                                <i class="bi bi-person-circle"></i>
                            </span>
                            <input type="text" class="form-control input-dark border-start-0" id="fullName" placeholder="e.g. Jane Smith" autocomplete="name">
                        </div>
                        <div class="text-danger small d-none mt-1" id="fullNameErr"></div>
                    </div>
                    <div class="mb-3">
                        <label for="regEmail" class="form-label small fw-semibold text-secondary">Email Address</label>
                        <div class="input-group">
                            <span class="input-group-text bg-transparent border-end-0 text-muted" style="border-color:var(--border-subtle);">
                                <i class="bi bi-envelope"></i>
                            </span>
                            <input type="email" class="form-control input-dark border-start-0" id="regEmail" placeholder="you@example.com" autocomplete="email">
                        </div>
                        <div class="text-danger small d-none mt-1" id="regEmailErr"></div>
                    </div>
                    <button type="button" class="btn btn-aurora w-100 py-2 d-flex align-items-center justify-content-center gap-2 mb-3" id="btnStep1">
                        <span>Continue</span><i class="bi bi-arrow-right"></i>
                    </button>
                </div>

                <!-- Step 2 -->
                <div class="register-step" id="step2">
                    <div class="mb-3">
                        <label for="regUsername" class="form-label small fw-semibold text-secondary">Username</label>
                        <div class="input-group">
                            <span class="input-group-text bg-transparent border-end-0 text-muted" style="border-color:var(--border-subtle);">
                                <i class="bi bi-at"></i>
                            </span>
                            <input type="text" class="form-control input-dark border-start-0" id="regUsername" placeholder="3-30 chars, letters/numbers/_" autocomplete="username">
                        </div>
                        <div class="text-muted" style="font-size:.72rem;margin-top:4px;">Only letters, numbers and underscores.</div>
                        <div class="text-danger small d-none mt-1" id="regUsernameErr"></div>
                    </div>
                    <div class="mb-1">
                        <label for="regPassword" class="form-label small fw-semibold text-secondary">Password</label>
                        <div class="input-group">
                            <span class="input-group-text bg-transparent border-end-0 text-muted" style="border-color:var(--border-subtle);">
                                <i class="bi bi-key"></i>
                            </span>
                            <input type="password" class="form-control input-dark border-start-0 border-end-0 px-0" id="regPassword" placeholder="Min. 6 characters" autocomplete="new-password">
                            <button class="btn btn-outline-secondary border-start-0 text-muted" type="button" id="toggleRegPassword" style="border-color:var(--border-subtle);">
                                <i class="bi bi-eye" id="toggleRegPassIcon"></i>
                            </button>
                        </div>
                        <div class="text-danger small d-none mt-1" id="regPasswordErr"></div>
                    </div>
                    <div class="strength-bar mb-3 mt-2" id="strengthBar"></div>
                    <div class="mb-4">
                        <label for="regConfirmPassword" class="form-label small fw-semibold text-secondary">Confirm Password</label>
                        <div class="input-group">
                            <span class="input-group-text bg-transparent border-end-0 text-muted" style="border-color:var(--border-subtle);">
                                <i class="bi bi-shield-lock"></i>
                            </span>
                            <input type="password" class="form-control input-dark border-start-0" id="regConfirmPassword" placeholder="Repeat your password" autocomplete="new-password">
                        </div>
                        <div class="text-danger small d-none mt-1" id="regConfirmErr"></div>
                    </div>
                    <div class="d-flex gap-2 mb-3">
                        <button type="button" class="btn btn-outline-secondary py-2 flex-shrink-0" id="btnBack">
                            <i class="bi bi-arrow-left"></i>
                        </button>
                        <button type="submit" class="btn btn-aurora w-100 py-2 d-flex align-items-center justify-content-center gap-2" id="btnRegister">
                            <span id="btnRegText">Create Account</span>
                            <span id="btnRegSpinner" class="spinner-border spinner-border-sm d-none" role="status" aria-hidden="true"></span>
                        </button>
                    </div>
                </div>
            </form>

            <!-- Login link -->
            <div class="pt-3 border-top text-center" id="loginLink" style="border-color:var(--border-subtle) !important;">
                <span class="text-secondary small">Already have an account?</span>
                <a href="${pageContext.request.contextPath}/login.jsp" class="small fw-semibold ms-1" style="color:#6366f1;text-decoration:none;">
                    Sign In <i class="bi bi-box-arrow-in-right"></i>
                </a>
            </div>
        </div>
    </div>
</div>

<script src="https://code.jquery.com/jquery-3.7.1.min.js" integrity="sha256-/JqT3SQfawRcv/BIHPThkBvs0OEvtFFmqPF/lYI/Cxo=" crossorigin="anonymous"></script>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js" integrity="sha384-YvpcrYf0tY3lHB60NNkmXc5s9fDVZLESaAA55NDzOxhy9GkcIdslK1eN7N6jIeHz" crossorigin="anonymous"></script>
<script>
const CONTEXT_PATH = "${pageContext.request.contextPath}";

function showAlert(msg, type) {
    var box = $("#regAlert");
    box.removeClass("d-none alert-danger alert-success alert-warning").addClass("d-flex alert-" + type);
    var icons = {danger:"bi-exclamation-octagon-fill text-danger", success:"bi-check-circle-fill text-success", warning:"bi-exclamation-triangle-fill text-warning"};
    $("#regAlertIcon").attr("class","bi flex-shrink-0 " + (icons[type]||""));
    $("#regAlertText").text(msg);
}
function hideAlert() { $("#regAlert").addClass("d-none"); }
function clearErrors() { $(".text-danger.small").addClass("d-none").text(""); }
function fieldErr(id, msg) { $("#"+id).removeClass("d-none").text(msg); }

function goToStep(n) {
    $(".register-step").removeClass("active");
    $("#step"+n).addClass("active");
    for (var i=1;i<=2;i++) {
        var d = $("#dot"+i);
        d.removeClass("active done");
        if (i<n) d.addClass("done");
        else if (i===n) d.addClass("active");
    }
}

$("#regPassword").on("input", function() {
    var v = $(this).val(), bar = $("#strengthBar");
    bar.removeClass("weak medium strong");
    if (!v.length) return;
    if (v.length < 6) { bar.addClass("weak"); return; }
    var s = 0;
    if (/[A-Z]/.test(v)) s++;
    if (/[0-9]/.test(v)) s++;
    if (/[^A-Za-z0-9]/.test(v)) s++;
    bar.addClass(s<=0?"weak":s===1?"medium":"strong");
});

$("#toggleRegPassword").on("click", function() {
    var f = $("#regPassword"), ic = $("#toggleRegPassIcon");
    if (f.attr("type")==="password") { f.attr("type","text"); ic.removeClass("bi-eye").addClass("bi-eye-slash"); }
    else { f.attr("type","password"); ic.removeClass("bi-eye-slash").addClass("bi-eye"); }
});

$("#btnStep1").on("click", function() {
    clearErrors(); hideAlert();
    var name = $("#fullName").val().trim(), email = $("#regEmail").val().trim(), ok = true;
    if (!name) { fieldErr("fullNameErr","Full name is required."); ok=false; }
    else if (name.length>100) { fieldErr("fullNameErr","Cannot exceed 100 characters."); ok=false; }
    if (!email) { fieldErr("regEmailErr","Email is required."); ok=false; }
    else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) { fieldErr("regEmailErr","Invalid email format."); ok=false; }
    if (ok) goToStep(2);
});

$("#btnBack").on("click", function() { clearErrors(); hideAlert(); goToStep(1); });

$("#registerForm").on("submit", function(e) {
    e.preventDefault(); clearErrors(); hideAlert();
    var username = $("#regUsername").val().trim();
    var password = $("#regPassword").val();
    var confirm  = $("#regConfirmPassword").val();
    var ok = true;
    if (!username) { fieldErr("regUsernameErr","Username is required."); ok=false; }
    else if (!/^[a-zA-Z0-9_]{3,30}$/.test(username)) { fieldErr("regUsernameErr","3-30 chars: letters, numbers, underscores only."); ok=false; }
    if (!password) { fieldErr("regPasswordErr","Password is required."); ok=false; }
    else if (password.length<6) { fieldErr("regPasswordErr","Minimum 6 characters."); ok=false; }
    if (password !== confirm) { fieldErr("regConfirmErr","Passwords do not match."); ok=false; }
    if (!ok) return;

    var btn = $("#btnRegister");
    btn.prop("disabled",true);
    $("#btnRegText").text("Creating account...");
    $("#btnRegSpinner").removeClass("d-none");

    $.ajax({
        url: CONTEXT_PATH + "/api/auth/register",
        type: "POST",
        contentType: "application/json; charset=utf-8",
        data: JSON.stringify({
            username: username,
            password: password,
            fullName: $("#fullName").val().trim(),
            email: $("#regEmail").val().trim()
        }),
        dataType: "json",
        success: function(res) {
            if (res && res.success) {
                $("#registerForm").hide();
                $("#loginLink").hide();
                $(".step-indicator").hide();
                $("#successPanel").removeClass("d-none");
                setTimeout(function() {
                    window.location.href = CONTEXT_PATH + "/login.jsp?registered=true";
                }, 2200);
            } else {
                showAlert(res.message || "Registration failed.", "danger");
                btn.prop("disabled",false);
                $("#btnRegText").text("Create Account");
                $("#btnRegSpinner").addClass("d-none");
            }
        },
        error: function(xhr) {
            var msg = "Registration failed. Please try again.";
            try { var r=JSON.parse(xhr.responseText); if(r&&r.message) msg=r.message; } catch(ex){}
            showAlert(msg, "danger");
            btn.prop("disabled",false);
            $("#btnRegText").text("Create Account");
            $("#btnRegSpinner").addClass("d-none");
        }
    });
});
</script>
</body>
</html>
