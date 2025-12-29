"use strict";

/**
 * A shorthand for document.getElementById.
 * @param {string} id The id of the element to find.
 * @returns {HTMLElement} The found element.
 */
export function byId(id) {
    return document.getElementById(id);
}

/**
 * Makes an element visible by removing its 'hidden' class (property of display:none).
 * @param {string} id The id of the element to show.
 */
export function show(id) {
    const element = byId(id);
    if (element) {
        element.classList.remove('hidden');
    }
}

/**
 * Hides an element by setting its 'hidden' class (property of display:none).
 * @param {string} id The id of the element to hide.
 */
export function hide(id) {
    const element = byId(id);
    if (element) {
        element.classList.add('hidden');
    }
}

/**
 * Sets the textContent of an element.
 * @param {string} id The id of the element.
 * @param {string} text The text to set.
 */
export function setText(id, text) {
    const element = byId(id);
    if (element) {
        element.textContent = text;
    }
}

/**
 * Displays a custom modal dialog.
 * @param {string} title - The title of the modal.
 * @param {string} message - The message to display in the modal.
 * @param {Array<{text: string, class: string, value: any}>} buttons - An array of button objects.
 * @returns {Promise<any>} A promise that resolves with the 'value' of the clicked button.
 */
export function showModal(title, message, buttons = []) {
    return new Promise(resolve => {

        const modalTitle = byId('modal-title');
        const modalMessage = byId('modal-message');
        const modalButtons = byId('modal-buttons');

        modalButtons.innerHTML = '';

        modalTitle.textContent = title;
        modalMessage.textContent = message;

        if (buttons.length === 0) {
            buttons.push({ text: 'OK', class: 'btn-primary', value: true });
        }

        buttons.forEach(buttonInfo => {
            const button = document.createElement('button');
            button.textContent = buttonInfo.text;
            button.className = `side-button ${buttonInfo.class || ''}`;
            button.onclick = () => {
                hide('modal-overlay');
                resolve(buttonInfo.value);
            };
            modalButtons.append(button);
        });

        // Show modal
        show('modal-overlay');
    });
}
