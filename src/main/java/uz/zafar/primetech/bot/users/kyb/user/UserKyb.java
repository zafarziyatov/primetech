package uz.zafar.primetech.bot.users.kyb.user;

import org.springframework.stereotype.Controller;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import uz.zafar.primetech.bot.users.Kyb;

@Controller
public class UserKyb extends Kyb {
    public ReplyKeyboardMarkup requestLang() {
        return setKeyboards(UserKybMsg.requestLang(), 2);
    }
    public ReplyKeyboardMarkup settings(String lang) {
        return setKeyboards(UserKybMsg.settings(lang), 2);
    }

}
