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

        borrowedBooksList.innerHTML = ""; // Clear previous results

        try {
            const response = await fetch(`/api/users/${userId}/books`);
            if (response.ok) {
                const books = await response.json();

                if (books.length === 0) {
                    borrowedBooksList.innerHTML = "<li>This user has no borrowed books.</li>";
                } else {
                    for (const book of books) {
                        const li = document.createElement("li");
                        // Link to book details page
                        const a = document.createElement("a");
                        a.href = "book.html";
                        a.innerText = `${book.title} (Year: ${book.year})`;
                        a.onclick = () => sessionStorage.setItem("bookId", book.id);
                        li.append(a);

                        // Add Return button
                        const returnButton = document.createElement("button");
                        returnButton.innerText = "Return";
                        returnButton.className = "side-button"; // Reuse button style
                        returnButton.addEventListener('click', async () => await returnBook(book.id));
                        li.append(returnButton);

                        borrowedBooksList.append(li);
                    }
                }
                show(borrowedBooksSection.id);
            } else {
                await showModal("Error", "Error loading borrowed books.");
                show(borrowedBooksSection.id);
            }
        } catch (error) {
            await showModal("Error", "A network error occurred while fetching borrowed books.");
            show(borrowedBooksSection.id);
        }
    }

    // --- Return Book Function ---
    async function returnBook(bookId) {
        const confirmed = await showModal("Confirm Return", "Are you sure you want to return this book?", [
            { text: "Yes, Return", class: "btn-primary", value: true },
            { text: "Cancel", class: "btn-secondary", value: false }
        ]);

        if (!confirmed) {
            return; // User cancelled
        }

        const returnRequest = {
            bookId: bookId
        };

        try {
            const response = await fetch(`/api/users/${userId}/return`, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(returnRequest)
            });

            if (response.ok) {
                await showModal("Success", "Book returned successfully!");
                await fetchAndDisplayBorrowedBooks(); // Refresh the list
            } else {
                const errorData = await response.json();
                await showModal("Error", `Return failed: ${errorData.message}`);
            }
        } catch (error) {
            await showModal("Error", "A network error occurred during the return process.");
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
