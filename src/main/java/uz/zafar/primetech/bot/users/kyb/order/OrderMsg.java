package uz.zafar.primetech.bot.users.kyb.order;

import org.springframework.stereotype.Controller;
import uz.zafar.primetech.db.domain.Branch;
import uz.zafar.primetech.db.domain.Category;

import java.util.List;

@Controller
public class OrderMsg {
    public String getLocation(String lang) {
        if (lang.equals("uz")) {
            return "Buyurtmani davom ettirish uchun iltimos lokatsiyangizni yuboring";
        } else if (lang.equals("en")) {
            return "Please send your location to proceed with the order";
        } else return "Пожалуйста, сообщите свое местоположение, чтобы продолжить заказ.";
    }

    public String checkLocation(String lang, String fullAddress) {
        if (lang.equals("uz")) {
            return """
                    Manzilingiz:
                    📍 %s
                    """.formatted(fullAddress);
        } else if (lang.equals("en")) {
            return """
                    Your address:
                    📍 %s
                    """.formatted(fullAddress);
        } else return """
                Ваш адрес:
                📍 %s
                """.formatted(fullAddress);
    }

    public String menu(String lang) {
        if (lang.equals("uz")) {
            return "\uD83D\uDDC2 | Asosiy menyudasiz";
        } else if (lang.equals("en")) {
            return "🗂 | Вы находитесь в главном меню";
        } else return "🗂 | You are in the main menu";
    }

    public String checkOrderType(String lang) {
        if (lang.equals("uz")) {
            return "Yetkazib berish turini tanlang";
        } else if (lang.equals("en")) {
            return "Select the type of delivery";
        } else return "Выберите тип доставки";
    }

    public String getCategoriesMsg(String lang) {
        if (lang.equals("uz")) {
            return "Quyidagi kategoriyalardan  birini tanlang";
        } else if (lang.equals("en")) {
            return "Choose one of the categories below";
        } else return "Выберите одну из категорий ниже";
    }

    public String addAddress(String lang, boolean succcess) {
        if (succcess) {
            if (lang.equals("uz")) {
                return "Manzil qo'shildi";
            } else if (lang.equals("en")) {
                return "Address added";
            } else return "Адрес добавлен";
        } else {
            if (lang.equals("uz")) {
                return "Bu manzil avvaldan mavjud";
            } else if (lang.equals("en")) {
                return "This address already exists";
            } else return "Этот адрес уже существует";
        }
    }

    public String getProductsOfCategory(Category category, String lang) {
        if (lang.equals("uz")) {
            return category.getNameUz() + "\n\nMahsulotni tanlang";
        } else if (lang.equals("en")) {
            return category.getNameEn() + "\n\nSelect a product";
        } else return category.getNameRu() + "\n\nВыберите продукт";
    }

    public String pickupMenu(String lang) {
        if (lang.equals("uz")) {
            return "Quyidagi filiallardan birini tanlang";
        } else if (lang.equals("en")) {
            return "Choose one of the branches below";
        } else return "Выберите одно из ветвей ниже";
    }

}
