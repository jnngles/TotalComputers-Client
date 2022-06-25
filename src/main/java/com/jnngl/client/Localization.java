package com.jnngl.client;

public class Localization {

    private static Localization singleton;

    public static void init(Localization localization) {
        singleton = localization;
    }

    public static String get(int idx) {
        if(singleton == null) return "ERR: Uninitialized localization";
        if(idx < 0 || idx >= singleton.messages.length) return "Invalid message index: "+idx;
        return singleton.messages[idx];
    }

    public static Localization get() {
        return singleton;
    }

    public final String[] messages;
    public Localization(String[] messages) {
        this.messages = messages;
    }

    public static class HeccrbqZpsr extends Localization {

        public HeccrbqZpsr() {
            super(new String[] {
                    "Дебаг включен", // 0
                    "Введите IP (Без порта): ", // 1
                    "Введите порт: ", // 2
                    "Введите токен: ", // 3
                    "Загрузка ядра...", // 4
                    "Версия API ядра: ", // 5
                    "Регистрация пакетов...", // 6
                    "Зарегистрировано "," пакетов", // 7,8
                    "Подключение к серверу ","", // 9,10
                    "Отключено: ", // 11
                    "Клиент подключён к игроку ", // 12
                    "Настройка палитры...", // 13
                    "Кеширование цветов... ", // 14
                    "Готово.", // 15
                    "Шифрование...", // 16
                    "Не удалось загрузить файл ядра", // 17
                    "Критическая ошибка: ", // 18
                    "Сообщите об этом разработчику (https://discord.gg/6fezjgfK7F)", // 19
                    "Попробуйте сбросить токен (/tcmp token reset)", // 20
                    "Отключение...", // 21
                    "Доступно обновление: ", // 22
                    "Не удалось проверить наличие обновлений", // 23
                    "Ссылка на скачивание: ", // 24
            });
        }
    }

    public static class EnglishLang extends Localization {

        public EnglishLang() {
            super(new String[]{
                    "Debug mode enabled", // 0
                    "Enter IP address (Without port): ", // 1
                    "Enter port: ", // 2
                    "Enter token: ", // 3
                    "Loading core...", // 4
                    "Core API version: ", // 5
                    "Registering packets...", // 6
                    "Registered "," packets", // 7,8
                    "Connecting to "," server", // 9,10
                    "Disconnected: ", // 11
                    "Connected to player ", // 12
                    "Setting up palette...", // 13
                    "Caching colors... ", // 14
                    "Done.", // 15
                    "Encrypting...", // 16
                    "Unable to load core file", // 17
                    "Unexpected error: ", // 18
                    "Report this to the developer (https://discord.gg/6fezjgfK7F)", // 19
                    "Try to reset token (/tcmp token reset)", // 20
                    "Disconnecting...", // 21
                    "An update is available: ", // 22
                    "Unable to check for updates", // 23
                    "Download link: ", // 24
            });
        }
    }

}
