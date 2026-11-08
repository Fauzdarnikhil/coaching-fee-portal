console.log("Company A Coaching Fee Portal loaded.");

 const API_URL = "/api/accounts/dashboard";
 const UNMATCHED_API_URL = "/api/accounts/unmatched-payments";
 const REFUND_API_URL = "/api/accounts/refunds/calculate";
 const REMINDERS_API_URL = "/api/accounts/reminders";
 const RISK_API_URL = "/api/accounts/student-risks";
 const COUNSELLOR_TASKS_API_URL = "/api/accounts/counsellor-tasks";

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

 const dashboardView = document.getElementById("dashboard-view");
 const overviewView = document.getElementById("overview-view");
 const studentsView = document.getElementById("students-view");
 const studentDetailView = document.getElementById("student-detail-view");
 const unmatchedView = document.getElementById("unmatched-view");
 const refundsView = document.getElementById("refunds-view");
 const remindersView = document.getElementById("reminders-view");
 const riskView = document.getElementById("risk-view");

 const allocationElements = {
     modal: document.getElementById("allocation-modal"),
     closeButton: document.getElementById("close-allocation-modal"),
     cancelButton: document.getElementById("cancel-allocation-btn"),
     form: document.getElementById("allocation-form"),
     studentName: document.getElementById("allocation-student-name"),
     transaction: document.getElementById("allocation-transaction"),
     receivedAmount: document.getElementById("allocation-received-amount"),
     remainingAmount: document.getElementById("allocation-remaining-amount"),
     instalment: document.getElementById("allocation-instalment"),
     instalmentBalance: document.getElementById("allocation-instalment-balance"),
     amount: document.getElementById("allocation-amount"),
     amountHelp: document.getElementById("allocation-amount-help"),
     message: document.getElementById("allocation-message"),
     confirmButton: document.getElementById("confirm-allocation-btn")
 };

 let feeLedger = [];
 let unmatchedPayments = [];
 let reminders = [];
 let studentRiskRecords = [];
 let counsellorTasks = [];

 let selectedPayment = null;
 let selectedInstalmentBalance = 0;
 let currentStudentDetailId = null;
 let selectedRefundStudent = null;

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
     return sessionStorage.getItem("accountsAccessToken");
 }

 async function apiRequest(url, options = {}) {
     const token = getAccessToken();

     if (!token) {
         sessionStorage.removeItem("accountsAccessToken");
         sessionStorage.removeItem("accountsUserName");
         window.location.replace("/accounts-login.html");
         throw new Error("Your session has expired.");
     }

     const headers = {
         Authorization: `Bearer ${token}`,
         ...(options.body ? { "Content-Type": "application/json" } : {}),
         ...options.headers
     };

     const response = await fetch(url, {
         ...options,
         headers
     });

     if (response.status === 401 || response.status === 403) {
         sessionStorage.removeItem("accountsAccessToken");
         sessionStorage.removeItem("accountsUserName");
         window.location.replace("/accounts-login.html");
         throw new Error("Your session has expired. Please log in again.");
     }

     const responseText = await response.text();
     let data = {};

     if (responseText) {
         try {
             data = JSON.parse(responseText);
         } catch {
             data = { message: responseText };
         }
     }

     if (!response.ok) {
         throw new Error(
             data.message || data.error || `Request failed with status ${response.status}`
         );
     }

     return data;
 }

 /* =========================================================
    DASHBOARD
 ========================================================= */

 function renderSummary(data) {
     elements.totalFees.textContent = formatCurrency(data.totalFees);
     elements.collectedFees.textContent = formatCurrency(data.collectedFees);
     elements.outstandingFees.textContent = formatCurrency(data.outstandingFees);
     elements.overdueFees.textContent = formatCurrency(data.overdueFees);
 }


function getAgeingBucket(daysOverdue) {
    if (daysOverdue <= 0) return null;
    if (daysOverdue <= 30) return "1to30";
    if (daysOverdue <= 60) return "31to60";
    return "61plus";
}

function aggregateLedger(rows, groupBy) {
    const groups = new Map();

    rows.forEach(row => {
        const key = row[groupBy] || (
            groupBy === "centreName"
                ? "Jaipur Main Centre"
                : "Unassigned batch"
        );

        if (!groups.has(key)) {
            groups.set(key, {
                name: key,
                totalFees: 0,
                collected: 0,
                outstanding: 0,
                overdue: 0,
                ageing: {
                    "1to30": 0,
                    "31to60": 0,
                    "61plus": 0
                }
            });
        }

        const group = groups.get(key);
        const amountDue = Number(row.amountDue || 0);
        const amountPaid = Number(row.amountPaid || 0);
        const outstanding = Number(row.outstandingAmount || 0);
        const daysOverdue = Number(row.daysOverdue || 0);

        group.totalFees += amountDue;
        group.collected += amountPaid;
        group.outstanding += outstanding;

        if (outstanding > 0 && daysOverdue > 0) {
            group.overdue += outstanding;

            const bucket = getAgeingBucket(daysOverdue);

            if (bucket) {
                group.ageing[bucket] += outstanding;
            }
        }
    });

    return [...groups.values()].sort((a, b) =>
        a.name.localeCompare(b.name)
    );
}

function renderBreakdownTable(bodyId, groups) {
    const tbody = document.getElementById(bodyId);

    if (!groups.length) {
        tbody.innerHTML = `
            <tr>
                <td colspan="8" class="empty-state">
                    No records available.
                </td>
            </tr>
        `;
        return;
    }

    tbody.innerHTML = groups.map(group => `
        <tr>
            <td><strong>${escapeHTML(group.name)}</strong></td>
            <td>${formatCurrency(group.totalFees)}</td>
            <td>${formatCurrency(group.collected)}</td>
            <td>${formatCurrency(group.outstanding)}</td>
            <td class="amount-outstanding">${formatCurrency(group.overdue)}</td>
            <td>${formatCurrency(group.ageing["1to30"])}</td>
            <td>${formatCurrency(group.ageing["31to60"])}</td>
            <td>${formatCurrency(group.ageing["61plus"])}</td>
        </tr>
    `).join("");
}

function renderOverviewBreakdowns() {
    const centreGroups = aggregateLedger(feeLedger, "centreName");
    const batchGroups = aggregateLedger(feeLedger, "batch");

    renderBreakdownTable("centre-summary-body", centreGroups);
    renderBreakdownTable("batch-summary-body", batchGroups);
}


function getStudentDirectory() {
    const students = new Map();

    feeLedger.forEach(row => {
        const id = String(row.studentId);

        if (!students.has(id)) {
            students.set(id, {
                studentId: row.studentId,
                studentName: row.studentName,
                studentCode: row.studentCode,
                batch: row.batch,
                centreName: row.centreName || "—",
                totalFees: 0,
                amountPaid: 0,
                outstandingAmount: 0
            });
        }

        const student = students.get(id);

        student.totalFees += Number(row.amountDue || 0);
        student.amountPaid += Number(row.amountPaid || 0);
        student.outstandingAmount += Number(row.outstandingAmount || 0);

        if (row.centreName) {
            student.centreName = row.centreName;
        }
    });

    return [...students.values()].sort((a, b) =>
        String(a.studentName).localeCompare(String(b.studentName))
    );
}

