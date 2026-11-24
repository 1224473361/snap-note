# 停止后台 Vite 开发服务
$ErrorActionPreference = "Stop"

$PcDir = (Resolve-Path (Join-Path $PSScriptRoot "..")).Path
$PidFile = Join-Path $PcDir ".dev\vite.pid"

if (-not (Test-Path $PidFile)) {
    Write-Host "未找到 PID 文件，服务可能未通过 start-dev.ps1 启动。" -ForegroundColor Yellow
    exit 0
}

$raw = (Get-Content $PidFile -Raw).Trim()
if ($raw -notmatch '^\d+$') {
    Remove-Item $PidFile -Force -ErrorAction SilentlyContinue
    Write-Host "PID 文件无效，已清理。" -ForegroundColor Yellow
    exit 0
}

$devPid = [int]$raw
$proc = Get-Process -Id $devPid -ErrorAction SilentlyContinue
if ($proc) {
    Stop-Process -Id $devPid -Force
    Write-Host "已停止开发服务 (PID $devPid)" -ForegroundColor Green
} else {
    Write-Host "进程 $devPid 不存在，可能已停止。" -ForegroundColor Gray
}

Remove-Item $PidFile -Force -ErrorAction SilentlyContinue
