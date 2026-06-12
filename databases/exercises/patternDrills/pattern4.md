# Pattern 4 - Consecutive Rows / Streaks

## Drill 1 - gaps and islands pattern

**Dataset**

logins

| user_id | login_date |
| ------- | ---------- |
| 1       | 2025-01-01 |
| 1       | 2025-01-02 |
| 1       | 2025-01-03 |
| 1       | 2025-01-05 |
| 1       | 2025-01-06 |

Problem

- Find each consecutive login streak.

A streak means login dates that continue day by day without a gap.

Expected output:

```
| user_id | streak_start | streak_end | streak_length |
| ------- | ------------ | ---------- | ------------: |
| 1       | 2025-01-01   | 2025-01-03 |             3 |
| 1       | 2025-01-05   | 2025-01-06 |             2 |

```

**Step 1: Get previous login date and Calculate gap**

Return:

| user_id | login_date | previous_login | gap_days |

Where:

- previous_login = previous login date
- gap_days = difference between current and prev

```
WITH user_login AS (
  SELECT user_id,
         login_date,
         LAG(login_date) OVER (
           PARTITION BY user_id
           ORDER BY login_date ASC
         ) AS previous_login
  FROM logins
)
SELECT user_id,
       login_date,
       previous_login,
       login_date - previous_login AS gap_days
FROM user_login;
```

**Step 2: Mark new streak starts**

Rule:

```
new streak starts when previous_login IS NULL OR gap_days > 1
```

Return:

| user_id | login_date | previous_login | gap_days | is_new_streak |

- where is_new_streak is 1 for a new streak and 0 otherwise.

```
WITH user_login AS (
  SELECT user_id,
         login_date,
         LAG(login_date) OVER (
           PARTITION BY user_id
           ORDER BY login_date ASC
         ) AS previous_login
  FROM logins
),
user_login_logs AS (
    SELECT user_id,
        login_date,
        previous_login,
        login_date - previous_login AS gap_days
    FROM user_login
)
SELECT
    user_id,
    login_date,
    previous_login,
    gap_days,
    CASE
        WHEN gap_days > 1 OR previous_login IS NULL
        THEN 1
        ELSE 0
    END AS is_new_streak
FROM user_login_logs
GROUP BY user_id, login_date, previous_login, gap_days
ORDER BY login_date ASC;

```

**Step 3: Running SUM creates streak groups**

turn those 1s into a streak group number using a running SUM() window.

```
WITH user_login AS (
  SELECT user_id,
         login_date,
         LAG(login_date) OVER (
           PARTITION BY user_id
           ORDER BY login_date ASC
         ) AS previous_login
  FROM logins
),
user_login_logs AS (
    SELECT user_id,
        login_date,
        previous_login,
        login_date - previous_login AS gap_days
    FROM user_login
),
streak_flags AS (
    SELECT
        user_id,
        login_date,
        previous_login,
        gap_days,
        CASE
            WHEN gap_days > 1 OR previous_login IS NULL
            THEN 1
            ELSE 0
        END AS is_new_streak
    FROM user_login_logs
    GROUP BY user_id, login_date, previous_login, gap_days
    ORDER BY login_date ASC
)
SELECT user_id,
       login_date,
       is_new_streak,
       SUM(is_new_streak) OVER (
         PARTITION BY user_id
         ORDER BY login_date ASC
       ) AS streak_group
FROM streak_flags
ORDER BY user_id, login_date;
```

**Mental model**

Think of 1 = start a new streak

Then running SUM counts how many streaks have started so far. That count becomes the streak identifier.

**Final step**

Now we can group by 'user_id', 'streak_group' to calculate each streak.

```
WITH user_login AS (
  SELECT user_id,
         login_date,
         LAG(login_date) OVER (
           PARTITION BY user_id
           ORDER BY login_date ASC
         ) AS previous_login
  FROM logins
),
user_login_logs AS (
    SELECT user_id,
        login_date,
        previous_login,
        login_date - previous_login AS gap_days
    FROM user_login
),
streak_flags AS (
    SELECT
        user_id,
        login_date,
        previous_login,
        gap_days,
        CASE
            WHEN gap_days > 1 OR previous_login IS NULL
            THEN 1
            ELSE 0
        END AS is_new_streak
    FROM user_login_logs
    GROUP BY user_id, login_date, previous_login, gap_days
    ORDER BY login_date ASC
),
streak_groups AS (
    SELECT user_id,
        login_date,
        is_new_streak,
        SUM(is_new_streak) OVER (
            PARTITION BY user_id
            ORDER BY login_date ASC
        ) AS streak_group
    FROM streak_flags
    ORDER BY user_id, login_date
)
SELECT user_id,
    MIN(login_date) AS streak_start,
    MAX(login_date) AS streak_end,
    COUNT(*) AS streak_length
FROM streak_groups
GROUP BY user_id, streak_group
ORDER BY user_id, streak_start;
```

