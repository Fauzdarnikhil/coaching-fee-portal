console.log("Company A Coaching Fee Portal loaded.");

const API_URL = "/api/accounts/dashboard";

const elements = {
    totalFees: document.getElementById("total-fees"),
    collectedFees: document.getElementById("collected-fees"),
    outstandingFees: document.getElementById("outstanding-fees"),
    overdueFees: document.getElementById("overdue-fees"),

    tableBody: document.getElementById("fee-table-body"),
    search: document.getElementById("student-search"),
    statusFilter: document.getElementById("status-filter"),
    refreshButton: document.getElementById("refresh-btn"),
    exportButton: document.getElementById("export-btn"),
    recordCount: document.getElementById("record-count")
};

let feeLedger = [];

const currencyFormatter = new Intl.NumberFormat("en-IN", {
    style: "currency",
    currency: "INR",
    maximumFractionDigits: 0
});

function formatCurrency(amount) {
    return currencyFormatter.format(Number(amount || 0));
}

function formatDate(dateString) {
    if (!dateString) return "—";

    const date = new Date(
        /^\d{4}-\d{2}-\d{2}$/.test(dateString)
            ? `${dateString}T00:00:00`
            : dateString
    );

    if (Number.isNaN(date.getTime())) return "—";

    return date.toLocaleDateString("en-IN", {
        day: "2-digit",
        month: "short",
        year: "numeric"
    });
}

function escapeHTML(value) {
    return String(value ?? "").replace(/[&<>"']/g, character => ({
        "&": "&amp;",
        "<": "&lt;",
        ">": "&gt;",
        '"': "&quot;",
        "'": "&#039;"
    })[character]);
}

function getAccessToken() {
    return sessionStorage.getItem("accessToken") ||
        localStorage.getItem("accessToken");
}

function renderSummary(data) {
    elements.totalFees.textContent = formatCurrency(data.totalFees);
    elements.collectedFees.textContent = formatCurrency(data.collectedFees);
    elements.outstandingFees.textContent = formatCurrency(data.outstandingFees);
    elements.overdueFees.textContent = formatCurrency(data.overdueFees);
}

/* Keep backend status separate from overdue calculation. */
function isOverdue(row) {
    return Number(row.daysOverdue) > 0 &&
        Number(row.outstandingAmount) > 0;
}

function formatDateTime(dateString) {
    if (!dateString) return "—";

    const date = new Date(dateString);

    if (Number.isNaN(date.getTime())) return "—";

    return date.toLocaleString("en-IN", {
        day: "2-digit",
        month: "short",
        year: "numeric",
        hour: "2-digit",
        minute: "2-digit"
    });
}

function getDisplayStatus(row) {
    if (isOverdue(row)) {
        return "OVERDUE";
    }

    return row.status;
}

function getStatusClass(status) {
    switch (status) {
        case "PAID":
            return "paid";

        case "OVERDUE":
            return "overdue";

        case "PARTIALLY_PAID":
            return "partial";

        case "UPCOMING":
            return "upcoming";

        default:
            return "upcoming";
    }
}

/* Dashboard ledger table */
function renderTable(rows) {

    if (!rows.length) {
        elements.tableBody.innerHTML = `
            <tr>
                <td colspan="8" class="empty-state">
                    No fee records found.
                </td>
            </tr>
        `;

        elements.recordCount.textContent = "0 records";
        return;
    }

    elements.tableBody.innerHTML = rows.map(row => {

        const displayStatus = getDisplayStatus(row);

        const overdueText = isOverdue(row)
            ? `<div class="overdue-hint">
                    ${Number(row.daysOverdue)} days overdue
               </div>`
            : "";

        return `
            <tr>
                <td>
                    <button
                        type="button"
                        class="student-name-link"
                        data-student-id="${escapeHTML(row.studentId)}"
                    >
                        ${escapeHTML(row.studentName)}
                    </button>

                    <div class="secondary-text">
                        ${escapeHTML(row.studentCode)}
                    </div>
                </td>

                <td>${escapeHTML(row.batch)}</td>

                <td>
                    Instalment ${escapeHTML(row.instalmentNumber)}
                </td>

                <td>
                    ${formatDate(row.dueDate)}
                    ${overdueText}
                </td>

                <td>${formatCurrency(row.amountDue)}</td>

                <td>${formatCurrency(row.amountPaid)}</td>

                <td class="amount-outstanding">
                    ${formatCurrency(row.outstandingAmount)}
                </td>

                <td>
                    <span class="status-badge ${getStatusClass(displayStatus)}">
                        ${escapeHTML(
                            String(displayStatus || "").replaceAll("_", " ")
                        )}
                    </span>
                </td>
            </tr>
        `;

    }).join("");

    elements.recordCount.textContent =
        `${rows.length} record${rows.length === 1 ? "" : "s"}`;
}

