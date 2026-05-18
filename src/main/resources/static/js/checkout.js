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
            
            // Update selected class
            document.querySelectorAll('.dropdown-item').forEach(i => i.classList.remove('selected'));
            this.classList.add('selected');
            
            // Close dropdown
            document.getElementById('paymentDropdown').classList.remove('open');
        });
    });
});
