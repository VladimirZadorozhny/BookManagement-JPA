"use strict";
import { byId, show, hide, setText, showModal } from "./util.js";

// --- DOM Elements ---
const actionForms = byId("action-forms");
const authorsList = byId("authors-list");
const resultsSeparator = byId("results-separator");

// Static form elements
const createAuthorForm = byId("create-author-form");
const createNameInput = byId("create-author-name");
const createBirthdateInput = byId("create-author-birthdate");

// Sidebar buttons
const showAllAuthorsButton = byId("show-all-authors");
const showCreateAuthorFormButton = byId("show-create-author-form");



// --- Event Listeners for Sidebar ---
showAllAuthorsButton.addEventListener("click", async () => {
    showAuthorsList();
    await fetchAndDisplayAuthors("/api/authors");
});
showCreateAuthorFormButton.addEventListener("click", showCreateAuthorFormView);

// --- Event Listeners for Create Author Form ---
createAuthorForm.addEventListener("submit", async event => {
    event.preventDefault();

    if (createNameInput.value.trim() === '') {
        await showModal("Error", `Author's name cannot be blank.`);
        return;
    }
    if (createBirthdateInput.value === '') {
        await showModal("Error", `Author's birthdate cannot be empty.`);
        return;
    }
    const birthdate = new Date(createBirthdateInput.value);
    const today = new Date();
    today.setHours(0, 0, 0, 0);

    if (birthdate > today) {
        await showModal("Error", `Author's birthdate cannot be in the future.`);
        return;
    }

    await createAuthor();
});
byId("cancel-button").addEventListener(
    "click",
    showAuthorsList
);


// --- UI State Management Functions ---

/* Hides the create form and shows the author list */
function showAuthorsList() {
    hide(actionForms.id);
    show(authorsList.id);
    show(resultsSeparator.id);
    showAllAuthorsButton.disabled = false;
    showCreateAuthorFormButton.disabled = false;
}

/* Hides the author list and shows the create form */
function showCreateAuthorFormView() {
    clearCreateAuthorForm();
    hide(authorsList.id);
    hide(resultsSeparator.id);
    show(actionForms.id);
    showAllAuthorsButton.disabled = true;
    showCreateAuthorFormButton.disabled = true;
}

/* Clears the create author form fields */
function clearCreateAuthorForm() {
    createNameInput.value = '';
    createBirthdateInput.value = '';
    createBirthdateInput.type = 'text';
}

/* Clears the author list  */
function clearResults() {
    authorsList.innerHTML = "";
    hide(resultsSeparator.id);
}

/**
 * Renders a list of authors in the UI
 * @param {Array} authors - An array of author DTOs
 */
async function displayAuthors(authors) {
    clearResults();
    if (authors.length === 0) {
        await showModal("Nothing found", `No authors found for this query.`);
        return;
    }

    for (const author of authors) {
        const li = document.createElement("li");
        const a = document.createElement("a");
        // This will link to a detail author's page
        a.href = "author.html"; 
        a.innerText = author.name + (author.birthdate ? ` (b. ${author.birthdate})` : '');
        a.onclick = () => sessionStorage.setItem("authorId", author.id);
        li.append(a);
        authorsList.append(li);
    }
    show(resultsSeparator.id);
}


// --- API Call Functions ---

/**
 * Fetches authors from a given URL and displays them
 * @param {string} url - The API endpoint to fetch from
 */
async function fetchAndDisplayAuthors(url) {
    try {
        const response = await fetch(url);
        if (response.ok) {
            const data = await response.json();
            displayAuthors(Array.isArray(data) ? data : [data]);
        } else {
            clearResults();
            await showModal("Error", `${response.status} ${response.statusText}`);
        }
    } catch (error) {
        clearResults();
        await showModal("Error", `A network error occurred.`);
    }
}

/* Handles the creation of a new author via POST request */
async function createAuthor() {
    const author = {
        name: createNameInput.value,
        birthdate: createBirthdateInput.value
    };

    try {
        const response = await fetch("/api/authors", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(author),
        });

        if (response.ok) {
            await showModal("Success", `Author '${author.name}' created successfully!`);
            clearCreateAuthorForm();
            showAuthorsList();
            await fetchAndDisplayAuthors("/api/authors");
        } else {
            const errorData = await response.json();
            await showModal("Error", `Creation failed: ${errorData.message}`);
        }
    } catch (error) {
        await showModal("Error", "A network error occurred while creating the author.");
    }
}