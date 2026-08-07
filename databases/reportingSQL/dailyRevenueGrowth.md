# Daily Revenue Growth

orders

| order_id | order_date | customer_id | amount |
| -------: | ---------- | ----------: | -----: |
|        1 | 2026-07-01 |         101 |    100 |
|        2 | 2026-07-01 |         102 |    200 |
|        3 | 2026-07-02 |         103 |    400 |
|        4 | 2026-07-03 |         101 |    300 |
|        5 | 2026-07-03 |         104 |    200 |

Excepted:

| order_date | revenue | previous_day_revenue | revenue_change |
| ---------- | ------: | -------------------: | -------------: |
| 2026-07-01 |     300 |                 NULL |           NULL |
| 2026-07-02 |     400 |                  300 |            100 |
| 2026-07-03 |     500 |                  400 |            100 |

```
WITH daily_revenue AS (
    SELECT order_date,
           SUM(amount) AS revenue
    FROM orders
    GROUP BY order_date
),
revenues AS (
    SELECT order_date,
           revenue,
           LAG(revenue) OVER (
               ORDER BY order_date
           ) AS previous_day_revenue
    FROM daily_revenue
)
SELECT order_date,
       revenue,
       previous_day_revenue,
       revenue - previous_day_revenue AS revenue_change
FROM revenues
ORDER BY order_date;
```

One important business nuance: `LAG()` compares with the previous available report row, not necessarily the previous calendar day. If there were no orders on July 2, then July 3 would compare against July 1. We’ll handle missing dates later when we build complete reporting calendars.

The reporting pattern is:

```
create complete calendar
→ attach actual data with LEFT JOIN
→ replace missing revenue with 0
→ apply LAG()
```

```
WITH date_range AS (
    SELECT generate_series(
        MIN(order_date),
        MAX(order_date),
        INTERVAL '1 day'
    )::date AS report_date
    FROM orders
),
daily_revenue AS (
    SELECT order_date,
           SUM(amount) AS revenue
    FROM orders
    GROUP BY order_date
),
complete_daily_revenue AS (
    SELECT d.report_date AS order_date,
           COALESCE(r.revenue, 0) AS revenue
    FROM date_range d
    LEFT JOIN daily_revenue r
      ON r.order_date = d.report_date
),
revenues AS (
    SELECT order_date,
           revenue,
           LAG(revenue) OVER (
               ORDER BY order_date
           ) AS previous_day_revenue
    FROM complete_daily_revenue
)
SELECT order_date,
       revenue,
       previous_day_revenue,
       revenue - previous_day_revenue AS revenue_change
FROM revenues
ORDER BY order_date;
```
