## Phase 1 — SQL foundations

- What a table really is
- Rows and columns
- `SELECT`
- `WHERE`
- `ORDER BY`
- simple filtering logic
- aliases

## Phase 2 — Combining data

- `JOIN`
- one-to-many and many-to-many relationships
- `NULL`
- inner vs left join

## Phase 3 — Summarizing data

- `GROUP BY`
- aggregate functions: `COUNT`, `SUM`, `AVG`, `MIN`, `MAX`
- `HAVING`

## Phase 4 — Database design

- entities and attributes
- tables and relationships
- primary keys and foreign keys
- normalization
- schema design tradeoffs

## Phase 5 — Internal database knowledge

- indexes
- query plans
- performance basics
- transactions
- ACID
- concurrency problems

---

# Next step

## Phase 1 — More Interview Pattern Fluency

Focus:

- streak problems
- gaps/islands
- nested ranking
- advanced analytics
- more “aggregate then join back”

| Pattern                   |    Level     |
| ------------------------- | :----------: |
| GROUP BY / HAVING         | 🟢 Excellent |
| Conditional Aggregation   | 🟢 Excellent |
| Aggregate → Join Back     | 🟢 Excellent |
| ROW_NUMBER()              | 🟢 Excellent |
| RANK / DENSE_RANK         | 🟢 Excellent |
| LAG / LEAD                | 🟢 Excellent |
| Streaks / Gaps & Islands  |  🟢 Strong   |
| Latest Row per Entity     | 🟢 Excellent |
| Self Join                 |   🟢 Good    |
| Mixed Pattern Recognition | 🟢 Excellent |

## Phase 2 — Real-world Reporting SQL

| Module   | Topic                   | Core question                                              | Status      |
| -------- | ----------------------- | ---------------------------------------------------------- | ----------- |
| **2.1**  | Reporting Foundations   | What does one output row mean?                             | ✅ Complete |
| **2.2**  | Multi-Table Reporting   | Where does each piece of information live?                 | 🟡 Current  |
| **2.3**  | Customer Lifecycle      | Is this customer new, returning, or reactivated?           | ⏳          |
| **2.4**  | Retention               | Did customers come back?                                   | ⏳          |
| **2.5**  | Cohort Analysis         | What happened to customers who started together?           | ⏳          |
| **2.6**  | Funnel Analysis         | How far do users progress through a process?               | ⏳          |
| **2.7**  | Conversion Metrics      | What proportion completed the desired action?              | ⏳          |
| **2.8**  | SaaS / Business Metrics | How do real products measure growth and behaviour?         | ⏳          |
| **2.9**  | Integrated Reporting    | Can we design a report from an ambiguous business request? | ⏳          |
| **2.10** | Phase Review            | Can we derive unfamiliar reports independently?            | ⏳          |

## Phase 3 — Database Internals (later)

- MVCC
- locking internals
- query planner deeper
- indexing internals
- storage engines
