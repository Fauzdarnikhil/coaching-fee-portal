
const API_BASE_URL = "/api/auth";

const elements = {
    mobileScreen: document.getElementById("mobile-screen"),
    otpScreen: document.getElementById("otp-screen"),

    sendOtpForm: document.getElementById("send-otp-form"),
    verifyOtpForm: document.getElementById("verify-otp-form"),

    mobileInput: document.getElementById("mobile-number"),
    otpInput: document.getElementById("otp-input"),

    sendOtpButton: document.getElementById("send-otp-btn"),
    verifyOtpButton: document.getElementById("verify-otp-btn"),

    displayMobile: document.getElementById("display-mobile"),
    changeNumberButton: document.getElementById("change-number-btn"),

    message: document.getElementById("login-message")
};

let currentMobileNumber = "";
let isSendingOtp = false;
let isVerifyingOtp = false;


// Display feedback messages
function showMessage(message, type = "info") {
    elements.message.textContent = message;
    elements.message.className = `login-message ${type}`;
}


// Clear feedback messages
function clearMessage() {
    elements.message.textContent = "";
    elements.message.className = "login-message";
}


// Show OTP screen
function showOtpScreen() {
    elements.mobileScreen.hidden = true;
    elements.otpScreen.hidden = false;

    elements.displayMobile.textContent = `+91 ${currentMobileNumber}`;

    elements.otpInput.value = "";
    elements.otpInput.focus();
}


// Show mobile number screen
function showMobileScreen() {
    elements.otpScreen.hidden = true;
    elements.mobileScreen.hidden = false;

    elements.mobileInput.focus();
    clearMessage();
}


// Send OTP request

async function sendOtp(mobileNo) {

    if (isSendingOtp) return;

    isSendingOtp = true;
    elements.sendOtpButton.disabled = true;
    elements.sendOtpButton.textContent = "Sending OTP...";

    clearMessage();

    try {

        const response = await fetch(`${API_BASE_URL}/send-otp`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({
                mobileNo: mobileNo
            })
        });

        // Read response body only once
        const data = await response.json();

        if (!response.ok) {
            throw new Error(
                data.message || "Unable to send OTP. Please try again."
            );
        }

        currentMobileNumber = mobileNo;

        showOtpScreen();

        showMessage(data.message, "success");

    } catch (error) {

        console.error("Send OTP error:", error);

        showMessage(
            error.message || "Something went wrong. Please try again.",
            "error"
        );

    } finally {

        isSendingOtp = false;
        elements.sendOtpButton.disabled = false;
        elements.sendOtpButton.textContent = "Send OTP";
    }
}



// Verify OTP request
async function verifyOtp(mobileNo, otp) {

    if (isVerifyingOtp) return;

    isVerifyingOtp = true;
    elements.verifyOtpButton.disabled = true;
    elements.verifyOtpButton.textContent = "Verifying...";

    clearMessage();

    try {

        const response = await fetch(`${API_BASE_URL}/verify-otp`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({
                mobileNo: mobileNo,
                otp: otp
            })
        });

        const data = await response.json();

        if (!response.ok) {
            throw new Error(
                data.message || "Invalid or expired OTP."
            );
        }

        if (!data.token) {
            throw new Error("Authentication token was not received.");
        }

        // Save JWT for the current browser session
        sessionStorage.setItem("accessToken", data.token);

        showMessage(
            "Login successful! Redirecting...",
            "success"
        );

        // Redirect to parent dashboard
        window.location.href = "/parent-dashboard.html";

    } catch (error) {

        console.error("OTP verification error:", error);

        showMessage(
            error.message || "OTP verification failed.",
            "error"
        );

        elements.otpInput.value = "";
        elements.otpInput.focus();

    } finally {

        isVerifyingOtp = false;
        elements.verifyOtpButton.disabled = false;
        elements.verifyOtpButton.textContent = "Verify & Continue";
    }
}


// Validate mobile number
function isValidMobileNumber(mobileNo) {
    return /^[6-9]\d{9}$/.test(mobileNo);
}


// Validate OTP
function isValidOtp(otp) {
    return /^\d{6}$/.test(otp);
}


// Send OTP form submission
elements.sendOtpForm.addEventListener("submit", function (event) {

    event.preventDefault();

    const mobileNo = elements.mobileInput.value.trim();

    if (!isValidMobileNumber(mobileNo)) {

        showMessage(
            "Please enter a valid 10-digit Indian mobile number.",
            "error"
        );

        return;
    }

    sendOtp(mobileNo);
});


// Verify OTP form submission
elements.verifyOtpForm.addEventListener("submit", function (event) {

    event.preventDefault();

    const otp = elements.otpInput.value.trim();

    if (!isValidOtp(otp)) {

        showMessage(
            "Please enter the valid 6-digit OTP.",
            "error"
        );

        return;
    }

    verifyOtp(currentMobileNumber, otp);
});


// Change mobile number
elements.changeNumberButton.addEventListener("click", function () {

    currentMobileNumber = "";

    elements.otpInput.value = "";

    showMobileScreen();
});


// Allow only numeric OTP input
elements.otpInput.addEventListener("input", function () {

    this.value = this.value.replace(/\D/g, "").slice(0, 6);

});


// Allow only numeric mobile input
elements.mobileInput.addEventListener("input", function () {

    this.value = this.value.replace(/\D/g, "").slice(0, 10);

});
