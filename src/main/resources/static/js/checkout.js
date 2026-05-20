document.addEventListener('DOMContentLoaded', function() {
    // Dropdown toggle
    window.toggleDropdown = function() {
        const dropdown = document.getElementById('paymentDropdown');
        if (dropdown) {
            dropdown.classList.toggle('open');
        }
    };
    
    // Close dropdown when clicking outside
    document.addEventListener('click', function(e) {
        const dropdown = document.getElementById('paymentDropdown');
        const header = document.querySelector('.dropdown-header');
        if (dropdown && !dropdown.contains(e.target) && !header.contains(e.target)) {
            dropdown.classList.remove('open');
        }
    });
    
    // Handle item selection
    const items = document.querySelectorAll('.dropdown-item:not(.disabled)');
    items.forEach(item => {
        item.addEventListener('click', function() {
            const text = this.querySelector('.dropdown-text').textContent;
            const icon = this.querySelector('.dropdown-icon').textContent;
            
            const headerText = document.querySelector('.dropdown-header .dropdown-text');
            const headerIcon = document.querySelector('.dropdown-header .dropdown-icon');
            
            if (headerText) headerText.textContent = text;
            if (headerIcon) headerIcon.textContent = icon;
            
            // Update hidden input
            const paymentInput = document.getElementById('paymentMethodInput');
            if (paymentInput) {
                if (text.includes('Tiền mặt')) paymentInput.value = 'CASH';
                else paymentInput.value = 'OTHER';
            }
            
            // Update selected class
            document.querySelectorAll('.dropdown-item').forEach(i => i.classList.remove('selected'));
            this.classList.add('selected');
            
            // Close dropdown
            document.getElementById('paymentDropdown').classList.remove('open');
        });
    });

    // Handle form submit loading effect
    const form = document.getElementById('checkoutForm');
    if (form) {
        form.addEventListener('submit', function() {
            const btn = document.querySelector('.btn-place-order');
            if (btn) {
                btn.innerHTML = '<i class="fa-solid fa-spinner fa-spin"></i> Đang đặt hàng...';
                btn.style.opacity = '0.7';
                btn.style.pointerEvents = 'none';
            }
        });
    }

    // Apply Promo Code
    window.applyPromoCode = function(code) {
        if (!code || code.trim() === '') return;
        
        fetch(`/api/cart/apply-coupon/${code}`, { method: 'POST' })
            .then(res => res.json())
            .then(data => {
                if (data.success) {
                    // Update input value
                    const inputField = document.getElementById('promoInput');
                    if (inputField) inputField.value = data.code;
                    
                    const hiddenInput = document.getElementById('couponCodeInput');
                    if (hiddenInput) hiddenInput.value = data.code;
                    
                    // Display discount row
                    const discountRow = document.getElementById('discountRow');
                    const appliedCodeDisplay = document.getElementById('appliedCodeDisplay');
                    const discountAmountDisplay = document.getElementById('discountAmountDisplay');
                    
                    if (discountRow && appliedCodeDisplay && discountAmountDisplay) {
                        appliedCodeDisplay.textContent = data.code;
                        discountAmountDisplay.textContent = '-' + new Intl.NumberFormat('vi-VN').format(data.discountAmount) + ' ₫';
                        discountRow.style.display = 'flex';
                    }
                    
                    // Recalculate and update final total amount at bottom bar
                    const subtotalTextEl = document.getElementById('subtotalText');
                    const deliveryFeeTextEl = document.getElementById('deliveryFeeText');
                    const totalValueEl = document.querySelector('.total-value');
                    
                    if (subtotalTextEl && deliveryFeeTextEl && totalValueEl) {
                        const subtotal = parseFloat(subtotalTextEl.getAttribute('data-price') || 0);
                        const deliveryFee = parseFloat(deliveryFeeTextEl.getAttribute('data-price') || 0);
                        const finalTotal = Math.max(0, subtotal + deliveryFee - data.discountAmount);
                        
                        totalValueEl.textContent = new Intl.NumberFormat('vi-VN').format(finalTotal) + ' ₫';
                    }
                    
                    alert(data.message);
                } else {
                    alert(data.message);
                }
            })
            .catch(err => {
                console.error("Error applying promo:", err);
                alert("Đã xảy ra lỗi khi áp dụng mã giảm giá!");
            });
    };

    window.applyPromoInput = function() {
        const inputField = document.getElementById('promoInput');
        if (inputField && inputField.value.trim() !== '') {
            applyPromoCode(inputField.value.trim().toUpperCase());
        } else {
            alert("Vui lòng nhập mã giảm giá trước!");
        }
    };
});
