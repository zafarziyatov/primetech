package uz.zafar.primetech.bot.superAdmin;

import org.springframework.stereotype.Controller;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import uz.zafar.primetech.bot.users.Kyb;
import uz.zafar.primetech.db.domain.Category;
import uz.zafar.primetech.db.domain.Product;
import uz.zafar.primetech.db.domain.User;
import uz.zafar.primetech.dto.CallbackData;
import uz.zafar.primetech.dto.NameAndPrice;

import java.util.ArrayList;
import java.util.List;

@Controller
public class SuperAdminKyb extends Kyb {
    public InlineKeyboardMarkup getUsers(List<User> users, int totalPages, int page) {
        List<CallbackData> data = new ArrayList<>();
        for (int i = 0; i < users.size(); i++) {
            User user = users.get(i);
            String[] a = user.getNickname().split(" ");
            data.add(new CallbackData(a[0], String.valueOf(user.getId())));
        }
        if (page == 0) {

            if (totalPages == 1) {
                return setKeyboards(data, 2);
            } else {
                data.add(new CallbackData("➡\uFE0F Next", "next"));
                return setKeyboards(data, 2);
            }
        } else {
            if (page + 1 != totalPages) {
                data.add(new CallbackData("⬅\uFE0F Back", "back"));
                data.add(new CallbackData("➡\uFE0F Next", "next"));
                return setKeyboards(data, 2);
            } else {
                data.add(new CallbackData("⬅\uFE0F Back", "back"));
                return setKeyboards(data, 2);
            }
        }
    }
    public ReplyKeyboardMarkup menu(){
        return setKeyboards(new String[]{
                "Foydalanuvchilarni username orqali izlash",
                "Foydalanuvchilarni nickname orqali izlash",
                "Foydalanuvchilarni chat id orqali izlash",
                "Foydalanuvchilarni id orqali izlash",
                "Barcha foydalanuvchilar ro'yxati",
                "Adminlar ro'yxati"
        }, 2);
    }
    public ReplyKeyboardMarkup set(List<KeyboardRow>rows){
        ReplyKeyboardMarkup markup = new ReplyKeyboardMarkup();
        markup.setResizeKeyboard(true);
        markup.setSelective(true);
        markup.setKeyboard(rows);
        return  markup ;
    }
}
