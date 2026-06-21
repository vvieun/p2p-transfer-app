[English](README.md) · [Русский](README.ru.md)

# p2p transfer app

An app to send money between accounts.
A user can sign up, make an account, see their accounts, and send money.

## Stack

- backend: java, spring boot, maven
- frontend: react, axios
- database: postgresql
- containerization: docker, docker compose

## Running

```bash
./start.sh
```

To stop:

```bash
./stop.sh
```

Or do it by hand:

```bash
cd backend
./mvnw spring-boot:run

cd frontend
npm start
```

## Tests

```bash
cd backend
./mvnw test
```
