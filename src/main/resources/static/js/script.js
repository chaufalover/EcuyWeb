
function login() {
    const username = document.getElementById('username').value;
    const password = document.getElementById('password').value;
    
    if (username && password) {

        const btn = document.querySelector('.btn');
        const originalText = btn.textContent;
        btn.textContent = 'Iniciando sesión...';
        btn.disabled = true;
        

        const loginData = {
            username: username,
            password: password
        };
        

        fetch('http://localhost:9090/api/usuarios/login', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(loginData)
        })
        .then(response => {
            if (!response.ok) {
                return response.json().then(data => {
                    throw new Error(data.mensaje || 'Error en el inicio de sesión');
                });
            }
            return response.json();
        })
        .then(data => {
            if (data.exito) {

                sessionStorage.setItem('usuarioActual', JSON.stringify(data.usuario));

                window.location.href = 'modules.html';
            } else {
                alert(data.mensaje || 'Error desconocido');

                btn.textContent = originalText;
                btn.disabled = false;
            }
        })
        .catch(error => {
            console.error('Error:', error);
            alert('Error: ' + error.message);
            // Restaurar botón
            btn.textContent = originalText;
            btn.disabled = false;
        });
    } else {
        alert('Por favor completa todos los campos');
    }
}


document.addEventListener('DOMContentLoaded', function() {

    const registerForm = document.querySelector('form[action="/register"]');
    

    if (registerForm) {
        registerForm.addEventListener('submit', function(event) {
            event.preventDefault();
            const username = document.getElementById('username').value;
            const nombre = document.getElementById('nombre').value;
            const apellido = document.getElementById('apellido').value;
            const email = document.getElementById('email').value;
            const password = document.getElementById('password').value;
            const confirmPassword = document.getElementById('confirm-password').value;
            
            if (username && nombre && apellido && email && password && confirmPassword) {
                if (password !== confirmPassword) {
                    alert('Las contraseñas no coinciden');
                    return;
                }

                console.log('Register data submitted:', { username, nombre, apellido, email });

                this.submit();
            } else {
                alert('Por favor completa todos los campos');
            }
        });
    }
});


document.addEventListener('DOMContentLoaded', function() {

    const usuarioActual = sessionStorage.getItem('usuarioActual');
    if (usuarioActual) {

        const currentPage = window.location.pathname;
        if (currentPage.includes('login.html') || currentPage.includes('register.html')) {
            window.location.href = 'modules.html';
        }
    }
});
