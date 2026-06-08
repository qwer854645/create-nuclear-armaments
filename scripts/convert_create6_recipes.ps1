$ErrorActionPreference = "Stop"

function Test-ItemStack([object]$Obj) {
    return ($Obj -is [System.Collections.IDictionary]) -and $Obj.Contains("item") -and -not $Obj.Contains("id") -and -not $Obj.Contains("type")
}

function Convert-Stack([hashtable]$Obj) {
    $converted = @{}
    if ($Obj.Contains("chance")) { $converted["chance"] = $Obj["chance"] }
    $converted["id"] = $Obj["item"]
    $count = 1
    if ($Obj.Contains("count")) { $count = $Obj["count"] }
    if ($count -ne 1) { $converted["count"] = $count }
    return $converted
}

function Convert-Node($Node) {
    if ($null -eq $Node) { return $null }
    if ($Node -is [System.Collections.IList] -and -not ($Node -is [string])) {
        return @($Node | ForEach-Object { Convert-Node $_ })
    }
    if ($Node -is [System.Collections.IDictionary]) {
        if (Test-ItemStack $Node) { return Convert-Stack $Node }

        $result = [ordered]@{}
        foreach ($entry in $Node.GetEnumerator()) {
            $key = [string]$entry.Key
            if ($key -eq "transitionalItem") { $key = "transitional_item" }
            $result[$key] = Convert-Node $entry.Value
        }
        return $result
    }
    return $Node
}

function Convert-RecipeFile([string]$Path) {
    $json = Get-Content -Raw -Encoding UTF8 $Path | ConvertFrom-Json -AsHashtable -Depth 100
    $converted = Convert-Node $json
    if ($Path -like "*fissile_precursor.json") {
        $converted["pattern"] = @($converted["pattern"] | ForEach-Object { $_.Replace("U", "H") })
    }
    ($converted | ConvertTo-Json -Depth 100) + "`n" | Set-Content -Encoding UTF8 $Path
}

$repo = Split-Path (Split-Path $PSScriptRoot -Parent) -Parent
if (Test-Path (Join-Path $PSScriptRoot "..\src\main\resources")) {
    $repo = Split-Path $PSScriptRoot -Parent
}

$modRecipeRoot = Join-Path $repo "src\main\resources\data\createnucleararmaments\recipe"
Get-ChildItem -Path $modRecipeRoot -Recurse -Filter *.json | ForEach-Object {
    Convert-RecipeFile $_.FullName
}

