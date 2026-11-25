# Recupera MP33

Piccola app Android per recuperare i file musicali (`.mp33`, `.mp` e `.mid`) scaricati dai gruppi Facebook che sul telefono "spariscono".

## Il problema

Nei gruppi Facebook i brani MP3 vengono spesso condivisi rinominati in `.mp33` o `.mp`. Sui telefoni **Samsung**, se li scarichi dall'app Facebook, il file non compare in Download: il download parte in una cartella nascosta temporanea (`Download/.!%#@$`) e poi finisce nel **Cestino** di Archivio (My Files).

## Cosa fa l'app

Ogni volta che la apri:

1. cerca i file `.mp33`, `.mp` e `.mid` nel Cestino di Archivio e nella cartella temporanea dei download;
2. li copia in **Download**, rinominando `.mp33` e `.mp` in `.mp3`;
3. li toglie dal Cestino (i file della cartella temporanea vengono solo copiati, perché potrebbero essere download ancora in corso);
4. salta i doppioni (per esempio `Brano-1.mp33` se `Brano.mp3` è già in Download);
5. mostra quanti file ha recuperato e si chiude.

## Installazione

1. Scarica `RecuperaMp33.apk` dalla pagina [Releases](../../releases/latest).
2. Aprilo dal telefono e tocca **Installa**. Android chiederà di consentire l'installazione da questa origine: conferma. Se Play Protect avvisa che l'app è sconosciuta, scegli **Installa comunque**.
3. Alla prima apertura attiva il permesso **"Accesso a tutti i file"** e torna indietro: serve per leggere il Cestino e le cartelle nascoste.

## Uso

Scarica il brano dal gruppo Facebook, poi apri **Recupera MP33**. Il brano sarà in Download.

Se l'app dice "Nessun file nuovo da recuperare", Facebook ha tenuto il file nella memoria privata dei download, dove nessuna app può leggerlo. In questo caso apri la tendina delle notifiche, tocca la notifica **"Download completato"** del brano e scegli **Recupera MP33**: il brano viene salvato in Download (`.mp33` e `.mp` diventano `.mp3`).

## Limiti

- Pensata per telefoni **Samsung** con Android 11 o successivo (usa il Cestino di Archivio di Samsung). Provata su un solo telefono.
- Funziona finché Facebook e Samsung si comportano così: se cambiano le cartelle, l'app va aggiornata.
- Non esiste una versione per iPhone.

## Compilare da sorgente

Serve Android Studio installato (usa il suo JDK e l'Android SDK con build-tools 35 e platform android-35). Da PowerShell:

```powershell
.\build.ps1
```

Genera `RecuperaMp33.apk` senza Gradle (javac → d8 → aapt2 → zipalign → apksigner).

L'icona usa due simboli di [Material Icons](https://fonts.google.com/icons) (licenza Apache 2.0).
