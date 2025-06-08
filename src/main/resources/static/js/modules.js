let modules = [];
let currentPage = 1;
const modulesPerPage = 8;
const colors = [
    '#7DE4EC', '#FFE48E', '#FF9D9D', '#9DFFC1', 
    '#FFADF4', '#9DB6FF', '#D6ADFF', '#D4FF9D'
];

async function handleModuleClick(event) {
    event.preventDefault();
    event.stopPropagation();
    
    const card = event.currentTarget;
    const moduleId = parseInt(card.dataset.moduleId);
    
    if (isNaN(moduleId) || moduleId < 1) {
        console.error('ID de módulo no válido:', moduleId);
        return;
    }
    
    console.log('Módulo clickeado - ID:', moduleId);
    
    try {
        const status = await getModuleStatus();
        const moduleIndex = moduleId - 1; 
        
        console.log('Estado completo de módulos:', status);
        console.log('Estado del módulo', moduleId, ':', status[moduleIndex]);
        
        
        const moduleExists = modules.some(m => m.id === moduleId);
        if (!moduleExists) {
            console.error(`El módulo con ID ${moduleId} no existe`);
            alert('Error: Módulo no encontrado');
            return;
        }
        
        
        if (!status[moduleIndex] || status[moduleIndex].unlocked === undefined) {
            console.log(`Módulo ${moduleId} - Sin estado, permitiendo acceso`);
            
            status[moduleIndex] = { unlocked: true, completed: false };
            saveModuleStatus(status);
            window.location.href = `/modulo/${moduleId}`;
            return;
        }
        
        
        if (!status[moduleIndex].unlocked) {
            console.log(`Módulo ${moduleId} - Bloqueado`);
            alert(`El Módulo ${moduleId} está bloqueado. Completa el módulo anterior para desbloquearlo.`);
            return;
        }
        
        console.log(`Redirigiendo al módulo ${moduleId}`);
        window.location.href = `/modulo/${moduleId}`;
    } catch (error) {
        console.error('Error al manejar el clic en el módulo:', error);
        alert('Ocurrió un error al intentar acceder al módulo. Por favor, inténtalo de nuevo.');
    }
}

function initializeModuleCards() {
    document.querySelectorAll('.module-card').forEach(card => {
        card.removeEventListener('click', handleModuleClick); 
        card.addEventListener('click', handleModuleClick);
    });
}


async function getModuleStatus() {
    try {
        
        let savedStatus = [];
        const savedData = localStorage.getItem('moduleStatus');
        
        if (savedData) {
            try {
                savedStatus = JSON.parse(savedData);
                if (!Array.isArray(savedStatus)) {
                    savedStatus = [];
                }
            } catch (e) {
                console.warn('Error al parsear el estado guardado, inicializando nuevo estado');
                savedStatus = [];
            }
        }
        
        
        const moduleCount = Math.max(
            modules.length,
            document.querySelectorAll('.module-card').length,
            savedStatus.length
        ) || 1; 
        
        
        const finalStatus = [];
        for (let i = 0; i < moduleCount; i++) {
            try {
                
                const response = await fetch(`/api/modulos/${i + 1}/can-access`);
                if (response.ok) {
                    const data = await response.json();
                    console.log(`Estado de desbloqueo para módulo ${i + 1}:`, data);
                    
                    
                    finalStatus.push({
                        unlocked: data.canAccess || i === 0, 
                        completed: savedStatus[i]?.completed || false
                    });
                    continue;
                }
            } catch (error) {
                console.error(`Error al verificar el estado del módulo ${i + 1}:`, error);
            }
            
            
            if (savedStatus[i]) {
                finalStatus.push({
                    unlocked: savedStatus[i].unlocked !== undefined ? savedStatus[i].unlocked : (i === 0),
                    completed: !!savedStatus[i].completed
                });
            } else {
                finalStatus.push({
                    unlocked: i === 0, 
                    completed: false
                });
            }
        }
        
        
        if (JSON.stringify(savedStatus) !== JSON.stringify(finalStatus)) {
            localStorage.setItem('moduleStatus', JSON.stringify(finalStatus));
        }
        
        console.log('Estado de los módulos actualizado:', finalStatus);
        return finalStatus;
    } catch (error) {
        console.error('Error al obtener el estado de los módulos:', error);
        
        return [{ unlocked: true, completed: false }];
    }
}


