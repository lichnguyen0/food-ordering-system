document.addEventListener('DOMContentLoaded', () => {
    const registerForm = document.getElementById('registerForm');
    const registerButton = document.getElementById('btnRegister');
    const inputs = document.querySelectorAll('.form-control');

    // 1. Tương tác vi mô khi chọn/focus vào ô nhập liệu
    inputs.forEach(input => {
        const parent = input.parentElement;

        input.addEventListener('focus', () => {
            parent.classList.add('is-focused');
        });

        input.addEventListener('blur', () => {
            parent.classList.remove('is-focused');
            if (input.value.trim() !== '') {
                parent.classList.add('has-value');
            } else {
                parent.classList.remove('has-value');
            }
        });

        // Thiết lập trạng thái ban đầu nếu tải trang đã điền sẵn dữ liệu (Thymeleaf values)
        if (input.value.trim() !== '') {
            parent.classList.add('has-value');
        }
    });

    // 2. Client-side Validation & Hiệu ứng nút khi nhấn submit
    if (registerForm && registerButton) {
        registerForm.addEventListener('submit', (e) => {
            const password = document.getElementById('password').value;
            const confirmPassword = document.getElementById('confirmPassword').value;

            // Xóa thông báo lỗi cũ nếu có
            const existingAlert = document.getElementById('errorAlert');
            if (existingAlert) {
                existingAlert.remove();
            }

            // Kiểm tra mật khẩu trùng khớp
            if (password !== confirmPassword) {
                e.preventDefault(); // Ngăn gửi form

                // Tạo thông báo lỗi cực đẹp
                const alertDiv = document.createElement('div');
                alertDiv.className = 'alert alert-danger';
                alertDiv.id = 'errorAlert';
                alertDiv.innerHTML = `<i class="fa-solid fa-circle-exclamation"></i> Mật khẩu xác nhận không khớp! Vui lòng thử lại.`;

                // Chèn vào phía trước form-group đầu tiên
                registerForm.insertBefore(alertDiv, registerForm.firstChild);
                
                // Cuộn mượt lên trên cùng form để người dùng thấy thông báo
                alertDiv.scrollIntoView({ behavior: 'smooth', block: 'center' });
                return;
            }

            // Hiển thị trạng thái đang tải cực kỳ mượt mà
            registerButton.disabled = true;
            registerButton.classList.add('is-loading');
            registerButton.innerHTML = `<i class="fa-solid fa-circle-notch fa-spin"></i> ĐANG ĐĂNG KÝ...`;
        });
    }

    // 3. Hiệu ứng chuyển động xuất hiện nhẹ khi tải trang (Fade-in-up)
    const registerCard = document.querySelector('.register-card-wrapper');
    if (registerCard) {
        registerCard.style.opacity = '0';
        registerCard.style.transform = 'translateY(15px)';
        registerCard.style.transition = 'opacity 0.6s cubic-bezier(0.16, 1, 0.3, 1), transform 0.6s cubic-bezier(0.16, 1, 0.3, 1)';
        
        setTimeout(() => {
            registerCard.style.opacity = '1';
            registerCard.style.transform = 'translateY(0)';
        }, 100);
    }
});
