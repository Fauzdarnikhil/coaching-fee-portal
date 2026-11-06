
const API_BASE_URL = "/api/parent";
const TOKEN_KEY = "accessToken";

const elements = {
    loadingState: document.getElementById("loading-state"),
    errorState: document.getElementById("error-state"),
    errorMessage: document.getElementById("error-message"),
    dashboardContent: document.getElementById("dashboard-content"),
    parentName: document.getElementById("parent-name"),
    welcomeName: document.getElementById("welcome-name"),
    parentAvatar: document.getElementById("parent-avatar"),
    currentDate: document.getElementById("current-date"),
    childrenContainer: document.getElementById("children-container"),
    totalFees: document.getElementById("total-fees"),
    amountPaid: document.getElementById("amount-paid"),
    outstandingBalance: document.getElementById("outstanding-balance"),
    overviewInstalmentsBody: document.getElementById("overview-instalments-body"),
    feePlansContainer: document.getElementById("fee-plans-container"),
    paymentHistoryContainer: document.getElementById("payment-history-container"),
    academicSection: document.getElementById("academic-section"),
    retryButton: document.getElementById("retry-btn"),
    logoutButton: document.getElementById("logout-btn"),
    menuToggle: document.getElementById("menu-toggle"),
    sidebar: document.getElementById("sidebar"),
    sidebarOverlay: document.getElementById("sidebar-overlay"),
    paymentModal: document.getElementById("payment-modal"),
    closePaymentModal: document.getElementById("close-payment-modal"),
    paymentForm: document.getElementById("payment-form"),
    paymentStudent: document.getElementById("payment-student"),
    paymentInstalment: document.getElementById("payment-instalment"),
    paymentOutstanding: document.getElementById("payment-outstanding"),
    paymentAmount: document.getElementById("payment-amount"),
    paymentMethod: document.getElementById("payment-method"),
    paymentSubmitButton: document.getElementById("payment-submit-btn"),
    paymentMessage: document.getElementById("payment-message")
};

let dashboardData = null;
let paymentHistoryData = [];
let paymentHistoryError = null;
let academicData = [];
let academicError = null;
let selectedPayment = null;
let pendingPaymentId = null;
let isLoading = false;
let isProcessingPayment = false;

function getToken() {
    return sessionStorage.getItem(TOKEN_KEY);
}

function logout() {
    sessionStorage.removeItem(TOKEN_KEY);
    window.location.replace("/parent-login.html");
}

async function apiRequest(url, options = {}) {
    const token = getToken();

    if (!token) {
        logout();
        throw new Error("Your session has expired. Please log in again.");
    }

    const response = await fetch(url, {
        ...options,
        headers: {
            "Content-Type": "application/json",
            "Authorization": `Bearer ${token}`,
            ...(options.headers || {})
        }
    });

    const responseText = await response.text();
    let data = {};

    if (responseText) {
        try {
            data = JSON.parse(responseText);
        } catch {
            data = { message: responseText };
        }
    }

    if (response.status === 401) {
        logout();
        throw new Error("Your session has expired. Please log in again.");
    }

    if (!response.ok) {
        throw new Error(
            data.message || "Something went wrong. Please try again."
        );
    }

    return data;
}

function formatCurrency(amount) {
    return new Intl.NumberFormat("en-IN", {
        style: "currency",
        currency: "INR",
        maximumFractionDigits: 2
    }).format(Number(amount || 0));
}

function formatDate(dateValue) {
    if (!dateValue) return "—";

    const parts = String(dateValue).split("-");

    if (parts.length !== 3) return "—";

    const date = new Date(
        Number(parts[0]),
        Number(parts[1]) - 1,
        Number(parts[2])
    );

    return date.toLocaleDateString("en-IN", {
        day: "2-digit",
        month: "short",
        year: "numeric"
    });
}

function formatDateTime(dateValue) {
    if (!dateValue) return "—";

    const date = new Date(dateValue);

    if (Number.isNaN(date.getTime())) return "—";

    return date.toLocaleString("en-IN", {
        day: "2-digit",
        month: "short",
        year: "numeric",
        hour: "2-digit",
        minute: "2-digit"
    });
}

function getInitial(name) {
    if (!name) return "P";
    return String(name).trim().charAt(0).toUpperCase();
}

