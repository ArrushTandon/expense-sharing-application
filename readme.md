# 💰 Expense Sharing Application

A robust backend system for managing shared expenses among groups, similar to Splitwise. Built with Spring Boot, this application allows users to create groups, track expenses, and automatically calculate simplified balances to minimize the number of transactions needed for settlements.

## 📋 Table of Contents

- [About the Project](https://github.com/ArrushTandon/expense-sharing-application?tab=readme-ov-file#-about-the-project)
- [Features](#features)
- [Tech Stack](#tech-stack)
- [Architecture](#architecture)
- [Getting Started](#getting-started)
- [API Documentation](#api-documentation)
- [Database Schema](#database-schema)
- [Key Algorithms](#key-algorithms)
- [Testing](#testing)
- [Known Limitations](#known-limitations)
- [Future Enhancements](#future-enhancements)

## 🎯 About the Project

This is a simplified expense-sharing application backend that demonstrates clean architecture principles, proper separation of concerns, and efficient algorithm implementation for financial calculations. The system handles complex scenarios like multiple split types, balance calculations, and transaction minimization.

### Problem Statement

When friends or colleagues share expenses, tracking who owes whom can become complicated. This application solves that problem by:
- Tracking all shared expenses
- Supporting multiple ways to split bills (equal, exact amounts, percentages)
- Automatically calculating balances
- Minimizing the number of transactions needed to settle debts

## ✨ Features

### Core Functionality
- **User Management**: Create and manage user profiles
- **Group Management**: Create groups and add members
- **Expense Tracking**: Record expenses with multiple split types
- **Balance Calculation**: Automatic calculation of who owes whom
- **Balance Simplification**: Minimize transactions using graph-based algorithms
- **Settlement Recording**: Track when debts are paid

### Split Types Supported
1. **Equal Split**: Divide expense equally among all participants
2. **Exact Amount Split**: Specify exact amount for each participant
3. **Percentage Split**: Split based on custom percentages

### Smart Features
- Automatic balance calculation across all expenses
- Transaction minimization (reduces N transactions to optimal number)
- Handles complex multi-user, multi-expense scenarios
- Proper rounding handling to avoid discrepancies

## 🛠️ Tech Stack

### Backend Framework
- **Spring Boot 3.5.9** - Application framework
- **Java 17** - Programming language
- **Maven** - Dependency management and build tool

### Database
- **PostgreSQL 16** - Primary relational database
- **Hibernate/JPA** - ORM for database interactions
- **HikariCP** - High-performance connection pooling

### Key Dependencies
- **Spring Data JPA** - Data access layer
- **Spring Web** - RESTful API development
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
            │     Controller Layer (REST API)     │
            │   - UserController                  │
            │   - GroupController                 │
            │   - ExpenseController               │
            │   - BalanceController               │
            │   - SettlementController            │
            └────────────┬────────────────────────┘
                         │
            ┌────────────▼────────────────────────┐
            │        Service Layer                │
            │   - UserService                     │
            │   - GroupService                    │
            │   - ExpenseService                  │
            │   - BalanceService                  │
            │   - SplitCalculator                 │
            │   - BalanceSimplifier               │
            └────────────┬────────────────────────┘
                         │
            ┌────────────▼────────────────────────┐
            │      Repository Layer (JPA)         │
            │   - UserRepository                  │
            │   - GroupRepository                 │
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
- **DTO Pattern**: Separate API models from domain entities
- **Builder Pattern**: Clean object construction (via Lombok)
- **Dependency Injection**: Loose coupling via Spring IoC
- **Strategy Pattern**: Different split calculation strategies

## 🚀 Getting Started

### Prerequisites

- Java 17 or higher
- PostgreSQL 15 or higher
- Maven 3.6+
- An IDE (IntelliJ IDEA recommended)

### Installation

1. **Clone the repository**
```bash
git clone https://github.com/yourusername/expense-sharing-app.git
cd expense-sharing-app
```

2. **Set up PostgreSQL database**
```sql
CREATE DATABASE expensesharing;
```

3. **Configure application**

Update `src/main/resources/application.yml`:
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/expensesharing
    username: postgres
    password: your_password
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

### Endpoints

#### User Management
```
POST   /users              - Create a new user
GET    /users              - Get all users details
GET    /users/{id}         - Get specific user details
```

#### Group Management
```
POST   /groups             - Create a new group
GET    /groups             - Get all groups details
GET    /groups/{id}        - Get specific group details
```

#### Expense Management
```
POST   /groups/{id}/expenses       - Add expense to group
GET    /groups/{id}/expenses       - List group expenses
GET    /groups/{id}/expenses/{id}  - Get expense details
```

#### Balance Tracking
```
GET    /users/{id}/balances        - Get user balances
GET    /groups/{id}/balances       - Get simplified group balances
```

#### Settlements
```
POST   /settlements                - Record a settlement
```

### Example Request

**Create an expense with equal split:**
```bash
POST /api/groups/{groupId}/expenses
Content-Type: application/json

{
  "description": "Dinner at restaurant",
  "totalAmount": 3000,
  "paidBy": "user-uuid",
  "splitType": "EQUAL",
  "splits": [
    {"userId": "user1-uuid"},
    {"userId": "user2-uuid"},
    {"userId": "user3-uuid"}
  ]
}
```

## 🗄️ Database Schema

### Core Tables

**users**
- Stores user information
- Unique constraint on email

**groups**
- Stores group information
- Links to creator (user)

**group_members**
- Many-to-many relationship between users and groups
- Tracks active/inactive status
- Unique constraint on (group_id, user_id)

**expenses**
- Stores expense details
- Links to group and payer
- Supports EQUAL, EXACT, PERCENTAGE split types

**expense_splits**
- Stores individual split details
- Links to expense and user
- Tracks payment status

**settlements**
- Records debt settlements
- Links from_user, to_user, and group

### Entity Relationships
```
User ──┬──< GroupMember >──┬── Group
       │                   │
       └──< ExpenseSplit <─┴─ Expense
       │
       └──< Settlement >────── Group
```

## 🧮 Key Algorithms

### 1. Balance Simplification Algorithm

**Problem**: When multiple people owe each other money, we want to minimize the number of transactions.

**Solution**: Greedy algorithm that matches largest creditors with largest debtors.

**Complexity**: O(n log n) where n is the number of users

**Example**:
```
Initial State:
- Alice owes Bob $100
- Alice owes Charlie $50
- Bob owes Charlie $30

After Simplification:
- Alice pays Charlie $80
- Bob pays Charlie $70
(Reduced from 3 to 2 transactions)
```

### 2. Split Calculation

#### Equal Split
```java
amount_per_person = total_amount / number_of_participants
// Remainder goes to last participant to handle rounding
```

#### Exact Split
```java
// Validates: sum(individual_amounts) == total_amount
```

#### Percentage Split
```java
individual_amount = (percentage / 100) * total_amount
// Remainder goes to last participant to handle rounding
```

### 3. Rounding Handling

To avoid floating-point errors and rounding discrepancies:
- Uses `BigDecimal` for all monetary calculations
- Allocates remainder to last participant
- Ensures total always matches exact amount

## 🧪 Testing

### Manual Testing with Postman

1. **Import the collection** (if provided)
2. **Set up environment variables**:
    - `base_url`: http://localhost:8080
3. **Run the test sequence**:
    - Create users
    - Create group
    - Add expenses
    - Check balances
    - Record settlements

### Test Scenarios

**Scenario 1: Simple Equal Split**
- 3 users, 1 expense of $300
- Each owes $100

**Scenario 2: Multiple Expenses**
- 3 users, multiple expenses with different payers
- Verify balance simplification

**Scenario 3: Exact Amount Split**
- Custom amounts for each participant
- Verify sum matches total

**Scenario 4: Percentage Split**
- 40%, 30%, 30% split
- Verify correct amounts and rounding

### Database Verification

Check data in pgAdmin:
```sql
SELECT * FROM users;
SELECT * FROM groups;
SELECT * FROM expenses;
SELECT * FROM expense_splits;
```

## ⚠️ Known Limitations

### Current Version Limitations

1. **No Authentication/Authorization**
    - No user login system
    - No JWT tokens
    - Any user can access any group
    - **Impact**: Not production-ready without adding security

2. **No Currency Support**
    - Single currency only (assumed INR)
    - No multi-currency conversion
    - No exchange rate handling
    - **Impact**: Cannot be used internationally

3. **Limited Error Messages**
    - Generic error responses
    - Could be more descriptive for debugging
    - **Impact**: Harder to troubleshoot API issues

4. **No Caching Implementation**
    - Balance calculations happen on every request
    - No Redis integration (despite dependency)
    - **Impact**: May have performance issues with large datasets

5. **No Pagination on Expense Lists**
    - All expenses returned at once
    - **Impact**: Performance issues with hundreds of expenses

6. **No Expense Editing/Deletion**
    - Once created, expenses cannot be modified
    - No soft delete implementation
    - **Impact**: Users cannot fix mistakes

7. **No Notifications**
    - No email/SMS alerts when added to expenses
    - Users must manually check balances
    - **Impact**: Poor user experience

8. **No Receipt/Image Upload**
    - Cannot attach proof of payment
    - **Impact**: Disputes harder to resolve

9. **No Recurring Expenses**
    - Cannot set up automatic monthly expenses (rent, subscriptions)
    - **Impact**: Users must manually add repetitive expenses

10. **No Data Export**
    - Cannot download expense reports
    - No CSV/PDF generation
    - **Impact**: Cannot use for tax/accounting purposes

11. **Limited Validation**
    - No check if payer is a group member
    - No validation if user exists when creating group
    - **Impact**: Can create inconsistent data

12. **No Audit Trail**
    - No logging of who changed what and when
    - Cannot track settlement history details
    - **Impact**: Accountability issues

### Technical Debt

1. **Test Coverage**: No unit tests or integration tests
2. **API Documentation**: No Swagger/OpenAPI integration (dependency exists but not configured)
3. **Monitoring**: Basic actuator only, no metrics dashboard
4. **Error Handling**: Could be more granular and informative
5. **Code Comments**: Minimal documentation in code

## 🚧 Future Enhancements

### High Priority
- [ ] Add Spring Security with JWT authentication
- [ ] Implement user registration and login
- [ ] Add expense editing and soft deletion
- [ ] Implement proper caching (Redis)
- [ ] Add comprehensive unit and integration tests

### Medium Priority
- [ ] Multi-currency support with exchange rates
- [ ] Email/SMS notifications
- [ ] Receipt upload and storage (AWS S3)
- [ ] Expense categories and tags
- [ ] Advanced filtering and search

### Low Priority
- [ ] Recurring expenses
- [ ] Expense splitting rules (who pays for what)
- [ ] Data export (CSV, PDF)
- [ ] Analytics dashboard
- [ ] Mobile app integration

## 📝 License

This project is part of an educational assignment and is open for learning purposes.

## 👤 Author

**Your Name**
- GitHub: [ArrushTandon](https://github.com/ArrushTandon)
- Email: [arrush6674@gmail.com](arrush6674@gmail.com)

## 🙏 Acknowledgments

- Inspired by Splitwise
- Built as a backend engineering design assignment
- Uses Spring Boot best practices and clean architecture principles

---

**Note**: This is a learning project and not intended for production use without addressing the known limitations, especially security features.