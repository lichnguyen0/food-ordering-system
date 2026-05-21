/* ========================================
   Admin Food Grid - Page Specific JavaScript
   Tách riêng theo yêu cầu: không nhúng JS vào HTML
   ======================================== */

document.addEventListener('DOMContentLoaded', function () {
    const categorySelect = document.getElementById('categoryFilter');

    if (categorySelect) {
        categorySelect.addEventListener('change', function () {
            const selectedCategoryId = this.value;
            // Xây dựng URL mới, reset về trang 0 khi đổi category
            let url = '/admin/foods/grid';
            const params = new URLSearchParams();

            if (selectedCategoryId && selectedCategoryId !== '') {
                params.set('categoryId', selectedCategoryId);
            }
            // Không cần set page=0 vì mặc định là 0 khi không truyền

            if (params.toString()) {
                url += '?' + params.toString();
            }

            window.location.href = url;
        });
    }

    // Optional: Có thể mở rộng sau này (search, items per page, v.v.)
});