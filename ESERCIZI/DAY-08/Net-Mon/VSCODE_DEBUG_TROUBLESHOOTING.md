# VS Code Java Workspace Cleanup Instructions

Se riscontri errori di dipendenze anche dopo la ricompilazione Maven, segui questi passaggi:

## Metodo 1: Pulisci la Workspace Java (RACCOMANDATO)

1. In VS Code, premi `Ctrl+Shift+P` (o `Cmd+Shift+P` su Mac)
2. Digita: `Java: Clean Java Language Server Workspace`
3. Seleziona il comando e conferma con "Reload and delete"
4. Attendi il ricaricamento di VS Code
5. Riprova il debug (F5)

## Metodo 2: Riavvio Completo

1. Chiudi completamente VS Code
2. Riapri la cartella `spark-normalizer`
3. Attendi che Java Language Server carichi il progetto
4. Riprova il debug (F5)

## Metodo 3: Esegui direttamente il JAR (se il debug non funziona)

Invece di usare il debug, puoi eseguire direttamente l'uber-jar:

```powershell
# PowerShell - dalla directory principale Net-Mon
.\run-spark-normalizer.ps1
```

oppure

```cmd
# Command Prompt
run-spark-normalizer.bat
```

Questo usa il JAR shaded appena compilato (220 MB) con tutte le dipendenze corrette.

## Verifica Versione Jackson nel JAR

Per verificare che il JAR contenga Jackson 2.15.3:

```powershell
# Estrai e controlla
Add-Type -AssemblyName System.IO.Compression.FileSystem
$zip = [System.IO.Compression.ZipFile]::OpenRead("spark-normalizer\target\spark-normalizer.jar")
$zip.Entries | Where-Object { $_.FullName -like "*jackson-databind*" } | Select-Object FullName
$zip.Dispose()
```

---

**Nota**: Il JAR è stato ricompilato con successo alle 17:13:32 con Jackson 2.15.3
