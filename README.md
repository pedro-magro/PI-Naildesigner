# Agende Nail: Plataforma de Agendamentos com Microsserviços
![Java](https://img.shields.io/badge/Java-17-blue) ![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.x-brightgreen) ![Spring Cloud](https://img.shields.io/badge/Spring_Cloud-2023-green) ![JWT](https://img.shields.io/badge/Security-JWT-purple) ![Docker](https://img.shields.io/badge/Docker-Ready-blue)

Uma plataforma completa para o agendamento de serviços de beleza, construída de ponta a ponta com uma arquitetura de microsserviços robusta e escalável. Este projeto demonstra a implementação de padrões de mercado para sistemas distribuídos, segurança e comunicação inter-serviços.

---

### Sobre o Projeto

O **Agende Nail** foi desenvolvido para resolver o desafio de gerir agendamentos de forma eficiente, oferecendo um portal intuitivo para os clientes e um painel de controlo completo para os administradores. A aplicação simula um ambiente de produção real, com separação de responsabilidades em serviços independentes que comunicam entre si.

### Diagrama da Arquitetura

A aplicação segue um padrão de microsserviços orquestrado por um API Gateway, com a descoberta de serviços gerida por um Eureka Server.

```
[ Cliente (Navegador) ]
        |
        v
[ API Gateway ] <--- (Porta 8080 - Ponto de Entrada Único e Segurança)
        |
        v
[ Eureka Server ] <--- (Registo e Descoberta de Serviços)
        |
        |--- [ Auth Service ] <--> (Base de Dados de Utilizadores - H2)
        |
        |--- [ Agendamento Service ] <--> (Base de Dados de Agendamentos - H2)
        |
        |--- [ Servico Service ] <--> (Base de Dados de Serviços - H2)
        |
        |--- [ Messaging Service ] ---> (Servidor de Email Externo)
        |
        |--- [ Frontend Web ] ---> (Serve as páginas HTML/Thymeleaf)
```

### Tecnologias Utilizadas

#### Backend
* **Java 17**
* **Spring Boot 3.x:** Framework principal para a criação dos microsserviços.
* **Spring Cloud:**
    * **API Gateway:** Roteamento, segurança e ponto de entrada único.
    * **Eureka Server:** Service Discovery.
    * **OpenFeign:** Comunicação síncrona e declarativa entre serviços.
* **Spring Data JPA / Hibernate:** Persistência de dados.
* **Maven:** Gestão de dependências.

#### Segurança
* **Spring Security 6:** Implementação completa de autenticação e autorização.
* **JWT (JSON Web Tokens):** Geração e validação de tokens para proteger os endpoints da API.
* **Gestão de Roles:** Separação de permissões entre `ROLE_USER` e `ROLE_ADMIN` (`@PreAuthorize`).

#### Frontend
* **Thymeleaf:** Renderização das páginas no lado do servidor.
* **JavaScript (Vanilla):** Manipulação do DOM, interatividade e chamadas assíncronas (Fetch API) para criar uma experiência de utilizador dinâmica.
* **HTML5 & CSS3:** Estrutura e estilização das páginas.

#### Base de Dados
* **H2 Database:** Banco de dados em memória para o ambiente de desenvolvimento.

#### Infraestrutura (Próximos Passos)
* **Docker & Docker Compose:** Containerização da aplicação para facilitar o deploy e a execução.

### Funcionalidades Implementadas

#### 👤 Área do Cliente
* **Autenticação:** Registo e Login com validação.
* **Visualização de Serviços:** Página dinâmica que carrega os serviços e as suas imagens a partir da base de dados.
* **Fluxo de Agendamento:**
    * Seleção de serviço que redireciona para a página de agendamento com o serviço pré-selecionado.
    * Formulário dinâmico que carrega profissionais e horários disponíveis em tempo real.
* **Gestão Pessoal:**
    * Página de "Meus Agendamentos" para visualizar e cancelar marcações.
    * Página de "Perfil" para visualizar e atualizar os seus dados pessoais.
* **Notificações:** Envio de email de confirmação na criação de um agendamento.

#### 👑 Painel de Administração
* **Gestão de Serviços (CRUD):** Interface completa para criar, listar, editar e apagar os serviços oferecidos.
* **Gestão de Utilizadores (CRUD):** Interface para gerir todas as contas de utilizadores (clientes e administradores).
* **Gestão de Disponibilidade (CRUD):** Ferramenta para o administrador bloquear dias ou horários específicos na agenda dos profissionais.
* **Gestão de Agendamentos (CRUD):** Painel central para visualizar, editar e apagar qualquer agendamento no sistema.

### Como Executar o Projeto
*(Nota: A containerização com Docker é um trabalho em progresso e o próximo passo para este projeto.)*

#### Execução Manual (via IDE)
1.  **Pré-requisitos:** Java 17 e Maven instalados.
2.  Clone este repositório: `git clone [URL_DO_SEU_REPOSITORIO]`
3.  Abra o projeto numa IDE (ex: IntelliJ ou Eclipse).
4.  Inicie cada um dos microsserviços na seguinte ordem:
    1.  `eureka-server`
    2.  `api-gateway`
    3.  `auth-service`
    4.  `servico-service`
    5.  `agendamento-service`
    6.  `messaging-service`
    7.  `frontend-web`
5.  Aceda à aplicação no seu navegador em `http://localhost:8085`.

### Próximos Passos e Melhorias
- [ ] **Implementar Testes:** Adicionar testes unitários (JUnit/Mockito) e de integração (`@SpringBootTest`) para garantir a qualidade e a robustez do código.
- [ ] **Dockerizar a Aplicação:** Criar `Dockerfiles` para cada serviço e um `docker-compose.yml` para orquestrar toda a aplicação com um único comando.
- [ ] **Migrar para um Banco de Dados Persistente:** Substituir o H2 por uma solução como o PostgreSQL.
- [ ] **Implementar o Dashboard de Admin:** Criar a página de dashboard com gráficos e estatísticas.
- [ ] **Refatorar para Comunicação Assíncrona:** Utilizar uma ferramenta de mensageria como o **RabbitMQ** ou **Kafka** para notificações e outras operações assíncronas.
