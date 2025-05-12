document.addEventListener('DOMContentLoaded', function() {
    const btnConfig = document.getElementById('btn-config');
    const popupConfig = document.getElementById('popup-config');
    
    if (btnConfig && popupConfig) {
        btnConfig.addEventListener('click', function(e) {
            e.stopPropagation();
            popupConfig.classList.toggle('activo');
        });

        document.addEventListener('click', function(e) {
            if (popupConfig.classList.contains('activo') && !popupConfig.contains(e.target) && e.target !== btnConfig) {
                popupConfig.classList.remove('activo');
            }
        });
        
        document.getElementById('cambiar-correo').addEventListener('click', function() {
            alert('La funcionalidad para cambiar el correo electrónico se implementará próximamente.');
            popupConfig.classList.remove('activo');
        });
        
        document.getElementById('cambiar-contrasena').addEventListener('click', function() {
            alert('La funcionalidad para cambiar la contraseña se implementará próximamente.');
            popupConfig.classList.remove('activo');
        });
        
        document.getElementById('eliminar-cuenta').addEventListener('click', function() {
            if (confirm('¿Estás seguro de que deseas eliminar tu cuenta? Esta acción no se puede deshacer.')) {
                alert('La funcionalidad para eliminar la cuenta se implementará próximamente.');
            }
            popupConfig.classList.remove('activo');
        });
    }
    
    const botonEditarAvatar = document.querySelector('.boton-editar-avatar');
    if (botonEditarAvatar) {
        botonEditarAvatar.addEventListener('click', function() {
            alert('Esta funcionalidad para cambiar la imagen de perfil se implementará próximamente.');
        });
    }
    
    const accionSeccion = document.querySelector('.accion-seccion');
    if (accionSeccion) {
        accionSeccion.addEventListener('click', function() {
            alert('Esta funcionalidad para gestionar las insignias se implementará próximamente.');
        });
    }
    
    const itemsInsignia = document.querySelectorAll('.item-insignia');
    itemsInsignia.forEach(function(item) {
        item.addEventListener('click', function() {
            if (this.classList.contains('bloqueada')) {
                alert('Esta insignia aún no está desbloqueada. ¡Sigue practicando para desbloquearla!');
            } else {
                alert('¡Felicidades por obtener esta insignia!');
            }
        });
    });
});
