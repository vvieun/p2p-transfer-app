#!/bin/bash

echo "Остановка P2P transfer app..."

docker-compose down

read -p "Удалить образы приложения? (y/N): " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]; then
    echo "Удаление образов..."
    docker image prune -f
    docker rmi p2p-transfer-app_backend p2p-transfer-app_frontend 2>/dev/null || true
fi

read -p "Удалить данные базы данных? (y/N): " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]; then
    echo "Удаление данных..."
    docker volume rm p2p-transfer-app_postgres_data 2>/dev/null || true
fi

echo "Приложение остановлено!" 
