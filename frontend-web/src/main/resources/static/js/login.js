document.addEventListener('DOMContentLoaded', () => {
    // --- Lógica do Menu Hamburger (Mantida) ---
    const hamburgerToggle = document.getElementById('hamburger-toggle');
    const offCanvasMenu = document.getElementById('off-canvas-menu');
    // ... (resto da lógica do menu) ...
    if (hamburgerToggle) {
        // ... (código do menu aqui) ...
    }

    // --- LÓGICA DO FORMULÁRIO DE LOGIN (AJUSTADA) ---
    const loginForm = document.getElementById('loginForm');
    const messageDiv = document.getElementById('message');
    
    if (loginForm) {
        loginForm.addEventListener('submit', async (event) => {
            event.preventDefault(); // Impede o envio padrão

            // MODIFICADO: Pega os valores pelos IDs corretos do HTML atualizado
            const usernameOrEmail = document.getElementById('usernameOrEmail').value;
            const password = document.getElementById('password').value;

            // O payload enviado para o backend continua correto
            const loginData = {
                username: usernameOrEmail,
                password: password
            };
            
            messageDiv.textContent = 'Aguarde...';
            messageDiv.style.color = 'black';

            try {
                const response = await fetch('http://localhost:8080/api/auth/login', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(loginData)
                });
                
                const responseData = await response.json();

                if (response.ok && responseData.token) {
                    localStorage.setItem('jwtToken', responseData.token);
                    messageDiv.style.color = 'green';
                    messageDiv.textContent = 'Login bem-sucedido! Redirecionando...';
                    
                    setTimeout(() => {
                        window.location.href = '/'; // Redireciona para a página home
                    }, 1500);

                } else {
                    throw new Error(responseData.message || 'Credenciais inválidas.');
                }
            } catch (error) {
                console.error('Falha no login:', error);
                messageDiv.style.color = 'red';
                messageDiv.textContent = `Erro: ${error.message}`;
            }
        });
    }
});