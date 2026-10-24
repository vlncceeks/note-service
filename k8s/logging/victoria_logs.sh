#!/usr/bin/env bash

set -euo pipefail

cd "$(dirname "$0")"

echo "=== Создание namespace ==="

kubectl create namespace logging

echo "=== Установка VictoriaLogs в namespace = logging  ==="

helm install vls vm/victoria-logs-single \
  --namespace logging \
  --set server.persistentVolume.size=2Gi \
  --wait \
  --timeout 5m

echo "=== Установка коллектора ==="

helm install collector vm/victoria-logs-collector \
  -n logging \
  --set "remoteWrite[0].url=http://vls-victoria-logs-single-server:9428"  

echo "=== Проброс портов 9428:9428 ==="

kubectl port-forward -n logging svc/vls-victoria-logs-single-server 9428:9428
