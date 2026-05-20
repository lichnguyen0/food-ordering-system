// Admin Layout JavaScript

document.addEventListener('DOMContentLoaded', function() {
    // 1. Sidebar Toggle
    const sidebarBtn = document.getElementById('sidebarToggle');
    if (sidebarBtn) {
        sidebarBtn.addEventListener('click', function() {
            if (window.innerWidth > 992) {
                document.body.classList.toggle('sidebar-collapsed');
            } else {
                document.body.classList.toggle('sidebar-open');
            }
        });
    }

    // 2. Profile Dropdown Toggle
    const profileTrigger = document.getElementById('profileDropdownTrigger');
    const profileDropdown = document.getElementById('profileDropdown');

    if (profileTrigger && profileDropdown) {
        profileTrigger.addEventListener('click', function(e) {
            e.stopPropagation();
            profileDropdown.classList.toggle('show');
        });

        document.addEventListener('click', function(e) {
            if (!profileDropdown.contains(e.target) && !profileTrigger.contains(e.target)) {
                profileDropdown.classList.remove('show');
            }
        });
    }

    // 3. Submenu Toggle Logic
    const submenuItems = document.querySelectorAll('.has-submenu > .menu-item');
    submenuItems.forEach(item => {
        item.addEventListener('click', function(e) {
            e.preventDefault();
            const parent = this.parentElement;
            const submenu = parent.querySelector('.submenu');
            parent.classList.toggle('open');
            if (submenu) {
                submenu.classList.toggle('show');
            }
        });
    });
});

