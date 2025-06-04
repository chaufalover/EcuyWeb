document.addEventListener('DOMContentLoaded', function() {
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
