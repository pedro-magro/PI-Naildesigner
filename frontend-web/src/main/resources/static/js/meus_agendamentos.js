// Aguarda o conteúdo completo da página carregar
document.addEventListener('DOMContentLoaded', () => {
    // Este script é responsável por buscar, renderizar e gerir os agendamentos.

    const token = localStorage.getItem('jwtToken');
    const agendamentosContainer = document.getElementById('agendamentos-lista'); 
    const modal = document.getElementById('confirmacaoCancelamentoModal');
    const btnConfirmarCancelamento = document.getElementById('confirmarCancelamentoBtn');
    const btnNaoCancelar = modal ? modal.querySelector('.btn-cancelar') : null;

    let agendamentoIdParaCancelar = null;

    // --- 1. Protege a página, verificando o token ---
    if (!token) {
        alert("Acesso negado. Por favor, faça login para ver seus agendamentos.");
        window.location.href = '/login';
        return; 
    }

    // --- 2. Função para buscar e exibir os agendamentos ---
    async function carregarMeusAgendamentos() {
        if (!agendamentosContainer) {
            console.error("Elemento com id 'agendamentos-lista' não foi encontrado no HTML.");
            return;
        }
        agendamentosContainer.innerHTML = '<p>A carregar os seus agendamentos...</p>';

        try {
            const response = await fetch('http://localhost:8080/api/agendamentos/meus', {
                headers: { 'Authorization': `Bearer ${token}` }
            });

            if (!response.ok) {
                const errorData = await response.json().catch(() => ({}));
                throw new Error(errorData.message || 'Falha ao carregar agendamentos.');
            }

            const agendamentos = await response.json();
            renderizarAgendamentos(agendamentos);
        } catch (error) {
            console.error('Erro em carregarMeusAgendamentos:', error);
            agendamentosContainer.innerHTML = `<p style="color: red;">${error.message}</p>`;
        }
    }

    // --- 3. Função para "desenhar" os cartões de agendamento ---
    function renderizarAgendamentos(agendamentos) {
        agendamentosContainer.innerHTML = ''; 

        if (!agendamentos || agendamentos.length === 0) {
            agendamentosContainer.innerHTML = `<div class="no-agendamentos"><p>Você não possui agendamentos.</p></div>`;
            return;
        }
        
        agendamentos.forEach(ag => {
            const card = document.createElement('div');
            card.className = 'agendamento-card';
            const dataHora = new Date(ag.dataHoraInicio);
            const dataFormatada = dataHora.toLocaleDateString('pt-BR', { timeZone: 'America/Sao_Paulo' });
            const horaFormatada = dataHora.toLocaleTimeString('pt-BR', { hour: '2-digit', minute:'2-digit', timeZone: 'America/Sao_Paulo' });
            const acoes = (ag.status !== 'CANCELADO' && ag.status !== 'CONCLUIDO')
                ? `<button class="btn-cancelar-agendamento" data-id="${ag.id}"><i class="fas fa-times-circle"></i> Cancelar</button>`
                : `<span class="agendamento-cancelado-info">Status: ${ag.status}</span>`;
            card.innerHTML = `
                <div class="card-header"><h3>Agendamento #${ag.id}</h3><span class="agendamento-status">${ag.status}</span></div>
                <div class="card-details">
                    <p><i class="fas fa-calendar-alt"></i> Data: <span>${dataFormatada}</span></p>
                    <p><i class="fas fa-clock"></i> Hora: <span>${horaFormatada}</span></p>
                    <p><i class="fas fa-cut"></i> Serviço ID: <span>#${ag.servicoId || 'N/A'}</span></p>
                    <p><i class="fas fa-user"></i> Profissional ID: <span>${(ag.profissionalId || 'N/A').toString().substring(0, 8)}...</span></p>
                </div>
                <div class="card-actions">${acoes}</div>`;
            agendamentosContainer.appendChild(card);
        });
    }
    
    // --- 4. Lógica de clique para abrir o modal ---
    if (agendamentosContainer) {
        agendamentosContainer.addEventListener('click', (event) => {
            const target = event.target.closest('.btn-cancelar-agendamento');
            if (target) {
                agendamentoIdParaCancelar = target.dataset.id;
                // Altera a forma de exibir o modal, adicionando a classe 'active'
                if (modal) modal.classList.add('active');
            }
        });
    }

    // --- 5. Lógica de cancelamento ---
    async function executarCancelamento() {
        if (!agendamentoIdParaCancelar) return;
        try {
            const response = await fetch(`http://localhost:8080/api/agendamentos/${agendamentoIdParaCancelar}/cancelar`, {
                method: 'PUT',
                headers: { 'Authorization': `Bearer ${token}` }
            });

            if (!response.ok) {
                const errorData = await response.json().catch(() => ({ message: 'Não foi possível cancelar.' }));
                throw new Error(errorData.message);
            }
            alert('Agendamento cancelado com sucesso!');
            fecharModal();
            carregarMeusAgendamentos(); 
        } catch (error) {
            console.error('Erro ao cancelar:', error);
            alert(`Erro: ${error.message}`);
            fecharModal();
        }
    }
    
    // --- 6. Funções para controlar o modal ---
    function fecharModal() {
        // Altera a forma de esconder o modal, removendo a classe 'active'
        if(modal) modal.classList.remove('active');
        agendamentoIdParaCancelar = null;
    }

    if(btnConfirmarCancelamento) btnConfirmarCancelamento.addEventListener('click', executarCancelamento);
    if(btnNaoCancelar) btnNaoCancelar.addEventListener('click', fecharModal);
    // Fecha o modal se o clique for no fundo escuro (overlay)
    if(modal) modal.addEventListener('click', (event) => {
        if(event.target === modal) fecharModal();
    });

    // --- Inicialização ---
    carregarMeusAgendamentos();
});
