# Sistema de Gestão Acadêmica — Backend + Frontend

Sistema de Gestão Acadêmica da UCSAL, desenvolvido para a disciplina de Projeto de Software. O projeto cobre autenticação JWT, gestão acadêmica, matrícula com regras de negócio, notas, frequência, relatórios, avisos, denúncias, auditoria e chatbot com Gemini.

> **Escopo desta entrega:** API REST em Spring Boot (`SPEC-BACKEND.md`) e SPA em Angular 22 (`SPEC-FRONTEND.md`). O backend serve o build do frontend em `src/main/resources/static` e faz fallback para `index.html` nas rotas do SPA.

---

## Stack

| Item | Tecnologia |
|---|---|
| Linguagem | Java 21 |
| Framework | Spring Boot 4.1.2 |
| Build | Maven (wrapper incluído) |
| Banco | PostgreSQL 16 |
| Persistência | Spring Data JPA / Hibernate |
| Segurança | Spring Security, JWT HS256, BCrypt |
| Documentação | springdoc-openapi / Swagger UI |
| Testes | JUnit 5, Spring Boot Test, H2 |
| IA | Gemini via `RestClient` |
| Frontend | Angular 22 standalone, Angular Material, SCSS |
| Gráficos | `ng2-charts` + Chart.js (apenas indicadores) |

---

## Estrutura do repositório

```
gestao-academica/
├── backend/                  # aplicação Spring Boot
│   ├── src/main/java/br/ucsal/gestaoacademica/
│   │   ├── auth/             # login e usuário autenticado
│   │   ├── usuario/          # usuários, alunos e professores
│   │   ├── academico/        # cursos, disciplinas, turmas, matrículas, calendário
│   │   ├── avaliacao/        # avaliações, notas e cálculos
│   │   ├── frequencia/       # aulas e registros de presença
│   │   ├── comunicacao/      # avisos
│   │   ├── denuncia/         # denúncias e andamentos
│   │   ├── relatorio/        # boletim, histórico, dashboards e indicadores
│   │   ├── chatbot/          # integração com o Gemini
│   │   ├── auditoria/        # logs de auditoria e segurança
│   │   ├── security/         # JWT, filtros e utilitários
│   │   ├── common/           # erros padronizados e exceções
│   │   └── config/           # segurança, OpenAPI, SPA fallback e seed
│   ├── src/test/             # testes automatizados
│   └── pom.xml
├── db/init/01-dump.sql       # schema + dados de demonstração
├── frontend/                 # SPA Angular 22 (standalone + Angular Material)
│   ├── src/app/core/         # auth, HTTP, modelos, serviços e layout
│   ├── src/app/features/     # telas por perfil (aluno, professor, gestão, denúncias, admin)
│   ├── src/app/shared/       # componentes, pipes e utilitários reutilizáveis
│   └── proxy.conf.json       # proxy de desenvolvimento para http://localhost:8080
├── Dockerfile                # build multi-stage: Angular + Spring Boot
├── docker-compose.yml        # PostgreSQL + aplicação
├── .env.example
└── README.md
```

---

## Como rodar

### Pré-requisitos

- Docker Desktop / Docker Engine com Compose
- Java 21 (apenas para rodar fora do Docker)
- Node.js 22 + npm (apenas para rodar o frontend fora do Docker)
- Opcional: chave da API do Gemini

### Modo demo/entrega (Docker)

```bash
cp .env.example .env
# edite o .env se quiser definir JWT_SECRET e GEMINI_API_KEY
docker compose up --build
```

Acesse:

- Aplicação: <http://localhost:8080/>
- Swagger UI: <http://localhost:8080/swagger-ui.html>
- OpenAPI JSON: <http://localhost:8080/v3/api-docs>
- API: `http://localhost:8080/api/*`

> O build do Angular é copiado para `src/main/resources/static` durante a imagem Docker. A mesma origem serve o SPA e a API, sem URLs fixas no frontend.

Na primeira subida, o PostgreSQL executa automaticamente `db/init/01-dump.sql`, criando o schema e carregando os dados de demonstração. A aplicação sobe com `ddl-auto=validate`, ou seja, o schema do dump é validado contra as entidades JPA.

Se a porta `5432` já estiver ocupada por um PostgreSQL local:

