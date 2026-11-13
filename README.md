# 🐾 SmartPet Web - Painel Administrativo & API REST

Sistema web e API REST do Pet Shop SmartPet, desenvolvido em **Java (Jakarta Servlets & JSP)** e integrado ao banco de dados **MySQL**.

---

## 🚀 Como Executar

### 1. Pré-requisitos
* **Java 17+** ou **JDK 25** instalado.
* **MySQL 8.0** ativo na porta `3306`.
* **Apache Tomcat 11** instalado (caminho padrão: `C:\src\apache-tomcat-11.0.27`).

### 2. Configuração do Banco de Dados
Execute o script SQL localizado em `database/schema.sql` no seu MySQL:
```sql
source database/schema.sql;
```

### 3. Iniciar a Aplicação (Windows)
Basta dar dois cliques no arquivo:
```cmd
Abrir-SmartPet.bat
```
Ou rodar via PowerShell:
```powershell
.\Iniciar-SmartPet.ps1
```
O painel abrirá automaticamente no navegador em: `http://localhost:8080/smartpet/`

* **Usuário Admin:** `adm`
* **Senha Admin:** `adm123`

---

## 📡 Endpoints da API para o Mobile
* `GET /api/servicos` - Listagem de serviços
* `GET /api/produtos` - Catálogo de produtos
* `GET /api/fila/cliente/{cpf}` - Fila de atendimento em tempo real
* `POST /api/fila/adicionar` - Agendamento de atendimento
* `POST /api/avaliacoes` - Registro de avaliação
* `POST /login/usuario` - Autenticação do tutor
