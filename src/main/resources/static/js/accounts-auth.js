
const API_URL = "/api/auth/accounts/login";

const form = document.getElementById("accounts-login-form");
const emailInput = document.getElementById("email");
const passwordInput = document.getElementById("password");
const loginButton = document.getElementById("login-btn");
const loginMessage = document.getElementById("login-message");

function showMessage(message) {
    loginMessage.textContent = message;
    loginMessage.hidden = false;
}

function hideMessage() {
    loginMessage.textContent = "";
    loginMessage.hidden = true;
}

form.addEventListener("submit", async event => {
    event.preventDefault();

    hideMessage();

    const email = emailInput.value.trim();
    const password = passwordInput.value;

    if (!email || !password) {
        showMessage("Please enter your email and password.");
        return;
    }

    loginButton.disabled = true;
    loginButton.innerHTML = "Signing in...";

    try {
        const response = await fetch(API_URL, {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify({
                email,
                password
            })
        });

        const data = await response.json();

        if (!response.ok) {
            throw new Error(
                data.message || "Unable to sign in. Please try again."
            );
        }

        if (!data.token || data.role !== "ACCOUNTS") {
            throw new Error("Invalid accounts authentication response.");
        }

        sessionStorage.removeItem("accountsAccessToken");
        sessionStorage.setItem("accountsAccessToken", data.token);
        sessionStorage.setItem("accountsUserName", data.name || "Accounts Executive");

        window.location.href = "/index.html";

    } catch (error) {
        showMessage(error.message);
    } finally {
        loginButton.disabled = false;
        loginButton.innerHTML = 'Sign in to workspace <span>→</span>';
    }
});
