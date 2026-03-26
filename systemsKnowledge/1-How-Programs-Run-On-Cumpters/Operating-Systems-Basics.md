# Operating Systems Basics

Your computer is controlled by the Operating System (OS).

The OS is responsible for:

- running programs
- managing memory
- managing files
- handling networking
- scheduling CPU time

Example:

Starting a Program When you run:

```
node server.js
```

The OS:

- loads the program into memory
- creates a process
- allocates memory
- schedules CPU time

So the stack becomes:

```
Hardware
↑
Operating System
↑
Processes
↑
Threads / Event Loop
↑
Your Application
↑
Users
```

## The CPU Executes Instructions

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

## Key idea

Your program NEVER talks directly to hardware.

It always goes through the OS.
