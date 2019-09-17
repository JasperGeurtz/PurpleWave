# Building VioletLily

## Get Started

What you'll need:
* Windows 
* Java Development Kit 8: https://adoptopenjdk.net/

## Building

Run
`gradlew.bat `

## Running PurpleWave

* Find the path of your 32-bit Java.exe (likely in c:\Program Files (x86)\, not c:\Program Files|)
* Open a command prompt (cmd.exe, not PowerShell). Substitute your Java.exe path.
`
C:\Program Files (x86)\Java\jre1.8.0_171\bin\java.exe" -jar C:\Users\d\AppData\Roaming\scbw\bots\PurpleDev\AI\PurpleWave.jar
`
* You'll know it's working if you see "Connecting to Broodwar" likely followed by "No server proc ID"
* You're now ready to connect to BWAPI 4.1.2! See [BWAPI documentation](https://github.com/bwapi/bwapi) for running it with client bots like PurpleWave.
    
You can also run PurpleWave directly from IntelliJ with Run -> Run 'PurpleWave'