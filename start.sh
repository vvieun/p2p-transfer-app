#!/bin/bash

echo "Запуск P2P transfer app..."

echo "Остановка старых контейнеров..."
docker-compose down -v

echo "Сборка и запуск контейнеров..."
docker-compose up --build -d

echo "Статус контейнеров:"
docker-compose ps

echo ""
echo "Приложение запущено"
echo "Frontend: http://localhost"
echo "Backend: http://localhost:8080"
echo "Database: localhost:5432"
echo ""
echo "Для остановки: ./stop.sh"
echo "Для просмотра логов: docker-compose logs -f"
