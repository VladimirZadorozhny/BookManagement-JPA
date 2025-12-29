"use strict";
import { byId, show, hide, setText, showModal } from "./util.js";

// --- DOM Elements ---
const staticDetailsDiv = byId("static-details");
const editFormContainerDiv = byId("edit-form-container");
const editButton = byId("edit-button");
const deleteButton = byId("delete-button");
const saveButton = byId("save-button");
const cancelButton = byId("cancel-button");
const editAuthorForm = byId("edit-author-form");

const editNameInput = byId("edit-name");
const editBirthdateInput = byId("edit-birthdate");

let currentAuthorData = {};
let originalAuthorData = {};

// --- Functions to toggle form visibility ---
function showStaticDetails() {
    hide(editFormContainerDiv.id);
    show(staticDetailsDiv.id);
}

function showEditForm() {
    show(editFormContainerDiv.id);
    hide(staticDetailsDiv.id);

    originalAuthorData = { ...currentAuthorData };

    // Pre-fill the form with current author data
    editNameInput.value = currentAuthorData.name;
    editBirthdateInput.value = currentAuthorData.birthdate;
    checkFormChanges();
}

// --- Change Detection for Save Button ---
function checkFormChanges() {
    const isNameChanged = editNameInput.value !== originalAuthorData.name;
    const isBirthdateChanged = editBirthdateInput.value !== (originalAuthorData.birthdate || '');
    
    const hasChanges = isNameChanged || isBirthdateChanged;

    saveButton.disabled = !hasChanges;
    if (!hasChanges) {
        saveButton.title = "No changes to save";
    } else {
        saveButton.title = "";
    }
}

// --- Event Listeners ---
editButton.addEventListener("click", showEditForm);
deleteButton.addEventListener("click", async () => await deleteAuthor());
cancelButton.addEventListener("click", () => {
    editBirthdateInput.type = 'text'; // Reset type on cancel
    showStaticDetails();
});
editAuthorForm.addEventListener("submit", async (event) => {
    event.preventDefault();
    await updateAuthor();
});
editNameInput.addEventListener("input", checkFormChanges);
editBirthdateInput.addEventListener("input", checkFormChanges);



const authorId = sessionStorage.getItem("authorId");

if (!authorId) {
    await showModal("Not found", `No author selected. Please return to the authors list.`);

} else {
    await fetchAuthorDetails();
}

async function fetchAuthorDetails() {
    try {
        const response = await fetch(`/api/authors/${authorId}`);
        if (response.ok) {
            currentAuthorData = await response.json();
            setText("name", currentAuthorData.name);
            setText("birthdate", currentAuthorData.birthdate);
            showStaticDetails();
        } else {
            await showModal("Not found", `Author with ID ${authorId} not found.`);

        }
    } catch (error) {
        await showModal("Error", `A network error occurred while fetching author details.`);

    }
}

// --- Update Author Function ---
async function updateAuthor() {

    if (editNameInput.value.trim() === '') {
        await showModal("Error", `Author's name cannot be blank.`);
        return;
    }

    if (editBirthdateInput.value === '') {
        await showModal("Error", `Author's birthdate cannot be empty.`);
        return;
    }
    // Frontend validation for birthdate (future date)
    const birthdate = new Date(editBirthdateInput.value);
    const today = new Date();
    today.setHours(0, 0, 0, 0); 

    if (birthdate > today) {
        await showModal("Error", `Author's birthdate cannot be in the future.`);
        return;
    }

    const updatedAuthor = {
        name: editNameInput.value,
        birthdate: editBirthdateInput.value
    };

    try {
        const response = await fetch(`/api/authors/${authorId}`, {
            method: "PUT",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(updatedAuthor),
        });

        if (response.ok) {
            await showModal("Success", `Author '${updatedAuthor.name}' updated successfully!`);
            await fetchAuthorDetails();
            editBirthdateInput.type = 'text';
            showStaticDetails();
        } else {
            const errorData = await response.json();
            await showModal("Error", `Update failed: ${errorData.message}`);
        }
    } catch (error) {
        await showModal("Error", "A network error occurred while updating the author.");
    }
}

// --- Delete Author Function ---
async function deleteAuthor() {
    const confirmed = await showModal("Confirm Deletion", "Are you sure you want to delete this author?", [
        { text: "Yes, Delete", class: "btn-danger", value: true },
        { text: "Cancel", class: "btn-secondary", value: false }
    ]);

    if (!confirmed) {
        return;
    }

    try {
        const response = await fetch(`/api/authors/${authorId}`, {
            method: "DELETE",
        });

        if (response.ok) {
            await showModal("Confirmation", `Deletion successful. Author with ID=${authorId} deleted.`);
            sessionStorage.removeItem("authorId");
            window.location.href = "authors.html"; // Redirect to authors list
        } else {
            const errorData = await response.json();
            await showModal("Error", `Deletion failed: ${errorData.message}`);
        }
    } catch (error) {
        await showModal("Error", "A network error occurred while deleting the author.");
    }
}
