/**
 * kitchen-foods.js — Logic bật/tắt trạng thái món ăn (AVAILABLE / SOLD_OUT)
 */
function showToast(message, isSuccess) {
    if (isSuccess === undefined) isSuccess = true;
    var toast = document.getElementById('toast');
    var messageEl = document.getElementById('toast-message');

    toast.style.borderColor = isSuccess ? 'var(--success)' : 'var(--danger)';
    toast.querySelector('i').className = isSuccess ? 'fa-solid fa-circle-check' : 'fa-solid fa-circle-xmark';
    toast.querySelector('i').style.color = isSuccess ? 'var(--success)' : 'var(--danger)';

    messageEl.textContent = message;
    toast.classList.add('show');

    setTimeout(function () {
        toast.classList.remove('show');
    }, 3000);
}

function toggleFoodStatus(foodId, checkbox) {
    var isAvailable = checkbox.checked;
    var badge = document.getElementById('status-badge-' + foodId);

    // Optimistically update UI
    if (isAvailable) {
        badge.textContent = 'Đang phục vụ';
        badge.className = 'food-status-badge available';
    } else {
        badge.textContent = 'Hết món';
        badge.className = 'food-status-badge sold-out';
    }

    // Send request to API
    fetch('/kitchen/api/food/' + foodId + '/toggle-status', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' }
    })
    .then(function (response) {
        if (!response.ok) throw new Error('Lỗi cập nhật trạng thái');
        return response.json();
    })
    .then(function (data) {
        if (data.success) {
            showToast('Đã đổi trạng thái món ăn thành công!');
        } else {
            throw new Error(data.message || 'Cập nhật thất bại');
        }
    })
    .catch(function (error) {
        console.error(error);
        // Revert UI on error
        checkbox.checked = !isAvailable;
        if (!isAvailable) {
            badge.textContent = 'Đang phục vụ';
            badge.className = 'food-status-badge available';
        } else {
            badge.textContent = 'Hết món';
            badge.className = 'food-status-badge sold-out';
        }
        showToast(error.message || 'Không thể kết nối máy chủ!', false);
    });
}
