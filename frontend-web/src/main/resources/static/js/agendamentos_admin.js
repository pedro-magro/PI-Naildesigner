document.addEventListener('DOMContentLoaded', function() {
    // --- Configuração da API e Autenticação ---
    const API_URL = 'http://localhost:8080/api';
    const token = localStorage.getItem('jwtToken');
    let isAdmin = false; // Flag para controlar o acesso de administrador globalmente

    // --- Mapeamento dos Elementos do DOM ---
    const agendamentoForm = document.getElementById('agendamento-form');
    const agendamentoIdInput = document.getElementById('agendamento-id');
    const clienteSelect = document.getElementById('agendamento-cliente');
    const servicoSelect = document.getElementById('agendamento-servico');
    const profissionalSelect = document.getElementById('agendamento-profissional');
    const dataInput = document.getElementById('agendamento-data');
    const horaInput = document.getElementById('agendamento-hora');
    const statusSelect = document.getElementById('agendamento-status');
    const observacoesTextarea = document.getElementById('agendamento-observacoes');
    const btnLimparForm = document.getElementById('btn-limpar-form');
    const listaAgendamentos = document.querySelector('.lista-agendamentos');
    
    // --- Ocultar elementos de ADMIN por padrão (se existirem) ---
    // Você deve garantir que seu HTML oculta este formulário por padrão com CSS (ex: display: none;)
    // O JS só o exibirá se o usuário for ADMIN.
    if (agendamentoForm) {
        agendamentoForm.style.display = 'none'; // Oculta o formulário de agendamento por padrão
    }

    // --- Verificação de Segurança (Inicial - token) ---
    if (!token) {
        alert("Acesso negado. Por favor, faça login.");
        window.location.href = '/login';
        return;
    }

    // --- Cache de Dados ---
    let todosClientes = [];
    let todosProfissionais = [];
    let todosServicos = [];

    // --- Funções de Carregamento e UI ---

    // **NOVA FUNÇÃO: Verifica o acesso de ADMIN e inicializa a página**
    async function checkAdminAccessAndInit() {
        try {
            // Primeiro, verifica o perfil do usuário para obter as roles
            const userProfileResponse = await fetch(`${API_URL}/auth/me`, {
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
                // Lida com outras respostas não-OK da API de perfil
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
            if (agendamentoForm) {
                agendamentoForm.style.display = 'block'; // Exibe o formulário de agendamento
            }

            await carregarDadosParaFormularios(); // Carrega clientes, profissionais, serviços
            await carregarAgendamentos(); // Carrega e renderiza agendamentos (com botões condicionais)

        } catch (error) {
            console.error("Erro na verificação de acesso:", error);
            // Mensagens de erro mais específicas
            if (error.message.includes('fetch') || error.message.includes('network')) {
                alert("Erro de rede ou servidor ao verificar seu perfil. Tente novamente mais tarde.");
            } else {
                localStorage.removeItem('jwtToken');
                alert("Erro crítico de autenticação ou perfil. Por favor, faça login novamente.");
                window.location.href = '/login';
            }
        }
    }


    // Carrega todos os dados necessários para os formulários
    async function carregarDadosParaFormularios() {
        try {
            const headers = { 'Authorization': `Bearer ${token}` };

            const [usersResponse, servicosResponse] = await Promise.all([
                fetch(`${API_URL}/auth/users`, { headers }),
                fetch(`${API_URL}/servicos`, { headers })
            ]);

            if (!usersResponse.ok) throw new Error('Falha ao carregar usuários.');
            const todosUsuarios = await usersResponse.json();
            
            todosClientes = todosUsuarios.filter(u => u.role === 'USER');
            todosProfissionais = todosUsuarios.filter(u => u.role === 'ADMIN');

            if (!servicosResponse.ok) throw new Error('Falha ao carregar serviços.');
            todosServicos = await servicosResponse.json();

            popularSelect(clienteSelect, todosClientes, 'Selecione o Cliente', 'id', 'username');
            popularSelect(profissionalSelect, todosProfissionais, 'Selecione a Profissional', 'id', 'username');
            popularSelect(servicoSelect, todosServicos, 'Selecione o Serviço', 'id', 'nome');

        } catch (error) {
            console.error("Erro ao carregar dados para formulários:", error);
            alert("Erro ao inicializar a página. Verifique a conexão com os serviços.");
        }
    }
    
    function popularSelect(selectElement, items, placeholder, valueField, textField) {
        selectElement.innerHTML = `<option value="">${placeholder}</option>`;
        items.forEach(item => {
            const option = document.createElement('option');
            option.value = item[valueField];
            option.textContent = item[textField];
            selectElement.appendChild(option);
        });
    }

    // Carrega e exibe todos os agendamentos na lista
    async function carregarAgendamentos() {
        listaAgendamentos.innerHTML = '<p>Carregando agendamentos...</p>';
        try {
            const response = await fetch(`${API_URL}/agendamentos`, { 
                headers: { 'Authorization': `Bearer ${token}` } 
            });

            if (!response.ok) {
                if (response.status === 403) {
                    throw new Error('Acesso negado. Você não tem permissão de administrador para listar agendamentos.');
                }
                throw new Error('Falha ao carregar agendamentos.');
            }
            
            const agendamentos = await response.json();
            renderizarListaAgendamentos(agendamentos);
        } catch (error) {
            console.error("Erro ao carregar agendamentos:", error);
            listaAgendamentos.innerHTML = `<p style="color: red;">${error.message}</p>`;
        }
    }
    
    // **Modificada para renderizar botões de ação condicionalmente**
    function renderizarListaAgendamentos(agendamentos) {
        listaAgendamentos.innerHTML = '';
        if (!agendamentos || agendamentos.length === 0) {
            listaAgendamentos.innerHTML = '<p>Nenhum agendamento encontrado.</p>';
            return;
        }

        agendamentos.sort((a, b) => new Date(b.dataHoraInicio) - new Date(a.dataHoraInicio));
        agendamentos.forEach(addAgendamentoItemUI);
    }
    
    // **Modificada para adicionar/ocultar botões de edição/exclusão**
    function addAgendamentoItemUI(agendamento) {
        let item = listaAgendamentos.querySelector(`.agendamento-item[data-id="${agendamento.id}"]`);
        if (!item) {
            item = document.createElement('div');
            item.className = 'agendamento-item';
            listaAgendamentos.appendChild(item);
        }
        item.dataset.id = agendamento.id;

        const cliente = todosClientes.find(c => String(c.id) === String(agendamento.clienteId))?.username || 'Cliente não encontrado';
        const profissional = todosProfissionais.find(p => String(p.id) === String(agendamento.profissionalId))?.username || 'Profissional não encontrado';
        const servico = todosServicos.find(s => String(s.id) === String(agendamento.servicoId))?.nome || 'Serviço não encontrado';


        const data = new Date(agendamento.dataHoraInicio);
        const dataFormatada = data.toLocaleDateString('pt-BR');
        const horaFormatada = data.toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' });
        
        const statusClass = `status-${agendamento.status.toLowerCase()}`;

        let actionButtonsHtml = '';
        if (isAdmin) { // Apenas mostra os botões se o usuário for um administrador
            actionButtonsHtml = `
                <div class="item-actions">
                    <button class="btn-edit"><i class="fas fa-edit"></i></button>
                    <button class="btn-delete"><i class="fas fa-trash-alt"></i></button>
                </div>
            `;
        }

        item.innerHTML = `
            <div class="agendamento-info">
                <h3>${servico} com ${cliente}</h3>
                <p>Profissional: ${profissional}</p>
                <p>Data: ${dataFormatada} às ${horaFormatada}</p>
                <span class="agendamento-meta ${statusClass}">Status: ${agendamento.status}</span>
            </div>
            ${actionButtonsHtml}
        `;
    }

    // ***** LÓGICA DE SUBMISSÃO E AÇÕES ADICIONADA *****

    function limparFormulario() {
        agendamentoForm.reset();
        agendamentoIdInput.value = '';
        btnLimparForm.style.display = 'none';
        document.getElementById('btn-salvar-agendamento').innerHTML = '<i class="fas fa-save"></i> Salvar Agendamento';
    }

    async function preencherFormularioParaEdicao(id) {
        // Esta função só será chamada se os botões de edição forem visíveis (ou seja, isAdmin é true)
        try {
            const response = await fetch(`${API_URL}/agendamentos/${id}`, { headers: { 'Authorization': `Bearer ${token}` } });
            if (!response.ok) throw new Error('Agendamento não encontrado.');
            
            const agendamento = await response.json();
            agendamentoIdInput.value = agendamento.id;
            clienteSelect.value = agendamento.clienteId;
            servicoSelect.value = agendamento.servicoId;
            profissionalSelect.value = agendamento.profissionalId;
            statusSelect.value = agendamento.status;
            observacoesTextarea.value = agendamento.observacoes || '';

            const dataHora = new Date(agendamento.dataHoraInicio);
            dataInput.value = dataHora.toISOString().split('T')[0];
            horaInput.value = dataHora.toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' });

            btnLimparForm.style.display = 'inline-flex';
            document.getElementById('btn-salvar-agendamento').innerHTML = '<i class="fas fa-save"></i> Atualizar Agendamento';
        } catch (error) {
            alert(error.message);
        }
    }
    
    agendamentoForm.addEventListener('submit', async (event) => {
        event.preventDefault(); 
        // A submissão do formulário só é possível se agendamentoForm estiver visível (isAdmin é true)

        const id = agendamentoIdInput.value || null;
        const dataHoraInicio = `${dataInput.value}T${horaInput.value}:00`;

        const agendamentoPayload = {
            id,
            clienteId: clienteSelect.value,
            servicoId: servicoSelect.value,
            profissionalId: profissionalSelect.value,
            dataHoraInicio: dataHoraInicio,
            status: statusSelect.value,
            observacoes: observacoesTextarea.value
        };

        const method = id ? 'PUT' : 'POST';
        const url = id ? `${API_URL}/agendamentos/${id}` : `${API_URL}/agendamentos/admin`; // Note a rota de POST/admin

        try {
            const response = await fetch(url, {
                method,
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${token}`
                },
                body: JSON.stringify(agendamentoPayload)
            });

            if (!response.ok) {
                const errorData = await response.json().catch(() => ({}));
                throw new Error(errorData.message || 'Falha ao salvar o agendamento.');
            }
            
            alert(`Agendamento ${id ? 'atualizado' : 'criado'} com sucesso!`);
            limparFormulario();
            await carregarAgendamentos();
        } catch (error) {
            alert(`Erro: ${error.message}`);
        }
    });

    btnLimparForm.addEventListener('click', limparFormulario);

    listaAgendamentos.addEventListener('click', async (event) => {
        const editButton = event.target.closest('.btn-edit');
        if (editButton) {
            const id = editButton.closest('.agendamento-item').dataset.id;
            await preencherFormularioParaEdicao(id);
        }

        const deleteButton = event.target.closest('.btn-delete');
        if (deleteButton) {
            const id = deleteButton.closest('.agendamento-item').dataset.id;
            if (confirm('Tem certeza que deseja excluir este agendamento?')) {
                try {
                    const response = await fetch(`${API_URL}/agendamentos/${id}`, {
                        method: 'DELETE',
                        headers: { 'Authorization': `Bearer ${token}` }
                    });
                    if (!response.ok) throw new Error('Falha ao excluir o agendamento.');
                    alert('Agendamento excluído com sucesso!');
                    await carregarAgendamentos();
                } catch (error) {
                    alert(`Erro: ${error.message}`);
                }
            }
        }
    });

    // --- Inicialização da Página ---
    // A página agora inicia verificando o acesso de ADMIN
    checkAdminAccessAndInit();
});
