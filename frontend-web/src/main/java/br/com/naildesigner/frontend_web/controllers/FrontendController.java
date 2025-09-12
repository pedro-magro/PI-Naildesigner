package br.com.naildesigner.frontend_web.controllers; // Ajuste o nome do pacote conforme seu projeto

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class FrontendController {

    // --- Rotas para as páginas de ADMIN ---

    @GetMapping("/admin/admin_dashboard")
    public String adminDashboardPage() {
        return "admin/admin_dashboard"; // Mapeia para src/main/resources/templates/admin/admin_dashboard.html
    }

    @GetMapping("/admin/agendamentos_admin") // Use um nome mais simples para a URL
    public String adminAgendamentosPage() {
        return "admin/agendamentos_admin"; // Mapeia para src/main/resources/templates/admin/agendamentos_admin.html
    }

    @GetMapping("/admin/disponibilidade_admin") // Use um nome mais simples para a URL
    public String adminDisponibilidadePage() {
        return "admin/disponibilidade_admin"; // Mapeia para src/main/resources/templates/admin/disponibilidade_admin.html
    }

    @GetMapping("/admin/servicos_admin") // Use um nome mais simples para a URL
    public String adminServicosPage() {
        return "admin/servicos_admin"; // Mapeia para src/main/resources/templates/admin/servicos_admin.html
    }

    @GetMapping("/admin/usuarios_admin") // Use um nome mais simples para a URL
    public String adminUsuariosPage() {
        return "admin/usuarios_admin"; // Mapeia para src/main/resources/templates/admin/usuarios_admin.html
    }

    // --- Rotas para as páginas de AUTH (Autenticação) ---
    // (Presumindo nomes comuns para páginas de autenticação)
    @GetMapping("/login")
    public String authLoginPage() {
        return "auth/login"; // Exemplo: mapeia para src/main/resources/templates/auth/login.html
    }

    @GetMapping("/cadastro")
    public String authRegisterPage() {
        return "auth/cadastro"; // Exemplo: mapeia para src/main/resources/templates/auth/register.html
    }
    // Adicione mais se tiver páginas como recuperação de senha, etc.


    // --- Rotas para as páginas de CLIENTES (e páginas gerais) ---

    @GetMapping("/") // Rota principal da aplicação, geralmente a landing page
    public String landingPage() {
        return "clientes/landing-page"; // Mapeia para src/main/resources/templates/clientes/landing-page.html
    }

    @GetMapping("/agendamento") // Página para o cliente fazer agendamento
    public String clienteAgendamentoPage() {
        return "clientes/agendamento"; // Mapeia para src/main/resources/templates/clientes/agendamento.html
    }

    @GetMapping("/meus-agendamentos") // Página para o cliente ver seus agendamentos
    public String clienteMeusAgendamentosPage() {
        // Corrigindo o nome do arquivo que parece ser "meus-agendamentos.h.html"
        return "clientes/meus-agendamentos"; // Assumindo que o nome do arquivo é meus-agendamentos.html
    }

    @GetMapping("/perfil") // Página de perfil do cliente (ou admin, dependendo do contexto)
    public String perfilPage() {
        return "clientes/perfil"; // Mapeia para src/main/resources/templates/clientes/perfil.html
    }

    @GetMapping("/servicos") // Página de listagem de serviços para clientes
    public String clienteServicosPage() {
        return "clientes/servicos"; // Mapeia para src/main/resources/templates/clientes/servicos.html
    }

    @GetMapping("/sobre")
    public String sobrePage() {
        return "clientes/sobre"; // Mapeia para src/main/resources/templates/clientes/sobre.html
    }

    // --- Exemplo de Rota para /perfil se for a mesma para admin e cliente, ou para um perfil genérico ---
    // Se '/perfil' for uma página única que se adapta, você pode ter um único mapeamento.
    // Se for um perfil diferente para admin e cliente, e ambos usam a mesma URL,
    // você pode precisar de lógica de autenticação/autorização para redirecionar.
    // Por enquanto, o mapeamento acima em 'clientes/perfil' já cobre uma URL /perfil.
}