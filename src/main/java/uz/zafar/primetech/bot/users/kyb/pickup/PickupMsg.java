package uz.zafar.primetech.bot.users.kyb.pickup;

import org.springframework.stereotype.Controller;
import uz.zafar.primetech.db.domain.Branch;
import uz.zafar.primetech.db.domain.Category;

@Controller
public class PickupMsg {
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

    public String nearBranch(String lang, Branch branch) {
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
    public String menu(String lang) {
        if (lang.equals("uz")) {
            return "\uD83D\uDDC2 | Asosiy menyudasiz";
        } else if (lang.equals("en")) {
            return "🗂 | Вы находитесь в главном меню";
        } else return "🗂 | You are in the main menu";
    }
    public String pickupMenu(String lang) {
        if (lang.equals("uz")) {
            return "Quyidagi filiallardan birini tanlang";
        } else if (lang.equals("en")) {
            return "Choose one of the branches below";
        } else return "Выберите одно из ветвей ниже";
    }
    public String getProductsOfCategory(Category category, String lang) {
        if (lang.equals("uz")) {
            return category.getNameUz() + "\n\nMahsulotni tanlang";
        } else if (lang.equals("en")) {
            return category.getNameEn() + "\n\nSelect a product";
        } else return category.getNameRu() + "\n\nВыберите продукт";
    }

}
