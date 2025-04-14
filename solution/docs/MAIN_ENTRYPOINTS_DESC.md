# 📱 Демонстрация работы приложения

## 📝 Оглавление
- [Краткое описание](#краткое-описание)
- [Примеры использования API](#примеры-использования-api)
- [Создание рекламной кампании](#1-создание-рекламной-кампании)
- [Получение рекламных кампаний](#2-получение-рекламных-кампаний-рекламодателя)
- [Получение рекламного объявления](#3-получение-рекламного-объявления-для-клиента)
- [Фиксация клика](#4-фиксация-клика-по-рекламе)
- [Получение статистики](#5-получение-статистики-по-кампании)

## 📋 Краткое описание

Это backend-сервис для управления рекламными кампаниями, показа объявлений, фиксации кликов и сбора статистики.
Взаимодействие с сервисом происходит через API и Telegram-бота.

## 🔄 Примеры использования API

### 1. Создание рекламной кампании 📢

**Запрос:**
```http
POST /advertisers/{advertiserId}/campaigns
Content-Type: application/json
```

<details>

<summary>Тело запроса:</summary>

```json
{
    "ad_title": "Осенние скидки",
    "ad_text": "Скидки до 50% на осеннюю коллекцию!",
    "start_date": 1712409600,
    "end_date": 1715001600,
    "impressions_limit": 10000,
    "clicks_limit": 1000,
    "cost_per_impression": 0.01,
    "cost_per_click": 0.10,
    "targeting": {
        "gender": "ALL",
        "age_from": 18,
        "age_to": 55,
        "location": "Москва"
    }
}
```

</details>

<details>

<summary>Ответ (`201 Created`):</summary>

```json
{
    "campaign_id": "123e4567-e89b-12d3-a456-426614174000",
    "advertiser_id": "123e4567-e89b-12d3-a456-426614174001",
    "ad_title": "Осенние скидки",
    "ad_text": "Скидки до 50% на осеннюю коллекцию!",
    "start_date": 1712409600,
    "end_date": 1715001600,
    "impressions_limit": 10000,
    "clicks_limit": 1000,
    "cost_per_impression": 0.01,
    "cost_per_click": 0.10
}
```

</details>

### 2. Получение рекламных кампаний рекламодателя 📋

**Запрос:**
```http
GET /advertisers/{advertiserId}/campaigns?page=0&size=10
```

**Параметры запроса:**
- `page`: номер страницы (начиная с 0)
- `size`: количество элементов на странице

<details>

<summary>Ответ (`200 OK`):</summary>

```json
[
    {
        "campaign_id": "123e4567-e89b-12d3-a456-426614174000",
        "ad_title": "Осенние скидки",
        "impressions_limit": 10000,
        "clicks_limit": 1000
    }
]
```

</details>

### 3. Получение рекламного объявления для клиента 🎯

**Запрос:**
```http
GET /ads?client_id=123e4567-e89b-12d3-a456-426614174002
```

<details>

<summary>Ответ (`200 OK`):</summary>

```json
{
    "ad_id": "123e4567-e89b-12d3-a456-426614174003",
    "advertiser_id": "123e4567-e89b-12d3-a456-426614174001",
    "ad_title": "Осенние скидки",
    "ad_text": "Скидки до 50% на осеннюю коллекцию!",
    "image_url": "https://example.com/ad-image.jpg"
}
```

</details>

### 4. Фиксация клика по рекламе 🖱️

**Запрос:**
```http
POST /ads/{advertisementId}/click
Content-Type: application/json
```

<details>

<summary>Тело запроса:</summary>

```json
{
    "client_id": "123e4567-e89b-12d3-a456-426614174002"
}
```

</details>

**Ответ:** `204 No Content`

### 5. Получение статистики по кампании 📊

**Запрос:**
```http
GET /stats/campaigns/{advertisementId}
```

<details>

<summary>Ответ (`200 OK`):</summary>

```json
{
    "impressions_count": 5000,
    "clicks_count": 300,
    "conversion": 0.06,
    "spent_impressions": 50.0,
    "spent_clicks": 30.0,
    "spent_total": 80.0
}
```

</details>

## ❌ Коды ошибок

| Код  | Описание |
|------|----------|
| 400  | Некорректные параметры запроса |
| 401  | Не авторизован |
| 403  | Нет доступа |
| 404  | Ресурс не найден |
| 429  | Превышен лимит запросов |
| 500  | Внутренняя ошибка сервера |
