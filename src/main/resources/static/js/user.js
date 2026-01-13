import { byId, show, hide, setText, showModal } from "./util.js";

    // --- DOM Elements ---
    const staticDetailsDiv = byId("static-details");
    const editFormContainerDiv = byId("edit-form-container");
    const editButton = byId("edit-button");
    const deleteButton = byId("delete-button");
    const saveButton = byId("save-button");
    const cancelButton = byId("cancel-button");
    const editUserForm = byId("edit-user-form");

    const editNameInput = byId("edit-name");
    const editEmailInput = byId("edit-email");

    const showBorrowedBooksButton = byId("show-borrowed-books");
    const borrowedBooksSection = byId("borrowed-books-section");
    const borrowedBooksList = byId("borrowed-books-list");


    let currentUserData = {}; // To store the fetched user data
    let originalUserData = {}; // To store the original data for change detection

    // --- Functions to toggle form visibility ---
    function showStaticDetails() {
        hide(editFormContainerDiv.id);
        hide(borrowedBooksSection.id); // Hide borrowed books section when static details are shown
        show(staticDetailsDiv.id);
        showBorrowedBooksButton.disabled = false; // Enable borrowed books button
    }

    function showEditForm() {
        show(editFormContainerDiv.id);
        hide(staticDetailsDiv.id);
        hide(borrowedBooksSection.id); // Hide borrowed books section when edit form is shown
        
        // Store original data
        originalUserData = { ...currentUserData };

        // Pre-fill the form with current user data
        editNameInput.value = currentUserData.name;
        editEmailInput.value = currentUserData.email;
        checkFormChanges(); // Check initial state
        showBorrowedBooksButton.disabled = true; // Disable borrowed books button when edit form is open
    }

    // --- Change Detection for Save Button ---
    function checkFormChanges() {
        const isNameChanged = editNameInput.value !== originalUserData.name;
        const isEmailChanged = editEmailInput.value !== originalUserData.email;
        
        const hasChanges = isNameChanged || isEmailChanged;

        saveButton.disabled = !hasChanges;
        if (!hasChanges) {
            saveButton.title = "No changes, nothing to update";
        } else {
            saveButton.title = "";
        }
    }

    // --- Event Listeners ---
    editButton.addEventListener("click", showEditForm);
    deleteButton.addEventListener("click", async () => await deleteUser());
    cancelButton.addEventListener("click", () => {
        showStaticDetails();
    });
    editUserForm.addEventListener("submit", async (event) => {
        event.preventDefault();
        await updateUser();
    });
    showBorrowedBooksButton.addEventListener("click", async () => await fetchAndDisplayBorrowedBooks());
    editNameInput.addEventListener("input", checkFormChanges);
    editEmailInput.addEventListener("input", checkFormChanges);


    // --- Initial Fetch and Display ---
    const userId = sessionStorage.getItem("userId");

    if (!userId) {
        await showModal("Error", "No user selected. Please return to the users list.");
    } else {
        await fetchUserDetails();
    }

    async function fetchUserDetails() {
        try {
            const response = await fetch(`/api/users/${userId}`);
            if (response.ok) {
                currentUserData = await response.json();
                setText("name", currentUserData.name);
                setText("email", currentUserData.email);
                showStaticDetails();
            } else {
                await showModal("Not found", `User with ID ${userId} not found.`);
            }
        } catch (error) {
            await showModal("Error", "A network error occurred while fetching user details.");
        }
    }

    async function fetchAndDisplayBorrowedBooks() {
        borrowedBooksList.innerHTML = "";

        try {
            const response = await fetch(`/api/users/${userId}/bookings`);
            if (response.ok) {
                let bookings = await response.json();

                // Show only active bookings OR bookings with unpaid fines.
                bookings = bookings.filter(b => !b.returnedAt || (b.fine > 0 && !b.finePaid));

                if (bookings.length === 0) {
                    borrowedBooksList.innerHTML = "<li>This user has no active bookings or unpaid fines.</li>";
                } else {
                    // Sort by borrowed date desc
                    bookings.sort((a, b) => new Date(b.borrowedAt) - new Date(a.borrowedAt));

                    for (const booking of bookings) {
                        const li = document.createElement("li");
                        li.className = "booking-item";

                        // Date normalization to midnight for day-based comparison
                        const today = new Date();
                        today.setHours(0, 0, 0, 0);
                        const dueDate = new Date(booking.dueAt);
                        dueDate.setHours(0, 0, 0, 0);

                        const diffTime = dueDate - today;
                        const diffDays = Math.round(diffTime / (1000 * 60 * 60 * 24));

                        const isReturned = !!booking.returnedAt;
                        const isOverdue = !isReturned && diffDays < 0;
                        const isNearDue = !isReturned && diffDays >= 0 && diffDays <= 3;

                        // Info Section
                        const infoDiv = document.createElement("div");
                        infoDiv.className = "booking-info";

                        const titleDiv = document.createElement("div");
                        titleDiv.className = "booking-title";
                        const titleLink = document.createElement("a");
                        titleLink.href = "book.html";
                        titleLink.innerText = `${booking.bookTitle} (${booking.bookYear})`;
                        titleLink.onclick = () => sessionStorage.setItem("bookId", booking.bookId);
                        titleDiv.appendChild(titleLink);
                        infoDiv.appendChild(titleDiv);

                        const detailsDiv = document.createElement("div");
                        detailsDiv.className = "booking-details";

                        // Borrowed Date
                        const bDate = new Date(booking.borrowedAt).toLocaleDateString();
                        detailsDiv.innerHTML += `<div>Borrowed: ${bDate}</div>`;

                        // Due Date Styling
                        let dueClass = "";
                        if (isOverdue) dueClass = "status-overdue";
                        else if (isNearDue) dueClass = "status-near-due";
                        
                        detailsDiv.innerHTML += `<div class="${dueClass}">Due: ${new Date(booking.dueAt).toLocaleDateString()}</div>`;

                        if (!isReturned) {
                            detailsDiv.innerHTML += `<div class="${dueClass}">Days left: ${diffDays}</div>`;
                        } else {
                            const returnedDate = new Date(booking.returnedAt).toLocaleDateString();
                            detailsDiv.innerHTML += `<div class="status-overdue">Returned: ${returnedDate}</div>`;
                        }

                        if (booking.fine > 0) {
                            detailsDiv.innerHTML += `<div class="status-overdue">Fine: $${booking.fine.toFixed(2)}</div>`;
                        }

                        infoDiv.appendChild(detailsDiv);
                        li.appendChild(infoDiv);

                        // Actions Section
                        const actionDiv = document.createElement("div");
                        actionDiv.className = "booking-actions";

                        if (!isReturned) {
                            const btn = document.createElement("button");
                            btn.innerText = "Return";
                            btn.className = "side-button";
                            btn.style.width = "auto";
                            if (isOverdue) btn.classList.add("btn-return-overdue");
                            else if (isNearDue) btn.classList.add("btn-return-near-due");
                            
                            btn.onclick = async () => await handleReturnClick(booking);
                            actionDiv.appendChild(btn);
                        } else {
                            const payBtn = document.createElement("button");
                            payBtn.innerText = "Pay fines";
                            payBtn.className = "side-button btn-pay-fine";
                            payBtn.style.width = "auto";
                            payBtn.onclick = async () => await payBookingFine(booking.id);
                            actionDiv.appendChild(payBtn);
                        }

                        li.appendChild(actionDiv);
                        borrowedBooksList.appendChild(li);
                    }
                }
                show(borrowedBooksSection.id);
            } else {
                await showModal("Error", "Error loading bookings.");
            }
        } catch (error) {
            console.error(error);
            await showModal("Error", "Network error occurred.");
        }
    }

    // --- Handle Return Logic ---
    async function handleReturnClick(booking) {

        if (booking.fine > 0) {
            const confirmed = await showModal("Overdue Return", 
                `This book is overdue. \nEstimated Fine: $${booking.fine.toFixed(2)}. \n\nYou can return the book now. The fine will be recorded on your account.`, 
                [
                    { text: "Return Book", class: "btn-primary", value: true },
                    { text: "Cancel", class: "btn-secondary", value: false }
                ]
            );

            if (confirmed === true) {
                await processReturn(booking.bookId);
            }
        } else {
            const confirmed = await showModal("Confirm Return", "Are you sure you want to return this book?", [
                { text: "Yes, Return", class: "btn-primary", value: true },
                { text: "Cancel", class: "btn-secondary", value: false }
            ]);
            if (confirmed === true) {
                await processReturn(booking.bookId);
            }
        }
    }

    // --- Process Return API Call ---
    async function processReturn(bookId) {
        try {
            const response = await fetch(`/api/users/${userId}/return`, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ bookId: bookId })
            });

            if (response.ok) {
                await showModal("Success", "Book returned successfully!");
                await fetchAndDisplayBorrowedBooks(); // Refresh list
            } else {
                const errorData = await response.json();
                await showModal("Error", `Return failed: ${errorData.message}`);
            }
        } catch (error) {
            await showModal("Error", "Network error during return.");
        }
    }

    // --- Pay Fine API Call ---
    async function payBookingFine(bookingId) {
        try {
            const response = await fetch(`/api/users/${userId}/bookings/${bookingId}/pay`, {
                method: "POST"
            });

            if (response.ok) {
                await showModal("Success", "Fine paid successfully!");
                await fetchAndDisplayBorrowedBooks();
            } else {
                const errorData = await response.json();
                await showModal("Error", `Payment failed: ${errorData.message}`);
            }
        } catch (error) {
            await showModal("Error", "Network error during payment.");
        }
    }


    // --- Update User Function ---
    async function updateUser() {
        // Frontend validation for name
        if (editNameInput.value.trim() === '') {
            await showModal("Error", `User's name cannot be blank.`);
            return;
        }
        // Frontend validation for email format
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        if (!emailRegex.test(editEmailInput.value)) {
            await showModal("Error", `Please enter a valid email address.`);
            return;
        }

        const updatedUser = {
            name: editNameInput.value,
            email: editEmailInput.value
        };

        try {
            const response = await fetch(`/api/users/${userId}`, {
                method: "PUT",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(updatedUser),
            });

            if (response.ok) {
                await showModal("Success", `User '${updatedUser.name}' updated successfully!`);
                await fetchUserDetails(); // Re-fetch to ensure consistency and display updates
                showStaticDetails();
            } else {
                const errorData = await response.json();
                await showModal("Error", `Update failed: ${errorData.message}`);
            }
        } catch (error) {
            await showModal("Error", "A network error occurred while updating the user.");
        }
    }

    // --- Delete User Function ---
    async function deleteUser() {
        const confirmed = await showModal("Confirm Deletion", "Are you sure you want to delete this user?", [
            { text: "Yes, Delete", class: "btn-danger", value: true },
            { text: "Cancel", class: "btn-secondary", value: false }
        ]);

        if (!confirmed) {
            return; // User cancelled
        }

        try {
            const response = await fetch(`/api/users/${userId}`, {
                method: "DELETE",
            });

            if (response.ok) {
                await showModal("Success", "User deleted successfully!");
                sessionStorage.removeItem("userId");
                window.location.href = "users.html"; // Redirect to user list
            } else {
                const errorData = await response.json();
                await showModal("Error", `Deletion failed: ${errorData.message}`);
            }
        } catch (error) {
            await showModal("Error", "A network error occurred while deleting the user.");
        }
    }
