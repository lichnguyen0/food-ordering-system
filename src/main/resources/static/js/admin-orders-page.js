/**
 * Admin Orders Page JavaScript
 * Handles tab switching, localStorage persistence, and AJAX status updates.
 */

document.addEventListener('DOMContentLoaded', function () {

    // Tab switching logic
    document.querySelectorAll('.tab-btn').forEach(function (btn) {
        btn.addEventListener('click', function () {
            document.querySelectorAll('.tab-btn').forEach(function (b) {
                b.classList.remove('active');
            });
            document.querySelectorAll('.tab-content').forEach(function (c) {
                c.classList.remove('active');
            });

            this.classList.add('active');
            var target = document.getElementById(this.getAttribute('data-target'));
            if (target) {
                target.classList.add('active');
            }
        });
    });

    // Restore active tab from localStorage if available
    var savedTab = localStorage.getItem('activeOrderTab');
    if (savedTab) {
        var btn = document.querySelector('.tab-btn[data-target="' + savedTab + '"]');
        if (btn) {
            btn.click();
        }
    }

    // Save tab state when clicked
    document.querySelectorAll('.tab-btn').forEach(function (btn) {
        btn.addEventListener('click', function () {
            localStorage.setItem('activeOrderTab', this.getAttribute('data-target'));
        });
    });

    // AJAX Update Status
    window.updateStatusAjax = function (orderId, status) {
        if (status === 'CANCELLED') {
            if (!confirm('Bạn có chắc chắn muốn hủy đơn hàng này không? Khách hàng sẽ không nhận được món!')) {
                return;
            }
        }

        // Add loading state to button
        var btn = event.currentTarget;
        var originalText = btn.innerHTML;
        btn.innerHTML = '<i class="fa-solid fa-spinner fa-spin"></i>';
        btn.disabled = true;

        var formData = new URLSearchParams();
        formData.append('orderId', orderId);
        formData.append('status', status);

        fetch('/admin/orders/api/update-status', {
            method: 'POST',
            body: formData,
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded'
            }
        })
            .then(function (response) {
                return response.json();
            })
            .then(function (data) {
                if (data.success) {
                    // Reload page to sync data
                    window.location.reload();
                } else {
                    alert('Cập nhật thất bại!');
                    btn.innerHTML = originalText;
                    btn.disabled = false;
                }
            })
            .catch(function (err) {
                console.error(err);
                alert('Đã xảy ra lỗi kết nối!');
                btn.innerHTML = originalText;
                btn.disabled = false;
            });
    };

});