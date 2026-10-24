#!/usr/bin/env bash

set -euo pipefail

cd "$(dirname "$0")"

echo "=== Сборка образов ==="

docker build -t note-service-backend:1.1 ../backend
docker build -t note-service-frontend:1.0 ../frontend

echo "=== Загрузка образов в Minikube ==="

minikube image load note-service-backend:1.1
minikube image load note-service-frontend:1.0

echo "=== Конфигурация ==="

kubectl apply -f app/postgres-config.yaml
kubectl apply -f app/postgres-secret.yaml
kubectl apply -f app/postgres-volume.yaml
kubectl apply -f app/backend-config.yaml
kubectl apply -f app/backend-secret.yaml

echo "=== Postgres ==="

kubectl apply -f app/postgres-deployment.yaml
kubectl apply -f app/postgres-service.yaml

echo "=== Ожидание готовности Postgres ==="

kubectl rollout status deployment/postgres --timeout=180s

echo "=== Бэкенд ==="

kubectl apply -f app/backend-deployment.yaml
kubectl apply -f app/backend-service.yaml

kubectl rollout status deployment/note-service-backend --timeout=180s

echo "=== Фронтенд ==="

kubectl apply -f app/frontend-deployment.yaml
kubectl apply -f app/frontend-service.yaml

kubectl rollout status deployment/note-service-frontend --timeout=120s

echo "=== Открыть фронтенд ==="

minikube service note-service-frontend

echo "=== Развёртывание завершено ==="
