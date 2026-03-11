#!/bin/bash

# Database Backup Script for RestaurantOS

set -e

# Configuration
DB_HOST="${1:-localhost}"
DB_PORT="${2:-5432}"
DB_NAME="${3:-restaurantos}"
DB_USER="${4:-postgres}"
BACKUP_DIR="./backups"
TIMESTAMP=$(date +%Y%m%d_%H%M%S)
BACKUP_FILE="${BACKUP_DIR}/restaurantos_backup_${TIMESTAMP}.sql"

# Create backup directory if it doesn't exist
mkdir -p "${BACKUP_DIR}"

echo "Starting database backup..."
echo "Host: ${DB_HOST}"
echo "Port: ${DB_PORT}"
echo "Database: ${DB_NAME}"
echo "Output: ${BACKUP_FILE}"

# Create backup
PGPASSWORD="${DB_PASSWORD}" pg_dump \
  -h "${DB_HOST}" \
  -p "${DB_PORT}" \
  -U "${DB_USER}" \
  -d "${DB_NAME}" \
  --verbose \
  --format=plain \
  > "${BACKUP_FILE}"

# Compress the backup
gzip "${BACKUP_FILE}"
BACKUP_FILE="${BACKUP_FILE}.gz"

echo "✅ Backup completed successfully!"
echo "Backup file: ${BACKUP_FILE}"
echo "File size: $(du -h ${BACKUP_FILE} | cut -f1)"

# Keep only last 7 backups
echo "Cleaning up old backups (keeping last 7)..."
ls -t ${BACKUP_DIR}/restaurantos_backup_*.sql.gz | tail -n +8 | xargs -r rm

echo "✅ Database backup completed!"

