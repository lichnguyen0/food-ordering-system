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
});
