$basePath = "src/main/resources/db/migration"

# Create directory if not exists
if (-not (Test-Path $basePath)) {
    New-Item -ItemType Directory -Path $basePath -Force | Out-Null
    Write-Host "Directory created: $basePath"
}

$files = @(
    "V5_05__trg_match_update.sql",
    "V5_06__trg_match_delete.sql",
    "V5_07__trg_match_reg_insert.sql",
    "V5_08__trg_match_reg_update.sql",
    "V5_09__trg_match_reg_delete.sql",
    "V5_10__trg_player_insert.sql",
    "V5_11__trg_player_update.sql",
    "V5_12__trg_player_delete.sql",
    "V5_13__trg_season_insert.sql",
    "V5_14__trg_season_update.sql",
    "V5_15__trg_season_delete.sql"
)

foreach ($file in $files) {

    $fullPath = Join-Path $basePath $file

    if (-not (Test-Path $fullPath)) {
        New-Item -ItemType File -Path $fullPath | Out-Null
        Write-Host "File created: $file"
    }
    else {
        Write-Host "File already exists: $file"
    }
}

Write-Host "Done."