## Drill 2 - Longest Login Streak Per User

**Dataset**

logins

| user_id | login_date |
| ------- | ---------- |
| 1       | 2025-01-01 |
| 1       | 2025-01-02 |
| 1       | 2025-01-03 |
| 1       | 2025-01-05 |
| 1       | 2025-01-06 |
| 2       | 2025-01-01 |
| 2       | 2025-01-03 |
| 2       | 2025-01-04 |

Goal

Return the longest streak per user.

Expected output:

```
| user_id | streak_start | streak_end | streak_length |
| ------- | ------------ | ---------- | ------------: |
| 1       | 2025-01-01   | 2025-01-03 |             3 |
| 2       | 2025-01-03   | 2025-01-04 |             2 |
```

```
WITH user_logins AS (
    SELECT user_id, login_date, LAG(login_date) OVER (
        PARTITION BY user_id
        ORDER BY login_date ASC
    ) AS previous_date
    FROM logins
),
streak_flags AS (
    SELECT user_id, login_date, previous_date,
        CASE
            WHEN login_date - previous_date > 1 OR previous_date IS NULL
            THEN 1
            ELSE 0
        END AS streaks
    FROM user_logins
),
streak_groups AS (
    SELECT user_id, login_date, previous_date,
    SUM(streaks) OVER (
        PARTITION BY user_id
        ORDER BY login_date
    ) AS streak_group
    FROM streak_flags
),
user_streaks AS (
    SELECT user_id,
        MIN(login_date) AS streak_start,
        MAX(login_date) AS streak_end,
        COUNT(*) AS streak_length
    FROM streak_groups
    GROUP BY user_id, streak_group
),
streak_ranks AS (
    SELECT user_id, streak_start, streak_end, streak_length, ROW_NUMBER() OVER (
        PARTITION BY user_id
        ORDER BY streak_length DESC, streak_start ASC
    ) AS rn
    FROM user_streaks
)
SELECT user_id, streak_start, streak_end, streak_length
FROM streak_ranks
WHERE rn = 1;
```

## Drill 3 - Users With At Least 3 Consecutive Login Days

**Dataset**

logins

| user_id | login_date |
| ------- | ---------- |
| 1       | 2025-01-01 |
| 1       | 2025-01-02 |
| 1       | 2025-01-03 |
| 1       | 2025-01-05 |
| 2       | 2025-01-01 |
| 2       | 2025-01-03 |
| 2       | 2025-01-04 |
| 3       | 2025-01-10 |
| 3       | 2025-01-11 |
| 3       | 2025-01-12 |
| 3       | 2025-01-13 |

Goal

Return Users With At Least 3 Consecutive Login Days

Expected output:

```
| user_id |
| ------- |
| 1       |
| 3       |
```

Because:

- user 1 has Jan 1–Jan 3 = 3 days
- user 3 has Jan 10–Jan 13 = 4 days
- user 2 only has max streak 2

```
WITH user_login AS (
    SELECT user_id,
           login_date,
           LAG(login_date) OVER (
               PARTITION BY user_id
               ORDER BY login_date ASC
           ) AS previous_date
    FROM logins
),
streak_flags AS (
    SELECT user_id,
           login_date,
           CASE
               WHEN previous_date IS NULL OR login_date - previous_date > 1
               THEN 1
               ELSE 0
           END AS is_new_streak
    FROM user_login
),
streak_groups AS (
    SELECT user_id,
           login_date,
           SUM(is_new_streak) OVER (
               PARTITION BY user_id
               ORDER BY login_date ASC
           ) AS streak_group
    FROM streak_flags
),
streak_lengths AS (
    SELECT user_id,
           MIN(login_date) AS first_login,
           MAX(login_date) AS last_login,
           COUNT(*) AS streak_length
    FROM streak_groups
    GROUP BY user_id, streak_group
)
SELECT DISTINCT user_id
FROM streak_lengths
WHERE streak_length >= 3
ORDER BY user_id;
```

## Drill 4 Consecutive Active Days Using `date - row_number`

**Dataset**

logins

