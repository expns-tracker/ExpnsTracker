import {app} from "/js/firebase/firebase-config.js";
import {getAuth, createUserWithEmailAndPassword} from "https://www.gstatic.com/firebasejs/10.0.0/firebase-auth.js";

const auth = getAuth(app);

function togglePassword(inputId, el) {
    const input = document.getElementById(inputId);
    if (input.type === "password") {
        input.type = "text";
        el.textContent = "visibility_off";
    } else {
        input.type = "password";
        el.textContent = "visibility";
    }
}

function formatFirebaseError(error) {
    switch(error.code) {
        case 'auth/email-already-in-use':
            return 'This email is already registered.';
        case 'auth/invalid-email':
            return 'The email address is not valid.';
        case 'auth/weak-password':
            return 'Password should be at least 6 characters.';
        case 'auth/network-request-failed':
            return 'Network error. Please try again.';
        default:
            return error.message;
    }
}

window.togglePassword = togglePassword;
window.signup = function (event) {
    const email = document.getElementById("email").value;
    const password = document.getElementById("password").value;
    const confirmPasswordInput = document.getElementById("confirm-password");
    const confirmPassword = confirmPasswordInput.value;

    const errorDiv = document.getElementById('error-message');
    const confirmError = document.getElementById("confirm-error");

    confirmPasswordInput.classList.remove('invalid');

    if(password !== confirmPassword){
        confirmPasswordInput.classList.add('invalid');
        confirmError.setAttribute('data-error', 'Passwords do not match');
        return;
    } else {
        errorDiv.style.display = 'none';
    }

    createUserWithEmailAndPassword(auth, email, password)
        .then(async (userCredential) => {
            // user created successfully
            const idToken = await userCredential.user.getIdToken();

            // Send token to Spring Boot to create session (same as login)
            const res = await fetch("/session-login", {
                method: "POST",
                headers: {
                    "Authorization": "Bearer " + idToken
                }
            });

            if (res.ok) {
                window.location.href = "/";
            }
        })
        .catch((error) => {
            const errorDiv = document.getElementById('error-message');
            errorDiv.textContent = formatFirebaseError(error);
            errorDiv.style.display = 'block';
        });
}