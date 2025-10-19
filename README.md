# Hotel Booking Platform

Микросервисная платформа для бронирования отелей, реализованная на Spring Boot с использованием Eureka, Gateway, PostgreSQL и Rest API.

## Структура проекта

hotel-booking-platform:
─ auth-service
─ booking-service
─ hotel-service
─ eureka-server
─ gateway
─ common

auth-service -  Сервис аутентификации и авторизации пользователей
booking-service - Сервис бронирования номеров
hotel-service - Сервис управления отелями и номерами
eureka-server - Сервис регистрации микросервисов
gateaway - API Gateway для маршрутизации запросов
common - Общие DTO, утилиты и константы

## Технологии

- Java 17, Spring Boot 3.3
- Spring Security
- Spring Data JPA, PostgreSQL 17
- Spring Cloud Netflix Eureka, Gateway
- Maven
- JUnit 5 + Mockito
- Lombok, MapStruct
- Swagger/OpenAPI

## Запуск проекта

1. Настройка базы данных
   - Создать PostgreSQL базу hotel_booking
   - Настроить учетные данные в application.yml каждого сервиса
2. Запуск Eureka Server
   ```
   cd eureka-server
   mvn spring-boot:run
   ```
3. Запуск остальных сервисов
   Сначала auth-service, потом hotel-service, потом booking-service и gateway:
    ```
    cd auth-service
    mvn spring-boot:run

    cd hotel-service
    mvn spring-boot:run

    cd booking-service
    mvn spring-boot:run

    cd gateway
    mvn spring-boot:run
    ```
4. Swagger/OpenAPI
   - Auth Service: http://localhost:8081/swagger-ui.html
   - Hotel Service: http://localhost:8082/swagger-ui.html
   - Booking Service: http://localhost:8083/swagger-ui.html

## Функционал

Auth Service
- Регистрация и вход пользователя
- Генерация JWT токена
- Защита REST API с использованием Spring Security

Hotel Service
- CRUD отелей и номеров
- Резервирование и освобождение номеров
- Рекомендации доступных номеров

Booking Service
- Создание и отмена бронирований
- Получение бронирований текущего пользователя
- Автоматический выбор номера при бронировании

Gateway
- Маршрутизация всех API-запросов через едину входную точку
```
  /api/bookings/** → Booking Service
  /api/hotels/**  → Hotel Service
```
- Передача JWT в сервисы

## Примеры запросов

### Регистрация пользователя

```
POST /user/register
Content-Type: application/json

{
  "username": "testuser",
  "password": "testpass"
}
```

### Создание бронирования с авто-подбором комнаты

```
POST /booking?autoSelect=true
Authorization: Bearer <JWT_TOKEN>
Content-Type: application/json

{
  "hotelId": 1,
  "checkInDate": "2025-10-20",
  "checkOutDate": "2025-10-22"
}
```

### Подтверждение доступности номера

```
POST /api/rooms/1/confirm-availability?requestId=req123
```

### Освобождение номера

```
POST /api/rooms/1/release?requestId=req123
```

## Тестирование

Все сервисы покрыты unit-тестами JUnit 5 + Mockito
Для запуска тестов:
```
mvn test
```

## Сборка

Сборка всех модулей Maven:
```
mvn clean install
```