/* Search and filters */
function filterLedger() {

    const searchTerm = elements.search.value.trim().toLowerCase();
    const selectedStatus = elements.statusFilter.value;

    const filteredRows = feeLedger.filter(row => {

        const matchesSearch =
            String(row.studentName || "").toLowerCase().includes(searchTerm) ||
            String(row.studentCode || "").toLowerCase().includes(searchTerm) ||
            String(row.batch || "").toLowerCase().includes(searchTerm);

        let matchesStatus = true;

        if (selectedStatus === "OVERDUE") {
            matchesStatus = isOverdue(row);
        } else if (selectedStatus) {
            matchesStatus = row.status === selectedStatus;
        }

        return matchesSearch && matchesStatus;
    });

    renderTable(filteredRows);
}

function showError(message) {

    elements.tableBody.innerHTML = `
        <tr>
            <td colspan="8" class="empty-state">
                ${escapeHTML(message)}
            </td>
        </tr>
    `;

    elements.recordCount.textContent = "Unable to load records";
}

/* Load accounts dashboard */
async function loadDashboard() {

    elements.refreshButton.disabled = true;
    elements.refreshButton.textContent = "Loading...";

    try {

        const token = getAccessToken();

        const headers = {
            "Content-Type": "application/json"
        };

        if (token) {
            headers.Authorization = `Bearer ${token}`;
        }

        const response = await fetch(API_URL, {
            method: "GET",
            headers
        });

        if (response.status === 401 || response.status === 403) {
            throw new Error(
                "Authentication failed. Please provide a valid demo JWT."
            );
        }

        if (!response.ok) {
            throw new Error(`Server error: ${response.status}`);
        }

        const data = await response.json();

        feeLedger = Array.isArray(data.feeLedger)
            ? data.feeLedger
            : [];

        renderSummary(data);
        filterLedger();

        console.log("Dashboard data loaded:", data);

    } catch (error) {

        console.error("Dashboard loading failed:", error);

        showError(
            error.message || "Unable to load dashboard data."
        );

    } finally {

        elements.refreshButton.disabled = false;
        elements.refreshButton.textContent = "Refresh";
    }
}

/* Export CSV */
function exportCSV() {

    if (!feeLedger.length) {
        alert("No fee records available to export.");
        return;
    }

    const headers = [
        "Student Name",
        "Student Code",
        "Batch",
        "Academic Year",
        "Instalment",
        "Due Date",
        "Amount Due",
        "Amount Paid",
        "Outstanding",
        "Status",
        "Days Overdue"
    ];

    const rows = feeLedger.map(row => [
        row.studentName,
        row.studentCode,
        row.batch,
        row.academicYear,
        row.instalmentNumber,
        row.dueDate,
        row.amountDue,
        row.amountPaid,
        row.outstandingAmount,
        getDisplayStatus(row),
        row.daysOverdue
    ]);

    const csvContent = [headers, ...rows]
        .map(row =>
            row.map(value =>
                `"${String(value ?? "").replace(/"/g, '""')}"`
            ).join(",")
        )
        .join("\n");

    const blob = new Blob(
        ["\uFEFF" + csvContent],
        { type: "text/csv;charset=utf-8;" }
    );

    const url = URL.createObjectURL(blob);
    const link = document.createElement("a");

    link.href = url;
    link.download = "company-a-fee-ledger.csv";
    link.click();

    URL.revokeObjectURL(url);
}

/* =====================================
   STUDENT DETAIL VIEW
===================================== */

const dashboardView = document.getElementById("dashboard-view");
const studentDetailView = document.getElementById("student-detail-view");

function showStudentDetailState(state) {

    document.getElementById("student-detail-loading").hidden =
        state !== "loading";

    document.getElementById("student-detail-error").hidden =
        state !== "error";

    document.getElementById("student-detail-content").hidden =
        state !== "success";
}

