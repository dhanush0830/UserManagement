<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
    <!-- Floating Dark Aurora Toast Notification Container -->
    <div class="toast-container position-fixed top-0 end-0 p-3" style="z-index: 1099;">
        <div id="liveToast" class="toast align-items-center border shadow-lg modal-content-dark"
             role="alert" aria-live="assertive" aria-atomic="true">
            <div class="d-flex">
                <div class="toast-body d-flex align-items-center gap-2" id="toastMessage">
                    <i class="bi bi-info-circle-fill fs-5 text-info" id="toastIcon"></i>
                    <span id="toastText">Action completed</span>
                </div>
                <button type="button" class="btn-close me-2 m-auto" data-bs-dismiss="toast" aria-label="Close"></button>
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
        window.APP_CONTEXT = "${pageContext.request.contextPath}";
    </script>
    <script src="${pageContext.request.contextPath}/js/app.js"></script>
</body>
</html>
