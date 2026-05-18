/**
 * Restaurant Detail Page Interactions - Refined
 */

document.addEventListener('DOMContentLoaded', function() {
    const tabPills = document.querySelectorAll('.tab-pill');
    const sections = document.querySelectorAll('.category-section');
    const tabsArea = document.querySelector('.tabs-scroll-area');

    // 1. Smooth Scroll with Offset for Sticky Tabs
    tabPills.forEach(pill => {
        pill.addEventListener('click', function(e) {
            const targetId = this.getAttribute('href');
            
            if (targetId.startsWith('#cat-')) {
                e.preventDefault();
                const targetSection = document.querySelector(targetId);
                
                if (targetSection) {
                    const offset = 140; // Space for Header + Sticky Tabs
                    const bodyRect = document.body.getBoundingClientRect().top;
                    const elementRect = targetSection.getBoundingClientRect().top;
                    const elementPosition = elementRect - bodyRect;
                    const offsetPosition = elementPosition - offset;

                    window.scrollTo({
                        top: offsetPosition,
                        behavior: 'smooth'
                    });
                }
            }
        });
    });

    // 2. Scrollspy Logic
    window.addEventListener('scroll', function() {
        let current = 'all';
        const scrollPosition = window.scrollY + 160;

        sections.forEach(section => {
            const sectionTop = section.offsetTop;
            if (scrollPosition >= sectionTop) {
                current = section.getAttribute('id');
            }
        });

        tabPills.forEach(pill => {
            pill.classList.remove('active');
            const href = pill.getAttribute('href');
            if (href === '#' + current || (current === 'all' && href === '#all')) {
                pill.classList.add('active');
                
                // Keep active pill in view within the horizontal scroll
                if (tabsArea) {
                    const pillLeft = pill.offsetLeft;
                    const pillWidth = pill.offsetWidth;
                    const areaWidth = tabsArea.offsetWidth;
                    
                    if (pillLeft < tabsArea.scrollLeft || (pillLeft + pillWidth) > (tabsArea.scrollLeft + areaWidth)) {
                        tabsArea.scrollTo({
                            left: pillLeft - 20,
                            behavior: 'smooth'
                        });
                    }
                }
            }
        });
    });

    // 3. Header Cart Logic & Quick Add
    const headerCartBadge = document.getElementById('headerCartBadge');
    const headerCartPrice = document.getElementById('headerCartPrice');
    const addButtons = document.querySelectorAll('.btn-quick-add');

    // Function to update cart UI
    function updateCartUI(totalQty, totalPrice) {
        if (headerCartBadge && headerCartPrice) {
            if (totalQty > 0) {
                headerCartBadge.style.display = 'inline-block';
                headerCartPrice.style.display = 'inline-block';
                
                headerCartBadge.textContent = totalQty;
                headerCartPrice.textContent = new Intl.NumberFormat('vi-VN').format(totalPrice) + ' ₫';
                
                // Add a small pop animation
                const cartBtn = document.getElementById('headerCartBtn');
                if (cartBtn) {
                    cartBtn.style.transform = 'scale(1.1)';
                    setTimeout(() => cartBtn.style.transform = 'scale(1)', 200);
                }
            } else {
                headerCartBadge.style.display = 'none';
                headerCartPrice.style.display = 'none';
            }
        }
    }

    // Function to update individual food card UI
    function updateItemCardUI(foodId, quantity) {
        const controlContainer = document.querySelector(`.cart-quantity-controls[data-food-id="${foodId}"]`);
        if (!controlContainer) return;

        const btnAdd = controlContainer.querySelector('.btn-quick-add');
        const btnDecrease = controlContainer.querySelector('.btn-decrease');
        const qtyText = controlContainer.querySelector('.qty-text');

        if (quantity > 0) {
            controlContainer.classList.add('active');
            btnDecrease.style.display = 'flex';
            qtyText.style.display = 'inline-block';
            qtyText.textContent = quantity;

            // Update decrease button icon based on quantity
            if (quantity === 1) {
                btnDecrease.innerHTML = '<i class="fa-solid fa-trash"></i>';
                btnDecrease.classList.remove('minus-icon');
            } else {
                btnDecrease.innerHTML = '<i class="fa-solid fa-minus"></i>';
                btnDecrease.classList.add('minus-icon');
            }
        } else {
            controlContainer.classList.remove('active');
            btnDecrease.style.display = 'none';
            qtyText.style.display = 'none';
        }
    }

    // Fetch initial cart state on load
    fetch('/api/cart/status')
        .then(response => response.json())
        .then(data => {
            updateCartUI(data.totalQuantity, data.totalPrice);
            // Restore UI for items already in cart
            if (data.itemQuantities) {
                Object.keys(data.itemQuantities).forEach(foodId => {
                    updateItemCardUI(foodId, data.itemQuantities[foodId]);
                });
            }
        })
        .catch(err => console.error("Error fetching cart status:", err));

    // Handle Add and Decrease clicks
    const quantityControls = document.querySelectorAll('.cart-quantity-controls');
    quantityControls.forEach(control => {
        const foodId = control.getAttribute('data-food-id');
        const btnAdd = control.querySelector('.btn-quick-add');
        const btnDecrease = control.querySelector('.btn-decrease');

        btnAdd.addEventListener('click', function(e) {
            e.preventDefault();
            
            // Visual feedback on button
            const icon = this.querySelector('i');
            const originalClass = icon.className;
            icon.className = 'fa-solid fa-check';
            this.style.background = '#008a3d';
            
            setTimeout(() => {
                icon.className = originalClass;
                this.style.background = '';
            }, 300);

            // AJAX request to backend
            fetch(`/api/cart/add/${foodId}`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' }
            })
            .then(response => response.json())
            .then(data => {
                updateCartUI(data.totalQuantity, data.totalPrice);
                updateItemCardUI(foodId, data.itemQuantities[foodId] || 0);
            })
            .catch(err => console.error("Error adding to cart:", err));
        });

        btnDecrease.addEventListener('click', function(e) {
            e.preventDefault();
            
            // Visual feedback on button
            this.style.transform = 'scale(0.9)';
            setTimeout(() => this.style.transform = '', 150);

            // AJAX request to backend
            fetch(`/api/cart/decrease/${foodId}`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' }
            })
            .then(response => response.json())
            .then(data => {
                updateCartUI(data.totalQuantity, data.totalPrice);
                updateItemCardUI(foodId, data.itemQuantities[foodId] || 0);
            })
            .catch(err => console.error("Error decreasing cart item:", err));
        });
    });

    // 4. Custom Delivery Dropdowns (n5.png style)
    function populateDeliveryDropdowns() {
        const dateDropdown = document.getElementById('dateDropdown');
        const timeDropdown = document.getElementById('timeDropdown');
        const dateOptions = document.getElementById('dateOptions');
        const timeOptions = document.getElementById('timeOptions');
        const dateText = document.getElementById('selectedDateText');
        const timeText = document.getElementById('selectedTimeText');

        if (!dateDropdown || !timeDropdown) return;

        // Toggle dropdowns
        [dateDropdown, timeDropdown].forEach(dropdown => {
            dropdown.querySelector('.dropdown-trigger').addEventListener('click', (e) => {
                e.stopPropagation();
                // Close other dropdown
                const other = dropdown === dateDropdown ? timeDropdown : dateDropdown;
                other.classList.remove('active');
                dropdown.classList.toggle('active');
            });
        });

        // Close when clicking outside
        document.addEventListener('click', () => {
            dateDropdown.classList.remove('active');
            timeDropdown.classList.remove('active');
        });

        const rawHours = timeDropdown.getAttribute('data-opening-hours') || '07:00 - 22:00';
        const shifts = rawHours.replace(/–|—/g, '-').replace(/\s+/g, ' ').split(/[,; ]+/).filter(s => s.includes('-'));
        const parsedShifts = shifts.map(shift => {
            const parts = shift.split('-').map(t => t.trim());
            const [startH, startM] = parts[0].split(':').map(Number);
            const [endH, endM] = parts[1].split(':').map(Number);
            return { startH, startM, endH, endM };
        });

        const days = ['Chủ Nhật', 'Thứ 2', 'Thứ 3', 'Thứ 4', 'Thứ 5', 'Thứ 6', 'Thứ 7'];
        const now = new Date();

        // Populate Dates
        dateOptions.innerHTML = '';
        for (let i = 0; i < 3; i++) {
            const d = new Date();
            d.setDate(now.getDate() + i);
            const label = (i === 0) ? 'Hôm nay' : `${days[d.getDay()]}, ngày ${d.getDate()} tháng ${d.getMonth() + 1}`;
            
            const item = document.createElement('div');
            item.className = 'option-item' + (i === 0 ? ' selected' : '');
            item.textContent = label;
            item.addEventListener('click', () => {
                dateText.textContent = label;
                document.querySelectorAll('#dateOptions .option-item').forEach(opt => opt.classList.remove('selected'));
                item.classList.add('selected');
                updateTimeSlots(i);
            });
            dateOptions.appendChild(item);
        }

        function updateTimeSlots(dateIndex) {
            timeOptions.innerHTML = '';
            const isToday = parseInt(dateIndex) === 0;
            const currentH = now.getHours();
            const currentM = now.getMinutes();

            let firstItemSet = false;

            // 1. "Now" option
            if (isToday) {
                const isOpenNow = parsedShifts.some(s => {
                    const nowTotal = currentH * 60 + currentM;
                    const startTotal = s.startH * 60 + s.startM;
                    const endTotal = s.endH * 60 + s.endM;
                    return nowTotal >= startTotal && nowTotal < endTotal;
                });

                if (isOpenNow) {
                    createTimeOption('Ngay bây giờ', 'now', true);
                    firstItemSet = true;
                }
            }

            // 2. Shift slots
            parsedShifts.forEach(shift => {
                let sH = isToday ? currentH : shift.startH;
                let sM = isToday ? currentM : shift.startM;

                if (isToday) {
                    sM += 20; // Buffer
                    if (sM >= 60) { sM -= 60; sH++; }
                    sM = Math.ceil(sM / 15) * 15;
                    if (sM >= 60) { sM = 0; sH++; }
                }

                if (sH < shift.startH || (sH === shift.startH && sM < shift.startM)) {
                    sH = shift.startH;
                    sM = shift.startM;
                }

                for (let h = sH; h <= shift.endH; h++) {
                    let startMin = (h === sH) ? sM : 0;
                    for (let m = startMin; m < 60; m += 15) {
                        let eH = h, eM = m + 30;
                        if (eM >= 60) { eM -= 60; eH++; }

                        if (h > shift.endH || (h === shift.endH && m >= shift.endM)) break;
                        if (eH > shift.endH || (eH === shift.endH && eM > shift.endM)) break;

                        const timeStr = `${h.toString().padStart(2,'0')}:${m.toString().padStart(2,'0')} - ${eH.toString().padStart(2,'0')}:${eM.toString().padStart(2,'0')}`;
                        createTimeOption(timeStr, timeStr, !firstItemSet);
                        firstItemSet = true;
                    }
                }
            });

            if (timeOptions.children.length === 0) {
                createTimeOption('Đã đóng cửa', 'closed', true);
            }
        }

        function createTimeOption(label, value, isSelected) {
            const item = document.createElement('div');
            item.className = 'option-item' + (isSelected ? ' selected' : '');
            item.textContent = label;
            if (isSelected) timeText.textContent = label;

            item.addEventListener('click', () => {
                timeText.textContent = label;
                document.querySelectorAll('#timeOptions .option-item').forEach(opt => opt.classList.remove('selected'));
                item.classList.add('selected');
            });
            timeOptions.appendChild(item);
        }

        updateTimeSlots(0);
    }

    populateDeliveryDropdowns();

    // 5. ScrollSpy & Smooth Scroll for Tabs
    const tabItems = document.querySelectorAll('.tab-item');
    // sections already declared at the top

    function updateActiveTab() {
        let currentSectionId = '';
        sections.forEach(section => {
            const sectionTop = section.offsetTop;
            const sectionHeight = section.clientHeight;
            if (window.scrollY >= sectionTop - 100) {
                currentSectionId = section.getAttribute('id');
            }
        });

        tabItems.forEach(tab => {
            tab.classList.remove('active');
            if (tab.getAttribute('href') === `#${currentSectionId}`) {
                tab.classList.add('active');
            }
        });
    }

    // Smooth scroll when clicking tabs
    tabItems.forEach(tab => {
        tab.addEventListener('click', function(e) {
            e.preventDefault();
            const targetId = this.getAttribute('href');
            const targetSection = document.querySelector(targetId);
            if (targetSection) {
                window.scrollTo({
                    top: targetSection.offsetTop - 70,
                    behavior: 'smooth'
                });
            }
        });
    });

    window.addEventListener('scroll', updateActiveTab);
});
