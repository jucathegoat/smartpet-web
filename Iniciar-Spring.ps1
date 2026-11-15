# ============================================================
#  SmartPet - Spring Boot
#  Sobe a API + painel web em http://localhost:8080
# ============================================================
$ErrorActionPreference = 'Stop'

$base = Split-Path -Parent $MyInvocation.MyCommand.Path
$spring = Join-Path $base 'backend-spring'

# --- Carrega as credenciais do .env (mesmo arquivo usado pelo projeto Servlet) ---
$envFile = Join-Path $base '.env'
if (Test-Path $envFile) {
    Get-Content $envFile | ForEach-Object {
        $linha = $_.Trim()
        if ($linha -and -not $linha.StartsWith('#') -and $linha.Contains('=')) {
            $i = $linha.IndexOf('=')
            $k = $linha.Substring(0, $i).Trim()
            $v = $linha.Substring($i + 1).Trim()
            Set-Item -Path "env:$k" -Value $v
        }
    }
}

if (-not $env:SMARTPET_DB_URL) {
    $host_ = if ($env:db_host) { $env:db_host } else { 'localhost' }
    $port  = if ($env:db_port) { $env:db_port } else { '3306' }
    $schema = if ($env:db_schema) { $env:db_schema } else { 'smartpet' }
    $ssl   = if ($env:db_ssl) { '&sslMode=' + $env:db_ssl } else { '' }
    $env:SMARTPET_DB_URL = "jdbc:mysql://${host_}:${port}/${schema}?serverTimezone=UTC&allowPublicKeyRetrieval=true$ssl"
    $env:SMARTPET_DB_USER = $env:db_user
    $env:SMARTPET_DB_PASSWORD = $env:db_pass
}

# Remove shims quebrados do PATH
$env:Path = ($env:Path -split ';' | Where-Object { $_ -notlike '*trash-shim*' }) -join ';'

$javaHome = if ($env:JAVA_HOME) { $env:JAVA_HOME } else { 'C:\Users\joaol\AppData\Local\Programs\Eclipse Adoptium\jdk-25.0.5.8-hotspot' }
$env:JAVA_HOME = $javaHome
$java = Join-Path $javaHome 'bin\java.exe'

Set-Location $spring

if (-not (Test-Path 'target\backend-0.0.1-SNAPSHOT.jar')) {
    Write-Host 'Compilando o projeto Spring Boot (primeira vez, pode levar 1-2 min)...' -ForegroundColor Yellow
    & .\mvnw.cmd -q -B package -DskipTests
    if ($LASTEXITCODE -ne 0) { throw 'Falha ao compilar o backend Spring Boot.' }
}

Write-Host ''
Write-Host '  SmartPet - Spring Boot' -ForegroundColor Cyan
Write-Host '  Painel web : http://localhost:8080/login' -ForegroundColor Green
Write-Host '  Login      : adm / adm123' -ForegroundColor Green
Write-Host '  API mobile : http://localhost:8080/login/usuario' -ForegroundColor Green
Write-Host ''

& $java -jar 'target\backend-0.0.1-SNAPSHOT.jar'
