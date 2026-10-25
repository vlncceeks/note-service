#!/usr/bin/env bash

set -euo pipefail

cd "$(dirname "$0")"

helm install vmsingle vm/victoria-metrics-single -f guide-vmsingle-values.yaml

kubectl rollout status statefulset/vmsingle-victoria-metrics-single-server \
  --timeout=180s

kubectl port-forward svc/vmsingle-victoria-metrics-single-server 8428:8428
