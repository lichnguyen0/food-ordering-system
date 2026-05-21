function toggleSidebar() {
    const sidebar = document.getElementById('profileSidebar');
    const overlay = document.getElementById('sidebarOverlay');
    if (sidebar && overlay) {
        sidebar.classList.toggle('show');
        overlay.classList.toggle('show');
        document.body.style.overflow = sidebar.classList.contains('show') ? 'hidden' : 'auto';
    }
}

function switchSidebarView(view) {
    const cartView = document.getElementById('cartViewContainer');
    const optView = document.getElementById('optionsViewContainer');
    const title = document.getElementById('sidebarTitle');
    const backBtn = document.getElementById('backToCartBtn');
    
    if (view === 'cart') {
        cartView.style.display = 'flex';
        cartView.style.flexDirection = 'column';
        optView.style.display = 'none';
        title.textContent = 'Giỏ hàng của bạn';
        backBtn.style.display = 'none';
        fetchCartStatus(); // Refresh cart view
    } else {
        cartView.style.display = 'none';
        optView.style.display = 'flex';
        optView.style.flexDirection = 'column';
        title.textContent = 'Tùy chỉnh món';
        backBtn.style.display = 'block';
    }
}

function toggleCartSidebar() {
    const sidebar = document.getElementById('cartSidebar');
    const overlay = document.getElementById('cartSidebarOverlay');
    if (sidebar && overlay) {
        sidebar.classList.toggle('show');
        overlay.classList.toggle('show');
        
        if (sidebar.classList.contains('show')) {
            document.body.style.overflow = 'hidden';
            switchSidebarView('cart'); // Always open in cart view by default
        } else {
            document.body.style.overflow = 'auto';
        }
    }
}

function fetchCartStatus() {
    fetch('/api/cart/status')
        .then(response => response.json())
        .then(data => updateCartUI(data))
        .catch(err => console.error("Error fetching cart status:", err));
}

function renderCartItems(items) {
    const listContainer = document.getElementById('cartSidebarItems');
    if (!listContainer) return;
    
    if (!items || items.length === 0) {
        listContainer.innerHTML = `
            <div class="empty-cart-message" style="display:flex; flex-direction:column; align-items:center; justify-content:center; padding: 50px 20px; color:#999; text-align:center; height:100%;">
                <i class="fa-solid fa-basket-shopping" style="font-size: 50px; margin-bottom: 20px; color:#e0e0e0;"></i>
                <p style="font-size: 16px; margin-bottom: 20px;">Giỏ hàng đang trống</p>
                <a href="javascript:void(0)" class="btn-checkout" onclick="toggleCartSidebar()" style="display:inline-block; padding:10px 20px; width:auto;">Duyệt nhà hàng</a>
            </div>`;
        document.getElementById('cartSidebarFooter').style.display = 'none';
        return;
    }
    
    document.getElementById('cartSidebarFooter').style.display = 'block';
    listContainer.innerHTML = '';
    
    items.forEach(item => {
        const itemEl = document.createElement('div');
        itemEl.className = 'cart-sidebar-item';
        itemEl.innerHTML = `
            <div class="item-img">
                <img src="${item.image || 'https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=60&h=60&fit=crop'}" alt="${item.foodName}">
            </div>
            <div class="item-details">
                <div class="item-name">${item.foodName}</div>
                ${item.optionsText ? `<div style="font-size:12px; color:#666; margin-bottom:4px;">${item.optionsText}</div>` : ''}
                <div class="item-price">${new Intl.NumberFormat('vi-VN').format(item.price)} ₫</div>
                <div class="item-qty-controls">
                    <button onclick="updateCartItemStr('${item.cartItemId}', 'decrease')">
                        <i class="fa-solid ${item.quantity === 1 ? 'fa-trash' : 'fa-minus'}"></i>
                    </button>
                    <span class="qty-display">${item.quantity}</span>
                    <button onclick="updateCartItemStr('${item.cartItemId}', 'add', ${item.foodId}, '${item.optionsText || ''}', ${(item.price)})">
                        <i class="fa-solid fa-plus"></i>
                    </button>
                </div>
            </div>
        `;
        listContainer.appendChild(itemEl);
    });
}

