#!/bin/bash
set -e
exec > /var/log/user-data.log 2>&1

# ── Swap (2GB) — prevents OOM on t2.micro ────────────────────────────────────
fallocate -l 2G /swapfile
chmod 600 /swapfile
mkswap /swapfile
swapon /swapfile
echo '/swapfile none swap sw 0 0' >> /etc/fstab

# ── Install Docker, Compose, Git ─────────────────────────────────────────────
dnf update -y
dnf install -y docker git
systemctl enable docker
systemctl start docker
usermod -aG docker ec2-user

mkdir -p /usr/local/lib/docker/cli-plugins
curl -SL "https://github.com/docker/compose/releases/latest/download/docker-compose-linux-aarch64" \
  -o /usr/local/lib/docker/cli-plugins/docker-compose
chmod +x /usr/local/lib/docker/cli-plugins/docker-compose

# ── Write .env ────────────────────────────────────────────────────────────────
mkdir -p /opt/nps-agent
cat > /opt/nps-agent/.env << EOF
OPENROUTER_API_KEY=${openrouter_api_key}
OPENROUTER_MODEL=${openrouter_model}
EOF

# ── Write docker-compose.yml ──────────────────────────────────────────────────
cat > /opt/nps-agent/docker-compose.yml << 'COMPOSE'
services:
  postgres:
    image: pgvector/pgvector:pg16
    restart: unless-stopped
    environment:
      POSTGRES_DB: parcel_sorter
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: postgres
    volumes:
      - postgres_data:/var/lib/postgresql/data
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U postgres"]
      interval: 10s
      timeout: 5s
      retries: 5

  zookeeper:
    image: confluentinc/cp-zookeeper:7.6.0
    restart: unless-stopped
    environment:
      ZOOKEEPER_CLIENT_PORT: 2181
      KAFKA_HEAP_OPTS: "-Xmx128m -Xms128m"

  kafka:
    image: confluentinc/cp-kafka:7.6.0
    restart: unless-stopped
    depends_on:
      - zookeeper
    environment:
      KAFKA_BROKER_ID: 1
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://kafka:29092,PLAINTEXT_HOST://localhost:9092
      KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: PLAINTEXT:PLAINTEXT,PLAINTEXT_HOST:PLAINTEXT
      KAFKA_INTER_BROKER_LISTENER_NAME: PLAINTEXT
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
      KAFKA_AUTO_CREATE_TOPICS_ENABLE: "true"
      KAFKA_HEAP_OPTS: "-Xmx256m -Xms256m"

  backend:
    build:
      context: ./backend/parcel-sorter
      dockerfile: Dockerfile
    restart: unless-stopped
    ports:
      - "8081:8081"
    environment:
      SUPABASE_DB_URL: jdbc:postgresql://postgres:5432/parcel_sorter
      SUPABASE_DB_USER: postgres
      SUPABASE_DB_PASSWORD: postgres
      KAFKA_BOOTSTRAP_SERVERS: kafka:29092
      OPENROUTER_API_KEY: $${OPENROUTER_API_KEY}
      OPENROUTER_MODEL: $${OPENROUTER_MODEL:-google/gemma-3-4b-it:free}
      QUARKUS_KAFKA_DEVSERVICES_ENABLED: "false"
    depends_on:
      postgres:
        condition: service_healthy
      kafka:
        condition: service_started

  frontend:
    build:
      context: ./frontend/parcel-sorter-ui
      dockerfile: Dockerfile
    restart: unless-stopped
    ports:
      - "80:80"
      - "443:443"
    volumes:
      - /etc/letsencrypt:/etc/letsencrypt:ro
      - /var/www/certbot:/var/www/certbot:ro
    depends_on:
      - backend

volumes:
  postgres_data:
COMPOSE

# ── Install Certbot ───────────────────────────────────────────────────────────
dnf install -y certbot

# ── Clone source code ─────────────────────────────────────────────────────────
git clone https://github.com/rameshashok/terminal-parcel-sorter.git /opt/nps-agent/src
cp /opt/nps-agent/docker-compose.yml /opt/nps-agent/src/docker-compose.yml
cp /opt/nps-agent/.env /opt/nps-agent/src/.env

# ── Get Let's Encrypt certificate (standalone, port 80 free at this point) ───
DOMAIN="3-225-155-252.sslip.io"
mkdir -p /var/www/certbot
certbot certonly --standalone \
  --non-interactive --agree-tos \
  --email admin@nps-agent.local \
  -d "$DOMAIN" || echo "WARNING: certbot failed, continuing without HTTPS"

# ── Build and start all services ──────────────────────────────────────────────
cd /opt/nps-agent/src
docker compose --env-file .env up -d --build

# ── Enable pgvector after postgres is healthy ─────────────────────────────────
echo "Waiting for postgres..."
until docker compose exec -T postgres pg_isready -U postgres; do sleep 3; done

docker compose exec -T postgres psql -U postgres -d parcel_sorter \
  -c "CREATE EXTENSION IF NOT EXISTS vector;"

# ── Set up automatic cert renewal ─────────────────────────────────────────────
echo "0 3 * * * root certbot renew --quiet --pre-hook 'docker compose -f /opt/nps-agent/src/docker-compose.yml stop frontend' --post-hook 'docker compose -f /opt/nps-agent/src/docker-compose.yml start frontend'" \
  > /etc/cron.d/certbot-renew

echo "Bootstrap complete"
