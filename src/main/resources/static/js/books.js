import { byId, show, hide, setText, showModal } from "./util.js";

// --- DOM Elements ---
const actionForms = byId("action-forms");
const booksList = byId("books-list");
const resultsSeparator = byId("results-separator");


// Static Form Elements

// Create Book Form
const createBookFormStatic = byId("create-book-form-static");
const createBookTitleInput = byId("create-book-title");
const createBookYearInput = byId("create-book-year");
const createBookAuthorIdInput = byId("create-book-author-id");
const createBookAvailableInput = byId("create-book-available");
const cancelCreateBookButton = byId("cancel-create-book");

// Find Book Form
const findBookFormStatic = byId("find-book-form-static");
const findBookTitleHeader = byId("find-book-title"); // Renamed to avoid conflict
const searchBookLabel = byId("search-book-label");
const searchBookInput = byId("search-book-input");
const searchBookButton = byId("search-book-button");
const cancelSearchBookButton = byId("cancel-search-book");

// Sidebar Buttons/Dropdown
const showAllBooksButton = byId("show-all");
const showAvailableBooksButton = byId("show-available");
const showByYearButton = byId("show-by-year");
const showByAuthorButton = byId("show-by-author");
const showByGenreButton = byId("show-by-genre");
const showGroupedByGenreButton = byId("show-grouped-by-genre");
const showByTitleButton = byId("show-by-title");
const showCreateBookButton = byId("show-create-form");
const showGenresButton = byId("show-genres");
const findBooksDropdownButton = document.querySelector(".dropdown .dropbtn");
const findBooksDropdownContainer = document.querySelector(".dropdown");


// --- Event Listeners for Sidebar ---
showAllBooksButton.addEventListener("click", async () => { showBooksList(); await fetchAndDisplayBooks("/api/books"); });
showAvailableBooksButton.addEventListener("click", async () => { showBooksList(); await fetchAndDisplayBooks("/api/books?available=true"); });
showByYearButton.addEventListener("click", () => showFindBookFormView("Year"));
showByAuthorButton.addEventListener("click", () => showFindBookFormView("Author Name"));
showByGenreButton.addEventListener("click", () => showFindBookFormView("Genre Name"));
showGroupedByGenreButton.addEventListener("click", async () => { showBooksList(); await fetchAndDisplayGroupedByGenre(); });
showByTitleButton.addEventListener("click", () => showFindBookFormView("Title"));
showCreateBookButton.addEventListener("click", showCreateBookFormView);
showGenresButton.addEventListener("click", async () => { showBooksList(); await fetchAndDisplayGenres(); });

// --- Event Listeners for Static Forms ---

// Create Book Form
createBookFormStatic.addEventListener("submit", async event => {
    event.preventDefault();
    await createBook();
});
cancelCreateBookButton.addEventListener("click", showBooksList);

// Find Book Form
findBookFormStatic.addEventListener("submit", async event => {
    event.preventDefault();
    const value = searchBookInput.value;
    const filterType = findBookTitleHeader.textContent.replace("Find Book by ", ""); // Extract actual filter type

    // Client-side validation
    if (value.trim() === '') {
        await showModal("Error", `Search input cannot be empty.`);
        return;
    }

    let url = "/api/books?";
    if (filterType === "Year") {
        const year = parseInt(value);
        if (isNaN(year) || year <= 0 || year > new Date().getFullYear()) { // Basic validation for year search
            await showModal("Error", `Year must be a positive number and not from future.`);
            return;
        }
        url += `year=${encodeURIComponent(year)}`;
    } else if (filterType === "Author Name") {
        url += `authorPartName=${encodeURIComponent(value)}`;
    } else if (filterType === "Genre Name") {

        await searchByGenreName(value);
        return;
    } else if (filterType === "Title") {
        url += `title=${encodeURIComponent(value)}`;
    }
    await fetchAndDisplayBooks(url);
    showBooksList(); // Transition back to book list view after search
});

// Enable/disable search button based on input
searchBookInput.addEventListener('input', () => {
    searchBookButton.disabled = searchBookInput.value.trim() === '';
});
cancelSearchBookButton.addEventListener("click", showBooksList);


// --- UI State Management Functions ---

