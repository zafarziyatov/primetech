package uz.zafar.primetech.bot.users;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import uz.zafar.primetech.bot.users.kyb.order.OrderKybMsg;
import uz.zafar.primetech.bot.users.kyb.user.UserKybMsg;
import uz.zafar.primetech.db.domain.Branch;
import uz.zafar.primetech.db.domain.User;
import uz.zafar.primetech.dto.CallbackData;

import java.util.ArrayList;
import java.util.List;

public class Kyb {
    public ReplyKeyboardMarkup setKeyboards(String[] words, int size) {
        KeyboardButton button;
        KeyboardRow row = new KeyboardRow();
        List<KeyboardRow> rows = new ArrayList<>();
        for (int i = 0; i < words.length; i++) {
            button = new KeyboardButton();
            button.setText(words[i]);
            row.add(button);
            if ((i + 1) % size == 0) {
                rows.add(row);
                row = new KeyboardRow();
            }
        }
        rows.add(row);
        ReplyKeyboardMarkup markup = new ReplyKeyboardMarkup();
        markup.setResizeKeyboard(true);
        markup.setSelective(true);
        markup.setKeyboard(rows);
        return markup;
    }

    public ReplyKeyboardMarkup menu(String lang) {
        String[] menus = UserKybMsg.menu(lang);
        KeyboardButton button = new KeyboardButton();
        button.setText(menus[0]);
        KeyboardRow row = new KeyboardRow();
        row.add(button);
        List<KeyboardRow> rows = new ArrayList<>();
        rows.add(row);
        row = new KeyboardRow();
        for (int i = 1; i < menus.length; i++) {
            button = new KeyboardButton();
            button.setText(menus[i]);
            row.add(button);
            if (i % 2 == 0) {
                rows.add(row);
                row = new KeyboardRow();
            }
        }
        rows.add(row);
        ReplyKeyboardMarkup markup = new ReplyKeyboardMarkup();
        markup.setSelective(true);
        markup.setResizeKeyboard(true);
        markup.setKeyboard(rows);
        return markup;
    }

    public ReplyKeyboardMarkup requestContact(String word) {
        KeyboardButton button;
        KeyboardRow row = new KeyboardRow();
        List<KeyboardRow> rows = new ArrayList<>();
        button = new KeyboardButton();
        button.setText(word);
        button.setRequestContact(true);
        row.add(button);
        rows.add(row);
        ReplyKeyboardMarkup markup = new ReplyKeyboardMarkup();
        markup.setResizeKeyboard(true);
        markup.setSelective(true);
        markup.setKeyboard(rows);
        return markup;
    }

    public ReplyKeyboardMarkup setKeyboard(String text) {
        return setKeyboards(new String[]{text}, 1);
    }

    public ReplyKeyboardMarkup checkOrderType(String lang) {
        return setKeyboards(UserKybMsg.checkOrderType(lang), 2);
    }

    public InlineKeyboardMarkup setKeyboards(List<CallbackData> datas, int size) {
        InlineKeyboardButton button = new InlineKeyboardButton();
        List<InlineKeyboardButton> row = new ArrayList<>();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        for (int i = 0; i < datas.size(); i++) {
            button = new InlineKeyboardButton();
            button.setText(datas.get(i).getText());
            button.setCallbackData(datas.get(i).getData());
            row.add(button);
            if ((i + 1) % size == 0) {
                rows.add(row);
                row = new ArrayList<>();
            }
        }
        rows.add(row);
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(rows);
        return markup;
    }

    public ReplyKeyboardMarkup pickup(List<Branch> list, String lang, User user) {
        String[] res = new String[list.size() + 2];
        for (int i = 0; i < list.size(); i++) {
            res[i + 2] = list.get(i).getName();
        }
        res[0] = OrderKybMsg.back(lang);
        if (lang.equals("uz")) {
            res[1] = "\uD83D\uDCCD Yaqin manzilni qidirish";
        } else if (lang.equals("en")) {
            res[1] = "\uD83D\uDCCD Search Near Address";
        } else res[1] = "\uD83D\uDCCD Поиск по адресу";
        KeyboardButton button = new KeyboardButton();
        List<KeyboardRow> rows = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();
        if (user.getCurrentLat() == null || user.getCurrentLon() == null) {
            for (int i = 0; i < 2; i++) {
                button = new KeyboardButton();
                button.setText(res[i]);
                if (i == 1) button.setRequestLocation(true);
                row.add(button);
                if ((i + 1) % 2 == 0) {
                    rows.add(row);
                    row = new KeyboardRow();
                }
            }
        } else {
            for (int i = 0; i < res.length; i++) {
                button = new KeyboardButton();
                button.setText(res[i]);
                if (i == 1) button.setRequestLocation(true);
                row.add(button);
                if ((i + 1) % 2 == 0) {
                    rows.add(row);
                    row = new KeyboardRow();
                }
            }
        }
        rows.add(row);
        ReplyKeyboardMarkup markup = new ReplyKeyboardMarkup();
        markup.setResizeKeyboard(true);
        markup.setSelective(true);
        markup.setKeyboard(rows);

        return markup;
    }
}