function renderStudentDirectory() {
    const tbody = document.getElementById("student-directory-body");
    const searchInput = document.getElementById("student-directory-search");

    const search = searchInput.value.trim().toLowerCase();

    const students = getStudentDirectory().filter(student =>
        String(student.studentName || "").toLowerCase().includes(search) ||
        String(student.studentCode || "").toLowerCase().includes(search) ||
        String(student.studentId || "").toLowerCase().includes(search)
    );

    if (!students.length) {
        tbody.innerHTML = `
            <tr>
                <td colspan="8" class="empty-state">
                    No students found.
                </td>
            </tr>
        `;
    } else {
        tbody.innerHTML = students.map(student => `
            <tr>
                <td>
                    <button
                        type="button"
                        class="student-name-link"
                        data-directory-student-id="${escapeHTML(student.studentId)}"
                    >
                        ${escapeHTML(student.studentName)}
                    </button>
                </td>
                <td>${escapeHTML(student.studentCode || student.studentId)}</td>
                <td>${escapeHTML(student.batch || "—")}</td>
                <td>${escapeHTML(student.centreName)}</td>
                <td>${formatCurrency(student.totalFees)}</td>
                <td>${formatCurrency(student.amountPaid)}</td>
                <td class="amount-outstanding">${formatCurrency(student.outstandingAmount)}</td>
                <td>
                    <button
                        type="button"
                        class="button button-secondary"
                        data-directory-student-id="${escapeHTML(student.studentId)}"
                    >
                        View profile
                    </button>
                </td>
            </tr>
        `).join("");
    }

    document.getElementById("student-directory-count").textContent =
        `${students.length} student${students.length === 1 ? "" : "s"}`;
}