$failed = @(
    "data/cbcmoreshells/recipe/torpedo_components/reinforced_reductive_short_range_torpedo_mold.json",
    "data/cbcmoreshells/recipe/dual_he_rocket.json",
    "data/cbcmoreshells/recipe/dual_aphe_rocket.json",
    "data/cbcmoreshells/recipe/torpedo_components/slow_long_range_torpedo_mold.json",
    "data/cbcmoreshells/recipe/torpedo_components/highspeed_torpedo_mold.json",
    "data/cbcmoreshells/recipe/torpedo_assembly/light_high_speed_torpedo_assembly.json",
    "data/cbcmoreshells/recipe/torpedo_assembly/gambler_medium_range_torpedo_assembly.json",
    "data/cbcmoreshells/recipe/torpedo_components/medium_range_deepwater_torpedo_mold.json",
    "data/cbcmoreshells/recipe/torpedo_assembly/reinforced_long_range_torpedo_assembly.json",
    "data/cbcmoreshells/recipe/torpedo_assembly/reinforced_medium_range_torpedo_assembly.json",
    "data/cbcmoreshells/recipe/torpedo_assembly/reinforced_short_range_torpedo_assembly.json",
    "data/cbcmoreshells/recipe/torpedo_components/medium_range_torpedo_mold.json",
    "data/cbcmoreshells/recipe/torpedo_components/reductive_medium_range_torpedo_mold.json",
    "data/cbcmoreshells/recipe/torpedo_assembly/reductive_medium_range_torpedo_assembly.json",
    "data/cbcmoreshells/recipe/torpedo_assembly/torpedo_head.json",
    "data/cbcmoreshells/recipe/torpedo_components/early_torpedo_mold.json",
    "data/cbcmoreshells/recipe/torpedo_assembly/reinforced_reductive_medium_range_torpedo_assembly.json",
    "data/cbcmoreshells/recipe/torpedo_components/gambler_medium_range_torpedo_mold.json",
    "data/cbcmoreshells/recipe/torpedo_components/highspeed_long_range_torpedo_mold.json",
    "data/cbcmoreshells/recipe/torpedo_components/reductive_highspeed_torpedo_mold.json",
    "data/cbcmoreshells/recipe/torpedo_assembly/reinforced_torpedo_head.json",
    "data/cbcmoreshells/recipe/torpedo_assembly/early_torpedo_assembly.json",
    "data/cbcmoreshells/recipe/torpedo_components/reinforced_long_range_torpedo_mold.json",
    "data/cbcmoreshells/recipe/torpedo_components/reinforced_short_range_torpedo_mold.json",
    "data/cbcmoreshells/recipe/torpedo_assembly/ultraspeed_torpedo_assembly.json",
    "data/cbcmoreshells/recipe/torpedo_components/reinforced_medium_range_torpedo_mold.json",
    "data/cbcmoreshells/recipe/torpedo_components/reinforced_reductive_medium_range_torpedo_mold.json",
    "data/cbcmoreshells/recipe/torpedo_assembly/highspeed_long_range_torpedo_assembly.json",
    "data/cbcmoreshells/recipe/torpedo_assembly/long_range_torpedo_assembly.json",
    "data/cbcmoreshells/recipe/torpedo_components/long_range_torpedo_mold.json",
    "data/cbcmoreshells/recipe/torpedo_assembly/medium_range_torpedo_assembly.json",
    "data/cbcmoreshells/recipe/torpedo_assembly/highspeed_torpedo_assembly.json",
    "data/cbcmoreshells/recipe/torpedo_components/ultraspeed_torpedo_mold.json",
    "data/cbcmoreshells/recipe/torpedo_assembly/reinforced_reductive_short_range_torpedo_assembly.json",
    "data/cbcmoreshells/recipe/torpedo_components/primary_torpedo_mold.json",
    "data/cbcmoreshells/recipe/deployer/normal_ap.json"
)

$workspace = Split-Path $repo -Parent
$datapackRoot = Join-Path $workspace "cbcmoreshells-create6-recipe-fix"
$jar = Join-Path $repo "libs\CBC-Military-Supplement-1.21.1-2.1.0.jar"
Add-Type -AssemblyName System.IO.Compression.FileSystem
$zip = [System.IO.Compression.ZipFile]::OpenRead($jar)

foreach ($entryPath in $failed) {
    $entry = $zip.GetEntry($entryPath)
    if ($null -eq $entry) { Write-Warning "Missing $entryPath"; continue }
    $reader = New-Object System.IO.StreamReader($entry.Open())
    $raw = $reader.ReadToEnd()
    $reader.Close()
    $json = $raw | ConvertFrom-Json -AsHashtable -Depth 100
    $converted = Convert-Node $json
    $target = Join-Path $datapackRoot $entryPath
    $dir = Split-Path $target -Parent
    New-Item -ItemType Directory -Force -Path $dir | Out-Null
    ($converted | ConvertTo-Json -Depth 100) + "`n" | Set-Content -Encoding UTF8 $target
}
$zip.Dispose()

$packMeta = @{
    pack = @{
        description = "Create 6 recipe format fixes for CBC Military Supplement"
        pack_format = 48
        supported_formats = @{ min_inclusive = 48; max_inclusive = 81 }
    }
} | ConvertTo-Json -Depth 5
$packMeta + "`n" | Set-Content -Encoding UTF8 (Join-Path $datapackRoot "pack.mcmeta")

@'
CBC Military Supplement — Create 6 recipe fix datapack

Install:
  - Copy this folder into your world's datapacks directory, or
  - For dev client: copy into create-nuclear-armaments/run/datapacks/

Fixes 36 recipes that failed JSON parsing under Create 6.0+.

Not fixable via datapack (require More Shells mod update):
  - cbcmoreshells:shell_fuzing
  - cbcmoreshells:shell_fuzing_deployer
'@ | Set-Content -Encoding UTF8 (Join-Path $datapackRoot "README.txt")

$runDp = Join-Path $repo "run\datapacks\cbcmoreshells-create6-recipe-fix"
if (Test-Path $runDp) { Remove-Item -Recurse -Force $runDp }
Copy-Item -Recurse $datapackRoot $runDp
Write-Host "Done. Datapack at $datapackRoot"
