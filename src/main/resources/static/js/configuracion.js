document.addEventListener('DOMContentLoaded', function() {

    const opcionesLaterales = document.querySelectorAll('.opcion-lateral');
    const seccionesConfig = document.querySelectorAll('.seccion-config');

    function mostrarSeccion(seccionId) {

        seccionesConfig.forEach(seccion => {
            seccion.classList.remove('activo');
        });
        
        const seccionActiva = document.getElementById('seccion-' + seccionId);
        if (seccionActiva) {
            seccionActiva.classList.add('activo');
        }
        
        opcionesLaterales.forEach(opcion => {
            opcion.classList.remove('activo');
            if (opcion.getAttribute('data-seccion') === seccionId) {
                opcion.classList.add('activo');
            }
        });
    }
    
    opcionesLaterales.forEach(opcion => {
        opcion.addEventListener('click', function() {
            const seccionId = this.getAttribute('data-seccion');
            mostrarSeccion(seccionId);
        });
    });
    
    const formularios = document.querySelectorAll('.formulario-config');
    formularios.forEach(form => {
        form.addEventListener('submit', function(e) {
            e.preventDefault();
            alert('Esta funcionalidad se implementará próximamente.');
        });
    });
    
    mostrarSeccion('correo');
    
    document.getElementById('correo-actual').value = 'usuario@ejemplo.com';
    document.getElementById('usuario-actual').value = 'Usuario123';
});
