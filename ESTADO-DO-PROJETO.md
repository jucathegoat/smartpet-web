# SmartPet — Estado do Projeto

> Documento de continuidade. Leia isto antes de mexer em qualquer coisa.
> Última atualização: 15/11/2026

---

## 1. Onde está cada coisa

```
C:\Users\joaol\OneDrive\Desktop\
├── smartpet-web\                        ← backend Java
│   ├── backend-spring\                  ← Spring Boot (O QUE RODA)
│   │   ├── src\main\java\com\petshop\backend\
│   │   │   ├── controller\               ← rotas HTTP
│   │   │   ├── model\                    ← 14 entidades JPA
│   │   │   ├── repository\               ← 13 interfaces
│   │   │   └── service\                  ← regras de negócio
│   │   ├── src\main\resources\
│   │   │   ├── application.properties
│   │   │   └── META-INF\resources\WEB-INF\   ← JSPs (versão ativa)
│   │   ├── src\main\webapp\WEB-INF\          ← JSPs (cópia espelho)
│   │   └── pom.xml
│   ├── src\                             ← Servlet antigo (FALLBACK, intacto)
│   ├── .env                             ← credenciais Aiven (NÃO subir)
│   ├── Iniciar-Spring.ps1               ← script para rodar
│   └── README.md
│
├── smartpet-mobile\                     ← app Flutter
│   └── lib\
│
├── SmartPet - Justificativa da Arquitetura.pdf
└── smartpet-FINAL.md
```

---

## 2. Como rodar

```powershell
cd "C:\Users\joaol\OneDrive\Desktop\smartpet-web"
powershell -NoProfile -ExecutionPolicy Bypass -File ".\Iniciar-Spring.ps1"
```

Abre em: **http://localhost:8080/login**

| O quê | Valor |
|---|---|
| Login do painel | `adm` / `adm123` |
| API mobile | `http://localhost:8080/login/usuario` |
| Tela de caixa | `http://localhost:8080/caixa` |

### Se o jar estiver travado

```powershell
Get-Process java | Stop-Process -Force
```

### Para recompilar após mudar código

```powershell
Get-Process java | Stop-Process -Force
cd "C:\Users\joaol\OneDrive\Desktop\smartpet-web\backend-spring"
.\mvnw.cmd clean package -DskipTests
```

---

## 3. Banco de dados

**Aiven MySQL 8.4.11** — schema `defaultdb`

```
host: petshop-petshop-121.c.aivencloud.com
porta: 23324
user: avnadmin
ssl:  REQUIRED
```

As credenciais ficam no `.env` (não commitar).

### Tabelas reais (16)

`agenda`, `avaliacao`, `caixa`, `despesa`, `fila_atendimento`, `funcionario`,
`item_venda`, `medicamento`, `pet`, `produto`, `receita`, `servico`,
`tutela`, `tutor`, `usuario_func`, `venda`

### Colunas que causam confusão

| Entidade | Tabela real | Pegadinha |
|---|---|---|
| `Cliente` | **`tutor`** | tabela não se chama `cliente` |
| `Pet` | `pet` | PK é `codigo_pet`, FK de tutor é `n_documento` |
| `Produto` | `produto` | PK é `id_produto`; coluna de tipo é `tipo`, não `categoria` |
| `Servico` | `servico` | PK é `codigo_servico` |
| `Avaliacao` | `avaliacao` | PK é `id_avaliacao` |
| `Notificacao` | `smartpet_notificacao` | tabela com prefixo |

---

## 4. O que FUNCIONA (testado de verdade)

