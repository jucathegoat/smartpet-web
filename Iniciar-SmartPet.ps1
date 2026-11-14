$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$settings = @{}
Get-Content (Join-Path $projectRoot '.env') | ForEach-Object {
    if ($_ -match '^\s*([^#=\s]+)\s*=(.*)$') { $settings[$matches[1].Trim()] = $matches[2].Trim() }
}

$tomcat = 'C:\src\apache-tomcat-10.1.61'
$javaHome = 'C:\Program Files\Eclipse Adoptium\jdk-25.0.5.8-hotspot'
$baseUrl = 'http://localhost:8080/smartpet/'
foreach ($key in @('db_host', 'db_port', 'db_user', 'db_pass', 'db_schema', 'admin_user', 'admin_password')) {
    if (!$settings.ContainsKey($key) -or !$settings[$key]) { throw "Falta configurar '$key' no arquivo .env." }
}
if (!(Test-Path (Join-Path $tomcat 'bin\startup.bat'))) { throw "Tomcat n�o encontrado em $tomcat." }
$war = Join-Path $projectRoot 'build-war\smartpet.war'
if (!(Test-Path $war)) {
    Write-Host 'Gerando arquivo smartpet.war via Maven...' -ForegroundColor Cyan
    & (Join-Path $projectRoot 'mvnw.cmd') package -DskipTests
    if (!(Test-Path $war)) {
        throw "Falha ao gerar o arquivo smartpet.war. Verifique os logs do Maven."
    }
}

$env:JAVA_HOME = $javaHome
$env:CATALINA_HOME = $tomcat
$env:CATALINA_BASE = $tomcat
$env:ADMIN_USER = $settings['admin_user']
$env:ADMIN_PASSWORD = $settings['admin_password']
$ssl = if ($settings.ContainsKey('db_ssl') -and $settings['db_ssl']) { $settings['db_ssl'] } else { 'false' }
$env:SMARTPET_DB_URL = "jdbc:mysql://$($settings['db_host']):$($settings['db_port'])/$($settings['db_schema'])?sslMode=$ssl&serverTimezone=UTC&allowPublicKeyRetrieval=true&connectTimeout=10000&socketTimeout=30000"
$env:SMARTPET_DB_USER = $settings['db_user']
$env:SMARTPET_DB_PASSWORD = $settings['db_pass']

$webapps = Join-Path $tomcat 'webapps'
# Para o Tomcat em execução. Se o shutdown não der conta (JVM presa),
# encerra à força o processo dono da porta 8080 antes de subir o novo.
& (Join-Path $tomcat 'bin\shutdown.bat') | Out-Null
for ($i = 0; $i -lt 20; $i++) {
    Start-Sleep -Seconds 1
    if (!(Get-NetTCPConnection -LocalPort 8080 -State Listen -ErrorAction SilentlyContinue)) { break }
}
$dono = Get-NetTCPConnection -LocalPort 8080 -State Listen -ErrorAction SilentlyContinue | Select-Object -First 1 -ExpandProperty OwningProcess
if ($dono) {
    $proc = Get-CimInstance Win32_Process -Filter "ProcessId=$dono" -ErrorAction SilentlyContinue
    $nome = if ($proc) { $proc.Name } else { 'desconhecido' }
    Write-Host "Porta 8080 ocupada por $nome (PID $dono). Encerrando..." -ForegroundColor Yellow
    & taskkill.exe /PID $dono /F /T 2>$null | Out-Null
    for ($i = 0; $i -lt 10; $i++) {
        Start-Sleep -Seconds 1
        if (!(Get-NetTCPConnection -LocalPort 8080 -State Listen -ErrorAction SilentlyContinue)) { break }
    }
}
if (Get-NetTCPConnection -LocalPort 8080 -State Listen -ErrorAction SilentlyContinue) {
    throw 'A porta 8080 continua ocupada. Feche o Tomcat e tente novamente.'
}
$deployId = Get-Date -Format 'yyyyMMdd-HHmmss'
$deployTemp = Join-Path $env:TEMP "smartpet-launcher-unpacked-$deployId"
New-Item -ItemType Directory -Path $deployTemp | Out-Null
Push-Location $deployTemp
try { & (Join-Path $javaHome 'bin\jar.exe') xf $war } finally { Pop-Location }
$appDir = Join-Path $webapps 'smartpet'
if (Test-Path $appDir) {
    $deployBackup = Join-Path $env:TEMP "smartpet-launcher-old-$deployId"
    Move-Item -LiteralPath $appDir -Destination $deployBackup
}
Copy-Item -LiteralPath $war -Destination (Join-Path $webapps 'smartpet.war') -Force
Move-Item -LiteralPath $deployTemp -Destination $appDir
# O Tomcat precisa receber as variáveis do .env. O Start-Process não as herda,
# então gravamos um setenv.bat que o catalina.bat executa antes de subir a JVM.
$setenv = Join-Path $tomcat 'bin\setenv.bat'
$linhas = @(
    '@echo off',
    "set `"SMARTPET_DB_URL=$env:SMARTPET_DB_URL`"",
    "set `"SMARTPET_DB_USER=$env:SMARTPET_DB_USER`"",
    "set `"SMARTPET_DB_PASSWORD=$env:SMARTPET_DB_PASSWORD`"",
    "set `"ADMIN_USER=$env:ADMIN_USER`"",
    "set `"ADMIN_PASSWORD=$env:ADMIN_PASSWORD`""
)
Set-Content -LiteralPath $setenv -Value $linhas -Encoding ASCII
Start-Process -FilePath (Join-Path $tomcat 'bin\startup.bat') -WorkingDirectory $tomcat -WindowStyle Hidden

$ready = $false
for ($i = 0; $i -lt 30; $i++) {
    Start-Sleep -Seconds 1
    try {
        $response = Invoke-WebRequest "$baseUrl`health/db" -UseBasicParsing -TimeoutSec 2
        if ($response.StatusCode -eq 200) { $ready = $true; break }
    } catch { }
}
Start-Process $baseUrl
if ($ready) {
    Write-Host 'SmartPet iniciou e conectou ao MySQL.' -ForegroundColor Green
    Write-Host "Login: $($settings['admin_user'])"
    Write-Host "Senha: $($settings['admin_password'])"
} else {
    Write-Host 'Tomcat iniciou, mas a conex�o MySQL falhou. Confira as configura��es db_* no .env.' -ForegroundColor Yellow
}
Read-Host 'Pressione Enter para fechar esta janela'
