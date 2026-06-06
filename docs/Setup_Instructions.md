# Setup Instructions — MediTrack

These are the steps I followed to get the project running. I'm on Windows 11 but I've included notes for Linux/Mac where things differ.

---

## What You Need Before Starting

You need two things:

1. **JDK 17 or later** — I used JDK 17 because the project uses switch expressions which require Java 14+ at minimum, but 17 is the LTS version so it's the safest choice.
2. **Git** — to clone the repository.

You can download JDK 17 from [Adoptium](https://adoptium.net) — I'd recommend the Temurin distribution, it's free and widely used.

---

## Step 1 — Check if Java is Already Installed

Open a terminal (Command Prompt or PowerShell on Windows, Terminal on Mac/Linux) and type:

```bash
java -version
javac -version
```

If you see something like this, you're good:
```
openjdk version "17.0.10" 2024-01-16
javac 17.0.10
```

If it says something like `'java' is not recognized`, then Java isn't installed or isn't on your PATH — follow the installation steps below.

---

## Step 2 — Installing Java (if needed)

**On Windows:**

1. Go to [https://adoptium.net](https://adoptium.net) and download the JDK 17 Windows x64 installer (.msi file).
2. Run the installer — it should automatically set `JAVA_HOME` and update your PATH. Make sure the checkbox for "Set JAVA_HOME variable" is ticked during installation.
3. Open a **new** Command Prompt (important — old ones won't pick up the new PATH) and run `java -version` again.

If the automatic PATH setup didn't work, you need to do it manually:
- Search "Environment Variables" in the Start menu
- Under System Variables, add a new variable: `JAVA_HOME` = `C:\Program Files\Eclipse Adoptium\jdk-17.x.x.x-hotspot` (adjust to wherever it installed)
- Find the `Path` variable, click Edit, and add `%JAVA_HOME%\bin`
- Open a new terminal and try again

**On Mac:**

If you have Homebrew: `brew install --cask temurin@17`

Or download the .pkg from Adoptium and run it.

**On Linux (Ubuntu/Debian):**

```bash
sudo apt update
sudo apt install openjdk-17-jdk
```

---

## Step 3 — Clone the Repository

```bash
git clone https://github.com/Rohitchandran98/MediTrack.git
cd MediTrack
```

---

## Step 4 — Compile the Project

From the project root directory (the one that contains `src/` and `docs/`):

**On Mac/Linux:**
```bash
mkdir -p out
find src/main/java -name "*.java" > sources.txt
javac --release 17 -d out @sources.txt
```

**On Windows (Command Prompt):**
```cmd
mkdir out
dir /s /b src\main\java\*.java > sources.txt
javac --release 17 -d out @sources.txt
```

If the compile step succeeds, you'll see no output — that's normal. Java compiler is silent on success.

If you see errors, the most common cause is using an older JDK. Double-check with `javac -version`.

---

## Step 5 — Running the App

```bash
java -cp out com.airtribe.meditrack.Main
```

This loads some sample data automatically so you can explore the system right away. You'll see 4 doctors, 3 patients, and 2 pre-booked appointments.

If you want to start from previously saved CSV data instead:
```bash
java -cp out com.airtribe.meditrack.Main --loadData
```

This flag tells the app to read from the `data/` folder (which gets created when you save data through the app menu).

---

## Step 6 — Running the Tests

```bash
java -cp out com.airtribe.meditrack.test.TestRunner
```

This runs 48 manual tests across all major features — validation, entities, deep copy, services, billing strategies, streams, exceptions, and enums. The output ends with:

```
============================
  Passed: 48 | Failed: 0
============================
```

No external testing library needed — it's all plain Java.

---

## Setting it up in IntelliJ IDEA

If you prefer working in an IDE (which I do for most things), here's how to open it:

1. Open IntelliJ → **File → Open** → select the project root folder
2. IntelliJ usually auto-detects things, but if it doesn't mark the source root:
   - Right-click `src/main/java` → **Mark Directory as → Sources Root**
3. Go to **File → Project Structure → Project** and set the SDK to JDK 17
4. To run the app: open `Main.java` → right-click → **Run 'Main.main()'**
5. To run tests: open `TestRunner.java` → right-click → **Run 'TestRunner.main()'**

---

## Where Does the Data Get Saved?

When you use the Save option in the app menu, it creates a `data/` directory in whatever folder you ran the app from:

```
data/
├── doctors.csv
├── patients.csv
├── appointments.csv
├── patients.ser        ← Java serialized binary
└── doctors.ser
```

The CSV files are plain text and you can open them in Excel or any text editor to see what's stored.
