// Aguarda o conteúdo completo da página carregar
document.addEventListener('DOMContentLoaded', () => {

    // --- LÓGICA DO MENU HAMBURGER ---
    const hamburgerToggle = document.getElementById('hamburger-toggle');
    const offCanvasMenu = document.getElementById('off-canvas-menu');
    const offCanvasClose = document.getElementById('off-canvas-close');
    const menuOverlay = document.getElementById('menu-overlay');

    if (hamburgerToggle && offCanvasMenu && offCanvasClose && menuOverlay) {
        function openMenu() {
            if (offCanvasMenu) offCanvasMenu.classList.add('active');
            if (menuOverlay) menuOverlay.classList.add('active');
            document.body.style.overflow = 'hidden'; 
        }

        function closeMenu() {
            if (offCanvasMenu) offCanvasMenu.classList.remove('active');
            if (menuOverlay) menuOverlay.classList.remove('active');
            document.body.style.overflow = ''; 
        }

        hamburgerToggle.addEventListener('click', openMenu);
        offCanvasClose.addEventListener('click', closeMenu);
        menuOverlay.addEventListener('click', closeMenu);
    }
    
    // --- LÓGICA DO MODAL DE FEEDBACK ---
    const feedbackModal = document.getElementById('feedbackModal');
    const modalTitle = document.getElementById('modalTitle');
    const modalMessage = document.getElementById('modalMessage');
    const closeModalButton = feedbackModal ? feedbackModal.querySelector('.close-button') : null;

    /**
     * Exibe o modal de feedback e gerencia o redirecionamento após sucesso.
     * @param {string} type 'success' ou 'error'
     * @param {string} title Título do modal
     * @param {string} message Mensagem a ser exibida
     */
    function showModal(type, title, message) {
        // Se os elementos do modal não forem encontrados, usa alert como fallback
        if (!feedbackModal || !modalTitle || !modalMessage) {
            alert(`${title}: ${message}`);
            // O redirecionamento aqui só ocorre no fallback do alert, não é o ideal para o fluxo principal
            if (type === 'success') {
                 setTimeout(() => { window.location.href = '/login'; }, 3000);
            }
            return;
        }
        
        modalTitle.textContent = title;
        modalMessage.textContent = message;
        modalTitle.className = `modal-title ${type}`;
        feedbackModal.classList.add('active'); // Ativa a exibição do modal

        // --- MOVEMOS O REDIRECIONAMENTO PARA AQUI ---
        // O redirecionamento só acontece se o modal for exibido e for uma mensagem de sucesso
        if (type === 'success') {
            // Aumentei o tempo para 4 segundos para dar mais tempo ao usuário ler.
            // Ajuste este valor conforme a necessidade.
            setTimeout(() => {
                window.location.href = '/login';
            }, 4000); // Redireciona 4 segundos APÓS o modal ser ativado
        }
    }

    if (closeModalButton) {
        const closeAction = () => feedbackModal.classList.remove('active');
        closeModalButton.addEventListener('click', closeAction);
        // Opcional: Fechar modal e limpar redirecionamento se houver um timer pendente
        // Se o usuário fechar o modal manualmente, podemos cancelar o redirecionamento.
        // No entanto, para cadastro, o redirecionamento é geralmente desejado.
    }

    // --- LÓGICA DO FORMULÁRIO DE CADASTRO ---
    const cadastroForm = document.getElementById('cadastroForm');
    const messageDiv = document.getElementById('form-message');
    
    if (cadastroForm && messageDiv) {
        cadastroForm.addEventListener('submit', async (event) => {
            event.preventDefault();

            const username = document.getElementById('nome').value; 
            const phone = document.getElementById('telefone').value;
            const email = document.getElementById('email').value;
            const password = document.getElementById('senha').value;
            
            const registerData = { username, email, password, phone };
            
            messageDiv.textContent = 'A processar o seu registo...';
            messageDiv.style.color = 'black'; // Garante que a mensagem de processamento seja visível

            try {
                const response = await fetch('http://localhost:8080/api/auth/register', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(registerData)
                });

                const resultText = await response.text();
                messageDiv.textContent = ''; // Limpa a mensagem "processando"

                if (response.ok) {
                    showModal('success', 'Sucesso!', `${resultText}. Você será redirecionado para a tela de login.`);
                    // REMOVIDO: O setTimeout para redirecionamento NÃO ESTÁ MAIS AQUI
                } else {
                    // Se o backend retornar um erro (ex: usuário já existe)
                    throw new Error(resultText || `Erro no servidor: ${response.status}`);
                }
            } catch (error) {
                messageDiv.textContent = ''; // Limpa a mensagem "processando"
                showModal('error', 'Erro no Cadastro', error.message);
            }
        });
    }
});
