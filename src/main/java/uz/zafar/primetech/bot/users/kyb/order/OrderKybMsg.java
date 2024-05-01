package uz.zafar.primetech.bot.users.kyb.order;

public class OrderKybMsg {
    public static String[] getLocations(String lang) {
        if (lang.equals("uz")) {
            return new String[]{
                    "\uD83D\uDCCD Joylashuvni yuborish",
                    "⬅\uFE0F Orqaga"
            };
        } else if (lang.equals("en")) {
            return new String[]{
                    "\uD83D\uDCCD Send location",
                    "⬅\uFE0F Back"
            };
        } else {
            return new String[]{
                    "\uD83D\uDCCD Место отправки",
                    "⬅\uFE0F Назад"
            };
        }
    }

    public static String[] addBack(String[] list, String lang) {
        String[] res = new String[list.length + 1];
        for (int i = 0; i < list.length; i++) {
            res[i] = list[i];
        }
        String back;
        if (lang.equals("uz")) {
            back = "⬅\uFE0F Orqaga";
        } else if (lang.equals("en")) {
            back = "⬅\uFE0F Back";
        } else {
            back = "⬅\uFE0F Назад";
        }
        res[res.length - 1] = back;
        return res;
    }

    public static String back(String lang) {
        String back;
        if (lang.equals("uz")) {
            back = "⬅\uFE0F Orqaga";
        } else if (lang.equals("en")) {
            back = "⬅\uFE0F Back";
        } else {
            back = "⬅\uFE0F Назад";
        }
        return back;
    }

    public static String basket(String lang) {
        if (lang.equals("uz")) {
            return "\uD83D\uDCE5 Savat";
        } else if (lang.equals("en")) {
            return "\uD83D\uDCE5 Basket";
        } else {
            return "\uD83D\uDCE5 Корзина";
        }
    }

    public static String[] checkLocation(String lang) {
        if (lang.equals("uz")) {
            return new String[]{
                    "\uD83D\uDCCD Joylashuvni qatadan yuborish",
                    "✅ Tasdiqlash",
                    "Mening manzillarimga qo'shish",
                    "⬅\uFE0F Orqaga qaytish"
            };
        } else if (lang.equals("en")) {
            return new String[]{
                    "\uD83D\uDCCD Send location from error",
                    "Confirm",
                    "Add to My Addresses",
                    "⬅\uFE0F Back"
            };
        } else {
            return new String[]{
                    "\uD83D\uDCCD Отправить местоположение из-за ошибки",
                    "Подтверждать",
                    "Добавить в мои адреса",
                    "⬅\uFE0F Назад"
            };
        }
    }
}
