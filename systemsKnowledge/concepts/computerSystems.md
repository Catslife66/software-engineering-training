## What Happens When You Run a Program?

- **Step 1 — The Operating System Takes Control**

Your computer is controlled by the Operating System (OS).

The OS is responsible for:

- running programs
- managing memory
- managing files
- handling networking
- scheduling CPU time

- **Step 2 — Your Program Becomes a Process**

A process is:

> A running instance of a program.

Each process is isolated from the others.

- **Step 3 — The Process Gets Its Own Memory**
  When a process starts, the OS gives it its own memory space.

Memory is usually organised like this:

```
+---------------------+
| Stack               |
| (function calls)    |
+---------------------+
| Heap                |
| (objects, arrays)   |
+---------------------+
| Global variables    |
+---------------------+
| Program code        |
+---------------------+
```

**Stack** used for:

- function calls
- local variables

**Heap** used for dynamic memory allocation.

- **Step 4 — The CPU Executes Instructions**
  Your program becomes machine instructions that the CPU executes.
  Simplified flow:

```
Your Code
   ↓
Interpreter / Compiler
   ↓
Machine Instructions
   ↓
CPU Executes Them
```

- **Step 5 — Multiple Programs Share the CPU**
  The OS constantly switches between processes:

```
Process A -> 5ms
Process B -> 5ms
Process C -> 5ms
Process A -> 5ms
```

This is called: **CPU Scheduling**
This switching happens thousands of times per second, which creates the illusion that programs run simultaneously.

### Key Mental Model

Think of software systems like this:

```
Hardware
   ↑
Operating System
   ↑
Processes
   ↑
Your Application
   ↑
Users
```

Every application you build sits on top of this stack.
