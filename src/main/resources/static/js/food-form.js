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
});
