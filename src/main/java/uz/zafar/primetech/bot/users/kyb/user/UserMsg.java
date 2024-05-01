package uz.zafar.primetech.bot.users.kyb.user;

import org.springframework.stereotype.Controller;
import uz.zafar.primetech.db.domain.AboutMe;
import uz.zafar.primetech.db.domain.Branch;

@Controller
public class UserMsg {
    public String requestLang(String nickname) {

        return """
                🇺🇿  Assalomu aleykum <b>%s</b>, Quyidagi tillardan birini talang
                 
                🇷🇺 Здравствуйте, <b>%s</b>. Выберите один из следующих языков.
                 
                🇺🇸 Hello <b>%s</b>, Choose one of the following languages
                  """.formatted(nickname, nickname, nickname);
    }

    public String requestLang1(String lang) {

        if (lang.equals("uz"))
            return "  🇺🇿  Quyidagi tillardan birini talang";
        else if (lang.equals("ru"))
            return " 🇷🇺 Здравствуйте, <b>%s</b>. Выберите один из следующих языков.";
        else return "  🇺🇸 Hello <b>%s</b>, Choose one of the following languages";
    }

    public String requestContact(String lang) {
        String r;
        if (lang.equals("uz")) r = """
                Botdan foydalanishingiz uchun ro'yxatdan o'tishingiz kerak                
                """;
        else if (lang.equals("en")) {
            r = "You must register to use the bot";
        } else r = "Для использования бота необходимо зарегистрироваться";
        return r;
    }

    public String menu(String lang) {
        if (lang.equals("uz")) {
            return "\uD83D\uDDC2 | Asosiy menyudasiz";
        } else if (lang.equals("en")) {
            return "\uD83D\uDDC2 | Main menu";
        } else return "\uD83D\uDDC2 | Главное меню";
    }

    public String errorPhone(String lang) {
        if (lang.equals("uz")) {
            return "Iltimos telefon raqamingizni +998 xx xxx xx xx formatda kiriting";
        } else if (lang.equals("en")) {
            return "After purchase, enter your phone number in the format +998 xx xxx xx xx";
        } else return "Пожалуйста, введите свой номер телефона в формате +998 хх ххх хх хх";
    }

    public String checkOrderType(String lang) {
        if (lang.equals("uz")) {
            return "Yetkazib berish turini tanlang";
        } else if (lang.equals("en")) {
            return "Select the type of delivery";
        } else return "Выберите тип доставки";
    }

    public String allBranches(String lang) {
        if (lang.equals("uz")) {
            return "Bizning filiallarimiz";
        } else if (lang.equals("en")) {
            return "Our branches";
        } else return "«Наши филиалы»";
    }

    public String errorMsg(String lang) {
        if (lang.equals("uz")) {
            return "❌ Iltimos, tugmalardan foydalaning";
        } else if (lang.equals("en")) {
            return "❌ Please use the buttons";
        } else return "❌ Пожалуйста, используйте кнопки";
    }

    public String aboutBranch(String lang, Branch branch) {
        if (lang.equals("uz")) {
            return """
                    📍 Filial:  %s
                                         
                    🗺 Manzil:  %s
                                          
                    🏢 Orientir:  %s
                                          
                    ☎️ Telefon raqami: %s
                                          
                    🕙 Ish vaqti : %d:00 - %d:00
                    """.formatted(branch.getName(), branch.getAddress(), branch.getLandmark(), branch.getPhone(), branch.getBegin(), branch.getLast());
        } else if (lang.equals("en")) {
            return """
                                        
                    📍 Branch: %s 
                                         
                    🗺 Address: %s 
                                          
                    🏢 Landmark: %s Mosque
                                          
                    ☎️ Phone number: %s
                                          
                    🕙 Working hours: %d:00 - %d:00
                    """.formatted(branch.getName(), branch.getAddress(), branch.getLandmark(), branch.getPhone(), branch.getBegin(), branch.getLast());
        } else return """
                📍Отделение: %s 
                                    
                🗺Адрес: %s
                                     
                🏢Ориентир: %s 
                                     
                ☎️ Телефон: %s
                                     
                🕙 График работы: %d:00 - %d:00.
                """.formatted(branch.getName(), branch.getAddress(), branch.getLandmark(), branch.getPhone(), branch.getBegin(), branch.getLast());
    }

    public String myOrder(String lang) {
        if (lang.equals("uz")) {
            return "Sizda hozir buyurtmalar yo'q";
        } else if (lang.equals("en")) {
            return "You currently have no orders";
        } else return "В настоящее время у вас нет заказов";
    }

    public String comment(String lang) {
        if (lang.equals("uz")) {
            return "Izoh qoldiring. Sizning fikringiz biz uchun muhim";
        } else if (lang.equals("en")) {
            return "Leave a comment. Your opinion is important to us";
        } else return "«Оставить комментарий. Нам важно ваше мнение»";
    }

    public String successComment(String lang) {
        if (lang.equals("uz")) {
            return "✅ Izohingiz qabul qilindi";
        } else if (lang.equals("en")) {
            return "✅ Your comment has been accepted";
        } else return "✅Ваш комментарий принят";
    }

    public String settings(String lang) {
        if (lang.equals("uz")) {
            return "Sozlamani tanlang";
        } else if (lang.equals("en")) {
            return "Choose a setting";
        } else return "Выберите настройку";
    }

    public String successPhone(String lang) {
        if (lang.equals("uz")) {
            return "✅ Telefon raqamingiz muvaffaqiyatli almshtirildi";
        } else if (lang.equals("en")) {
            return "✅ Your phone number has been successfully replaced";
        } else return "✅ Ваш номер телефона успешно заменен";
    }

    public String aboutMe(AboutMe me, String lang) {
        if (lang.equals("uz")) {
            return """
                   🍟 %s
                    ☎️\s Aloqa markazi: %s
                    """.formatted(me.getCompanyName(), me.getPhone());
        } else if (lang.equals("en")) {
            return """
                   🍟 %s
                    ☎️\s Contact center: %s
                    """.formatted(me.getCompanyName(), me.getPhone());
        } else             return """
                   🍟 %s
                ☎️\s Контактный центр: %s
                    """.formatted(me.getCompanyName(), me.getPhone());

    }

}
