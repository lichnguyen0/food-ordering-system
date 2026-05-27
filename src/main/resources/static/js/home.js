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
    const heroForm = document.getElementById('heroSearchForm');
    const heroLatInput = document.getElementById('heroUserLat');
    const heroLngInput = document.getElementById('heroUserLng');

// Helper: Calculate delivery time based on distance (matches backend DistanceService)
     function calculateDeliveryTime(distanceKm) {
         if (distanceKm <= 0) return 15;
         return 15 + Math.round(distanceKm * 3);
     }

     // Helper: Vietnamese city center coordinates for fallback
     const VIETNAM_CITIES = [
         { name: 'Hà Nội', lat: 21.0278, lng: 105.8342, threshold: 0.5 },
         { name: 'TP.HCM', lat: 10.8231, lng: 106.6297, threshold: 0.5 },
         { name: 'Đà Nẵng', lat: 16.0544, lng: 108.2022, threshold: 0.3 },
         { name: 'Hải Phòng', lat: 20.8448, lng: 106.3711, threshold: 0.3 },
         { name: 'Cần Thơ', lat: 10.0333, lng: 105.7833, threshold: 0.3 }
     ];

     function detectCityFromCoordinates(lat, lng) {
         for (const city of VIETNAM_CITIES) {
             const dist = locationService.calculateHaversineDistance(lat, lng, city.lat, city.lng);
             if (dist <= city.threshold) {
                 return city.name;
             }
         }
         return null;
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

// Helper: Format short address for display (street, ward, city)
     function formatShortAddress(reverseResult) {
         if (!reverseResult || !reverseResult.address) return null;
         const addr = reverseResult.address;
         const parts = [];
         
         if (addr.road) parts.push(addr.road);
         if (addr.neighbourhood) parts.push(addr.neighbourhood);
         if (addr.suburb) parts.push(addr.suburb);
         if (addr.city) parts.push(addr.city);
         
         return parts.length > 0 ? parts.join(', ') : null;
     }

// Helper: Extract city from reverse geocode result
      function extractCityFromAddress(reverseResult) {
          // Try from address object first (most reliable)
          if (reverseResult && reverseResult.address) {
              const addr = reverseResult.address;
              if (addr.city) return addr.city;
              if (addr.state) return addr.state;
              if (addr.town) return addr.town;
              if (addr.municipality) return addr.municipality;
              if (addr.village) return addr.village;
              if (addr.province) return addr.province;
          }
          
          if (reverseResult && reverseResult.displayName) {
              const displayName = reverseResult.displayName;
              if (displayName.includes('Hà Nội') || displayName.includes('Ha Noi')) return 'Hà Nội';
              if (displayName.includes('TP.HCM') || displayName.includes('Hồ Chí Minh') || displayName.includes('Sài Gòn')) return 'TP.HCM';
              if (displayName.includes('Đà Nẵng') || displayName.includes('Da Nang')) return 'Đà Nẵng';
          }
          return null;
      }

// Helper: Set hidden fields and update UI
      function applyUserLocation(lat, lng, source = 'current', addressLabel = null, reverseResult = null) {
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

// Update location address in hero section - display short address or full display_name
           if (reverseResult) {
               // Try short address first (road, neighbourhood, city)
               let displayAddress = formatShortAddress(reverseResult);
               // Fallback to full displayName if short address not available
               if (!displayAddress && reverseResult.displayName) {
                   // Extract just the Vietnamese part (before postal code)
                   const parts = reverseResult.displayName.split(', ');
                   // Take first N parts - usually enough for street/ward/city
                   displayAddress = parts.slice(0, -2).join(', '); // Remove postcode and country
               }
               // Fallback to city name only
               if (!displayAddress) {
                   displayAddress = extractCityFromAddress(reverseResult);
               }
               
               console.log('Display address:', displayAddress, 'from result:', reverseResult);
               if (displayAddress) {
                   const addrSpan = document.getElementById('heroLocationAddress');
                   if (addrSpan) {
                       addrSpan.textContent = displayAddress;
                   }
               }
           } else if (source !== 'current') {
               const detectedCity = detectCityFromCoordinates(lat, lng);
               if (detectedCity) {
                   const citySpan = document.getElementById('heroLocationAddress');
                   if (citySpan) citySpan.textContent = detectedCity;
               }
           }

          // Update status UI
          // Removed location status div from HTML - no longer needed

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

// Re-apply with the nice label and city info
                      console.log('Reverse geocode result:', reverse);
                      applyUserLocation(loc.lat, loc.lng, 'current', niceAddress, reverse);
                  } catch (revErr) {
                      console.warn('Reverse geocoding failed, using generic label', revErr);
                      const detectedCity = detectCityFromCoordinates(loc.lat, loc.lng);
                      if (detectedCity) {
                          const citySpan = document.getElementById('heroLocationAddress');
                          if (citySpan) citySpan.textContent = detectedCity;
                      }
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

// Handle form submit to geocode address and update city
      if (heroForm) {
          heroForm.addEventListener('submit', async (e) => {
              const addressInput = document.getElementById('deliveryAddressInput');
              const address = addressInput.value.trim();
              
              // If already has coordinates, let it submit normally
              if (heroLatInput.value && heroLngInput.value) {
                  return;
              }
              
              // Try to geocode the entered address
              if (address && address.length >= 3) {
                  try {
                      const geo = await locationService.geocodeAddress(address);
                      heroLatInput.value = geo.lat;
                      heroLngInput.value = geo.lng;
                      
// Update city display from geocode result
                       const addrSpan = document.getElementById('heroLocationAddress');
                       const shortAddr = formatShortAddress(geo);
                       if (shortAddr && addrSpan) addrSpan.textContent = shortAddr;
                   } catch (err) {
                       console.warn('Geocoding failed on submit, proceeding anyway');
                   }
              }
          });
      }

      // Auto-geocode address input with debounce for real-time city preview
      const addressInput = document.getElementById('deliveryAddressInput');
      if (addressInput) {
          let geocodeTimeout;
          addressInput.addEventListener('input', () => {
              clearTimeout(geocodeTimeout);
              const address = addressInput.value.trim();
              if (address.length >= 5) {
                  geocodeTimeout = setTimeout(async () => {
                      try {
                          const geo = await locationService.geocodeAddress(address);
                          heroLatInput.value = geo.lat;
                          heroLngInput.value = geo.lng;
                          
// Update city in hero section for preview
                           const shortAddr = formatShortAddress(geo);
                           const addrSpan = document.getElementById('heroLocationAddress');
                           if (shortAddr && addrSpan) addrSpan.textContent = shortAddr;
                          
                          // Update restaurant distances in real-time
                          updateRestaurantDistances(geo.lat, geo.lng);
                      } catch (err) {
                          // Silent fail for real-time preview
                      }
                  }, 1000); // Debounce 1 second
              }
          });
      }

    // On page load: try to restore previous location from sessionStorage
    // and apply distances if restaurants have coordinates
    const serverLat = /*[[${userLat}]]*/ null;
    const serverLng = /*[[${userLng}]]*/ null;

if (serverLat && serverLng) {
         // Try to get city info from reverse geocode
         locationService.reverseGeocode(serverLat, serverLng).then(reverse => {
             applyUserLocation(serverLat, serverLng, 'server', reverse.displayName, reverse);
         }).catch(() => {
             const detectedCity = detectCityFromCoordinates(serverLat, serverLng);
             if (detectedCity) {
                 const addrSpan = document.getElementById('heroLocationAddress');
                 if (addrSpan) addrSpan.textContent = detectedCity;
             }
             applyUserLocation(serverLat, serverLng, 'server');
         });
     } else {
         const stored = locationService.getStoredLocation();
         if (stored) {
             locationService.reverseGeocode(stored.lat, stored.lng).then(reverse => {
                 applyUserLocation(stored.lat, stored.lng, 'stored', reverse.displayName, reverse);
             }).catch(() => {
                 const detectedCity = detectCityFromCoordinates(stored.lat, stored.lng);
                 if (detectedCity) {
                     const addrSpan = document.getElementById('heroLocationAddress');
                     if (addrSpan) addrSpan.textContent = detectedCity;
                 }
                 applyUserLocation(stored.lat, stored.lng, 'stored');
             });
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
