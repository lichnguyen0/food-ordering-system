/**
 * Admin Invoices JavaScript
 */

document.addEventListener('DOMContentLoaded', function() {
    // 1. Khởi tạo chức năng tìm kiếm
    const searchInput = document.getElementById('invoiceSearch');
    if (searchInput) {
        searchInput.addEventListener('keyup', function(e) {
            const searchTerm = e.target.value.toLowerCase();
            const tableRows = document.querySelectorAll('#invoicesTable tbody tr');
            
            tableRows.forEach(row => {
                const text = row.textContent.toLowerCase();
                if (text.includes(searchTerm)) {
                    row.style.display = '';
                } else {
                    row.style.display = 'none';
                }
            });
        });
    }

    // 2. Khởi tạo chức năng lọc theo trạng thái
    const statusFilter = document.getElementById('statusFilter');
    if (statusFilter) {
        statusFilter.addEventListener('change', function(e) {
            const status = e.target.value;
            const tableRows = document.querySelectorAll('#invoicesTable tbody tr');
            
            // Một số map text cơ bản từ value sang text hiển thị (trong data mẫu)
            const statusMap = {
                'pending': 'Chờ xác nhận',
                'confirmed': 'Đã xác nhận',
                'shipping': 'Đang giao',
                'completed': 'Đã hoàn thành',
                'cancelled': 'Đã hủy'
            };

            const filterText = status ? statusMap[status].toLowerCase() : '';

            tableRows.forEach(row => {
                if (!status) {
                    row.style.display = '';
                    return;
                }
                
                const statusCellText = row.querySelector('td:nth-child(6)').textContent.toLowerCase();
                if (statusCellText.includes(filterText)) {
                    row.style.display = '';
                } else {
                    row.style.display = 'none';
                }
            });
        });
    }

    // 3. Các sự kiện xử lý nút bấm khác (nếu có)
});