function updateCartUI(data) {
    // Update header badges
    const badge = document.getElementById('headerCartBadge');
    const price = document.getElementById('headerCartPrice');
    if (badge && price) {
        if (data.totalQuantity > 0) {
            badge.style.display = 'inline-block';
            price.style.display = 'inline-block';
            badge.textContent = data.totalQuantity;
            price.textContent = new Intl.NumberFormat('vi-VN').format(data.totalPrice) + ' ₫';
        } else {
            badge.style.display = 'none';
            price.style.display = 'none';
        }
    }
    
    // Update sidebar total
    document.getElementById('cartSidebarTotal').textContent = new Intl.NumberFormat('vi-VN').format(data.totalPrice) + ' ₫';
    
    // Render items
    renderCartItems(data.items);
    
    // Sync with restaurant detail page buttons if they exist
    if (typeof updateItemCardUI === 'function') {
        document.querySelectorAll('.cart-quantity-controls').forEach(controls => {
            const fId = controls.dataset.foodId;
            if (fId) updateItemCardUI(fId, data.itemQuantities[fId] || 0);
        });
    }
}

// Add/decrease by cartItemId (String) for items already in cart
window.updateCartItemStr = function(cartItemId, action, foodId, optionsText, unitPrice) {
    if (action === 'decrease') {
        fetch(`/api/cart/decrease/${cartItemId}`, { method: 'POST' })
            .then(r => r.json())
            .then(data => updateCartUI(data));
    } else {
        // Better approach for + button on cart: Just call /add-with-options but we need original extra price.
        // Let's just fix it by updating backend to handle normal /add on cartItemId. 
        // Actually, since this is a complex UI, let's just use the cartItemId to increment quantity in backend.
    }
}

// This is called from the Food Grid "+" button
window.openOptionsOrAdd = function(foodId, foodName, price, image, desc) {
    fetch(`/api/food/${foodId}/options`)
        .then(r => r.json())
        .then(options => {
            if (options && options.length > 0) {
                window.currentOptFood = foodId;
                window.currentOptQty = 1;
                window.currentOptBasePrice = parseFloat(price);
                
                document.getElementById('optFoodName').textContent = foodName;
                document.getElementById('optFoodPrice').textContent = new Intl.NumberFormat('vi-VN').format(price) + ' ₫';
                document.getElementById('optFoodImage').src = image || 'https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=300&h=300&fit=crop';
                document.getElementById('optFoodDesc').textContent = desc || '';
                document.getElementById('optQtyDisplay').textContent = '1';
                
                const groupsContainer = document.getElementById('optionsFormGroups');
                groupsContainer.innerHTML = '';
                
                options.forEach(group => {
                    const badgeHtml = group.required 
                        ? `<span style="font-size: 11px; background: #fff3e0; color: #e65100; padding: 3px 8px; border-radius: 12px; font-weight: 600;">Bắt buộc</span>`
                        : `<span style="font-size: 11px; background: #f5f5f5; color: #777; padding: 3px 8px; border-radius: 12px; font-weight: 500;">Tùy chọn</span>`;

                    let html = `<div class="opt-group" style="margin-bottom: 20px; background: #fff; padding: 12px 0; border-radius: 8px;">
                        <div style="display:flex; justify-content:space-between; align-items:center; margin-bottom:12px;">
                            <strong style="font-size:15px; color:#333; font-weight:700;">${group.groupName}</strong>
                            ${badgeHtml}
                        </div>`;
                    
                    group.optionItems.forEach(item => {
                        const inputType = group.multiple ? 'checkbox' : 'radio';
                        const inputName = `opt_${group.groupId}`;
                        const priceText = item.extraPrice > 0 ? `+${new Intl.NumberFormat('vi-VN').format(item.extraPrice)} ₫` : '';
                        
                        html += `<label style="display:flex; justify-content:space-between; align-items:center; padding:12px 0; border-bottom:1px solid #f9f9f9; cursor:pointer;">
                            <div style="display:flex; align-items:center; gap:12px;">
                                <input type="${inputType}" name="${inputName}" value="${item.itemName}" data-price="${item.extraPrice}" class="${inputType === 'checkbox' ? 'opt-checkbox' : 'opt-radio'}" onchange="updateOptTotalPrice()" ${group.required && !group.multiple ? 'required' : ''} style="accent-color: var(--grab-green); width: 18px; height: 18px; cursor: pointer;">
                                <span style="font-size:14px; color:#444; font-weight:500;">${item.itemName}</span>
                            </div>
                            <span style="font-size:13px; color:var(--grab-green); font-weight:600;">${priceText}</span>
                        </label>`;
                    });
                    html += `</div>`;
                    groupsContainer.innerHTML += html;
                });
                
                options.forEach(group => {
                    if (group.required && !group.multiple && group.optionItems.length > 0) {
                        const firstRadio = document.querySelector(`input[name="opt_${group.groupId}"]`);
                        if (firstRadio) firstRadio.checked = true;
                    }
                });
                
                updateOptTotalPrice();
                
                const sidebar = document.getElementById('cartSidebar');
                const overlay = document.getElementById('cartSidebarOverlay');
                sidebar.classList.add('show');
                overlay.classList.add('show');
                document.body.style.overflow = 'hidden';
                
                switchSidebarView('options');
            } else {
                // Kiểu 1: Món đơn giản (không có tùy chọn)
                // Thêm thẳng vào giỏ hàng, cập nhật header badge + card UI
                // KHÔNG mở sidebar để không gián đoạn trải nghiệm lướt menu
                const addFoodDirect = (force = false) => {
                    fetch(`/api/cart/add/${foodId}${force ? '?force=true' : ''}`, { method: 'POST' })
                        .then(r => r.json())
                        .then(data => {
                            if (data.status === 'CONFLICT') {
                                // Vẫn hiển thị modal cảnh báo trùng cửa hàng (không liên quan đến sidebar)
                                showCustomConfirmModal(data.conflictRestaurantName, () => {
                                    addFoodDirect(true);
                                });
                            } else {
                                // Chỉ cập nhật UI (header badge + thẻ món ăn), không mở sidebar
                                updateCartUI(data);
                            }
                        })
                        .catch(err => console.error("Error adding directly:", err));
                };
                addFoodDirect(false);
            }
        });
};