function showStudents() {
    currentStudentDetailId = null;

    overviewView.hidden = true;
    dashboardView.hidden = true;
    studentsView.hidden = false;
    studentDetailView.hidden = true;
    unmatchedView.hidden = true;
    refundsView.hidden = true;
    remindersView.hidden = true;
    riskView.hidden = true;

    updatePageHeader("students");
    renderStudentDirectory();
}



 function isOverdue(row) {
     return Number(row.daysOverdue) > 0 &&
         Number(row.outstandingAmount) > 0;
 }

 function getDisplayStatus(row) {
     return isOverdue(row) ? "OVERDUE" : row.status;
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
             ? `<div class="overdue-hint">${Number(row.daysOverdue)} days overdue</div>`
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

                 <td>Instalment ${escapeHTML(row.instalmentNumber)}</td>

                 <td>${formatDate(row.dueDate)}${overdueText}</td>

                 <td>${formatCurrency(row.amountDue)}</td>

                 <td>${formatCurrency(row.amountPaid)}</td>

                 <td class="amount-outstanding">
                     ${formatCurrency(row.outstandingAmount)}
                 </td>

                 <td>
                     <span class="status-badge ${getStatusClass(displayStatus)}">
                         ${escapeHTML(String(displayStatus || "").replaceAll("_", " "))}
                     </span>
                 </td>
             </tr>
         `;
     }).join("");

     elements.recordCount.textContent =
         `${rows.length} record${rows.length === 1 ? "" : "s"}`;
 }

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

 async function loadDashboard() {
     elements.refreshButton.disabled = true;
     elements.refreshButton.textContent = "Loading...";

     try {
         const data = await apiRequest(API_URL);

         feeLedger = Array.isArray(data.feeLedger)
             ? data.feeLedger
             : [];

         renderSummary(data);
         filterLedger();
         renderOverviewBreakdowns();
         renderStudentDirectory();

     } catch (error) {
         console.error("Dashboard loading failed:", error);
         showError(error.message || "Unable to load dashboard data.");

     } finally {
         elements.refreshButton.disabled = false;
         elements.refreshButton.textContent = "Refresh";
     }
 }

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

 /* =========================================================
    STUDENT DETAILS
 ========================================================= */

 function showStudentDetailState(state) {
     document.getElementById("student-detail-loading").hidden =
         state !== "loading";

     document.getElementById("student-detail-error").hidden =
         state !== "error";

     document.getElementById("student-detail-content").hidden =
         state !== "success";
 }


function showDashboard() {
    currentStudentDetailId = null;

    dashboardView.hidden = true;
    studentsView.hidden = true;
    overviewView.hidden = false;
    studentDetailView.hidden = true;
    unmatchedView.hidden = true;
    refundsView.hidden = true;
    remindersView.hidden = true;
    riskView.hidden = true;

    updatePageHeader("dashboard");
}

function showFeeLedger() {
    currentStudentDetailId = null;

    overviewView.hidden = true;
    studentsView.hidden = true;
    dashboardView.hidden = false;
    studentDetailView.hidden = true;
    unmatchedView.hidden = true;
    refundsView.hidden = true;
    remindersView.hidden = true;
    riskView.hidden = true;

    updatePageHeader("fees");
}


 async function openStudentDetail(studentId) {
     currentStudentDetailId = studentId;

     overviewView.hidden = true;
     dashboardView.hidden = true;
     studentsView.hidden = true;
     unmatchedView.hidden = true;
     refundsView.hidden = true;
     remindersView.hidden = true;
    riskView.hidden = true;
     studentDetailView.hidden = false;

     updatePageHeader("students");
     showStudentDetailState("loading");

     try {
         const student = await apiRequest(
             `/api/accounts/students/${encodeURIComponent(studentId)}`
         );

         renderStudentDetail(student);
         showStudentDetailState("success");

     } catch (error) {
         document.getElementById("student-detail-error").textContent =
             error.message;

         showStudentDetailState("error");
     }
 }

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
         (student.studentName || student.name || "S")
             .trim()
             .charAt(0)
             .toUpperCase();

     document.getElementById("detail-student-name").textContent =
         student.studentName || student.name || "Student";

     document.getElementById("detail-student-id").textContent =
         student.StudentId ?? student.studentId ?? student.id ?? "—";

     document.getElementById("detail-student-meta").textContent =
         `${student.studentCode || "—"} · ${student.batch || "—"}`;

     const activeStatus = document.getElementById("detail-active-status");

     activeStatus.textContent = student.active ? "Active" : "Inactive";

     activeStatus.className =
         `status-badge ${student.active ? "status-paid" : "status-overdue"}`;

     document.getElementById("detail-net-fee").textContent =
         formatCurrency(totalNetFee);

     document.getElementById("detail-paid").textContent =
         formatCurrency(totalPaid);

     document.getElementById("detail-outstanding").textContent =
         formatCurrency(totalOutstanding);

     document.getElementById("detail-overdue").textContent =
         formatCurrency(totalOverdue);

     document.getElementById("detail-student-code").textContent =
         student.studentCode || "—";

     document.getElementById("detail-centre").textContent =
         student.centreName || "—";

     document.getElementById("detail-batch").textContent =
         student.batch || "—";

     renderFeePlans(feePlans);
     renderPaymentHistory(payments);
 }

 function getDetailStatusClass(status) {
     const normalized = String(status || "").toUpperCase();

     if (normalized === "PAID" || normalized === "SUCCESS") {
         return "status-paid";
     }

     if (normalized === "OVERDUE" || normalized === "FAILED") {
         return "status-overdue";
     }

     if (normalized === "PARTIALLY_PAID" || normalized === "PENDING") {
         return "status-partial";
     }

     return "status-upcoming";
 }

 function renderFeePlans(feePlans) {
     const container = document.getElementById("detail-fee-plans");

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
                         <h3>Academic year ${escapeHTML(plan.academicYear)}</h3>
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
                                         <td>Instalment ${escapeHTML(item.instalmentNumber)}</td>
                                         <td>${formatDate(item.dueDate)}</td>
                                         <td>${formatCurrency(item.amountDue)}</td>
                                         <td>${formatCurrency(item.amountPaid)}</td>
                                         <td>${formatCurrency(item.outstandingAmount)}</td>
                                         <td>${overdue ? escapeHTML(item.daysOverdue) : "—"}</td>

                                         <td>
                                             <span class="status-badge ${getDetailStatusClass(displayStatus)}">
                                                 ${escapeHTML(String(displayStatus || "—").replaceAll("_", " "))}
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

 function renderPaymentHistory(payments) {
     const tbody = document.getElementById("detail-payment-history");

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

     tbody.innerHTML = payments.map(payment => `
         <tr>
             <td>
                 <strong>${escapeHTML(payment.transactionReference || "—")}</strong>

                 <small class="table-subtext">
                     Payment #${escapeHTML(payment.paymentId)}
                 </small>
             </td>

             <td>${formatDateTime(payment.receivedAt || payment.createdAt)}</td>

             <td>${escapeHTML(payment.paymentMethod || "—")}</td>

             <td>${formatCurrency(payment.amountReceived)}</td>

             <td>${formatCurrency(payment.allocatedAmount)}</td>

             <td>${formatCurrency(payment.unallocatedAmount)}</td>

             <td>
                 <span class="status-badge ${getDetailStatusClass(payment.paymentStatus)}">
                     ${escapeHTML(payment.paymentStatus || "—")}
                 </span>
             </td>
         </tr>
     `).join("");
 }

 /* =========================================================
    PAGE NAVIGATION
 ========================================================= */

 function updatePageHeader(page) {
     const titles = {
         dashboard: ["Overview", "Fee Overview"],
         students: ["Students", "Student Profile"],
         fees: ["Fee Ledger", "Fee Ledger"],
         unmatched: ["Unmatched Payments", "Unmatched Payments"],
         reminders: ["Reminders", "Reminders"],
         refunds: ["Refunds", "Refund Calculator"],
         risk: ["Student Risk", "Student Risk"]
     };

     const [breadcrumb, title] = titles[page] || titles.dashboard;

     document.getElementById("breadcrumb-current").textContent = breadcrumb;
     document.getElementById("page-title").textContent = title;

     document.querySelectorAll(".nav-link").forEach(link => {
         link.classList.toggle("active", link.dataset.page === page);
     });
 }

 /* =========================================================
    REFUND CALCULATOR
 ========================================================= */

 function showRefunds() {
     overviewView.hidden = true;
     dashboardView.hidden = true;
     studentDetailView.hidden = true;
     studentsView.hidden = true;
     unmatchedView.hidden = true;
     remindersView.hidden = true;
    riskView.hidden = true;
     refundsView.hidden = false;

     updatePageHeader("refunds");
     populateRefundStudents();
 }

 function populateRefundStudents() {
     const studentSelect = document.getElementById("refund-student");
     const feePlanSelect = document.getElementById("refund-fee-plan");

     if (!studentSelect || !feePlanSelect) return;

     const students = new Map();

     feeLedger.forEach(row => {
         if (row.studentId == null) return;

         students.set(String(row.studentId), {
             studentId: row.studentId,
             studentName: row.studentName,
             studentCode: row.studentCode
         });
     });

     const currentValue = studentSelect.value;

     studentSelect.innerHTML = '<option value="">Select a student</option>';

     [...students.values()]
         .sort((a, b) =>
             String(a.studentName).localeCompare(String(b.studentName))
         )
         .forEach(student => {
             const option = document.createElement("option");

             option.value = student.studentId;

             option.textContent =
                 `${student.studentName} (${student.studentCode || student.studentId})`;

             studentSelect.appendChild(option);
         });

     if (currentValue && students.has(currentValue)) {
         studentSelect.value = currentValue;
         loadRefundFeePlans(currentValue);
     } else {
         feePlanSelect.innerHTML =
             '<option value="">Select a fee plan</option>';
     }
 }

 async function loadRefundFeePlans(studentId) {
     const feePlanSelect = document.getElementById("refund-fee-plan");
     const calculateButton = document.getElementById("calculate-refund-btn");

     if (!feePlanSelect) return;

     selectedRefundStudent = studentId;

     feePlanSelect.disabled = true;
     feePlanSelect.innerHTML =
         '<option value="">Loading fee plans...</option>';

     if (calculateButton) {
         calculateButton.disabled = true;
     }

     hideRefundFeedback();
     document.getElementById("refund-result").hidden = true;

     try {
         const student = await apiRequest(
             `/api/accounts/students/${encodeURIComponent(studentId)}`
         );

         const feePlans = Array.isArray(student.feePlans)
             ? student.feePlans
             : [];

         feePlanSelect.innerHTML =
             '<option value="">Select a fee plan</option>';

         feePlans.forEach(plan => {
             const planId = plan.feePlanId ?? plan.id;

             if (planId == null) return;

             const option = document.createElement("option");

             option.value = planId;

             option.textContent =
                 `${plan.academicYear || "Fee plan"} · Net fee ${formatCurrency(plan.netFee)}`;

             feePlanSelect.appendChild(option);
         });

         if (!feePlans.length) {
             feePlanSelect.innerHTML =
                 '<option value="">No fee plans found</option>';
         }

     } catch (error) {
         feePlanSelect.innerHTML =
             '<option value="">Unable to load fee plans</option>';

         showRefundError(
             error.message || "Unable to load this student's fee plans."
         );

     } finally {
         feePlanSelect.disabled = false;

         if (calculateButton) {
             calculateButton.disabled = !feePlanSelect.value;
         }
     }
 }

 function hideRefundFeedback() {
     const loading = document.getElementById("refund-loading");
     const error = document.getElementById("refund-error");

     if (loading) {
         loading.hidden = true;
     }

     if (error) {
         error.hidden = true;
         error.textContent = "";
     }
 }

 function showRefundError(message) {
     const error = document.getElementById("refund-error");

     if (!error) return;

     error.textContent = message;
     error.hidden = false;
 }

 function renderRefundResult(result) {
     document.getElementById("refund-result-student").textContent =
         result.studentName || "—";

     document.getElementById("refund-result-policy").textContent =
         `${result.policyName || "—"} · v${result.policyVersion || "—"}`;

     document.getElementById("refund-result-date").textContent =
         formatDate(result.withdrawalDate);

     document.getElementById("refund-course-start").textContent =
         formatDate(result.courseStartDate);

     document.getElementById("refund-course-end").textContent =
         formatDate(result.courseEndDate);

     document.getElementById("refund-total-days").textContent =
         Number(result.totalCourseDays || 0).toLocaleString("en-IN");

     document.getElementById("refund-remaining-days").textContent =
         Number(result.remainingCourseDays || 0).toLocaleString("en-IN");

     document.getElementById("refund-total-paid").textContent =
         formatCurrency(result.totalEligiblePaid);

     document.getElementById("refund-pro-rata").textContent =
         formatCurrency(result.proRataRefund);

     document.getElementById("refund-admin-fee").textContent =
         formatCurrency(result.administrativeFee);

     document.getElementById("refund-final-amount").textContent =
         formatCurrency(result.finalRefundAmount);

     document.getElementById("refund-processing-deadline").textContent =
         formatDate(result.processingDeadline);

     document.getElementById("refund-result").hidden = false;
 }

 async function calculateRefund(event) {
     event.preventDefault();

     const feePlanId = document.getElementById("refund-fee-plan").value;

     const withdrawalDate =
         document.getElementById("refund-withdrawal-date").value;

     const calculateButton =
         document.getElementById("calculate-refund-btn");

     const loading = document.getElementById("refund-loading");
     const resultContainer = document.getElementById("refund-result");

     hideRefundFeedback();
     resultContainer.hidden = true;

     if (!feePlanId || !withdrawalDate) {
         showRefundError(
             "Select a student, fee plan, and withdrawal date."
         );
         return;
     }

     calculateButton.disabled = true;
     calculateButton.textContent = "Calculating...";
     loading.hidden = false;

     try {
         const result = await apiRequest(
             `${REFUND_API_URL}?feePlanId=${encodeURIComponent(feePlanId)}&withdrawalDate=${encodeURIComponent(withdrawalDate)}`
         );

         renderRefundResult(result);

     } catch (error) {
         showRefundError(
             error.message || "Unable to calculate the refund."
         );

     } finally {
         loading.hidden = true;
         calculateButton.disabled = false;
         calculateButton.textContent = "Calculate refund";
     }
 }

 /* =========================================================
    UNMATCHED PAYMENTS
 ========================================================= */

 function showUnmatchedPayments() {
     overviewView.hidden = true;
     dashboardView.hidden = true;
     studentDetailView.hidden = true;
     studentsView.hidden = true;
     refundsView.hidden = true;
     remindersView.hidden = true;
    riskView.hidden = true;
     unmatchedView.hidden = false;

     updatePageHeader("unmatched");
     loadUnmatchedPayments();
 }

 async function loadUnmatchedPayments() {
     const refreshButton =
         document.getElementById("refresh-unmatched-btn");

     const tableBody =
         document.getElementById("unmatched-table-body");

     const loading =
         document.getElementById("unmatched-loading");

     const errorElement =
         document.getElementById("unmatched-error");

     refreshButton.disabled = true;
     refreshButton.textContent = "Loading...";

     loading.hidden = false;
     errorElement.hidden = true;

     tableBody.innerHTML = `
         <tr>
             <td colspan="8" class="empty-state">
                 Loading unmatched payments...
             </td>
         </tr>
     `;

     try {
         const data = await apiRequest(UNMATCHED_API_URL);

         unmatchedPayments = Array.isArray(data) ? data : [];

         renderUnmatchedPayments(unmatchedPayments);

     } catch (error) {
         console.error("Unmatched payments loading failed:", error);

         errorElement.textContent = error.message;
         errorElement.hidden = false;

         tableBody.innerHTML = `
             <tr>
                 <td colspan="8" class="empty-state">
                     Unable to load unmatched payments.
                 </td>
             </tr>
         `;

         updateUnmatchedSummary([]);

     } finally {
         loading.hidden = true;
         refreshButton.disabled = false;
         refreshButton.textContent = "Refresh";
     }
 }

 function updateUnmatchedSummary(payments) {
     const totalReceived = payments.reduce(
         (sum, payment) =>
             sum + Number(payment.amountReceived || 0),
         0
     );

     const totalAllocated = payments.reduce(
         (sum, payment) =>
             sum + Number(payment.allocatedAmount || 0),
         0
     );

     const totalUnallocated = payments.reduce(
         (sum, payment) =>
             sum + Number(payment.unallocatedAmount || 0),
         0
     );

     document.getElementById("unmatched-count").textContent =
         payments.length;

     document.getElementById("unmatched-total-received").textContent =
         formatCurrency(totalReceived);

     document.getElementById("unmatched-total-allocated").textContent =
         formatCurrency(totalAllocated);

     document.getElementById("unmatched-total-balance").textContent =
         formatCurrency(totalUnallocated);

     document.getElementById("unmatched-record-count").textContent =
         `${payments.length} transaction${payments.length === 1 ? "" : "s"}`;
 }

 function renderUnmatchedPayments(payments) {
     const tbody = document.getElementById("unmatched-table-body");

     updateUnmatchedSummary(payments);

     if (!payments.length) {
         tbody.innerHTML = `
             <tr>
                 <td colspan="8" class="empty-state">
                     No unmatched payments. All successful payments are allocated.
                 </td>
             </tr>
         `;

         return;
     }

     tbody.innerHTML = payments.map(payment => `
         <tr>
             <td>
                 <strong>${escapeHTML(payment.studentName)}</strong>

                 <div class="secondary-text">
                     ${escapeHTML(payment.studentCode)}
                 </div>
             </td>

             <td>
                 <strong>${escapeHTML(payment.transactionReference || "—")}</strong>

                 <div class="secondary-text">
                     Payment #${escapeHTML(payment.paymentId)}
                 </div>
             </td>

             <td>${formatDateTime(payment.receivedAt)}</td>

             <td>${escapeHTML(payment.paymentMethod || "—")}</td>

             <td>${formatCurrency(payment.amountReceived)}</td>

             <td>${formatCurrency(payment.allocatedAmount)}</td>

             <td class="amount-outstanding">
                 ${formatCurrency(payment.unallocatedAmount)}
             </td>

             <td>
                 <button
                     type="button"
                     class="button button-primary allocate-payment-btn"
                     data-payment-id="${escapeHTML(payment.paymentId)}"
                 >
                     Allocate
                 </button>
             </td>
         </tr>
     `).join("");
 }

 /* =========================================================
    PAYMENT ALLOCATION
 ========================================================= */

 function showAllocationMessage(message, type) {
     allocationElements.message.textContent = message;
     allocationElements.message.className =
         `allocation-message ${type}`;

     allocationElements.message.hidden = false;
 }

 function hideAllocationMessage() {
     allocationElements.message.hidden = true;
     allocationElements.message.textContent = "";
     allocationElements.message.className = "allocation-message";
 }

 function closeAllocationModal() {
     allocationElements.modal.hidden = true;
     allocationElements.form.reset();

     selectedPayment = null;
     selectedInstalmentBalance = 0;

     allocationElements.instalment.innerHTML = `
         <option value="">Select an outstanding instalment</option>
     `;

     allocationElements.instalmentBalance.textContent =
         "Select an instalment to view its outstanding balance.";

     allocationElements.amount.max = "";

     allocationElements.amountHelp.textContent =
         "The amount cannot exceed the payment balance or instalment balance.";

     hideAllocationMessage();
 }

 async function openAllocationModal(paymentId) {
     selectedPayment = unmatchedPayments.find(
         payment => Number(payment.paymentId) === Number(paymentId)
     );

     if (!selectedPayment) {
         alert("Payment record not found. Refresh the queue and try again.");
         return;
     }

     allocationElements.modal.hidden = false;

     allocationElements.studentName.textContent =
         selectedPayment.studentName;

     allocationElements.transaction.textContent =
         selectedPayment.transactionReference || "—";

     allocationElements.receivedAmount.textContent =
         formatCurrency(selectedPayment.amountReceived);

     allocationElements.remainingAmount.textContent =
         formatCurrency(selectedPayment.unallocatedAmount);

     allocationElements.instalment.innerHTML = `
         <option value="">Loading instalments...</option>
     `;

     allocationElements.instalment.disabled = true;
     allocationElements.confirmButton.disabled = true;

     hideAllocationMessage();

     try {
         const student = await apiRequest(
             `/api/accounts/students/${encodeURIComponent(selectedPayment.studentId)}`
         );

         const feePlans = student.feePlans || [];

         const instalments = feePlans.flatMap(plan =>
             (plan.instalments || []).map(item => ({
                 ...item,
                 academicYear: plan.academicYear
             }))
         );

         const eligibleInstalments = instalments.filter(item =>
             Number(item.outstandingAmount || 0) > 0
         );

         allocationElements.instalment.innerHTML = `
             <option value="">Select an outstanding instalment</option>
         `;

         if (!eligibleInstalments.length) {
             allocationElements.instalment.innerHTML = `
                 <option value="">No outstanding instalments available</option>
             `;

             showAllocationMessage(
                 "This student has no outstanding instalments available for allocation.",
                 "error"
             );

             return;
         }

         eligibleInstalments.forEach(item => {
             const option = document.createElement("option");

             option.value = item.instalmentId;
             option.dataset.balance = Number(item.outstandingAmount);

             option.textContent =
                 `Year ${item.academicYear} · Instalment ${item.instalmentNumber} · Balance ${formatCurrency(item.outstandingAmount)}`;

             allocationElements.instalment.appendChild(option);
         });

         allocationElements.instalment.disabled = false;
         allocationElements.confirmButton.disabled = false;

     } catch (error) {
         console.error("Unable to load student instalments:", error);

         allocationElements.instalment.innerHTML = `
             <option value="">Unable to load instalments</option>
         `;

         showAllocationMessage(error.message, "error");
     }
 }

 function handleInstalmentSelection() {
     const selectedOption =
         allocationElements.instalment.selectedOptions[0];

     if (!selectedOption || !selectedOption.value) {
         selectedInstalmentBalance = 0;

         allocationElements.instalmentBalance.textContent =
             "Select an instalment to view its outstanding balance.";

         allocationElements.amount.max = "";
         return;
     }

     selectedInstalmentBalance =
         Number(selectedOption.dataset.balance || 0);

     allocationElements.instalmentBalance.textContent =
         `Outstanding balance: ${formatCurrency(selectedInstalmentBalance)}`;

     const paymentBalance =
         Number(selectedPayment?.unallocatedAmount || 0);

     const maximumAllocation = Math.min(
         paymentBalance,
         selectedInstalmentBalance
     );

     allocationElements.amount.max =
         maximumAllocation.toFixed(2);

     allocationElements.amount.value =
         maximumAllocation.toFixed(2);

     allocationElements.amountHelp.textContent =
         `Maximum allocatable amount: ${formatCurrency(maximumAllocation)}`;

     hideAllocationMessage();
 }

 async function submitAllocation(event) {
     event.preventDefault();

     if (!selectedPayment) {
         showAllocationMessage(
             "Please select a payment first.",
             "error"
         );

         return;
     }

     const instalmentId = allocationElements.instalment.value;
     const allocationAmount = Number(allocationElements.amount.value);

     const paymentBalance =
         Number(selectedPayment.unallocatedAmount || 0);

     if (!instalmentId) {
         showAllocationMessage(
             "Please select an instalment.",
             "error"
         );

         return;
     }

     if (!Number.isFinite(allocationAmount) || allocationAmount <= 0) {
         showAllocationMessage(
             "Enter a valid allocation amount.",
             "error"
         );

         return;
     }

     if (allocationAmount > paymentBalance) {
         showAllocationMessage(
             "Allocation amount cannot exceed the unallocated payment balance.",
             "error"
         );

         return;
     }

     if (allocationAmount > selectedInstalmentBalance) {
         showAllocationMessage(
             "Allocation amount cannot exceed the instalment outstanding balance.",
             "error"
         );

         return;
     }

     allocationElements.confirmButton.disabled = true;
     allocationElements.confirmButton.textContent = "Allocating...";

     try {
         await apiRequest(
             `/api/accounts/payments/${encodeURIComponent(selectedPayment.paymentId)}/allocate`,
             {
                 method: "POST",
                 body: JSON.stringify({
                     instalmentId: Number(instalmentId),
                     allocationAmount
                 })
             }
         );

         showAllocationMessage(
             "Payment allocated successfully.",
             "success"
         );

         await loadUnmatchedPayments();

         if (currentStudentDetailId) {
             await openStudentDetail(currentStudentDetailId);
         }

         setTimeout(() => {
             closeAllocationModal();
         }, 900);

     } catch (error) {
         console.error("Payment allocation failed:", error);

         showAllocationMessage(error.message, "error");

     } finally {
         allocationElements.confirmButton.disabled = false;
         allocationElements.confirmButton.textContent =
             "Confirm allocation";
     }
 }

 /* =========================================================
    REMINDERS
 ========================================================= */

 function showReminders() {
     overviewView.hidden = true;
     dashboardView.hidden = true;
     studentDetailView.hidden = true;
     studentsView.hidden = true;
     unmatchedView.hidden = true;
     refundsView.hidden = true;
     remindersView.hidden = false;

     updatePageHeader("reminders");
     loadReminders();
 }

 async function loadReminders() {
     const tableBody =
         document.getElementById("reminder-table-body");

     const refreshButton =
         document.getElementById("refresh-reminders-btn");

     refreshButton.disabled = true;
     refreshButton.textContent = "Loading...";

     tableBody.innerHTML = `
         <tr>
             <td colspan="8" class="empty-state">
                 Loading reminders...
             </td>
         </tr>
     `;

     hideReminderFeedback();

     try {
         const data = await apiRequest(REMINDERS_API_URL);

         reminders = Array.isArray(data) ? data : [];

         filterReminders();

     } catch (error) {
         console.error("Reminder loading failed:", error);

         tableBody.innerHTML = `
             <tr>
                 <td colspan="8" class="empty-state">
                     Unable to load reminder history.
                 </td>
             </tr>
         `;

         updateReminderSummary([]);

         showReminderFeedback(
             error.message || "Unable to load reminders.",
             "error"
         );

     } finally {
         refreshButton.disabled = false;
         refreshButton.textContent = "Refresh";
     }
 }

 function updateReminderSummary(data) {
     const sentCount = data.filter(
         reminder => reminder.status === "SENT"
     ).length;

     const failedCount = data.filter(
         reminder => reminder.status === "FAILED"
     ).length;

     document.getElementById("reminder-total").textContent =
         data.length;

     document.getElementById("reminder-sent").textContent =
         sentCount;

     document.getElementById("reminder-failed").textContent =
         failedCount;

     document.getElementById("reminder-record-count").textContent =
         `${data.length} reminder${data.length === 1 ? "" : "s"}`;
 }

 function renderReminders(data) {
     const tbody =
         document.getElementById("reminder-table-body");

     updateReminderSummary(data);

     if (!data.length) {
         tbody.innerHTML = `
             <tr>
                 <td colspan="8" class="empty-state">
                     No reminders found.
                 </td>
             </tr>
         `;

         return;
     }

     tbody.innerHTML = data.map(reminder => {
         const type = reminder.reminderType || "";

         const typeClass =
             type === "OVERDUE"
                 ? "overdue"
                 : type === "DUE_TODAY"
                     ? "due-today"
                     : "upcoming";

         const statusClass =
             getDetailStatusClass(reminder.status);

         return `
             <tr>
                 <td>
                     <strong>
                         ${escapeHTML(reminder.studentName)}
                     </strong>
                 </td>

                 <td>
                     <strong>
                         ${escapeHTML(reminder.parentName)}
                     </strong>

                     <div class="secondary-text">
                         ${escapeHTML(reminder.recipientMobile)}
                     </div>
                 </td>

                 <td>
                     Instalment ${escapeHTML(reminder.instalmentNumber)}
                 </td>

                 <td>
                     <span class="reminder-type ${typeClass}">
                         ${escapeHTML(type.replaceAll("_", " "))}
                     </span>
                 </td>

                 <td>
                     ${escapeHTML(reminder.language)}
                 </td>

                 <td>
                     ${formatDate(reminder.scheduledDate)}

                     <div class="secondary-text">
                         ${formatDateTime(reminder.sentAt)}
                     </div>
                 </td>

                 <td>
                     <span class="status-badge ${statusClass}">
                         ${escapeHTML(reminder.status)}
                     </span>
                 </td>

                 <td>
                     <div
                         class="reminder-message"
                         title="${escapeHTML(reminder.message)}"
                     >
                         ${escapeHTML(reminder.message)}
                     </div>
                 </td>
             </tr>
         `;
     }).join("");
 }

 function filterReminders() {
     const search = document
         .getElementById("reminder-search")
         .value
         .trim()
         .toLowerCase();

     const type =
         document.getElementById("reminder-type-filter").value;

     const status =
         document.getElementById("reminder-status-filter").value;

     const filtered = reminders.filter(reminder => {
         const matchesSearch =
             String(reminder.studentName || "")
                 .toLowerCase()
                 .includes(search) ||

             String(reminder.parentName || "")
                 .toLowerCase()
                 .includes(search) ||

             String(reminder.recipientMobile || "")
                 .includes(search);

         const matchesType =
             !type || reminder.reminderType === type;

         const matchesStatus =
             !status || reminder.status === status;

         return matchesSearch && matchesType && matchesStatus;
     });

     renderReminders(filtered);
 }

 function showReminderFeedback(message, type) {
     const feedback =
         document.getElementById("reminder-feedback");

     feedback.textContent = message;
     feedback.className = `reminder-feedback ${type}`;
     feedback.hidden = false;
 }

 function hideReminderFeedback() {
     const feedback =
         document.getElementById("reminder-feedback");

     if (!feedback) return;

     feedback.hidden = true;
     feedback.textContent = "";
 }

 async function generateReminders() {
     const button = document.getElementById("generate-reminders-btn");

     button.disabled = true;
     button.textContent = "Generating...";
     hideReminderFeedback();

     try {
         const result = await apiRequest(`${REMINDERS_API_URL}/generate`, {
             method: "POST"
         });

         await loadReminders();

         showReminderFeedback(
             `${result.generatedCount} new reminder(s) generated.`,
             "success"
         );
     } catch (error) {
         showReminderFeedback(
             error.message || "Unable to generate reminders.",
             "error"
         );
     } finally {
         button.disabled = false;
         button.textContent = "Generate reminders";
     }
 }


 /* =========================================================
    STUDENT RISK AND COUNSELLOR TASKS
 ========================================================= */

 function showRisk() {
     currentStudentDetailId = null;

     overviewView.hidden = true;
     dashboardView.hidden = true;
     studentsView.hidden = true;
     studentDetailView.hidden = true;
     unmatchedView.hidden = true;
     refundsView.hidden = true;
     remindersView.hidden = true;
     riskView.hidden = false;

     updatePageHeader("risk");
     loadStudentRisks();
 }

 function getRiskLevelClass(level) {
     switch (String(level || "").toUpperCase()) {
         case "HIGH":
             return "overdue";
         case "MEDIUM":
             return "partial";
         case "LOW":
             return "paid";
         default:
             return "upcoming";
     }
 }

 function getTaskStatusClass(status) {
     switch (String(status || "").toUpperCase()) {
         case "OPEN":
             return "overdue";
         case "CONTACTED":
             return "partial";
         case "FALSE_POSITIVE":
             return "paid";
         default:
             return "upcoming";
     }
 }

 function getRiskFactorsText(factors) {
     if (Array.isArray(factors)) {
         return factors.map(factor => {
             if (typeof factor === "string") return factor;
             return factor.description || factor.reason || factor.message || JSON.stringify(factor);
         }).join("; ");
     }

     return String(factors || "No risk factors reported.");
 }

 function renderRiskSummary() {
     const high = studentRiskRecords.filter(item => item.riskLevel === "HIGH").length;
     const medium = studentRiskRecords.filter(item => item.riskLevel === "MEDIUM").length;
     const low = studentRiskRecords.filter(item => item.riskLevel === "LOW").length;
     const openTasks = counsellorTasks.filter(item => item.status === "OPEN").length;

     document.getElementById("risk-high-count").textContent = high;
     document.getElementById("risk-medium-count").textContent = medium;
     document.getElementById("risk-low-count").textContent = low;
     document.getElementById("risk-task-count").textContent = openTasks;
 }

 function renderStudentRiskTable() {
     const tbody = document.getElementById("risk-table-body");
     const search = document.getElementById("risk-search").value.trim().toLowerCase();
     const selectedLevel = document.getElementById("risk-level-filter").value;

     const filtered = studentRiskRecords.filter(student => {
         const matchesSearch =
             String(student.studentName || "").toLowerCase().includes(search) ||
             String(student.studentCode || "").toLowerCase().includes(search) ||
             String(student.batch || "").toLowerCase().includes(search);
         const matchesLevel = !selectedLevel || student.riskLevel === selectedLevel;
         return matchesSearch && matchesLevel;
     });

     if (!filtered.length) {
         tbody.innerHTML = `
             <tr><td colspan="7" class="empty-state">No risk assessments match these filters.</td></tr>
         `;
     } else {
         tbody.innerHTML = filtered.map(student => `
             <tr>
                 <td>
                     <strong>${escapeHTML(student.studentName || "—")}</strong>
                     <div class="secondary-text">${escapeHTML(student.studentCode || student.studentId || "—")}</div>
                 </td>
                 <td>${escapeHTML(student.batch || "—")}</td>
                 <td><strong>${escapeHTML(student.riskScore ?? 0)}/100</strong></td>
                 <td><span class="status-badge ${getRiskLevelClass(student.riskLevel)}">${escapeHTML(student.riskLevel || "—")}</span></td>
                 <td>${escapeHTML(getRiskFactorsText(student.riskFactors))}</td>
                 <td>${student.attendanceDataAvailable ? "Available" : "Missing"} / ${student.testDataAvailable ? "Available" : "Missing"} / ${student.feeDataAvailable ? "Available" : "Missing"}</td>
                 <td>
                     <button type="button" class="button button-secondary" data-risk-student-id="${escapeHTML(student.studentId)}">View profile</button>
                 </td>
             </tr>
         `).join("");
     }

     document.getElementById("risk-record-count").textContent =
         `${filtered.length} student${filtered.length === 1 ? "" : "s"}`;
 }

 function renderCounsellorTasks() {
     const tbody = document.getElementById("counsellor-task-body");
     const selectedStatus = document.getElementById("task-status-filter").value;
     const filtered = counsellorTasks.filter(task =>
         !selectedStatus || task.status === selectedStatus
     );

     if (!filtered.length) {
         tbody.innerHTML = `
             <tr><td colspan="7" class="empty-state">No counsellor follow-up tasks found.</td></tr>
         `;
     } else {
         tbody.innerHTML = filtered.map(task => `
             <tr>
                 <td>
                     <strong>${escapeHTML(task.studentName || "—")}</strong>
                     <div class="secondary-text">${escapeHTML(task.studentCode || task.studentId || "—")}</div>
                 </td>
                 <td>${escapeHTML(task.batch || "—")}</td>
                 <td>${escapeHTML(task.riskScore ?? 0)}/100 · ${escapeHTML(task.riskLevel || "—")}</td>
                 <td>${escapeHTML(getRiskFactorsText(task.riskReasons))}</td>
                 <td><span class="status-badge ${getTaskStatusClass(task.status)}">${escapeHTML(String(task.status || "—").replaceAll("_", " "))}</span></td>
                 <td>${escapeHTML(task.falsePositiveReason || formatDateTime(task.reviewedAt || task.createdAt))}</td>
                 <td>
                     ${task.status === "OPEN" ? `
                         <div class="risk-task-actions">
                             <button type="button" class="button button-secondary" data-task-action="contacted" data-task-id="${escapeHTML(task.taskId)}">Mark contacted</button>
                             <button type="button" class="button button-secondary" data-task-action="false-positive" data-task-id="${escapeHTML(task.taskId)}">False positive</button>
                         </div>
                     ` : task.status === "CONTACTED" ? `
                         <button type="button" class="button button-secondary" data-task-action="false-positive" data-task-id="${escapeHTML(task.taskId)}">Mark false positive</button>
                     ` : "—"}
                 </td>
             </tr>
         `).join("");
     }

     document.getElementById("counsellor-task-count").textContent =
         `${filtered.length} task${filtered.length === 1 ? "" : "s"}`;
 }

 function showRiskFeedback(message, type = "error") {
     const feedback = document.getElementById("risk-feedback");
     feedback.textContent = message;
     feedback.className = `risk-feedback ${type}`;
     feedback.hidden = false;
 }

 function hideRiskFeedback() {
     const feedback = document.getElementById("risk-feedback");
     feedback.hidden = true;
     feedback.textContent = "";
 }

 async function loadStudentRisks() {
     const refreshButton = document.getElementById("refresh-risk-btn");
     refreshButton.disabled = true;
     refreshButton.textContent = "Loading...";
     hideRiskFeedback();

     document.getElementById("risk-table-body").innerHTML =
         '<tr><td colspan="7" class="empty-state">Loading student risk assessments...</td></tr>';
     document.getElementById("counsellor-task-body").innerHTML =
         '<tr><td colspan="7" class="empty-state">Loading counsellor tasks...</td></tr>';

     try {
         const risks = await apiRequest(RISK_API_URL);
         studentRiskRecords = Array.isArray(risks) ? risks : [];

         const tasks = await apiRequest(COUNSELLOR_TASKS_API_URL);
         counsellorTasks = Array.isArray(tasks) ? tasks : [];

         renderRiskSummary();
         renderStudentRiskTable();
         renderCounsellorTasks();
     } catch (error) {
         console.error("Student risk loading failed:", error);
         showRiskFeedback(error.message || "Unable to load student risk data.");
         document.getElementById("risk-table-body").innerHTML =
             '<tr><td colspan="7" class="empty-state">Unable to load risk assessments.</td></tr>';
         document.getElementById("counsellor-task-body").innerHTML =
             '<tr><td colspan="7" class="empty-state">Unable to load counsellor tasks.</td></tr>';
     } finally {
         refreshButton.disabled = false;
         refreshButton.textContent = "Refresh";
     }
 }

 async function updateCounsellorTask(taskId, action) {
     const task = counsellorTasks.find(item => Number(item.taskId) === Number(taskId));
     if (!task) return;

     let url;
     let options;

     if (action === "contacted") {
         url = `${COUNSELLOR_TASKS_API_URL}/${encodeURIComponent(taskId)}/contacted`;
         options = { method: "PATCH" };
     } else {
         const reason = window.prompt("Why is this risk flag a false positive?");
         if (reason === null) return;
         if (!reason.trim()) {
             showRiskFeedback("Please enter a reason before marking this task as a false positive.");
             return;
         }

         url = `${COUNSELLOR_TASKS_API_URL}/${encodeURIComponent(taskId)}/false-positive`;
         options = {
             method: "PATCH",
             body: JSON.stringify({ reason: reason.trim() })
         };
     }

     hideRiskFeedback();

     try {
         await apiRequest(url, options);
         showRiskFeedback(
             action === "contacted" ? "Task marked as contacted." : "Risk flag marked as a false positive.",
             "success"
         );
         await loadStudentRisks();
     } catch (error) {
         showRiskFeedback(error.message || "Unable to update the counsellor task.");
     }
 }

 /* =========================================================
    EVENT LISTENERS
 ========================================================= */

 elements.tableBody.addEventListener("click", event => {
     const button = event.target.closest("[data-student-id]");

     if (!button) return;

     openStudentDetail(button.dataset.studentId);
 });

 document.getElementById("back-to-dashboard")
     .addEventListener("click", showDashboard);

 document.getElementById("student-directory-search")
     .addEventListener("input", renderStudentDirectory);

 document.getElementById("refresh-students-btn")
     .addEventListener("click", async () => {
         await loadDashboard();
         renderStudentDirectory();
     });

 document.getElementById("student-directory-body")
     .addEventListener("click", event => {
         const button = event.target.closest("[data-directory-student-id]");

         if (!button) return;

         openStudentDetail(button.dataset.directoryStudentId);
     });



 const sidebar = document.getElementById("sidebar");
 const menuToggle = document.getElementById("menu-toggle");
 const sidebarOverlay = document.getElementById("sidebar-overlay");
 const logoutButton = document.getElementById("logout-btn");

 function closeSidebar() {
     sidebar.classList.remove("open");
     sidebarOverlay.classList.remove("show");
     menuToggle.setAttribute("aria-expanded", "false");
     menuToggle.setAttribute("aria-label", "Open navigation");
     sidebarOverlay.setAttribute("aria-hidden", "true");
 }

 function openSidebar() {
     sidebar.classList.add("open");
     sidebarOverlay.classList.add("show");
     menuToggle.setAttribute("aria-expanded", "true");
     menuToggle.setAttribute("aria-label", "Close navigation");
     sidebarOverlay.setAttribute("aria-hidden", "false");
 }

 menuToggle.addEventListener("click", () => {
     if (sidebar.classList.contains("open")) {
         closeSidebar();
     } else {
         openSidebar();
     }
 });

 sidebarOverlay.addEventListener("click", closeSidebar);

 document.addEventListener("keydown", event => {
     if (event.key === "Escape") closeSidebar();
 });

 logoutButton.addEventListener("click", () => {
     sessionStorage.removeItem("accountsAccessToken");
     sessionStorage.removeItem("accountsUserName");
     window.location.replace("/accounts-login.html");
 });

 document.querySelectorAll(".nav-link").forEach(link => {
     link.addEventListener("click", event => {
         event.preventDefault();
         closeSidebar();

         const page = link.dataset.page;

         if (page === "unmatched") {
             showUnmatchedPayments();
             return;
         }

         if (page === "refunds") {
             showRefunds();
             return;
         }

         if (page === "reminders") {
             showReminders();
             return;
         }

         if (page === "risk") {
             showRisk();
             return;
         }

         if (page === "dashboard") {
             showDashboard();
             return;
         }

         if (page === "fees") {
             showFeeLedger();
             return;
         }

         if (page === "students") {
             showStudents();
             return;
         }

         showDashboard();
         updatePageHeader(page);
     });
 });

 /* Dashboard events */

 elements.refreshButton.addEventListener("click", loadDashboard);
 elements.exportButton.addEventListener("click", exportCSV);
 elements.search.addEventListener("input", filterLedger);
 elements.statusFilter.addEventListener("change", filterLedger);

 /* Unmatched payment events */

 document.getElementById("refresh-unmatched-btn")
     .addEventListener("click", loadUnmatchedPayments);

 document.getElementById("unmatched-table-body")
     .addEventListener("click", event => {
         const button =
             event.target.closest(".allocate-payment-btn");

         if (!button) return;

         openAllocationModal(button.dataset.paymentId);
     });

 allocationElements.closeButton.addEventListener(
     "click",
     closeAllocationModal
 );

 allocationElements.cancelButton.addEventListener(
     "click",
     closeAllocationModal
 );

 allocationElements.instalment.addEventListener(
     "change",
     handleInstalmentSelection
 );

 allocationElements.form.addEventListener(
     "submit",
     submitAllocation
 );

 allocationElements.modal.addEventListener("click", event => {
     if (event.target === allocationElements.modal) {
         closeAllocationModal();
     }
 });

 /* Refund events */

 const refundStudentSelect =
     document.getElementById("refund-student");

 const refundFeePlanSelect =
     document.getElementById("refund-fee-plan");

 const refundForm =
     document.getElementById("refund-form");

 if (refundStudentSelect) {
     refundStudentSelect.addEventListener("change", event => {
         if (event.target.value) {
             loadRefundFeePlans(event.target.value);

         } else if (refundFeePlanSelect) {
             refundFeePlanSelect.innerHTML =
                 '<option value="">Select a fee plan</option>';
         }
     });
 }

 if (refundFeePlanSelect) {
     refundFeePlanSelect.addEventListener("change", () => {
         const button =
             document.getElementById("calculate-refund-btn");

         if (button) {
             button.disabled = !refundFeePlanSelect.value;
         }

         document.getElementById("refund-result").hidden = true;

         hideRefundFeedback();
     });
 }

 if (refundForm) {
     refundForm.addEventListener("submit", calculateRefund);
 }

 /* Reminder events */

 document.getElementById("refresh-reminders-btn")
     .addEventListener("click", loadReminders);

 document.getElementById("generate-reminders-btn")
     .addEventListener("click", generateReminders);

 document.getElementById("reminder-search")
     .addEventListener("input", filterReminders);

 document.getElementById("reminder-type-filter")
     .addEventListener("change", filterReminders);

 document.getElementById("reminder-status-filter")
     .addEventListener("change", filterReminders);

 /* Student risk events */

 document.getElementById("refresh-risk-btn")
     .addEventListener("click", loadStudentRisks);

 document.getElementById("risk-search")
     .addEventListener("input", renderStudentRiskTable);

 document.getElementById("risk-level-filter")
     .addEventListener("change", renderStudentRiskTable);

 document.getElementById("task-status-filter")
     .addEventListener("change", renderCounsellorTasks);

 document.getElementById("risk-table-body")
     .addEventListener("click", event => {
         const button = event.target.closest("[data-risk-student-id]");
         if (button) openStudentDetail(button.dataset.riskStudentId);
     });

 document.getElementById("counsellor-task-body")
     .addEventListener("click", event => {
         const button = event.target.closest("[data-task-action]");
         if (!button) return;
         updateCounsellorTask(button.dataset.taskId, button.dataset.taskAction);
     });

 /* =========================================================
    APPLICATION INITIALIZATION
 ========================================================= */

 if (!getAccessToken()) {
     window.location.replace("/accounts-login.html");
 } else {
     loadDashboard();
 }