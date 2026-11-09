#!/usr/bin/env bash

set -euo pipefail

cd "$(dirname "$0")"

NAMESPACE="monitoring"

if ! kubectl get namespace "$NAMESPACE" &>/dev/null; then
    kubectl create namespace "$NAMESPACE"
fi

helm install vmsingle vm/victoria-metrics-single \
  -f guide-vmsingle-values.yaml \
  -n "$NAMESPACE"

kubectl rollout status statefulset/vmsingle-victoria-metrics-single-server \
  -n "$NAMESPACE" \
  --timeout=180s

kubectl port-forward \
  -n "$NAMESPACE" \
  svc/vmsingle-victoria-metrics-single-server \
  8428:8428
