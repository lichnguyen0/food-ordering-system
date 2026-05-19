document.addEventListener('DOMContentLoaded', function() {
    const fileInput = document.getElementById('imageFileInput');
    const preview = document.getElementById('imagePreview');

    if (fileInput && preview) {
        fileInput.addEventListener('change', function() {
            const file = this.files[0];
            if (file) {
                const reader = new FileReader();
                reader.onload = function(e) {
                    preview.src = e.target.result;
                    preview.classList.add('has-image');
                }
                reader.readAsDataURL(file);
            }
        });
    }

    // Toggle chi tiết ưu đãi
    const promoToggle = document.getElementById('hasPromoToggle');
    const promoDetailSection = document.getElementById('promoDetailSection');

    if (promoToggle && promoDetailSection) {
        const togglePromoDetails = () => {
            if (promoToggle.checked) {
                promoDetailSection.style.display = 'grid';
            } else {
                promoDetailSection.style.display = 'none';
            }
        };

        // Chạy lần đầu để set trạng thái đúng (khi load trang edit)
        togglePromoDetails();

        // Lắng nghe sự kiện thay đổi
        promoToggle.addEventListener('change', togglePromoDetails);
    }
});