```bash
POSTGRES_PORT=5433 docker compose up --build
```

O app continua usando `db:5432` internamente; apenas a porta exposta no host muda.

Para parar:

```bash
docker compose down          # mantém os dados
docker compose down -v       # apaga o volume; o dump será recarregado na próxima subida
```

### Modo desenvolvimento (sem container da aplicação)

```bash
docker compose up -d db

cd backend
./mvnw spring-boot:run
```

Se o banco estiver exposto em outra porta do host:

```bash
DB_PORT=5433 ./mvnw spring-boot:run
```

Com o backend em execução, rode o frontend em outro terminal:

```bash
cd frontend
npm install
npm start
```

O Angular sobe em <http://localhost:4200/> e encaminha `/api/**` para `http://localhost:8080` via `proxy.conf.json`.

Para gerar o build de produção:

```bash
cd frontend
npm run build
# saída: frontend/dist/frontend/browser
```

---

## Credenciais de demonstração

Todos os usuários usam a senha **`Demo@123`** (hash BCrypt no dump).

| Perfil | E-mail |
|---|---|
| ADMIN | `admin@ucsal.br` |
| COORDENADOR | `coordenador@ucsal.br` |
| SECRETARIA | `secretaria@ucsal.br` |
| PROFESSOR | `professor@ucsal.br` |
| PROFESSOR | `professor2@ucsal.br` |
| ALUNO | `aluno@ucsal.br` (Ana) |
| ALUNO | `aluno2@ucsal.br` (Bruno) |
| ALUNO | `aluno3@ucsal.br` (Carla) |
| ALUNO | `aluno4@ucsal.br` (Diego) |
| ALUNO | `aluno5@ucsal.br` (Elisa) |
| ALUNO | `aluno6@ucsal.br` (Felipe) |

