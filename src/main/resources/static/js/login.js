document.addEventListener('DOMContentLoaded', () => {
    const loginForm = document.querySelector('form');
    const loginButton = document.querySelector('.btn-login');
    const inputs = document.querySelectorAll('.form-control');

    // 1. Tương tác vi mô khi chọn/focus vào ô nhập liệu
    inputs.forEach(input => {
        const parent = input.parentElement;

        input.addEventListener('focus', () => {
            parent.classList.add('is-focused');
        });

        input.addEventListener('blur', () => {
            parent.classList.remove('is-focused');
            // Thêm class nếu có giá trị để giữ trạng thái styling
            if (input.value.trim() !== '') {
                parent.classList.add('has-value');
            } else {
                parent.classList.remove('has-value');
            }
        });

        // Thiết lập trạng thái ban đầu nếu tải trang đã điền sẵn dữ liệu
        if (input.value.trim() !== '') {
            parent.classList.add('has-value');
        }
    });

    // 2. Hiệu ứng nút khi nhấn submit
    if (loginForm && loginButton) {
        loginForm.addEventListener('submit', (e) => {
            // Hiển thị trạng thái đang tải cực kỳ mượt mà
            loginButton.disabled = true;
            loginButton.classList.add('is-loading');
            
            // Lưu giữ lại nội dung gốc và thay thế bằng text đang xử lý
            const originalText = loginButton.innerText;
            loginButton.innerHTML = `<i class="fa-solid fa-circle-notch fa-spin"></i> ĐANG ĐĂNG NHẬP...`;
        });
    }

    // 3. Hiệu ứng chuyển động xuất hiện nhẹ khi tải trang (Fade-in-up)
    const loginCard = document.querySelector('.login-card-wrapper');
    if (loginCard) {
        loginCard.style.opacity = '0';
        loginCard.style.transform = 'translateY(15px)';
        loginCard.style.transition = 'opacity 0.6s cubic-bezier(0.16, 1, 0.3, 1), transform 0.6s cubic-bezier(0.16, 1, 0.3, 1)';
        
        setTimeout(() => {
            loginCard.style.opacity = '1';
            loginCard.style.transform = 'translateY(0)';
        }, 100);
    }
});