| Item | Como foi verificado |
|---|---|
| Spring Boot → banco Aiven | login real, dados reais nas telas |
| `/clientes` | 4 tutores: Maria Jane, Marcelo, tutorTeste, Joao |
| `/pets` | 4 pets: Garfield, Luke, Rex, Tornado |
| `/loja` | 7 produtos com preço, estoque **e foto por link** |
| `/vendas` (RF06 loja) | venda de 2× ração, estoque 20→18, receita no caixa |
| `/avaliacoes` | 2 avaliações |
| `/notificacoes` | lê do banco |
| `/caixa` (RF07) | abertura, entrada, saída, fechamento, saldo |
| **`/balconista`** | registrar chegada (selects do banco), fila, modal de Ações |
| **`/veterinario`** | em atendimento, aguardando, histórico |
| **`/admin/clientes/editar`** | dados, pets (com cor), parentes |
| `/api/servicos` | 5 serviços em JSON |
| `/api/produtos` | 7 produtos com `url` |
| `/api/fila` | fila real do banco |
| `/api/fila/cliente/{cpf}` | atendimentos do cliente com posição e espera |
| `/api/pets/{id}` | GET / PUT / DELETE |
| `/api/clientes/validar/{cpf}` | liberado / existe / bloqueado / invalido |
| `/caixa-api/status` | saldo em JSON |
| `/login/usuario` | `MJ`/`12345` → JSON com sucesso |
| Servlet antigo | intacto em `src/`, não foi tocado |

### Testes de fluxo feitos hoje

```
Balconista registra Garfield + Banho  → fila: "4 Garfield Em espera"
Balconista chama próximo              → fila: "3 Rex Em atendimento"
Veterinário conclui o Rex             → fila: "3 Rex Concluído"

Bloquear Joao (motivo)                → login Joao/ocara = HTTP 403
Desbloquear Joao                      → login Joao/ocara = HTTP 200

Editar Maria Jane (telefone)          → banco: (11) 98888-7777
Adicionar pet Mel com cor             → banco: (16,'Mel','Cão','Poodle / Branco',4,6.50)
```

### Teste do caixa (gravado no Aiven)

```
Abertura ........ R$ 100,00
Receita Banho ... + R$ 80,00
Receita Consulta + R$ 120,00
Despesa Ração ... - R$ 45,00
──────────────────────────
Saldo ........... R$ 255,00
```

---

## 5. O que FALTA (atualizado em 16/11/2026)

**Nenhum requisito do documento está pendente.** Os 7 RFs e os 4 RNFs foram implementados e
verificados contra o banco real. O que resta são limitações impostas pelo schema (que não pode
ser alterado) e defeitos de dados.

### Limitações declaradas (não são bugs de código)

| # | Limitação | Motivo |
|---|---|---|
| 1 | Vínculos de parentesco (RF02) ficam em memória | `tutela.cpf` é `varchar(12)`; CPF formatado tem 14 caracteres |
| 2 | Cor do pet (RF02) é gravada dentro de `pet.raca` | Não existe coluna `cor`; formato `"Poodle / Branco"` |
| 3 | Motivo do bloqueio (RF03) fica em memória | `status_conta` é `varchar(20)`; a coluna guarda só `Bloqueado` |

### Defeitos conhecidos (não afetam funcionalidade)

| # | Defeito | Situação |
|---|---|---|
| 4 | `ddl-auto=update` gera aviso `there can be only one auto column` em `UsuarioFunc` | Inofensivo; a tabela existe e funciona |
| 5 | `pet.n_documento` com valores `XV5123`, `6343GER`, `GBV234` (não são CPF) | Dado do banco; a consulta busca por CPF **e** por esse campo |

### 🟡 JSPs duplicados (dívida técnica)

Os 13 JSPs existem em **dois lugares** e são sincronizados à mão:

```
src/main/webapp/WEB-INF/                        ← fonte
src/main/resources/META-INF/resources/WEB-INF/  ← o que o Tomcat embutido serve
```

**Sempre que editar um JSP, copie para o outro lado:**

```powershell
cd backend-spring
Get-ChildItem src/main/resources/META-INF/resources/WEB-INF/*.jsp |
  ForEach-Object { Copy-Item $_.FullName "src/main/webapp/WEB-INF/$($_.Name)" -Force }
```

## 6. Armadilhas de build (já resolvidas, mas cuidado ao mexer)

### Bug do Jasper (JSP velho em cache)

**Sintoma:** corrige o JSP, recompila, e a tela continua vazia.

**Causa:** o Maven não copiava os JSPs para `META-INF/resources/`, então o
Jasper compilava uma versão antiga.

**Solução (já no `pom.xml`):**

```xml
<resources>
    <resource>
        <directory>src/main/resources</directory>
        <filtering>false</filtering>
    </resource>
    <resource>
        <directory>src/main/webapp</directory>
        <targetPath>META-INF/resources</targetPath>
        <filtering>false</filtering>
    </resource>
</resources>
```

