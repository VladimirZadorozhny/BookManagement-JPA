import { byId, show, hide, setText, showModal } from "./util.js";

// --- DOM Elements ---
const staticDetailsDiv = byId("static-details");
const editFormContainerDiv = byId("edit-form-container");
const editButton = byId("edit-button");
const deleteButton = byId("delete-button");
const rentButton = byId("rent-this-book-button");
const saveButton = byId("save-button");
const cancelButton = byId("cancel-button");
const editBookForm = byId("edit-book-form");

const editTitleInput = byId("edit-title");
const editYearInput = byId("edit-year");
const editAuthorIdInput = byId("edit-authorId");
const editAvailableInput = byId("edit-available");

// --- State ---
const bookDataString = sessionStorage.getItem("bookData");
const bookDataFromStorage = bookDataString ? JSON.parse(bookDataString) : {};
const bookId = bookDataFromStorage.id;


let currentBookData = {}; // To store the fetched DTO data for display
let originalBookDataForEdit = {}; // To store data for checking changes in edit form

// --- UI State Functions ---
function showStaticDetails() {
    hide(editFormContainerDiv.id);
    show(staticDetailsDiv.id);
    editButton.disabled = false;
    deleteButton.disabled = false;
}

function showEditForm() {
    hide(staticDetailsDiv.id);
    show(editFormContainerDiv.id);
    

    // Store original data for change detection
    originalBookDataForEdit = {
        title: currentBookData.title,
        year: currentBookData.year,
        authorId: currentBookData.authorId,
        available: currentBookData.available
    };

    // Pre-fill the form with original data
    editTitleInput.value = originalBookDataForEdit.title;
    editYearInput.value = originalBookDataForEdit.year;
    editAuthorIdInput.value = originalBookDataForEdit.authorId;
    editAvailableInput.value = originalBookDataForEdit.available;

    // Disable save button initially and add listeners for validation
    saveButton.disabled = true;
    [editTitleInput, editYearInput, editAuthorIdInput, editAvailableInput].forEach(input => {
        input.addEventListener('input', checkFormChanges);
    });
}

// --- Change Detection for Save Button ---
function checkFormChanges() {
    const isTitleChanged = editTitleInput.value !== originalBookDataForEdit.title;

    const isYearChanged = parseInt(editYearInput.value) !== originalBookDataForEdit.year;
    const isAuthorIdChanged = parseInt(editAuthorIdInput.value) !== originalBookDataForEdit.authorId;
    const isAvailableChanged = parseInt(editAvailableInput.value) !== originalBookDataForEdit.available;

    const hasChanges = isTitleChanged || isYearChanged || isAuthorIdChanged || isAvailableChanged;
    saveButton.disabled = !hasChanges;

    if (!hasChanges) {
        saveButton.title = "No changes to save";
    } else {
        saveButton.title = "";
    }
}

// --- Event Listeners ---
editButton.addEventListener("click", showEditForm);
deleteButton.addEventListener("click", async () => await deleteBook());
rentButton.addEventListener("click", () => {
    if (currentBookData.available) {
        sessionStorage.setItem('pendingAction', JSON.stringify({
            action: 'rent',
            bookId: bookId,
            bookTitle: currentBookData.title
        }));
        window.location.href = "users.html";
    }
});
cancelButton.addEventListener("click", showStaticDetails);
editBookForm.addEventListener("submit", async (event) => {
    event.preventDefault();
    await updateBook();
});

// --- Initial Load ---
if (!bookId) {
    showModal("Error", "No book selected. Please return to the books list.").then(() => {
        window.location.href = "books.html"; // Redirect back
    });
} else {
    await fetchBookDetails();
}

// --- API Call Functions ---
async function fetchBookDetails() {
    try {
        const response = await fetch(`/api/books/${bookId}/details`);
        if (response.ok) {
            currentBookData = await response.json(); // Store the fetched data for display
            setText("title", currentBookData.title);
            setText("year", currentBookData.year);
            setText("author", currentBookData.authorName || "Unknown");
            
            // Display genres
            const genresSpan = byId("genres");
            genresSpan.innerHTML = "";
            if (currentBookData.genres && currentBookData.genres.length > 0) {
                currentBookData.genres.forEach((genreName, index) => {
                    const a = document.createElement("a");
                    a.href = "#";
                    a.innerText = genreName;
                    a.onclick = async (e) => {
                        e.preventDefault();
                          sessionStorage.setItem("booksFilter", JSON.stringify({ type: "genre", name: genreName }));
                            window.location.href = "books.html";
                            }
                      genresSpan.appendChild(a);
                    if (index < currentBookData.genres.length - 1) {
                        genresSpan.appendChild(document.createTextNode(", "));
                    }
                })
            } else {
                genresSpan.innerText = "None";
            }

            setText("available", Number(currentBookData.available) > 0 ? "Yes" : "No");

            rentButton.disabled = !currentBookData.available;
            rentButton.classList.toggle("disabled-button", !currentBookData.available);

            showStaticDetails();
        } else {
            const errorData = await response.json();
            await showModal("Error", `Could not load book details: ${errorData.message || 'Book not found.'}`);
            window.location.href = "books.html";
        }
    }
    catch (error) {
        await showModal("Error", "A network error occurred while fetching book details.");
        window.location.href = "books.html";
    }
}

async function updateBook() {
    // 1 - Frontend validation on click
    if (editTitleInput.value.trim() === '') {
        await showModal("Error", `Book title cannot be blank.`);
        return;
    }
    const year = parseInt(editYearInput.value);
    const currentYear = new Date().getFullYear();
    if (isNaN(year) || year <= 0 || year > currentYear) {
        await showModal("Error", `Year must be a positive number and not in the future.`);
        return;
    }
    const authorId = parseInt(editAuthorIdInput.value);
    if (isNaN(authorId) || authorId <= 0) {
        await showModal("Error", `Author ID must be a positive number.`);
        return;
    }
    const available = parseInt(editAvailableInput.value);
    if (isNaN(available) || available < 0) {
        await showModal("Error", `Available count must be a non-negative number.`);
        return;
    }

    // 2 - Create DTO and send request
    const updatedBook = {
        title: editTitleInput.value,
        year: year,
        authorId: authorId,
        available: available
    };

    try {
        const response = await fetch(`/api/books/${bookId}`, {
            method: "PUT",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(updatedBook),
        });

        if (response.ok) {

            await showModal("Success", `Book '${updatedBook.title}' updated successfully!`);
            await fetchBookDetails(); 
        } else {
            const errorData = await response.json();
            await showModal("Error", `Update failed: ${errorData.message}`);
        }
    } catch (error) {
        await showModal("Error", "A network error occurred while updating the book.");
    }
}

async function deleteBook() {
    const confirmed = await showModal("Confirm Deletion", "Are you sure you want to delete this book?", [
        { text: "Yes, Delete", class: "btn-danger", value: true },
        { text: "Cancel", class: "btn-secondary", value: false }
    ]);

    if (!confirmed) return;

    try {
        const response = await fetch(`/api/books/${bookId}`, {
            method: "DELETE",
        });

        if (response.ok) {
            sessionStorage.removeItem("bookData");
            await showModal("Success", "Book deleted successfully.");
            window.location.href = "books.html";
        } else {
            const errorData = await response.json();
            await showModal("Error", `Deletion failed: ${errorData.message}`);
        }
    } catch (error) {
        await showModal("Error", "A network error occurred while deleting the book.");
    }
}

