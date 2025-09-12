document.addEventListener('DOMContentLoaded', function() {
    // Mapeamento dos elementos do DOM para fácil acesso
    const servicoForm = document.getElementById('servico-form');
    const servicoIdInput = document.getElementById('servico-id');
    const servicoNomeInput = document.getElementById('servico-nome');
    const servicoDescricaoInput = document.getElementById('servico-descricao');
    
    // IDs corrigidos para corresponder ao HTML
    const servicoPrecoInput = document.getElementById('servico-valor');
    const servicoDuracaoInput = document.getElementById('servico-duracao');
    const servicoImagensInput = document.getElementById('servico-imagem-url');
    
    const btnLimparForm = document.getElementById('btn-limpar-form');
    const listaServicosDiv = document.querySelector('.lista-servicos');
    const formTitulo = document.getElementById('form-titulo'); // Supondo que o seu H2 do formulário tenha este ID

    const API_BASE_URL = 'http://localhost:8080/api/servicos';
    const API_AUTH_URL = 'http://localhost:8080/api/auth'; // Adicionado para buscar perfil do usuário
    const token = localStorage.getItem('jwtToken');
    let isAdmin = false; // Flag global para controlar o acesso de administrador

    // --- Ocultar elementos de ADMIN por padrão ---
    // É recomendado que seu HTML também oculte o formulário com CSS (ex: display: none;)
    if (servicoForm) {
        servicoForm.style.display = 'none'; // Oculta o formulário de serviço por padrão
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

            // Se o usuário for um administrador, exibe o formulário
            if (servicoForm) {
                servicoForm.style.display = 'block'; // Exibe o formulário de serviço
            }

            await carregarServicos(); // Carrega e renderiza os serviços (com botões condicionais)

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


    // --- FUNÇÕES DA INTERFACE (UI) ---

    /**
     * Adiciona ou atualiza um item de serviço na lista da página.
     * @param {object} servico - O objeto de serviço com id, nome, etc.
     */
    function addOrUpdateServicoItemUI(servico) {
        let servicoItem = listaServicosDiv.querySelector(`.servico-item[data-id="${servico.id}"]`);
        
        if (!servicoItem) {
            servicoItem = document.createElement('div');
            servicoItem.classList.add('servico-item');
            listaServicosDiv.appendChild(servicoItem);
        }
        
        servicoItem.dataset.id = servico.id;

        const valorFormatado = new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' }).format(servico.preco);
        // Use uma imagem de placeholder se não houver imagens ou se a URL for inválida
        const imageUrl = (servico.imagens && servico.imagens.length > 0 && servico.imagens[0]) ? servico.imagens[0] : 'https://placehold.co/100x100/A0A0A0/FFFFFF?text=Servi%C3%A7o';

        let actionButtonsHtml = '';
        if (isAdmin) { // Apenas mostra os botões de edição/exclusão se o usuário for um administrador
            actionButtonsHtml = `
                <div class="item-actions">
                    <button class="btn-edit"><i class="fas fa-edit"></i></button>
                    <button class="btn-delete"><i class="fas fa-trash-alt"></i></button>
                </div>
            `;
        }

        servicoItem.innerHTML = `
            <img src="${imageUrl}" alt="${servico.nome}" onerror="this.onerror=null;this.src='https://placehold.co/100x100/A0A0A0/FFFFFF?text=Erro'">
            <div class="servico-info">
                <h3>${servico.nome}</h3>
                <p>${servico.descricao}</p>
                <span class="servico-meta">Valor: ${valorFormatado} | Duração: ${servico.duracao} min</span>
            </div>
            ${actionButtonsHtml}
        `;
    }

    /**
     * Limpa o formulário e o redefine para o modo "Adicionar".
     */
    function limparFormulario() {
        servicoForm.reset();
        servicoIdInput.value = '';
        if(formTitulo) formTitulo.textContent = 'Adicionar Novo Serviço';
        btnLimparForm.style.display = 'none';
        servicoNomeInput.focus(); 
    }

    /**
     * Preenche o formulário com os dados de um serviço para edição.
     * @param {object} servico - O serviço a ser editado.
     */
    function preencherFormularioParaEdicao(servico) {
        servicoIdInput.value = servico.id;
        servicoNomeInput.value = servico.nome;
        servicoDescricaoInput.value = servico.descricao;
        servicoPrecoInput.value = servico.preco;
        servicoDuracaoInput.value = servico.duracao;
        servicoImagensInput.value = (servico.imagens && servico.imagens.length > 0) ? servico.imagens.join(', ') : '';
        if(formTitulo) formTitulo.textContent = 'Editar Serviço';
        btnLimparForm.style.display = 'inline-flex';
    }

    // --- FUNÇÕES DE COMUNICAÇÃO COM A API ---

    /**
     * Carrega todos os serviços do backend e os exibe na lista.
     */
    async function carregarServicos() {
        try {
            listaServicosDiv.innerHTML = '<p>A carregar serviços...</p>';
            const response = await fetch(API_BASE_URL, {
                // GET de serviços pode ser público no backend, mas aqui exigimos token para consistência
                headers: { 'Authorization': `Bearer ${token}` }
            });
            // Lógica para lidar com 403 Forbidden se o endpoint de listar serviços for protegido no backend
            if (response.status === 403) {
                throw new Error('Você não tem permissão para listar serviços.');
            }
            if (!response.ok) throw new Error('Erro ao carregar serviços!');
            
            const servicos = await response.json();
            listaServicosDiv.innerHTML = '';
            if (servicos.length === 0) {
                listaServicosDiv.innerHTML = '<p>Nenhum serviço registado.</p>';
            } else {
                servicos.forEach(addOrUpdateServicoItemUI);
            }
        } catch (error) {
            console.error("Erro ao carregar serviços:", error);
            listaServicosDiv.innerHTML = `<p style="color: red;">${error.message}</p>`;
        }
    }

    /**
     * Envia os dados do formulário para criar ou atualizar um serviço.
     */
    async function salvarOuAtualizarServico(event) {
        event.preventDefault();
        // Verifica permissão antes de tentar salvar/atualizar
        if (!isAdmin) {
            alert('Você não tem permissão para salvar ou atualizar serviços.');
            return;
        }

        const id = servicoIdInput.value || null;
        
        const servicoPayload = {
            id,
            nome: servicoNomeInput.value,
            descricao: servicoDescricaoInput.value,
            preco: parseFloat(servicoPrecoInput.value),
            duracao: parseInt(servicoDuracaoInput.value),
            imagens: servicoImagensInput.value ? servicoImagensInput.value.split(',').map(url => url.trim()) : []
        };

        const method = id ? 'PUT' : 'POST';
        const url = id ? `${API_BASE_URL}/${id}` : API_BASE_URL;

        try {
            const response = await fetch(url, {
                method,
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${token}` // Autenticação de ADMIN necessária
                },
                body: JSON.stringify(servicoPayload),
            });
            if (!response.ok) {
                const errorData = await response.json().catch(() => ({}));
                // Lida com erros 403 Forbidden que podem vir do backend
                if (response.status === 403) {
                    throw new Error('Você não tem permissão para salvar ou atualizar serviços.');
                }
                throw new Error(errorData.message || `Falha ao ${id ? 'atualizar' : 'salvar'}.`);
            }
            
            alert(`Serviço ${id ? 'atualizado' : 'salvo'} com sucesso!`);
            limparFormulario();
            await carregarServicos();
        } catch (error) {
            console.error('Erro ao salvar:', error);
            alert(`Erro: ${error.message}`);
        }
    }

    /**
     * Busca os dados de um serviço específico para preencher o formulário de edição.
     * Esta função é acessível para usuários autenticados comuns, mas a edição só por ADMIN.
     */
    async function buscarParaEdicao(id) {
        try {
            const response = await fetch(`${API_BASE_URL}/${id}`, {
                headers: { 'Authorization': `Bearer ${token}` } // Adiciona token para buscar mesmo que GET seja público
            }); 
            if (!response.ok) {
                 if (response.status === 403) {
                     throw new Error('Você não tem permissão para buscar detalhes do serviço.');
                 }
                throw new Error('Serviço não encontrado!');
            }
            const servico = await response.json();
            preencherFormularioParaEdicao(servico);
        } catch (error) {
            alert(error.message);
        }
    }

    /**
     * Exclui um serviço após confirmação.
     */
    async function excluirServico(id) {
        // Verifica permissão antes de tentar a exclusão
        if (!isAdmin) {
            alert('Você não tem permissão para excluir serviços.');
            return;
        }

        if (!confirm(`Tem a certeza que deseja excluir o serviço com ID ${id}?`)) return;
        try {
            const response = await fetch(`${API_BASE_URL}/${id}`, {
                method: 'DELETE',
                headers: { 'Authorization': `Bearer ${token}` } // Autenticação de ADMIN necessária
            });
            
            // DELETE pode não retornar corpo, então verificamos o status.
            if (!response.ok) { // Verifica se não foi um 2xx (incluindo 204 No Content)
                const errorData = await response.json().catch(() => ({}));
                // Lida com erros 403 Forbidden que podem vir do backend
                if (response.status === 403) {
                    throw new Error('Você não tem permissão para excluir serviços.');
                }
                 throw new Error(errorData.message || 'Falha ao excluir o serviço.');
            }
            
            alert('Serviço excluído com sucesso!');
            await carregarServicos();
            if (servicoIdInput.value === id) limparFormulario();
        } catch (error) {
            alert(error.message);
        }
    }

    // --- REGISTO DE EVENTOS ---
    
    // Evento de submissão do formulário
    servicoForm.addEventListener('submit', salvarOuAtualizarServico);
    // Evento para limpar o formulário
    btnLimparForm.addEventListener('click', limparFormulario);

    // Usa delegação de eventos para os botões de editar e excluir
    listaServicosDiv.addEventListener('click', function(event) {
        const editButton = event.target.closest('.btn-edit');
        if (editButton) {
            const servicoId = editButton.closest('.servico-item').dataset.id;
            buscarParaEdicao(servicoId); // Qualquer usuário pode ver os detalhes para edição
        }
        
        const deleteButton = event.target.closest('.btn-delete');
        if (deleteButton) {
            const servicoId = deleteButton.closest('.servico-item').dataset.id;
            excluirServico(servicoId); // Apenas ADMIN pode excluir
        }
    });

    // --- INICIALIZAÇÃO ---
    // A página agora inicia verificando o acesso de ADMIN
    checkAdminAccessAndInit();
});
