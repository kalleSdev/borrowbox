# BorrowBox

A peer-to-peer lending system. Members list things they own, borrow each other's for a run of days, and pay for them in credits. Time is simulated: nothing happens until someone advances the day.

[![CI](https://github.com/kalleSdev/borrowbox/actions/workflows/ci.yml/badge.svg)](https://github.com/kalleSdev/borrowbox/actions/workflows/ci.yml)

**Java 21 · Spring Boot 3 · Spring Data JPA · React 19 · TypeScript · Tailwind · 157 tests**

<!-- screenshots go here -->

---

## What it does

Everyone starts with a balance of credits. Listing an item earns you 100, which is what keeps credits flowing into the pool. Borrowing costs the owner's daily rate for every day of the loan, both ends included, and that money goes from the borrower to the lender.

The calendar is the interesting part. There are no real dates anywhere — loans are booked against day numbers, and the whole system sits still until you press **Advance the day**. That is what starts and ends loans, and each one shows up in the activity feed as it happens.

The rules the system will not let you break:

| Rule | What happens |
|---|---|
| A loan cannot start in the past | `422` with the reason |
| A loan cannot end before it starts | `422` |
| An item cannot be booked twice over the same days | `422` — the calendar is inclusive, so days 2–6 means busy on day 6 and free on day 7 |
| A borrower cannot spend credits they do not have | `422`, and nothing is charged |
| A member cannot borrow their own item | `422` |
| An email or mobile belongs to exactly one member | `409` |
| An item with loans against it cannot be deleted | `409` — removing it would take the loan history with it |

---

## Running it

Two processes. You need JDK 21 and Node 20+.

```bash
cd backend && mvn spring-boot:run
```

```bash
cd frontend && npm install && npm run dev
```

The app is on **http://localhost:5173** and the API on **http://localhost:8080**. The dev server proxies `/api` through, so there is no CORS to configure locally. Three members and two items are seeded at startup so there is something to click.

Browsable API docs, where you can fire real requests without installing anything: **http://localhost:8080/swagger-ui.html**

```bash
cd backend && mvn test
```

---

## How it is put together

```
backend/
  api/         REST controllers, request/response DTOs, the exception handler
  model/       the domain — entities and the rules they enforce
  repository/  Spring Data interfaces, one per aggregate
  service/     the use cases, and where transactions begin and end
  config/      bean wiring and the demo-data seeder
frontend/
  api/         typed client, mirrored response types, TanStack Query hooks
  components/  shared UI and the dialogs
  pages/       dashboard, catalogue, item, members, loans
```

Nothing in `model/` imports Spring. The entities carry JPA mapping annotations, but the rules live in plain Java that a unit test can construct directly and run in milliseconds — `Contract.create` deciding whether a loan is allowed needs no container and no database. The service layer is where Spring appears, and it is thin on purpose: it moves credits, saves, and publishes, and defers every judgement to the model.

The model also knows nothing about HTTP. It throws `LendingNotAllowedException` when a rule is broken; `ApiExceptionHandler` is what decides that this means `422`. Every error comes back as an [RFC 7807](https://datatracker.ietf.org/doc/html/rfc7807) problem detail with a message already written for a person, so the frontend never has to invent its own wording per status code:

```json
{
  "title": "Not allowed",
  "status": 422,
  "detail": "The borrower has 100 credits but this loan costs 250.",
  "instance": "/api/contracts"
}
```

### Persistence

H2 on disk by default, so cloning the repo and running it installs nothing. The simulated day is a row like everything else, which means the clock, the loans and the activity feed all survive a restart together. Demo data is seeded only into an empty database, so a restart never duplicates it.

Pointing at Postgres instead is a profile, not a code change:

```bash
docker compose up -d
```

```bash
cd backend && mvn spring-boot:run -Dspring-boot.run.profiles=postgres
```

CI runs the whole backend suite a second time against a real Postgres service container, so that profile is checked on every push rather than being a claim nobody has tried.

`open-in-view` is off. A request should not be holding a database session open while it renders a response, so what the API needs is fetched inside the service instead.

### The domain

```mermaid
classDiagram
    class MemberService {
        +register(name, email, mobile) Member
        +requireMemberById(id) Member
        +requireItemById(id) Item
        +getAllItems() List~Item~
    }
    class Member {
        -int credits
        +createItem(name, description, category, costPerDay) Item
        +addCredits(int)
        +deductCredits(int)
    }
    class Item {
        -Member owner
        +isAvailable(start, end) bool
        +changeItemInfo(name, description, category, costPerDay)
    }
    class Contract {
        <<immutable>>
        +create(item, borrower, startDay, endDay, today) Contract
        +getCost() int
        +isActiveOn(day) bool
    }
    class LendingService {
        +lend(item, borrower, start, end) Contract
    }
    class Simulation {
        +advanceDay() DayAdvanced
    }
    class Time {
        +advanceDay()
        +getCurrentDay() int
    }
    class EventPublisher {
        +subscribe(Observer)
        +publish(DomainEvent)
    }
    class Observer {
        <<interface>>
        +onEvent(DomainEvent)
    }

    Observer <|.. EventLog
    Observer <|.. LoggingObserver
    EventPublisher o-- Observer
    MemberService "1" o-- "*" Member
    Member "1" o-- "*" Item : owns
    Item "1" o-- "*" Contract : booked by
    Contract --> Member : lender, borrower
    LendingService ..> Contract : creates
    LendingService ..> EventPublisher : publishes to
    Simulation ..> Time : advances
    Simulation ..> EventPublisher : publishes to
```

### Booking a loan

```mermaid
sequenceDiagram
    participant UI as React
    participant C as ContractController
    participant L as LendingService
    participant Ct as Contract
    participant P as EventPublisher

    UI->>C: POST /api/contracts
    C->>L: lend(item, borrower, 2, 4)
    L->>Ct: create(...)
    Note over Ct: dates, availability,<br/>self-borrowing
    alt a rule is broken
        Ct-->>L: throws
        L-->>UI: 422 + the reason
    else allowed
        Ct-->>L: contract
        L->>L: borrower pays lender, once
        L->>P: LOAN_AGREED
        P-->>UI: appears in the activity feed
    end
```

---

## The three patterns, and where you can see them

This started as a design-patterns assignment. Rather than leave the patterns as something you have to read the code to find, each one drives a feature you can click.

**Strategy — the catalogue filters.** `SearchStrategy` has two implementations. `GET /api/items?name=drill` sets one on the context, `?maxPrice=30` sets the other, and passing both runs the results through each in turn. The search bar is the pattern.

**Observer — the activity feed.** `EventPublisher` fans typed events out to whoever subscribed. `EventLog` keeps them, which is what `GET /api/events` reads back; `LoggingObserver` writes them to the server log. Neither is known to the code raising the event.

**Simulated clock.** `Time` is a day counter stored as a single row. `Simulation.advanceDay()` moves it and announces every loan starting or ending, which is why advancing the day returns the events it caused in the same response.

---

## API

| | | |
|---|---|---|
| `GET` | `/api/members` | Everyone |
| `POST` | `/api/members` | Sign up — `409` if the email or mobile is taken |
| `GET` `PUT` `DELETE` | `/api/members/{id}` | One member |
| `GET` | `/api/items?name=&maxPrice=` | The catalogue, filtered by the search strategies |
| `POST` | `/api/items` | List an item, paying the owner the bonus |
| `GET` `PUT` `DELETE` | `/api/items/{id}` | One item, with its booking history |
| `GET` | `/api/contracts?memberId=` | Loans, optionally for one member |
| `POST` | `/api/contracts` | Book an item out |
| `GET` | `/api/clock` | What day it is |
| `POST` | `/api/clock/advance` | Move to the next day, and get back what it brought |
| `GET` | `/api/events?limit=` | Activity feed, newest first |

---

## Tests

118 on the backend and 39 on the frontend.

The backend tests come in three layers. The domain tests construct their objects directly and need no Spring context. The service tests run against a real database and roll back after each one, so they are independent without a context restart between them. The API tests drive real requests through MockMvc and assert on the JSON and the status codes.

The frontend tests run against a mock server rather than stubbed hooks: MSW intercepts at the network layer, so they exercise the real client and the real error handling, and the only thing replaced is what is on the other end of the wire.

```bash
cd backend  && mvn test    # 118, plus the same suite again on Postgres in CI
cd frontend && npm test    # 39
cd frontend && npm run build   # typecheck and bundle
```

---

## Where this came from

The first commit is a university OOP assignment exactly as it was handed in: a console app, no tests, no build file. Everything after it is the rebuild, one concern per commit, so the history is readable as a sequence of decisions rather than a single dump.

Writing the tests before touching anything was worth it. They pinned down what already worked and made the following genuine bugs safe to fix:

- **The observer never fired.** A contract raised its event inside its constructor, but the only caller attached the listener on the line after. The list was always empty at the point it was read.
- **Every loan was charged three times, to the wrong person.** The constructor deducted from the lender, then `addContract` deducted again for each of the two members the contract was filed against. The borrower should have been paying the lender.
- **Items could be double-booked.** `Item` and `Contract` each had their own overlap check and disagreed about whether the last day counted. The one guarding new bookings was the lenient one.
- **The uniqueness check was bypassed.** `MemberList` kept sets of taken emails, but only one of the two creation paths updated them — and the menu used the other one.
- **Items had no owner.** `Item.ownerId` was declared and never written to, so every item printed "Owner not found".

## What is next

- A deployed demo
- Screenshots in this README
