# Q: Give an integer ’n’, find the factorial of that integer.

n!=n×(n−1)×(n−2)×⋯×1
By definition, Factorial of zero is 1.

```
def factorial(n):
    if n == 0:
        return 1
    return n * factorial(n-1)
```

# Q: sum of the first n integers

```
def sumN(n):
    if n == 0
        return 0
    return n + sumN(n-1)
```
