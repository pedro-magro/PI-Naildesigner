document.addEventListener('DOMContentLoaded', function() {
    const API_AUTH_URL = 'http://localhost:8080/api/auth';
    const token = localStorage.getItem('jwtToken');

    // --- Lógica do Menu Hamburger (Replicada ou movida de menu.js se for o caso) ---
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

    // --- Função para verificar o acesso e carregar a página ---
    async function checkAdminAccessAndLoadPage() {
        // 1. Verifica se há um token JWT no localStorage
        if (!token) {
            alert("Acesso negado. Por favor, faça login.");
            window.location.href = '/login'; // Redireciona para a página de login
            return; // Interrompe a execução
        }

        try {
            // 2. Faz uma requisição para a API de autenticação para obter os detalhes do perfil
            const userProfileResponse = await fetch(`${API_AUTH_URL}/me`, {
                headers: { 'Authorization': `Bearer ${token}` }
            });

            // Adicionado LOG para inspecionar o corpo da resposta raw
            const responseText = await userProfileResponse.text();
            console.log("Resposta RAW de /auth/me:", responseText); // Loga o texto da resposta

            // Tenta converter a resposta para JSON APENAS SE HOUVER CONTEÚDO
            let userData = {};
            if (responseText) {
                try {
                    userData = JSON.parse(responseText);
                    console.log("Resposta JSON PARSEADA de /auth/me:", userData); // Loga o objeto JSON
                } catch (jsonError) {
                    console.error("Erro ao fazer parse do JSON da resposta:", jsonError, "Resposta:", responseText);
                    throw new Error("Formato de resposta inesperado do servidor.");
                }
            } else {
                console.warn("Resposta de /auth/me estava vazia.");
            }

            // Lida com sessão expirada (401 Unauthorized) ou outros problemas na resposta OK
            if (userProfileResponse.status === 401) {
                localStorage.removeItem('jwtToken'); // Limpa o token inválido
                alert('Sua sessão expirou. Faça login novamente.');
                window.location.href = '/login';
                return;
            }
            // Se o status não for 200 OK (e não 401), trata como erro genérico do servidor
            if (!userProfileResponse.ok) {
                throw new Error('Falha ao verificar perfil do usuário: ' + responseText);
            }

            const userRoles = userData.roles || []; // Garante que userRoles seja um array
            const isAdmin = userRoles.includes('ROLE_ADMIN'); // Verifica se a role 'ROLE_ADMIN' está presente

            // --- LÓGICA PRINCIPAL DE BLOQUEIO DE ACESSO ---
            if (!isAdmin) {
                // Se o usuário NÃO for um administrador, exibe um alerta e redireciona
                alert('Acesso negado. Você não tem permissão para acessar esta página de administração.');
                window.location.href = '/'; // Redireciona para a página inicial (raiz do site)
                return; // Interrompe a execução
            }

            // 4. Se o usuário for um administrador, a página é liberada
            document.body.classList.add('admin-loaded'); // Adiciona uma classe ao body para torná-lo visível (definido no CSS)
            console.log("Acesso concedido: Usuário é administrador.");

        } catch (error) {
            console.error("Erro na verificação de acesso:", error);
            // Mensagens de erro mais específicas para o usuário
            if (error.message.includes('fetch') || error.message.includes('network')) {
                alert("Erro de rede ou servidor ao verificar seu perfil. Tente novamente mais tarde.");
            } else {
                localStorage.removeItem('jwtToken'); // Em caso de erro crítico no perfil, invalida o token
                alert("Erro crítico de autenticação ou perfil. Por favor, faça login novamente.");
                window.location.href = '/login';
            }
        }
    }

    // --- Inicialização ---
    // Inicia o processo de verificação de acesso assim que o DOM estiver completamente carregado
    checkAdminAccessAndLoadPage();
});