function escapeHTML(value) {
    return String(value ?? "").replace(/[&<>"']/g, character => {
        const entities = {
            "&": "&amp;",
            "<": "&lt;",
            ">": "&gt;",
            '"': "&quot;",
            "'": "&#39;"
        };

        return entities[character];
    });
}

function getAllInstalments(children) {
    const result = [];

    children.forEach(child => {
        (child.feePlans || []).forEach(plan => {
            (plan.instalments || []).forEach(instalment => {
                result.push({
                    ...instalment,
                    studentId: child.studentId,
                    studentName: child.studentName,
                    academicYear: plan.academicYear,
                    feePlanId: plan.feePlanId
                });
            });
        });
    });

    return result;
}

function getStatusClass(status) {
    switch (status) {
        case "PAID":
            return "status-paid";
        case "OVERDUE":
            return "status-overdue";
        case "PARTIALLY_PAID":
            return "status-partial";
        case "UPCOMING":
            return "status-upcoming";
        default:
            return "status-upcoming";
    }
}

function getStatusLabel(status) {
    switch (status) {
        case "PAID":
            return "Paid";
        case "OVERDUE":
            return "Overdue";
        case "PARTIALLY_PAID":
            return "Partially Paid";
        case "UPCOMING":
            return "Upcoming";
        default:
            return status || "Unknown";
    }
}

function showLoading() {
    elements.loadingState.hidden = false;
    elements.errorState.hidden = true;
    elements.dashboardContent.hidden = true;
}

function showDashboard() {
    elements.loadingState.hidden = true;
    elements.errorState.hidden = true;
    elements.dashboardContent.hidden = false;
}

function showError(message) {
    elements.loadingState.hidden = true;
    elements.dashboardContent.hidden = true;
    elements.errorState.hidden = false;
    elements.errorMessage.textContent = message;
}

async function loadDashboard() {
    if (isLoading) return;

    isLoading = true;
    showLoading();

    try {
        dashboardData = await apiRequest(
            `${API_BASE_URL}/dashboard`,
            {
                method: "GET"
            }
        );

        try {
            paymentHistoryData = await apiRequest(
                `${API_BASE_URL}/payments/history`,
                {
                    method: "GET"
                }
            );

            paymentHistoryError = null;
        } catch (error) {
            console.error("Payment history error:", error);
            paymentHistoryData = [];
            paymentHistoryError = error.message;
        }

        try {
            const academicResponse = await apiRequest(
                `${API_BASE_URL}/academic`,
                {
                    method: "GET"
                }
            );

            academicData = Array.isArray(academicResponse.children)
                ? academicResponse.children
                : [];

            academicError = null;
        } catch (error) {
            console.error("Academic records error:", error);
            academicData = [];
            academicError = error.message;
        }

        renderDashboard();
        showDashboard();

    } catch (error) {
        console.error("Dashboard error:", error);
        showError(error.message);

    } finally {
        isLoading = false;
    }
}

function renderDashboard() {
    if (!dashboardData) return;

    const parentName = dashboardData.parentName || "Parent";
    const children = dashboardData.children || [];

    elements.parentName.textContent = parentName;
    elements.welcomeName.textContent = parentName;
    elements.parentAvatar.textContent = getInitial(parentName);

    elements.currentDate.textContent =
        new Date().toLocaleDateString("en-IN", {
            weekday: "short",
            day: "2-digit",
            month: "long",
            year: "numeric"
        });

    renderChildren(children);
    renderFeeSummary(children);
    renderOverviewInstalments(children);
    renderFeePlans(children);
    renderPaymentHistory();
    renderAcademicSection();
}

function renderChildren(children) {
    if (children.length === 0) {
        elements.childrenContainer.innerHTML = `
            <div class="child-card">
                <p>No students are linked to your account.</p>
            </div>
        `;
        return;
    }

    elements.childrenContainer.innerHTML = children.map(child => `
        <article class="child-card">
            <div class="child-card-header">
                <div class="child-avatar">
                    ${escapeHTML(getInitial(child.studentName))}
                </div>
                <div>
                    <h3>${escapeHTML(child.studentName)}</h3>
                    <p>
                        Student ID:
                        ${escapeHTML(child.studentCode)}
                    </p>
                </div>
            </div>
            <div class="child-info">
                <span>
                    ${escapeHTML(child.batchName || "Batch not assigned")}
                </span>
                <span>
                    ${escapeHTML(child.relationship || "Parent")}
                </span>
            </div>
        </article>
    `).join("");
}

