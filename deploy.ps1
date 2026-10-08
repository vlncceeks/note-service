$ErrorActionPreference = "Stop"

Write-Host "Сборка образов" -ForegroundColor Cyan
docker build -t note-service-backend:1.1 ./backend
docker build -t note-service-frontend:1.0 ./frontend

Write-Host "Загрузка образов в minikube" -ForegroundColor Cyan
minikube image load note-service-backend:1.1
minikube image load note-service-frontend:1.0

Write-Host "Конфигурация" -ForegroundColor Cyan
kubectl apply -f k8s/postgres-config.yaml
kubectl apply -f k8s/postgres-secret.yaml
kubectl apply -f k8s/postgres-volume.yaml
kubectl apply -f k8s/backend-config.yaml
kubectl apply -f k8s/backend-secret.yaml

Write-Host "Postgres" -ForegroundColor Cyan
kubectl apply -f k8s/postgres-deployment.yaml
kubectl apply -f k8s/postgres-service.yaml

Write-Host "Ожидание готовности Postgres" -ForegroundColor Yellow
kubectl rollout status deployment/postgres --timeout=180s

Write-Host "Бэкенд" -ForegroundColor Cyan
kubectl apply -f k8s/backend-deployment.yaml
kubectl apply -f k8s/backend-service.yaml
kubectl rollout status deployment/note-service-backend --timeout=180s

Write-Host "Фронтенд" -ForegroundColor Cyan
kubectl apply -f k8s/frontend-deployment.yaml
kubectl apply -f k8s/frontend-service.yaml
kubectl rollout status deployment/note-service-frontend --timeout=120s

Write-Host "Открываю фронт" -ForegroundColor Green
minikube service note-service-frontend