async function updateModuleUI() {
    try {
        const status = await getModuleStatus();
        const moduleCards = document.querySelectorAll('.module-card');
        
        if (!status || status.length === 0) {
            console.warn('No hay estado de módulos disponible');
            return;
        }
        
        console.log('Actualizando UI con estado:', status);
        
        moduleCards.forEach((card, index) => {
            
            card.classList.remove('locked', 'unlocked', 'completed');
            
            const existingIcons = card.querySelectorAll('.lock-icon, .completed-icon');
            existingIcons.forEach(icon => card.removeChild(icon));
            
            if (!status[index]) {
                console.warn(`No hay estado para el módulo en el índice ${index}`);
                return;
            }
            
            const moduleStatus = status[index];
            console.log(`Actualizando UI para módulo ${index + 1}:`, moduleStatus);
            
            if (!moduleStatus.unlocked) {
                card.classList.add('locked');
                const lockIcon = document.createElement('div');
                lockIcon.className = 'lock-icon';
                lockIcon.innerHTML = '🔒';
                card.appendChild(lockIcon);
            } else {
                card.classList.add('unlocked');
                
                if (moduleStatus.completed) {
                    card.classList.add('completed');
                    const completedIcon = document.createElement('div');
                    completedIcon.className = 'completed-icon';
                    completedIcon.innerHTML = '✓';
                    card.appendChild(completedIcon);
                }
            }
        });
        
        
        initializeModuleCards();
    } catch (error) {
        console.error('Error en updateModuleUI:', error);
    }
}

function initPagination() {
    console.log('Inicializando paginación...');
    const prevBtn = document.getElementById('prevPage');
    const nextBtn = document.getElementById('nextPage');
    
    if (prevBtn) {
        const newPrevBtn = prevBtn.cloneNode(true);
        prevBtn.parentNode.replaceChild(newPrevBtn, prevBtn);
        
        newPrevBtn.addEventListener('click', () => {
            console.log('Botón Anterior clickeado');
            if (currentPage > 1) {
                currentPage--;
                console.log('Navegando a la página:', currentPage);
                displayModules(currentPage);
            }
        });
    }
    
    if (nextBtn) {
        const newNextBtn = nextBtn.cloneNode(true);
        nextBtn.parentNode.replaceChild(newNextBtn, nextBtn);
        
        newNextBtn.addEventListener('click', () => {
            console.log('Botón Siguiente clickeado');
            const totalPages = Math.ceil(modules.length / modulesPerPage);
            console.log('Página actual:', currentPage, 'Total de páginas:', totalPages);
            if (currentPage < totalPages) {
                currentPage++;
                console.log('Navegando a la página:', currentPage);
                displayModules(currentPage);
            } else {
                console.log('Ya estás en la última página');
            }
        });
    }
}

function displayModules(page) {
    console.log('Mostrando página', page, 'de módulos');
    const container = document.getElementById('modulesContainer');
    const pagination = document.getElementById('pagination');
    
    if (!modules || !Array.isArray(modules) || modules.length === 0) {
        console.log('No hay módulos para mostrar');
        if (container) container.innerHTML = '<p>No hay módulos disponibles</p>';
        if (pagination) pagination.style.display = 'none';
        return;
    }
    
    currentPage = page;
    const totalPages = Math.ceil(modules.length / modulesPerPage);
    console.log(`Mostrando página ${page} de ${totalPages} (${modules.length} módulos en total)`);
    
    if (pagination) {
        if (totalPages > 1) {
            pagination.style.display = 'flex';
            const pageInfo = document.getElementById('pageInfo');
            if (pageInfo) pageInfo.textContent = `Página ${page} de ${totalPages}`;
            
            const prevBtn = document.getElementById('prevPage');
            const nextBtn = document.getElementById('nextPage');
            
            if (prevBtn) {
                prevBtn.disabled = page === 1;
                prevBtn.style.opacity = page === 1 ? '0.5' : '1';
                prevBtn.style.cursor = page === 1 ? 'not-allowed' : 'pointer';
            }
            
            if (nextBtn) {
                nextBtn.disabled = page >= totalPages;
                nextBtn.style.opacity = page >= totalPages ? '0.5' : '1';
                nextBtn.style.cursor = page >= totalPages ? 'not-allowed' : 'pointer';
            }
            
            console.log(`Botones: Anterior ${prevBtn?.disabled ? 'deshabilitado' : 'habilitado'}, Siguiente ${nextBtn?.disabled ? 'deshabilitado' : 'habilitado'}`);
        } else {
            console.log('Ocultando paginación (solo una página)');
            pagination.style.display = 'none';
        }
    }
    
    const startIndex = (page - 1) * modulesPerPage;
    const endIndex = Math.min(startIndex + modulesPerPage, modules.length);
    const modulesToShow = modules.slice(startIndex, endIndex);
    
    if (container) {
        container.innerHTML = '';
        
        for (let i = 0; i < modulesToShow.length; i += 4) {
            const row = document.createElement('div');
            row.className = 'module-row';
            
            for (let j = 0; j < 4 && (i + j) < modulesToShow.length; j++) {
                const module = modulesToShow[i + j];
                const moduleIndex = startIndex + i + j;
                const colorIndex = moduleIndex % colors.length;
                
                const moduleCard = document.createElement('div');
                moduleCard.className = 'module-card';
                moduleCard.style.backgroundColor = colors[colorIndex];
                moduleCard.innerHTML = `
                    <h2>${module.nombre || 'Módulo sin nombre'}</h2>
                    <h1>${moduleIndex + 1}</h1>
                `;
                
                moduleCard.style.cursor = 'pointer';
                moduleCard.onclick = function() {
                    console.log('Módulo clickeado:', module.nombre || 'Sin nombre');
                    
                    window.location.href = '/modulo/' + module.id;
                };
                
                row.appendChild(moduleCard);
            }
            
            container.appendChild(row);
        }
    }
    
    updateModuleUI();
}

