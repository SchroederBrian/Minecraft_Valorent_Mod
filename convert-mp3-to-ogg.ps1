# Convert-ToOggReplace.ps1
# Converts supported audio formats to .ogg, deletes originals, renames outputs to lowercase and dash-separated, and cleans up leftovers.

param(
    [int]$Quality = 5  # Vorbis audio quality 0-10
)

$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
Write-Host "Root folder:" $ScriptDir
Write-Host "Vorbis quality level:" $Quality

# Temporary output folder
$TempDir = Join-Path $ScriptDir "_converted_temp"
if (Test-Path $TempDir) {
    Remove-Item -Recurse -Force $TempDir
}
New-Item -ItemType Directory -Path $TempDir | Out-Null

# Define supported input extensions
$exts = @("*.mp3", "*.wav")

foreach ($ext in $exts) {
    Get-ChildItem -Path $ScriptDir -Filter $ext -Recurse -File | ForEach-Object {
        $inFile = $_.FullName
        $relPath = $_.FullName.Substring($ScriptDir.Length + 1)
        $baseName = [System.IO.Path]::GetFileNameWithoutExtension($inFile)
        $subDir = [System.IO.Path]::GetDirectoryName($relPath)

        if ($subDir) {
            $outFolder = Join-Path $TempDir $subDir
        } else {
            $outFolder = $TempDir
        }
        if (-not (Test-Path $outFolder)) {
            New-Item -ItemType Directory -Path $outFolder | Out-Null
        }

        $outFileTemp = Join-Path $outFolder ($baseName + "_converted.ogg")

        Write-Host "`nConverting:" $inFile "→" $outFileTemp
        & ffmpeg -y -fflags +genpts -avoid_negative_ts make_zero -i "$inFile" -ac 1 -ar 44100 -c:a libvorbis -q:a $Quality "$outFileTemp"

        if ($LASTEXITCODE -eq 0 -and (Test-Path $outFileTemp)) {
            Write-Host "  ✓ Conversion success"

            # Prepare target filename: lowercase and spaces replaced with dashes
            $newBaseName = $baseName.ToLower().Replace(" ", "-")
            $targetDir = [System.IO.Path]::GetDirectoryName($inFile)
            $finalFile = Join-Path $targetDir ($newBaseName + ".ogg")

            # Delete original file
            try {
                Remove-Item -LiteralPath $inFile -Force
                Write-Host "  ⨯ Deleted original:" $inFile
            } catch {
                Write-Host "  ! Failed to delete original:" $inFile
            }

            # Move converted file into place
            try {
                Move-Item -LiteralPath $outFileTemp -Destination $finalFile -Force
                Write-Host "  → Saved final file as:" $finalFile
            } catch {
                Write-Host "  ! Failed to move final file:" $outFileTemp "→" $finalFile
            }
        } else {
            Write-Host "  ✖ Conversion failed for:" $inFile
        }
    }
}

# Cleanup leftover converted temp files
Write-Host "`nCleaning up leftover *_converted.ogg files..."
Get-ChildItem -Path $ScriptDir -Filter "*_converted.ogg" -Recurse -File | ForEach-Object {
    Write-Host "  Deleting leftover:" $_.FullName
    try {
        Remove-Item -LiteralPath $_.FullName -Force
    } catch {
        Write-Host "  ! Could not delete leftover:" $_.FullName
    }
}

# Remove temp directory
if (Test-Path $TempDir) {
    Remove-Item -Recurse -Force $TempDir
}

Write-Host "`n✅ Conversion + renaming complete."
