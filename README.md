# Backend Service untuk perpustakaan online menggunakan Spring Boot.

## How to run:
1. clone github repo
2. build jar file -> mvn clean package -DskipTests
3. run docker compose -> docker-compose up --build
   - Auto create docker network 
   - Auto create and run PostgreSQL 
   - Auto create and run Dragonfly (Redis alternative)
   - Auto run backend service (termasuk init data)
4. testing endpoint dengan swagger, buka di browser -> http://localhost:8080/swagger-ui/index.html

## Endpoint:
1. Login (POST: /auth/login)
   - User dengan Role admin -> dummyadmin@gmail.com (password: Password1)
   - User dengan Role User -> dummyuser@gmail.com (password: Password1)
2. Get daftar buku yang available untuk dipinjam (GET: /books)
   - Bearer token diinput pada button Authorize
3. Get buku by id (GET: /books/{id})
   - Bearer token diinput pada button Authorize
   - Path Parameter id diisi
4. Pinjam buku (POST: /rent)
   - Bearer token diinput pada button Authorize
   - id buku dan jumlah hari diisi pada Request Body
5. Get daftar buku yang dipinjam (termasuk yang jatuh tempo) (GET: /rent/admin/active)
   - Bearer token diinput pada button Authorize
   - Hanya role admin yang bisa consume endpoint ini
6. Get daftar buku yang sudah jatuh tempo (GET: /rent/admin/overdue)
   - Bearer token diinput pada button Authorize
   - Hanya role admin yang bisa consume endpoint ini
7. Kembalikan buku (POST: /rent/return)
   - Bearer token diinput pada button Authorize
   - Tidak perlu id buku karena 1 user hanya boleh pinjam 1 buku
8. Logout (POST: /auth/logout)
   - Bearer token diinput pada button Authorize
   - Session dicabut, bearer token tidak bisa digunakan lagi

## 📁 Project Structure
- src/main/java/com/ansy/library/ – Application source code 
- src/test/java/com/ansy/library/ – Unit & integration tests 
- src/main/resources/ – Config & DB migration 
- docker-compose.yml – Container orchestration 
- pom.xml – Maven dependencies

## ⚙️ Environment Variables
- `SPRING_MAIL_USERNAME` – SMTP email
- `SPRING_MAIL_PASSWORD` – SMTP password
- `JWT_SECRET` – JWT encryption secret
- `REDIS_HOST`, `REDIS_PORT` – Redis/Dragonfly config

## 🔐 Auth Feature:
1. Register via email only, validasi domain email. 
2. Email verifikasi (isi parameter SMTP di application-docker.yml)
3. Login via email, rate limit failed login attempts 
4. JWT + session ID dalam token → simpan session ke Redis / Dragonfly 
5. Logout: invalidasi session 
6. Forgot password + Change password 
7. Password policy: panjang + karakter (strength validation)
8. Hash password pakai BCrypt + Salt 
9. Role: USER, ADMIN

## 🔧 Tech stack:
1. Spring Boot 
2. Spring Security 
3. JWT (with sessionId)
4. PostgreSQL 
5. Redis / Dragonfly 
6. BCryptPasswordEncoder 
7. JavaMailSender (untuk verifikasi email & forgot password)
8. Swagger/OpenAPI 3