document.addEventListener('DOMContentLoaded', () => {
    // Seleciona o container onde os cartões de serviço serão inseridos.
    const galeriaServicos = document.querySelector('.galeria-servicos');

    /**
     * Função assíncrona para buscar os serviços da API e iniciar a renderização.
     */
    async function carregarServicos() {
        // Garante que o container existe antes de continuar.
        if (!galeriaServicos) {
            console.error("Elemento com a classe 'galeria-servicos' não foi encontrado no HTML.");
            return;
        }

        // Exibe uma mensagem de carregamento para o utilizador.
        galeriaServicos.innerHTML = '<p class="loading-message">A carregar os nossos serviços...</p>';

        try {
            // Faz a chamada GET para o endpoint público que lista os serviços.
            const response = await fetch('http://localhost:8080/api/servicos');

            if (!response.ok) {
                throw new Error('Não foi possível carregar os serviços no momento. Tente novamente mais tarde.');
            }

            const servicos = await response.json();
            renderizarServicos(servicos);

        } catch (error) {
            console.error('Erro ao carregar serviços:', error);
            galeriaServicos.innerHTML = `<p class="error-message">${error.message}</p>`;
        }
    }

    /**
     * Função para "desenhar" cada cartão de serviço na página.
     * @param {Array} servicos - A lista de serviços recebida da API.
     */
    function renderizarServicos(servicos) {
        // Limpa a mensagem de "carregando" ou qualquer conteúdo anterior.
        galeriaServicos.innerHTML = ''; 

        if (!servicos || servicos.length === 0) {
            galeriaServicos.innerHTML = '<p>Nenhum serviço disponível no momento.</p>';
            return;
        }

        // Itera sobre cada serviço e cria o seu respectivo cartão.
        servicos.forEach(servico => {
            const card = document.createElement('div');
            card.className = 'card-servico artistic-card';

            // Formata o preço para o padrão brasileiro (R$ XX,XX).
            const precoFormatado = new Intl.NumberFormat('pt-BR', {
                style: 'currency',
                currency: 'BRL'
            }).format(servico.preco);
            
            // Pega a URL da primeira imagem da lista. Se não houver, usa uma imagem padrão.
            const imageUrl = (servico.imagens && servico.imagens.length > 0) 
                ? servico.imagens[0] 
                : '/img/logo-preto-agende-nail.png'; // Imagem de fallback

            // Cria o HTML interno do cartão com os dados dinâmicos.
            card.innerHTML = `
                <div class="card-image-wrapper">
                    <img src="${imageUrl}" alt="${servico.nome}">
                    <div class="card-overlay"></div>
                </div>
                <div class="card-content">
                    <i class="fas fa-gem card-icon"></i>
                    <h3>${servico.nome}</h3>
                    <p>${servico.descricao}</p>
                    <span class="card-price">A partir de ${precoFormatado}</span>
                    <!-- ROTA CORRIGIDA: Aponta para /agendamento com o parâmetro -->
                    <a href="/agendamento?servicoId=${servico.id}" class="btn-agendar-card">Agendar</a>
                </div>
            `;

            // Adiciona o cartão recém-criado ao container da galeria.
            galeriaServicos.appendChild(card);
        });
    }

    // Chama a função principal para iniciar todo o processo.
    carregarServicos();
});
