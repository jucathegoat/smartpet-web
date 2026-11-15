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
| `/loja` | 7 produtos com preço e estoque |
| `/avaliacoes` | 2 avaliações |
| `/notificacoes` | lê do banco |
| `/caixa` (RF07) | abertura, entrada, saída, fechamento, saldo |
| `/api/servicos` | 5 serviços em JSON |
| `/caixa-api/status` | saldo em JSON |
| `/login/usuario` | `MJ`/`12345` → JSON com sucesso |
| Servlet antigo | intacto em `src/`, não foi tocado |

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

## 5. O que FALTA

### 🔴 CRÍTICO — fila com dados falsos

`FilaService.java` tem dados inventados no construtor:

```java
public FilaService() {
    adicionar("Carlos Silva", "Thor", "Banho e Tosa");
    adicionar("Ana Souza", "Mel", "Consulta Veterinária");
    adicionar("Marcos Lima", "Bob", "Vacinação");
}
```

**Carlos Silva, Ana Souza e Marcos Lima não existem no banco.**

E a fila é `ArrayList` em memória:

```java
private final List<ItemFilaDTO> fila = Collections.synchronizedList(new ArrayList<>());
```

Consequências:
- Reiniciar o servidor → fila volta aos 3 nomes falsos
- Cancelamento **nunca chega ao banco**
- A tabela `fila_atendimento` existe no Aiven e está **vazia**

**Correção:** `FilaService` deve usar `FilaAtendimentoRepository` (já existe).

### 🔴 RF06 — Registrar venda: NÃO EXISTE

```
GET /vendas  → 404
GET /pedidos → 404
```

`Venda.java` e `VendaRepository.java` existem, mas **nenhum controller**.
A loja mostra produtos e não faz nada ao clicar.

### 🟠 RF03 — Bloquear cliente: NÃO EXISTE

A coluna `tutor.status_conta` existe no banco. Ninguém lê nem escreve.

### 🟠 RF01 — Consultar cliente por CPF: NÃO EXISTE

Só lista todos. Sem campo de busca, sem mensagem "CPF não encontrado".

### 🟡 RNF03 — Validar nota 1 a 3: NÃO EXISTE

`AvaliacaoController.java` aceita qualquer inteiro:

```java
Integer nota = Integer.valueOf(body.get("nota").toString());
```

Aceita `nota: 999`.

### 🟡 Imagens de produto: NÃO EXISTE

- A coluna `produto.url` existe no banco
- A entidade `Produto.java` **não tem** o campo
- `loja.jsp` não tem `<img>`

**Como deve funcionar:** campo de texto no formulário onde se cola a URL
da imagem; na listagem aparece a foto; se vazio, aparece placeholder 🐾.

### 🟡 JSPs duplicados (dívida técnica)

Os 9 JSPs existem em **dois** lugares:

```
backend-spring/src/main/webapp/WEB-INF/                    ← cópia espelho
backend-spring/src/main/resources/META-INF/resources/WEB-INF/  ← ativa
```

Editar um **não** atualiza o outro. Sincronizar à mão por enquanto.

---

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

| Requisito | Descrição | Estado |
|---|---|---|
| RNF01 | JSP + Servlets | ✅ atendido |
| RNF04 | OO + ArrayList | ✅ atendido |
| Seção 3.1/3.3 | Spring Boot + API REST | ✅ atendido |
| RF07 | Controle de caixa | ✅ **completo e testado** |
| RF05 | Gerir fila | ⚠️ existe mas em memória, com dados falsos |
| RNF03 | Nota 1 a 3 | ⚠️ existe, falta validação |
| RF06 | Registrar venda | ❌ não existe |
| RF03 | Bloquear cliente | ❌ não existe |
| RF01 | Consultar por CPF | ❌ não existe |

---

## 8. Ordem de trabalho recomendada

| # | Tarefa | Tempo |
|---|---|---|
| 1 | Fila no banco (remover dados falsos) | 1h |
| 2 | RF06 — registrar venda | 1h30 |
| 3 | Imagens por URL | 40min |
| 4 | RF03 — bloquear cliente | 45min |
| 5 | RF01 — buscar por CPF | 30min |
| 6 | RNF03 — validar nota 1–3 | 10min |
| 7 | JSPs duplicados | 20min |

---

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