function renderFeeSummary(children) {
    let totalFees = 0;
    let totalPaid = 0;
    let totalOutstanding = 0;

    children.forEach(child => {
        (child.feePlans || []).forEach(plan => {
            totalFees += Number(plan.netFee || 0);

            (plan.instalments || []).forEach(instalment => {
                totalPaid += Number(instalment.amountPaid || 0);
                totalOutstanding += Number(
                    instalment.outstandingAmount || 0
                );
            });
        });
    });

    elements.totalFees.textContent = formatCurrency(totalFees);
    elements.amountPaid.textContent = formatCurrency(totalPaid);
    elements.outstandingBalance.textContent =
        formatCurrency(totalOutstanding);
}

function renderOverviewInstalments(children) {
    const instalments = getAllInstalments(children);

    const visibleInstalments = instalments
        .filter(item => Number(item.outstandingAmount || 0) > 0)
        .sort((a, b) =>
            String(a.dueDate).localeCompare(String(b.dueDate))
        );

    if (visibleInstalments.length === 0) {
        elements.overviewInstalmentsBody.innerHTML = `
            <tr>
                <td colspan="7" class="empty-cell">
                    No outstanding instalments. All fees are paid!
                </td>
            </tr>
        `;
        return;
    }

    elements.overviewInstalmentsBody.innerHTML =
        visibleInstalments.map(instalment => `
            <tr>
                <td>
                    ${escapeHTML(instalment.studentName)}
                </td>
                <td>
                    Instalment ${escapeHTML(instalment.instalmentNumber)}
                </td>
                <td>
                    ${formatDate(instalment.dueDate)}
                </td>
                <td>
                    ${formatCurrency(instalment.amountDue)}
                </td>
                <td>
                    <strong>
                        ${formatCurrency(instalment.outstandingAmount)}
                    </strong>
                </td>
                <td>
                    <span class="status-badge ${getStatusClass(instalment.status)}">
                        ${getStatusLabel(instalment.status)}
                    </span>
                </td>
                <td>
                    <button
                        class="pay-button"
                        data-payment-action="open"
                        data-student-id="${escapeHTML(instalment.studentId)}"
                        data-instalment-id="${escapeHTML(instalment.instalmentId)}"
                    >
                        Pay Now
                    </button>
                </td>
            </tr>
        `).join("");
}

function renderFeePlans(children) {
    if (children.length === 0) {
        elements.feePlansContainer.innerHTML = `
            <div class="fee-plan-card">
                No fee plans are available.
            </div>
        `;
        return;
    }

    let html = "";

    children.forEach(child => {
        (child.feePlans || []).forEach(plan => {
            const instalments = plan.instalments || [];

            const rows = instalments.map(instalment => {
                const outstanding = Number(
                    instalment.outstandingAmount || 0
                );

                const payButton = outstanding > 0
                    ? `
                        <button
                            class="pay-button"
                            data-payment-action="open"
                            data-student-id="${escapeHTML(child.studentId)}"
                            data-instalment-id="${escapeHTML(instalment.instalmentId)}"
                        >
                            Pay Now
                        </button>
                    `
                    : `<span class="status-badge status-paid">Paid</span>`;

                return `
                    <tr>
                        <td>
                            Instalment ${escapeHTML(instalment.instalmentNumber)}
                        </td>
                        <td>
                            ${formatDate(instalment.dueDate)}
                        </td>
                        <td>
                            ${formatCurrency(instalment.amountDue)}
                        </td>
                        <td>
                            ${formatCurrency(instalment.amountPaid)}
                        </td>
                        <td>
                            ${formatCurrency(instalment.outstandingAmount)}
                        </td>
                        <td>
                            <span class="status-badge ${getStatusClass(instalment.status)}">
                                ${getStatusLabel(instalment.status)}
                            </span>
                        </td>
                        <td>${payButton}</td>
                    </tr>
                `;
            }).join("");

            html += `
                <article class="fee-plan-card">
                    <div class="fee-plan-header">
                        <div>
                            <h3>${escapeHTML(child.studentName)}</h3>
                            <p>
                                Academic Year ${escapeHTML(plan.academicYear)}
                                · ${escapeHTML(child.batchName || "")}
                            </p>
                        </div>
                    </div>

                    <div class="fee-plan-summary">
                        <div>
                            <span>Total Fee</span>
                            <strong>${formatCurrency(plan.totalFee)}</strong>
                        </div>
                        <div>
                            <span>Discount</span>
                            <strong>${formatCurrency(plan.discountAmount)}</strong>
                        </div>
                        <div>
                            <span>Net Fee</span>
                            <strong>${formatCurrency(plan.netFee)}</strong>
                        </div>
                    </div>

                    <div class="table-container">
                        <table>
                            <thead>
                                <tr>
                                    <th>Instalment</th>
                                    <th>Due Date</th>
                                    <th>Amount Due</th>
                                    <th>Paid</th>
                                    <th>Outstanding</th>
                                    <th>Status</th>
                                    <th>Action</th>
                                </tr>
                            </thead>
                            <tbody>
                                ${rows}
                            </tbody>
                        </table>
                    </div>
                </article>
            `;
        });
    });

    elements.feePlansContainer.innerHTML = html || `
        <div class="fee-plan-card">
            No fee plans are available.
        </div>
    `;
}

