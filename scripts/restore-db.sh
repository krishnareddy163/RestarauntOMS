#!/bin/bash

# Database Restore Script for RestaurantOS

set -e

# Configuration
BACKUP_FILE="${1:-}"
DB_HOST="${2:-localhost}"
DB_PORT="${3:-5432}"
DB_NAME="${4:-restaurantos}"
DB_USER="${5:-postgres}"

# Validate input
if [ -z "${BACKUP_FILE}" ]; then
  echo "Usage: restore-db.sh <backup-file> [db-host] [db-port] [db-name] [db-user]"
  echo "Example: restore-db.sh ./backups/restaurantos_backup_20260311_120000.sql.gz"
  exit 1
fi

if [ ! -f "${BACKUP_FILE}" ]; then
  echo "❌ Error: Backup file not found: ${BACKUP_FILE}"
  exit 1
fi

echo "Starting database restore..."
echo "Backup file: ${BACKUP_FILE}"
echo "Host: ${DB_HOST}"
echo "Port: ${DB_PORT}"
echo "Database: ${DB_NAME}"

# Ask for confirmation
read -p "⚠️  This will overwrite the database. Continue? (yes/no): " confirm
if [ "${confirm}" != "yes" ]; then
  echo "Restore cancelled."
  exit 0
fi

# Handle compressed files
if [[ "${BACKUP_FILE}" == *.gz ]]; then
  echo "Decompressing backup file..."
  TEMP_FILE=$(mktemp)
  gunzip -c "${BACKUP_FILE}" > "${TEMP_FILE}"
  BACKUP_FILE="${TEMP_FILE}"
fi

# Drop existing database and recreate
echo "Dropping existing database..."
PGPASSWORD="${DB_PASSWORD}" psql \
  -h "${DB_HOST}" \
  -p "${DB_PORT}" \
  -U "${DB_USER}" \
  -d postgres \
  -c "DROP DATABASE IF EXISTS ${DB_NAME};"

echo "Creating new database..."
PGPASSWORD="${DB_PASSWORD}" psql \
  -h "${DB_HOST}" \
  -p "${DB_PORT}" \
  -U "${DB_USER}" \
  -d postgres \
  -c "CREATE DATABASE ${DB_NAME};"

# Restore backup
echo "Restoring database..."
PGPASSWORD="${DB_PASSWORD}" psql \
  -h "${DB_HOST}" \
  -p "${DB_PORT}" \
  -U "${DB_USER}" \
  -d "${DB_NAME}" \
  < "${BACKUP_FILE}"

echo "✅ Database restore completed successfully!"

