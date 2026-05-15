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

    // 3. Quick Add Feedback
    const addButtons = document.querySelectorAll('.btn-quick-add');
    addButtons.forEach(btn => {
        btn.addEventListener('click', function() {
            this.innerHTML = '<i class="fa-solid fa-check"></i>';
            this.style.background = '#28a745';
            
            setTimeout(() => {
                this.innerHTML = '<i class="fa-solid fa-plus"></i>';
                this.style.background = ''; // Revert to CSS default
            }, 1000);
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
    const sections = document.querySelectorAll('.category-section');

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