| user_id | login_date |
| ------- | ---------- |
| 1       | 2025-01-01 |
| 1       | 2025-01-02 |
| 1       | 2025-01-03 |
| 1       | 2025-01-05 |
| 1       | 2025-01-06 |

Goal

Group consecutive dates.

Expected groups:

| user_id | streak_start | streak_end | streak_length |
| ------- | ------------ | ---------- | ------------: |
| 1       | 2025-01-01   | 2025-01-03 |             3 |
| 1       | 2025-01-05   | 2025-01-06 |             2 |

Write a query that first returns:

| user_id | login_date | rn | group_key |

Where:

```
rn = ROW_NUMBER() OVER (PARTITION BY user_id ORDER BY login_date)
```

and:

```
group_key = login_date - rn
```

Then:

```
WITH login_rn AS (
    SELECT user_id,
           login_date,
           ROW_NUMBER() OVER (
               PARTITION BY user_id
               ORDER BY login_date ASC
           ) AS rn
    FROM logins
),
login_groups AS (
    SELECT user_id,
           login_date,
           login_date - rn AS group_key
    FROM login_rn
)
SELECT user_id,
       MIN(login_date) AS streak_start,
       MAX(login_date) AS streak_end,
       COUNT(*) AS streak_length
FROM login_groups
GROUP BY user_id, group_key
ORDER BY user_id, streak_start;
```

## Drill 5 - Consecutive Completed Orders

**Dataset**

orders

| user_id | order_date | status    |
| ------- | ---------- | --------- |
| 1       | 2025-01-01 | completed |
| 1       | 2025-01-02 | completed |
| 1       | 2025-01-03 | cancelled |
| 1       | 2025-01-04 | completed |
| 1       | 2025-01-05 | completed |
| 1       | 2025-01-06 | completed |
| 2       | 2025-01-01 | completed |
| 2       | 2025-01-03 | completed |

Goal

Return completed-order streaks only:

| user_id | streak_start | streak_end | streak_length |
| ------- | ------------ | ---------- | ------------- |
| 1       | 2025-01-01   | 2025-01-02 | 2             |
| 1       | 2025-01-04   | 2025-01-06 | 3             |
| 2       | 2025-01-01   | 2025-01-01 | 1             |
| 2       | 2025-01-03   | 2025-01-03 | 1             |

```
WITH order_rn AS (
    SELECT user_id, order_date, ROW_NUMBER() OVER (
        PARTITION BY user_id
        ORDER BY order_date ASC
    ) AS rn
    FROM orders
    WHERE status = 'completed'
),
order_streaks AS (
    SELECT user_id, order_date, order_date - rn::int AS streak_group
    FROM order_rn
)
SELECT user_id,
    MIN(order_date) AS streak_start,
    MAX(order_date) AS streak_end,
    COUNT(*) AS streak_length
FROM order_streaks
GROUP BY user_id, streak_group
ORDER BY user_id, streak_start;
```

## Drill 6 - Duplicate Logins Per Day

**Dataset**

logins

| user_id | login_date |
| ------- | ---------- |
| 1       | 2025-01-01 |
| 1       | 2025-01-01 |
| 1       | 2025-01-02 |
| 1       | 2025-01-03 |
| 1       | 2025-01-05 |

Goal

Return login streaks, but duplicate login dates should count as one day.

Expected output:

| user_id | streak_start | streak_end | streak_length |
| ------- | ------------ | ---------- | ------------: |
| 1       | 2025-01-01   | 2025-01-03 |             3 |
| 1       | 2025-01-05   | 2025-01-05 |             1 |

```
WITH unique_login AS (
    SELECT DISTINCT user_id, login_date
    FROM logins
),
login_rn AS (
    SELECT user_id, login_date,
           ROW_NUMBER() OVER (
               PARTITION BY user_id
               ORDER BY login_date ASC
           ) AS rn
    FROM unique_login
),
login_groups AS (
    SELECT user_id,
           login_date,
           login_date - rn::int AS streak_group
    FROM login_rn
)
SELECT user_id,
       MIN(login_date) AS streak_start,
       MAX(login_date) AS streak_end,
       COUNT(*) AS streak_length
FROM login_groups
GROUP BY user_id, streak_group
ORDER BY user_id, streak_start;
```

## Drill 7 - Longest Streak With Duplicate Login Rows

**Dataset**

logins

| user_id | login_date |
| ------- | ---------- |
| 1       | 2025-01-01 |
| 1       | 2025-01-01 |
| 1       | 2025-01-02 |
| 1       | 2025-01-03 |
| 1       | 2025-01-05 |
| 2       | 2025-01-01 |
| 2       | 2025-01-01 |
| 2       | 2025-01-03 |
| 2       | 2025-01-04 |
| 2       | 2025-01-05 |

