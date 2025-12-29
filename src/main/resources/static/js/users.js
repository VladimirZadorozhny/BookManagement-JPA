"use strict";
import { byId, show, hide, setText, showModal } from "./util.js";

// --- DOM Elements ---
const actionForms = byId("action-forms");
const usersList = byId("users-list");
const resultsSeparator = byId("results-separator");
const userSidebarNav = byId("user-sidebar-nav");

// Static Form Elements (referencing the containers and their inputs/buttons)

// Create User Form
const createUserFormStatic = byId("create-user-form-static");
const createUserNameInput = byId("create-user-name");
const createUserEmailInput = byId("create-user-email");
const cancelCreateUserButton = byId("cancel-create-user");

// Find User Form
const findUserFormStatic = byId("find-user-form-static");
const findUserTitle = byId("find-user-title");
const searchUserLabel = byId("search-user-label");
const searchUserInput = byId("search-user-input");
const searchUserButton = byId("search-user-button");
const cancelSearchUserButton = byId("cancel-search-user");

const findUserDropdownButton = document.querySelector(".dropdown .dropbtn"); // Reference the main dropdown button
const findUserDropdownContainer = document.querySelector(".dropdown"); // Reference the main dropdown container


// --- State ---
let pendingAction = null;

// --- Initial Page Load Logic ---
await checkPendingAction();
if (!pendingAction) {
    // Standard event listeners if no action is pending
    byId("show-all-users").addEventListener("click", () => fetchAndDisplayUsers("/api/users"));
    byId("show-by-id").addEventListener("click", () => showUserSearchFormView("ID"));
    byId("show-by-name-email").addEventListener("click", () => showUserSearchFormView("Name or Email"));
    byId("show-create-user-form").addEventListener("click", showCreateUserFormView);
}

// --- Event Listeners for Static Forms ---
// Create User Form
createUserFormStatic.addEventListener("submit", async event => {
    event.preventDefault(); // Prevent default form submission

    // Client-side validation
    if (createUserNameInput.value.trim() === '') {
        await showModal("Error", `User's name cannot be blank.`);
        return;
    }
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(createUserEmailInput.value)) {
        await showModal("Error", `Please enter a valid email address.`);
        return;
    }
    await createUser();
});
cancelCreateUserButton.addEventListener("click", showUsersList);

// Find User Form
findUserFormStatic.addEventListener("submit", async event => {
    event.preventDefault();
    const value = searchUserInput.value;
    if (value.trim() === '') { // Double check for empty after disabling native validation
        await showModal("Error", `Search input cannot be empty.`);
        return;
    }

    // Sidebar remains visible
    if (findUserTitle.textContent.includes("ID")) { // Check original type of search
        await fetchAndDisplayUsers(`/api/users/${value}`);
    } else {
        await fetchAndDisplayUsers(`/api/users/search?by=${encodeURIComponent(value)}`);
    }
    showUsersList(); // Transition back to user list view after search
});

// Enable/disable search button based on input
searchUserInput.addEventListener('input', () => {
    searchUserButton.disabled = searchUserInput.value.trim() === '';
});
cancelSearchUserButton.addEventListener("click", showUsersList);


// --- UI and Action Flow Functions ---

/* Checks for a pending action (like renting) in sessionStorage */
async function checkPendingAction() {
    const actionData = sessionStorage.getItem("pendingAction");
    if (actionData) {
        pendingAction = JSON.parse(actionData);
        if (pendingAction.action === 'rent') {
            hide(userSidebarNav.id);
            await showModal(`Select a User to Rent: '${pendingAction.bookTitle}`, `Please select a user from the list below to complete the rental.` );
            // actionForms.innerHTML = `<h3>Select a User to Rent: '${pendingAction.bookTitle}'</h3><p>Please select a user from the list below to complete the rental.</p>`;
            await fetchAndDisplayUsers("/api/users");
        }
    }
}

/* Clears all forms and results list */
function clearAllForms() {
    hide(createUserFormStatic.id);
    hide(findUserFormStatic.id);
}

/* Clears the user list  */
function clearResults() {
    usersList.innerHTML = "";
    hide(resultsSeparator.id);
}

/* Renders a list of users in the UI, adapting behavior for pending actions */
async function displayUsers(users) {
    clearResults();
    if (users.length === 0) {
        await showModal("Not found", `No users found.`);
        return;
    }

    for (const user of users) {
        const li = document.createElement("li");
        const a = document.createElement("a");
        a.innerText = `${user.name} (${user.email})`;

        if (pendingAction && pendingAction.action === 'rent') {
            a.href = "#"; // Prevent navigation for rent action
            a.addEventListener('click', async (event) => {
                event.preventDefault();
                await rentBookToUser(user.id, user.name);
            });
        } else {
            a.href = "user.html";
            a.addEventListener('click', () => {
                sessionStorage.setItem("userId", user.id);
            });
        }
        li.append(a);
        usersList.append(li);
    }
    show(resultsSeparator.id);
}

