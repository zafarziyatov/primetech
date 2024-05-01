package uz.zafar.primetech.bot.admins;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Controller;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import uz.zafar.primetech.bot.users.Kyb;
import uz.zafar.primetech.db.domain.Category;
import uz.zafar.primetech.db.domain.Product;
import uz.zafar.primetech.dto.CallbackData;
import uz.zafar.primetech.dto.NameAndPrice;

import java.util.ArrayList;
import java.util.List;

@Controller
public class AdminKyb extends Kyb {
    public ReplyKeyboardMarkup menu() {
        String[] menu = {
                "Kategriyalar bo'limi",
                "Reklama qilish bo'limi",
                "Filiallar bo'limi",
                "Biz haqimizda bo'limi"
        };
        return setKeyboards(menu, 2);
    }

    public ReplyKeyboardMarkup getAllCategories(List<Category> list) {
        String[] categories = new String[list.size() + 2];
        for (int i = 0; i < list.size(); i++) {
            categories[i] = list.get(i).getNameUz();
        }
        categories[categories.length - 2] = "Kategroiya qo'shish";
        categories[categories.length - 1] = "⬅\uFE0F Orqaga";
        return setKeyboards(categories, 2);
    }

    public ReplyKeyboardMarkup getAllProducts(List<Product> list, String categoryName) {
        String[] categories = new String[list.size() + 2];
        for (int i = 0; i < list.size(); i++) {
            categories[i] = list.get(i).getNameUz();
        }
        categories[categories.length - 2] = "Mahsulot qo'shish";
        categories[categories.length - 1] = "⬅\uFE0F Orqaga";
        return setKeyboards(categories, 2);
    }

    public InlineKeyboardMarkup crudCategory() {
        String edit = "\uD83D\uDD8B O'zgartirish";
        String delete = "❌ O'chirish";
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText(edit);
        button.setCallbackData("edit");
        List<InlineKeyboardButton> row = new ArrayList<>();
        row.add(button);
        button = new InlineKeyboardButton();
        button.setText(delete);
        button.setCallbackData("delete");
        row.add(button);
        List<List<InlineKeyboardButton>> r = new ArrayList<>();
        r.add(row);
        row = new ArrayList<>();
        button = new InlineKeyboardButton();
        button.setText("Mahsulotlarini ko'rish");
        button.setCallbackData("get products");
        row.add(button);
        r.add(row);
        InlineKeyboardMarkup m = new InlineKeyboardMarkup();
        m.setKeyboard(r);
        return m;
    }
    public InlineKeyboardMarkup crudBranch() {
        String edit = "\uD83D\uDD8B O'zgartirish";
        String delete = "❌ O'chirish";
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText(edit);
        button.setCallbackData("edit");
        List<InlineKeyboardButton> row = new ArrayList<>();
        row.add(button);
        button = new InlineKeyboardButton();
        button.setText(delete);
        button.setCallbackData("delete");
        row.add(button);
        List<List<InlineKeyboardButton>> r = new ArrayList<>();
        r.add(row);
        InlineKeyboardMarkup m = new InlineKeyboardMarkup();
        m.setKeyboard(r);
        return m;
    }
    public InlineKeyboardMarkup crudBranch1() {
        String edit = "\uD83D\uDD8B O'zgartirish";
        String delete = "❌ O'chirish";
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText(edit);
        button.setCallbackData("edit");
        List<InlineKeyboardButton> row = new ArrayList<>();
        row.add(button);

        List<List<InlineKeyboardButton>> r = new ArrayList<>();
        r.add(row);
        InlineKeyboardMarkup m = new InlineKeyboardMarkup();
        m.setKeyboard(r);
        return m;
    }

    public InlineKeyboardMarkup addProduct() {
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText("Mahsulot qo'shish");
        button.setCallbackData("add product");
        List<InlineKeyboardButton> row = new ArrayList<>();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        row.add(button);
        rows.add(row);
        InlineKeyboardMarkup m = new InlineKeyboardMarkup();
        m.setKeyboard(rows);
        return m;
    }

    public InlineKeyboardMarkup isSuccess() {
        List<CallbackData> dataList = new ArrayList<>();
        dataList.add(new CallbackData(
                "✅ Ha", "yes delete"
        ));
        dataList.add(new CallbackData(
                "❌ Yo'q", "no delete"
        ));
        return setKeyboards(dataList, 2);

    }

    public InlineKeyboardMarkup crudProduct(List<NameAndPrice> list, String productType) {
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setCallbackData("edit");
        button.setText("\uD83D\uDD8A O'zgartirish");
        List<InlineKeyboardButton> row = new ArrayList<>();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        row.add(button);
        button = new InlineKeyboardButton();
        button.setText("❌ O'chirish");
        button.setCallbackData("delete");
        row.add(button);
        rows.add(row);
        row = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            String s = list.get(i).getFullNameAndPrice().equals(productType) ? "✅" : "";
            button = new InlineKeyboardButton();
            button.setText(s + list.get(i).getNameUz());
            button.setCallbackData(list.get(i).getFullNameAndPrice());
            row.add(button);
            if ((i + 1) % 3 == 0) {
                rows.add(row);
                row = new ArrayList<>();
            }
        }
        rows.add(row);

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(rows);
        return markup;
    }

    public InlineKeyboardMarkup editProduct() {
        CallbackData[] data = {
                new CallbackData("Uzbekcha nomi", "nameUz"),
                new CallbackData("Ruscha nomi", "nameRu"),
                new CallbackData("Inglizcha nomi", "nameEn"),
                new CallbackData("Uzbekcha tavsifi", "captionUz"),
                new CallbackData("Ruscha tavsifi", "captionRu"),
                new CallbackData("Inglizcha tavsifi", "captionEn"),
                new CallbackData("Rasmni", "img"),
                new CallbackData("Narxni", "price"),
                new CallbackData("⬅\uFE0F Orqaga", "to back"),
        };
        InlineKeyboardButton button;
        List<InlineKeyboardButton> row = new ArrayList<>();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        for (int i = 0; i < data.length; i++) {
            button = new InlineKeyboardButton();
            button.setText(data[i].getText());
            button.setCallbackData(data[i].getData());
            row.add(button);
            if ((i + 1) % 2 == 0) {
                rows.add(row);
                row = new ArrayList<>();
            }
        }
        rows.add(row);
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(rows);
        return markup;
    }
}
