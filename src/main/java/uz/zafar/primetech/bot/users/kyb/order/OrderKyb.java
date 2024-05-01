package uz.zafar.primetech.bot.users.kyb.order;

import org.springframework.stereotype.Controller;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import uz.zafar.primetech.bot.users.Kyb;
import uz.zafar.primetech.db.domain.Basket;
import uz.zafar.primetech.db.domain.Category;
import uz.zafar.primetech.db.domain.Location;
import uz.zafar.primetech.db.domain.User;

import java.util.ArrayList;
import java.util.List;

@Controller
public class OrderKyb extends Kyb {
    public ReplyKeyboardMarkup getLocation(User user) {
        String lang = user.getLang();
        List<Location> locations = user.getLocations();
        ReplyKeyboardMarkup markup = new ReplyKeyboardMarkup();
        markup.setSelective(true);
        markup.setResizeKeyboard(true);
        KeyboardButton button;
        KeyboardRow row = new KeyboardRow();
        List<KeyboardRow> rows = new ArrayList<>();
        button = new KeyboardButton();
        button.setText(OrderKybMsg.getLocations(lang)[0]);
        button.setRequestLocation(true);
        row.add(button);
        rows.add(row);
        if (locations.isEmpty()) {
            row = new KeyboardRow();
            button = new KeyboardButton();
            button.setText(OrderKybMsg.getLocations(lang)[1]);
            row.add(button);
            rows.add(row);
            markup.setKeyboard(rows);
            return markup;
        }
        for (Location location : locations) {
            button = new KeyboardButton();
            button.setText(location.getFullAddress());
            row = new KeyboardRow();
            row.add(button);
            rows.add(row);
        }
        row = new KeyboardRow();
        button = new KeyboardButton();
        button.setText(OrderKybMsg.getLocations(lang)[1]);
        row.add(button);
        rows.add(row);
        markup.setKeyboard(rows);
        return markup;
    }

    public ReplyKeyboardMarkup setCategories(String[] categories, String lang) {
        String[] res = new String[categories.length + 1];
        res[0] = OrderKybMsg.back(lang);
        for (int i = 0; i < categories.length; i++) {
            res[i + 1] = categories[i];
        }
        return setKeyboards(res, 2);
    }

    public ReplyKeyboardMarkup setProductsOfCategory(Category category, String lang) {
        String[] res = new String[category.getProducts().size() + 2];
        String uz = "uz", ru = "ru", en = "en";
        res[0] = OrderKybMsg.back(lang);
        res[1] = OrderKybMsg.basket(lang);

        for (int i = 2; i < res.length; i++) {
            res[i] = lang.equals(uz) ? (category.getProducts().get(i - 2).getNameUz()) :
                    (lang.equals(en) ? category.getProducts().get(i - 2).getNameEn() :
                            category.getProducts().get(i - 2).getNameRu())
            ;
        }
        return setKeyboards(res, 2);
    }

    public ReplyKeyboardMarkup checkLocation(String lang) {
        KeyboardButton button = new KeyboardButton();
        KeyboardRow row = new KeyboardRow();
        List<KeyboardRow> rows = new ArrayList<>();
        for (int i = 0; i < OrderKybMsg.checkLocation(lang).length; i++) {
            button = new KeyboardButton();
            String word = OrderKybMsg.checkLocation(lang)[i];
            button.setText(word);
            if (i == 0) button.setRequestLocation(true);
            row.add(button);
            if ((i + 1) % 2 == 0) {
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

    public InlineKeyboardMarkup setProduct(String[] list, String name, int number, String lang) {
        InlineKeyboardButton button;
        List<InlineKeyboardButton> row = new ArrayList<>();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        button = new InlineKeyboardButton();
        button.setText("➖");
        button.setCallbackData("-");
        row.add(button);
        button = new InlineKeyboardButton();
        button.setText(String.valueOf(number));
        button.setCallbackData(String.valueOf(number));
        row.add(button);
        button = new InlineKeyboardButton();
        button.setText("➕");
        button.setCallbackData("+");
        row.add(button);
        rows.add(row);
        row = new ArrayList<>();
        if (list.length > 1) {
            for (int i = 0; i < list.length; i++) {
                button = new InlineKeyboardButton();
                button.setCallbackData(list[i]);
                String r = name.equals(list[i]) ? "✅ " : "";
                button.setText(r + list[i].substring(0, list[i].indexOf("_")));
                row.add(button);
                if ((i + 1) % 3 == 0) {
                    rows.add(row);
                    row = new ArrayList<>();
                }
            }
            rows.add(row);
            row = new ArrayList<>();
        }
        button = new InlineKeyboardButton();
        button.setCallbackData("basket");
        String basket;
        if (lang.equals("uz")) basket = "\uD83D\uDCE5 Savatga qo'shish";
        else if (lang.equals("ru")) basket = "\uD83D\uDCE5 В корзину";
        else basket = "\uD83D\uDCE5 Add to cart";
        button.setText(basket);
        row.add(button);
        rows.add(row);
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(rows);
        return markup;
    }

    public InlineKeyboardMarkup basket(List<Basket> list, String lang) {
        InlineKeyboardButton button = new InlineKeyboardButton();
        String successOrder;
        if (lang.equals("uz"))
            successOrder = "Buyurtmani tasdiqlash";
        else if (lang.equals("en"))
            successOrder = "Order confirmation";
        else successOrder = "Подтверждение заказа";
        button.setText(successOrder);
        button.setCallbackData("success order");
        List<InlineKeyboardButton> row = new ArrayList<>();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        row.add(button);
        rows.add(row);
        row = new ArrayList<>();
        if (lang.equals("uz"))
            successOrder = "Buyurtmani davom ettirish";
        else if (lang.equals("en"))
            successOrder = "Continue Order";
        else successOrder = "Продолжить заказ";
        button = new InlineKeyboardButton();
        button.setText(successOrder);
        button.setCallbackData("continue order");
        row.add(button);
        rows.add(row);
        row = new ArrayList<>();
        button = new InlineKeyboardButton();

        if (lang.equals("uz"))
            successOrder = "\uD83C\uDD91 Tozalash";
        else if (lang.equals("en"))
            successOrder = "\uD83C\uDD91 Clear";
        else successOrder = "\uD83C\uDD91 Очистить";
        button.setText(successOrder);
        button.setCallbackData("clear order");
        row.add(button);
        rows.add(row);
        for (Basket basket : list) {
            row = new ArrayList<>();
            InlineKeyboardButton button1 = new InlineKeyboardButton();
            button1.setText("➖");
            button1.setCallbackData(basket.getId() + "_minus");
            row.add(button1);
            InlineKeyboardButton button2 = new InlineKeyboardButton();
            button2.setCallbackData(basket.getProductName() + " " + basket.getProductType().substring(0, basket.getProductType().indexOf("_")));
            button2.setText(basket.getProductName());
            row.add(button2);
            InlineKeyboardButton button3 = new InlineKeyboardButton();
            button3.setText("➕");
            button3.setCallbackData(basket.getId() + "_plus");
            row.add(button3);
            rows.add(row);
        }
        return set(rows);
    }

    public InlineKeyboardMarkup set(List<List<InlineKeyboardButton>> rows) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(rows);
        return markup;
    }

}
