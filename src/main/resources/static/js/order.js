document.addEventListener('DOMContentLoaded', function() {
    // Tab switching and filtering logic
    document.querySelectorAll('.order-tab-btn').forEach(btn => {
        btn.addEventListener('click', function() {
            // Remove active class from all buttons
            document.querySelectorAll('.order-tab-btn').forEach(b => b.classList.remove('active'));
            // Add active class to clicked button
            this.classList.add('active');
            
            const selectedStatus = this.getAttribute('data-status');
            const orderCards = document.querySelectorAll('.customer-order-card');
            
            orderCards.forEach(card => {
                const cardStatus = card.getAttribute('data-order-status');
                
                if (selectedStatus === 'ALL') {
                    card.style.display = 'block';
                } else if (selectedStatus === 'PENDING' && cardStatus === 'PENDING') {
                    card.style.display = 'block';
                } else if (selectedStatus === 'PREPARING' && (cardStatus === 'CONFIRMED' || cardStatus === 'PREPARING')) {
                    card.style.display = 'block';
                } else if (selectedStatus === 'DELIVERING' && cardStatus === 'DELIVERING') {
                    card.style.display = 'block';
                } else if (selectedStatus === 'DELIVERED' && cardStatus === 'DELIVERED') {
                    card.style.display = 'block';
                } else if (selectedStatus === 'CANCELLED' && cardStatus === 'CANCELLED') {
                    card.style.display = 'block';
                } else {
                    card.style.display = 'none';
                }
            });
        });
    });

    // Hàm tính toán số lượng đơn hàng cho mỗi tab
    function updateTabCounts() {
        const orders = document.querySelectorAll('.customer-order-card');
        const counts = {
            ALL: orders.length,
            PENDING: 0,
            PREPARING: 0,
            DELIVERING: 0,
            DELIVERED: 0,
            CANCELLED: 0
        };
        
        orders.forEach(card => {
            const status = card.getAttribute('data-order-status');
            if (status === 'PENDING') counts.PENDING++;
            else if (status === 'CONFIRMED' || status === 'PREPARING') counts.PREPARING++;
            else if (status === 'DELIVERING') counts.DELIVERING++;
            else if (status === 'DELIVERED') counts.DELIVERED++;
            else if (status === 'CANCELLED') counts.CANCELLED++;
        });
        
        document.querySelector('.order-tab-btn[data-status="ALL"] .tab-count').innerText = counts.ALL;
        document.querySelector('.order-tab-btn[data-status="PENDING"] .tab-count').innerText = counts.PENDING;
        document.querySelector('.order-tab-btn[data-status="PREPARING"] .tab-count').innerText = counts.PREPARING;
        document.querySelector('.order-tab-btn[data-status="DELIVERING"] .tab-count').innerText = counts.DELIVERING;
        document.querySelector('.order-tab-btn[data-status="DELIVERED"] .tab-count').innerText = counts.DELIVERED;
        document.querySelector('.order-tab-btn[data-status="CANCELLED"] .tab-count').innerText = counts.CANCELLED;
    }
    
    updateTabCounts();
});
