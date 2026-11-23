# SmartPet — Backend e painel administrativo

Backend Java que fornece a API usada pelo aplicativo Flutter e o painel web do SmartPet. O Render usa o projeto Spring Boot em `backend-spring/`.

## Estrutura

```text
smartpet-web/
├── backend-spring/       # backend ativo: API, painel JSP e integração MySQL
│   ├── src/main/java/    # controllers, models, repositories e services
│   ├── src/main/resources/
│   ├── src/main/webapp/  # páginas JSP
│   ├── Dockerfile        # imagem usada pelo Render
│   └── pom.xml
├── database/schema.sql  # schema SQL do projeto
├── render.yaml          # configuração do serviço no Render
└── src/                 # implementação Servlet legada
```

`backend-spring/` é o backend usado no deploy definido em `render.yaml`. A pasta `src/` contém uma implementação anterior em Servlets; não é o alvo do deploy atual.

## Requisitos

- JDK 21 ou superior
- MySQL configurado para o projeto
- Flutter instalado para executar o aplicativo mobile

## Executar o backend no Windows

Abra o PowerShell nesta pasta e execute:

```powershell
cd .\backend-spring
.\mvnw.cmd spring-boot:run
```

Execute o comando dentro de `backend-spring/`, onde está o `pom.xml` do Spring Boot. O painel fica em [http://localhost:8080/login](http://localhost:8080/login); a API usa a mesma base `http://localhost:8080`.

Configure a conexão MySQL em `backend-spring/src/main/resources/application.properties` ou nas variáveis de ambiente definidas para o ambiente de execução. Não publique credenciais no Git.

## Executar o aplicativo Flutter

Consulte o README do repositório mobile. Com o backend ativo, a configuração local da API fica em `mobile/smart-pet-projeto/lib/config/app_config.dart`.

## Deploy

O serviço Render está descrito em `render.yaml` e usa `backend-spring/Dockerfile`. As credenciais do banco devem ser configuradas como variáveis de ambiente no Render.