Goal

Return the longest login streak per user.

Expected output:

| user_id | streak_start | streak_end | streak_length |
| ------- | ------------ | ---------- | ------------: |
| 1       | 2025-01-01   | 2025-01-03 |             3 |
| 2       | 2025-01-03   | 2025-01-05 |             3 |

```
WITH unique_login AS (
    SELECT DISTINCT user_id, login_date
    FROM logins
),
login_rn AS (
    SELECT user_id, login_date, ROW_NUMBER() OVER (
        PARTITION BY user_id
        ORDER BY login_date
    ) AS rn
    FROM unique_login
),
streak_groups AS (
    SELECT user_id, login_date, login_date - rn::int AS streak_group
    FROM login_rn
),
streak_lengths AS (
    SELECT user_id,
        MIN(login_date) AS streak_start,
        MAX(login_date) AS streak_end,
        COUNT(*) AS streak_length
    FROM streak_groups
    GROUP BY user_id, streak_group
),
streak_rn AS (
    SELECT user_id,
           streak_start,
           streak_end,
           streak_length,
           ROW_NUMBER() OVER (
               PARTITION BY user_id
               ORDER BY streak_length DESC, streak_start ASC
           ) AS rn
    FROM streak_lengths
)
SELECT user_id, streak_start, streak_end, streak_length
FROM streak_rn
WHERE rn = 1;
```

## Drill 8 - Find Numbers Appearing at Least 4 Times Consecutively

**Dataset**

logs

| id  | num |
| --- | --- |
| 1   | 5   |
| 2   | 5   |
| 3   | 5   |
| 4   | 5   |
| 5   | 2   |
| 6   | 2   |
| 7   | 2   |
| 8   | 3   |

Goal

Return:

| ConsecutiveNums |
| --------------- |
| 5               |

```
SELECT DISTINCT num AS ConsecutiveNums
FROM (
    SELECT id,
           num,
           LAG(num, 1) OVER (ORDER BY id) AS lag1,
           LAG(num, 2) OVER (ORDER BY id) AS lag2,
           LAG(num, 3) OVER (ORDER BY id) AS lag3
    FROM logs
) t
WHERE num = lag1
  AND num = lag2
  AND num = lag3;
```

## Drill 9 - At Least 3 Consecutive Login Days Using LAG()

**Dataset**

logins

| user_id | login_date |
| ------- | ---------- |
| 1       | 2025-01-01 |
| 1       | 2025-01-02 |
| 1       | 2025-01-03 |
| 2       | 2025-01-01 |
| 2       | 2025-01-03 |
| 2       | 2025-01-04 |
| 3       | 2025-01-10 |
| 3       | 2025-01-11 |
| 3       | 2025-01-12 |

Goal

Return users who have at least 3 consecutive login days.

Expected output:

| user_id |
| ------- |
| 1       |
| 3       |

```
SELECT DISTINCT user_id
FROM (
    SELECT user_id,
        login_date,
        LAG(login_date, 1) OVER (
            PARTITION BY user_id
            ORDER BY login_date
        ) AS prev1,
        LAG(login_date, 2) OVER (
            PARTITION BY user_id
            ORDER BY login_date
        ) AS prev2
    FROM logins
) t
WHERE login_date - prev1 = 1 AND login_date - prev2 = 2;
```

## Drill 10 - 3 Consecutive Logins With Duplicates

**Dataset**

logins

| user_id | login_date |
| ------- | ---------- |
| 1       | 2025-01-01 |
| 1       | 2025-01-01 |
| 1       | 2025-01-02 |
| 1       | 2025-01-03 |
| 2       | 2025-01-01 |
| 2       | 2025-01-03 |
| 2       | 2025-01-04 |

Goal

Return users who have at least 3 consecutive login days.

Expected:

| user_id |
| ------- |
| 1       |

```
WITH prev_logins AS (
    SELECT user_id,
           login_date,
           LAG(login_date, 1) OVER (
               PARTITION BY user_id
               ORDER BY login_date
           ) AS prev1,
           LAG(login_date, 2) OVER (
               PARTITION BY user_id
               ORDER BY login_date
           ) AS prev2
    FROM (
        SELECT DISTINCT user_id, login_date
        FROM logins
    ) unique_logins
)
SELECT DISTINCT user_id
FROM prev_logins
WHERE login_date - prev1 = 1
  AND login_date - prev2 = 2;
```
