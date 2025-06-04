document.addEventListener('DOMContentLoaded', function() {

    function getModuleStatus() {
        const savedStatus = localStorage.getItem('moduleStatus');
        if (savedStatus) {
            return JSON.parse(savedStatus);
        } else {

            const initialStatus = [];

            const moduleCount = document.querySelectorAll('.module-card').length;
            
            for (let i = 0; i < moduleCount; i++) {
                initialStatus.push({
                    unlocked: i === 0,
                    completed: false
                });
            }
            localStorage.setItem('moduleStatus', JSON.stringify(initialStatus));
            return initialStatus;
        }
    }
    

    function saveModuleStatus(status) {
        localStorage.setItem('moduleStatus', JSON.stringify(status));
    }
    

    function completeModule(moduleIndex) {
        const status = getModuleStatus();
        status[moduleIndex].completed = true;
        

        if (moduleIndex + 1 < status.length) {
            status[moduleIndex + 1].unlocked = true;
        }
        
        saveModuleStatus(status);
        updateModuleUI();
    }
    

    function updateModuleUI() {
        const status = getModuleStatus();
        const moduleCards = document.querySelectorAll('.module-card');
        
        moduleCards.forEach((card, index) => {

            card.classList.remove('locked', 'unlocked', 'completed');
            
            if (!status[index].unlocked) {

                card.classList.add('locked');
                

                if (!card.querySelector('.lock-icon')) {
                    const lockIcon = document.createElement('div');
                    lockIcon.className = 'lock-icon';
                    lockIcon.innerHTML = '🔒';
                    card.appendChild(lockIcon);
                }
            } else {

                card.classList.add('unlocked');
                

                const lockIcon = card.querySelector('.lock-icon');
                if (lockIcon) {
                    card.removeChild(lockIcon);
                }
                

                if (status[index].completed) {
                    card.classList.add('completed');
                    
                    if (!card.querySelector('.completed-icon')) {
                        const completedIcon = document.createElement('div');
                        completedIcon.className = 'completed-icon';
                        completedIcon.innerHTML = '✓';
                        card.appendChild(completedIcon);
                    }
                }
            }
        });
    }
    

    const moduleCards = document.querySelectorAll('.module-card');
    

    updateModuleUI();
    

    moduleCards.forEach((card, index) => {
        card.addEventListener('click', function() {
            const status = getModuleStatus();
            
            if (!status[index].unlocked) {

                alert(`El Módulo ${index + 1} está bloqueado. Completa el módulo anterior para desbloquearlo.`);
                return;
            }
            

            if (index === 0) {
                console.log('Redirigiendo a la página de capítulos del Módulo 1');
                

                completeModule(0);
                
                window.location.href = 'chapters.html';
            } else {
                console.log(`Módulo ${index + 1} seleccionado`);
                

                if (!status[index].completed && confirm(`¿Deseas marcar el Módulo ${index + 1} como completado? (Solo para demostración)`)) {
                    completeModule(index);
                    alert(`Módulo ${index + 1} completado. El siguiente módulo ha sido desbloqueado.`);
                } else {
                    alert(`Has seleccionado el Módulo ${index + 1}.`);
                }
            }
        });
    });
    

    const menuButton = document.querySelector('.menu-btn');
    if (menuButton) {
        menuButton.addEventListener('click', function() {
            console.log('Menú desplegado');

            alert('El menú lateral se implementará próximamente.');
        });
    }
    

    const resetButton = document.getElementById('reset-progress');
    if (resetButton) {
        resetButton.addEventListener('click', function() {
            if (confirm('¿Estás seguro de que deseas reiniciar todo el progreso? Todos los módulos volverán a estar bloqueados excepto el primero.')) {
                localStorage.removeItem('moduleStatus');
                getModuleStatus();
                updateModuleUI();
                alert('Progreso reiniciado. Solo el Módulo 1 está desbloqueado.');
            }
        });
    }
});