/* Clears all forms and results list */
function clearAllForms() {
    hide(createBookFormStatic.id);
    hide(findBookFormStatic.id);
}

/* Clears the book list */
function clearResults() {
    booksList.innerHTML = "";
    hide(resultsSeparator.id);
}

/* Displays a list of books in the UI */
async function displayBooks(books) {
    clearResults();
    if (books.length === 0) {
        await showModal("Info", "No books found for your search.");
        return;
    }

    for (const book of books) {
        const li = document.createElement("li");
        const a = document.createElement("a");
        a.href = "book.html";
        a.innerText = book.title;
        a.onclick = () => {
            const bookData = {
                id: book.id,
//                available: book.available
            };
            sessionStorage.setItem("bookData", JSON.stringify(bookData));
        };

        li.append(a);
        booksList.append(li);
    }
    show(resultsSeparator.id);
}

/* Hides forms, shows book list, enables sidebar buttons */
function showBooksList() {
    clearAllForms();
    show(booksList.id);
    show(resultsSeparator.id);

    showCreateBookButton.disabled = false;
    if (findBooksDropdownContainer) findBooksDropdownContainer.classList.remove("blocked");
    if (findBooksDropdownButton) findBooksDropdownButton.disabled = false;
}

/* Hides book list, shows create form, disables sidebar buttons */
function showCreateBookFormView() {
    clearAllForms();
    hide(booksList.id);
    hide(resultsSeparator.id);
    show(createBookFormStatic.id);
    // Clear inputs
    createBookTitleInput.value = '';
    createBookYearInput.value = '';
    createBookAuthorIdInput.value = '';
    createBookAvailableInput.value = '';
    // Disable sidebar buttons
    // The dropdown container will be blocked, so individual buttons don't need explicit disabling
    if (findBooksDropdownContainer) findBooksDropdownContainer.classList.add("blocked");
    if (findBooksDropdownButton) findBooksDropdownButton.disabled = true;
    showCreateBookButton.disabled = true;
}

/* Hides book list, shows find form, disables sidebar buttons */
function showFindBookFormView(filterType) {
    clearAllForms();
    findBookTitleHeader.textContent = `Find Book by ${filterType}`;
    searchBookLabel.textContent = `${filterType}:`;
    searchBookInput.value = '';
    searchBookButton.disabled = true;

    // Set input type based on filterType
    searchBookInput.type = (filterType === "Year") ? "number" : "text";

    hide(booksList.id);
    hide(resultsSeparator.id);
    show(findBookFormStatic.id);

    if (findBooksDropdownContainer) findBooksDropdownContainer.classList.add("blocked");
    if (findBooksDropdownButton) findBooksDropdownButton.disabled = true;
    showCreateBookButton.disabled = true;
}

// --- API Call Functions ---

/**
 * Fetches books from a given URL and displays them
 * @param {string} url - The API endpoint to fetch from
 */
async function fetchAndDisplayBooks(url) {
setText("results-separator", "");
    try {
        const response = await fetch(url);
        if (response.ok) {
            const data = await response.json();
            await displayBooks(Array.isArray(data) ? data : [data]);
        } else {
            clearResults();
            const errorData = await response.json();
            if (response.status === 404) {
                await showModal("Info", `No book found for your search.`);
            } else {
                await showModal("Error", `Error: ${response.status} ${errorData.message || response.statusText}`);
            }
        }
    } catch (error) {
        clearResults();
        await showModal("Error", "A network error occurred.");
    }
}

