# 🚀 Инструкция по запуску приложения

## ⚙️ Предварительные требования

Для работы с приложением необходимо:

- 🐳 Docker
- 🔧 Docker Compose
- 🔌 Свободные порты:
    - 8080 (backend API)
    - 5432 (PostgreSQL)
    - 9000 (MinIO API)

## 🔑 Настройка переменных окружения

**_(для демонстрации настраивать переменные окружения не требуется.)_**

1. Создайте файл `.env` в директории `solution`
2. Скопируйте содержимое из `.env.example` или создайте новый файл со следующими переменными:

```env
# PostgreSQL
POSTGRES_USER=your_postgres_user
POSTGRES_PASSWORD=your_postgres_password
POSTGRES_DB=your_database_name

# MinIO
MINIO_ROOT_USER=your_minio_user           # минимум 3 символа
MINIO_ROOT_PASSWORD=your_minio_password   # минимум 8 символов

# API Keys
GOOGLE_PERSPECTIVE_API_KEY=your_google_perspective_api_key
GOOGLE_PERSPECTIVE_TOXICITY_THRESHOLD=0.7  # значение от 0 до 1
GROQ_API_KEY=your_groq_api_key
TELEGRAM_BOT_TOKEN=bot_token
```

## 🚦 Запуск приложения

1. Перейдите в директорию проекта:
   ```sh
   cd solution
   ```

2. Запустите приложение:
   ```sh
   docker compose up -d
   ```

3. Проверьте статус запуска:
   ```sh
   docker compose ps
   ```

## 🔍 Описание сервисов

### 🖥️ Backend (порт 8080)

- Spring Boot приложение
- Healthcheck: проверка каждые 30 секунд

### 🗄️ PostgreSQL (порт 5432)

- Версия: 17
- Persistent volume для данных
- Healthcheck: проверка подключения

### 📦 MinIO (порты 9000)

- API: порт 9000
- Persistent volume для объектов
- Healthcheck: проверка API

## 🎮 Управление приложением

### ⏹️ Остановка:

```sh
docker compose down
```

### 🗑️ Остановка с удалением volumes:

```sh
docker compose down -v
```

### 🔄 Пересборка и запуск:

```sh
docker compose up -d --build
```

## 📋 Просмотр логов

Все сервисы:

```sh
docker compose logs -f
```

Конкретный сервис:

```sh
docker compose logs -f backend
```

## 🔧 Устранение неполадок

1. 🚫 Если сервисы не запускаются:
    - Проверьте, свободны ли необходимые порты
    - Проверьте права доступа к директориям volumes
    - Проверьте корректность переменных в .env

2. ❌ Если MinIO недоступен:
    - Проверьте, что длина MINIO_ROOT_USER ≥ 3 символов
    - Проверьте, что длина MINIO_ROOT_PASSWORD ≥ 8 символов

3. 🔄 Для сброса состояния:
   ```sh
   docker compose down -v
   docker compose up -d
   ```

## 📝 Дополнительная информация

- [API-документация](./docs/api.yaml)
- [Описание зависимостей](./docs/DEPENDENCIES.md)
- [Схема БД](./docs/DB_SCHEMA.md)
