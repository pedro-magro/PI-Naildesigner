document.addEventListener('DOMContentLoaded', function() {
    // --- Configuração da API e Autenticação ---
    // ROTA CORRIGIDA: Aponta para /api/auth, que é a base do seu AuthController.
    const API_USERS_URL = 'http://localhost:8080/api/auth/users'; 
    const API_AUTH_URL = 'http://localhost:8080/api/auth'; // Adicionado para buscar perfil do usuário
    const token = localStorage.getItem('jwtToken');
    let isAdmin = false; // Flag global para controlar o acesso de administrador

    // --- Mapeamento dos Elementos do DOM ---
    const usuarioForm = document.getElementById('usuario-form');
    const usuarioIdInput = document.getElementById('usuario-id');
    const usuarioNomeInput = document.getElementById('usuario-nome');
    const usuarioEmailInput = document.getElementById('usuario-email');
    const usuarioTelefoneInput = document.getElementById('usuario-telefone');
    const usuarioSenhaInput = document.getElementById('usuario-senha');
    const usuarioTipoSelect = document.getElementById('usuario-tipo');
    const btnLimparForm = document.getElementById('btn-limpar-form');
    const listaUsuarios = document.querySelector('.lista-usuarios');
    
    // --- Ocultar elementos de ADMIN por padrão ---
    // É recomendado que seu HTML também oculte o formulário com CSS (ex: display: none;)
    if (usuarioForm) {
        usuarioForm.style.display = 'none'; // Oculta o formulário de usuário por padrão
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
            if (usuarioForm) {
                usuarioForm.style.display = 'block'; // Exibe o formulário de usuário
            }

            await carregarUsuarios(); // Carrega e renderiza os usuários (com botões condicionais)

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


    // --- Funções da Interface (UI) ---

    function addOrUpdateUsuarioItem(usuario) {
        let usuarioItem = listaUsuarios.querySelector(`.usuario-item[data-id="${usuario.id}"]`);
        if (!usuarioItem) {
            usuarioItem = document.createElement('div');
            usuarioItem.classList.add('usuario-item');
            listaUsuarios.appendChild(usuarioItem);
        }
        
        usuarioItem.dataset.id = usuario.id;
        const fotoUrl = usuario.fotoUrl || 'https://placehold.co/100x100/A0A0A0/FFFFFF?text=Usu%C3%A1rio'; // Placeholder para foto

        let actionButtonsHtml = '';
        if (isAdmin) { // Apenas mostra os botões de edição/exclusão se o usuário for um administrador
            actionButtonsHtml = `
                <div class="item-actions">
                    <button class="btn-edit"><i class="fas fa-edit"></i></button>
                    <button class="btn-delete"><i class="fas fa-trash-alt"></i></button>
                </div>
            `;
        }

        usuarioItem.innerHTML = `
            <img src="${fotoUrl}" alt="Foto de Perfil" onerror="this.onerror=null;this.src='https://placehold.co/100x100/A0A0A0/FFFFFF?text=Erro'">
            <div class="usuario-info">
                <h3>${usuario.username}</h3>
                <p>Email: ${usuario.email}</p>
                <span class="usuario-meta">Telefone: ${usuario.phone || 'N/A'} | Tipo: ${usuario.role}</span>
            </div>
            ${actionButtonsHtml}
        `;
    }

    function limparFormulario() {
        usuarioForm.reset();
        usuarioIdInput.value = '';
        usuarioSenhaInput.setAttribute('placeholder', '********');
        btnLimparForm.style.display = 'none';
        document.getElementById('btn-salvar-usuario').innerHTML = '<i class="fas fa-save"></i> Salvar Usuário';
    }

    function preencherFormularioParaEdicao(usuario) {
        usuarioIdInput.value = usuario.id;
        usuarioNomeInput.value = usuario.username;
        usuarioEmailInput.value = usuario.email;
        usuarioTelefoneInput.value = usuario.phone || '';
        usuarioSenhaInput.value = '';
        usuarioSenhaInput.setAttribute('placeholder', 'Deixe em branco para manter a senha atual');
        usuarioTipoSelect.value = usuario.role;
        btnLimparForm.style.display = 'inline-flex';
        document.getElementById('btn-salvar-usuario').innerHTML = '<i class="fas fa-save"></i> Atualizar Usuário';
    }

    // --- Funções de Comunicação com a API ---

    async function carregarUsuarios() {
        try {
            listaUsuarios.innerHTML = '<p>Carregando usuários...</p>';
            const response = await fetch(API_USERS_URL, {
                headers: { 'Authorization': `Bearer ${token}` }
            });
            // Lida com erros 403 Forbidden que podem vir do backend (se a rota for protegida para listar)
            if (response.status === 403) {
                throw new Error('Você não tem permissão para listar usuários.');
            }
            if (!response.ok) throw new Error('Falha ao carregar usuários.');
            
            const usuarios = await response.json();
            listaUsuarios.innerHTML = '';
            if (usuarios.length === 0) {
                listaUsuarios.innerHTML = '<p>Nenhum usuário cadastrado.</p>';
            } else {
                usuarios.forEach(addOrUpdateUsuarioItem);
            }
        } catch (error) {
            console.error("Erro ao carregar usuários:", error);
            listaUsuarios.innerHTML = `<p style="color: red;">${error.message}</p>`;
        }
    }

    async function salvarOuAtualizarUsuario(event) {
        event.preventDefault();
        // Verifica permissão antes de tentar salvar/atualizar
        if (!isAdmin) {
            alert('Você não tem permissão para salvar ou atualizar usuários.');
            return;
        }

        const id = usuarioIdInput.value || null;
        
        const password = usuarioSenhaInput.value;
        const usuarioPayload = {
            id,
            username: usuarioNomeInput.value,
            email: usuarioEmailInput.value,
            phone: usuarioTelefoneInput.value,
            role: usuarioTipoSelect.value
        };

        if (password) {
            usuarioPayload.password = password;
        }

        const method = id ? 'PUT' : 'POST';
        const url = id ? `${API_USERS_URL}/${id}` : API_USERS_URL;

        try {
            const response = await fetch(url, {
                method,
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${token}`
                },
                body: JSON.stringify(usuarioPayload),
            });
            if (!response.ok) {
                const errorData = await response.json().catch(() => ({}));
                // Lida com erros 403 Forbidden que podem vir do backend
                if (response.status === 403) {
                    throw new Error('Você não tem permissão para salvar ou atualizar usuários.');
                }
                throw new Error(errorData.message || 'Falha na operação.');
            }
            
            alert(`Usuário ${id ? 'atualizado' : 'salvo'} com sucesso!`);
            limparFormulario();
            await carregarUsuarios();
        } catch (error) {
            console.error('Erro ao salvar:', error);
            alert(`Erro: ${error.message}`);
        }
    }

    async function buscarParaEdicao(id) {
        try {
            const response = await fetch(`${API_USERS_URL}/${id}`, {
                headers: { 'Authorization': `Bearer ${token}` }
            });
            if (!response.ok) {
                 if (response.status === 403) {
                     throw new Error('Você não tem permissão para buscar detalhes do usuário.');
                 }
                throw new Error('Usuário não encontrado!');
            }
            const usuario = await response.json();
            preencherFormularioParaEdicao(usuario);
        } catch (error) {
            alert(error.message);
        }
    }

    async function excluirUsuario(id) {
        // Verifica permissão antes de tentar a exclusão
        if (!isAdmin) {
            alert('Você não tem permissão para excluir usuários.');
            return;
        }

        if (!confirm(`Tem certeza que deseja excluir o usuário com ID ${id}?`)) return;
        try {
            const response = await fetch(`${API_USERS_URL}/${id}`, {
                method: 'DELETE',
                headers: { 'Authorization': `Bearer ${token}` }
            });
            if (!response.ok) {
                const errorData = await response.json().catch(() => ({}));
                // Lida com erros 403 Forbidden que podem vir do backend
                if (response.status === 403) {
                    throw new Error('Você não tem permissão para excluir usuários.');
                }
                throw new Error(errorData.message || 'Falha ao excluir o usuário.');
            }
            
            alert('Usuário excluído com sucesso!');
            await carregarUsuarios();
            if (usuarioIdInput.value === id) limparFormulario();
        } catch (error) {
            alert(error.message);
        }
    }

    // --- Registo de Eventos ---
    
    usuarioForm.addEventListener('submit', salvarOuAtualizarUsuario);
    btnLimparForm.addEventListener('click', limparFormulario);

    listaUsuarios.addEventListener('click', function(event) {
        const editButton = event.target.closest('.btn-edit');
        if (editButton) {
            const usuarioId = editButton.closest('.usuario-item').dataset.id;
            buscarParaEdicao(usuarioId); // Qualquer usuário pode ver os detalhes para edição (se backend permitir)
        }
        
        const deleteButton = event.target.closest('.btn-delete');
        if (deleteButton) {
            const usuarioId = deleteButton.closest('.usuario-item').dataset.id;
            excluirUsuario(usuarioId); // Apenas ADMIN pode excluir
        }
    });

    // --- Inicialização ---
    // A página agora inicia verificando o acesso de ADMIN
    checkAdminAccessAndInit();
});
