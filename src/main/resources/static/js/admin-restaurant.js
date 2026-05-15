/**
 * Admin Restaurant Management Scripts
 * Handles: Image preview on upload
 */
document.addEventListener('DOMContentLoaded', function () {

    // Image Upload Preview
    const imageFile = document.getElementById('imageFile');
    const uploadArea = document.getElementById('imageUploadArea');

    if (imageFile && uploadArea) {
        imageFile.addEventListener('change', function (e) {
            const file = e.target.files[0];
            if (file && file.type.startsWith('image/')) {
                const reader = new FileReader();
                reader.onload = function (event) {
                    uploadArea.innerHTML = `
                        <div class="image-preview-wrapper">
                            <img src="${event.target.result}" class="image-preview" alt="Preview">
                            <p class="image-hint">Click to change image</p>
                        </div>
                        <input type="file" name="imageFile" id="imageFile" accept="image/*" class="file-input-hidden">
                    `;
                    // Re-bind the event since we replaced innerHTML
                    document.getElementById('imageFile').addEventListener('change', arguments.callee);
                };
                reader.readAsDataURL(file);
            }
        });
    }

});