Exemplo de login:

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"admin@ucsal.br","senha":"Demo@123"}'
```

Use o token retornado no header `Authorization: Bearer <token>`. No Swagger, clique em **Authorize** e informe o token.

---

## Roteiro rápido para a apresentação

1. **Admin** — `GET /api/usuarios`, `GET /api/admin/logs-seguranca` e `GET /api/admin/logs-auditoria`.
2. **Aluno (`aluno@ucsal.br`)** — `GET /api/aluno/dashboard`, `/notas`, `/frequencia` e `/historico`. Ana tem 7 presenças em 9 aulas (77,8%) e o alerta de frequência é exibido.
3. **Matrícula com regras** — entre como `aluno6@ucsal.br` e envie `POST /api/matriculas` com `{"turmaId":3}`:
   - a turma 3 (Estrutura de Dados, 2026.2) está lotada → HTTP **422** com `motivo: "SEM_VAGA"` e a turma 4 como alternativa;
   - repetindo com `{"turmaId":4}`, a matrícula é criada.
   - `aluno3@ucsal.br` tentando a turma 3 recebe `PREREQUISITO` (Carla foi reprovada em Algoritmos);
   - `aluno@ucsal.br` na turma 3 recebe `JA_MATRICULADO`.
4. **Professor (`professor@ucsal.br`)** — `GET /api/professor/turmas`, `GET /api/turmas/3/relatorio`, lançamento de notas em `PUT /api/turmas/3/notas`.
5. **Coordenação (`coordenador@ucsal.br`)** — `GET /api/indicadores/disciplinas`, denúncias e estatísticas em `/api/denuncias`.
6. **Denúncia anônima** — `POST /api/denuncias` retorna um protocolo `DEN-2026-0000xx`, sem gravar o denunciante.
7. **Chatbot** — `POST /api/chatbot/perguntas` com `{"pergunta":"..."}`. Sem `GEMINI_API_KEY`, retorna 503; com `GEMINI_FALLBACK=true` ou perfil `demo`, devolve uma resposta padrão.

---

## Principais endpoints

Todas as rotas exigem JWT, exceto `POST /api/auth/login`.

| Método | Rota | Perfis |
|---|---|---|
| POST | `/api/auth/login` | público |
| GET | `/api/auth/me` | todos |
| GET/POST | `/api/usuarios` | ADMIN, SECRETARIA |
| GET/PUT | `/api/usuarios/{id}` | ADMIN, SECRETARIA |
| PATCH | `/api/usuarios/{id}/ativo` | ADMIN, SECRETARIA |
| CRUD | `/api/cursos`, `/api/disciplinas` | leitura: todos; escrita: SECRETARIA, COORDENADOR, ADMIN |
| CRUD | `/api/turmas` | leitura: todos; escrita: SECRETARIA, COORDENADOR, ADMIN |
| GET | `/api/aluno/turmas-disponiveis` | ALUNO |
| POST | `/api/matriculas` | ALUNO, SECRETARIA, ADMIN |
| DELETE | `/api/matriculas/{id}` | ALUNO (própria), SECRETARIA, ADMIN |
| GET | `/api/aluno/dashboard`, `/notas`, `/frequencia`, `/historico` | ALUNO |
| GET | `/api/professor/turmas` | PROFESSOR |
| GET | `/api/turmas/{id}/alunos` | PROFESSOR (dono), gestão |
| GET/POST | `/api/turmas/{id}/avaliacoes` | PROFESSOR (dono) |
| PUT | `/api/turmas/{id}/notas` | PROFESSOR (dono); COORDENADOR/SECRETARIA após encerramento |
| GET/POST | `/api/turmas/{id}/aulas` | PROFESSOR (dono) |
| PUT | `/api/aulas/{id}/frequencia` | PROFESSOR (dono) |
| GET | `/api/relatorios/boletim/{matriculaId}` | ALUNO (próprio), gestão |
| GET | `/api/relatorios/historico/{alunoId}` | ALUNO (próprio), gestão |
| GET | `/api/relatorios/turma/{turmaId}` | PROFESSOR (dono), COORDENADOR, SECRETARIA |
| GET | `/api/indicadores/disciplinas` | COORDENADOR |
| GET | `/api/indicadores/turmas/{id}` | COORDENADOR, PROFESSOR |
| GET/POST | `/api/avisos` | leitura: todos; escrita: PROFESSOR (suas turmas), COORDENADOR, SECRETARIA, ADMIN |
| POST | `/api/denuncias` | todos |
| GET | `/api/denuncias/protocolo/{protocolo}` | autenticado |
| GET | `/api/denuncias`, `/estatisticas` | COORDENADOR, ADMIN |
| POST | `/api/denuncias/{id}/andamentos` | COORDENADOR, ADMIN |
| GET | `/api/admin/logs-seguranca`, `/logs-auditoria` | ADMIN |
| POST | `/api/chatbot/perguntas` | todos |

Relatórios aceitam `?formato=csv` para download em CSV.

Erros seguem o formato:

```json
{
  "timestamp": "2026-11-07T22:00:00",
  "status": 400,
  "mensagem": "Dados invalidos",
  "campos": { "email": "deve ser um endereco de e-mail valido" }
}
```

Falhas de matrícula retornam HTTP 422 com `motivo`, `disciplinasPendentes` e `turmasAlternativas`.

---

## Regras de negócio implementadas

| Código | Implementação |
|---|---|
| RN-001 | Matrícula exige pré-requisitos com status `APROVADO` |
| RN-002 / RN-005 | Matrícula e cancelamento somente dentro do calendário acadêmico |
| RN-003 / RN-009 | Professor só acessa/altera turmas das quais é responsável |
| RN-004 | Autorização por perfil via `SecurityConfig` e `@PreAuthorize` |
| RN-006 | Nota entre 0 e 10; frequência entre 0% e 100% |
| RN-007 | Logs de auditoria e de segurança |
| RN-008 | Alteração de nota após encerramento exige COORDENADOR/SECRETARIA e gera auditoria |
| RN-010 | Matrícula bloqueada quando as vagas ativas se esgotam, com sugestão de turma alternativa |

Cálculos:

- média ponderada = `Σ(nota × peso) / Σ(pesos lançados)`;
- nota necessária para atingir a média alvo;
- frequência = `presenças / aulas registradas × 100`;
- alerta quando a frequência fica a menos de 5 pontos percentuais do mínimo de 75%.

---

## Testes

```bash
cd backend
./mvnw test
```

A suíte cobre as regras principais de matrícula, notas, frequência, chatbot e autenticação (23 testes). Os testes usam H2 em memória e não dependem do PostgreSQL.

Para gerar o JAR:

```bash
cd backend
./mvnw clean package
java -jar target/gestao-academica-0.0.1-SNAPSHOT.jar
```

---

## Configuração

Variáveis de ambiente suportadas:

| Variável | Padrão | Descrição |
|---|---|---|
| `DB_HOST` | `localhost` | Host do PostgreSQL |
| `DB_PORT` | `5432` | Porta do PostgreSQL |
| `DB_NAME` | `gestao` | Nome do banco |
| `DB_USER` | `postgres` | Usuário |
| `DB_PASSWORD` | `postgres` | Senha |
| `SERVER_PORT` | `8080` | Porta da API |
| `JWT_SECRET` | segredo de desenvolvimento | Chave HS256 (mínimo 32 caracteres em produção) |
| `GEMINI_API_KEY` | vazio | Chave da API do Gemini |
| `GEMINI_MODEL` | `gemini-2.5-flash` | Modelo utilizado |
| `GEMINI_FALLBACK` | `false` | Resposta padrão quando o Gemini falha/sem cota |
| `SEED_ENABLED` | `false` | Executa o seed quando o banco está vazio |
| `POSTGRES_PORT` | `5432` | Porta do banco no host (somente Docker Compose) |

Parâmetros acadêmicos também podem ser ajustados em `backend/src/main/resources/application.yml` (`academico.*`): média de aprovação, frequência mínima, margem de alerta, tentativas de login e tempo de bloqueio.

Há ainda o perfil `demo`, que ativa o fallback do chatbot:

```bash
SPRING_PROFILES_ACTIVE=demo ./mvnw spring-boot:run
```

---

## Banco de dados, seed e dump

- O schema e os dados de exemplo estão em `db/init/01-dump.sql`.
- O PostgreSQL executa esse arquivo **apenas quando o volume está vazio**. Para recarregar: `docker compose down -v`.
- A aplicação usa `spring.jpa.hibernate.ddl-auto=validate`; se o modelo JPA mudar, o dump precisa ser regenerado.
- O seed Java (`DemoDataSeeder`) só roda com `academico.seed-enabled=true` e banco sem usuários. O dump já contém os dados; o seed serve para recriar o cenário do zero.

### Regenerar o dump

1. Suba um PostgreSQL vazio (sem o volume de init):

```bash
docker run -d --name gestao-seed \
  -e POSTGRES_DB=gestao \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -p 5433:5432 postgres:16
