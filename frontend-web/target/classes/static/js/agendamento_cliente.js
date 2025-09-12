// Aguarda o conteúdo completo da página carregar
document.addEventListener('DOMContentLoaded', () => {

    const API_GATEWAY_BASE_URL = 'http://localhost:8080/api';
    const token = localStorage.getItem('jwtToken');

    // --- 1. PROTEÇÃO DA PÁGINA ---
    if (!token) {
        alert("Acesso negado. Por favor, faça login para aceder à página de agendamento.");
        window.location.href = '/login';
        return; 
    }

    // --- Mapeamento dos Elementos do DOM ---
    const agendamentoForm = document.getElementById('agendamento-cliente-form');
    // IMPORTANTE: O seu <select> de serviços no HTML deve ter o id="form-servico"
    const servicoSelect = document.getElementById('form-servico');
    const profissionalSelect = document.getElementById('form-profissional');
    const dataInput = document.getElementById('form-data');
    const horariosContainer = document.getElementById('horarios-disponiveis-lista');
    const horaSelecionadaInput = document.getElementById('form-hora-selecionada');
    const btnAgendar = document.getElementById('btn-agendar');
    let messageDiv = document.querySelector('.mensagem-formulario');
    if (!messageDiv) {
        messageDiv = document.createElement('div');
        messageDiv.className = 'mensagem-formulario';
        agendamentoForm.prepend(messageDiv);
    }

    // --- Funções para Carregar Dados Dinamicamente ---

    /**
     * Busca os serviços da API, preenche o dropdown e pré-seleciona
     * o serviço se um ID for passado pela URL.
     */
    async function carregarServicos() {
        try {
            const response = await fetch(`${API_GATEWAY_BASE_URL}/servicos`);
            if (!response.ok) throw new Error('Falha ao carregar serviços.');
            
            const servicos = await response.json();
            servicoSelect.innerHTML = '<option value="">Selecione um serviço...</option>';
            servicos.forEach(servico => {
                const option = document.createElement('option');
                option.value = servico.id;
                option.textContent = `${servico.nome} - R$ ${servico.preco.toFixed(2)}`;
                servicoSelect.appendChild(option);
            });
            
            // ***** LÓGICA DE PRÉ-SELEÇÃO MOVIDA PARA AQUI *****
            // Agora, ela é executada DEPOIS de o dropdown ser preenchido.
            preSelecionarServico();

        } catch (error) {
            messageDiv.textContent = "Erro ao carregar serviços.";
            messageDiv.className = 'mensagem-formulario erro';
        }
    }

    /**
     * Busca profissionais da API e preenche o respectivo dropdown.
     */
    async function carregarProfissionais() {
        try {
            // ***** LINHA CORRIGIDA *****
            // A URL agora está correta: /api/auth/profissionais
            const response = await fetch(`${API_GATEWAY_BASE_URL}/auth/profissionais`, {
                headers: { 'Authorization': `Bearer ${token}` }
            });
            if (!response.ok) throw new Error('Falha ao carregar profissionais.');

            const profissionais = await response.json();
            profissionalSelect.innerHTML = '<option value="">Selecione uma profissional...</option>';
            profissionais.forEach(prof => {
                const option = document.createElement('option');
                option.value = prof.id; 
                option.textContent = prof.username; 
                profissionalSelect.appendChild(option);
            });
        } catch (error) {
            messageDiv.textContent = "Erro ao carregar profissionais.";
            messageDiv.className = 'mensagem-formulario erro';
        }
    }
    
    /**
     * Busca e exibe os horários disponíveis com base nas seleções.
     */
    async function buscarEExibirHorarios() {
        const servicoId = servicoSelect.value;
        const profissionalId = profissionalSelect.value;
        const data = dataInput.value;

        horariosContainer.innerHTML = '';
        if (horaSelecionadaInput) horaSelecionadaInput.value = '';
        if (btnAgendar) btnAgendar.disabled = true;

        if (!servicoId || !profissionalId || !data) {
            horariosContainer.innerHTML = '<p class="info-text">Por favor, selecione serviço, profissional e data.</p>';
            return;
        }

        horariosContainer.innerHTML = '<p class="info-text">A procurar horários...</p>';

        try {
            const response = await fetch(`${API_GATEWAY_BASE_URL}/agendamentos/disponibilidade?data=${data}&servicoId=${servicoId}&profissionalId=${profissionalId}`, {
                headers: { 'Authorization': `Bearer ${token}` }
            });
            
            const disponibilidade = await response.json();
            if (!response.ok) throw new Error(disponibilidade.message || 'Não foi possível buscar os horários.');

            if (disponibilidade.horariosDisponiveis.length === 0) {
                horariosContainer.innerHTML = '<p class="info-text">Nenhum horário disponível para esta seleção.</p>';
            } else {
                horariosContainer.innerHTML = ''; 
                disponibilidade.horariosDisponiveis.forEach(hora => {
                    const radioId = `hora-${hora.replace(':', '-')}`;
                    const item = document.createElement('div');
                    item.className = 'horario-item';
                    item.innerHTML = `<input type="radio" id="${radioId}" name="horario-selecionado" value="${hora}"><label for="${radioId}">${hora}</label>`;
                    horariosContainer.appendChild(item);
                });
            }
        } catch (error) {
            horariosContainer.innerHTML = `<p class="info-text erro">${error.message}</p>`;
        }
    }

    /**
     * Lógica para pré-selecionar o serviço.
     */
    function preSelecionarServico() {
        const urlParams = new URLSearchParams(window.location.search);
        const servicoIdFromUrl = urlParams.get('servicoId');

        if (servicoIdFromUrl && servicoSelect) {
             // Define o valor do select para o ID vindo da URL.
            servicoSelect.value = servicoIdFromUrl;
            
            // Dispara o evento 'change' para que a busca por horários seja ativada.
            servicoSelect.dispatchEvent(new Event('change'));
        }
    }

    // --- LÓGICA DE SUBMISSÃO DO AGENDAMENTO ---
    if (agendamentoForm) {
        agendamentoForm.addEventListener('submit', async (event) => {
            event.preventDefault();
            btnAgendar.disabled = true;
            btnAgendar.textContent = 'A agendar...';
            
            const horaSelecionadaRadio = document.querySelector('input[name="horario-selecionado"]:checked');

            if (!horaSelecionadaRadio) {
                alert("Por favor, selecione um horário.");
                btnAgendar.disabled = false;
                btnAgendar.textContent = 'Confirmar Agendamento';
                return;
            }

            const agendamentoData = {
                profissionalId: profissionalSelect.value,
                servicoId: servicoSelect.value,
                dataHoraInicio: `${dataInput.value}T${horaSelecionadaRadio.value}:00`,
                observacoes: "" 
            };
            
            try {
                const response = await fetch(`${API_GATEWAY_BASE_URL}/agendamentos`, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json', 'Authorization': `Bearer ${token}` },
                    body: JSON.stringify(agendamentoData)
                });
                
                const responseData = await response.json();
                if (!response.ok) throw new Error(responseData.message || 'Erro ao criar agendamento.');

                // ***** ALTERAÇÃO REALIZADA *****
                // Removemos o alert e redirecionamos o utilizador diretamente.
                console.log(`Agendamento realizado com sucesso! ID: ${responseData.id}`);
                window.location.href = '/meus-agendamentos'; 

            } catch (error) {
                alert(`Erro: ${error.message}`);
                btnAgendar.disabled = false;
                btnAgendar.textContent = 'Confirmar Agendamento';
            }
        });
    }
    
    // --- Gatilhos de Eventos ---
    if (servicoSelect) servicoSelect.addEventListener('change', buscarEExibirHorarios);
    if (profissionalSelect) profissionalSelect.addEventListener('change', buscarEExibirHorarios);
    if (dataInput) dataInput.addEventListener('change', buscarEExibirHorarios);
    
    if (horariosContainer && btnAgendar) {
        horariosContainer.addEventListener('change', (event) => {
            if (event.target.name === 'horario-selecionado') {
                btnAgendar.disabled = false; 
            }
        });
    }

    // --- Inicialização da Página ---
    carregarProfissionais();
    carregarServicos(); // carregarServicos agora cuida da pré-seleção.
});
