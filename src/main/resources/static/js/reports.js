import { byId, show, hide, showModal } from "./util.js";

// --- DOM Elements ---
const reportTitle = byId("report-title");
const reportContent = byId("report-content");
const paginationControlsDiv = byId("pagination-controls");
const prevPageButton = byId("prev-page");
const nextPageButton = byId("next-page");
const pageInfoSpan = byId("page-info");

// Report type buttons
const reportButtons = {
    all: byId("show-all"),
    active: byId("show-active"),
    returned: byId("show-returned"),
    fines: byId("show-fines"),
    "unpaid-fines": byId("show-unpaid-fines"),
};

// Filter-specific elements
const heavyUsersButton = byId("show-heavy-users");
const heavyUsersFilterBox = byId("heavy-users-filter-box");
const heavyUsersForm = byId("heavy-users-form");
const heavyUsersCountInput = byId("heavy-users-count");

const dueSoonButton = byId("show-due-soon");
const dueSoonFilterBox = byId("due-soon-filter-box");
const dueSoonForm = byId("due-soon-form");
const dueSoonDaysInput = byId("due-soon-days");


// --- State ---
let currentReportType = "";
let currentPage = 0;
let totalPages = 0;
let currentFilters = {};


// --- Enum mapping ---
const reportTypeMap = {
  "heavy-users": "HEAVY_USERS",
  "due-soon": "DUE_SOON",
  "unpaid-fines": "UNPAID_FINES"
};

// --- Event Listeners ---

// Event listeners for simple reports (without extra filter) and correct enum mapping between UI and backend
Object.entries(reportButtons).forEach(([type, button]) => {
    reportTypeMap[type] ??= type.toUpperCase();
    button.addEventListener("click", () => setupReport(type, button.innerText));
});

// Report with a filter input: Heavy Users
heavyUsersButton.addEventListener("click", () => {
    currentReportType = "heavy-users";
    reportTitle.innerText = "Heavy Users Report";
    hideAllFilters();
    show(heavyUsersFilterBox.id);
    clearReportContent();
});

heavyUsersForm.addEventListener("submit", (e) => {
    e.preventDefault();
    const count = parseInt(heavyUsersCountInput.value, 10);
    if (isNaN(count) || count < 1) {
        showModal("Validation Error", "Please enter a valid number greater than 0.");
        return;
    }
    currentFilters = { minActiveBooks: count };
    setupReport("heavy-users", `Users with > ${count} Books`, false);
});

// Report with a filter input: Due Soon
dueSoonButton.addEventListener("click", () => {
    currentReportType = "due-soon";
    reportTitle.innerText = "Due Soon Report";
    hideAllFilters();
    show(dueSoonFilterBox.id);
    clearReportContent();
});

dueSoonForm.addEventListener("submit", (e) => {
    e.preventDefault();
    const days = parseInt(dueSoonDaysInput.value, 10);
    if (isNaN(days) || days < 1) {
        showModal("Validation Error", "Please enter a valid number of days greater than 0.");
        return;
    }
    currentFilters = { days: days };
    setupReport("due-soon", `Due in the next ${days} days`, false);
});


// Pagination
prevPageButton.addEventListener("click", () => {
    if (currentPage > 0) {
        currentPage--;
        fetchAndDisplayReport();
    }
});

nextPageButton.addEventListener("click", () => {
    if (currentPage < totalPages - 1) {
        currentPage++;
        fetchAndDisplayReport();
    }
});

// --- Helper Functions ---
function hideAllFilters() {
    hide(heavyUsersFilterBox.id);
    hide(dueSoonFilterBox.id);
}

function clearReportContent() {
    reportContent.innerHTML = "<p>Please apply a filter.</p>";
    hide(paginationControlsDiv.id);
}

// --- Main Logic Functions ---

function setupReport(type, title, hideFilter = true) {
    currentReportType = type;
    reportTitle.innerText = title;
    currentPage = 0;
    currentFilters = (type === 'heavy-users' || type === 'due-soon') ? currentFilters : {};

    if (hideFilter) {
        hideAllFilters();
    }
    
    fetchAndDisplayReport();
}