/* Handles the creation of a new book via POST request */
async function createBook() {
    // Client-side validation
    if (createBookTitleInput.value.trim() === '') {
        await showModal("Error", `Book title cannot be blank.`);
        return;
    }
    const year = parseInt(createBookYearInput.value);
    const currentYear = new Date().getFullYear();
    if (isNaN(year) || year <= 0 || year > currentYear) {
        await showModal("Error", `Year must be a positive number and not in the future.`);
        return;
    }
    const authorId = parseInt(createBookAuthorIdInput.value);
    if (isNaN(authorId) || authorId <= 0) {
        await showModal("Error", `Author ID must be a positive number.`);
        return;
    }
    const available = parseInt(createBookAvailableInput.value);
    if (isNaN(available) || available < 0) {
        await showModal("Error", `Available count must be a non-negative number.`);
        return;
    }

    const book = {
        title: createBookTitleInput.value,
        year: year,
        authorId: authorId,
        available: available,
    };

    try {
        const response = await fetch("/api/books", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(book),
        });

        if (response.ok) {
            await showModal("Success", `Book '${book.title}' created successfully!`);
            showBooksList(); // Show the list view
            await fetchAndDisplayBooks("/api/books"); // Refresh the list
        } else {
            const errorData = await response.json();
            await showModal("Error", `Creation failed: ${errorData.message}`);
        }
    } catch (error) {
        await showModal("Error", "A network error occurred while creating the book.");
    }
}

async function fetchAndDisplayGenres() {
    try {
        const response = await fetch("/api/genres");
        if (response.ok) {
            const genres = await response.json();
            displayGenres(genres);
        } else {
            await showModal("Error", "Failed to fetch genres.");
        }
    } catch (error) {
        await showModal("Error", "A network error occurred.");
    }
}

function displayGenres(genres) {
    clearResults();
    setText("results-separator", "Genres");
    show("results-separator");
    
    if (genres.length === 0) {
        booksList.innerHTML = "<li>No genres found.</li>";
        return;
    }

    genres.forEach(genre => {
        const li = document.createElement("li");
        const a = document.createElement("a");
        a.href = "#";
        a.innerText = genre.name;
        a.onclick = async (e) => {
            e.preventDefault();
            await fetchAndDisplayBooks(`/api/genres/name/${encodeURIComponent(genre.name)}/books`);
            setText("results-separator", `Books for Genre: ${genre.name}`);
        };
        li.append(a);
        booksList.append(li);
    });
}

async function searchByGenreName(name) {
    try {
        const response = await fetch(`/api/genres/name/${encodeURIComponent(name)}/books`);
        if (response.ok) {
            const books = await response.json();
            await displayBooks(books);
            setText("results-separator", `Books for Genre: ${name}`);
            showBooksList();
        } else {
            const errorData = await response.json();
            await showModal("Info", `No books found for genre "${name}"`);
        }
    } catch (error) {
        await showModal("Error", "Network error while searching genre.");
    }
}

async function fetchAndDisplayGroupedByGenre() {
    try {
        const response = await fetch("/api/genres/with-books");
        if (response.ok) {
            const data = await response.json();
            displayGroupedBooks(data);
        } else {
            await showModal("Error", "Failed to fetch grouped books.");
        }
    } catch (error) {
        await showModal("Error", "A network error occurred.");
    }
}

function displayGroupedBooks(genresWithBooks) {
    clearResults();
    setText("results-separator", "Books Grouped by Genre");
    show("results-separator");

    if (genresWithBooks.length === 0) {
        booksList.innerHTML = "<li>No genres found.</li>";
        return;
    }

    genresWithBooks.forEach(genre => {
        if (genre.books && genre.books.length > 0) {
            const genreItem = document.createElement("li");
            genreItem.className = "genre-group";
            genreItem.innerHTML = `<span class='genre-title'>${genre.name}</span>`;
            const innerUl = document.createElement("ul");
            innerUl.className = "genre-books-list";
            
            genre.books.forEach(book => {
                const bookLi = document.createElement("li");
                const a = document.createElement("a");
                a.href = "book.html";
                a.innerText = book.title;
                a.onclick = () => {
                    sessionStorage.setItem("bookData", JSON.stringify({ id: book.id }));
                };
                bookLi.append(a);
                innerUl.append(bookLi);
            });
            
            genreItem.append(innerUl);
            booksList.append(genreItem);
        }
    });
}

// Initial check for genre filter from other pages
window.addEventListener("DOMContentLoaded", async () => {
    const filter = sessionStorage.getItem("booksFilter");
    if (filter) {
        const filterData = JSON.parse(filter);
        if (filterData.type === "genre") {
            await fetchAndDisplayBooks(`/api/books?genreId=${filterData.id}`);
            setText("results-separator", `Books for Genre: ${filterData.name}`);
        }
        sessionStorage.removeItem("booksFilter");

}
});