# Проект: Отслеживание и анализ сетевого трафика на Android

Мобильное Android-приложение + десктопный сервер для мониторинга параметров сотовой сети в реальном времени.

## Автор

**Стаценко Александр**, группа ИКС-432

---

## Архитектура

- **Android-клиент** (Kotlin) - сбор GPS, данных сотовых вышек и уровня сигнала, отправка на сервер через ZeroMQ
[https://github.com/KBACokk/Android_back]

- **Серверный бэкенд** (C++) - приём телеметрии, сохранение в файл, визуализация через ImGui/ImPlot
[https://github.com/KBACokk/Android_programm]

---

## Android-клиент


### Экраны приложения

| Экран | Файл | Описание |
|---|---|---|
| Главный экран | [MainActivity.kt](https://github.com/KBACokk/Android_programm/blob/main/app/src/main/java/com/example/android_project/MainActivity.kt) | Навигация по модулям |
| Калькулятор | [Calculator.kt](https://github.com/KBACokk/Android_programm/blob/main/app/src/main/java/com/example/android_project/Calculator.kt) | Арифметические операции |
| Аудиоплеер | [MediaPlayer.kt](https://github.com/KBACokk/Android_programm/blob/main/app/src/main/java/com/example/android_project/MediaPlayer.kt) | Воспроизведение музыки |
| GPS-трекер | [Location.kt](https://github.com/KBACokk/Android_programm/blob/main/app/src/main/java/com/example/android_project/Location.kt) | Отслеживание координат, сохранение в JSON |
| Телефония | [Telephony.kt](https://github.com/KBACokk/Android_programm/blob/main/app/src/main/java/com/example/android_project/Telephony.kt) | Информация о сотовых вышках (GSM/LTE/NR) |
| ZMQ-клиент | [Sockets.kt](https://github.com/KBACokk/Android_programm/blob/main/app/src/main/java/com/example/android_project/Sockets.kt) | Отправка данных на сервер |
| Фоновый сервис | [BackgroundService.kt](https://github.com/KBACokk/Android_programm/blob/main/app/src/main/java/com/example/android_programm/BackgroundService.kt) | Периодическая отправка телеметрии (каждые 7.5 сек) |

---

## Серверный бэкенд (`Android_back/`)



- Приём JSON-телеметрии через ZeroMQ (TCP, порт 7777)
- Визуализация: текущие данные, график сигнала, журнал пакетов


### Файлы ранних практик

| Файл | Описание |
|---|---|
| [MainMove.kt](https://github.com/KBACokk/Android_programm/blob/main/untitled/src/MainMove.kt) | Основная функция с массивом симуляций |
| [Lists.kt](https://github.com/KBACokk/Android_programm/blob/main/untitled/src/Lists.kt) | Дополнительные данные для `Driver()` |
| [Driver.kt](https://github.com/KBACokk/Android_programm/blob/main/untitled/src/Driver.kt) | Класс `Driver()` - движение в декартовой системе |
| [Movable.kt](https://github.com/KBACokk/Android_programm/blob/main/untitled/src/Movable.kt) | Интерфейс `Movable()` |