window.changeOptQty = function(delta) {
    if (window.currentOptQty + delta >= 1) {
        window.currentOptQty += delta;
        document.getElementById('optQtyDisplay').textContent = window.currentOptQty;
        updateOptTotalPrice();
    }
}

window.updateOptTotalPrice = function() {
    let extraPrice = 0;
    document.querySelectorAll('.opt-checkbox:checked, .opt-radio:checked').forEach(el => {
        extraPrice += parseFloat(el.dataset.price || 0);
    });
    const total = (window.currentOptBasePrice + extraPrice) * window.currentOptQty;
    document.getElementById('optTotalPriceDisplay').textContent = new Intl.NumberFormat('vi-VN').format(total) + ' ₫';
}

document.addEventListener('DOMContentLoaded', function() {
    const btnConfirm = document.getElementById('btnConfirmAddOptions');
    if (btnConfirm) {
        btnConfirm.addEventListener('click', function() {
            let optionsTextArr = [];
            let extraPrice = 0;
            document.querySelectorAll('.opt-checkbox:checked, .opt-radio:checked').forEach(el => {
                optionsTextArr.push(el.value);
                extraPrice += parseFloat(el.dataset.price || 0);
            });
            
            const optionsText = optionsTextArr.join(', ');
            
            const addWithOptionsFn = (index, force = false) => {
                if (index >= window.currentOptQty) {
                    switchSidebarView('cart');
                    return;
                }
                fetch(`/api/cart/add-with-options/${window.currentOptFood}${force ? '?force=true' : ''}`, {
                    method: 'POST',
                    headers: {'Content-Type': 'application/json'},
                    body: JSON.stringify({optionsText: optionsText, extraPrice: extraPrice})
                })
                .then(r => r.json())
                .then(data => {
                    if (data.status === 'CONFLICT') {
                        showCustomConfirmModal(data.conflictRestaurantName, () => {
                            // Clear cart on server and retry adding first item
                            addWithOptionsFn(0, true);
                        });
                    } else {
                        updateCartUI(data);
                        // Add the rest without force (since restaurant is now established)
                        addWithOptionsFn(index + 1, false);
                    }
                })
                .catch(err => console.error("Error adding options:", err));
            };
            addWithOptionsFn(0, false);
        });
    }
});