```

2. Rode a aplicação apontando para ele, com schema `create` e seed habilitado:

```bash
cd backend
DB_PORT=5433 SEED_ENABLED=true ./mvnw spring-boot:run \
  -Dspring-boot.run.arguments="--spring.jpa.hibernate.ddl-auto=create"
```

3. Aguarde a mensagem `Started GestaoAcademicaApplication`, interrompa com `Ctrl+C` e gere o dump:

```bash
docker exec gestao-seed pg_dump -U postgres --no-owner gestao \
  > db/init/01-dump.sql
docker rm -f gestao-seed
```

4. Confira que a aplicação sobe com `ddl-auto=validate`:

```bash
docker compose down -v
docker compose up --build
```

### Dados do cenário

- 2 cursos, 8 disciplinas e 4 vínculos de pré-requisito (cadeias Algoritmos → Estrutura → Banco e Algoritmos → Engenharia → Qualidade);
- períodos `2025.2`, `2026.1` e `2026.2` (este com matrícula aberta);
- 6 turmas, incluindo a turma 3 lotada e a turma 4 como alternativa;
- 11 matrículas, notas, 9 aulas e registros de frequência;
- 5 avisos e 3 denúncias em etapas diferentes;
- aluna Ana com histórico aprovado e frequência de 77,8%.

---

## Observações e limitações

- O frontend Angular não faz parte desta entrega; a API já serve arquivos estáticos de `classpath:/static/` e redireciona rotas desconhecidas para `index.html`.
- Quando o frontend for adicionado, inclua um stage Node no `Dockerfile` e copie `frontend/dist/<app>/browser` para `backend/src/main/resources/static` antes do `mvn package`.
- Sem `GEMINI_API_KEY`, o chatbot responde 503; com `GEMINI_FALLBACK=true`, responde com uma mensagem orientando o usuário a procurar a Secretaria Acadêmica.
- O projeto não possui deploy em nuvem; o alvo é execução local para demonstração.
