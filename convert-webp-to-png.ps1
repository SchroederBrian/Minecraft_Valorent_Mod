# Convert-WebpToPngReplace.ps1
# Converts webp images to .png, OR renames _gui.png files to _icon.png format, deletes originals, renames outputs to lowercase and dash-separated with _gui to _icon replacement, and cleans up leftovers.

param(
    [string]$TargetFolder = "",  # Optional target folder, defaults to script directory
    [switch]$RenameGuiFiles      # If set, will rename existing _gui.png files instead of converting webp
)

$ScriptDir = if ($TargetFolder -and (Test-Path $TargetFolder)) {
    $TargetFolder
} else {
    Split-Path -Parent $MyInvocation.MyCommand.Path
}

Write-Host "Target folder:" $ScriptDir
if ($RenameGuiFiles) {
    Write-Host "Mode: Rename _gui.png files to _icon.png format"
} else {
    Write-Host "Mode: Convert .webp files to .png"
}

# Temporary output folder
$TempDir = Join-Path $ScriptDir "_converted_temp"
if (Test-Path $TempDir) {
    Remove-Item -Recurse -Force $TempDir
}
New-Item -ItemType Directory -Path $TempDir | Out-Null

if ($RenameGuiFiles) {
    # Rename existing _gui.png files to _icon.png format
    Get-ChildItem -Path $ScriptDir -Filter "*_gui.png" -Recurse -File | ForEach-Object {
        $inFile = $_.FullName
        $baseName = [System.IO.Path]::GetFileNameWithoutExtension($inFile)
        $targetDir = [System.IO.Path]::GetDirectoryName($inFile)

        # Prepare target filename: lowercase, spaces replaced with dashes, _gui replaced with _icon
        $newBaseName = $baseName.ToLower().Replace(" ", "-").Replace("_gui", "_icon")
        $finalFile = Join-Path $targetDir ($newBaseName + ".png")

        Write-Host "`nRenaming:" $inFile "→" $finalFile

        try {
            Move-Item -LiteralPath $inFile -Destination $finalFile -Force
            Write-Host "  ✓ Renamed successfully"
        } catch {
            Write-Host "  ! Failed to rename:" $inFile
        }
    }
} else {
    # Define supported input extensions (webp)
    $exts = @("*.webp")

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

            $outFileTemp = Join-Path $outFolder ($baseName + "_converted.png")

            Write-Host "`nConverting:" $inFile "→" $outFileTemp
            & ffmpeg -y -i "$inFile" "$outFileTemp"

            if ($LASTEXITCODE -eq 0 -and (Test-Path $outFileTemp)) {
                Write-Host "  ✓ Conversion success"

                # Prepare target filename: lowercase, spaces replaced with dashes, _gui replaced with _icon
                $newBaseName = $baseName.ToLower().Replace(" ", "-").Replace("_gui", "_icon")
                $targetDir = [System.IO.Path]::GetDirectoryName($inFile)
                $finalFile = Join-Path $targetDir ($newBaseName + ".png")

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
}

# Cleanup leftover converted temp files (only for webp conversion mode)
if (-not $RenameGuiFiles) {
    Write-Host "`nCleaning up leftover *_converted.png files..."
    Get-ChildItem -Path $ScriptDir -Filter "*_converted.png" -Recurse -File | ForEach-Object {
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
}

if ($RenameGuiFiles) {
    Write-Host "`n✅ _gui.png to _icon.png renaming complete."
} else {
    Write-Host "`n✅ WebP to PNG conversion + renaming complete."
}
