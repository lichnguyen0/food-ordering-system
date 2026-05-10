document.addEventListener("DOMContentLoaded", () => {
    const foodGridContainer = document.querySelector(".food-grid-container");
    const searchInput = document.querySelector(".grid-search-input");
    const entriesSelect = document.querySelector(".entries-select");
    const paginationControls = document.getElementById("pagination-controls");
    const pageInfoStart = document.getElementById("page-info-start");
    const pageInfoEnd = document.getElementById("page-info-end");
    const pageInfoTotal = document.getElementById("page-info-total");

    let currentPage = 1;
    let itemsPerPage = parseInt(entriesSelect.value);
    let allFoods = []; // This will be populated from the Thymeleaf model initially

    // Assume `foods` data is already available globally from Thymeleaf, or fetch it via AJAX
    // For this example, we'll assume `initialFoods` is passed from Thymeleaf
    // In a real app, you might fetch it with:
    // fetch('/api/admin/foods').then(response => response.json()).then(data => { allFoods = data; renderGrid(); });

    // For demonstration, let's mock some data if not coming from Thymeleaf directly
    if (typeof initialFoods === 'undefined') {
        // This part would ideally be populated by Thymeleaf in food-grid.html
        // Example: <script th:inline="javascript"> var initialFoods = [[${foods}]]; </script>
        allFoods = Array.from(foodGridContainer.children).map(card => {
            // Extract data from existing cards if they are rendered by Thymeleaf initially
            return {
                foodId: card.querySelector(".action-buttons a.edit").href.split('/').pop(),
                foodName: card.querySelector(".food-name").textContent,
                categoryName: card.querySelector(".food-category").textContent,
                price: parseFloat(card.querySelector(".price-tag").textContent.replace('$', '')),
                status: card.querySelector(".food-badge").textContent.includes('In Stock') ? 'AVAILABLE' : 'OUT_OF_STOCK',
                image: card.querySelector(".food-image-wrapper img").src
            };
        });
    } else {
        allFoods = initialFoods;
    }

    function renderGrid() {
        foodGridContainer.innerHTML = ''; // Clear current grid
        const filteredFoods = filterFoods(allFoods);
        const totalPages = Math.ceil(filteredFoods.length / itemsPerPage);
        const start = (currentPage - 1) * itemsPerPage;
        const end = start + itemsPerPage;
        const foodsToDisplay = filteredFoods.slice(start, end);

        foodsToDisplay.forEach(food => {
            const foodCard = document.createElement("div");
            foodCard.classList.add("food-card");

            const statusClass = food.status === 'AVAILABLE' ? 'instock' : 'outstock';
            const statusText = food.status === 'AVAILABLE' ? 'In Stock' : 'Out Of Stock';
            const imageUrl = food.image && food.image !== 'null' ? food.image : '/images/placeholder.png';
            const formattedPrice = '$' + (food.price || 45.50).toFixed(2);

            foodCard.innerHTML = `
                <div class="food-image-wrapper">
                    <img src="${imageUrl}" alt="Food Image">
                </div>
                <div class="food-content">
                    <div class="food-info-top">
                        <h3 class="food-name">${food.foodName}</h3>
                        <span class="price-tag">${formattedPrice}</span>
                    </div>
                    <div class="food-info-mid">
                        <span class="food-qty">Qty${food.foodId || '1467'}</span>
                        <span class="food-badge ${statusClass}">${statusText}</span>
                    </div>
                    <p class="food-desc">Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.</p>
                    <div class="action-buttons">
                        <a href="/admin/foods/delete/${food.foodId}" 
                           class="action-btn-grid delete"
                           onclick="return confirm('Are you sure you want to delete this item?');">Remove</a>
                        <a href="/admin/foods/edit/${food.foodId}" class="action-btn-grid edit">Edit</a>
                    </div>
                </div>
            `;
            foodGridContainer.appendChild(foodCard);
        });

        updatePaginationControls(totalPages, filteredFoods.length);
        updatePageInfo(filteredFoods.length);
    }

    function filterFoods(foods) {
        const searchTerm = searchInput.value.toLowerCase();
        return foods.filter(food =>
            food.foodName.toLowerCase().includes(searchTerm) ||
            food.category.categoryName.toLowerCase().includes(searchTerm) // Assuming category is an object with a name
        );
    }

    function updatePaginationControls(totalPages, totalItems) {
        paginationControls.innerHTML = '';

        if (totalItems === 0) return; // No pagination if no items

        const createButton = (text, page, isDisabled) => {
            const button = document.createElement("button");
            button.textContent = text;
            button.classList.add("page-btn");
            if (page === currentPage) {
                button.classList.add("active");
            }
            if (isDisabled) {
                button.disabled = true;
                button.classList.add("disabled");
            }
            button.addEventListener("click", () => {
                if (!isDisabled) {
                    currentPage = page;
                    renderGrid();
                }
            });
            return button;
        };

        // Previous button
        const prevButton = createButton("Previous", currentPage - 1, currentPage === 1);
        paginationControls.appendChild(prevButton);

        // Page numbers
        for (let i = 1; i <= totalPages; i++) {
            paginationControls.appendChild(createButton(i, i, false));
        }

        // Next button
        const nextButton = createButton("Next", currentPage + 1, currentPage === totalPages);
        paginationControls.appendChild(nextButton);
    }

    function updatePageInfo(totalFilteredItems) {
        const start = (currentPage - 1) * itemsPerPage + 1;
        const end = Math.min(currentPage * itemsPerPage, totalFilteredItems);
        pageInfoStart.textContent = totalFilteredItems > 0 ? start : 0;
        pageInfoEnd.textContent = end;
        pageInfoTotal.textContent = totalFilteredItems;
    }

    // Event Listeners
    searchInput.addEventListener("keyup", () => { currentPage = 1; renderGrid(); });
    entriesSelect.addEventListener("change", (e) => {
        itemsPerPage = parseInt(e.target.value);
        currentPage = 1;
        renderGrid();
    });

    // Initial render
    // In a real Thymeleaf app, `initialFoods` would be defined in the HTML like:
    // <script th:inline="javascript"> var initialFoods = [[${foods}]]; </script>
    // This ensures allFoods is populated correctly from the backend.
    // For now, let's assume `foods` is available globally from Thymeleaf for initial load
    if (typeof foods !== 'undefined') {
        allFoods = foods;
    } else {
        console.warn("initialFoods or foods variable not found. Mocking data for demonstration.");
        // Fallback for local testing if Thymeleaf doesn't provide initialFoods
        // This mock data should match the structure expected by renderGrid
        allFoods = [
            { foodId: 1, foodName: 'French Fries', category: { categoryName: 'Sides' }, price: 47, status: 'AVAILABLE', image: 'https://images.unsplash.com/photo-1513104890138-7c749659a591?q=80&w=2070&auto=format&fit=crop' },
            { foodId: 2, foodName: 'Pizza Margherita', category: { categoryName: 'Main Course' }, price: 120, status: 'AVAILABLE', image: 'https://images.unsplash.com/photo-1513104890138-7c749659a591?q=80&w=2070&auto=format&fit=crop' },
            { foodId: 3, foodName: 'Hamburger', category: { categoryName: 'Main Course' }, price: 95, status: 'OUT_OF_STOCK', image: 'https://images.unsplash.com/photo-1513104890138-7c749659a591?q=80&w=2070&auto=format&fit=crop' },
            { foodId: 4, foodName: 'Ceasar Salad', category: { categoryName: 'Salads' }, price: 60, status: 'AVAILABLE', image: 'https://images.unsplash.com/photo-1513104890138-7c749659a591?q=80&w=2070&auto=format&fit=crop' },
            { foodId: 5, foodName: 'Sushi Combo', category: { categoryName: 'Japanese' }, price: 180, status: 'AVAILABLE', image: 'https://images.unsplash.com/photo-1513104890138-7c749659a591?q=80&w=2070&auto=format&fit=crop' },
            { foodId: 6, foodName: 'Taco Platter', category: { categoryName: 'Mexican' }, price: 110, status: 'AVAILABLE', image: 'https://images.unsplash.com/photo-1513104890138-7c749659a591?q=80&w=2070&auto=format&fit=crop' },
            { foodId: 7, foodName: 'Pasta Carbonara', category: { categoryName: 'Italian' }, price: 130, status: 'OUT_OF_STOCK', image: 'https://images.unsplash.com/photo-1513104890138-7c749659a591?q=80&w=2070&auto=format&fit=crop' },
            { foodId: 8, foodName: 'Chicken Wings', category: { categoryName: 'Appetizers' }, price: 75, status: 'AVAILABLE', image: 'https://images.unsplash.com/photo-1513104890138-7c749659a591?q=80&w=2070&auto=format&fit=crop' },
            { foodId: 9, foodName: 'Vegan Bowl', category: { categoryName: 'Healthy' }, price: 90, status: 'AVAILABLE', image: 'https://images.unsplash.com/photo-1513104890138-7c749659a591?q=80&w=2070&auto=format&fit=crop' },
            { foodId: 10, foodName: 'Chocolate Lava Cake', category: { categoryName: 'Desserts' }, price: 65, status: 'AVAILABLE', image: 'https://images.unsplash.com/photo-1513104890138-7c749659a591?q=80&w=2070&auto=format&fit=crop' },
            { foodId: 11, foodName: 'Cappuccino', category: { categoryName: 'Drinks' }, price: 35, status: 'AVAILABLE', image: 'https://images.unsplash.com/photo-1513104890138-7c749659a591?q=80&w=2070&auto=format&fit=crop' },
            { foodId: 12, foodName: 'Steak Frites', category: { categoryName: 'Main Course' }, price: 250, status: 'AVAILABLE', image: 'https://images.unsplash.com/photo-1513104890138-7c749659a591?q=80&w=2070&auto=format&fit=crop' },
        ];
    }

    renderGrid();
});