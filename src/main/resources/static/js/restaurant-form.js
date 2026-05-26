document.addEventListener('DOMContentLoaded', function() {
    // === Existing image preview logic ===
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

    // === Promo toggle ===
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

        togglePromoDetails();
        promoToggle.addEventListener('change', togglePromoDetails);
    }

    // === Vietnam Provinces Open API v2 (2026 post-merger data) ===
    const provinceSelect = document.getElementById('provinceSelect');
    const wardSelect = document.getElementById('wardSelect');
    const streetDetailInput = document.getElementById('streetDetailInput');
    const fullAddressInput = document.getElementById('fullAddressInput');
    const latitudeInput = document.getElementById('latitudeInput');
    const longitudeInput = document.getElementById('longitudeInput');
    const geoStatus = document.getElementById('geoStatus');

    const API_V2_BASE = 'https://provinces.open-api.vn/api/v2';

    let provincesCache = []; // Cache the list of 34 provinces

    if (!provinceSelect || !wardSelect) {
        return;
    }

    // Load all provinces from v2 API (34 provinces/cities after 2025-2026 merger)
    async function loadAdministrativeData() {
        try {
            const res = await fetch(`${API_V2_BASE}/p/`);
            if (!res.ok) throw new Error('Failed to load provinces from API v2');

            provincesCache = await res.json();

            // Sắp xếp: Thành phố trung ương lên trước, sau đó mới đến Tỉnh (theo alphabet)
            provincesCache.sort((a, b) => {
                const aIsCity = a.division_type === 'thành phố trung ương' ? 0 : 1;
                const bIsCity = b.division_type === 'thành phố trung ương' ? 0 : 1;
                
                if (aIsCity !== bIsCity) {
                    return aIsCity - bIsCity;
                }
                
                // Cùng loại thì sort theo tên (tiếng Việt)
                return a.name.localeCompare(b.name, 'vi');
            });

            // Populate provinces
            provinceSelect.innerHTML = '<option value="">-- Chọn Tỉnh/TP --</option>';
            provincesCache.forEach(p => {
                const opt = document.createElement('option');
                opt.value = p.code;
                opt.textContent = p.name;
                provinceSelect.appendChild(opt);
            });

            console.log('✅ Loaded', provincesCache.length, 'provinces from Vietnam Provinces API v2 (sorted)');
        } catch (e) {
            console.error('Error loading provinces from v2 API:', e);
            alert('Không thể tải danh sách tỉnh/thành từ API. Vui lòng thử lại sau.');
        }
    }

    // Load wards for selected province using v2 API with depth=2
    async function loadWards(provinceCode) {
        wardSelect.innerHTML = '<option value="">-- Chọn Phường/Xã --</option>';
        wardSelect.disabled = true;

        if (!provinceCode) return;

        try {
            const res = await fetch(`${API_V2_BASE}/p/${provinceCode}?depth=2`);
            if (!res.ok) throw new Error('Failed to load wards');

            const data = await res.json();

            // Sắp xếp: Phường trước, Xã sau (theo alphabet trong mỗi nhóm)
            if (data.wards && data.wards.length > 0) {
                data.wards.sort((a, b) => {
                    const aIsPhuong = a.division_type === 'phường' ? 0 : 1;
                    const bIsPhuong = b.division_type === 'phường' ? 0 : 1;
                    
                    if (aIsPhuong !== bIsPhuong) {
                        return aIsPhuong - bIsPhuong;
                    }
                    
                    return a.name.localeCompare(b.name, 'vi');
                });

                data.wards.forEach(w => {
                    const opt = document.createElement('option');
                    opt.value = w.code;
                    opt.textContent = w.name;
                    wardSelect.appendChild(opt);
                });
                wardSelect.disabled = false;
                console.log('Loaded', data.wards.length, 'wards for', data.name, '(sorted: Phường trước, Xã sau)');
            } else {
                console.warn('No wards found for province code', provinceCode);
                wardSelect.disabled = false;
            }
        } catch (e) {
            console.error('Error loading wards from v2 API:', e);
            alert('Không thể tải danh sách phường/xã. Vui lòng thử lại.');
        }
    }

    // Construct full address and trigger geocoding (2026 structure: Province + Ward)
    async function updateFullAddressAndGeocode() {
        const provinceText = provinceSelect.options[provinceSelect.selectedIndex]?.text || '';
        const wardText = wardSelect.options[wardSelect.selectedIndex]?.text || '';
        const street = streetDetailInput.value.trim();

        // 2026 post-merger format: Street, Ward, Province
        let fullAddress = [street, wardText, provinceText]
            .filter(Boolean)
            .join(', ');

        if (fullAddressInput) {
            fullAddressInput.value = fullAddress;
        }

        // Geocode when we have province + ward
        if (provinceText && wardText) {
            await geocodeAddress(fullAddress);
        }
    }

    // Call Nominatim to get lat/lng
    async function geocodeAddress(address) {
        if (!address) return;

        const encoded = encodeURIComponent(address);
        const url = `https://nominatim.openstreetmap.org/search?format=json&q=${encoded}&limit=1&addressdetails=1&countrycodes=vn`;

        try {
            const res = await fetch(url, {
                headers: { 'User-Agent': 'LunoFood-Admin/1.0' }
            });
            const results = await res.json();

            if (results.length > 0) {
                const loc = results[0];
                if (latitudeInput) latitudeInput.value = parseFloat(loc.lat).toFixed(6);
                if (longitudeInput) longitudeInput.value = parseFloat(loc.lon).toFixed(6);

                if (geoStatus) {
                    geoStatus.style.display = 'block';
                    geoStatus.innerHTML = `<i class="fa-solid fa-check-circle"></i> Đã lấy tọa độ tự động (${parseFloat(loc.lat).toFixed(4)}, ${parseFloat(loc.lon).toFixed(4)})`;
                }
            }
        } catch (err) {
            console.warn('Geocoding failed for admin form:', err);
        }
    }

    // Event listeners for cascading (2026 structure - Province → Ward directly)
    provinceSelect.addEventListener('change', () => {
        loadWards(provinceSelect.value);
        updateFullAddressAndGeocode();
    });

    wardSelect.addEventListener('change', updateFullAddressAndGeocode);
    streetDetailInput.addEventListener('input', updateFullAddressAndGeocode);

    // Manual geocode button
    const manualBtn = document.getElementById('manualGeocodeBtn');
    if (manualBtn) {
        manualBtn.addEventListener('click', async () => {
            const addr = fullAddressInput?.value;
            if (addr) {
                await geocodeAddress(addr);
            } else {
                alert('Vui lòng chọn Tỉnh/Thành và Phường/Xã trước khi lấy tọa độ.');
            }
        });
    }

    // Initial load from Vietnam Provinces API v2
    loadAdministrativeData();

    // If editing and lat/lng already exist, show status
    if (latitudeInput && longitudeInput && geoStatus) {
        const lat = parseFloat(latitudeInput.value);
        const lng = parseFloat(longitudeInput.value);
        if (!isNaN(lat) && !isNaN(lng)) {
            geoStatus.style.display = 'block';
            geoStatus.innerHTML = `<i class="fa-solid fa-check-circle"></i> Tọa độ hiện tại: ${lat.toFixed(4)}, ${lng.toFixed(4)}`;
        }
    }
});