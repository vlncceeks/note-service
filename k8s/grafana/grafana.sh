#!/bin/bash

set -euo pipefail

cd "$(dirname "$0")"

# Добавление Helm-репозитория
helm repo add grafana-community https://grafana-community.github.io/helm-charts
helm repo update

# Установка или обновление Grafana
helm upgrade --install grafana grafana-community/grafana \
  --namespace monitoring \
  --create-namespace \
  -f grafana-values.yaml \
  --wait

# Проброс порта
kubectl port-forward svc/grafana 3000:80 -n monitoring