document.addEventListener('DOMContentLoaded', function() {

    const contentBody = document.querySelector('.content-body');
    const contentTitle = document.querySelector('.content-title');
    

    async function cargarContenidoCapitulo(idCapitulo) {
        try {
            const response = await fetch(`/api/capitulos/${idCapitulo}/contenido`);
            if (!response.ok) {
                throw new Error('Error al cargar el contenido del capítulo');
            }
            
            const data = await response.json();
            console.log('Datos del capítulo recibidos:', data);
            
            if (data.error) {
                throw new Error(data.error);
            }
            

            contentTitle.textContent = data.titulo || '';
            

            const oldContent = contentBody.querySelector('.chapter-content');
            if (oldContent) {
                oldContent.remove();
            }
            

            let contenidoHTML = '';
            

            {

                switch (data.tipo) {
                    case 'INTRODUCCION':
                    case 'RESUMEN':
                        contenidoHTML = crearContenidoInformativo(data);
                        break;
                    case 'TEXTO_INCOMPLETO':
                        contenidoHTML = crearTextoIncompleto(data);
                        break;
                    case 'ROMPECABEZAS':
                        contenidoHTML = crearRompecabezas(data);
                        break;
                    default:
                        contenidoHTML = `<div class="chapter-content"><p>Tipo de contenido no soportado: ${data.tipo}</p></div>`;
                }
            }
            

            const navButtons = contentBody.querySelector('.navigation-buttons');
            
            if (navButtons) {
                navButtons.insertAdjacentHTML('beforebegin', contenidoHTML);
            } else {
                contentBody.innerHTML = contenidoHTML + `
                    <div class="navigation-buttons">
                        <button class="nav-button prev">
                            <span class="nav-icon">◄</span>
                        </button>
                        <button class="nav-button next">
                            <span class="nav-icon">►</span>
                        </button>
                    </div>`;
            }
            

            if (data.tipo === 'TEXTO_INCOMPLETO') {
                inicializarTextoIncompleto(data);
            } else if (data.tipo === 'ROMPECABEZAS') {
                inicializarRompecabezas(data);
            }
            

            actualizarEventListenersNavegacion();
            
        } catch (error) {
            console.error('Error:', error);
            contentBody.innerHTML = `<p>Error al cargar el contenido: ${error.message}</p>`;
        }
    }
    

    function crearContenidoInformativo(data) {
        if (!data.contenido) {
            return '<div class="chapter-content"><p>No se encontró contenido para este capítulo.</p></div>';
        }
        
        let html = '<div class="chapter-content content-container-styled">';
        

        if (data.contenido.titulo) {
            html += `<h2>${data.contenido.titulo}</h2>`;
        }
        

        if (data.contenido.imagen_url && data.id_capitulo !== 1 && data.id_capitulo !== 5) {
            html += `
                <div class="content-image">
                    <img src="${data.contenido.imagen_url}" alt="${data.contenido.titulo || 'Imagen del capítulo'}">
                </div>`;
        }
        

        if (data.contenido.descripcion) {

            const paragraphs = data.contenido.descripcion.split('\n').filter(p => p.trim() !== '');
            const descriptionHTML = paragraphs.map(p => `<p>${p}</p>`).join('');
            html += `<div class="content-text">${descriptionHTML}</div>`;
        }
        
        html += '</div>';
        return html;
    }
    

    function crearTextoIncompleto(data) {
        if (!data.texto_incompleto) {
            return '<div class="chapter-content"><p>No se encontró contenido para este capítulo.</p></div>';
        }
        
        const textoIncompleto = data.texto_incompleto;
        const esCapituloEmoji = textoIncompleto.texto_con_huecos && textoIncompleto.texto_con_huecos.includes('🤟🏽');
        
        let html = '<div class="chapter-content texto-incompleto content-container-styled">';
        

        if (esCapituloEmoji) {
            html += '<h2>¿Qué significa este gesto?</h2>';
            

            html += '<div class="emoji-grande">🤟🏽</div>';
            
            html += '<p class="instrucciones">Selecciona el significado correcto:</p>';
        } else {
            html += '<h2>Completa el texto</h2>';
            

            html += '<div class="content-image"><img src="img/register.png" alt="Imagen de lenguaje de señas"></div>';
            
            html += '<p class="instrucciones">Selecciona la seña correcta para completar:</p>';
        }
        

        let textoConFormato = textoIncompleto.texto_con_huecos;
        

        if (!esCapituloEmoji) {

            for (let i = 1; i <= Object.keys(textoIncompleto.opciones_por_hueco).length; i++) {
                const placeholder = `[HUECO${i}]`;
                const hueco = `<span class="hueco" data-hueco="${i}">_______</span>`;
                textoConFormato = textoConFormato.replace(placeholder, hueco);
            }
            
            html += `<div class="texto-con-huecos">${textoConFormato}</div>`;
            

            html += '<div class="opciones-container">';
        } else {

            html += '<div class="opciones-contenedor">';
        }
        

        html += '</div>';
        

        html += '<div class="feedback-container"></div>';
        
        html += '</div>';
        return html;
    }
    

    function inicializarTextoIncompleto(data) {
        if (!data.texto_incompleto) return;
        
        const textoIncompleto = data.texto_incompleto;
        const opcionesPorHueco = textoIncompleto.opciones_por_hueco;
        const esCapituloEmoji = textoIncompleto.texto_con_huecos && textoIncompleto.texto_con_huecos.includes('🤟🏽');
        

        let opcionesContainer;
        if (esCapituloEmoji) {
            opcionesContainer = document.querySelector('.opciones-contenedor');
        } else {
            opcionesContainer = document.querySelector('.opciones-container');
        }
        
        const feedbackContainer = document.querySelector('.feedback-container');
        
        if (!opcionesContainer) return;
        

        let seleccionActual = null;
        

        const opciones = opcionesPorHueco['1'] || [];
        
        opciones.forEach(opcion => {
            const boton = document.createElement('button');
            

            if (esCapituloEmoji) {
                boton.className = 'opcion-btn';
            } else {
                boton.className = 'emoji-option';
            }
            
            boton.innerHTML = opcion.texto;
            boton.dataset.opcionId = opcion.id_opcion;
            boton.dataset.correcta = opcion.es_correcta;
            

            boton.addEventListener('click', function() {

                const selector = esCapituloEmoji ? '.opcion-btn' : '.emoji-option';
                document.querySelectorAll(selector).forEach(btn => {
                    btn.classList.remove('selected', 'seleccionado');
                });
                

                if (esCapituloEmoji) {
                    this.classList.add('seleccionado');
                } else {
                    this.classList.add('selected');
                }
                

                seleccionActual = {
                    id: this.dataset.opcionId,
                    texto: this.innerHTML,
                    correcta: this.dataset.correcta === 'true'
                };
                

                if (!esCapituloEmoji) {
                    const hueco = document.querySelector('.hueco[data-hueco="1"]');
                    if (hueco) {
                        hueco.innerHTML = this.innerHTML;
                        hueco.classList.remove('correcta', 'incorrecta');
                    }
                }
                

                verificarRespuesta();
            });
            
            opcionesContainer.appendChild(boton);
        });
        
        // Función para verificar si la respuesta es correcta
        function verificarRespuesta() {
            if (!seleccionActual) {
                feedbackContainer.innerHTML = '';
                return;
            }
            
            if (esCapituloEmoji) {
                // Para el capítulo del emoji
                const botonSeleccionado = document.querySelector('.opcion-btn.seleccionado');
                
                if (seleccionActual.correcta) {
                    botonSeleccionado.classList.add('correcta');
                    feedbackContainer.innerHTML = '<div class="feedback correcto">¡Correcto! 🤟🏽 significa "Te amo" en lenguaje de señas.</div>';
                } else {
                    botonSeleccionado.classList.add('incorrecta');
                    feedbackContainer.innerHTML = '<div class="feedback incorrecto">Incorrecto. 🤟🏽 significa "Te amo" en lenguaje de señas.</div>';
                    
                    // Resaltar la opción correcta
                    document.querySelectorAll('.opcion-btn').forEach(btn => {
                        if (btn.dataset.correcta === 'true') {
                            btn.classList.add('correcta');
                        }
                    });
                }
            } else {
                // Para capítulos normales de texto incompleto
                const hueco = document.querySelector('.hueco[data-hueco="1"]');
                
                if (seleccionActual.correcta) {
                    hueco.classList.add('correcta');
                    feedbackContainer.innerHTML = '<div class="feedback correcto">¡Correcto! Has elegido la seña correcta.</div>';
                } else {
                    hueco.classList.add('incorrecta');
                    feedbackContainer.innerHTML = '<div class="feedback incorrecto">Selección incorrecta. Intenta con otra seña.</div>';
                }
            }
        }
    }
    
    // Crear contenido para rompecabezas
    function crearRompecabezas(data) {
        if (!data.rompecabezas) {
            return '<div class="chapter-content"><p>No se encontró contenido para este capítulo.</p></div>';
        }
        
        const rompecabezas = data.rompecabezas;
        
        let html = '<div class="chapter-content rompecabezas-container content-container-styled">';
        html += '<h2>Rompecabezas</h2>';
        
        if (rompecabezas.descripcion) {
            html += `<p class="instrucciones">${rompecabezas.descripcion}</p>`;
        } else {
            html += '<p class="instrucciones">Arrastra las piezas para completar el rompecabezas.</p>';
        }
        
        // Contenedor del rompecabezas
        html += `<div class="rompecabezas" data-imagen="${rompecabezas.imagen_url}" data-piezas="${rompecabezas.numero_piezas}">`;
        html += '<div class="piezas-contenedor"></div>';
        html += '<div class="objetivo-contenedor">';
        html += `<img src="${rompecabezas.imagen_url}" alt="Imagen objetivo" class="imagen-objetivo"/>`;
        html += '</div>';
        html += '</div>';
        
        html += '<button class="reiniciar-btn">Reiniciar rompecabezas</button>';
        html += '<div class="feedback-container"></div>';
        
        html += '</div>';
        return html;
    }
    
    // Crear contenido para rompecabezas de prueba con imagen fija (capitulo.png)
    function crearRompecabezasPrueba() {
        // Probar con diferentes formatos de ruta para encontrar la correcta
        const imagenUrl = 'img/login.png'; // Usar otra imagen que sabemos existe
        const numeroPiezas = 9; // 3x3 grid
        
        return `
            <div class="chapter-content rompecabezas-container content-container-styled">
                <h2>Rompecabezas</h2>
                <p>Arrastra las piezas para completar el rompecabezas.</p>
                
                <div class="rompecabezas" data-imagen="${imagenUrl}" data-piezas="${numeroPiezas}">
                    <div class="piezas-contenedor"></div>
                    <div class="feedback-container"></div>
                    <button class="reiniciar-btn">Reiniciar rompecabezas</button>
                </div>
                
                <!-- Imagen oculta para precargar (no visible) -->
                <img src="${imagenUrl}" alt="Imagen del rompecabezas" style="display: none; width: 0; height: 0;" id="imagen-rompecabezas">
            </div>
        `;
    }
    
    // Inicializar la funcionalidad del rompecabezas
    function inicializarRompecabezas(data) {
        let imagenUrl;
        let numeroPiezas;
        
        if (data && data.rompecabezas) {
            // Recibir los datos desde la API
            imagenUrl = data.rompecabezas.imagen_url;
            // Si la URL empieza con '/', la dejamos como está, de lo contrario añadimos la barra
            if (imagenUrl && !imagenUrl.startsWith('/') && !imagenUrl.startsWith('http')) {
                imagenUrl = '/' + imagenUrl;
            }
            numeroPiezas = data.rompecabezas.numero_piezas || 9; // Por defecto 9 piezas
        } else {
            // Fallback en caso de error, no deberíamos llegar aquí
            console.error('No se recibieron datos del rompecabezas desde la API');
            imagenUrl = '/img/insignia.png'; // Imagen de respaldo
            numeroPiezas = 9;
        }
        
        console.log('Ruta de la imagen del rompecabezas:', imagenUrl);
        
        const piezasContainer = document.querySelector('.piezas-contenedor');
        const feedbackContainer = document.querySelector('.feedback-container');
        const reiniciarBtn = document.querySelector('.reiniciar-btn');
        
        if (!piezasContainer || !imagenUrl) return;
        
        console.log('Inicializando rompecabezas con imagen:', imagenUrl);
        
        // Número de filas y columnas para el grid (raíz cuadrada del número de piezas)
        const gridSize = Math.sqrt(numeroPiezas);
        const piezas = [];
        
        // Precargar la imagen antes de crear las piezas
        const imagenRompecabezas = new Image();
        imagenRompecabezas.src = imagenUrl;
        imagenRompecabezas.onload = function() {
            console.log('Imagen del rompecabezas cargada correctamente');
            crearPiezas();
        };
        imagenRompecabezas.onerror = function(e) {
            console.error('Error al cargar la imagen del rompecabezas:', imagenUrl);
            console.error('Detalles del error:', e);
            
            // Intentar cargar una imagen alternativa
            console.log('Intentando con imagen de respaldo...');
            imagenUrl = '/img/insignia.png'; // Usar la misma imagen que configuramos en la base de datos
            const imagenRespaldo = new Image();
            imagenRespaldo.src = imagenUrl;
            imagenRespaldo.onload = function() {
                console.log('Imagen de respaldo cargada correctamente');
                imagenRompecabezas.src = imagenUrl; // Actualizar la referencia
                crearPiezas();
            };
            imagenRespaldo.onerror = function() {
                // Si también falla la imagen de respaldo, mostrar mensaje de error
                console.error('Error al cargar incluso la imagen de respaldo');
                if (piezasContainer) {
                    piezasContainer.innerHTML = `<div class="error-message">Error al cargar las imágenes. Por favor, recarga la página.</div>`;
                }
            };
        };
        
        // Funciones para manejar el arrastre y soltar
        function handleDragStart(e) {
            this.classList.add('dragging');
            e.dataTransfer.setData('text/plain', this.dataset.currentPosition);
        }
        
        function handleDragOver(e) {
            e.preventDefault();
        }
        
        function handleDrop(e) {
            e.preventDefault();
            const draggedPos = e.dataTransfer.getData('text/plain');
            const dropPos = this.dataset.currentPosition;
            
            // Obtener elementos por posición actual
            const draggedElement = document.querySelector(`.pieza[data-current-position="${draggedPos}"]`);
            const dropTarget = this;
            
            if (draggedElement && dropTarget !== draggedElement) {
                // Intercambiar las posiciones en el DOM
                const parent = piezasContainer;
                
                // Guardar la posición temporal en la grilla
                const tempPosition = draggedElement.dataset.currentPosition;
                draggedElement.dataset.currentPosition = dropTarget.dataset.currentPosition;
                dropTarget.dataset.currentPosition = tempPosition;
                
                // Realizar intercambio visual moviendo los elementos en el grid
                const children = Array.from(parent.children);
                const index1 = children.indexOf(draggedElement);
                const index2 = children.indexOf(dropTarget);
                
                // Reordenar los elementos
                if (index1 < index2) {
                    parent.insertBefore(draggedElement, dropTarget.nextSibling);
                } else {
                    parent.insertBefore(draggedElement, dropTarget);
                    parent.insertBefore(dropTarget, children[index1 + 1]);
                }
            }
            
            document.querySelectorAll('.pieza').forEach(p => p.classList.remove('dragging'));
            
            // Verificar si el rompecabezas está completo
            checkCompletion();
        }
        
        // Crear las piezas
        function crearPiezas() {
            piezasContainer.innerHTML = '';
            piezas.length = 0;
            
            // Preparar los índices para ser desordenados
            const indices = [];
            for (let i = 0; i < numeroPiezas; i++) {
                indices.push(i);
            }
            
            // Desordenar los índices (algoritmo Fisher-Yates)
            for (let i = indices.length - 1; i > 0; i--) {
                const j = Math.floor(Math.random() * (i + 1));
                [indices[i], indices[j]] = [indices[j], indices[i]];
            }
            
            // Crear piezas en orden aleatorio
            for (let i = 0; i < numeroPiezas; i++) {
                const pieceIndex = indices[i]; // Índice desordenado
                
                const pieza = document.createElement('div');
                pieza.className = 'pieza';
                pieza.dataset.index = pieceIndex; // Posición correcta
                pieza.dataset.currentPosition = i; // Posición actual
                
                // Calcular la posición de la pieza en la imagen original
                const row = Math.floor(pieceIndex / gridSize);
                const col = pieceIndex % gridSize;
                
                // Crear una imagen que mostrará solo la porción correspondiente
                const innerImg = document.createElement('img');
                innerImg.src = imagenUrl;
                innerImg.style.position = 'absolute';
                innerImg.style.width = `${gridSize * 100}%`; // 300% para un grid 3x3
                innerImg.style.height = `${gridSize * 100}%`;
                innerImg.style.objectFit = 'cover';
                innerImg.style.left = `${-col * 100}%`; // Desplazamiento horizontal
                innerImg.style.top = `${-row * 100}%`;  // Desplazamiento vertical
                innerImg.draggable = false; // Evitar que la imagen sea arrastrable
                
                pieza.appendChild(innerImg);
                
                // Se eliminaron los números de debugging
                
                // Añadir la pieza al contenedor en la posición actual
                piezasContainer.appendChild(pieza);
                piezas.push(pieza);
                
                // Hacer la pieza arrastrable
                pieza.draggable = true;
                pieza.addEventListener('dragstart', handleDragStart);
                pieza.addEventListener('dragover', handleDragOver);
                pieza.addEventListener('drop', handleDrop);
                pieza.addEventListener('dragend', checkCompletion);
            }
        }
        
        // Verificar si el rompecabezas está completo
        function checkCompletion() {
            let correcto = true;
            
            // Obtener todas las piezas en orden actual
            const piezasActuales = Array.from(piezasContainer.querySelectorAll('.pieza'));
            
            // Verificar si cada pieza está en su posición correcta
            piezasActuales.forEach((pieza, posicionActual) => {
                const posicionCorrecta = parseInt(pieza.dataset.index);
                // Si la posición actual no coincide con la correcta, el rompecabezas no está completo
                if (posicionActual !== posicionCorrecta) {
                    correcto = false;
                }
            });
            
            console.log('Verificando rompecabezas - completado:', correcto);
            
            if (correcto && feedbackContainer) {
                feedbackContainer.innerHTML = '<div class="feedback correcto">¡Felicidades! Has completado el rompecabezas correctamente.</div>';
                // Agregar animación de celebración
                piezasActuales.forEach(pieza => {
                    pieza.classList.add('completed');
                });
            }
        }
        
        // Botón de reinicio
        if (reiniciarBtn) {
            reiniciarBtn.addEventListener('click', function() {
                crearPiezas();
                if (feedbackContainer) {
                    feedbackContainer.innerHTML = '';
                }
            });
        }
        
        // Si la imagen ya está en caché, iniciar el rompecabezas inmediatamente
        if (imagenRompecabezas.complete) {
            crearPiezas();
        }
        
        // Botón de reinicio
        if (reiniciarBtn) {
            reiniciarBtn.addEventListener('click', function() {
                crearPiezas();
                if (feedbackContainer) {
                    feedbackContainer.innerHTML = '';
                }
            });
        }
        
        // Inicializar el rompecabezas
        crearPiezas();
    }
    
    // Función para actualizar los event listeners de los botones de navegación
    function actualizarEventListenersNavegacion() {
        const prevButton = document.querySelector('.nav-button.prev');
        const nextButton = document.querySelector('.nav-button.next');
        
        if (prevButton) {
            prevButton.addEventListener('click', function() {
                console.log('Navegando al capítulo anterior');
                navegarCapitulo('prev');
            });
        }
        
        if (nextButton) {
            nextButton.addEventListener('click', function() {
                console.log('Navegando al capítulo siguiente');
                navegarCapitulo('next');
            });
        }
    }
    
    // Función para navegar entre capítulos
    function navegarCapitulo(direccion) {
        // Obtener el item de capítulo actualmente activo
        const activeItem = document.querySelector('.chapter-item.active');
        if (!activeItem) return;
        
        let targetItem;
        
        if (direccion === 'next') {
            targetItem = activeItem.nextElementSibling;
            if (targetItem && targetItem.classList.contains('chapter-item')) {
                triggerClickOnChapterItem(targetItem);
            } else {
                alert('Ya estás en el último capítulo de este módulo.');
            }
        } else if (direccion === 'prev') {
            targetItem = activeItem.previousElementSibling;
            if (targetItem && targetItem.classList.contains('chapter-item')) {
                triggerClickOnChapterItem(targetItem);
            } else {
                alert('Ya estás en el primer capítulo de este módulo.');
            }
        }
    }
    
    // Función para simular clic en un ítem de capítulo
    function triggerClickOnChapterItem(item) {
        if (item) {
            item.click();
        }
    }
    
    // Seleccionar todos los items de capítulos
    const chapterItems = document.querySelectorAll('.chapter-item');
    
    // Función para actualizar los títulos de los capítulos en la barra lateral
    async function actualizarTitulosCapitulos() {
        try {
            const response = await fetch('/api/capitulos');
            if (!response.ok) {
                throw new Error('Error al cargar los datos de capítulos');
            }
            
            const capitulos = await response.json();
            console.log('Datos de todos los capítulos recibidos:', capitulos);
            
            // Actualizar las descripciones de capítulos en la barra lateral
            chapterItems.forEach((item, index) => {
                const descripcionElement = item.querySelector('.chapter-description');
                if (descripcionElement && index < capitulos.length) {
                    // Usar el título real del capítulo en lugar de "Mini descripcion..."
                    descripcionElement.textContent = capitulos[index].titulo;
                }
            });
        } catch (error) {
            console.error('Error al cargar títulos de capítulos:', error);
        }
    }
    
    // Cargar los títulos de los capítulos al inicio
    actualizarTitulosCapitulos();
    
    // Agregar evento de clic a cada item de capítulo
    chapterItems.forEach((item, index) => {
        item.addEventListener('click', function() {
            // Remover la clase 'active' de todos los items
            chapterItems.forEach(i => i.classList.remove('active'));
            
            // Agregar la clase 'active' al item seleccionado
            this.classList.add('active');
            
            // Obtener el ID del capítulo (asumiendo formato "1.1", "1.2", etc.)
            const chapterNumber = this.querySelector('.chapter-number').textContent;
            console.log(`Capítulo ${chapterNumber} seleccionado`);
            
            // Extraer el ID numérico del capítulo
            // En una implementación real, se obtendría el verdadero id_capitulo de la base de datos
            const idCapitulo = index + 1;
            
            // Cargar el contenido del capítulo
            cargarContenidoCapitulo(idCapitulo);
        });
    });
    
    // Cargar automáticamente el capítulo 1 (Introducción) al iniciar
    if (chapterItems.length > 0) {
        chapterItems[0].classList.add('active');
        cargarContenidoCapitulo(1);
    }
});
