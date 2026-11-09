#!/usr/bin/env bash

set -euo pipefail

cd "$(dirname "$0")"

docker build -t prober:1.0 ../prober

minikube image load prober:1.0

kubectl apply -f app/prober-deployment.yaml
kubectl apply -f app/prober-service.yaml
