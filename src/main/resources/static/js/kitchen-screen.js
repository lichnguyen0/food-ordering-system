/**
 * kitchen-screen.js — Logic hủy đơn đột xuất trên màn hình bếp
 */
document.addEventListener('DOMContentLoaded', function () {

    let cancelOrderId = null;

    // Expose global functions for th:onclick
    window.openCancelModal = function (orderId) {
        cancelOrderId = orderId;
        document.getElementById('cancel-order-id-display').textContent = '#' + orderId;
        document.getElementById('cancel-form').action = '/kitchen/order/' + orderId + '/cancel';
        document.getElementById('cancel-reason-select').value = 'Hết nguyên liệu';
        document.getElementById('cancel-reason-custom').style.display = 'none';
        document.getElementById('cancel-reason-custom').value = 'Hết nguyên liệu';
        document.getElementById('cancel-modal').style.display = 'block';
    };

    window.closeCancelModal = function () {
        document.getElementById('cancel-modal').style.display = 'none';
        cancelOrderId = null;
    };

    // Reason select change handler
    var selectEl = document.getElementById('cancel-reason-select');
    if (selectEl) {
        selectEl.addEventListener('change', function () {
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

        // Sync on load
        var customInput = document.getElementById('cancel-reason-custom');
        if (customInput) {
            customInput.value = selectEl.value;
        }
    }

    // Close modal on backdrop click
    var modalOverlay = document.getElementById('cancel-modal');
    if (modalOverlay) {
        modalOverlay.addEventListener('click', function (e) {
            if (e.target === this) window.closeCancelModal();
        });
    }
});
