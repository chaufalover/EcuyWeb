document.addEventListener('DOMContentLoaded', function () {
    const inputPassword = document.getElementById('inputContraseña');
    const inputRepetir = document.getElementById('inputRepetir');
    const formulario = document.querySelector('form');

    const feedback = document.createElement('div');
    feedback.className = 'invalid-feedback';
    feedback.textContent = 'Las contraseñas no coinciden';
    inputRepetir.parentNode.appendChild(feedback);

    inputRepetir.addEventListener('input', function () {
        const passwordsMatch = (inputPassword.value === inputRepetir.value);

        if (inputRepetir.value === '') {
            inputRepetir.classList.remove('is-invalid');
        } else if (!passwordsMatch) {
            inputRepetir.classList.add('is-invalid');
        } else {
            inputRepetir.classList.remove('is-invalid');
        }
    });

});