/* Back to dashboard */
function showDashboard() {

    studentDetailView.hidden = true;
    dashboardView.hidden = false;

    document.getElementById("page-title").textContent = "Fee Overview";
    document.getElementById("breadcrumb-current").textContent = "Overview";
}

document.getElementById("back-to-dashboard")
    .addEventListener("click", showDashboard);

/* Event delegation for dynamically generated student buttons */
elements.tableBody.addEventListener("click", event => {

    const button = event.target.closest("[data-student-id]");

    if (!button) return;

    openStudentDetail(button.dataset.studentId);
});

/* Fetch student details */
async function openStudentDetail(studentId) {

    dashboardView.hidden = true;
    studentDetailView.hidden = false;

    document.getElementById("page-title").textContent = "Student Profile";
    document.getElementById("breadcrumb-current").textContent = "Student Profile";

    showStudentDetailState("loading");

    try {

        const token = getAccessToken();

        if (!token) {
            throw new Error(
                "Your session has expired. Please log in again."
            );
        }

        const response = await fetch(
            `/api/accounts/students/${encodeURIComponent(studentId)}`,
            {
                headers: {
                    Authorization: `Bearer ${token}`
                }
            }
        );

        if (!response.ok) {

            if (response.status === 401 || response.status === 403) {
                throw new Error(
                    "You are not authorised to view this student."
                );
            }

            if (response.status === 404) {
                throw new Error("Student record not found.");
            }

            throw new Error(
                "Unable to load student details. Please try again."
            );
        }

        const student = await response.json();

        renderStudentDetail(student);

        showStudentDetailState("success");

    } catch (error) {

        const errorElement =
            document.getElementById("student-detail-error");

        errorElement.textContent = error.message;

        showStudentDetailState("error");
    }
}

/* Render student profile */
function renderStudentDetail(student) {

    const feePlans = student.feePlans || [];
    const payments = student.paymentHistory || [];

    const instalments = feePlans.flatMap(
        plan => plan.instalments || []
    );

    const totalNetFee = feePlans.reduce(
        (sum, plan) => sum + Number(plan.netFee || 0),
        0
    );

    const totalPaid = instalments.reduce(
        (sum, item) => sum + Number(item.amountPaid || 0),
        0
    );

    const totalOutstanding = instalments.reduce(
        (sum, item) => sum + Number(item.outstandingAmount || 0),
        0
    );

    const totalOverdue = instalments.reduce(
        (sum, item) => sum + (
            Number(item.daysOverdue || 0) > 0
                ? Number(item.outstandingAmount || 0)
                : 0
        ),
        0
    );

    document.getElementById("detail-avatar").textContent =
        (student.studentName || "S")
            .trim()
            .charAt(0)
            .toUpperCase();

    // FIX: API currently returns StudentId with capital S.
    document.getElementById("detail-student-id").textContent =
        student.StudentId ?? student.studentId ?? student.id ?? "—";

    document.getElementById("detail-student-meta").textContent =
        `${student.studentCode || "—"} · ${student.batch || "—"}`;

    const activeStatus =
        document.getElementById("detail-active-status");

    activeStatus.textContent =
        student.active ? "Active" : "Inactive";

    activeStatus.className =
        `status-badge ${
            student.active ? "status-paid" : "status-overdue"
        }`;

    document.getElementById("detail-net-fee").textContent =
        formatCurrency(totalNetFee);

    document.getElementById("detail-paid").textContent =
        formatCurrency(totalPaid);

    document.getElementById("detail-outstanding").textContent =
        formatCurrency(totalOutstanding);

    document.getElementById("detail-overdue").textContent =
        formatCurrency(totalOverdue);

    // Other student information
    document.getElementById("detail-student-code").textContent =
        student.studentCode || "—";

    document.getElementById("detail-centre").textContent =
        student.centreName || "—";

    document.getElementById("detail-batch").textContent =
        student.batch || "—";

    renderFeePlans(feePlans);
    renderPaymentHistory(payments);
}

/* Student detail status styling */
function getDetailStatusClass(status) {

    const normalized = String(status || "").toUpperCase();

    if (
        normalized === "PAID" ||
        normalized === "SUCCESS"
    ) {
        return "status-paid";
    }

    if (
        normalized === "OVERDUE" ||
        normalized === "FAILED"
    ) {
        return "status-overdue";
    }

    if (
        normalized === "PARTIALLY_PAID" ||
        normalized === "PENDING"
    ) {
        return "status-partial";
    }

    return "status-upcoming";
}

