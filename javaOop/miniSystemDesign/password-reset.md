# User Resets Password

POST /users/reset-password

Input:

- email

System:

- check user exists
- generate reset token
- save reset token
- send reset email
- return success message

Controller:

- receives ResetPasswordRequestDTO { email }
- calls UserService.requestPasswordReset(email)
- maps Result to HTTP response

Service:

- validates email
- checks whether user exists
- calls ResetTokenService to generate token
- calls ResetTokenRepository to save token
- calls EmailService/Notification to send reset email
- returns Result

Repositories:

- UserRepository.findByEmail(email)
- ResetTokenRepository.save(token)

Helper services

- ResetTokenService.generateToken(user)
- EmailService.sendResetEmail(email, token)

Possible results:

- invalid email format → 400 Bad Request
- valid request, user may or may not exist → 200 OK with generic message
- email service/database failure → exception / 500

## Reset Password Flow

**1. User requests reset**

```
POST /users/reset-password
```

System:

- generates a reset token
- saves it
- sends email with link: "https://yourapp.com/reset-password?token=abc123"

**2. User clicks the link**

Now the user proves:

```
“I have access to this email account”
```

**3. User submits new password**

```
POST /users/reset-password/confirm
{
  "token": "abc123",
  "newPassword": "..."
}
```

Now backend must verify:

- is this token valid?
- is it expired?
- which user does it belong to?

**4. System confirms reset**

- Validate the reset token exists
- Check the token is not expired
- Check the token has not already been used
- Validate the new password
- Find the user linked to the token
- Hash the new password with `PasswordService`
- Update and save the user
- Mark the reset token as used
- Optionally send a confirmation email

## Reset token expiration

1. Token leakage risk

```
Email could be:
- intercepted
- accessed later
- left open on shared device
```

2. Reduce attack window

```
Short-lived token = smaller risk window
```

3. Security best practice

```
All sensitive tokens should be time-limited
```

Even with expiration, we also:

- mark token as used
- invalidate after first use

So:

```
Single-use + expiration = strong security
```

1. system validates the token and its TTL
2. validates password
3. find the use object and update the password
4. find the reset token object and mark it as used
5. notify user the password has updated

## Confirm Reset Flow

Service:

```
PasswordResetService.confirmReset(token, newPassword)

- tokenRecord = resetTokenRepository.findByToken(token)
- if tokenRecord == null → failure INVALID_TOKEN
- if tokenRecord.isExpired() → failure TOKEN_EXPIRED
- if tokenRecord.isUsed() → failure TOKEN_USED
- if password invalid → failure INVALID_PASSWORD
- user = userRepository.findById(tokenRecord.getUserId())
- hashedPassword = passwordService.hash(newPassword)
- user.updatePassword(hashedPassword)
- userRepository.save(user)
- tokenRecord.markUsed()
- resetTokenRepository.save(tokenRecord)
- notification.send("Your password was changed")
- return Result.success("Password updated successfully")
```

Possible results:

```
invalid/expired/used token → 400 or 401 depending on design
invalid password → 400
success → 200
unexpected database/email issue → exception / 500
```
