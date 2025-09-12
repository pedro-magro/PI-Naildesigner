document.addEventListener('DOMContentLoaded', function() {
    // --- Configuração da API e Autenticação ---
    const API_BLOQUEIOS_URL = 'http://localhost:8080/api/bloqueios';
    const API_AUTH_URL = 'http://localhost:8080/api/auth';
    const token = localStorage.getItem('jwtToken');
    let isAdmin = false; // Flag global para controlar o acesso de administrador

    // --- Elementos do DOM ---
    const bloqueioForm = document.getElementById('bloqueio-form');
    const dataBloqueioInput = document.getElementById('data-bloqueio');
    const horaInicioBloqueioInput = document.getElementById('hora-inicio-bloqueio');
    const horaFimBloqueioInput = document.getElementById('hora-fim-bloqueio');
    const motivoBloqueioInput = document.getElementById('motivo-bloqueio');
    const profissionalSelect = document.getElementById('bloqueio-profissional');
    const listaBloqueios = document.querySelector('.lista-bloqueios');
    
    // --- Ocultar elementos de ADMIN por padrão ---
    // É recomendado que seu HTML também oculte o formulário com CSS (ex: display: none;)
    if (bloqueioForm) {
        bloqueioForm.style.display = 'none'; // Oculta o formulário de bloqueio por padrão
    }

    // --- NOVA FUNÇÃO: Verifica o acesso de ADMIN e inicializa a página ---
    async function checkAdminAccessAndInit() {
        // Primeiro, verifica se há um token
        if (!token) {
            alert("Acesso negado. Por favor, faça login.");
            window.location.href = '/login';
            return;
        }

        try {
            // Busca o perfil do usuário para obter as roles
            const userProfileResponse = await fetch(`${API_AUTH_URL}/me`, {
                headers: { 'Authorization': `Bearer ${token}` }
            });

            // Lida com sessão expirada ou token inválido
            if (userProfileResponse.status === 401) {
                localStorage.removeItem('jwtToken');
                alert('Sua sessão expirou. Faça login novamente.');
                window.location.href = '/login';
                return;
            }
            if (!userProfileResponse.ok) {
                throw new Error('Falha ao verificar perfil do usuário.');
            }

            const userData = await userProfileResponse.json();
            const userRoles = userData.roles || [];
            isAdmin = userRoles.includes('ROLE_ADMIN'); // Define a flag global isAdmin

            // --- LÓGICA DE BLOQUEIO DE ACESSO ADMIN ---
            if (!isAdmin) {
                alert('Acesso negado. Você não tem permissão para acessar esta página de administração.');
                window.location.href = '/'; // Redireciona para a página raiz
                return; // Interrompe a execução aqui
            }

            // Se o usuário for um administrador, exibe o formulário e carrega os dados
            if (bloqueioForm) {
                bloqueioForm.style.display = 'block'; // Exibe o formulário de bloqueio
            }

            // Garante que os profissionais sejam carregados ANTES de renderizar os bloqueios
            await carregarEPopularProfissionais(); 
            await renderizarBloqueios(); 

        } catch (error) {
            console.error("Erro na verificação de acesso:", error);
            if (error.message.includes('fetch') || error.message.includes('network')) {
                alert("Erro de rede ou servidor ao verificar seu perfil. Tente novamente mais tarde.");
            } else {
                localStorage.removeItem('jwtToken');
                alert("Erro crítico de autenticação ou perfil. Por favor, faça login novamente.");
                window.location.href = '/login';
            }
        }
    }


    // --- Funções de API e UI ---

    // Cache de dados de profissionais para evitar buscas repetidas
    let todosProfissionais = []; 

    /**
     * Busca a lista de profissionais (admins) do auth-service e popula o select.
     */
    async function carregarEPopularProfissionais() {
        try {
            const response = await fetch(`${API_AUTH_URL}/profissionais`, {
                headers: { 'Authorization': `Bearer ${token}` }
            });
            if (!response.ok) throw new Error('Falha ao carregar profissionais.');

            todosProfissionais = await response.json(); // Armazena no cache global
            
            profissionalSelect.innerHTML = '<option value="">Selecione a Profissional</option>';
            todosProfissionais.forEach(prof => { // Popula o select a partir do cache
                const option = document.createElement('option');
                option.value = prof.id; // Usa o UUID do profissional
                option.textContent = prof.username;
                profissionalSelect.appendChild(option);
            });
        } catch (error) {
            console.error(error);
            profissionalSelect.innerHTML = '<option value="">Erro ao carregar</option>';
            alert('Não foi possível carregar a lista de profissionais. Verifique se o auth-service está a funcionar.');
        }
    }

    /**
     * Busca todos os bloqueios e os renderiza na lista.
     */
    async function renderizarBloqueios() {
        listaBloqueios.innerHTML = '<p>A carregar bloqueios...</p>';
        try {
            const response = await fetch(API_BLOQUEIOS_URL, {
                 headers: { 'Authorization': `Bearer ${token}` }
            });

            // Lógica para lidar com 403 Forbidden se o endpoint de listar bloqueios for protegido
            if (response.status === 403) {
                throw new Error('Você não tem permissão para listar bloqueios.');
            }
            if (!response.ok) {
                throw new Error('Erro ao buscar bloqueios.');
            }
            
            const bloqueios = await response.json();
            listaBloqueios.innerHTML = ''; 
            
            if (bloqueios.length === 0) {
                 listaBloqueios.innerHTML = '<p>Nenhum período bloqueado encontrado.</p>';
            } else {
                bloqueios.sort((a, b) => {
                    const dataA = new Date(`${a.dataBloqueio}T${a.horaInicio || '00:00:00'}`);
                    const dataB = new Date(`${b.dataBloqueio}T${b.horaInicio || '00:00:00'}`);
                    return dataA - dataB;
                });
                bloqueios.forEach(addBloqueioItem);
            }
        } catch (error) {
            console.error('Erro ao carregar bloqueios:', error);
            listaBloqueios.innerHTML = `<p style="color: red;">${error.message}</p>`;
        }
    }

    /**
     * Adiciona um item de bloqueio à lista na UI.
     * @param {object} bloqueio - O objeto de bloqueio vindo da API.
     */
    function addBloqueioItem(bloqueio) {
        const newItem = document.createElement('div');
        newItem.classList.add('disponibilidade-item', 'bloqueio-item');
        newItem.dataset.id = bloqueio.id;

        if (!bloqueio.dataBloqueio) {
            console.warn("Bloqueio com ID " + bloqueio.id + " tem dados de data inválidos e foi ignorado.", bloqueio);
            newItem.innerHTML = `<span><strong>ID ${bloqueio.id}:</strong> ERRO - Dados de data inválidos.</span>`;
            listaBloqueios.appendChild(newItem);
            return;
        }
        
        const [ano, mes, dia] = bloqueio.dataBloqueio.split('-');
        const dataFormatada = `${dia}/${mes}/${ano}`;

        let periodo;
        const horaInicio = bloqueio.horaInicio;
        const horaFim = bloqueio.horaFim;

        if ((horaInicio && horaFim) && (horaInicio.startsWith('00:00') && horaFim.startsWith('23:59'))) {
             periodo = 'Dia Inteiro';
        } else if (horaInicio && horaFim) {
            periodo = `${horaInicio.substring(0, 5)} - ${horaFim.substring(0, 5)}`;
        } else {
            periodo = 'Dia Inteiro';
        }
        
        // --- CORREÇÃO APLICADA AQUI ---
        // Busca o nome do profissional diretamente no array em cache
        const profissionalEncontrado = todosProfissionais.find(p => String(p.id) === String(bloqueio.profissionalId));
        const profissionalNome = profissionalEncontrado ? profissionalEncontrado.username : 'Profissional não encontrado';


        let actionButtonsHtml = '';
        if (isAdmin) { // Apenas mostra o botão de exclusão se o usuário for um administrador
            actionButtonsHtml = `
                <div class="item-actions">
                    <button class="btn-delete" data-id="${bloqueio.id}"><i class="fas fa-trash-alt"></i></button>
                </div>
            `;
        }

        newItem.innerHTML = `
            <span><strong>${dataFormatada}:</strong> ${periodo} (${profissionalNome}) - ${bloqueio.motivo || 'Sem motivo'}</span>
            ${actionButtonsHtml}
        `;
        listaBloqueios.appendChild(newItem);
    }

    // --- Lógica de Eventos ---

    bloqueioForm.addEventListener('submit', async function(event) {
        event.preventDefault();

        // A submissão do formulário só é possível se bloqueioForm estiver visível (isAdmin é true)
        // No entanto, reforçamos a verificação aqui para segurança extra
        if (!isAdmin) {
            alert('Você não tem permissão para criar bloqueios.');
            return;
        }

        const profissionalId = profissionalSelect.value;
        if (!dataBloqueioInput.value || !profissionalId) {
            alert('Por favor, selecione a Data e a Profissional.');
            return;
        }

        let horaInicio = horaInicioBloqueioInput.value;
        let horaFim = horaFimBloqueioInput.value;

        if (horaInicio && !horaFim) {
            horaFim = '23:59';
        } else if (!horaInicio && horaFim) {
            horaInicio = '00:00';
        } else if (!horaInicio && !horaFim) {
            horaInicio = '00:00';
            horaFim = '23:59';
        }

        const bloqueioData = {
            dataBloqueio: dataBloqueioInput.value,
            horaInicio: horaInicio,
            horaFim: horaFim,
            profissionalId: profissionalId,
            motivo: motivoBloqueioInput.value || "Bloqueio manual"
        };

        try {
            const response = await fetch(API_BLOQUEIOS_URL, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${token}`
                },
                body: JSON.stringify(bloqueioData)
            });

            if (!response.ok) {
                const errorData = await response.json().catch(() => ({}));
                // Lida com erros 403 Forbidden que podem vir do backend
                if (response.status === 403) {
                    throw new Error('Você não tem permissão para criar bloqueios.');
                }
                throw new Error(errorData.message || 'Falha ao criar o bloqueio.');
            }
            
            alert('Período bloqueado com sucesso!');
            await renderizarBloqueios();
            bloqueioForm.reset();
        } catch (error) {
            alert(`Erro ao bloquear período: ${error.message}`);
        }
    });

    listaBloqueios.addEventListener('click', async function(event) {
        const deleteButton = event.target.closest('.btn-delete');
        if (!deleteButton) return;

        // Verifica permissão antes de tentar a exclusão
        if (!isAdmin) {
            alert('Você não tem permissão para remover bloqueios.');
            return;
        }

        const bloqueioId = deleteButton.dataset.id;
        if (confirm(`Tem certeza que deseja remover o bloqueio com ID ${bloqueioId}?`)) {
            try {
                const response = await fetch(`${API_BLOQUEIOS_URL}/${bloqueioId}`, {
                    method: 'DELETE',
                    headers: { 'Authorization': `Bearer ${token}` }
                });

                if (!response.ok) {
                    const errorData = await response.json().catch(() => ({}));
                    // Lida com erros 403 Forbidden que podem vir do backend
                    if (response.status === 403) {
                        throw new Error('Você não tem permissão para remover bloqueios.');
                    }
                    throw new Error(errorData.message || 'Falha ao remover o bloqueio.');
                }
                
                alert(`Bloqueio removido com sucesso!`);
                await renderizarBloqueios();
            } catch (error) {
                alert(`Erro ao remover bloqueio: ${error.message}`);
            }
        }
    });

    // --- Inicialização ---
    // A página agora inicia verificando o acesso de ADMIN
    checkAdminAccessAndInit();
});
