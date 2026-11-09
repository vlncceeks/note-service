#!/usr/bin/env bash

set -euo pipefail

cd "$(dirname "$0")"

NAMESPACE="monitoring"

if ! helm status vmsingle -n "$NAMESPACE" &>/dev/null; then
    echo "VictoriaMetrics не установлена. Сначала запусти её установку."
    exit 1
fi

kubectl rollout status statefulset/vmsingle-victoria-metrics-single-server \
    -n "$NAMESPACE" \
    --timeout=180s

helm install vma vm/victoria-metrics-alert \
    -f vmalert-values.yaml \
    -f alertmanager-values.yaml \
    -n "$NAMESPACE" \
    --wait \
    --timeout=180s

kubectl port-forward \
    -n monitoring \
    svc/vma-victoria-metrics-alert-server \
    8880:8880