function getPaymentStatusClass(status) {
    switch (status) {
        case "SUCCESS":
            return "status-paid";
        case "PENDING":
            return "status-upcoming";
        case "FAILED":
            return "status-overdue";
        default:
            return "status-upcoming";
    }
}

function getPaymentStatusLabel(status) {
    switch (status) {
        case "SUCCESS":
            return "Successful";
        case "PENDING":
            return "Pending";
        case "FAILED":
            return "Failed";
        default:
            return status || "Unknown";
    }
}

function getAllocationLabel(payment) {
    if (payment.paymentStatus !== "SUCCESS") {
        return "Not applicable";
    }

    const allocated = Number(payment.allocatedAmount || 0);
    const unallocated = Number(payment.unallocatedAmount || 0);

    if (unallocated > 0 && allocated > 0) {
        return "Partially allocated";
    }

    if (unallocated > 0) {
        return "Awaiting allocation";
    }

    return "Fully allocated";
}

function renderPaymentHistory() {
    if (paymentHistoryError) {
        elements.paymentHistoryContainer.innerHTML = `
            <div class="payment-history-card">
                <h3>Unable to load payment history</h3>
                <p>${escapeHTML(paymentHistoryError)}</p>
                <button class="pay-button" data-payment-history-retry>
                    Retry
                </button>
            </div>
        `;
        return;
    }

    if (!Array.isArray(paymentHistoryData) ||
        paymentHistoryData.length === 0) {
        elements.paymentHistoryContainer.innerHTML = `
            <div class="payment-history-card">
                <h3>No payment records yet</h3>
                <p>Your payment transactions will appear here.</p>
            </div>
        `;
        return;
    }

    const rows = paymentHistoryData.map(payment => {
        const statusClass = getPaymentStatusClass(
            payment.paymentStatus
        );

        const statusLabel = getPaymentStatusLabel(
            payment.paymentStatus
        );

        const allocationLabel = getAllocationLabel(payment);

        return `
            <tr>
                <td>
                    <strong>${escapeHTML(payment.studentName)}</strong>
                    <br>
                    <small>${escapeHTML(payment.studentCode)}</small>
                </td>
                <td>
                    ${escapeHTML(payment.transactionReference)}
                </td>
                <td>
                    <strong>
                        ${formatCurrency(payment.amountReceived)}
                    </strong>
                </td>
                <td>
                    ${escapeHTML(payment.paymentMethod)}
                </td>
                <td>
                    ${formatDateTime(payment.paymentDate)}
                </td>
                <td>
                    <span class="status-badge ${statusClass}">
                        ${statusLabel}
                    </span>
                </td>
                <td>
                    ${formatCurrency(payment.allocatedAmount)}
                </td>
                <td>
                    ${formatCurrency(payment.unallocatedAmount)}
                </td>
                <td>
                    ${escapeHTML(allocationLabel)}
                </td>
            </tr>
        `;
    }).join("");

    elements.paymentHistoryContainer.innerHTML = `
        <div class="payment-history-card">
            <h3>Payment records</h3>
            <p>
                View all payments made for your linked students.
            </p>
            <div class="table-container">
                <table>
                    <thead>
                        <tr>
                            <th>Student</th>
                            <th>Transaction ID</th>
                            <th>Amount</th>
                            <th>Method</th>
                            <th>Date</th>
                            <th>Status</th>
                            <th>Allocated</th>
                            <th>Unallocated</th>
                            <th>Allocation Status</th>
                        </tr>
                    </thead>
                    <tbody>
                        ${rows}
                    </tbody>
                </table>
            </div>
        </div>
    `;
}

