document.addEventListener('DOMContentLoaded', function () {

    // Toggle Status
    var toggleBtn = document.getElementById('statusToggle');
    if (toggleBtn) {
        toggleBtn.addEventListener('change', function() {
            var isAvailable = this.checked;
            var label = document.getElementById('statusLabel');

            fetch('/shipper/api/toggle-status', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' }
            })
            .then(function(response) {
                if (!response.ok) throw new Error('Network response was not ok');
                return response.json();
            })
            .then(function(data) {
                if(data.success) {
                    label.textContent = data.available ? 'Đang trực' : 'Nghỉ ngơi';
                    showToast('Đã cập nhật trạng thái làm việc!');
                } else {
                    throw new Error(data.message);
                }
            })
            .catch(function(error) {
                console.error('Error toggling status:', error);
                toggleBtn.checked = !isAvailable; // revert
                showToast('Không thể cập nhật trạng thái!', false);
            });
        });
    }

    // Modal Logic
    let cancelOrderId = null;

    window.openFailModal = function(orderId) {
        cancelOrderId = orderId;
        document.getElementById('cancel-order-id-display').textContent = '#' + orderId;
        document.getElementById('cancel-form').action = '/shipper/order/' + orderId + '/fail';
        document.getElementById('cancel-reason-select').value = 'Không liên lạc được khách';
        document.getElementById('cancel-reason-custom').style.display = 'none';
        document.getElementById('cancel-reason-custom').value = 'Không liên lạc được khách';
        document.getElementById('cancel-modal').style.display = 'block';
    };

    window.closeFailModal = function() {
        document.getElementById('cancel-modal').style.display = 'none';
        cancelOrderId = null;
    };

    var selectEl = document.getElementById('cancel-reason-select');
    if (selectEl) {
        selectEl.addEventListener('change', function() {
            var customInput = document.getElementById('cancel-reason-custom');
            if (this.value === 'other') {
                customInput.style.display = 'block';
                customInput.value = '';
                customInput.focus();
            } else {
                customInput.style.display = 'none';
                customInput.value = this.value;
            }
        });
        document.getElementById('cancel-reason-custom').value = selectEl.value;
    }

    var modalOverlay = document.getElementById('cancel-modal');
    if (modalOverlay) {
        modalOverlay.addEventListener('click', function (e) {
            if (e.target === this) window.closeFailModal();
        });
    }

    // Deliver modal logic
    var deliverModalEl = document.getElementById('deliver-modal');
    if (deliverModalEl) {
        deliverModalEl.addEventListener('click', function (e) {
            if (e.target === this) closeDeliverModal();
        });
    }

    // Cancel reason custom input behavior already set above

    // Initialize shipper map if present
    const shipperMapEl = document.getElementById('shipper-map');
    if (shipperMapEl && typeof L !== 'undefined') {
        // create map and marker variables in outer scope
        try {
            window._shipperMap = L.map(shipperMapEl, { zoomControl: true }).setView([21.0285, 105.8542], 13);
            L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png').addTo(window._shipperMap);

            // Ensure map container has low z-index so buttons below are clickable
            shipperMapEl.style.position = shipperMapEl.style.position || 'relative';
            shipperMapEl.style.zIndex = '0';

            window._shipperMarker = null;

            // Try to use stored location
            if (typeof LocationService !== 'undefined') {
                const locService = new LocationService();
                const stored = locService.getStoredLocation();
                if (stored) {
                    window._shipperMap.setView([stored.lat, stored.lng], 15);
                    window._shipperMarker = L.marker([stored.lat, stored.lng]).addTo(window._shipperMap).bindPopup('Bạn ở đây').openPopup();
                }

                // center button
                const centerBtn = document.getElementById('centerOnMeBtn');
                if (centerBtn) {
                    centerBtn.addEventListener('click', async () => {
                        try {
                            const pos = await locService.detectUserLocation();
                            if (window._shipperMarker) window._shipperMarker.setLatLng([pos.lat, pos.lng]);
                            else window._shipperMarker = L.marker([pos.lat, pos.lng]).addTo(window._shipperMap).bindPopup('Bạn ở đây').openPopup();
                            window._shipperMap.setView([pos.lat, pos.lng], 15);
                        } catch (e) {
                            alert('Không thể lấy vị trí. Vui lòng cho phép truy cập vị trí.');
                        }
                    });
                }
            }

            // Fix rendering issue when map container was hidden or not sized when leaflet initialized
            setTimeout(() => {
                try { window._shipperMap.invalidateSize(); } catch (e) {}
            }, 200);

            // Add route drawing support and order-card click handlers
            window._shipperRouteLayer = null;

            function clearRoute() {
                if (window._shipperRouteLayer) {
                    try { window._shipperMap.removeLayer(window._shipperRouteLayer); } catch (e) {}
                    window._shipperRouteLayer = null;
                }
            }

            async function drawRoute(fromLat, fromLng, toLat, toLng) {
                clearRoute();
                if (!fromLat || !fromLng || !toLat || !toLng) return;

                const osrmUrl = `https://router.project-osrm.org/route/v1/driving/${fromLng},${fromLat};${toLng},${toLat}?overview=full&geometries=geojson`;
                try {
                    const res = await fetch(osrmUrl);
                    const data = await res.json();
                    if (data && data.code === 'Ok' && data.routes && data.routes.length > 0) {
                        const route = data.routes[0];
                        const geo = route.geometry;
                        window._shipperRouteLayer = L.geoJSON(geo, {
                            style: { color: '#06b6d4', weight: 4, opacity: 0.9 }
                        }).addTo(window._shipperMap);

                        const bounds = window._shipperRouteLayer.getBounds();
                        window._shipperMap.fitBounds(bounds.pad(0.2));
                    } else {
                        // fallback: straight polyline
                        const poly = L.polyline([[fromLat, fromLng],[toLat,toLng]], { color: '#06b6d4', weight: 3, dashArray: '6 6' }).addTo(window._shipperMap);
                        window._shipperRouteLayer = poly;
                        window._shipperMap.fitBounds(poly.getBounds().pad(0.2));
                    }
                } catch (e) {
                    console.warn('OSRM routing failed, drawing straight line', e);
                    const poly = L.polyline([[fromLat, fromLng],[toLat,toLng]], { color: '#06b6d4', weight: 3, dashArray: '6 6' }).addTo(window._shipperMap);
                    window._shipperRouteLayer = poly;
                    window._shipperMap.fitBounds(poly.getBounds().pad(0.2));
                }
            }

            // Attach click handlers to order cards to show route
            const orderCards = document.querySelectorAll('.order-card[data-dest-lat][data-dest-lng]');
            orderCards.forEach(card => {
                // prevent buttons inside card from triggering navigation
                const buttons = card.querySelectorAll('button');
                buttons.forEach(b => b.addEventListener('click', e => e.stopPropagation()));

                card.addEventListener('click', async () => {
                    const destLat = parseFloat(card.dataset.destLat);
                    const destLng = parseFloat(card.dataset.destLng);

                    // get current shipper location
                    let fromLat = null, fromLng = null;
                    if (window._shipperMarker) {
                        const latlng = window._shipperMarker.getLatLng();
                        fromLat = latlng.lat; fromLng = latlng.lng;
                    } else if (typeof LocationService !== 'undefined') {
                        try {
                            const loc = await (new LocationService()).getCurrentPosition();
                            fromLat = loc.lat; fromLng = loc.lng;
                        } catch (e) {}
                    }

                    if (!fromLat || !fromLng) {
                        alert('Không xác định được vị trí hiện tại của bạn. Vui lòng bật vị trí.');
                        return;
                    }

                    // highlight selected card
                    document.querySelectorAll('.order-card.selected').forEach(c => c.classList.remove('selected'));
                    card.classList.add('selected');

                    drawRoute(fromLat, fromLng, destLat, destLng);
                });
            });
        } catch (err) {
            console.error('Failed to initialize shipper map', err);
        }
    }

});

