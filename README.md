# Layanan otentikasi untuk aplikasi Java Portofolio menggunakan Spring Boot.

## 🔐 Auth
1. Register via email only, validasi domain email. 
2. Email verifikasi (SMTP Gmail manual dulu)
3. Login via email, rate limit failed login attempts 
4. JWT + session ID dalam token → simpan session ke Redis / Dragonfly 
5. Logout: invalidasi session 
6. Forgot password + Change password 
7. Password policy: panjang + karakter (strength validation)
8. Hash password pakai BCrypt + Salt 
9. Role: USER, ADMIN 
10. User info endpoint: untuk validasi downstream oleh BE logic

## 🔧 Tech stack:
1. Spring Boot 
2. Spring Security 
3. JWT (with sessionId)
4. PostgreSQL 
5. Redis / Dragonfly 
6. BCryptPasswordEncoder 
7. JavaMailSender (untuk verifikasi email & forgot password)
8. Swagger/OpenAPI 3

### 🧠 Step-by-step pseudocode plan (Auth Service)
1. Entities:
   - User (id, email, password, role, emailVerified, createdAt)
   - VerificationToken (for email verification)
   - PasswordResetToken 
   - Session (optional tracking model or in Redis only)
2. Auth flow:
   - POST /auth/register: validate email → check domain → save user (emailVerified=false) → send email token 
   - GET /auth/verify?token=...: verify email token → set emailVerified=true 
   - POST /auth/login: validate password → check email verified → generate JWT w/ session ID → save session in Redis → return token 
   - Rate limit failed login attempts (Spring Security Filter + Redis counter)
   - POST /auth/logout: invalidate session in Redis 
   - POST /auth/forgot-password: email a token 
   - POST /auth/reset-password: use token to reset password 
   - POST /auth/change-password: old → new 
   - GET /auth/me: get user info from JWT 
   - GET /auth/roles: optional, return available roles
3. Security config:
   - Spring Security filter chain 
   - JWT filter w/ session validation 
   - Role-based access: ADMIN vs USER
4. Redis:
   - Store session ID (sid) → userId mapping 
   - TTL for idle session expiry
5. Swagger/OpenAPI:
   - Auto config + grouping

### Docker Setup
1. Create docker network
2. Run PostgreSQL
3. Run Dragonfly (Redis alternative)
4. Run auth service

## Run semua docker dengan docker compose
1. ./mvnw clean package -DskipTests
2. docker-compose up --build