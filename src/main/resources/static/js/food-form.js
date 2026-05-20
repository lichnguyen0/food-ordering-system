document.addEventListener('DOMContentLoaded', function() {
    const fileInput = document.querySelector('input[type="file"]');
    const fileNameDisplay = document.querySelector('.file-name-display');
    const previewImg = document.querySelector('.main-preview-img');
    const thumbnailRow = document.querySelector('.thumbnail-row');
    
    let selectedFiles = []; // To store File objects

    if (fileInput) {
        fileInput.addEventListener('change', function() {
            if (this.files && this.files.length > 0) {
                // Add new files to our list
                Array.from(this.files).forEach(file => {
                    selectedFiles.push(file);
                });
                
                updatePreviews();
                syncFiles();
            }
        });
    }

    function updatePreviews() {
        // Clear existing dynamic thumbnails (but keep server-side ones if any, 
        // actually let's clear all for simplicity or handle both)
        // For now, let's just clear and rebuild the row
        thumbnailRow.innerHTML = '';
        
        if (selectedFiles.length === 0) {
            fileNameDisplay.textContent = 'No file chosen';
            previewImg.src = 'https://via.placeholder.com/800x450?text=Product+Preview';
            return;
        }

        fileNameDisplay.textContent = selectedFiles.length + ' files selected';
        
        selectedFiles.forEach((file, index) => {
            const reader = new FileReader();
            
            reader.onload = function(e) {
                const wrapper = document.createElement('div');
                wrapper.className = 'thumb-wrapper';
                
                const thumb = document.createElement('img');
                thumb.src = e.target.result;
                thumb.className = 'thumb-img' + (index === 0 ? ' active' : '');
                
                const removeBtn = document.createElement('div');
                removeBtn.className = 'remove-img';
                removeBtn.innerHTML = '<i class="fa-solid fa-xmark"></i>';
                
                removeBtn.addEventListener('click', (event) => {
                    event.stopPropagation(); // Prevent thumbnail click
                    removeFile(index);
                });

                thumb.addEventListener('click', function() {
                    document.querySelectorAll('.thumb-img').forEach(t => t.classList.remove('active'));
                    this.classList.add('active');
                    previewImg.src = this.src;
                });
                
                wrapper.appendChild(thumb);
                wrapper.appendChild(removeBtn);
                thumbnailRow.appendChild(wrapper);
                
                // Set first image as main preview if it's the first one being added/rebuilt
                if (index === 0 && !thumbnailRow.querySelector('.thumb-img.active')) {
                    previewImg.src = e.target.result;
                } else if (index === 0) {
                    previewImg.src = e.target.result;
                }
            }
            
            reader.readAsDataURL(file);
        });
    }

    function removeFile(index) {
        selectedFiles.splice(index, 1);
        updatePreviews();
        syncFiles();
    }

    function syncFiles() {
        const dataTransfer = new DataTransfer();
        selectedFiles.forEach(file => {
            dataTransfer.items.add(file);
        });
        fileInput.files = dataTransfer.files;
    }

    // Initialize existing thumbnails (for Edit mode)
    // Note: This logic might need refinement to work with server-side thumbnails
    const existingThumbs = document.querySelectorAll('.thumb-img');
    if (existingThumbs.length > 0 && selectedFiles.length === 0) {
        existingThumbs.forEach(thumb => {
            thumb.addEventListener('click', function() {
                document.querySelectorAll('.thumb-img').forEach(t => t.classList.remove('active'));
                this.classList.add('active');
                previewImg.src = this.src;
            });
        });
    }

    // Status Toggle Logic
    const statusToggle = document.getElementById('statusToggle');
    const statusHidden = document.getElementById('statusHidden');

    if (statusToggle && statusHidden) {
        // Initial sync if empty
        if (!statusHidden.value) {
            statusHidden.value = statusToggle.checked ? 'AVAILABLE' : 'SOLD_OUT';
        }

        statusToggle.addEventListener('change', function() {
            statusHidden.value = this.checked ? 'AVAILABLE' : 'SOLD_OUT';
        });
    }

    // Dynamic Discount Toggle Logic
    const discountPriceInput = document.getElementById('discountPrice');
    const discountToggle = document.getElementById('discountToggle');
    if (discountPriceInput && discountToggle) {
        const updateDiscountToggle = () => {
            const val = discountPriceInput.value.trim();
            discountToggle.checked = (val !== '' && parseFloat(val) > 0);
        };
        discountPriceInput.addEventListener('input', updateDiscountToggle);
        discountPriceInput.addEventListener('change', updateDiscountToggle);
        // Initial run
        updateDiscountToggle();
    }
});

