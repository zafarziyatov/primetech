package uz.zafar.primetech.bot.users.kyb.user;

public class UserKybMsg {
    public static String[] requestLang() {
        return new String[]{
                "\uD83C\uDDFA\uD83C\uDDFF O'zbekcha",
                "\uD83C\uDDF7\uD83C\uDDFA Русский",
                "\uD83C\uDDFA\uD83C\uDDF8 English"
        };
    }

    public static String requestContact(String lang) {
        if (lang.equals("uz")) {
            return "📞 Kontaktni ulashish";
        } else if (lang.equals("en")) {
            return "📞 Share contact";
        } else return "📞 Поделиться контактом";
    }

    public static String[] checkOrderType(String lang) {
        if (lang.equals("uz")) {
            return new String[]{
                    "\uD83D\uDE96 Yetkazib berish",
                    "\uD83C\uDFC3 Olib ketish",
                    "⬅\uFE0F Orqaga"
            };
        } else if (lang.equals("en")) {
            return new String[]{
                    "\uD83D\uDE96 Delivery",
                    "\uD83C\uDFC3 Pickup",
                    "⬅\uFE0F Back"
            };
        } else return new String[]{
                "\uD83D\uDE96 Доставка",
                "\uD83C\uDFC3 Пикап",
                "⬅\uFE0F Назад"
        };
    }

    public static String[] menu(String lang) {
        if (lang.equals("uz")) {
            return new String[]{
                    "\uD83D\uDECD Buyurtma berish",
                    "\uD83C\uDFD8 Barcha filiallar", "✍\uFE0F Izoh qoldirish",
                    "⚙\uFE0F Sozlamalar", "\uD83D\uDCCB Mening buyurtmalarim",
                    "ℹ\uFE0F Biz haqimizda", "\uD83D\uDCBC Vakansiyalar" , "\uD83D\uDCF9 Zerikkanlar uchun video ko'rish"
            };
        } else if (lang.equals("en")) {
            return new String[]{
                    "\uD83D\uDECD Order",
                    "\uD83C\uDFD8 All branches", "✍\uFE0F Leave a comment",
                    "⚙\uFE0F Settings", "\uD83D\uDCCB My Orders",
                    "ℹ \uFE0F About us", "\uD83D\uDCBC Vacancies","\uD83D\uDCF9 Watch videos for the bored"
            };
        } else return new String[]{
                "\uD83D\uDECD Порядок",
                "\uD83C\uDFD8 Все ветки", "✍\uFE0F Оставить комментарий",
                "⚙\uFE0F Настройки", "\uD83D\uDCCB Мои заказы",
                "ℹ \uFE0F О нас", "\uD83D\uDCBC Вакансии","\uD83D\uDCF9 Смотрите видео для скучающих"
        };
    }

    public static String[] settings(String lang) {
        if (lang.equals("uz")) {
            return new String[]{
                    "\uD83D\uDCDE Telefon raqamni o'zgartirish",
                    "\uD83C\uDDFA\uD83C\uDDFF Tilni o'zgartitish", checkOrderType(lang)[2]
            };
        } else if (lang.equals("en")) {
            return new String[]{
                    "\uD83D\uDCDE Change phone number",
                    "\uD83C\uDDFA\uD83C\uDDF8 Change language", checkOrderType(lang)[2]
            };
        } else return new String[]{
                "\uD83D\uDCDE Изменить номер телефона",
                "\uD83C\uDDF7\uD83C\uDDFA Изменить язык", checkOrderType(lang)[2]
        };
    }
}
