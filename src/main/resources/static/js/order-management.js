document.addEventListener("DOMContentLoaded", () => {
    // Timeline Toggling
    const timelineTriggers = document.querySelectorAll(".timeline-trigger");
    timelineTriggers.forEach(trigger => {
        trigger.addEventListener("click", function() {
            const orderId = this.getAttribute("data-order-id");
            const detailRow = document.getElementById(`detail-${orderId}`);
            if (detailRow.style.display === "none") {
                detailRow.style.display = "table-row";
                this.classList.replace("fa-clock-rotate-left", "fa-circle-xmark");
            } else {
                detailRow.style.display = "none";
                this.classList.replace("fa-circle-xmark", "fa-clock-rotate-left");
            }
        });
    });

    // AJAX Status Updates (Premium Experience)
    const actionButtons = document.querySelectorAll(".btn-action[data-status]");
    actionButtons.forEach(button => {
        button.addEventListener("click", function(e) {
            e.preventDefault();
            const orderId = this.getAttribute("data-order-id");
            const newStatus = this.getAttribute("data-status");
            
            if (confirm(`Are you sure you want to move this order to ${newStatus}?`)) {
                updateOrderStatus(orderId, newStatus);
            }
        });
    });
});

async function updateOrderStatus(orderId, status) {
    try {
        const formData = new FormData();
        formData.append("orderId", orderId);
        formData.append("status", status);

        const response = await fetch("/admin/orders/update-status", {
            method: "POST",
            body: formData
        });

        if (response.ok) {
            // Smoothly reload the page to reflect changes and timeline
            // In a more advanced version, we would update the DOM partially
            window.location.reload();
        } else {
            alert("Failed to update status. Please try again.");
        }
    } catch (error) {
        console.error("Error updating order status:", error);
        alert("An error occurred. Check console for details.");
    }
}
