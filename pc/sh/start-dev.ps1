# SnapNote 育儿记 PC 页：后台启动 Vite 并打开浏览器
$ErrorActionPreference = "Stop"

$PcDir = (Resolve-Path (Join-Path $PSScriptRoot "..")).Path
$Port = 5173
$Url = "http://localhost:$Port"
$DevDir = Join-Path $PcDir ".dev"
$PidFile = Join-Path $DevDir "vite.pid"
$LogFile = Join-Path $DevDir "vite.log"
$ErrFile = Join-Path $DevDir "vite.err.log"

Set-Location $PcDir

function Test-ServerReady {
    try {
        $null = Invoke-WebRequest -Uri $Url -UseBasicParsing -TimeoutSec 2
        return $true
    }
    catch {
        return $false
    }
}

function Get-RunningDevPid {
    if (-not (Test-Path $PidFile)) {
        return $null
    }
    $raw = (Get-Content $PidFile -Raw).Trim()
    if ($raw -notmatch '^\d+$') {
        return $null
    }
    $proc = Get-Process -Id ([int]$raw) -ErrorAction SilentlyContinue
    if ($proc -and -not $proc.HasExited) {
        return [int]$raw
    }
    return $null
}

if (Test-ServerReady) {
    $existing = Get-RunningDevPid
    if ($existing) {
        Write-Host "开发服务已在后台运行 (PID $existing)" -ForegroundColor Yellow
    }
    else {
        Write-Host "端口 $Port 已有服务，直接打开浏览器。" -ForegroundColor Yellow
    }
    Start-Process $Url
    exit 0
}

if (-not (Test-Path (Join-Path $PcDir "node_modules"))) {
    Write-Host "首次运行，正在安装依赖..." -ForegroundColor Cyan
    npm install
    if ($LASTEXITCODE -ne 0) {
        Write-Error "npm install 失败"
        exit 1
    }
}

$viteEntry = Join-Path $PcDir "node_modules\vite\bin\vite.js"
if (-not (Test-Path $viteEntry)) {
    Write-Error "未找到 Vite，请在 pc 目录执行 npm install"
    exit 1
}

New-Item -ItemType Directory -Force -Path $DevDir | Out-Null

$oldPid = Get-RunningDevPid
if ($oldPid) {
    Write-Host "结束旧进程 PID $oldPid ..." -ForegroundColor Gray
    Stop-Process -Id $oldPid -Force -ErrorAction SilentlyContinue
    Start-Sleep -Milliseconds 300
}

Write-Host "正在后台启动开发服务器..." -ForegroundColor Cyan
$node = (Get-Command node -ErrorAction Stop).Source
$proc = Start-Process -FilePath $node `
    -ArgumentList @($viteEntry) `
    -WorkingDirectory $PcDir `
    -WindowStyle Hidden `
    -PassThru `
    -RedirectStandardOutput $LogFile `
    -RedirectStandardError $ErrFile

$proc.Id | Set-Content -Path $PidFile -Encoding ascii -NoNewline

Write-Host "等待 $Url 就绪... (PID $($proc.Id))" -ForegroundColor Gray
$ready = $false
for ($i = 0; $i -lt 60; $i++) {
    Start-Sleep -Milliseconds 500
    if (Test-ServerReady) {
        $ready = $true
        break
    }
    if ($proc.HasExited) {
        Write-Error "Vite 已退出，请查看日志: $LogFile 与 $ErrFile"
        exit 1
    }
}

if ($ready) {
    Start-Process $Url
    Write-Host "已打开浏览器: $Url" -ForegroundColor Green
    Write-Host "停止服务: 执行 pc\sh\stop-dev.ps1" -ForegroundColor Gray
}
else {
    Write-Warning "未在 30 秒内检测到服务，仍尝试打开浏览器。日志: $LogFile"
    Start-Process $Url
}
