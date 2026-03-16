# Memory Basics (Stack vs Heap)

When the OS creates a process, it gives it memory space.

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

## Stack

The stack used for:

- function calls
- local variables
- execution context

Example:

```
function add(a, b) {
  return a + b
}
```

Stack memory is:

- fast
- automatically managed
- small

## Heap

The heap stores dynamically allocated objects.
Example:

```
const user = {
  name: "Alice",
  age: 30
}
```

Heap memory is:

- larger
- slower
- managed by garbage collection

In languages like JavaScript or Java, the runtime automatically cleans unused heap memory.
