#!/usr/bin/env bash

set -euo pipefail

cd "$(dirname "$0")"

echo "=== Удаление подов ==="

kubectl delete -f k8s/

echo "=== Удаление образов ==="

minikube image rm note-service-backend:1.1

minikube image rm note-service-frontend:1.0