async function fetchAndDisplayReport() {
    reportContent.classList.add("loading");

    const params = new URLSearchParams({
        type: reportTypeMap[currentReportType],
        page: currentPage,
        size: 10,
        ...currentFilters
    });

    const url = `/api/reports/bookings?${params.toString()}`;

    try {
        const response = await fetch(url);
        if (response.ok) {
            const pageData = await response.json();
            updatePagination(pageData); // Update pagination first
            renderTable(pageData.content); // Then render the table
        } else {
            const error = await response.json();
            reportContent.innerHTML = `<p class="fout">Error loading report: ${error.message || response.statusText}</p>`;
            hide(paginationControlsDiv.id);
            await showModal("Error", `Error loading report: ${error.message}`);
        }
    } catch (e) {
        reportContent.innerHTML = `<p class="fout">A network error occurred.</p>`;
        hide(paginationControlsDiv.id);
        await showModal("Error", "A network error occurred while loading report.");
    } finally {
        reportContent.classList.remove("loading");
    }
}

function renderTable(bookings) {
    reportContent.innerHTML = ""; // Clear previous content

    if (!bookings || bookings.length === 0) {
        reportContent.innerHTML = "<p>No records found for this report.</p>";
        return;
    }

    const table = document.createElement("table");
    table.className = "report-table";

    const headers = ["User", "Email", "Book", "Borrowed", "Returned", "Overdue", "Fine", "Fine Paid", "Status"];
    const thead = table.createTHead();
    const headerRow = thead.insertRow();
    headers.forEach(text => {
        const th = document.createElement("th");
        th.innerText = text;
        headerRow.appendChild(th);
    });

    const tbody = table.createTBody();
    bookings.forEach(b => {
        const row = tbody.insertRow();
        const { userName, userEmail, bookTitle, borrowedAt, returnedAt, overdueDays, fine, finePaid } = b;


        row.insertCell().innerText = userName;
        row.insertCell().innerText = userEmail;
        row.insertCell().innerText = bookTitle;
        row.insertCell().innerText = new Date(borrowedAt).toLocaleDateString();
        row.insertCell().innerText = returnedAt ? new Date(returnedAt).toLocaleDateString() : "-";
        

        const overdueCell = row.insertCell();
        overdueCell.innerText = overdueDays > 0 ? `${overdueDays} days` : "-";
        if (overdueDays > 0) {
            overdueCell.classList.add("status-overdue");
        }


        const fineCell = row.insertCell();
        fineCell.innerText = fine && fine > 0 ? `$${fine.toFixed(2)}` : "-";
        if (fine > 0) {
            fineCell.classList.add("status-overdue");
        }
        

        const finePaidCell = row.insertCell();
        const finePaidText = finePaid ? "Yes" : (fine > 0 ? "No" : "-");
        finePaidCell.innerText = finePaidText;
        if (finePaidText === "No") {
            finePaidCell.classList.add("status-overdue");
        }


        let statusText = returnedAt ? "Returned" : "Active";
        if (overdueDays > 0) statusText += ", Overdue";
        if (fine > 0) statusText += finePaid ? ", Fine Paid" : ", Unpaid Fine";

        const hasUnresolvedIssues = (!returnedAt && overdueDays > 0) || (fine > 0 && !finePaid);
        const statusCell = row.insertCell();
        statusCell.innerText = statusText;
        if (hasUnresolvedIssues) {
            statusCell.classList.add("status-overdue");
        }
    });

    reportContent.appendChild(table);
}

function updatePagination(pageData) {
    // set totalPages to 0 if the first page has no content.
    if (pageData.number === 0 && pageData.totalElements === 0) {
        totalPages = 0;
        hide(paginationControlsDiv.id);
    } else {
        totalPages = pageData.totalPages;
        currentPage = pageData.number;
        pageInfoSpan.innerText = `Page ${currentPage + 1} of ${totalPages}`;
        prevPageButton.disabled = (currentPage === 0);
        nextPageButton.disabled = (currentPage >= totalPages - 1);
        show(paginationControlsDiv.id);
    }
}
