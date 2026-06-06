# JVM Report — Understanding How Java Actually Works

When I first started learning Java, I honestly just wrote code and ran it without thinking much about what happens underneath. But going through this project made me dig deeper into how the JVM works, and it's actually pretty fascinating once you get past the initial complexity.

---

## 1. The Class Loader — How Java Finds Your Code

So before any of your Java code actually runs, the JVM needs to load it. That's the Class Loader's job. It doesn't just dump everything into memory at once — it loads classes on demand, which is actually a smart design choice.

The process has three steps:

**Loading** — The class loader reads the `.class` file (the compiled bytecode) and brings it into memory. This can come from your local disk, a JAR file, or even over a network.

**Linking** — This step has three parts inside it. First, it *verifies* that the bytecode isn't corrupted or malicious. Then it *prepares* memory for all the static variables and gives them default values (like `0` for int, `null` for objects). Finally, it *resolves* all the symbolic references — basically it figures out where all the classes and methods actually live in memory.

**Initialization** — This is when your `static {}` blocks run and static variables get their actual assigned values. In my MediTrack project, you'll see this in every entity class — for example, when `Doctor.class` is loaded for the first time, the static block prints `"[Doctor] Doctor registry initialized."` This is not just for show — it's a real demonstration of when class initialization happens.

There are three built-in class loaders that work in a parent-delegation model:

- **Bootstrap ClassLoader** — loads the core Java library (`java.lang`, `java.util`, etc.). This one is written in native code, not Java itself.
- **Extension ClassLoader** — loads JDK extension classes.
- **Application ClassLoader** — this is the one that loads your actual project classes from the classpath.

The delegation model means that before any loader tries to load a class, it first asks its parent. This prevents you from accidentally overriding core Java classes.

---

## 2. Runtime Data Areas — Where Everything Lives in Memory

This is one of the things I found most confusing at first, but once I drew it out it made sense. The JVM divides memory into several distinct areas:

### Heap
The heap is where all objects live. Every time you do `new Patient(...)` or `new Doctor(...)` in MediTrack, that object is created on the heap. The heap is shared across all threads, which is why thread safety matters for shared objects. The garbage collector is responsible for cleaning up objects on the heap when they're no longer referenced — so when an appointment gets cancelled and no variable holds a reference to it, the GC can eventually free that memory.

### JVM Stack (Thread Stack)
Every thread gets its own stack. When a method is called, a new *stack frame* is pushed onto it containing the local variables for that method, a reference to the current object (`this`), and some bookkeeping info. When the method returns, that frame is popped off. This is why stack overflow errors happen — if you have infinite recursion, you keep pushing frames until there's no space left.

In MediTrack, when `bookAppointment()` in `Main.java` calls into `AppointmentService`, and that calls `IdGenerator`, each of those method calls has its own stack frame active at the same time.

### Method Area (Metaspace in Java 8+)
Class-level data lives here — the bytecode of methods, static variables, the constant pool. So `Constants.DEFAULT_TAX_RATE` (which is a static field) lives here, not on the heap. In Java 8+, this area is called *Metaspace* and it lives in native memory rather than the JVM heap, which removed a lot of `OutOfMemoryError: PermGen space` errors that used to plague older Java apps.

### PC (Program Counter) Register
Each thread has its own PC register. It holds the address of the JVM instruction currently being executed. As the execution engine runs through bytecode, the PC register keeps track of where we are. It's a very small piece of memory but essential for the JVM to know which instruction to execute next.

### Native Method Stack
Used when Java calls native code (written in C/C++) through JNI. Not something I used directly in this project, but it's there.

---

## 3. The Execution Engine — Actually Running Your Code

The execution engine is what reads bytecode and actually *does* something with it. It has two main strategies:

**Interpreter** — Goes through bytecode one instruction at a time and executes each. Simple and starts immediately, but slow because every instruction has overhead even if it's been executed thousands of times before.

**JIT (Just-In-Time) Compiler** — The smarter approach. The JVM watches which parts of the code are executed frequently (called "hot spots"). When it identifies a hot method, it compiles the entire thing to native machine code. After that, subsequent calls to that method run at native speed — no more bytecode interpretation.

The JVM uses both together. The interpreter handles everything at startup, and the JIT kicks in over time for hot code. In a long-running app like a hospital management system server, the JIT would make a huge difference.

---

## 4. JIT Compiler vs Interpreter — When Does Each Make Sense?

I used to think the JIT was just "better" than the interpreter, but they actually serve different purposes.

The **interpreter** is better for:
- Short-lived programs where JIT compilation overhead isn't worth it
- Code that only runs once or rarely
- Startup time — the interpreter just goes, no warm-up needed

The **JIT compiler** is better for:
- Long-running applications (web servers, backend services)
- Code that runs in tight loops or is called thousands of times
- Performance-critical paths

HotSpot JVM (the standard Oracle/OpenJDK JVM) actually has two JIT compilers: **C1** (client compiler — quick compilation, moderate optimization) and **C2** (server compiler — slower compilation but aggressive optimization). Modern JVMs use tiered compilation, starting with C1 and eventually using C2 for the hottest methods.

In MediTrack, the main menu loop and frequently-called methods like `DataStore.filter()` would eventually get JIT-compiled in a long-running session.

---

## 5. "Write Once, Run Anywhere" — Why Java Works on Every OS

This was Java's big selling point when it launched in 1995, and it still holds true. The idea is simple but the implementation is clever.

When I run `javac Main.java`, the compiler doesn't produce machine code for Windows or Linux. It produces **bytecode** — an intermediate representation that the JVM understands but no physical CPU natively executes. This bytecode is stored in `.class` files.

```
My Java source (.java)
       ↓  javac (compiler)
  Bytecode (.class)  ← same file on any platform
       ↓  JVM (platform-specific)
Native machine code (Windows x64, Linux ARM, macOS, etc.)
```

Each operating system has its own JVM implementation, but the bytecode is identical. So the `out/` folder I compile on my Windows machine can be zipped up and run on a Linux server or a Mac without recompilation. That's WORA.

The limitation is that the JVM itself must be installed on the target machine, and some things (like native libraries via JNI) can still be platform-specific. But for pure Java code like MediTrack, it genuinely works everywhere.
