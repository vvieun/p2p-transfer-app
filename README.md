# p2p transfer app

приложение для переводов между счетами.
пользователь может зарегистрироваться, создать счет, посмотреть свои счета и сделать перевод.

## стек

- backend: java, spring boot, maven
- frontend: react, axios
- база данных: postgresql
- контейнеризация: docker, docker compose

## запуск

```bash
./start.sh
```

остановка:

```bash
./stop.sh
```

или вручную:

```bash
cd backend
./mvnw spring-boot:run

cd frontend
npm start
```

## тесты

```bash
cd backend
./mvnw test
```
