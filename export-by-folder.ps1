# Kořen Java zdrojáků – u tebe v backendu
$root = "backend\src\main\java"

# Výstupní adresář – kam uložíme TXT soubory
$outputDir = Join-Path $root "FilesForCheck"

# Vytvoří výstupní složku, pokud neexistuje
if (-not (Test-Path $outputDir)) {
    New-Item -ItemType Directory -Path $outputDir | Out-Null
}

# Všechny .java soubory rekurzivně, kromě zatimNepouzite a FilesForCheck
$allJavaFiles = Get-ChildItem -Path $root -Recurse -File -Filter *.java |
    Where-Object {
        $_.FullName -notlike "*\zatimNepouzite\*" -and
        $_.FullName -notlike "*\FilesForCheck\*"
    }

# Seskupíme soubory podle adresáře (folderu = "balíčku")
$groups = $allJavaFiles | Group-Object DirectoryName

# Připravíme enkodér UTF-8 s BOM
$utf8Bom = New-Object System.Text.UTF8Encoding($true)

foreach ($group in $groups) {
    $dirPath = $group.Name                    # plná cesta ke složce
    $packageName = Split-Path $dirPath -Leaf  # název složky (balíčku)
    $txtPath = Join-Path $outputDir ("{0}.txt" -f $packageName)

    $javaFiles = $group.Group | Sort-Object FullName

    if ($javaFiles.Count -eq 0) {
        continue
    }

    # Postavíme celý obsah do jednoho velkého stringu
    $sb = New-Object System.Text.StringBuilder

    [void]$sb.AppendLine("Balíček (složka): $packageName")
    [void]$sb.AppendLine("Cesta: $dirPath")
    [void]$sb.AppendLine()
    [void]$sb.AppendLine("Seznam souborů:")
    foreach ($file in $javaFiles) {
        [void]$sb.AppendLine($file.Name)
    }
    [void]$sb.AppendLine()
    [void]$sb.AppendLine()

    # ===== OBSAH JEDNOTLIVÝCH SOUBORŮ =====
    foreach ($file in $javaFiles) {
        [void]$sb.AppendLine()
        [void]$sb.AppendLine("-----")
        [void]$sb.AppendLine("# Soubor: $($file.FullName)")
        [void]$sb.AppendLine("-----")
        [void]$sb.AppendLine()

        $content = Get-Content $file.FullName -Raw
        [void]$sb.AppendLine($content)
        [void]$sb.AppendLine()
    }

    # Zapíšeme jedním šupem jako UTF-8 s BOM
    [System.IO.File]::WriteAllText($txtPath, $sb.ToString(), $utf8Bom)
}