document.addEventListener('DOMContentLoaded', async function() {
    console.log('DOM completamente cargado');
    
    try {
        console.log('Datos de módulos disponibles (crudos):', window.modulesData);
        
        let modulesData = window.modulesData;
        if (typeof modulesData === 'string') {
            try {
                modulesData = JSON.parse(modulesData);
                console.log('Datos de módulos parseados:', modulesData);
            } catch (e) {
                console.error('Error al parsear JSON de módulos:', e);
                modulesData = [];
            }
        }
        
        if (modulesData && Array.isArray(modulesData)) {
            modules = modulesData;
            console.log('Módulos cargados correctamente. Total:', modules.length);
            
            
            initPagination();
            
            
            displayModules(1);
            
            
            try {
                console.log('Actualizando estado de módulos...');
                await loadModules(modules);
                console.log('Estado de módulos actualizado');
            } catch (error) {
                console.error('Error al actualizar el estado de los módulos:', error);
                
                const container = document.getElementById('modulesContainer');
                if (container) {
                    container.innerHTML = '<p>Error al cargar el estado de los módulos. Por favor, recarga la página.</p>';
                }
            }
            
            
            document.getElementById('prevPage')?.addEventListener('click', function() {
                if (this.disabled) return;
                currentPage--;
                displayModules(currentPage);
            });
            
            document.getElementById('nextPage')?.addEventListener('click', function() {
                if (this.disabled) return;
                currentPage++;
                displayModules(currentPage);
            });
        } else {
            console.error('Formato de datos de módulos inválido');
            const container = document.getElementById('modulesContainer');
            if (container) {
                container.innerHTML = '<p>Error: Formato de datos de módulos inválido.</p>';
            }
        }
    } catch (error) {
        console.error('Error al inicializar los módulos:', error);
        const container = document.getElementById('modulesContainer');
        if (container) {
            container.innerHTML = '<div class="alert alert-danger">Error al cargar los módulos: ' + error.message + '</div>';
        }
    }

    async function loadModules(modules) {
        console.log('Cargando módulos:', modules);
        const container = document.getElementById('modulesContainer');
        if (!container) {
            console.error('No se encontró el contenedor de módulos');
            return;
        }
        
        
        container.innerHTML = '';
        
        if (!Array.isArray(modules) || modules.length === 0) {
            console.warn('No hay módulos para mostrar');
            container.innerHTML = '<p>No hay módulos disponibles en este momento.</p>';
            return;
        }
        
        try {
            
            displayModules(1);
            
            
            await updateModuleUI();
            
            
            initializeModuleCards();
            
            console.log('Módulos cargados y estado actualizado');
        } catch (error) {
            console.error('Error al cargar los módulos:', error);
            container.innerHTML = '<p>Error al cargar los módulos. Por favor, recarga la página.</p>';
        }
    }


    

    

    function saveModuleStatus(status) {
        localStorage.setItem('moduleStatus', JSON.stringify(status));
    }
    

    
    async function verificarPuntosParaSiguienteModulo(moduleIndex) {
        try {
            
            if (moduleIndex === 0) {
                console.log('Módulo 1 siempre está desbloqueado');
                return true;
            }
            
            
            const currentModuleId = moduleIndex + 1; 
            
            
            const response = await fetch(`/api/modulos/${currentModuleId}/can-access`);
            
            if (!response.ok) {
                throw new Error(`Error al verificar acceso al módulo: ${response.statusText}`);
            }
            
            const data = await response.json();
            
            console.log(`Verificando acceso al módulo ${currentModuleId + 1}:`, {
                puedeAcceder: data.canAccess,
                mensaje: data.message || ''
            });
            
            return data.canAccess === true;
        } catch (error) {
            console.error('Error al verificar acceso al módulo:', error);
            
            return true;
        }
    }
    
    async function completeModule(moduleIndex) {
        try {
            
            const status = await getModuleStatus();
            
            if (!status || !status[moduleIndex]) {
                console.error('No se pudo completar el módulo: estado inválido');
                showNotification('Error', 'No se pudo completar el módulo: estado inválido', 'error');
                return;
            }
            
            console.log(`Completando módulo ${moduleIndex + 1}...`);
            
            
            status[moduleIndex].completed = true;
            
            
            if (moduleIndex < status.length - 1) {
                try {
                    console.log(`Verificando si se puede desbloquear el módulo ${moduleIndex + 2}...`);
                    
                    
                    const puedeDesbloquear = await verificarPuntosParaSiguienteModulo(moduleIndex);
                    
                    console.log(`¿Puede desbloquear el módulo ${moduleIndex + 2}?`, puedeDesbloquear);
                    
                    if (puedeDesbloquear) {
                        
                        if (!status[moduleIndex + 1]) {
                            status[moduleIndex + 1] = { unlocked: true, completed: false };
                        } else {
                            status[moduleIndex + 1].unlocked = true;
                        }
                        
                        console.log(`Módulo ${moduleIndex + 2} desbloqueado exitosamente.`);
                        
                        
                        showNotification('¡Módulo desbloqueado!', 'Has desbloqueado un nuevo módulo.', 'success');
                    } else {
                        console.log(`No se puede desbloquear el módulo ${moduleIndex + 2}: puntos insuficientes.`);
                        showNotification('Módulo bloqueado', 'Necesitas completar el módulo anterior con al menos 10 puntos para desbloquear este módulo.', 'warning');
                    }
                } catch (error) {
                    console.error('Error al verificar el desbloqueo del módulo:', error);
                    
                    if (!status[moduleIndex + 1]) {
                        status[moduleIndex + 1] = { unlocked: true, completed: false };
                    } else {
                        status[moduleIndex + 1].unlocked = true;
                    }
                    
                    
                    showNotification('Advertencia', 'No se pudo verificar el estado de desbloqueo. Se ha permitido el acceso al siguiente módulo.', 'warning');
                }
            } else {
                console.log('No hay más módulos para desbloquear.');
            }
            
            
            console.log('Guardando estado actualizado de los módulos...');
            saveModuleStatus(status);
            
            
            console.log('Actualizando la interfaz de usuario...');
            await updateModuleUI();
            
            console.log('Módulo completado exitosamente.');
        } catch (error) {
            console.error('Error al completar el módulo:', error);
            
            showNotification('Error', 'Ocurrió un error al procesar la acción. Por favor, inténtalo de nuevo.', 'error');
        }
    }
    

    async function updateModuleUI() {
        try {
            const status = await getModuleStatus();
            const moduleCards = document.querySelectorAll('.module-card');
            
            if (!status || status.length === 0) {
                console.warn('No hay estado de módulos disponible');
                return;
            }
            
            console.log('Actualizando UI con estado:', status);
            
            moduleCards.forEach((card, index) => {
                
                card.classList.remove('locked', 'unlocked', 'completed');
                
                const existingIcons = card.querySelectorAll('.lock-icon, .completed-icon');
                existingIcons.forEach(icon => card.removeChild(icon));
                
                if (!status[index]) {
                    console.warn(`No hay estado para el módulo en el índice ${index}`);
                    return;
                }
                
                const moduleStatus = status[index];
                console.log(`Actualizando UI para módulo ${index + 1}:`, moduleStatus);
                
                if (!moduleStatus.unlocked) {
                    card.classList.add('locked');
                    const lockIcon = document.createElement('div');
                    lockIcon.className = 'lock-icon';
                    lockIcon.innerHTML = '🔒';
                    card.appendChild(lockIcon);
                } else {
                    card.classList.add('unlocked');
                    
                    if (moduleStatus.completed) {
                        card.classList.add('completed');
                        const completedIcon = document.createElement('div');
                        completedIcon.className = 'completed-icon';
                        completedIcon.innerHTML = '✓';
                        card.appendChild(completedIcon);
                    }
                }
            });
        } catch (error) {
            console.error('Error en updateModuleUI:', error);
        }
    }
    

    const moduleCards = document.querySelectorAll('.module-card');
    

    updateModuleUI();
    

    
    
    
    initializeModuleCards();
    
    
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
