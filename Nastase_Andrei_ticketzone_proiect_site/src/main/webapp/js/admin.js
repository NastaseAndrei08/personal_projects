document.addEventListener("DOMContentLoaded", () => {
    const eventsContainer = document.getElementById("events-container");
    const modal = document.getElementById("event-modal");
    const modalTitle = document.getElementById("modal-title");
    const eventForm = document.getElementById("event-form");
    const closeModalButton = document.getElementById("close-modal-button");

    const loadEvents = () => {
        fetch("/admin/events")
            .then(response => response.json())
            .then(events => {
                eventsContainer.innerHTML = "";

                if (events.length === 0) {
                    eventsContainer.innerHTML = "<p>No events available.</p>";
                    return;
                }

                events.forEach(event => {
                    const eventPrice = parseFloat(event.price) || 0; // Ensure price is a number
                    const eventCard = `
                    <div class="event-card">
                        <h3>${event.title}</h3>
                        <p><strong>Date:</strong> ${event.date}</p>
                        <p><strong>Location:</strong> ${event.location}</p>
                        <p><strong>Description:</strong> ${event.description}</p>
                        <p><strong>Price:</strong> $${eventPrice.toFixed(2)}</p>
                        <button class="edit-event" data-id="${event.id}">Edit</button>
                        <button class="delete-event" data-id="${event.id}">Delete</button>
                    </div>
                    `;
                    eventsContainer.innerHTML += eventCard;
                });

                // Attach delete event listeners
                document.querySelectorAll(".delete-event").forEach(button => {
                    button.addEventListener("click", event => {
                        const eventId = event.target.dataset.id;
                        fetch(`/admin/event?id=${eventId}`, { method: "DELETE" })
                            .then(response => {
                                if (response.ok) {
                                    alert("Event deleted successfully.");
                                    loadEvents();
                                } else {
                                    alert("Failed to delete event.");
                                }
                            });
                    });
                });

                // Attach edit event listeners
                document.querySelectorAll(".edit-event").forEach(button => {
                    button.addEventListener("click", event => {
                        const eventId = event.target.dataset.id;
                        openModal("Edit", eventId);
                    });
                });
            })
            .catch(error => {
                console.error("Error loading events:", error);
                eventsContainer.innerHTML = "<p>Failed to load events.</p>";
            });
    };

    const openModal = (action, eventId = null) => {
        modalTitle.textContent = `${action} Event`;
        if (action === "Edit" && eventId) {
            fetch(`/admin/event?id=${eventId}`)
                .then(response => {
                    if (!response.ok) throw new Error("Failed to fetch event details.");
                    return response.json();
                })
                .then(event => {
                    document.getElementById("event-id").value = event.id;
                    document.getElementById("event-title").value = event.title;
                    document.getElementById("event-date").value = event.date;
                    document.getElementById("event-location").value = event.location;
                    document.getElementById("event-description").value = event.description;
                    document.getElementById("event-price").value = parseFloat(event.price).toFixed(2); // Ensure price is formatted
                    document.getElementById("event-image").value = event.image;
                })
                .catch(error => alert(error.message));
        } else {
            eventForm.reset();
        }
        modal.classList.remove("hidden");
    };

    const closeModal = () => modal.classList.add("hidden");

    const saveEvent = (event) => {
        event.preventDefault();
        const formData = new FormData(eventForm);
        fetch("/admin/event", {
            method: "POST",
            body: new URLSearchParams(formData),
        })
            .then(response => {
                if (!response.ok) throw new Error("Failed to save event.");
                closeModal();
                loadEvents();
            })
            .catch(error => alert(error.message));
    };

    document.getElementById("add-event-button").addEventListener("click", () => openModal("Add"));
    eventForm.addEventListener("submit", saveEvent);
    closeModalButton.addEventListener("click", closeModal);

    loadEvents();
});