function getAcademicStatusClass(status) {
    switch (String(status || "").toUpperCase()) {
        case "PRESENT":
            return "status-paid";
        case "LATE":
            return "status-partial";
        case "ABSENT":
            return "status-overdue";
        default:
            return "status-upcoming";
    }
}

function renderAcademicSection() {
    if (!elements.academicSection) return;

    let container = elements.academicSection.querySelector("[data-academic-content]");

    if (!container) {
        container = document.createElement("div");
        container.dataset.academicContent = "true";
        elements.academicSection.appendChild(container);
    }

    if (academicError) {
        container.innerHTML = `
            <div class="payment-history-card">
                <h3>Unable to load academic records</h3>
                <p>${escapeHTML(academicError)}</p>
                <button class="pay-button" data-academic-retry>Retry</button>
            </div>
        `;
        return;
    }

    if (!academicData.length) {
        container.innerHTML = `
            <div class="payment-history-card">
                <h3>No academic records available</h3>
                <p>Attendance and test results will appear here when they are uploaded by the institute.</p>
            </div>
        `;
        return;
    }

    container.innerHTML = academicData.map(child => {
        const attendance = Array.isArray(child.attendance) ? child.attendance : [];
        const testResults = Array.isArray(child.testResults) ? child.testResults : [];
        const percentage = Number(child.attendancePercentage || 0);
        const safePercentage = Math.min(100, Math.max(0, percentage));

        const attendanceRows = attendance.length
            ? attendance.map(record => `
                <tr>
                    <td>${formatDate(record.date)}</td>
                    <td>
                        <span class="status-badge ${getAcademicStatusClass(record.status)}">
                            ${escapeHTML(record.status)}
                        </span>
                    </td>
                </tr>
            `).join("")
            : `
                <tr>
                    <td colspan="2" class="empty-cell">
                        No attendance records available.
                    </td>
                </tr>
            `;

        const testRows = testResults.length
            ? testResults.map(result => `
                <tr>
                    <td>${escapeHTML(result.testName)}</td>
                    <td>${escapeHTML(result.subject)}</td>
                    <td>${formatDate(result.testDate)}</td>
                    <td>
                        ${Number(result.marksObtained || 0).toFixed(1)}
                        /
                        ${Number(result.maximumMarks || 0).toFixed(1)}
                    </td>
                    <td>
                        <strong>${Number(result.percentage || 0).toFixed(1)}%</strong>
                    </td>
                </tr>
            `).join("")
            : `
                <tr>
                    <td colspan="5" class="empty-cell">
                        No test results available.
                    </td>
                </tr>
            `;

        return `
            <article class="payment-history-card academic-child-card">
                <div class="fee-plan-header">
                    <div>
                        <h3>${escapeHTML(child.studentName)}</h3>
                        <p>
                            ${escapeHTML(child.studentCode)}
                            ·
                            ${escapeHTML(child.batch || "Batch not assigned")}
                        </p>
                    </div>
                </div>

                <div class="fee-plan-summary academic-summary">
                    <div>
                        <span>Attendance</span>
                        <strong>${percentage.toFixed(1)}%</strong>
                    </div>
                    <div>
                        <span>Total Classes</span>
                        <strong>${Number(child.totalClasses || 0)}</strong>
                    </div>
                    <div>
                        <span>Present</span>
                        <strong>${Number(child.presentClasses || 0)}</strong>
                    </div>
                    <div>
                        <span>Absent</span>
                        <strong>${Number(child.absentClasses || 0)}</strong>
                    </div>
                    <div>
                        <span>Late</span>
                        <strong>${Number(child.lateClasses || 0)}</strong>
                    </div>
                </div>

                <div class="academic-progress-track"
                     role="progressbar"
                     aria-label="Attendance percentage"
                     aria-valuemin="0"
                     aria-valuemax="100"
                     aria-valuenow="${safePercentage}">
                    <div
                        class="academic-progress-fill"
                        style="width: ${safePercentage}%"
                    ></div>
                </div>

                <div class="academic-subsection">
                    <h3>Attendance history</h3>
                    <div class="table-container">
                        <table>
                            <thead>
                                <tr>
                                    <th>Date</th>
                                    <th>Status</th>
                                </tr>
                            </thead>
                            <tbody>
                                ${attendanceRows}
                            </tbody>
                        </table>
                    </div>
                </div>

                <div class="academic-subsection">
                    <h3>Test results</h3>
                    <div class="table-container">
                        <table>
                            <thead>
                                <tr>
                                    <th>Test</th>
                                    <th>Subject</th>
                                    <th>Date</th>
                                    <th>Marks</th>
                                    <th>Score</th>
                                </tr>
                            </thead>
                            <tbody>
                                ${testRows}
                            </tbody>
                        </table>
                    </div>
                </div>
            </article>
        `;
    }).join("");
}

