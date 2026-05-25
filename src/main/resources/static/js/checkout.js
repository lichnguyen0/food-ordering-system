document.addEventListener('DOMContentLoaded', function() {
    // Dropdown toggle for payment method
    window.toggleDropdown = function() {
        const dropdown = document.getElementById('paymentDropdown');
        if (dropdown) {
            dropdown.classList.toggle('open');
        }
    };
    
    // Dropdown toggle for address
    window.toggleAddressDropdown = function() {
        const dropdown = document.getElementById('addressDropdown');
        if (dropdown) {
            dropdown.classList.toggle('open');
        }
    };
    
    // Close dropdowns when clicking outside
    document.addEventListener('click', function(e) {
        const paymentDropdown = document.getElementById('paymentDropdown');
        const addressDropdown = document.getElementById('addressDropdown');
        const header = document.querySelector('.dropdown-header');
        
        if (paymentDropdown && !paymentDropdown.contains(e.target) && !header.contains(e.target)) {
            paymentDropdown.classList.remove('open');
        }
        
        if (addressDropdown && !addressDropdown.contains(e.target) && !header.contains(e.target)) {
            addressDropdown.classList.remove('open');
        }
    });
    
    // Handle payment method item selection
    const paymentItems = document.querySelectorAll('#paymentDropdown .dropdown-item:not(.disabled)');
    paymentItems.forEach(item => {
        item.addEventListener('click', function() {
            const text = this.querySelector('.dropdown-text').textContent;
            const icon = this.querySelector('.dropdown-icon').textContent;
            
            const headerText = document.querySelector('#paymentDropdown .dropdown-header .dropdown-text');
            const headerIcon = document.querySelector('#paymentDropdown .dropdown-header .dropdown-icon');
            
            if (headerText) headerText.textContent = text;
            if (headerIcon) headerIcon.textContent = icon;
            
            // Update hidden input
            const paymentInput = document.getElementById('paymentMethodInput');
            if (paymentInput) {
                if (text.includes('Tiền mặt')) paymentInput.value = 'CASH';
                else paymentInput.value = 'OTHER';
            }
            
            // Update selected class
            document.querySelectorAll('#paymentDropdown .dropdown-item').forEach(i => i.classList.remove('selected'));
            this.classList.add('selected');
            
            // Close dropdown
            document.getElementById('paymentDropdown').classList.remove('open');
        });
    });
    
    // Fetch and populate addresses
    window.fetchAddresses = function() {
        fetch('/api/addresses', { credentials: 'same-origin' })
            .then(response => {
                if (response.status === 401) {
                    const dropdownList = document.getElementById('addressDropdownList');
                    if (dropdownList) {
                        dropdownList.innerHTML = '<div class="dropdown-item disabled"><span class="dropdown-text">Vui lòng đăng nhập để chọn hồ sơ</span></div>';
                    }
                    throw new Error('Unauthorized');
                }
                if (!response.ok) {
                    throw new Error('Network response was not ok');
                }
                return response.json();
            })
            .then(addresses => {
                const dropdownList = document.getElementById('addressDropdownList');
                if (!dropdownList) return;
                
                // Clear existing items
                dropdownList.innerHTML = '';
                
                if (addresses.length === 0) {
                    const emptyItem = document.createElement('div');
                    emptyItem.className = 'dropdown-item disabled';
                    emptyItem.innerHTML = '<span class="dropdown-text">Chưa có địa chỉ được lưu</span>';
                    dropdownList.appendChild(emptyItem);
                } else {
                    // "Chọn hồ sơ" (clear selection) option at the top
                    const clearItem = document.createElement('div');
                    clearItem.className = 'dropdown-item';
                    clearItem.innerHTML = `<span class="dropdown-text" style="color:#666;">Chọn hồ sơ</span>`;
                    clearItem.addEventListener('click', function(e) {
                        e.stopPropagation();
                        const headerText = document.querySelector('#addressDropdown .dropdown-header .dropdown-text');
                        const headerIcon = document.querySelector('#addressDropdown .dropdown-header .dropdown-icon');
                        const addressIdInput = document.getElementById('addressIdInput');

                        if (headerText) headerText.textContent = 'Chọn hồ sơ';
                        if (headerIcon) headerIcon.innerHTML = '<i class="fa-solid fa-user"></i>';
                        if (addressIdInput) addressIdInput.value = '';

                        document.querySelectorAll('#addressDropdown .dropdown-item').forEach(i => i.classList.remove('selected'));
                        document.getElementById('addressDropdown').classList.remove('open');
                    });
                    dropdownList.appendChild(clearItem);

                    // Visual separator
                    const separator = document.createElement('div');
                    separator.style.height = '1px';
                    separator.style.background = '#eee';
                    separator.style.margin = '4px 8px';
                    dropdownList.appendChild(separator);

                    addresses.forEach(address => {
                        const item = document.createElement('div');
                        item.className = 'dropdown-item';
                        item.dataset.addressId = address.id;
                        item.style.display = 'flex';
                        item.style.alignItems = 'center';
                        item.style.justifyContent = 'space-between';

                        const label = address.label || `${address.recipientName} - ${address.addressLine}`;

                        // Main clickable area for selection
                        const mainContent = document.createElement('div');
                        mainContent.style.display = 'flex';
                        mainContent.style.alignItems = 'center';
                        mainContent.style.gap = '10px';
                        mainContent.style.flex = '1';
                        mainContent.style.cursor = 'pointer';
                        mainContent.innerHTML = `
                            <span class="dropdown-icon"><i class="fa-solid fa-map-marker-alt"></i></span>
                            <span class="dropdown-text">${label}</span>
                        `;

                        mainContent.addEventListener('click', function(e) {
                            e.stopPropagation();
                            const headerText = document.querySelector('#addressDropdown .dropdown-header .dropdown-text');
                            const headerIcon = document.querySelector('#addressDropdown .dropdown-header .dropdown-icon');
                            
                            if (headerText) headerText.textContent = address.label || address.recipientName;
                            if (headerIcon) headerIcon.innerHTML = '<i class="fa-solid fa-map-marker-alt"></i>';
                            
                            const addressIdInput = document.getElementById('addressIdInput');
                            if (addressIdInput) addressIdInput.value = address.id;
                            
                            document.querySelectorAll('#addressDropdown .dropdown-item').forEach(i => i.classList.remove('selected'));
                            item.classList.add('selected');
                            document.getElementById('addressDropdown').classList.remove('open');
                        });

                        // Edit icon
                        const editIcon = document.createElement('span');
                        editIcon.innerHTML = '<i class="fa-solid fa-pencil-alt" style="color:#888; font-size:13px; padding: 4px 8px;"></i>';
                        editIcon.style.cursor = 'pointer';
                        editIcon.title = 'Sửa địa chỉ';

                        editIcon.addEventListener('click', function(e) {
                            e.stopPropagation();
                            document.getElementById('addressDropdown').classList.remove('open');
                            openAddAddressModal(address);
                        });

                        item.appendChild(mainContent);
                        item.appendChild(editIcon);
                        dropdownList.appendChild(item);
                    });
                }
                
                // Always add "Thêm địa chỉ mới" at the bottom
                const addNewItem = document.createElement('div');
                addNewItem.className = 'dropdown-item';
                addNewItem.style.borderTop = '1px solid #eee';
                addNewItem.innerHTML = `<span class="dropdown-text" style="color:#d32f2f;font-weight:600;">+ Thêm địa chỉ mới</span>`;
                addNewItem.addEventListener('click', function() {
                    document.getElementById('addressDropdown').classList.remove('open');
                    openAddAddressModal();
                });
                dropdownList.appendChild(addNewItem);
            })
            .catch(error => {
                if (error.message === 'Unauthorized') return;
                console.error('Error fetching addresses:', error);
                const dropdownList = document.getElementById('addressDropdownList');
                if (dropdownList) {
                    dropdownList.innerHTML = '<div class="dropdown-item disabled"><span class="dropdown-text">Lỗi khi tải địa chỉ</span></div>';
                }
            });
    };
    
    // Fetch addresses on page load
    fetchAddresses();

    // ===== Address Modal Logic =====
    const addAddressModal = document.getElementById('addAddressModal');
    let editingAddressId = null;

    window.openAddAddressModal = function(address = null) {
        if (!addAddressModal) return;

        addAddressModal.classList.add('active');
        document.body.style.overflow = 'hidden';

        const titleEl = addAddressModal.querySelector('.confirm-modal-title');
        const saveBtn = addAddressModal.querySelector('.btn-confirm-submit');

        const fields = ['newAddrLabel', 'newAddrRecipient', 'newAddrPhone', 'newAddrLine'];
        fields.forEach(id => {
            const el = document.getElementById(id);
            if (el) el.value = '';
        });

        if (address) {
            // Edit mode
            editingAddressId = address.id;
            if (titleEl) titleEl.textContent = 'Sửa địa chỉ';
            if (saveBtn) saveBtn.innerHTML = '<i class="fa-solid fa-save"></i> Cập nhật';

            document.getElementById('newAddrLabel').value = address.label || '';
            document.getElementById('newAddrRecipient').value = address.recipientName || '';
            document.getElementById('newAddrPhone').value = address.phoneNumber || '';
            document.getElementById('newAddrLine').value = address.addressLine || '';
        } else {
            // Create mode
            editingAddressId = null;
            if (titleEl) titleEl.textContent = 'Thêm địa chỉ mới';
            if (saveBtn) saveBtn.innerHTML = '<i class="fa-solid fa-save"></i> Lưu vào hồ sơ';
        }
    };

    window.closeAddAddressModal = function() {
        if (addAddressModal) {
            addAddressModal.classList.remove('active');
            document.body.style.overflow = '';
        }
        editingAddressId = null; // reset edit mode when manually closing
    };

    window.submitNewAddressFromModal = function() {
        const label = document.getElementById('newAddrLabel')?.value.trim();
        const recipientName = document.getElementById('newAddrRecipient')?.value.trim();
        const phoneNumber = document.getElementById('newAddrPhone')?.value.trim();
        const addressLine = document.getElementById('newAddrLine')?.value.trim();

        if (!label || !recipientName || !phoneNumber || !addressLine) {
            alert('Vui lòng điền đầy đủ các trường bắt buộc (*)');
            return;
        }

        const payload = { label, recipientName, phoneNumber, addressLine };
        const saveBtn = addAddressModal?.querySelector('.btn-confirm-submit');
        const originalText = saveBtn ? saveBtn.innerHTML : '';

        if (saveBtn) {
            saveBtn.disabled = true;
            saveBtn.innerHTML = '<i class="fa-solid fa-spinner fa-spin"></i> Đang lưu...';
        }

        const isEdit = !!editingAddressId;
        const url = isEdit ? `/api/addresses/${editingAddressId}` : '/api/addresses';
        const method = isEdit ? 'PUT' : 'POST';

        fetch(url, {
            method: method,
            headers: { 'Content-Type': 'application/json' },
            credentials: 'same-origin',
            body: JSON.stringify(payload)
        })
        .then(response => {
            if (!response.ok) {
                return response.text().then(t => { throw new Error(t || 'Không thể lưu địa chỉ'); });
            }
            return response.json();
        })
        .then(savedAddress => {
            closeAddAddressModal();
            editingAddressId = null;
            fetchAddresses();

            // Auto-select after refresh
            setTimeout(() => {
                const selector = `#addressDropdownList .dropdown-item[data-address-id="${savedAddress.id}"]`;
                const newItem = document.querySelector(selector);
                if (newItem) newItem.click();
            }, 400);
        })
        .catch(err => {
            console.error('Save address error:', err);
            alert('Lỗi khi lưu địa chỉ: ' + err.message);
        })
        .finally(() => {
            if (saveBtn) {
                saveBtn.disabled = false;
                saveBtn.innerHTML = originalText || '<i class="fa-solid fa-save"></i> Lưu vào hồ sơ';
            }
        });
    };

    // Close address modal on overlay click
    if (addAddressModal) {
        addAddressModal.addEventListener('click', function(e) {
            if (e.target === addAddressModal) {
                closeAddAddressModal();
            }
        });
    }
    
    // ===== Confirmation Modal Logic =====
    const form = document.getElementById('checkoutForm');
    const modal = document.getElementById('confirmOrderModal');
    const btnPlaceOrder = document.querySelector('.btn-place-order');

    // Intercept "Đặt đơn" button — show modal instead of submitting
    if (btnPlaceOrder) {
        btnPlaceOrder.addEventListener('click', function(e) {
            e.preventDefault();
            e.stopPropagation();
            // Validate form before showing modal
            if (form && !form.checkValidity()) {
                form.reportValidity();
                return;
            }
            syncModalTotals();
            openConfirmModal();
        });
    }

    // Also intercept form submit (in case submitted via Enter key)
    if (form) {
        form.addEventListener('submit', function(e) {
            if (!form.dataset.confirmed) {
                e.preventDefault();
                if (!form.checkValidity()) {
                    form.reportValidity();
                    return;
                }
                syncModalTotals();
                openConfirmModal();
            } else {
                // Confirmed — show loading effect
                const btn = document.querySelector('.btn-place-order');
                if (btn) {
                    btn.innerHTML = '<i class="fa-solid fa-spinner fa-spin"></i> Đang đặt hàng...';
                    btn.style.opacity = '0.7';
                    btn.style.pointerEvents = 'none';
                }
            }
        });
    }

    // Sync current totals into modal
    function syncModalTotals() {
        const totalValueEl = document.querySelector('.total-value');
        const modalTotal = document.getElementById('modalTotal');
        if (totalValueEl && modalTotal) {
            modalTotal.textContent = totalValueEl.textContent;
        }

        // Sync discount if applied
        const discountRow = document.getElementById('discountRow');
        const modalDiscountRow = document.getElementById('modalDiscountRow');
        const discountAmountDisplay = document.getElementById('discountAmountDisplay');
        const modalDiscount = document.getElementById('modalDiscount');

        if (discountRow && discountRow.style.display !== 'none') {
            if (modalDiscountRow) modalDiscountRow.style.display = 'flex';
            if (discountAmountDisplay && modalDiscount) {
                modalDiscount.textContent = discountAmountDisplay.textContent;
            }
        }
    }

    // Open modal with animation
    window.openConfirmModal = function() {
        if (modal) {
            modal.classList.add('active');
            document.body.style.overflow = 'hidden';
            // Force reflow for animation
            void modal.offsetWidth;
        }
    };

    // Close modal
    window.closeConfirmModal = function() {
        if (modal) {
            modal.classList.remove('active');
            document.body.style.overflow = '';
        }
    };

    // Confirm and submit the form
    window.confirmAndSubmit = function() {
        if (form) {
            form.dataset.confirmed = 'true';
            closeConfirmModal();
            // Small delay for modal close animation
            setTimeout(function() {
                form.submit();
                // Show loading on button
                const btn = document.querySelector('.btn-place-order');
                if (btn) {
                    btn.innerHTML = '<i class="fa-solid fa-spinner fa-spin"></i> Đang đặt hàng...';
                    btn.style.opacity = '0.7';
                    btn.style.pointerEvents = 'none';
                }
            }, 200);
        }
    };

    // Close modal on overlay click
    if (modal) {
        modal.addEventListener('click', function(e) {
            if (e.target === modal) {
                closeConfirmModal();
            }
        });
    }

    // Close modal on Escape key
    document.addEventListener('keydown', function(e) {
        if (e.key === 'Escape') {
            if (modal && modal.classList.contains('active')) {
                closeConfirmModal();
            }
            if (addAddressModal && addAddressModal.classList.contains('active')) {
                closeAddAddressModal();
            }
        }
    });

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
                        
                        // Update "Thành tiền" row inside Tóm tắt đơn hàng
                        const finalTotalText = document.getElementById('finalTotalText');
                        if (finalTotalText) {
                            finalTotalText.textContent = new Intl.NumberFormat('vi-VN').format(finalTotal) + ' ₫';
                        }
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
