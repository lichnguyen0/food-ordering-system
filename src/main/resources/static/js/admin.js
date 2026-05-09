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

    // Unified Client-Side Search and Pagination for Food List Table
    const tableBody = document.querySelector('.food-management-table tbody');
    const tableSearchInput = document.querySelector('.table-search-input');
    const entriesSelect = document.querySelector('.entries-select');
    const paginationControls = document.getElementById('pagination-controls');
    
    if (tableBody && tableSearchInput && entriesSelect && paginationControls) {
        let currentPage = 1;
        let rowsPerPage = parseInt(entriesSelect.value);
        let allRows = Array.from(tableBody.querySelectorAll('tr'));
        
        function renderTable() {
            const filter = tableSearchInput.value.trim().toLowerCase();
            
            // 1. Filter rows
            let filteredRows = allRows.filter(row => {
                const cells = row.getElementsByTagName('td');
                if (cells.length < 5) return false;
                
                const idText = cells[0].textContent.trim().toLowerCase();
                const nameText = cells[1].textContent.trim().toLowerCase();
                const qtyText = cells[2].textContent.trim().toLowerCase();
                const statusText = cells[3].textContent.trim().toLowerCase();
                const priceText = cells[4].textContent.replace('$', '').trim().toLowerCase();
                
                if (filter === '') return true;
                
                return idText.includes(filter) || 
                       nameText.includes(filter) || 
                       statusText.includes(filter) || 
                       qtyText.includes(filter) ||
                       priceText.includes(filter);
            });
            
            // 2. Paginate filtered rows
            const totalRows = filteredRows.length;
            const totalPages = Math.ceil(totalRows / rowsPerPage) || 1;
            
            if (currentPage > totalPages && totalPages > 0) currentPage = totalPages;
            if (currentPage < 1) currentPage = 1;
            
            const startIdx = (currentPage - 1) * rowsPerPage;
            const endIdx = Math.min(startIdx + rowsPerPage, totalRows);
            
            // Hide all rows first
            allRows.forEach(row => row.style.display = 'none');
            
            // Show only the paginated slice
            for (let i = startIdx; i < endIdx; i++) {
                if(filteredRows[i]) filteredRows[i].style.display = '';
            }
            
            // 3. Update info text
            document.getElementById('page-info-start').textContent = totalRows === 0 ? 0 : startIdx + 1;
            document.getElementById('page-info-end').textContent = endIdx;
            document.getElementById('page-info-total').textContent = totalRows;
            
            // 4. Render pagination buttons
            renderPaginationBtns(totalPages);
        }
        
        function renderPaginationBtns(totalPages) {
            paginationControls.innerHTML = '';
            if (totalPages <= 1 && allRows.length === 0) return;
            
            // Prev button
            const prevBtn = document.createElement('button');
            prevBtn.className = 'page-btn-prev';
            prevBtn.textContent = 'Previous';
            if(currentPage === 1) prevBtn.style.opacity = '0.5';
            prevBtn.addEventListener('click', () => {
                if (currentPage > 1) {
                    currentPage--;
                    renderTable();
                }
            });
            paginationControls.appendChild(prevBtn);
            
            // Number buttons
            let startPage = Math.max(1, currentPage - 2);
            let endPage = Math.min(totalPages, startPage + 4);
            if (endPage - startPage < 4) {
                startPage = Math.max(1, endPage - 4);
            }
            
            for (let i = startPage; i <= endPage; i++) {
                const btn = document.createElement('button');
                btn.className = `page-btn ${i === currentPage ? 'active' : ''}`;
                btn.textContent = i;
                btn.addEventListener('click', () => {
                    currentPage = i;
                    renderTable();
                });
                paginationControls.appendChild(btn);
            }
            
            // Next button
            const nextBtn = document.createElement('button');
            nextBtn.className = 'page-btn-next';
            nextBtn.textContent = 'Next';
            if(currentPage === totalPages || totalPages === 0) nextBtn.style.opacity = '0.5';
            nextBtn.addEventListener('click', () => {
                if (currentPage < totalPages) {
                    currentPage++;
                    renderTable();
                }
            });
            paginationControls.appendChild(nextBtn);
        }
        
        // Event Listeners
        tableSearchInput.addEventListener('input', () => {
            currentPage = 1; // Reset to page 1 on search
            renderTable();
        });
        
        entriesSelect.addEventListener('change', function() {
            rowsPerPage = parseInt(this.value);
            currentPage = 1;
            renderTable();
        });
        
        // Initial render
        renderTable();
    }
});
