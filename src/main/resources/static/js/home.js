document.addEventListener("DOMContentLoaded", () => {
    const header = document.getElementById("mainHeader");
    const searchContainer = document.getElementById("headerSearch");

    // Scroll Effect for Header
    window.addEventListener("scroll", () => {
        if (window.scrollY > 50) {
            header.classList.remove("is-home-top");
            if (searchContainer) searchContainer.style.opacity = "1";
            if (searchContainer) searchContainer.style.visibility = "visible";
        } else {
            header.classList.add("is-home-top");
            if (searchContainer) searchContainer.style.opacity = "0";
            if (searchContainer) searchContainer.style.visibility = "hidden";
        }
    });

    // Initialize state
    if (window.scrollY <= 50) {
        header.classList.add("is-home-top");
        if (searchContainer) searchContainer.style.opacity = "0";
        if (searchContainer) searchContainer.style.visibility = "hidden";
    }

    // Carousel Logic
    const carousel = document.getElementById("promoCarousel");
    const prevBtn = document.getElementById("prevBtn");
    const nextBtn = document.getElementById("nextBtn");

    if (carousel && prevBtn && nextBtn) {
        const scrollAmount = 300; // Khoảng cách cuộn mỗi lần click

        prevBtn.addEventListener("click", () => {
            carousel.scrollBy({
                left: -scrollAmount,
                behavior: "smooth"
            });
        });

        nextBtn.addEventListener("click", () => {
            carousel.scrollBy({
                left: scrollAmount,
                behavior: "smooth"
            });
        });
    }

    // ============================================
    // LOCATION DETECTION & REAL DISTANCE FEATURE
    // ============================================
    const locationService = new LocationService();
    let currentUserLocation = null;

    const detectBtn = document.getElementById('detectLocationBtn');
    const locationStatus = document.getElementById('locationStatus');
    const locationStatusText = document.getElementById('locationStatusText');
    const heroForm = document.getElementById('heroSearchForm');
    const heroLatInput = document.getElementById('heroUserLat');
    const heroLngInput = document.getElementById('heroUserLng');

// Helper: Calculate delivery time based on distance (matches backend DistanceService)
     function calculateDeliveryTime(distanceKm) {
         if (distanceKm <= 0) return 15;
         return 15 + Math.round(distanceKm * 5);
     }

     // Helper: Update all restaurant distance displays
     function updateRestaurantDistances(userLat, userLng) {
         const cards = document.querySelectorAll('.promo-card-link[data-lat][data-lng]');
         
         cards.forEach(card => {
             const restLat = parseFloat(card.dataset.lat);
             const restLng = parseFloat(card.dataset.lng);
             const distanceSpan = card.querySelector('.restaurant-distance');
             const timeSpan = card.querySelector('.promo-meta span:nth-child(2) span');
             
             if (!isNaN(restLat) && !isNaN(restLng) && distanceSpan) {
                 const distKm = locationService.calculateHaversineDistance(userLat, userLng, restLat, restLng);
                 distanceSpan.textContent = locationService.formatDistance(distKm);
                 
                 // Update delivery time based on calculated distance
                 if (timeSpan) {
                     const deliveryTime = calculateDeliveryTime(distKm);
                     timeSpan.textContent = deliveryTime + ' phút';
                 }
                 
                 // Also update the href to include current user location for restaurant detail page
                 const baseHref = card.getAttribute('href').split('?')[0];
                 const newHref = `${baseHref}?userLat=${userLat}&userLng=${userLng}`;
                 card.setAttribute('href', newHref);
             }
         });
     }

    // Helper: Set hidden fields and update UI
    function applyUserLocation(lat, lng, source = 'current', addressLabel = null) {
        currentUserLocation = { lat, lng };
        
        // Update hidden fields in hero form
        if (heroLatInput) heroLatInput.value = lat;
        if (heroLngInput) heroLngInput.value = lng;

        // Populate the visible address input so the form can submit (fixes required field issue)
        const addressInput = document.getElementById('deliveryAddressInput');
        if (addressInput) {
            if (addressLabel) {
                addressInput.value = addressLabel;
            } else {
                // Temporary label until reverse geocode finishes
                addressInput.value = 'Vị trí hiện tại';
            }
        }

        // Update status UI
        if (locationStatus) locationStatus.style.display = 'block';
        if (locationStatusText) {
            locationStatusText.textContent = source === 'current' 
                ? 'Đang dùng vị trí hiện tại của bạn' 
                : 'Đã đặt vị trí giao hàng';
        }

        // Update all visible restaurant distances
        updateRestaurantDistances(lat, lng);

        // Store for other pages in this session
        locationService.storeLocation(lat, lng);

        // Also try to update the header search if present
        const headerSearchInput = document.querySelector('#headerSearch input[name="keyword"]');
        if (headerSearchInput && !headerSearchInput.value) {
            // Optional: could prefill with "Vị trí hiện tại" but keep simple
        }
    }

    // Detect location button
    if (detectBtn) {
        detectBtn.addEventListener('click', async () => {
            detectBtn.disabled = true;
            detectBtn.innerHTML = '<i class="fa-solid fa-spinner fa-spin"></i>';

            try {
                const loc = await locationService.detectUserLocation();
                
                // Apply basic location first (so distances update immediately)
                applyUserLocation(loc.lat, loc.lng, 'current');

                // Try to get a nice readable address via reverse geocoding
                try {
                    const reverse = await locationService.reverseGeocode(loc.lat, loc.lng);
                    const niceAddress = reverse.displayName || 'Vị trí hiện tại';

                    // Update the address input with real address
                    const addressInput = document.getElementById('deliveryAddressInput');
                    if (addressInput) {
                        addressInput.value = niceAddress;
                    }

                    // Re-apply with the nice label
                    applyUserLocation(loc.lat, loc.lng, 'current', niceAddress);
                } catch (revErr) {
                    console.warn('Reverse geocoding failed, using generic label');
                }
                
                // Show success feedback
                detectBtn.innerHTML = '<i class="fa-solid fa-check"></i>';
                detectBtn.style.color = '#22c55e';
                
                setTimeout(() => {
                    detectBtn.innerHTML = '<i class="fa-solid fa-crosshairs"></i>';
                    detectBtn.style.color = '';
                    detectBtn.disabled = false;
                }, 1500);

            } catch (err) {
                console.warn('Location detection failed:', err.message);
                alert('Không thể lấy vị trí của bạn. Vui lòng cho phép truy cập vị trí hoặc nhập địa chỉ thủ công.');
                detectBtn.innerHTML = '<i class="fa-solid fa-crosshairs"></i>';
                detectBtn.disabled = false;
            }
        });
    }

    // On page load: try to restore previous location from sessionStorage
    // and apply distances if restaurants have coordinates
    const serverLat = /*[[${userLat}]]*/ null;
    const serverLng = /*[[${userLng}]]*/ null;

    if (serverLat && serverLng) {
        applyUserLocation(serverLat, serverLng, 'server');
    } else {
        // Try to restore from sessionStorage silently
        const stored = locationService.getStoredLocation();
        if (stored) {
            applyUserLocation(stored.lat, stored.lng, 'stored');
            // Optionally reverse geocode in background to improve the address label
            locationService.reverseGeocode(stored.lat, stored.lng).then(reverse => {
                const addressInput = document.getElementById('deliveryAddressInput');
                if (addressInput && (!addressInput.value || addressInput.value === 'Vị trí hiện tại')) {
                    addressInput.value = reverse.displayName;
                }
            }).catch(() => {});
        }
    }

    // Also expose globally for other scripts if needed
    window.LunoFoodLocation = {
        getCurrent: () => currentUserLocation,
        detect: () => locationService.detectUserLocation()
    };
});
