// Aguarda o conteúdo da página carregar completamente
document.addEventListener('DOMContentLoaded', () => {
    // --- Lógica do Menu Hamburger (para consistência) ---
    const hamburgerToggle = document.getElementById('hamburger-toggle');
    const offCanvasMenu = document.getElementById('off-canvas-menu');
    const offCanvasClose = document.getElementById('off-canvas-close');
    const menuOverlay = document.getElementById('menu-overlay');

    if (hamburgerToggle && offCanvasMenu && offCanvasClose && menuOverlay) {
        const openMenu = () => {
            offCanvasMenu.classList.add('active');
            menuOverlay.classList.add('active');
            document.body.style.overflow = 'hidden'; 
        };
        const closeMenu = () => {
            offCanvasMenu.classList.remove('active');
            menuOverlay.classList.remove('active');
            document.body.style.overflow = ''; 
        };
        hamburgerToggle.addEventListener('click', openMenu);
        offCanvasClose.addEventListener('click', closeMenu);
        menuOverlay.addEventListener('click', closeMenu);
    }

    // --- Lógica da Página de Perfil ---
    const token = localStorage.getItem('jwtToken');
    console.log("Token a ser utilizado:", token); // Log para depuração
    
    // Mapeamento dos elementos do seu formulário HTML
    const nomeInput = document.getElementById('nome');
    const emailInput = document.getElementById('email');
    const telefoneInput = document.getElementById('telefone');
    const senhaInput = document.getElementById('senha');
    const logoutButton = document.getElementById('logoutButton');
    const perfilForm = document.querySelector('.formulario-perfil');
    
    // Cria uma div para mensagens, caso não exista no seu HTML
    let messageDiv = document.getElementById('form-message');
    if (!messageDiv && perfilForm) {
        messageDiv = document.createElement('div');
        messageDiv.id = 'form-message';
        messageDiv.className = 'form-message'; // Usando uma classe para estilização
        perfilForm.prepend(messageDiv);
    }

    // 1. Proteger a Página e Carregar Dados
    if (!token) {
        alert("Acesso negado. Por favor, faça login.");
        window.location.href = '/login';
        return;
    }

    async function carregarDadosDoUsuario() {
        try {
            const response = await fetch('http://localhost:8080/api/auth/me', {
                headers: { 
                    'Authorization': `Bearer ${token}` 
                }
            });

            // Tratamento de erro mais específico para o status 401
            if (response.status === 401) {
                 throw new Error('Sua sessão expirou ou é inválida. Faça login novamente.');
            }
            if (response.status === 403) {
                 throw new Error('Você não tem permissão para acessar este recurso.');
            }
            if (!response.ok) {
                throw new Error(`Não foi possível carregar os dados do perfil. Status: ${response.status}`);
            }
            
            const userData = await response.json();
            
            // Preenche os campos do formulário
            if (nomeInput) nomeInput.value = userData.username || '';
            if (emailInput) emailInput.value = userData.email || '';
            if (telefoneInput) telefoneInput.value = userData.phone || '';

        } catch (error) {
            console.error("Erro em carregarDadosDoUsuario:", error);
            localStorage.removeItem('jwtToken'); // Limpa o token inválido
            alert(error.message);
            window.location.href = '/login';
        }
    }

    // 2. Lógica de Logout
    if (logoutButton) {
        logoutButton.addEventListener('click', () => {
            localStorage.removeItem('jwtToken'); 
            alert('Você saiu da sua conta.');
            window.location.href = '/login';
        });
    }

    // 3. Lógica para Submeter o Formulário de Edição
    if (perfilForm) {
        perfilForm.addEventListener('submit', async (event) => {
            event.preventDefault();
            messageDiv.textContent = 'A salvar alterações...';
            messageDiv.className = 'form-message form-message-info';

            const updateData = {
                username: nomeInput.value,
                phone: telefoneInput.value
            };

            if (senhaInput.value && senhaInput.value.trim() !== '') {
                updateData.password = senhaInput.value;
            }

            try {
                const response = await fetch('http://localhost:8080/api/auth/me', {
                    method: 'PUT',
                    headers: {
                        'Content-Type': 'application/json',
                        'Authorization': `Bearer ${token}`
                    },
                    body: JSON.stringify(updateData)
                });
                
                const resultText = await response.text();
                if (!response.ok) {
                    throw new Error(resultText || `Erro ${response.status}`);
                }
                
                messageDiv.className = 'form-message form-message-success';
                messageDiv.textContent = resultText; 

                senhaInput.value = '';

            } catch (error) {
                console.error("Erro ao atualizar perfil:", error);
                messageDiv.className = 'form-message form-message-error';
                messageDiv.textContent = `Erro: ${error.message}`;
            }
        });
    }

    // Carrega os dados do utilizador ao abrir a página
    carregarDadosDoUsuario();
});