document.addEventListener("click", event => {
    if (event.target.closest("[data-payment-history-retry]")) {
        loadDashboard();
    }

    if (event.target.closest("[data-academic-retry]")) {
        loadDashboard();
    }
});

function showSection(sectionName) {
    const sections = [
        "overview",
        "fees",
        "payments",
        "academic"
    ];

    if (!sections.includes(sectionName)) return;

    sections.forEach(section => {
        const sectionElement =
            document.getElementById(`${section}-section`);

        if (sectionElement) {
            sectionElement.hidden = section !== sectionName;
        }
    });

    document.querySelectorAll(".nav-item").forEach(item => {
        item.classList.toggle(
            "active",
            item.dataset.section === sectionName
        );
    });

    const titles = {
        overview: "Dashboard",
        fees: "Fee Details",
        payments: "Payment History",
        academic: "Academic Progress"
    };

    document.querySelector(".topbar-title h2").textContent =
        titles[sectionName];

    closeSidebar();
}

document.querySelectorAll(".nav-item").forEach(item => {
    item.addEventListener("click", event => {
        event.preventDefault();
        showSection(item.dataset.section);
    });
});

document.querySelectorAll("[data-navigate]").forEach(button => {
    button.addEventListener("click", () => {
        showSection(button.dataset.navigate);
    });
});

function openSidebar() {
    elements.sidebar.classList.add("open");
    elements.sidebarOverlay.classList.add("show");
}

function closeSidebar() {
    elements.sidebar.classList.remove("open");
    elements.sidebarOverlay.classList.remove("show");
}

elements.menuToggle.addEventListener("click", openSidebar);
elements.sidebarOverlay.addEventListener("click", closeSidebar);

function showPaymentMessage(message, type = "error") {
    elements.paymentMessage.textContent = message;
    elements.paymentMessage.className = `payment-message ${type}`;
    elements.paymentMessage.hidden = false;
}

function clearPaymentMessage() {
    elements.paymentMessage.textContent = "";
    elements.paymentMessage.className = "payment-message";
    elements.paymentMessage.hidden = true;
}

function resetPaymentModal() {
    selectedPayment = null;
    pendingPaymentId = null;
    isProcessingPayment = false;

    elements.paymentForm.reset();

    elements.paymentSubmitButton.disabled = false;
    elements.paymentSubmitButton.textContent = "Continue to Payment";

    clearPaymentMessage();
}

function closePaymentModal() {
    elements.paymentModal.hidden = true;
    resetPaymentModal();
}

function openPaymentModal(studentId, instalmentId) {
    const children = dashboardData?.children || [];

    let selectedChild = null;
    let selectedInstalment = null;

    for (const child of children) {
        if (String(child.studentId) !== String(studentId)) {
            continue;
        }

        for (const plan of child.feePlans || []) {
            const instalment = (plan.instalments || []).find(
                item => String(item.instalmentId) === String(instalmentId)
            );

            if (instalment) {
                selectedChild = child;
                selectedInstalment = instalment;
                break;
            }
        }

        if (selectedInstalment) break;
    }

    if (!selectedChild || !selectedInstalment) {
        alert("Unable to find the selected instalment.");
        return;
    }

    const outstanding = Number(
        selectedInstalment.outstandingAmount || 0
    );

    if (outstanding <= 0) {
        alert("This instalment has no outstanding balance.");
        return;
    }

    resetPaymentModal();

    selectedPayment = {
        studentId: selectedChild.studentId,
        instalmentId: selectedInstalment.instalmentId,
        studentName: selectedChild.studentName,
        instalmentNumber: selectedInstalment.instalmentNumber,
        outstandingAmount: outstanding
    };

    elements.paymentStudent.textContent = selectedChild.studentName;

    elements.paymentInstalment.textContent =
        `Instalment ${selectedInstalment.instalmentNumber}`;

    elements.paymentOutstanding.textContent =
        formatCurrency(outstanding);

    elements.paymentAmount.value = outstanding;
    elements.paymentAmount.max = outstanding;

    elements.paymentModal.hidden = false;
}

