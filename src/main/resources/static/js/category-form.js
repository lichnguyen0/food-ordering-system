document.addEventListener('DOMContentLoaded', function() {
    // 1. Đồng bộ nhãn của nút gạt trạng thái hiển thị
    const checkbox = document.querySelector('.admin-switch-input');
    const labelText = document.getElementById('activeLabelText');
    if (checkbox && labelText) {
        const updateLabel = () => {
            labelText.textContent = checkbox.checked ? 'Hiển thị trên Trang chủ' : 'Ẩn khỏi Trang chủ';
        };
        checkbox.addEventListener('change', updateLabel);
        updateLabel();
    }

    // 2. Xem trước ảnh khi chọn file từ máy tính
    const fileInput = document.querySelector('input[type="file"][name="imageFile"]');
    if (fileInput) {
        fileInput.addEventListener('change', function(e) {
            const file = e.target.files[0];
            if (file) {
                let preview = document.getElementById('currentImagePreview');
                if (!preview) {
                    // Tạo mới container preview nếu chưa có
                    const container = document.createElement('div');
                    container.className = 'category-preview-container';
                    
                    preview = document.createElement('img');
                    preview.id = 'currentImagePreview';
                    preview.className = 'category-preview-img';
                    
                    const textDiv = document.createElement('div');
                    const title = document.createElement('p');
                    title.className = 'category-preview-title';
                    title.textContent = 'Ảnh xem trước';
                    
                    const sub = document.createElement('span');
                    sub.className = 'category-preview-subtitle';
                    sub.textContent = 'Ảnh mới chuẩn bị được lưu';
                    
                    textDiv.appendChild(title);
                    textDiv.appendChild(sub);
                    container.appendChild(preview);
                    container.appendChild(textDiv);
                    
                    fileInput.parentElement.insertAdjacentElement('beforebegin', container);
                } else {
                    const subText = preview.nextElementSibling ? preview.nextElementSibling.querySelector('span') : null;
                    if (subText) subText.textContent = 'Ảnh mới chuẩn bị được lưu';
                }
                
                const reader = new FileReader();
                reader.onload = function(event) {
                    preview.src = event.target.result;
                };
                reader.readAsDataURL(file);
            }
        });
    }
});
