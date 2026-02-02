$root = "backend\src\main\java"

$outputDir = Join-Path $root "FilesForCheck"

if (-not (Test-Path $outputDir)) {
    New-Item -ItemType Directory -Path $outputDir | Out-Null
}

$allJavaFiles = Get-ChildItem -Path $root -Recurse -File -Filter *.java |
        Where-Object {
            $_.FullName -notlike "*\zatimNepouzite\*" -and
                    $_.FullName -notlike "*\FilesForCheck\*"
        }

$groups = $allJavaFiles | Group-Object DirectoryName

$utf8Bom = New-Object System.Text.UTF8Encoding($true)

foreach ($group in $groups) {
    $dirPath = $group.Name
    $packageName = Split-Path $dirPath -Leaf
    $txtPath = Join-Path $outputDir ("{0}.txt" -f $packageName)

    $javaFiles = $group.Group | Sort-Object FullName
    if ($javaFiles.Count -eq 0) { continue }

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

    foreach ($file in $javaFiles) {
        [void]$sb.AppendLine()
        [void]$sb.AppendLine("-----")
        [void]$sb.AppendLine("# Soubor: $($file.FullName)")
        [void]$sb.AppendLine("-----")
        [void]$sb.AppendLine()

        # ⬇⬇⬇ TADY JE ZMĚNA ⬇⬇⬇
        $content = Get-Content $file.FullName -Raw -Encoding UTF8
        [void]$sb.AppendLine($content)
        [void]$sb.AppendLine()
    }

    [System.IO.File]::WriteAllText($txtPath, $sb.ToString(), $utf8Bom)
}
