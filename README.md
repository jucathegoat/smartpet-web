# 🐾 SmartPet Web — Painel Administrativo & API REST

Sistema web e API REST do Pet Shop SmartPet, desenvolvido em **Java (Jakarta Servlets & JSP)** e integrado ao banco de dados **MySQL**.

Este repositório é o **backend**. Ele atende duas coisas ao mesmo tempo:

| Cliente | O que consome | Formato |
|---|---|---|
| **Painel web** (navegador) | páginas JSP | HTML |
| **App mobile** (Flutter) | rotas `/api/*` e `/login/usuario` | JSON |

Os dois rodam no **mesmo Tomcat, na mesma porta**. Não são duas aplicações — é uma só, com rotas diferentes.

---

## 📋 Índice

1. [Arquitetura](#-arquitetura)
2. [Pré-requisitos](#-pré-requisitos)
3. [Configuração do banco](#-configuração-do-banco)
4. [Variáveis de ambiente](#-variáveis-de-ambiente)
5. [Como executar (Windows)](#-como-executar-windows)
6. [Como executar (Linux/macOS)](#-como-executar-linuxmacos)
7. [Credenciais de acesso](#-credenciais-de-acesso)
8. [Endpoints da API](#-endpoints-da-api-para-o-mobile)
9. [Deploy gratuito no Render](#-deploy-gratuito-no-render)
10. [Como atualizar o código em produção](#-como-atualizar-o-código-em-produção)
11. [Solução de problemas](#-solução-de-problemas)
12. [Estrutura do projeto](#-estrutura-do-projeto)
13. [Segurança — leia antes de publicar](#-segurança--leia-antes-de-publicar)

---

## 🏗 Arquitetura

```
┌─────────────────┐         ┌──────────────────┐
│  App Flutter    │         │  Navegador       │
│  (celular)      │         │  (painel web)    │
└────────┬────────┘         └────────┬─────────┘
         │ JSON                      │ HTML
         └───────────┬───────────────┘
                     ▼
         ┌───────────────────────┐
         │  Tomcat 11  :8080     │
         │  /smartpet/           │
         │  └─ SmartPetServlet   │
         │     ├─ CorsFilter     │
         │     ├─ Validacao      │
         │     └─ MySql (JDBC)   │
         └───────────┬───────────┘
                     ▼
         ┌───────────────────────┐
         │  MySQL 8 (Aiven)      │
         │  schema: defaultdb    │
         └───────────────────────┘
```

**Componentes principais:**

| Arquivo | Função |
|---|---|
| `SmartPetServlet.java` | Roteador único — atende painel e API |
| `CorsFilter.java` | Libera CORS e trata `OPTIONS` (preflight do app) |
| `Validacao.java` | Valida CPF, usuário, senha, e-mail, decimais |
| `MySql.java` | Abre conexão JDBC usando variáveis de ambiente |

---

## ✅ Pré-requisitos

| Item | Versão | Obrigatório? |
|---|---|---|
| **JDK** | 21 ou superior | sim |
| **Apache Tomcat** | 11 | sim |
| **MySQL** | 8.0+ | sim (pode ser remoto/Aiven) |
| **Maven** | vem pelo `mvnw` | não (wrapper incluso) |

> O projeto **não usa Spring Boot**. É Servlet puro. O arquivo
> `src/main/resources/application.properties` é resquício de uma versão anterior
> e **não é lido** por nada — ignore o `server.port=8086` que está lá.
> A porta real vem do `.env`.

---

## 🗄 Configuração do banco

### Opção A — MySQL local

```sql
source database/schema.sql;
```

O script cria as 16 tabelas. Ele **não** insere dados — para ter um tutor de teste,
cadastre pelo app (tela "Cadastre-se") ou pelo painel (`/clientes`).

### Opção B — MySQL no Aiven (nuvem)

1. Crie o serviço em https://console.aiven.io
2. Importe o schema:
   ```powershell
   mysql -h SEU_HOST.aivencloud.com -P PORTA -u avnadmin -p defaultdb < database/schema.sql
   ```
3. Em **Allowed IP Addresses**, libere o IP de onde o backend vai rodar
   (`0.0.0.0/0` se for o Render)

---

## ⚙️ Variáveis de ambiente

O sistema **não guarda credenciais no código**. Tudo vem de variáveis de ambiente.

### Criando o `.env`

```powershell
Copy-Item .env.example .env
```

Depois edite o `.env`:

```ini
server_address=localhost
server_port=8080
jwt_key=troque-esta-chave

db_host=SEU_HOST_AIVEN
db_port=23324
db_user=SEU_USUARIO
db_pass=SUA_SENHA
db_schema=defaultdb
db_ssl=REQUIRED

admin_user=adm
admin_password=TROQUE_ESTA_SENHA
```

> ⚠️ **O `.env` está no `.gitignore`.** Ele nunca vai para o Git.
> Use o `.env.example` como modelo e **nunca** commite senhas reais.

### O que cada variável faz

| Variável | Descrição |
|---|---|
| `server_port` | porta em que o Tomcat escuta (padrão 8080) |
| `db_host` / `db_port` | endereço do MySQL |
| `db_schema` | nome do banco |
| `db_ssl` | `REQUIRED` para Aiven, `useSSL` para local |
| `admin_user` / `admin_password` | login do painel administrativo |

### Como o script monta a URL do JDBC

```
jdbc:mysql://<db_host>:<db_port>/<db_schema>?sslMode=<db_ssl>&serverTimezone=UTC
  &allowPublicKeyRetrieval=true&connectTimeout=10000&socketTimeout=30000
```

---

## 🚀 Como executar (Windows)

### 1. Ajuste os caminhos do Tomcat e do JDK

Abra `Iniciar-SmartPet.ps1` e confira as duas primeiras variáveis:

```powershell
$tomcat   = 'C:\src\apache-tomcat-11.0.27'
$javaHome = 'C:\Users\SEU_USUARIO\AppData\Local\Programs\Eclipse Adoptium\jdk-21'
```

Troque pelos caminhos da **sua** máquina.

### 2. Rode o script

```powershell
.\Iniciar-SmartPet.ps1
```

Ou dê dois cliques em `Abrir-SmartPet.bat`.

**O que o script faz:**

1. Lê o `.env`
2. Gera `setenv.bat` no Tomcat com as variáveis (o `Start-Process` não herda `$env:`)
3. Libera a porta 8080 (mata o processo que estiver nela, se o `shutdown.bat` falhar)
4. Compila o WAR com `mvnw package`
5. Sobe o Tomcat
6. Valida `GET /health/db` antes de dizer que subiu

Saída esperada:

```
SmartPet iniciou e conectou ao MySQL.
Login: <o admin_user do seu .env>
Senha: <o admin_password do seu .env>
```

Painel: **http://localhost:8080/smartpet/**

---

## 🐧 Como executar (Linux/macOS)

```bash
# 1. Configurar
cp .env.example .env
nano .env

# 2. Compilar
./mvnw package

# 3. Exportar as variáveis e subir
export SMARTPET_DB_URL="jdbc:mysql://HOST:PORTA/defaultdb?sslMode=REQUIRED&serverTimezone=UTC"
export SMARTPET_DB_USER="SEU_USUARIO"
export SMARTPET_DB_PASSWORD="SUA_SENHA"
export ADMIN_USER="adm"
export ADMIN_PASSWORD="SUA_SENHA_ADMIN"

cp target/smartpet.war $CATALINA_HOME/webapps/
$CATALINA_HOME/bin/catalina.sh run
```

---

## 🔑 Credenciais de acesso

### Painel web

| Campo | Onde definir |
|---|---|
| usuário | `admin_user` no `.env` (padrão `adm`) |
| senha | `admin_password` no `.env` |

> O admin **não** fica na tabela `tutor`. Ele vem do ambiente.

### App mobile

O app autentica contra a tabela `tutor` do banco. Para testar, cadastre um tutor
pelo app ou pelo painel (`/clientes`), depois use:

```
POST /smartpet/login/usuario
{ "usuario": "SEU_USUARIO", "senha": "SUA_SENHA" }
```

> O login do painel (`adm`) **não funciona** no app — são autenticações diferentes.

---

## 📡 Endpoints da API para o Mobile

### Autenticação

| Método | Rota | Corpo |
|---|---|---|
| `POST` | `/smartpet/login/usuario` | `{"usuario":"...","senha":"..."}` |

Resposta `200`:
```json
{ "sucesso": true, "cpf": "625464946-64", "nome": "Maria Jane", "usuario": "MJ" }
```

Erros: `401` senha incorreta · `403` conta inativa · `400` campos vazios

### Consultas

| Método | Rota | Descrição |
|---|---|---|
| `GET` | `/smartpet/api/servicos` | catálogo de serviços |
| `GET` | `/smartpet/api/produtos` | catálogo de produtos |
| `GET` | `/smartpet/tutor/{cpf}/pets` | pets do tutor |
| `GET` | `/smartpet/api/fila/cliente/{cpf}` | fila de atendimento |
| `GET` | `/smartpet/api/avaliacoes` | avaliações |
| `GET` | `/smartpet/health/db` | saúde do banco (`{"ok":true}`) |

### Escritas

| Método | Rota | Descrição |
|---|---|---|
| `POST` | `/smartpet/usuario` | cadastro de tutor |
| `POST` | `/smartpet/cadastrar/pet` | cadastro de pet |
| `POST` | `/smartpet/api/fila/adicionar` | agendamento (JSON) |
| `POST` | `/smartpet/api/avaliacoes` | registrar avaliação |
| `PUT` | `/smartpet/api/pets/{id}` | atualizar pet |

### CORS

O `CorsFilter` libera `Access-Control-Allow-Origin: *` e responde `OPTIONS` com `204`.
Isso permite que o app Flutter (web ou mobile) chame a API de outra origem.

---

## ☁️ Deploy gratuito no Render

Custa **R$ 0**, sem cartão. Passo a passo completo.

### 1. Liberar o IP no Aiven

Console Aiven → seu serviço → **Allowed IP Addresses** → adicione `0.0.0.0/0`.

> Sem isso o deploy sobe mas o login falha com **503**.

### 2. Subir o código no GitHub

```powershell
git add -A
git commit -m "Prepara deploy"
git push
```

### 3. Criar o serviço no Render

1. https://render.com → login com GitHub
2. **New +** → **Web Service**
3. Conecte este repositório
4. Preencha:

| Campo | Valor |
|---|---|
| Name | `smartpet` |
| Language | **Docker** |
| Branch | `main` |
| Instance Type | **Free** |
| Health Check Path | `/smartpet/health/db` |

### 4. Variáveis de ambiente no Render

| Chave | Valor |
|---|---|
| `SMARTPET_DB_URL` | `jdbc:mysql://HOST:PORTA/defaultdb?sslMode=REQUIRED&serverTimezone=UTC&allowPublicKeyRetrieval=true` |
| `SMARTPET_DB_USER` | seu usuário |
| `SMARTPET_DB_PASSWORD` | sua senha |
| `ADMIN_USER` | `adm` |
| `ADMIN_PASSWORD` | sua senha do painel |
| `JWT_KEY` | uma chave qualquer |

> O Render define `$PORT` sozinho. O `docker-entrypoint-port.sh` reescreve o
> `server.xml` do Tomcat para escutar nela.

### 5. Testar

```powershell
curl https://smartpet-xxxx.onrender.com/smartpet/health/db
# {"ok":true}
```

### Limites do plano gratuito

| Item | Comportamento |
|---|---|
| Custo | R$ 0 |
| Cartão | não pede |
| Dorme | após 15 min sem acesso |
| Acorda | ~1 minuto |
| RAM | 512 MB (JVM limitada a 320 MB) |
| Horas | 750/mês |

> **Para apresentações:** abra o app 2 minutos antes. Isso acorda o servidor.

---

## 🔄 Como atualizar o código em produção

Depois do primeiro deploy, **não existe upload de arquivo**. O Render lê sempre do GitHub.

```powershell
# 1. altere o código
# 2. commite e envie
git add -A
git commit -m "ajusta validação de CPF"
git push
```

O Render detecta o push e republica sozinho em ~5–8 minutos.

| Item | Comportamento |
|---|---|
| Gatilho | `git push` na branch `main` |
| URL | **não muda** |
| APK | **não precisa recompilar** |
| Rollback | disponível no painel do Render |

> O container é **imutável**: não dá para editar arquivos direto no servidor.
> Isso garante que o que está no ar é exatamente o que está no repositório.

---

## 🔧 Solução de problemas

### `O computador remoto recusou a conexão`

Nada está escutando na porta. Causas:

| Sintoma | Quem respondeu |
|---|---|
| `{"ok":true}` | Tomcat (Java) ✓ |
| `404 Route not found` | `backend.dart` (Dart) — **conflito** |
| `{"sucesso":false,"mensagem":"Falha ao consultar o MySQL."}` | Tomcat no ar, banco errado |

**Se `backend.dart` estiver rodando, pare ele.** Os dois disputam a 8080.
O `bin/backend.dart` é código legado do app Flutter e **não é mais usado**.

### `Access denied for user`

Usuário ou senha do banco errados no `.env`. Confira `db_user` / `db_pass`.

### `health/db` devolve 503

Tomcat subiu, banco recusou. Normalmente é o **allowlist do Aiven** fechado.

### `Port 8080 was already in use`

```powershell
# descubra quem está na porta
netstat -ano | findstr :8080
# mate o processo
taskkill /PID <PID> /F /T
```

### App não conecta, mas o painel abre

O app precisa do **IP do computador que roda o servidor**, não `localhost`.
`localhost` no celular é o próprio celular.

```powershell
ipconfig | Select-String "IPv4"   # ex: 192.168.1.102
flutter build apk --release --dart-define=SMARTPET_API_URL=http://192.168.1.102:8080/smartpet
```

O celular precisa estar na **mesma rede** do computador.

---

## 📁 Estrutura do projeto

```
smartpet-web/
├── Dockerfile                    # imagem para deploy (Tomcat 11 + JDK 21)
├── docker-entrypoint-port.sh     # ajusta a porta do Tomcat para $PORT
├── .dockerignore
├── .env.example                  # modelo de configuração (sem senhas)
├── .env                          # SUAS credenciais — não vai para o Git
├── pom.xml                       # Maven, packaging war, finalName smartpet
├── mvnw / mvnw.cmd               # Maven wrapper
├── Iniciar-SmartPet.ps1          # script de inicialização (Windows)
├── Abrir-SmartPet.bat
├── database/
│   └── schema.sql                # 16 tabelas
└── src/main/
    ├── java/com/petshop/backend/servlet/
    │   ├── SmartPetServlet.java  # roteador
    │   ├── CorsFilter.java       # CORS + OPTIONS
    │   ├── Validacao.java        # validações
    │   └── MySql.java            # conexão JDBC
    ├── resources/
    │   └── application.properties  # legado do Spring Boot — ignorado
    └── webapp/
        ├── css/style.css
        └── WEB-INF/
            ├── login.jsp
            ├── fila.jsp
            ├── clientes.jsp
            ├── pets.jsp
            ├── loja.jsp
            ├── avaliacoes.jsp
            ├── notificacoes.jsp
            ├── cadastro_usuario.jsp
            └── cadastro_agendamento.jsp
```

### Banco de dados — 16 tabelas

`tutor` · `pet` · `funcionario` · `usuario_func` · `servico` · `produto`
`venda` · `item_venda` · `caixa` · `despesa` · `receita` · `agenda`
`fila_atendimento` · `avaliacao` · `medicamento` · `tutela`

---

## ⚠️ Segurança — leia antes de publicar

Este projeto é acadêmico. Antes de expor na internet, saiba o que está aberto:

### 🔴 Crítico

| Problema | Impacto | Correção |
|---|---|---|
| **Senhas em texto puro** na tabela `tutor` | quem ler o banco vê todas as senhas | hash (BCrypt/Argon2) — exige `varchar(50)` → `varchar(100)` |
| **CPF como única credencial** em `/api/fila/cliente/{cpf}`, `PUT /api/pets/{id}` | qualquer um consulta ou cancela pelo CPF de outra pessoa | token de sessão (JWT) |
| **`Access-Control-Allow-Origin: *`** | qualquer site pode chamar a API | restringir à origem do app |

### 🟡 Atenção

| Problema | Correção |
|---|---|
| `.env` com senha do Aiven | já está no `.gitignore` — **nunca** commite |
| Senha do admin em texto no README | trocar por instrução "defina no `.env`" |
| `jwt_key=SecretyKey` no `.env.example` | gerar chave real no deploy |
| Uma conexão JDBC por query | usar pool (HikariCP) para produção |

### ✅ O que já está protegido

- Nenhuma credencial no código-fonte
- `.env` fora do controle de versão
- Validação de CPF, usuário, senha e e-mail no servidor
- Mensagem genérica no login (não revela se o usuário existe)
- `status_conta` verificada — conta inativa recebe `403`
- Erro 1062 do MySQL tratado (`Já existe cadastro com este CPF`)

---

## 📄 Licença

Projeto acadêmico — SmartPet Pet Shop.
