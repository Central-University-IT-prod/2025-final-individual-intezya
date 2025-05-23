# 📱 Демонстрация работы приложения

## 🤖 Работа через [Telegram бота](https://t.me/prodadvertisementservicebot)

[Бот](https://t.me/prodadvertisementservicebot) предоставляет удобный интерфейс для управления рекламными кампаниями и просмотра статистики.

По нажатию на START, бот выдаст основное сообщение с кнопками


### 📝 Создание рекламной кампании (работает только в режиме автогенерации)

1. Нажимаем на "Рекламодатели"
2. Тыкаем на "Создать автоматически"
3. Выбираем "Создать объявление автоматически"

<img src="../assets/advertisement_created.png" alt="Создание кампании" width="300">

### 📈 Просмотр статистики
(Если статистики нет - она не отобразится)

1. Ищем рекламодателя в "Просмотр всех рекламодателей"
2. Выбираем кампанию после нажатия на "Посмотреть объявления"
3. Нажимаем на "Статистика" или "Ежедневная статистика" и получаем отчет с основными метриками:
    - 👀 Количество показов и кликов
    - 📊 Конверсия
    - 💵 Потраченный бюджет


<img src="../assets/stats.png" alt="Статистика" width="300">

<img src="../assets/daily_stats_day4.png" alt="Статистика" width="300">

<img src="../assets/daily_stats_day6.png" alt="Статистика" width="300">

## 🔌 API Endpoints

### 🛠️ Основные точки входа

#### 1. Показ рекламы 📺

```http
GET /ads
```

- Выбирает подходящее объявление для показа
- Учитывает все критерии таргетинга
- Возвращает данные для отображения

#### 2. Регистрация клика 🖱️

```http
POST /ads/{id}/click
```

- Фиксирует клик по рекламе
- Обновляет статистику
- Списывает средства с баланса

#### 3. Управление кампаниями 📱

```http

GET /advertisers/{advertiserId}/campaigns
PUT /advertisers/{advertiserId}/campaigns
POST /advertisers/{advertiserId}/campaigns/{advertisementId}
GET /advertisers/{advertiserId}/campaigns/{advertisementId}
PUT /advertisers/{advertiserId}/campaigns/{advertisementId}
DELETE /advertisers/{advertiserId}/campaigns/{advertisementId}
```

## 👥 Основные сценарии использования

### 1. Рекламодатель 👨‍💼

- Создает рекламную кампанию через бота
- Настраивает таргетинг и бюджет
- Отслеживает статистику

### 2. Система 🖥️

- Получает запрос на показ рекламы
- Подбирает подходящее объявление
- Фиксирует показы и клики
- Обновляет статистику

### 3. Клиент 👤

- Получает релевантную рекламу
- Взаимодействует с объявлениями
- Переходит по рекламным ссылкам