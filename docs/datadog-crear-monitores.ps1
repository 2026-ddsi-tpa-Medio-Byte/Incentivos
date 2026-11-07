<#
.SYNOPSIS
  Crea o actualiza en Datadog los monitores (alarmas) de Incentivos definidos en docs/datadog-monitor-*.json.

.DESCRIPTION
  Es idempotente: si ya existe un monitor con el mismo nombre y el tag service:incentivos, lo actualiza
  en vez de duplicarlo. Las claves se leen de variables de entorno y nunca se escriben en disco:
    DD_API_KEY   API key de la organización (la misma que usa el servicio en Render)
    DD_APP_KEY   Application key (Organization Settings -> Application Keys)
    DD_SITE      sitio de Datadog, por ejemplo us5.datadoghq.com (por defecto datadoghq.com)

.PARAMETER Notificar
  Destinatarios EXTRA que se agregan al mensaje de cada monitor, por ejemplo "@otro@mail.com".
  Los JSON ya incluyen @taraujo@frba.utn.edu.ar, asi que no hace falta pasarlo de nuevo.
  Un @mail en el mensaje hace que Datadog mande la alerta por mail a esa direccion.

.PARAMETER SoloValidar
  Valida cada definición contra la API sin crear ni modificar nada.

.EXAMPLE
  $env:DD_API_KEY = "..."; $env:DD_APP_KEY = "..."; $env:DD_SITE = "us5.datadoghq.com"
  .\docs\datadog-crear-monitores.ps1
#>
param(
  [string]$Notificar = "",
  [switch]$SoloValidar
)

$ErrorActionPreference = "Stop"

$apiKey = $env:DD_API_KEY
$appKey = $env:DD_APP_KEY
$site = if ($env:DD_SITE) { $env:DD_SITE.Trim() } else { "datadoghq.com" }
if (-not $apiKey -or -not $appKey) {
  throw "Faltan DD_API_KEY y/o DD_APP_KEY en las variables de entorno."
}

$base = "https://api.$site/api/v1/monitor"
$headers = @{ "DD-API-KEY" = $apiKey.Trim(); "DD-APPLICATION-KEY" = $appKey.Trim() }

function Invocar($metodo, $url, $cuerpo) {
  $params = @{ Method = $metodo; Uri = $url; Headers = $headers; ContentType = "application/json; charset=utf-8" }
  if ($cuerpo) {
    # Se envía como bytes UTF-8: Windows PowerShell 5.1 manda los strings en otra codificación y
    # los acentos de los mensajes llegarían rotos.
    $params.Body = [System.Text.Encoding]::UTF8.GetBytes(($cuerpo | ConvertTo-Json -Depth 10))
  }
  Invoke-RestMethod @params
}

$definiciones = Get-ChildItem -Path $PSScriptRoot -Filter "datadog-monitor-*.json"
if (-not $definiciones) { throw "No se encontraron archivos datadog-monitor-*.json en $PSScriptRoot" }

$existentes = @(Invocar "GET" "$($base)?monitor_tags=service:incentivos" $null)
Write-Host "Sitio: $site · monitores de Incentivos ya existentes: $($existentes.Count)"

foreach ($archivo in $definiciones) {
  $monitor = [System.IO.File]::ReadAllText($archivo.FullName, [System.Text.Encoding]::UTF8) | ConvertFrom-Json
  if ($Notificar) { $monitor.message = "$($monitor.message)`n`n$Notificar" }

  if ($SoloValidar) {
    Invocar "POST" "$base/validate" $monitor | Out-Null
    Write-Host "  [válido]      $($monitor.name)"
    continue
  }

  $previo = $existentes | Where-Object { $_.name -eq $monitor.name } | Select-Object -First 1
  if ($previo) {
    $r = Invocar "PUT" "$base/$($previo.id)" $monitor
    Write-Host "  [actualizado] $($r.name) (id $($r.id))"
  } else {
    $r = Invocar "POST" $base $monitor
    Write-Host "  [creado]      $($r.name) (id $($r.id))"
  }
}

if (-not $SoloValidar) {
  Write-Host "Listo. Verlos en https://app.$($site -replace '^app\.', '')/monitors/manage?q=service%3Aincentivos"
}
