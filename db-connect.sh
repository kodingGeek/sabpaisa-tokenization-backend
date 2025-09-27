#!/bin/bash
# Easy database connection script

echo "Connecting to SabPaisa Tokenization Database..."
docker-compose exec -it postgres psql -U postgres -d sabpaisa_tokenization