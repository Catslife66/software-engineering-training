# Hashing

How systems store sensitive data securely?

**Problem**

Suppose a user signs up:

```
username: alice
password: 123456
```

If we store:

```
password = "123456"
```

👉 Huge problem:

if DB leaks → all passwords exposed ❌

**Solution — Hashing**

Hashing transforms data:

```
input → fixed output
```

Example:

```
"123456" → e10adc3949ba59abbe56e057f20f883e
```

## Key Properties of Hashing

### 1. One-way

```
cannot reverse hash → original password
```

### 2. Deterministic

```
same input → same output
```

### 3. Fixed length

```
any input → same length output
```

## How Password Storage Works

Instead of storing:

```
password = "123456"
```

We store:

```
hash("123456")
```

Login Flow

```
User enters password
   ↓
hash(input)
   ↓
compare with stored hash
```

## Same Passwords Problem

If two users use: "123456"

They get: same hash ❌

This is insecure.

**Solution — Salt**

Add randomness:

```
password + random salt → hash
```

Now:

```
same password → different hash
```

## Real Algorithms

Examples:

- SHA-256
- bcrypt

## Important Note

For passwords, we use:

```
slow hashing (bcrypt) -> increase the time required to compute each hash
```

This makes brute-force and dictionary attacks significantly more expensive, because attackers must spend more time and computational resources for each password attempt. So attackers cannot brute-force easily.

## Mental Model

```
Hash = fingerprint of data
Hashing → hides password
Salt → prevents pattern attacks
Slow hashing → slows attackers down
```

## CHECKPOINT

**1️⃣ Why is storing plain-text passwords dangerous?**

Storing plain-text passwords is dangerous because they are directly readable if the database is compromised.

Since there is no protection or transformation applied, attackers can immediately access all user credentials.

As a result, this can lead to account takeovers, data breaches, and significant financial and reputational damage.

**2️⃣ Why is hashing considered one-way?**

Hashing is considered one-way because it is computationally infeasible to reverse a hash back to its original input.

Given a hash output, it is extremely difficult to determine the original password.

As a result, even if attackers obtain the hashed data, they cannot easily recover the original passwords.

**3️⃣ Why do we add salt to passwords?**

Salt adds a random value to each password before hashing.

This ensures that even if two users have the same password, their hashes will be different.

As a result, it prevents attackers from using precomputed tables (rainbow tables) and makes large-scale attacks much harder.
