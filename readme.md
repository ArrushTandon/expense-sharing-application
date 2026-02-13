# 💰 Expense Sharing Application - Secure Backend API

A robust and secure backend system for managing shared expenses among groups, similar to Splitwise. Built with Spring Boot and featuring JWT authentication, role-based access control, and comprehensive security measures. This application allows users to create groups, track expenses, and automatically calculate simplified balances to minimize the number of transactions needed for settlements.

## 📋 Table of Contents

- [About the Project](#-about-the-project)
- [Features](#-features)
- [Security Features](#-security-features)
- [Tech Stack](#️-tech-stack)
- [Architecture](#️-architecture)
- [Getting Started](#-getting-started)
- [API Documentation](#-api-documentation)
- [Database Schema](#️-database-schema)
- [Key Algorithms](#-key-algorithms)
- [Security Implementation](#-security-implementation)
- [Testing](#-testing)
- [Known Limitations](#️-known-limitations)
- [Future Enhancements](#-future-enhancements)

## 🎯 About the Project

This is a production-ready expense-sharing application backend that demonstrates clean architecture principles, security best practices, proper separation of concerns, and efficient algorithm implementation for financial calculations. The system handles complex scenarios like multiple split types, balance calculations, transaction minimization, and secure multi-user access control.

### Problem Statement

When friends or colleagues share expenses, tracking who owes whom can become complicated and insecure. This application solves that problem by:
- Securely tracking all shared expenses with proper authentication
- Protecting user privacy with role-based access control
- Supporting multiple ways to split bills (equal, exact amounts, percentages)
- Automatically calculating balances with security validations
- Minimizing the number of transactions needed to settle debts
- Ensuring data integrity and preventing unauthorized access

## ✨ Features

### Core Functionality
- **User Authentication**: Secure registration and login with JWT tokens
- **User Management**: Create and manage user profiles with privacy controls
- **Group Management**: Create groups, add/remove members with access control
- **Expense Tracking**: Record expenses with multiple split types and validations
- **Balance Calculation**: Automatic calculation of who owes whom
- **Balance Simplification**: Minimize transactions using graph-based algorithms
- **Settlement Recording**: Track when debts are paid with proper validation

### Split Types Supported
1. **Equal Split**: Divide expense equally among all participants
2. **Exact Amount Split**: Specify exact amount for each participant (with validation)
3. **Percentage Split**: Split based on custom percentages (validates to 100%)

### Smart Features
- JWT-based authentication and authorization
- Role-based access control (USER and ADMIN roles)
- Automatic balance calculation across all expenses
- Transaction minimization (reduces N transactions to optimal number)
- Handles complex multi-user, multi-expense scenarios
- Proper rounding handling to avoid discrepancies
- Privacy enforcement (users can only see their own data and groups they belong to)
- Circular reference protection in API responses

## 🔒 Security Features

### Authentication & Authorization
- ✅ **JWT Token Authentication**: Secure stateless authentication
- ✅ **Password Encryption**: BCrypt hashing for passwords
- ✅ **Token Expiration**: 24-hour token validity with automatic logout
- ✅ **Role-Based Access Control**: USER and ADMIN roles
- ✅ **Protected Endpoints**: All sensitive operations require authentication

### Privacy & Access Control
- ✅ **User Privacy**: Users can only view their own profile details
- ✅ **Group Privacy**: Only group members can access group data
- ✅ **Expense Privacy**: Only group members can view/create expenses
- ✅ **Admin Override**: Admins can view all data for management purposes

### Data Protection
- ✅ **Input Validation**: Comprehensive validation on all inputs
- ✅ **SQL Injection Protection**: JPA/Hibernate prevents SQL injection
- ✅ **Circular Reference Prevention**: DTOs prevent infinite JSON loops
- ✅ **Sensitive Data Protection**: No passwords or tokens in responses
- ✅ **CORS Configuration**: Controlled cross-origin access

### Business Logic Security
- ✅ **Amount Validation**: Prevents negative or zero amounts
- ✅ **Split Validation**: Ensures splits sum correctly
- ✅ **Self-Payment Prevention**: Cannot settle with yourself
- ✅ **Group Creator Protection**: Cannot remove group creator
- ✅ **Member Validation**: Only members can modify group data

## 🛠️ Tech Stack

### Backend Framework
- **Spring Boot 3.5.9** - Application framework
- **Java 17** - Programming language
- **Maven** - Dependency management and build tool

### Security
- **Spring Security** - Authentication and authorization
- **JWT (JSON Web Tokens)** - Stateless authentication
- **BCrypt** - Password hashing
- **JJWT** - JWT token generation and validation

### Database
- **PostgreSQL 16** - Primary relational database
- **Hibernate/JPA** - ORM for database interactions
- **HikariCP** - High-performance connection pooling

### Key Dependencies
- **Spring Data JPA** - Data access layer
- **Spring Web** - RESTful API development
- **Spring Security** - Security framework
- **Spring Validation** - Request validation
- **Spring Boot Actuator** - Application monitoring
- **Lombok** - Reduce boilerplate code
- **PostgreSQL Driver** - Database connectivity

### Development Tools
- **Spring Boot DevTools** - Hot reload during development
- **IntelliJ IDEA** - IDE

## 🏗️ Architecture

### Layered Architecture

```
            ┌─────────────────────────────────────┐
            │   Security Layer (JWT Filter)       │
            │   - JwtAuthenticationFilter         │
            │   - JwtUtil (Token Management)      │
            │   - CustomUserDetailsService        │
            └────────────┬────────────────────────┘
                         │
            ┌────────────▼────────────────────────┐
            │     Controller Layer (REST API)     │
            │   - AuthController                  │
            │   - UserController                  │
            │   - GroupController                 │
            │   - ExpenseController               │
            │   - BalanceController               │
            │   - SettlementController            │
            └────────────┬────────────────────────┘
                         │
            ┌────────────▼────────────────────────┐
            │        Service Layer                │
            │   - AuthService                     │
            │   - UserService                     │
            │   - GroupService                    │
            │   - ExpenseService                  │
            │   - BalanceService                  │
            │   - SettlementService               │
            │   - SplitCalculator                 │
            │   - BalanceSimplifier               │
            └────────────┬────────────────────────┘
                         │
            ┌────────────▼────────────────────────┐
            │      Repository Layer (JPA)         │
            │   - UserRepository                  │
            │   - GroupRepository                 │
            │   - GroupMemberRepository           │
            │   - ExpenseRepository               │
            │   - ExpenseSplitRepository          │
            │   - SettlementRepository            │
            └────────────┬────────────────────────┘
                         │
            ┌────────────▼────────────────────────┐
            │        PostgreSQL Database          │
            └─────────────────────────────────────┘
```

### Design Patterns Used
- **Repository Pattern**: Data access abstraction
- **Service Layer Pattern**: Business logic separation
- **DTO Pattern**: Separate API models from domain entities (prevents circular references)
- **Builder Pattern**: Clean object construction (via Lombok)
- **Dependency Injection**: Loose coupling via Spring IoC
- **Strategy Pattern**: Different split calculation strategies
- **Interceptor Pattern**: JWT token validation on requests
- **Factory Pattern**: User details creation for authentication

## 🚀 Getting Started

### Prerequisites

- Java 17 or higher
- PostgreSQL 15 or higher
- Maven 3.6+
- An IDE (IntelliJ IDEA recommended)

### Installation

1. **Clone the repository**
```bash
git clone https://github.com/ArrushTandon/expense-sharing-application.git
cd expense-sharing-application
```

2. **Set up PostgreSQL database**
```sql
CREATE DATABASE expensesharing;
```

3. **Configure application**

Update `src/main/resources/application.yml`:
```yaml
spring:
  application:
    name: expense-sharing-app

  datasource:
    url: jdbc:postgresql://localhost:5432/expensesharing
    username: postgres
    password: your_password
    driver-class-name: org.postgresql.Driver

  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        format_sql: true

server:
  port: 8080

# JWT Configuration (Change secret in production!)
jwt:
  secret: mySecretKeyForJWTTokenGenerationThatIsAtLeast256BitsLongForHS256Algorithm
  expiration: 86400000  # 24 hours in milliseconds
```

4. **Build the project**
```bash
mvn clean install
```

5. **Run the application**
```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

## 📚 API Documentation

### Base URL
```
http://localhost:8080/api
```

### Authentication

All endpoints except `/api/auth/*` require JWT token in the Authorization header:
```
Authorization: Bearer <your-jwt-token>
```

### Endpoints

#### Authentication (Public)
```
POST   /auth/register          - Register a new user
POST   /auth/login             - Login and get JWT token
GET    /auth/test              - Test endpoint (public)
```

#### User Management (Protected)
```
GET    /users                  - Get all users (ADMIN only)
GET    /users/{id}             - Get specific user (self or ADMIN only)
POST   /users                  - Create a new user
```

#### Group Management (Protected)
```
POST   /groups                 - Create a new group
GET    /groups                 - Get user's groups (member groups only)
GET    /groups/{id}            - Get group details (members only)
POST   /groups/{id}/members    - Add member to group (members only)
DELETE /groups/{id}/members/{userId} - Remove member (members only)
```

#### Expense Management (Protected)
```
POST   /groups/{id}/expenses       - Add expense to group (members only)
GET    /groups/{id}/expenses       - List group expenses (members only)
GET    /groups/{id}/expenses/{id}  - Get expense details (members only)
```

#### Balance Tracking (Protected)
```
GET    /users/{id}/balances        - Get user balances
GET    /groups/{id}/balances       - Get simplified group balances (members only)
```

#### Settlements (Protected)
```
POST   /settlements                - Record a settlement (members only)
```

### Example Requests

**Register a new user:**
```bash
POST /api/auth/register
Content-Type: application/json

{
  "name": "John Doe",
  "email": "john@example.com",
  "password": "password123",
  "phone": "+1234567890"
}

Response:
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "email": "john@example.com",
  "name": "John Doe",
  "message": "User registered successfully"
}
```

**Login:**
```bash
POST /api/auth/login
Content-Type: application/json

{
  "email": "john@example.com",
  "password": "password123"
}

Response:
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "email": "john@example.com",
  "name": "John Doe",
  "message": "Login successful"
}
```

**Create a group:**
```bash
POST /api/groups
Authorization: Bearer <your-token>
Content-Type: application/json

{
  "name": "Weekend Trip",
  "description": "Trip to the mountains",
  "createdBy": "user-uuid",
  "memberIds": ["user2-uuid", "user3-uuid"]
}
```

**Create an expense with equal split:**
```bash
POST /api/groups/{groupId}/expenses
Authorization: Bearer <your-token>
Content-Type: application/json

{
  "description": "Dinner at restaurant",
  "totalAmount": 300.00,
  "paidBy": "user-uuid",
  "splitType": "EQUAL",
  "splits": [
    {"userId": "user1-uuid"},
    {"userId": "user2-uuid"},
    {"userId": "user3-uuid"}
  ]
}
```

**Record a settlement:**
```bash
POST /api/settlements
Authorization: Bearer <your-token>
Content-Type: application/json

{
  "groupId": "group-uuid",
  "fromUser": "debtor-uuid",
  "toUser": "creditor-uuid",
  "amount": 100.00,
  "note": "Paid via PayPal"
}
```

## 🗄️ Database Schema

### Core Tables

**users**
- id (UUID, Primary Key)
- name (VARCHAR, NOT NULL)
- email (VARCHAR, UNIQUE, NOT NULL)
- password (VARCHAR, NOT NULL) - BCrypt hashed
- phone (VARCHAR)
- role (ENUM: USER, ADMIN) - Default: USER
- created_at (TIMESTAMP)
- updated_at (TIMESTAMP)

**groups**
- id (UUID, Primary Key)
- name (VARCHAR, NOT NULL)
- description (TEXT)
- created_by (UUID, Foreign Key → users.id)
- created_at (TIMESTAMP)
- updated_at (TIMESTAMP)

**group_members**
- id (UUID, Primary Key)
- group_id (UUID, Foreign Key → groups.id)
- user_id (UUID, Foreign Key → users.id)
- joined_at (TIMESTAMP)
- is_active (BOOLEAN, Default: true)
- UNIQUE (group_id, user_id)

**expenses**
- id (UUID, Primary Key)
- group_id (UUID, Foreign Key → groups.id)
- description (VARCHAR, NOT NULL)
- total_amount (NUMERIC(10,2), NOT NULL)
- paid_by (UUID, Foreign Key → users.id)
- split_type (ENUM: EQUAL, EXACT, PERCENTAGE)
- created_at (TIMESTAMP)
- updated_at (TIMESTAMP)

**expense_splits**
- id (UUID, Primary Key)
- expense_id (UUID, Foreign Key → expenses.id)
- user_id (UUID, Foreign Key → users.id)
- amount_owed (NUMERIC(10,2), NOT NULL)
- percentage (NUMERIC(5,2))
- paid (BOOLEAN, Default: false)
- created_at (TIMESTAMP)

**settlements**
- id (UUID, Primary Key)
- group_id (UUID, Foreign Key → groups.id)
- from_user (UUID, Foreign Key → users.id)
- to_user (UUID, Foreign Key → users.id)
- amount (NUMERIC(10,2), NOT NULL)
- settled_at (TIMESTAMP)
- note (TEXT)

### Entity Relationships
```
User ──┬──< GroupMember >──┬── Group
       │                   │
       ├──< ExpenseSplit <─┴─ Expense
       │                   │
       └──< Settlement >───┴── Group
```

## 🧮 Key Algorithms

### 1. Balance Simplification Algorithm

**Problem**: When multiple people owe each other money, we want to minimize the number of transactions.

**Solution**: Greedy algorithm that matches largest creditors with largest debtors.

**Algorithm**:
```
1. Calculate net balance for each user (total owed - total owing)
2. Separate into creditors (positive balance) and debtors (negative balance)
3. Sort both lists by amount (descending)
4. Match largest creditor with largest debtor
5. Settle minimum of both amounts
6. Update balances and repeat until all settled
```

**Complexity**: O(n log n) where n is the number of users

**Example**:
```
Initial State:
- Alice owes Bob $100
- Alice owes Charlie $50
- Bob owes Charlie $30

Net Balances:
- Alice: -$150 (owes)
- Bob: +$70 (is owed)
- Charlie: +$80 (is owed)

After Simplification:
- Alice pays Charlie $80
- Alice pays Bob $70
(Reduced from 3 to 2 transactions)
```

### 2. Split Calculation

#### Equal Split
```java
amount_per_person = total_amount / number_of_participants
// Remainder goes to last participant to handle rounding
// Example: $100 / 3 = $33.33, $33.33, $33.34
```

#### Exact Split
```java
// Validates: sum(individual_amounts) == total_amount
// Example: $100 = $40 + $35 + $25
```

#### Percentage Split
```java
individual_amount = (percentage / 100) * total_amount
// Remainder goes to last participant to handle rounding
// Validates: sum(percentages) == 100
// Example: $100 = 40% + 35% + 25% = $40 + $35 + $25
```

### 3. Balance Calculation

**For User Balances:**
```
For each expense where user is involved:
  If user is payer:
    For each other split in expense:
      If not paid: user is owed that amount
  Else if user is participant and not paid:
    User owes the payer their split amount

Apply settlements:
  For each settlement FROM user: reduce what user owes
  For each settlement TO user: reduce what user is owed
```

**For Group Balances:**
```
For each expense in group:
  Payer gets credited: (total_amount - their_share)
  Each participant gets debited: their_share

Apply settlements to adjust balances
Use simplification algorithm to minimize transactions
```

### 4. Rounding Handling

To avoid floating-point errors and rounding discrepancies:
- Uses `BigDecimal` with 2 decimal precision for all monetary calculations
- `RoundingMode.HALF_UP` for standard rounding
- Allocates remainder to last participant to ensure exact total
- Ensures sum of splits always matches total amount exactly

## 🔐 Security Implementation

### Authentication Flow

1. **Registration**:
    - User submits credentials
    - Password validated (minimum 6 characters)
    - Password hashed with BCrypt
    - User stored in database with USER role
    - JWT token generated and returned

2. **Login**:
    - User submits credentials
    - Email and password validated
    - Password compared with BCrypt
    - JWT token generated with 24-hour expiration
    - Token returned to client

3. **Protected Requests**:
    - Client sends token in Authorization header
    - `JwtAuthenticationFilter` intercepts request
    - Token extracted and validated
    - User loaded from database
    - Security context updated with user details
    - Request proceeds to controller

### Authorization Checks

**User Privacy:**
```java
// Only allow users to see their own profile
if (!requestingUser.getId().equals(userId) && 
    requestingUser.getRole() != Role.ADMIN) {
    throw new UnauthorizedException();
}
```

**Group Member Access:**
```java
// Only group members can access group data
if (!isMemberOfGroup(groupId, requestingUser.getId()) && 
    requestingUser.getRole() != Role.ADMIN) {
    throw new UnauthorizedException();
}
```

### Input Validation

All inputs are validated at multiple levels:
1. **DTO Validation**: `@Valid`, `@NotNull`, `@NotBlank`, `@Email`, `@DecimalMin`
2. **Service Validation**: Business logic checks
3. **Security Validation**: Access control checks

### Error Handling

Centralized exception handling with security-conscious responses:
- Never expose internal errors to clients
- Log security violations for monitoring
- Return appropriate HTTP status codes
- Provide user-friendly error messages without revealing system details

## 🧪 Testing

### Manual Testing with Postman

1. **Import the collection** (Postman collection available)
2. **Set up environment variables**:
    - `baseUrl`: http://localhost:8080
    - Auto-populated: `aliceToken`, `bobToken`, `charlieToken`, etc.
3. **Run the test sequence**:
    - Register users
    - Login users (tokens auto-saved)
    - Create groups
    - Add expenses with different split types
    - Check balances
    - Record settlements
    - Verify balance updates

### Test Scenarios

**Scenario 1: Authentication & Authorization**
- Register new users
- Login with correct/incorrect credentials
- Access protected endpoints with/without token
- Try accessing other user's data (should fail)
- Try accessing groups you're not member of (should fail)

**Scenario 2: Simple Equal Split**
- 3 users, 1 group, 1 expense of $300
- Each user should owe $100
- Verify balances are correct

**Scenario 3: Multiple Expenses with Settlements**
- Multiple expenses with different payers
- Record settlements
- Verify balance simplification
- Check balances update correctly after settlements

**Scenario 4: Exact Amount Split**
- Custom amounts for each participant
- Verify sum matches total (validation)
- Try invalid sum (should fail)

**Scenario 5: Percentage Split**
- 40%, 35%, 25% split
- Verify correct amounts and rounding
- Try percentages not summing to 100 (should fail)

**Scenario 6: Privacy Tests**
- User A tries to view User B's profile (should fail)
- User A tries to access Group X they're not member of (should fail)
- Admin can view all users and groups (should succeed)

### Security Testing

**Test for common vulnerabilities:**

1. **JWT Token Manipulation**:
    - Modify token payload
    - Use expired token
    - Use invalid signature
    - All should be rejected with 401/403

2. **Authorization Bypass**:
    - Access other user's data
    - Modify other group's data
    - All should be rejected with 403

3. **Input Validation**:
    - Negative amounts
    - Invalid email formats
    - Missing required fields
    - All should return 400 Bad Request

4. **SQL Injection** (Protected by JPA):
    - Try SQL injection in input fields
    - Should be prevented by parameterized queries

### Database Verification

Check data in pgAdmin or psql:
```sql
-- View users (passwords should be hashed)
SELECT id, name, email, role, created_at FROM users;

-- View groups and members
SELECT g.name, u.name as member_name, gm.is_active
FROM groups g
JOIN group_members gm ON g.id = gm.group_id
JOIN users u ON gm.user_id = u.id;

-- View expenses with splits
SELECT e.description, e.total_amount, u.name as paid_by,
       es.amount_owed, u2.name as owed_by
FROM expenses e
JOIN users u ON e.paid_by = u.id
JOIN expense_splits es ON e.id = es.expense_id
JOIN users u2 ON es.user_id = u2.id;

-- View settlements
SELECT s.amount, u1.name as from_user, u2.name as to_user, s.settled_at
FROM settlements s
JOIN users u1 ON s.from_user = u1.id
JOIN users u2 ON s.to_user = u2.id;
```

## ⚠️ Known Limitations

### Current Version Limitations

1. **No Refresh Token Implementation**
    - JWT tokens expire after 24 hours
    - Users must login again after expiration

2. **No Rate Limiting**
    - No protection against brute force attacks, vulnerable to DoS Attacks

3. **No Currency Support**
    - Single currency only (amount is just a number)

4. **No Email Verification**
    - Users can register with any email

5. **No Expense Editing/Deletion**
    - Once created, expenses cannot be modified

6. **No Notifications**

7. **Basic Password Policy**
    - Only minimum 6 characters required

## 📝 License

This project is a personal development project and new feedback is welcomed.

## 👤 Author

**Arrush Tandon**
- GitHub: [@ArrushTandon](https://github.com/ArrushTandon)
- LinkedIn: [Arrush Tandon](https://www.linkedin.com/in/arrush-tandon/)

## 🙏 Acknowledgments

- Inspired by Splitwise
- Uses Spring Boot and Spring Security best practices
- Implements clean architecture principles
- Security-focused development approach for cybersecurity learning

## 🛡️ Security Notice

This application implements several security best practices:
- JWT-based authentication
- BCrypt password hashing
- Role-based access control
- Input validation and sanitization
- Protection against common vulnerabilities (SQL injection, XSS via DTOs)
- Privacy enforcement (users can only see their data)

---

**Note**: This project demonstrates security-conscious development practices suitable for educational purposes and as a foundation for production systems.

## 📊 Project Statistics

- **Lines of Code**: ~3,000+
- **API Endpoints**: 20+
- **Security Features**: 15+
- **Database Tables**: 6
- **Split Types**: 3
