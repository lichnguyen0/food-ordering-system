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
});
