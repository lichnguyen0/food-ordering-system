document.addEventListener('DOMContentLoaded', function() {
    // Basic sidebar interaction
    const menuItems = document.querySelectorAll('.menu-item');
    
    menuItems.forEach(item => {
        item.addEventListener('click', function(e) {
            const parent = this.parentElement;
            
            if (parent.classList.contains('has-submenu')) {
                e.preventDefault();
                parent.classList.toggle('open');
                const submenu = parent.querySelector('.submenu');
                submenu.classList.toggle('show');
            } else {
                menuItems.forEach(i => i.classList.remove('active'));
                this.classList.add('active');
            }
        });
    });


    // Mock search interaction
    const searchInput = document.querySelector('.search-bar input');
    if (searchInput) {
        searchInput.addEventListener('keypress', function(e) {
            if (e.key === 'Enter') {
                console.log('Searching for:', this.value);
            }
        });
    }

    // Load More functionality for Catalogue
    const loadMoreBtn = document.getElementById('loadMoreBtn');
    if (loadMoreBtn) {
        loadMoreBtn.addEventListener('click', function() {
            const currentPage = parseInt(this.getAttribute('data-page'));
            const nextPage = currentPage + 1;
            
            this.classList.add('loading');
            
            fetch(`/admin/categories/load-more?page=${nextPage}`)
                .then(response => {
                    if (!response.ok) throw new Error('Network response was not ok');
                    return response.json();
                })
                .then(data => {
                    this.classList.remove('loading');
                    
                    if (data.foods && data.foods.length > 0) {
                        const grid = document.querySelector('.catalogue-grid');
                        data.foods.forEach(food => {
                            const cardHtml = renderFoodCard(food);
                            grid.insertAdjacentHTML('beforeend', cardHtml);
                        });
                        
                        this.setAttribute('data-page', data.currentPage);
                        
                        if (!data.hasMore) {
                            this.classList.add('hidden');
                        }
                    } else {
                        this.classList.add('hidden');
                    }
                })
                .catch(error => {
                    console.error('Error loading more foods:', error);
                    this.classList.remove('loading');
                });
        });
    }

    function renderFoodCard(food) {
        const imageUrl = food.image || 'https://images.unsplash.com/photo-1513104890138-7c749659a591?q=80&w=2070&auto=format&fit=crop';
        const price = Math.floor(food.price);
        // Matching Thymeleaf logic for dummy stats
        const totalOrder = food.foodId * 15;
        const revenue = Math.floor(food.price * 50);
        
        return `
            <div class="food-card">
                <div class="food-img-wrapper">
                    <img src="${imageUrl}" alt="${food.foodName}">
                </div>
                <div class="food-info">
                    <div class="food-title-row">
                        <div class="food-title">${food.foodName}</div>
                        <div class="price-badge">$${price}</div>
                    </div>
                    <div class="food-stats">
                        <div class="stat-item">Total Order : <span>${totalOrder}</span></div>
                        <div class="stat-item">Revenue : <span>$${revenue}</span></div>
                    </div>
                    <button class="btn-see-more">See More</button>
                </div>
            </div>
        `;
    }
});