/* Shows the form for creating a new user */
function showCreateUserFormView() {
    clearAllForms();
    hide(usersList.id);
    hide(resultsSeparator.id);
    show(createUserFormStatic.id);
    createUserNameInput.value = ''; // Clear inputs
    createUserEmailInput.value = '';
    
    // Enable/disable sidebar buttons
    byId("show-all-users").disabled = true;
    if (findUserDropdownContainer) findUserDropdownContainer.classList.add("blocked");
    if (findUserDropdownButton) findUserDropdownButton.disabled = true;
    byId("show-create-user-form").disabled = true;
    show(userSidebarNav.id);
}

// Function to reset users view after create/cancel
function showUsersList() {
    clearAllForms();
    show(usersList.id);
    show(resultsSeparator.id);
    byId("show-all-users").disabled = false;
    if (findUserDropdownContainer) findUserDropdownContainer.classList.remove("blocked");
    if (findUserDropdownButton) findUserDropdownButton.disabled = false;
    byId("show-create-user-form").disabled = false;
    show(userSidebarNav.id);
}

/* Shows a generic search form for a given filter type */
function showUserSearchFormView(filterType) {
    clearAllForms();
    findUserTitle.textContent = `Find User by ${filterType}`;
    searchUserLabel.textContent = `${filterType}:`;
    searchUserInput.value = '';
    searchUserButton.disabled = true;

    hide(usersList.id);
    hide(resultsSeparator.id);
    show(findUserFormStatic.id);

    byId("show-all-users").disabled = true;
    if (findUserDropdownContainer) findUserDropdownContainer.classList.add("blocked");
    if (findUserDropdownButton) findUserDropdownButton.disabled = true;
    byId("show-create-user-form").disabled = true;
    show(userSidebarNav.id);
}

// --- API Call Functions ---

/* Fetches users from a given URL and displays them */
async function fetchAndDisplayUsers(url) {
    try {
        const response = await fetch(url);
        if (response.ok) {
            const data = await response.json();
            displayUsers(Array.isArray(data) ? data : [data]);
        } else {
            clearResults();
            const errorData = await response.json();
            if (response.status === 404) {
                await showModal("Not found", `No user found for your search.`);
            } else {
                await showModal("Error", `Error: ${response.status} ${errorData.message || response.statusText}`);
            }
        }
    } catch (error) {
        clearResults();
        await showModal("Error", "A network error occurred.");
    }
}

/* Handles the creation of a new user via POST request */
async function createUser() {
    const user = {
        name: createUserNameInput.value,
        email: createUserEmailInput.value,
    };

    try {
        const response = await fetch("/api/users", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(user),
        });

        if (response.ok) {
            await showModal("Success", `User '${user.name}' created successfully!`);
            clearAllForms();
            showUsersList();
            await fetchAndDisplayUsers("/api/users");
        } else {
            const errorData = await response.json();
            await showModal("Error", `Creation failed: ${errorData.message}`);
        }
    } catch (error) {
        await showModal("Error", "A network error occurred while creating the user.");
    }
}

/* Handles the renting of a book to a specific user */
async function rentBookToUser(userId, userName) {

    const confirmed = await showModal("Confirm Rental", `Rent '${pendingAction.bookTitle}' to ${userName}?`, [
        { text: "Yes, Rent", class: "btn-primary", value: true },
        { text: "Cancel", class: "btn-secondary", value: false }
    ]);

    if (!confirmed) {
        return;
    }

    const rentRequest = {
        bookId: pendingAction.bookId
    };

    try {
        const response = await fetch(`/api/users/${userId}/rent`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(rentRequest)
        });

        if (response.ok) {
            await showModal("Success", `Book '${pendingAction.bookTitle}' rented to ${userName} successfully!`);
            sessionStorage.removeItem("pendingAction");
            sessionStorage.setItem("userId", userId);
            sessionStorage.setItem("showBorrowedBooks", "true");
            window.location.href = "user.html";
        } else {
            const errorData = await response.json();
            await showModal("Error", `Rent failed: ${errorData.message}`);
            sessionStorage.removeItem("pendingAction");
        }
    } catch (error) {
        await showModal("Error", "A network error occurred during the rent process.");
        sessionStorage.removeItem("pendingAction");
    }
}

// In case when user leaves the page without choosing the "user to rent a book"
window.addEventListener("beforeunload", () => {
    if (sessionStorage.getItem("pendingAction")) {
        sessionStorage.removeItem("pendingAction");
    }
})