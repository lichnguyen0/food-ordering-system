document.addEventListener('DOMContentLoaded', function () {

    // Toggle Status
    var toggleBtn = document.getElementById('statusToggle');
    if (toggleBtn) {
        toggleBtn.addEventListener('change', function() {
            var isAvailable = this.checked;
            var label = document.getElementById('statusLabel');

            fetch('/shipper/api/toggle-status', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' }
            })
            .then(function(response) {
                if (!response.ok) throw new Error('Network response was not ok');
                return response.json();
            })
            .then(function(data) {
                if(data.success) {
                    label.textContent = data.available ? 'Đang trực' : 'Nghỉ ngơi';
                    showToast('Đã cập nhật trạng thái làm việc!');
                } else {
                    throw new Error(data.message);
                }
            })
            .catch(function(error) {
                console.error('Error toggling status:', error);
                toggleBtn.checked = !isAvailable; // revert
                showToast('Không thể cập nhật trạng thái!', false);
            });
        });
    }

    // Modal Logic
    let cancelOrderId = null;

    window.openFailModal = function(orderId) {
        cancelOrderId = orderId;
        document.getElementById('cancel-order-id-display').textContent = '#' + orderId;
        document.getElementById('cancel-form').action = '/shipper/order/' + orderId + '/fail';
        document.getElementById('cancel-reason-select').value = 'Không liên lạc được khách';
        document.getElementById('cancel-reason-custom').style.display = 'none';
        document.getElementById('cancel-reason-custom').value = 'Không liên lạc được khách';
        document.getElementById('cancel-modal').style.display = 'block';
    };

    window.closeFailModal = function() {
        document.getElementById('cancel-modal').style.display = 'none';
        cancelOrderId = null;
    };

    var selectEl = document.getElementById('cancel-reason-select');
    if (selectEl) {
        selectEl.addEventListener('change', function() {
            var customInput = document.getElementById('cancel-reason-custom');
            if (this.value === 'other') {
                customInput.style.display = 'block';
                customInput.value = '';
                customInput.focus();
            } else {
                customInput.style.display = 'none';
                customInput.value = this.value;
            }
        });
        document.getElementById('cancel-reason-custom').value = selectEl.value;
    }

    var modalOverlay = document.getElementById('cancel-modal');
    if (modalOverlay) {
        modalOverlay.addEventListener('click', function (e) {
            if (e.target === this) window.closeFailModal();
        });
    }

    // Deliver modal logic
    var deliverModalEl = document.getElementById('deliver-modal');
    if (deliverModalEl) {
        deliverModalEl.addEventListener('click', function (e) {
            if (e.target === this) closeDeliverModal();
        });
    }

});

function showToast(message, isSuccess = true) {
    var toast = document.getElementById('toast');
    if(!toast) return;
    var messageEl = document.getElementById('toast-message');

    toast.style.borderColor = isSuccess ? 'var(--primary)' : 'var(--danger)';
    toast.querySelector('i').className = isSuccess ? 'fa-solid fa-circle-check' : 'fa-solid fa-circle-xmark';
    toast.querySelector('i').style.color = isSuccess ? 'var(--primary)' : 'var(--danger)';

    messageEl.textContent = message;
    toast.classList.add('show');

    setTimeout(function () {
        toast.classList.remove('show');
    }, 3000);
}

// Open/close deliver modal (global so inline onclick can call it)
function openDeliverModal(orderId) {
    var el = document.getElementById('deliver-modal');
    if (!el) return;
    document.getElementById('deliver-order-id-display').textContent = '#' + orderId;
    var form = document.getElementById('deliver-form');
    if (form) form.action = '/shipper/order/' + orderId + '/delivered';
    el.style.display = 'block';
}

function closeDeliverModal() {
    var el = document.getElementById('deliver-modal');
    if (!el) return;
    el.style.display = 'none';
}

