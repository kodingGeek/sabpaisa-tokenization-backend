# Database Access Guide

## Connection Details
- **Host**: localhost
- **Port**: 5432
- **Database**: sabpaisa_tokenization
- **Username**: postgres
- **Password**: password

## Method 1: Using Docker Exec (Interactive)
```bash
# Connect to PostgreSQL via Docker
docker-compose exec -it postgres psql -U postgres -d sabpaisa_tokenization

# Common queries once connected:
# List all tables
\dt

# View merchants table
SELECT * FROM merchants;

# View tokens table
SELECT * FROM tokens;

# View token details with merchant info
SELECT t.token_value, t.masked_pan, t.status, t.usage_count, m.merchant_id 
FROM tokens t 
JOIN merchants m ON t.merchant_id = m.id;

# Exit PostgreSQL
\q
```

## Method 2: Using Docker Run (One-off queries)
```bash
# View all merchants
docker-compose exec postgres psql -U postgres -d sabpaisa_tokenization -c "SELECT * FROM merchants;"

# View all tokens
docker-compose exec postgres psql -U postgres -d sabpaisa_tokenization -c "SELECT * FROM tokens;"

# View token statistics
docker-compose exec postgres psql -U postgres -d sabpaisa_tokenization -c "SELECT COUNT(*) as total_tokens, status FROM tokens GROUP BY status;"
```

## Method 3: Using psql from host (if installed)
```bash
# Connect from your host machine
psql -h localhost -p 5432 -U postgres -d sabpaisa_tokenization
# Enter password when prompted: password
```

## Method 4: Using a GUI Tool
You can use any PostgreSQL GUI client:

1. **pgAdmin**: https://www.pgadmin.org/
2. **DBeaver**: https://dbeaver.io/
3. **TablePlus**: https://tableplus.com/
4. **DataGrip**: https://www.jetbrains.com/datagrip/

Connection settings for GUI tools:
- Host: localhost
- Port: 5432
- Database: sabpaisa_tokenization
- Username: postgres
- Password: password

## Sample Queries for Token Analysis

### View recent tokens
```sql
SELECT * FROM tokens ORDER BY created_at DESC LIMIT 10;
```

### View token usage statistics
```sql
SELECT 
    merchant_id,
    COUNT(*) as total_tokens,
    SUM(usage_count) as total_usage,
    AVG(usage_count) as avg_usage
FROM tokens
GROUP BY merchant_id;
```

### Find tokens expiring soon
```sql
SELECT * FROM tokens 
WHERE expires_at < NOW() + INTERVAL '30 days' 
AND status = 'ACTIVE';
```

### View merchant token counts
```sql
SELECT 
    m.merchant_id,
    m.business_name,
    COUNT(t.id) as token_count
FROM merchants m
LEFT JOIN tokens t ON m.id = t.merchant_id
GROUP BY m.id, m.merchant_id, m.business_name;
```