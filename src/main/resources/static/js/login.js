import {app} from "/js/firebase/firebase-config.js";
import {getAuth, signInWithEmailAndPassword, GoogleAuthProvider ,
    signInWithPopup, sendPasswordResetEmail } from "https://www.gstatic.com/firebasejs/10.0.0/firebase-auth.js";

const auth = getAuth(app);
const googleProvider = new GoogleAuthProvider();

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
            return "An unknown error occurred.";
    }
}

window.sendResetEmail = function() {
    const email = document.getElementById('resetEmail').value;

    if (!email) {
        M.toast({html: 'Please enter your email address', classes: 'red rounded'});
        return;
    }

    sendPasswordResetEmail(auth, email)
        .then(() => {
            M.toast({html: 'Password reset email sent!', classes: 'green rounded'});
            // Close modal
            const elem = document.getElementById('forgotPasswordModal');
            const instance = M.Modal.getInstance(elem);
            instance.close();
        })
        .catch((error) => {
            const errorCode = error.code;
            const errorMessage = error.message;
            console.error("Error sending reset email:", errorCode, errorMessage);

            let displayError = "Failed to send reset email.";
            if (errorCode === 'auth/user-not-found') {
                displayError = "No account found with this email.";
            } else if (errorCode === 'auth/invalid-email') {
                displayError = "Invalid email format.";
            }

            M.toast({html: displayError, classes: 'red rounded'});
        });
}

window.togglePassword = togglePassword;
window.signin = function (event) {
    const email = document.getElementById("email").value;
    const password = document.getElementById("password").value;
    const errorDiv = document.getElementById('error-message');


    signInWithEmailAndPassword(auth, email, password)
        .then(async (userCredential) => {
            // user created successfully
            const idToken = await userCredential.user.getIdToken();

            console.log(idToken);

            // Send token to Spring Boot to create session (same as login)
            const res = await fetch("api/auth/session-login", {
                method: "POST",
                headers: {
                    "Authorization": "Bearer " + idToken
                }
            });

            if (res.ok) {
                console.log(res.body);
                window.location.href = "/";
            }
        })
        .catch((error) => {
            const errorDiv = document.getElementById('error-message');
            errorDiv.textContent = formatFirebaseError(error);
            errorDiv.style.display = 'block';
            console.error(error);
        });
}
window.signInWithGoogle = function () {
    signInWithPopup(auth, googleProvider)
        .then(async (result) => {
            const user = result.user;
            const idToken = await user.getIdToken();

            console.log("Google user:", user);

            // Trimite token-ul la backend pentru crearea sesiunii
            const res = await fetch("api/auth/session-login", {
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
            console.error("Google Sign-In Error:", error);
        });
}