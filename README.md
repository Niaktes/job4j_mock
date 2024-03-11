## CheckDev - пробные собеседования в IT

Проект посвящен проверке знаний пользователей в сфере IT.

Проверка производится путём собеседования одних пользователей другими пользователями на установленные темы.

Существует возможность выбора стороны: собеседуемый или собеседователь, очередь желающих на прохождение собеседования и возможность дальнейших отзывов о знаниях оппонента по собеседованию.

### Требования к окружению:
* Java 17,
* PostgreSQL 14.0,
* Apache Maven 3.8.4
* Docker 4.25

### Используемые технологии:
* Java 17
* Maven 3.8
* Spring Boot 2.7.11
* * Oauth2
* * Data JPA
* * Security
* * Web
* * Validation
* * Thymeleaf
* * Webflux
* * Test
* Spring Kafka 2.8.11
* Spring Security Test 5.7.8
* Springdoc openAPI 1.7.0
* Jsoup 1.11.3
* Google Gson 2.8.6
* Google Guava 19.0
* Mailgun 1.9.0
* Freemarker 2.3.23
* Hibernate 5.6.15
* PostgreSQL 14 (driver v.42.6.0)
* Liquibase 4.23.1
* Lombok 1.18.22
* Checkstyle 3.2.0
* Log4J
* Telegram Telegrambots 6.7.0


* Junit 4.12
* H2database 2.1.214
* Bootstrap 5.3.2
* Httpmime 4.3.6
* AssertJ
* Mockito

### Запуск проекта c использованием <img src="https://uncommonsolutions.com/wp-content/uploads/2018/12/Microsoft-Docker-logo.png" alt="Docker" height="40">:

```
1. Клонировать проект;
2. Запустить Docker, открыть терминал в папке с проектом и выполнить команду:
    docker-compose buil
3. Поднять все базы данных и сервсисы проекта:
    docker-compose up -d 
4. Использование проекта происходит через сайт и телеграм-бота;
```