function showToast(message, isSuccess = true) {
    var toast = document.getElementById('toast');
    if(!toast) return;
    var messageEl = document.getElementById('toast-message');

    toast.style.borderColor = isSuccess ? 'var(--primary)' : 'var(--danger)';
    toast.querySelector('i').className = isSuccess ? 'fa-solid fa-circle-check' : 'fa-solid fa-circle-xmark';
    toast.querySelector('i').style.color = isSuccess ? 'var(--primary)' : 'var(--danger)';

    messageEl.textContent = message;
    toast.classList.add('show');

    setTimeout(function () {
        toast.classList.remove('show');
    }, 3000);
}

// Open/close deliver modal (global so inline onclick can call it)
// Expose modal functions on window to ensure inline onclick works
window.openDeliverModal = function(orderId) {
    var el = document.getElementById('deliver-modal');
    if (!el) return;
    document.getElementById('deliver-order-id-display').textContent = '#' + orderId;
    var form = document.getElementById('deliver-form');
    if (form) form.action = '/shipper/order/' + orderId + '/delivered';
    el.style.display = 'block';
};

window.closeDeliverModal = function() {
    var el = document.getElementById('deliver-modal');
    if (!el) return;
    el.style.display = 'none';
};

// Open/close fail modal
window.openFailModal = function(orderId) {
    var el = document.getElementById('cancel-modal');
    if (!el) return;
    document.getElementById('cancel-order-id-display').textContent = '#' + orderId;
    document.getElementById('cancel-form').action = '/shipper/order/' + orderId + '/fail';
    el.style.display = 'block';
};

window.closeFailModal = function() {
    var el = document.getElementById('cancel-modal');
    if (!el) return;
    el.style.display = 'none';
};

