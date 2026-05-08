function filterFoods() {
    const categoryId = document.getElementById("categorySelect").value;
    const foodListContainer = document.getElementById("foodList");

    // Add a loading effect (optional)
    foodListContainer.style.opacity = "0.5";

    fetch(`/foods/filter?categoryId=${categoryId}`)
        .then(response => {
            if (!response.ok) {
                throw new Error("Network response was not ok");
            }
            return response.text();
        })
        .then(html => {
            foodListContainer.innerHTML = html;
            foodListContainer.style.opacity = "1";
        })
        .catch(error => {
            console.error("Error filtering foods:", error);
            foodListContainer.style.opacity = "1";
        });
}