/* Render fee plans and instalments */
function renderFeePlans(feePlans) {

    const container =
        document.getElementById("detail-fee-plans");

    if (!feePlans.length) {

        container.innerHTML = `
            <p class="detail-empty">
                No fee plans found for this student.
            </p>
        `;

        return;
    }

    container.innerHTML = feePlans.map(plan => {

        const instalments = plan.instalments || [];

        return `
            <div class="fee-plan-block">

                <div class="fee-plan-heading">
                    <div>
                        <h3>
                            Academic year ${escapeHTML(plan.academicYear)}
                        </h3>

                        <p>
                            Total ${formatCurrency(plan.totalFee)}
                            · Discount ${formatCurrency(plan.discountAmount)}
                        </p>
                    </div>

                    <strong>${formatCurrency(plan.netFee)}</strong>
                </div>

                <div class="table-scroll">

                    <table class="detail-table">

                        <thead>
                            <tr>
                                <th>INSTALMENT</th>
                                <th>DUE DATE</th>
                                <th>AMOUNT</th>
                                <th>PAID</th>
                                <th>BALANCE</th>
                                <th>DAYS OVERDUE</th>
                                <th>STATUS</th>
                            </tr>
                        </thead>

                        <tbody>

                            ${instalments.map(item => {

                                const overdue =
                                    Number(item.daysOverdue || 0) > 0 &&
                                    Number(item.outstandingAmount || 0) > 0;

                                const displayStatus = overdue
                                    ? "OVERDUE"
                                    : item.status;

                                return `
                                    <tr>

                                        <td>
                                            Instalment ${escapeHTML(item.instalmentNumber)}
                                        </td>

                                        <td>${formatDate(item.dueDate)}</td>

                                        <td>${formatCurrency(item.amountDue)}</td>

                                        <td>${formatCurrency(item.amountPaid)}</td>

                                        <td>${formatCurrency(item.outstandingAmount)}</td>

                                        <td>
                                            ${overdue
                                                ? escapeHTML(item.daysOverdue)
                                                : "—"}
                                        </td>

                                        <td>
                                            <span class="status-badge ${getDetailStatusClass(displayStatus)}">
                                                ${escapeHTML(
                                                    String(displayStatus || "—")
                                                        .replaceAll("_", " ")
                                                )}
                                            </span>
                                        </td>

                                    </tr>
                                `;

                            }).join("")}

                        </tbody>
                    </table>

                </div>
            </div>
        `;

    }).join("");
}

/* Render payment history */
function renderPaymentHistory(payments) {

    const tbody =
        document.getElementById("detail-payment-history");

    if (!payments.length) {

        tbody.innerHTML = `
            <tr>
                <td colspan="7" class="detail-empty">
                    No payment history available.
                </td>
            </tr>
        `;

        return;
    }

    tbody.innerHTML = payments.map(payment => {

        return `
            <tr>

                <td>
                    <strong>
                        ${escapeHTML(payment.transactionReference || "—")}
                    </strong>

                    <small class="table-subtext">
                        Payment #${escapeHTML(payment.paymentId)}
                    </small>
                </td>

                <td>
                    ${formatDateTime(payment.receivedAt || payment.createdAt)}
                </td>

                <td>
                    ${escapeHTML(payment.paymentMethod || "—")}
                </td>

                <td>
                    ${formatCurrency(payment.amountReceived)}
                </td>

                <td>
                    ${formatCurrency(payment.allocatedAmount)}
                </td>

                <td>
                    ${formatCurrency(payment.unallocatedAmount)}
                </td>

                <td>
                    <span class="status-badge ${getDetailStatusClass(payment.paymentStatus)}">
                        ${escapeHTML(payment.paymentStatus || "—")}
                    </span>
                </td>

            </tr>
        `;

    }).join("");
}

/* Event listeners */
elements.refreshButton.addEventListener("click", loadDashboard);
elements.exportButton.addEventListener("click", exportCSV);
elements.search.addEventListener("input", filterLedger);
elements.statusFilter.addEventListener("change", filterLedger);

/* Initial dashboard load */
loadDashboard();