Se o problema voltar, apague `backend-spring/tomcat-work/` e recompile.

### Outras

| Problema | Solução |
|---|---|
| `Unable to rename ...jar` | matar java antes de compilar |
| OneDrive trava `build/` | `mvnw clean` |
| `flutter.bat` acha `pwsh` shim quebrado | filtrar `*trash-shim*` do PATH |
| `ddl-auto` tenta alterar `usuario_func` | warning inofensivo no log |

---

## 7. Requisitos do documento — situação

| Requisito | Estado | Onde |
|---|---|---|
| **RF01** Consultar cliente por CPF | ✅ | `/clientes?busca=` e `GET /api/clientes/{cpf}` |
| **RF02** Editar cliente (telefone, pet, raça, cor, parentes, novo pet) | ✅ | `/admin/clientes/editar` |
| **RF03** Bloquear cliente com motivo | ✅ | `POST /admin/clientes/bloquear`, 403 no login e no cadastro |
| **RF04** Cadastro manual com validação prévia | ✅ | `GET /api/clientes/validar/{cpf}` → liberado / existe / bloqueado / invalido |
| **RF05** Visualizar e gerir fila (balconista ↔ veterinário) | ✅ | `/fila`, `/balconista`, `/veterinario`, modal de Ações |
| **RF06** Avaliação rápida (Ruim/Bom/Excelente + nova tentativa) | ✅ | `lib/servicos/avaliacao_helper.dart` |
| **RF07** Controle financeiro (receitas, despesas, saldo) | ✅ | `/caixa`, `/caixa-api/*`, `/vendas` |
| **RNF01** HTML, CSS, JSP e Servlets | ✅ | 13 JSPs + `HttpSession` + Tomcat embutido |
| **RNF02** Flutter com interface simples | ✅ | `smartpet-mobile` |
| **RNF03** SQL + nota só 1, 2 ou 3 | ✅ | `AvaliacaoController.salvarAvaliacaoApi` → HTTP 400 fora de 1–3 |
| **RNF04** OO, `ArrayList`, falhas sem fechar | ✅ | 14 entidades, `ArrayList` em `FilaService`/`ClienteAdminController`, try/catch no app |

### Extra (fora do documento, pedido pelo João Victor no WhatsApp)

| Item | Estado |
|---|---|
| Modal "Ações" na fila (cancelar/concluir, sem apagar) | ✅ |
| Serviços e pets vêm do banco (não fixos no código) | ✅ |
| Fotos dos produtos por link (`produto.url`) | ✅ |
| Endpoints do app: `/api/pets/{id}`, `/api/fila/cliente/{cpf}`, `/api/fila/{id}/cancelar` | ✅ |

## 8. Ordem de trabalho

**Concluído.** Todos os requisitos do documento estão atendidos.

Se for continuar, a ordem que faz sentido é:

1. **Autorização para alterar o schema** — resolveria as 3 limitações da seção 5 de uma vez:
   - `ALTER TABLE tutela MODIFY cpf VARCHAR(14)` → parentes passam a persistir
   - `ALTER TABLE pet ADD COLUMN cor VARCHAR(30)` → cor em coluna própria
   - `ALTER TABLE tutor MODIFY status_conta VARCHAR(255)` → motivo do bloqueio persistido
2. **Corrigir os dados de `pet.n_documento`** — os 3 valores que não são CPF
3. **Juntar os JSPs duplicados** — mudança estrutural, avisar antes de mexer

## 9. Justificativa Spring vs Servlet (para o professor)

O documento pede Servlet no RNF01 e Spring Boot nas seções 3.1 e 3.3.
**Não é contradição: Spring Boot contém Servlet.**

- Roda sobre Tomcat embutido (servlet container)
- Usa `jakarta.servlet.http.HttpSession`
- Renderiza JSPs normalmente
- E ainda oferece `@RestController` para a API REST

Documento completo: `SmartPet - Justificativa da Arquitetura.pdf`

---

## 10. Repositórios

| Projeto | Repositório | Branch |
|---|---|---|
| smartpet-web | github.com/jucathegoat/smartpet-web | `main` |
| smartpet-mobile | github.com/jucathegoat/smart-pet-projeto | `versao-nova` |

**Nunca usar `git push --force`.**