window.updateCartItemStr = function(cartItemId, action) {
     const url = action === 'add' ? `/api/cart/increase/${cartItemId}` : `/api/cart/decrease/${cartItemId}`;
     fetch(url, { method: 'POST' })
        .then(r => r.json())
        .then(data => updateCartUI(data));
}

function showCustomConfirmModal(restaurantName, onConfirm, onCancel) {
    // Remove existing modal if any
    const existingModal = document.getElementById('cartConflictModal');
    if (existingModal) existingModal.remove();

    // Create modal overlay
    const modal = document.createElement('div');
    modal.id = 'cartConflictModal';
    modal.style.cssText = `
        position: fixed;
        top: 0; left: 0; width: 100%; height: 100%;
        background: rgba(0, 0, 0, 0.55);
        backdrop-filter: blur(4px);
        display: flex; align-items: center; justify-content: center;
        z-index: 99999;
        opacity: 0;
        transition: opacity 0.3s ease;
    `;

    // Modal container card
    const card = document.createElement('div');
    card.style.cssText = `
        background: #fff;
        width: 90%;
        max-width: 400px;
        border-radius: 16px;
        padding: 24px;
        box-shadow: 0 10px 25px rgba(0,0,0,0.15);
        text-align: center;
        transform: translateY(20px);
        transition: transform 0.3s ease;
    `;

    // Modal content HTML
    card.innerHTML = `
        <div style="width: 56px; height: 56px; background: #FFF5F5; color: #FF4D4F; border-radius: 50%; display: flex; align-items: center; justify-content: center; margin: 0 auto 16px auto; font-size: 24px;">
            <i class="fa-solid fa-circle-exclamation"></i>
        </div>
        <h3 style="margin: 0 0 10px 0; font-size: 18px; font-weight: 700; color: #1f1f1f;">Tạo giỏ hàng mới?</h3>
        <p style="margin: 0 0 24px 0; font-size: 14.5px; line-height: 1.5; color: #666;">
            Bạn đang có món ăn từ cửa hàng khác trong giỏ hàng. Thêm món này sẽ xóa toàn bộ giỏ hàng hiện tại.
        </p>
        <div style="display: flex; gap: 12px; justify-content: center;">
            <button id="conflictCancelBtn" style="flex: 1; padding: 12px; border-radius: 8px; border: 1px solid #d9d9d9; background: #fff; color: #595959; font-weight: 600; font-size: 14px; cursor: pointer; transition: all 0.2s;">
                Hủy
            </button>
            <button id="conflictConfirmBtn" style="flex: 1; padding: 12px; border-radius: 8px; border: none; background: #FF4D4F; color: #fff; font-weight: 600; font-size: 14px; cursor: pointer; transition: all 0.2s;">
                Tạo giỏ mới
            </button>
        </div>
    `;

    modal.appendChild(card);
    document.body.appendChild(modal);

    // Fade in transition
    setTimeout(() => {
        modal.style.opacity = '1';
        card.style.transform = 'translateY(0)';
    }, 10);

    // Close function
    const closeModal = (callback) => {
        modal.style.opacity = '0';
        card.style.transform = 'translateY(20px)';
        setTimeout(() => {
            modal.remove();
            if (callback) callback();
        }, 300);
    };

    // Event listeners
    document.getElementById('conflictCancelBtn').addEventListener('click', () => {
        closeModal(onCancel);
    });
    document.getElementById('conflictConfirmBtn').addEventListener('click', () => {
        closeModal(onConfirm);
    });

    // Button hovers
    const cancelBtn = document.getElementById('conflictCancelBtn');
    cancelBtn.addEventListener('mouseenter', () => {
        cancelBtn.style.background = '#f5f5f5';
        cancelBtn.style.borderColor = '#d9d9d9';
    });
    cancelBtn.addEventListener('mouseleave', () => {
        cancelBtn.style.background = '#fff';
        cancelBtn.style.borderColor = '#d9d9d9';
    });

    const confirmBtn = document.getElementById('conflictConfirmBtn');
    confirmBtn.addEventListener('mouseenter', () => {
        confirmBtn.style.background = '#ff7875';
    });
    confirmBtn.addEventListener('mouseleave', () => {
        confirmBtn.style.background = '#FF4D4F';
    });
}
