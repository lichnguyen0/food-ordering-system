/* ========================================
   Admin Category List - Page Specific JavaScript
   Tách riêng theo yêu cầu: không nhúng JS vào HTML
   ======================================== */

document.addEventListener('DOMContentLoaded', function () {
    const searchInput = document.getElementById('categorySearch');

    if (searchInput) {
        searchInput.addEventListener('keypress', function (e) {
            if (e.key === 'Enter') {
                e.preventDefault();
                const keyword = this.value.trim();
                let url = '/admin/categories';
                if (keyword) {
                    url += '?keyword=' + encodeURIComponent(keyword);
                }
                window.location.href = url;
            }
        });

        // Clear search when input is cleared and Enter is pressed with empty value
        searchInput.addEventListener('input', function () {
            if (this.value.trim() === '') {
                const params = new URLSearchParams(window.location.search);
                if (params.has('keyword')) {
                    window.location.href = '/admin/categories';
                }
            }
        });
    }
});