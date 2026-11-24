param(
    [Parameter(Mandatory = $true)]
    [string]$LocalPropertiesPath
)

if (-not (Test-Path -LiteralPath $LocalPropertiesPath)) {
    exit 0
}

function Unescape-LocalPropertyValue([string]$Value) {
    $sb = New-Object System.Text.StringBuilder
    for ($i = 0; $i -lt $Value.Length; $i++) {
        $ch = $Value[$i]
        if ($ch -eq '\' -and $i + 1 -lt $Value.Length) {
            $sb.Append($Value[++$i]) | Out-Null
        } else {
            $sb.Append($ch) | Out-Null
        }
    }
    return $sb.ToString()
}

Get-Content -LiteralPath $LocalPropertiesPath | ForEach-Object {
    $line = $_.Trim()
    if ($line.StartsWith('#') -or -not $line.Contains('=')) {
        return
    }
    if ($line -match '^\s*java\.home\s*=(.*)$') {
        $raw = $matches[1].Trim()
        if ($raw.Length -gt 0) {
            $javaHome = (Unescape-LocalPropertyValue $raw).Trim()
            if ($javaHome.Length -gt 0) {
                [Console]::Out.Write($javaHome)
            }
        }
        exit 0
    }
}

exit 0