document.addEventListener("click", event => {
    const button = event.target.closest(
        '[data-payment-action="open"]'
    );

    if (!button) return;

    openPaymentModal(
        button.dataset.studentId,
        button.dataset.instalmentId
    );
});

elements.closePaymentModal.addEventListener(
    "click",
    closePaymentModal
);

elements.paymentModal.addEventListener("click", event => {
    if (event.target === elements.paymentModal) {
        closePaymentModal();
    }
});

async function initiatePayment() {
    const amount = Number(elements.paymentAmount.value);
    const paymentMethod = elements.paymentMethod.value;

    if (!selectedPayment) {
        throw new Error("Please select an instalment first.");
    }

    if (!Number.isFinite(amount) || amount <= 0) {
        throw new Error("Enter a valid payment amount.");
    }

    if (amount > selectedPayment.outstandingAmount) {
        throw new Error(
            "Payment amount cannot exceed the outstanding balance."
        );
    }

    const requestBody = {
        instalmentId: selectedPayment.instalmentId,
        amount: amount,
        paymentMethod: paymentMethod
    };

    const response = await apiRequest(
        `${API_BASE_URL}/payments/initiate`,
        {
            method: "POST",
            body: JSON.stringify(requestBody)
        }
    );

    if (!response.paymentId) {
        throw new Error("Payment ID was not returned by the server.");
    }

    pendingPaymentId = response.paymentId;

    elements.paymentSubmitButton.textContent =
        "Simulate Successful Payment";

    showPaymentMessage(
        `Payment ${response.transactionReference || ""} has been initiated. ` +
        "This is demo mode. Click the button again to simulate successful payment.",
        "success"
    );
}

async function simulatePaymentSuccess() {
    if (!pendingPaymentId) {
        throw new Error("No pending payment was found.");
    }

    const response = await apiRequest(
        `${API_BASE_URL}/payments/${pendingPaymentId}/mock-success`,
        {
            method: "POST"
        }
    );

    if (response.status && response.status !== "SUCCESS") {
        throw new Error(
            response.message || "Payment has not been confirmed."
        );
    }

    showPaymentMessage(
        response.message || "Demo payment completed successfully.",
        "success"
    );

    elements.paymentSubmitButton.disabled = true;
    elements.paymentSubmitButton.textContent = "Payment Successful";

    await loadDashboard();

    window.setTimeout(() => {
        closePaymentModal();
    }, 1500);
}

elements.paymentForm.addEventListener("submit", async event => {
    event.preventDefault();

    if (isProcessingPayment) return;

    isProcessingPayment = true;
    elements.paymentSubmitButton.disabled = true;

    clearPaymentMessage();

    try {
        if (!pendingPaymentId) {
            elements.paymentSubmitButton.textContent =
                "Initiating Payment...";

            await initiatePayment();
        } else {
            elements.paymentSubmitButton.textContent =
                "Processing Demo Payment...";

            await simulatePaymentSuccess();
        }
    } catch (error) {
        console.error("Payment error:", error);

        showPaymentMessage(
            error.message || "Unable to process payment.",
            "error"
        );

        elements.paymentSubmitButton.textContent =
            pendingPaymentId
                ? "Simulate Successful Payment"
                : "Continue to Payment";
    } finally {
        isProcessingPayment = false;

        if (elements.paymentSubmitButton.textContent !== "Payment Successful") {
            elements.paymentSubmitButton.disabled = false;
        }
    }
});

elements.retryButton.addEventListener("click", loadDashboard);
elements.logoutButton.addEventListener("click", logout);

function initializeDashboard() {
    if (!getToken()) {
        logout();
        return;
    }

    loadDashboard();
}

initializeDashboard();