// Dynamic Option Groups Logic
function addOptionGroup() {
    const container = document.getElementById('optionGroupsContainer');
    const groupCount = container.querySelectorAll('.option-group-card').length;
    
    const groupHtml = `
        <div class="option-group-card" style="border: 1px solid #eee; padding: 15px; border-radius: 8px; margin-bottom: 15px; background: #fafafa; position: relative;">
            <button type="button" onclick="this.closest('.option-group-card').remove(); reindexOptions();" style="position: absolute; top: 15px; right: 15px; background: none; border: none; color: #dc3545; cursor: pointer;"><i class="fa-solid fa-trash"></i></button>
            <input type="hidden" name="optionGroups[${groupCount}].groupId" value="">
            
            <div class="form-row" style="margin-bottom: 10px;">
                <div class="form-group" style="flex: 2;">
                    <label>Tên Nhóm (VD: Kích cỡ, Topping)</label>
                    <input type="text" name="optionGroups[${groupCount}].groupName" class="form-control group-name-input" required>
                </div>
                <div class="form-group" style="flex: 1; display: flex; align-items: center; gap: 10px; margin-top: 25px;">
                    <label style="margin:0; display:flex; align-items:center; gap:5px; cursor:pointer;">
                        <input type="checkbox" name="optionGroups[${groupCount}].required" value="true"> Bắt buộc
                    </label>
                    <label style="margin:0; display:flex; align-items:center; gap:5px; cursor:pointer;">
                        <input type="checkbox" name="optionGroups[${groupCount}].multiple" value="true"> Chọn nhiều
                    </label>
                </div>
            </div>
            
            <div class="option-items-container">
                <label style="font-size: 13px; font-weight: 500; color: #555; display: block; margin-bottom: 8px;">Danh sách Tùy chọn con:</label>
                <div class="items-list">
                    <!-- Items go here -->
                </div>
                <button type="button" onclick="addOptionItem(this)" style="background: none; border: 1px dashed #ccc; padding: 5px 10px; border-radius: 4px; font-size: 12px; cursor: pointer; margin-top: 5px; color: #666; width: 100%;"><i class="fa-solid fa-plus"></i> Thêm tùy chọn con</button>
            </div>
        </div>
    `;
    
    container.insertAdjacentHTML('beforeend', groupHtml);
    reindexOptions();
}

function addOptionItem(btn) {
    const itemsList = btn.previousElementSibling;
    const groupCard = btn.closest('.option-group-card');
    
    // Get current group index by searching all groups
    const allGroups = document.querySelectorAll('.option-group-card');
    const groupIndex = Array.from(allGroups).indexOf(groupCard);
    
    const itemCount = itemsList.querySelectorAll('.option-item-row').length;
    
    const itemHtml = `
        <div class="option-item-row" style="display: flex; gap: 10px; margin-bottom: 8px; align-items: center;">
            <input type="hidden" name="optionGroups[${groupIndex}].optionItems[${itemCount}].itemId" value="">
            <input type="text" name="optionGroups[${groupIndex}].optionItems[${itemCount}].itemName" class="form-control item-name-input" placeholder="Tên (VD: Size L)" required style="flex: 2; padding: 6px 12px;">
            <div class="price-input-wrapper" style="flex: 1; margin: 0;">
                <input type="number" name="optionGroups[${groupIndex}].optionItems[${itemCount}].extraPrice" class="form-control item-price-input" placeholder="Giá cộng thêm" value="0" style="padding: 6px 12px;" required>
                <span class="price-suffix" style="padding: 6px;">₫</span>
            </div>
            <button type="button" onclick="this.closest('.option-item-row').remove(); reindexOptions();" style="background: none; border: none; color: #dc3545; cursor: pointer; padding: 5px;"><i class="fa-solid fa-times"></i></button>
        </div>
    `;
    
    itemsList.insertAdjacentHTML('beforeend', itemHtml);
    reindexOptions();
}

function reindexOptions() {
    const groups = document.querySelectorAll('.option-group-card');
    groups.forEach((group, gIndex) => {
        // Reindex Group Inputs
        const groupId = group.querySelector('input[name$=".groupId"]');
        if (groupId) groupId.name = `optionGroups[${gIndex}].groupId`;
        
        const groupName = group.querySelector('.group-name-input');
        if (groupName) groupName.name = `optionGroups[${gIndex}].groupName`;
        
        const reqCheck = group.querySelector('input[name$=".required"]');
        if (reqCheck) reqCheck.name = `optionGroups[${gIndex}].required`;
        
        const mulCheck = group.querySelector('input[name$=".multiple"]');
        if (mulCheck) mulCheck.name = `optionGroups[${gIndex}].multiple`;
        
        // Reindex Item Inputs
        const items = group.querySelectorAll('.option-item-row');
        items.forEach((item, iIndex) => {
            const itemId = item.querySelector('input[name*=".itemId"]');
            if (itemId) itemId.name = `optionGroups[${gIndex}].optionItems[${iIndex}].itemId`;
            
            const itemName = item.querySelector('.item-name-input');
            if (itemName) itemName.name = `optionGroups[${gIndex}].optionItems[${iIndex}].itemName`;
            
            const extraPrice = item.querySelector('.item-price-input');
            if (extraPrice) extraPrice.name = `optionGroups[${gIndex}].optionItems[${iIndex}].extraPrice`;
        });
    });
}

