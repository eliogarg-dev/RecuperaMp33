# Builds RecuperaMp33.apk without Gradle: javac -> d8 -> aapt2 -> zipalign -> apksigner.
$ErrorActionPreference = 'Stop'
Set-Location $PSScriptRoot

$sdk = "$env:LOCALAPPDATA\Android\Sdk"
$bt  = "$sdk\build-tools\35.0.0"
$jar = "$sdk\platforms\android-35\android.jar"
$jdk = 'C:\Program Files\Android\Android Studio\jbr'
$env:JAVA_HOME = $jdk
$env:PATH = "$jdk\bin;$env:PATH"   # d8/apksigner need Java 11+, PATH has Java 8

function Invoke-Tool { & $args[0] @($args | Select-Object -Skip 1 | ForEach-Object { $_ }); if ($LASTEXITCODE) { throw "$($args[0]) failed ($LASTEXITCODE)" } }

if (Test-Path build) { Remove-Item build -Recurse -Force }
New-Item -ItemType Directory build\classes | Out-Null

Invoke-Tool "$jdk\bin\javac.exe" -source 11 -target 11 -cp $jar -d build\classes src\it\recupera\mp33\MainActivity.java
Invoke-Tool "$bt\d8.bat" --lib $jar --min-api 30 --output build (Get-ChildItem build\classes -Recurse -Filter *.class).FullName
Invoke-Tool "$bt\aapt2.exe" compile --dir res -o build\res.zip
Invoke-Tool "$bt\aapt2.exe" link -I $jar build\res.zip --manifest AndroidManifest.xml --min-sdk-version 30 --target-sdk-version 35 -o build\unsigned.apk
Invoke-Tool "$jdk\bin\jar.exe" uf build\unsigned.apk -C build classes.dex
Invoke-Tool "$bt\zipalign.exe" -p -f 4 build\unsigned.apk build\aligned.apk

$ks = "$env:USERPROFILE\.android\debug.keystore"
if (-not (Test-Path $ks)) {
    Invoke-Tool "$jdk\bin\keytool.exe" -genkeypair -keystore $ks -storepass android -keypass android -alias androiddebugkey -keyalg RSA -validity 10000 -dname "CN=Android Debug,O=Android,C=US"
}
Invoke-Tool "$bt\apksigner.bat" sign --ks $ks --ks-pass pass:android --out RecuperaMp33.apk build\aligned.apk
Write-Host "OK: $PSScriptRoot\RecuperaMp33.apk"
