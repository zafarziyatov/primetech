package uz.zafar.primetech.bot.users;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Controller;
import org.telegram.telegrambots.meta.api.methods.send.SendVenue;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageCaption;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Location;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import uz.zafar.primetech.bot.TelegramBot;
import uz.zafar.primetech.bot.users.kyb.order.OrderKyb;
import uz.zafar.primetech.bot.users.kyb.order.OrderKybMsg;
import uz.zafar.primetech.bot.users.kyb.order.OrderMsg;
import uz.zafar.primetech.bot.users.kyb.user.UserKybMsg;
import uz.zafar.primetech.db.domain.*;
import uz.zafar.primetech.db.repositories.BranchRepository;
import uz.zafar.primetech.db.service.*;
import uz.zafar.primetech.dto.CallbackData;
import uz.zafar.primetech.dto.DtoLocation;
import uz.zafar.primetech.dto.ResponseDto;
import uz.zafar.primetech.json.read.GetLocation;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Log4j2
@Controller
@RequiredArgsConstructor
public class Order {
    @Lazy
    private final TelegramBot bot;
    @Lazy
    private final OrderKyb kyb;
    @Lazy
    private final OrderMsg msg;
    @Lazy
    private final UserService userService;
    @Lazy
    private final LocationService locationService;
    @Lazy
    private final CategoryService categoryService;
    @Lazy
    private final ProductService productService;
    @Lazy
    private final BasketService basketService;
    @Lazy
    private final BranchRepository branchRepository;

    public void menu(User user, Update update, String serverPath) {

        Long chatId = user.getChatId();
        int level = user.getLevel();
        if (update.hasMessage()) {
            if (update.getMessage().hasText()) {
                int messageId = update.getMessage().getMessageId();
                String text = update.getMessage().getText();
                if (level == 0) {
                    zero(user, text, messageId);
                } else if (level == 1) {
                    one(user, text, messageId);
                } else if (level == 2) {
                    two(user, text, messageId);
                } else if (level == 3) {
                    three(user, update.getMessage());
                } else if (level == 4) {
                    four(user, text, serverPath, messageId);
                }
            } else if (update.getMessage().hasLocation()) {
                if (level == 1 || level == 2) {
                    Location userLocation = update.getMessage().getLocation();
                    user.setCurrentLon(userLocation.getLongitude());
                    user.setCurrentLat(userLocation.getLatitude());
                    user.setLevel(2);
                    user.setCurrentLocation(GetLocation.getLocation(userLocation.getLatitude(), userLocation.getLongitude()).getDisplay_name());
                    userService.save(user);
                    bot.sendMessage(chatId, msg.checkLocation(user.getLang(), GetLocation.getLocation(user.getCurrentLat(), user.getCurrentLon()).getDisplay_name()), kyb.checkLocation(user.getLang()));
                }
            }
        } else if (update.hasCallbackQuery()) {
            if (level == 3) {
                three(user, update.getCallbackQuery(), serverPath);
            } else if (level == 4) {
                four(user, update.getCallbackQuery(), serverPath);
            } else if (level == 5) {
                five(user, update.getCallbackQuery(), serverPath);
            }
        }
    }

    public void zero(User user, String text, int messageId) {
        String message;
        ReplyKeyboardMarkup markup;
        if (text.equals(UserKybMsg.checkOrderType(user.getLang())[0])) {
            message = msg.getLocation(user.getLang());
            markup = kyb.getLocation(user);
            user.setLevel(1);
        } else if (text.equals(UserKybMsg.checkOrderType(user.getLang())[1])) {
            message = msg.pickupMenu(user.getLang());
            markup = kyb.pickup(branchRepository.findAll(), user.getLang(),user);
            user.setEventCode("pickup type");
            user.setEventCode2("choose branch");
            user.setLevel(1);
        } else if (text.equals(UserKybMsg.checkOrderType(user.getLang())[2])) {
            message = msg.menu(user.getLang());
            markup = kyb.menu(user.getLang());
            user.setEventCode("menu");
        } else {
            bot.deleteMessage(user.getChatId(), messageId);
            return;
        }
        userService.save(user);
        bot.sendMessage(user.getChatId(), message, markup);
    }

    public void one(User user, String text, int messageId) {
        if (text.equals(OrderKybMsg.getLocations(user.getLang())[1])) {
            user.setLevel(0);
            userService.save(user);
            ReplyKeyboardMarkup markup = kyb.checkOrderType(user.getLang());
            String msgToUser = msg.checkOrderType(user.getLang());
            bot.sendMessage(user.getChatId(), msgToUser, markup);
            return;
        }
        boolean success = false;
        uz.zafar.primetech.db.domain.Location currentLocation = null;
        for (uz.zafar.primetech.db.domain.Location location : user.getLocations()) {
            if (text.equals(location.getFullAddress())) {
                success = true;
                currentLocation = location;
                break;
            }
        }

        user.setCurrentLat(currentLocation.getLatitude());
        user.setCurrentLon(currentLocation.getLongitude());
        user.setCurrentLocation(currentLocation.getFullAddress());
        userService.save(user);
        ReplyKeyboardMarkup markup;
        String msgToUser;
        if (text.equals(OrderKybMsg.getLocations(user.getLang())[1])) {
            user.setLevel(0);
            user.setEventCode("order type");
            markup = kyb.checkOrderType(user.getLang());
            msgToUser = msg.checkOrderType(user.getLang());
        } else if (success) {
            List<Category> categories = categoryService.findAll().getData();
            String[] list = new String[categories.size() + 1];
            list[0] = OrderKybMsg.basket(user.getLang());
            if (!categories.isEmpty()) {
                for (int i = 1; i < list.length; i++) {
                    if (user.getLang().equals("ru")) list[i] = categories.get(i - 1).getNameRu();
                    else if (user.getLang().equals("en")) list[i] = categories.get(i - 1).getNameEn();
                    else if (user.getLang().equals("uz")) list[i] = categories.get(i - 1).getNameUz();
                    else log.error("Til topishdagi xatolik");
                }
            }
            user.setLevel(3);
            userService.save(user);
            bot.sendMessage(user.getChatId(), msg.getCategoriesMsg(user.getLang()), kyb.setCategories(list, user.getLang()));
            return;
        } else {
            bot.deleteMessage(user.getChatId(), messageId);
            return;
        }
        bot.sendMessage(user.getChatId(), msgToUser, markup);
        userService.save(user);
    }

    public void two(User user, String text, int messageId) {
        String message;
        ReplyKeyboardMarkup markup;
        if (text.equals(OrderKybMsg.checkLocation(user.getLang())[3])) {

            message = msg.getLocation(user.getLang());
            markup = kyb.getLocation(user);
            user.setLevel(1);
            bot.sendMessage(user.getChatId(), message, markup);
            userService.save(user);
            return;
        }
        if (text.equals(OrderKybMsg.checkLocation(user.getLang())[1])) {
            List<Category> categories = categoryService.findAll().getData();
            String[] list = new String[categories.size() + 1];
            list[0] = OrderKybMsg.basket(user.getLang());
            if (!categories.isEmpty()) {
                for (int i = 1; i < list.length; i++) {
                    if (user.getLang().equals("ru")) list[i] = categories.get(i - 1).getNameRu();
                    else if (user.getLang().equals("en")) list[i] = categories.get(i - 1).getNameEn();
                    else if (user.getLang().equals("uz")) list[i] = categories.get(i - 1).getNameUz();
                    else log.error("Til topishdagi xatolik");
                }
            }
            user.setLevel(3);
            userService.save(user);
            bot.sendMessage(user.getChatId(), msg.getCategoriesMsg(user.getLang()), kyb.setCategories(list, user.getLang()));
            return;
        } else if (text.equals(OrderKybMsg.checkLocation(user.getLang())[2])) {
            uz.zafar.primetech.db.domain.Location location = new uz.zafar.primetech.db.domain.Location();
            location.setFullAddress(GetLocation.getLocation(user.getCurrentLat(), user.getCurrentLon()).getDisplay_name());
            location.setLongitude(user.getCurrentLon());
            location.setLatitude(user.getCurrentLat());
            location.setUser(user);
            boolean success = true;
            for (uz.zafar.primetech.db.domain.Location userLocation : user.getLocations()) {
                if (userLocation.getLatitude().equals(user.getCurrentLat()) && user.getCurrentLon().equals(userLocation.getLongitude()))
                    success = false;
            }
            if (success) locationService.save(location);
            message = msg.addAddress(user.getLang(), success);
            bot.sendMessage(user.getChatId(), message, messageId);
        } else {
            bot.deleteMessage(user.getChatId(), messageId);
            return;
        }
        userService.save(user);
    }

    public void three(User user, Message message) {
        String text = message.getText();
        boolean success = false;
        for (Category category : categoryService.findAll().getData()) {
            String name;
            if (user.getLang().equals("uz")) {
                name = category.getNameUz();
            } else if (user.getLang().equals("en")) {
                name = category.getNameEn();
            } else name = category.getNameRu();
            if (text.equals(name)) {
                success = true;
            }
        }

        if (text.equals(OrderKybMsg.back(user.getLang()))) {
            user.setLevel(0);
            user.setEventCode("order type");
            bot.sendMessage(user.getChatId(), msg.checkOrderType(user.getLang()), kyb.checkOrderType(user.getLang()));
            userService.save(user);
        } else if (success) {
            Category category = categoryService.findByName(text).getData();
            user.setHelperCategoryId(category.getId());
            user.setLevel(4);/// productsga o'tish
            userService.save(user);
            String lang = user.getLang();
            bot.sendMessage(user.getChatId(), msg.getProductsOfCategory(category, lang), kyb.setProductsOfCategory(category, lang));
        } else if (text.equals(OrderKybMsg.basket(user.getLang()))) {
            basket(user);
        } else bot.deleteMessage(user.getChatId(), message.getMessageId());
    }

    public void three(User user, CallbackQuery callbackQuery, String serverPath) {
        if (callbackQuery.getData().equals("clear")) {
            clear(user, callbackQuery, serverPath);
        }
    }

    public void four(User user, String text, String serverPath, Integer messageId) {
        if (text.equals(OrderKybMsg.back(user.getLang()))) {
            List<Category> categories = categoryService.findAll().getData();
            String[] list = new String[categories.size() + 1];
            list[0] = OrderKybMsg.basket(user.getLang());
            if (!categories.isEmpty()) {
                for (int i = 1; i < list.length; i++) {
                    if (user.getLang().equals("ru")) list[i] = categories.get(i - 1).getNameRu();
                    else if (user.getLang().equals("en")) list[i] = categories.get(i - 1).getNameEn();
                    else if (user.getLang().equals("uz")) list[i] = categories.get(i - 1).getNameUz();
                    else log.error("Til topishdagi xatolik");
                }
            }
            user.setLevel(3);
            userService.save(user);
            bot.sendMessage(user.getChatId(), msg.getCategoriesMsg(user.getLang()), kyb.setCategories(list, user.getLang()));
            return;
        } else if (text.equals(OrderKybMsg.basket(user.getLang()))) {
            basket(user);
        } else {
            try {
                Product product = productService.findByName(text).getData();
                Category category = categoryService.findById(user.getHelperCategoryId()).getData();
                String fileUrl = serverPath + "/" + category.getNameUz() + "/" + product.getNameUz();
                File file = new File(fileUrl);
                user.setHelperProductId(product.getId());
                String[] list = file.list();
                assert list != null;
                fileUrl = fileUrl.concat("/" + list[0]);
                file = new File(fileUrl);
                user.setHelperType(list[0]);
                user.setCountProduct(1);
                userService.save(user);
                product = productService.findByName(text).getData();
                bot.sendPhoto(user.getChatId(), file, getCaption(user, product), kyb.setProduct(list, list[0], user.getCountProduct(), user.getLang()));
            } catch (Exception e) {
                bot.sendMessage(user.getChatId(), e.getMessage());
            }
        }
    }


    public void five(User user, CallbackQuery callbackQuery, String serverPath) {
        String lang = user.getLang(), data = callbackQuery.getData();
        List<Basket> baskets = user.getBaskets().stream().filter(Basket::getActive).toList();
        if (data.equals("success order")) {
            if (sum(baskets) < 30000) {
                bot.alertMessage(callbackQuery, lang.equals("uz") ? "Buyurtmaning eng kam miqdori 30 000 so'mni tashkil etadi" : (lang.equals("en") ? "The minimum amount of the order is 30,000 soums" : "Минимальная сумма заказа 30 000 сум."));
                return;
            }
            String captionBasket = getCaptionBasket(baskets);
            String a = "";
            Branch b = null;
            double lat = user.getCurrentLat();
            double lon = user.getCurrentLon();
            List<DtoLocation> list1 = new ArrayList<>();
            for (Branch branch : branchRepository.findAll()) {
                Double lat1 = branch.getLat();
                Double lon1 = branch.getLon();
                Double distance = Math.sqrt((lat - lat1) * (lat - lat1) + (lon - lon1) * (lon - lon1));
                list1.add(new DtoLocation(branch.getName(), branch.getLat(), branch.getLon(), distance));
            }
            list1.sort(Comparator.comparingDouble(DtoLocation::getDistance));
            if (lang.equals("uz")) {

                a = """
                        Yetkazib berish narxi: %.1f
                        Umumiy: %.1f
                        Yetkazib berish turi: 🚖 Yetkazib berish
                        Geolokatsiya: %s
                        🏠 Eng yaqin filial: %s
                                               
                        To'lov turi:
                        """.formatted(10000.0, sum(baskets), user.getCurrentLocation(), list1.get(0).getFullAddress());
            }
            captionBasket = captionBasket.concat("\n\n" + a);
            List<CallbackData> list = new ArrayList<>();
            if (lang.equals("uz")) {
                list.add(new CallbackData("\uD83D\uDCB4 Naqd to'lash", "money"));
                list.add(new CallbackData("\uD83D\uDCB3 Payme", "pay me"));
                list.add(new CallbackData("\uD83D\uDCB3 Click", "click"));
            } else if (lang.equals("en")) {
                list.add(new CallbackData("\uD83D\uDCB4 Pay cash", "money"));
                list.add(new CallbackData("\uD83D\uDCB3 Payme", "pay me"));
                list.add(new CallbackData("\uD83D\uDCB3 Click", "click"));
            } else {
                list.add(new CallbackData("\uD83D\uDCB4 Платить наличными", "money"));
                list.add(new CallbackData("\uD83D\uDCB3 Payme", "pay me"));
                list.add(new CallbackData("\uD83D\uDCB3 Click", "click"));
            }
            bot.editMessageText(user.getChatId(), captionBasket,
                    callbackQuery.getMessage().getMessageId(),
                    kyb.setKeyboards(list, 2));
        } else if (data.equals("continue order")) {
            bot.deleteMessage(user.getChatId(), callbackQuery.getMessage().getMessageId());
            clear(user, callbackQuery, serverPath);
        } else if (data.equals("clear order")) {
            for (Basket basket : user.getBaskets()) {
                basket.setActive(false);
                basketService.save(basket);
            }
            clear(user, callbackQuery, serverPath);
        } else if (data.equals("cancel")) {
            String message = msg.menu(user.getLang());
            ReplyKeyboardMarkup markup = kyb.menu(user.getLang());
            user.setEventCode("menu");
            user.setLevel(0);
            bot.deleteMessage(user.getChatId(), callbackQuery.getMessage().getMessageId());
            bot.sendMessage(user.getChatId(), message, markup);
            userService.save(user);
        } else if (data.equals("change")) {

//            String captionBasket = ;
            String a = "";
            Branch b = null;
            double lat = user.getCurrentLat();
            double lon = user.getCurrentLon();
            List<DtoLocation> list1 = new ArrayList<>();
            for (Branch branch : branchRepository.findAll()) {
                Double lat1 = branch.getLat();
                Double lon1 = branch.getLon();
                Double distance = Math.sqrt((lat - lat1) * (lat - lat1) + (lon - lon1) * (lon - lon1));
                list1.add(new DtoLocation(branch.getName(), branch.getLat(), branch.getLon(), distance));
            }
            list1.sort(Comparator.comparingDouble(DtoLocation::getDistance));
            if (lang.equals("uz")) {
                a = """
                        Yetkazib berish narxi: %.1f
                        Umumiy: %.1f
                        Yetkazib berish turi: 🚖 Yetkazib berish
                        Geolokatsiya: %s
                        🏠 Eng yaqin filial: %s
                                               
                        To'lov turi:
                        """.formatted(10000.0, sum(baskets), user.getCurrentLocation(), list1.get(0).getFullAddress());
            }
            if (lang.equals("en")) {
                a = """
                        Shipping cost: %.1f
                        General: %.1f
                        Delivery type: 🚖 Delivery
                        Geolocation: %s
                        🏠 Nearest branch: %s
                                              
                        Payment type:
                        """.formatted(10000.0, sum(baskets), user.getCurrentLocation(),
                        list1.get(0).getFullAddress());
            }
            if (lang.equals("ru")) {
                a = """
                        Стоимость доставки: %.1f
                        Общее: %.1f
                        Тип доставки: 🚖 Доставка.
                        Геолокация: %s
                        🏠 Ближайший филиал: %s
                                              
                        Способ оплаты:
                        """.formatted(10000.0, sum(baskets), user.getCurrentLocation(),
                        list1.get(0).getFullAddress());
            }

            List<CallbackData> list = new ArrayList<>();
            if (lang.equals("uz")) {
                list.add(new CallbackData("\uD83D\uDCB4 Naqd to'lash", "money"));
                list.add(new CallbackData("\uD83D\uDCB3 Payme", "pay me"));
                list.add(new CallbackData("\uD83D\uDCB3 Click", "click"));
            } else if (lang.equals("en")) {
                list.add(new CallbackData("\uD83D\uDCB4 Pay cash", "money"));
                list.add(new CallbackData("\uD83D\uDCB3 Payme", "pay me"));
                list.add(new CallbackData("\uD83D\uDCB3 Click", "click"));
            } else {
                list.add(new CallbackData("\uD83D\uDCB4 Платить наличными", "money"));
                list.add(new CallbackData("\uD83D\uDCB3 Payme", "pay me"));
                list.add(new CallbackData("\uD83D\uDCB3 Click", "click"));
            }
            bot.editMessageText(user.getChatId(), getCaptionBasket(baskets) + "\n\n" + a,
                    callbackQuery.getMessage().getMessageId(),
                    kyb.setKeyboards(list, 2));

        } else if (data.equals("success")) {
            String message = msg.menu(user.getLang());
            ReplyKeyboardMarkup markup = kyb.menu(user.getLang());

            for (User admin : userService.findByRole("admin").getData()) {
                try {
                    String str = getCaptionBasket(user.getBaskets().stream().filter(Basket::getActive).toList(), true);
                    str = str.concat("\nUmumiy summa: " + sum(user.getBaskets().stream().filter(Basket::getActive).toList()));
                    str = str.concat("\n\nManzili: " + user.getCurrentLocation());
                    str = str.concat("\n\nTelefon raqami: " + user.getPhone());
                    str = str.concat("""
                            \nBog'lanish: <a href="tg://user?id=%d">%s</a>""".formatted(user.getChatId(), user.getNickname()));
                    str = str.concat("\n\nTo'lov turi: " + user.getPaymentType());
                    SendVenue sendVenue = new SendVenue();
                    sendVenue.setLongitude(user.getCurrentLon());
                    sendVenue.setLatitude(user.getCurrentLat());
                    sendVenue.setChatId(admin.getChatId());
                    sendVenue.setTitle(user.getCurrentLocation());
                    sendVenue.setAddress(user.getCurrentLocation());
                    bot.sendMessage(admin.getChatId(), str);
                    bot.executes(sendVenue);
                } catch (Exception e) {
                    log.error(e);
                }
            }
            user.setEventCode("menu");
            user.setLevel(0);
            for (Basket basket : user.getBaskets().stream().filter(Basket::getActive).toList()) {
                basket.setActive(false);
                basketService.save(basket);
            }

            String h = "";
            if (user.getLang().equals("uz")) {
                h = """
                        Buyurtmangiz: %d ko'rib chiqilmoqda!
                        Buyurtmangizni 60 daqiqa ichida yetkazib beramiz.
                        """.formatted(user.getBaskets().get(user.getBaskets().size() - 1).getId());
            }
            if (user.getLang().equals("ru")) {
                h = """
                        Ваш заказ: %d находится в обработке!
                        Мы доставим ваш заказ в течение 60 минут.
                        """.formatted(user.getBaskets().get(user.getBaskets().size() - 1).getId());
            }
            if (user.getLang().equals("en")) {
                h = """
                        Your order: %d is being processed!
                        We will deliver your order within 60 minutes.
                        """.formatted(user.getBaskets().get(user.getBaskets().size() - 1).getId());
            }
            bot.deleteMessage(user.getChatId(), callbackQuery.getMessage().getMessageId());
            bot.sendMessage(user.getChatId(), h);
            bot.sendMessage(user.getChatId(), message, markup);
            userService.save(user);
        } else {
            if (data.equals("money") || data.equals("pay me") || data.equals("click")) {
                String abc;
                if (user.getLang().equals("uz")) {
                    if (data.equals("money")) {
                        abc = "\uD83D\uDCB4 Naqd pul";
                    } else if (data.equals("pay me")) {
                        abc = "\uD83D\uDCB3 Payme";
                    } else abc = "\uD83D\uDCB3 Click";
                } else if (lang.equals("en")) {
                    if (data.equals("money")) {
                        abc = "\uD83D\uDCB4 Pay cash";
                    } else if (data.equals("pay me")) {
                        abc = "\uD83D\uDCB3 Payme";
                    } else abc = "\uD83D\uDCB3 Click";
                } else {
                    if (data.equals("money")) {
                        abc = "\uD83D\uDCB4 Платить наличными";
                    } else if (data.equals("pay me")) {
                        abc = "\uD83D\uDCB3 Payme";
                    } else abc = "\uD83D\uDCB3 Click";
                }
                user.setPaymentType(abc);
                userService.save(user);
                String captionBasket = getCaptionBasket(baskets);
                String a = "";
                Branch b = null;
                double lat = user.getCurrentLat();
                double lon = user.getCurrentLon();
                List<DtoLocation> list1 = new ArrayList<>();
                for (Branch branch : branchRepository.findAll()) {
                    Double lat1 = branch.getLat();
                    Double lon1 = branch.getLon();
                    Double distance = Math.sqrt((lat - lat1) * (lat - lat1) + (lon - lon1) * (lon - lon1));
                    list1.add(new DtoLocation(branch.getName(), branch.getLat(), branch.getLon(), distance));
                }
                list1.sort(Comparator.comparingDouble(DtoLocation::getDistance));
                if (lang.equals("uz")) {

                    a = """
                            Yetkazib berish narxi: %.1f
                            Umumiy: %.1f
                            Yetkazib berish turi: 🚖 Yetkazib berish
                            Geolokatsiya: %s
                            🏠 Eng yaqin filial: %s
                                                   
                            To'lov turi: %s
                            """.formatted(10000.0, sum(baskets), user.getCurrentLocation(), list1.get(0).getFullAddress(), abc);
                }
                captionBasket = captionBasket.concat("\n\n" + a);
                List<CallbackData> list = new ArrayList<>();
                if (lang.equals("uz")) {
                    list.add(new CallbackData("❌ Bekor qilish", "cancel"));
                    list.add(new CallbackData("✅ Tasdiqlash", "success"));
                    list.add(new CallbackData("\uD83D\uDD02 O'zgartirish", "change"));
                } else if (lang.equals("en")) {
                    list.add(new CallbackData("❌ Cancel", "cancel"));
                    list.add(new CallbackData(" ✅ Confirmation", "success"));
                    list.add(new CallbackData("\uD83D\uDD02 Change", "change"));
                } else {
                    list.add(new CallbackData("❌ Отмена", "cancel"));
                    list.add(new CallbackData(" ✅ Подтверждение", "success"));
                    list.add(new CallbackData("\uD83D\uDD02 Change", "change"));
                }
                bot.editMessageText(user.getChatId(), captionBasket,
                        callbackQuery.getMessage().getMessageId(),
                        kyb.setKeyboards(list, 2));
                return;
            }
            try {
                String[] a = data.split("_");
                Basket basket = new Basket();
                Long basketId = Long.valueOf(a[0]);
                for (Basket basket1 : user.getBaskets()) {
                    if (basket1.getId().equals(basketId)) {
                        basket = basket1;
                    }
                }
                boolean ab = true;
                double price = basket.getPrice() / basket.getCount();
                if (a[1].equals("plus")) {
                    basket.setCount(basket.getCount() + 1);
                    basket.setPrice(basket.getPrice() + price);

                } else {
                    if (basket.getCount() < 2) {
                        basket.setActive(false);
                        basketService.save(basket);
                        user = userService.findByChatId(user.getChatId()).getData();
                        bot.editMessageText(user.getChatId(), getCaptionBasket(baskets), callbackQuery.getMessage().getMessageId(), kyb.basket(baskets, user.getLang()));
                        bot.alertMessage(callbackQuery, "Afsuski 1 tadan kam buyurtma berish mumkin emas");
                    }
                    basket.setCount(basket.getCount() - 1);
                    basket.setPrice(basket.getPrice() - price);
                }
                bot.editMessageText(user.getChatId(), getCaptionBasket(baskets), callbackQuery.getMessage().getMessageId(), kyb.basket(baskets, user.getLang()));
                basketService.save(basket);
            } catch (Exception e) {
                try {
                    Long a = Long.valueOf(data.split("_")[0]);
                } catch (NumberFormatException ex) {
                    bot.alertMessage(callbackQuery, data);
                }
            }
        }
    }

    public void four(User user, CallbackQuery callbackQuery, String serverPath) {
        Integer messageId = callbackQuery.getMessage().getMessageId();
        String data = callbackQuery.getData();
        Product product = productService.findById(user.getHelperProductId()).getData();
        Category category = categoryService.findById(user.getHelperCategoryId()).getData();
        String fileUrl = serverPath + "/" + category.getNameUz() + "/" + product.getNameUz();
        File file = new File(fileUrl);
        String[] list = file.list();
        if (data.equals("+")) {
            user.setCountProduct(user.getCountProduct() + 1);
            userService.save(user);
            EditMessageCaption caption = new EditMessageCaption();
            caption.setCaption(getCaption(user, product));
            caption.setReplyMarkup(kyb.setProduct(list, user.getHelperType(), user.getCountProduct(), user.getLang()));
            caption.setParseMode("HTML");
            caption.setChatId(user.getChatId());
            caption.setMessageId(messageId);
            bot.executes(caption);
        } else if (data.equals("-")) {
            if (user.getCountProduct() == 1) {
                bot.alertMessage(callbackQuery, "Afsuski 1 tadan kam buyurtma berish mumkin emas");
            } else {
                user.setCountProduct(user.getCountProduct() - 1);
                userService.save(user);
                EditMessageCaption caption = new EditMessageCaption();
                caption.setCaption(getCaption(user, product));
                caption.setParseMode("HTML");
                caption.setReplyMarkup(kyb.setProduct(list, user.getHelperType(), user.getCountProduct(), user.getLang()));
                caption.setChatId(user.getChatId());
                caption.setMessageId(messageId);
                bot.executes(caption);
            }
        } else if (data.equals("basket")) {
            String lang = user.getLang();
            Basket basket = new Basket();
            basket.setActive(true);
            basket.setPrice(getPrice(user));
            basket.setCount(user.getCountProduct());
            basket.setProductType(user.getHelperType());
            String categoryName = "";
            String productName = "";
            if (lang.equals("uz")) {
                categoryName = category.getNameUz();
                productName = product.getNameUz();
            } else if (lang.equals("en")) {
                categoryName = category.getNameEn();
                productName = product.getNameEn();
            } else {
                categoryName = category.getNameRu();
                productName = product.getNameRu();
            }
            basket.setCategoryName(categoryName);
            basket.setProductName(productName);
            user.getBaskets().add(basket);
            basket.getUsers().add(user);
            userService.save(user);
            bot.alertMessage(callbackQuery, lang.equals("uz") ? "\uD83D\uDCE5 Savatga qo'shildi" : (lang.equals("en") ? "\uD83D\uDCE5 Added to cart" : "\uD83D\uDCE5 Добавлено в корзину."));
        } else if (data.equals("clear")) {
            clear(user, callbackQuery, serverPath);
        } else {
            user.setHelperType(data);
            user.setCountProduct(1);
            userService.save(user);
            fileUrl = fileUrl.concat("/" + data);
            file = new File(fileUrl);
            bot.editMessagePhoto(user.getChatId(), file, getCaption(user, product), messageId, kyb.setProduct(list, data, user.getCountProduct(), user.getLang()));
        }
    }

    public void clear(User user, CallbackQuery callbackQuery, String serverPath) {
        Integer messageId = callbackQuery.getMessage().getMessageId();
        String data = callbackQuery.getData();
        /*Product product = productService.findById(user.getHelperProductId()).getData();*/
//        Category category = categoryService.findById(user.getHelperCategoryId()).getData();
        /*String fileUrl = serverPath + "/" + category.getNameUz() + "/" + product.getNameUz();*/
        /*File file = new File(fileUrl);*/
        String[] list;
        bot.deleteMessage(user.getChatId(), messageId);
        List<Category> categories = categoryService.findAll().getData();
        list = new String[categories.size() + 1];
        list[0] = OrderKybMsg.basket(user.getLang());
        if (!categories.isEmpty()) {
            for (int i = 1; i < list.length; i++) {
                if (user.getLang().equals("ru")) list[i] = categories.get(i - 1).getNameRu();
                else if (user.getLang().equals("en")) list[i] = categories.get(i - 1).getNameEn();
                else if (user.getLang().equals("uz")) list[i] = categories.get(i - 1).getNameUz();
                else log.error("Til topishdagi xatolik");
            }
        }
        user.setLevel(3);
        userService.save(user);
        bot.sendMessage(user.getChatId(), msg.getCategoriesMsg(user.getLang()),
                kyb.setCategories(list, user.getLang()));
    }

    private String getCaption(User user, Product product) {
        String captionMsg = "", a = "", lang = user.getLang();
        if (lang.equals("uz")) a = product.getCaptionUz();
        if (lang.equals("ru")) a = product.getCaptionRu();
        if (lang.equals("en")) a = product.getCaptionEn();
        captionMsg = captionMsg.concat(user.getHelperType().substring(0, user.getHelperType().indexOf("_")) + "\n");
        captionMsg = captionMsg.concat(a + "\n\n");
        if (lang.equals("uz")) a = product.getNameUz();
        if (lang.equals("ru")) a = product.getNameRu();
        if (lang.equals("en")) a = product.getNameEn();
        Double price = Double.valueOf(user.getHelperType().substring(user.getHelperType().indexOf("_") + 1, user.getHelperType().lastIndexOf(".")));
        String r = "";
        r = r.concat(price + " x " + user.getCountProduct() + " = " + price * user.getCountProduct());
        captionMsg = captionMsg.concat(a + "(<b>" + user.getHelperType().substring(0, user.getHelperType().indexOf("_")) + "</b>): " + r);
        captionMsg = captionMsg.concat("\n\nUmumiy narxi: " + price * user.getCountProduct());
        return captionMsg;
    }

    private String getCaptionBasket(List<Basket> baskets) {
        String res = "";
        double sum = 0.0;
        for (Basket basket : baskets) {
            res = res.concat(basket.getProductName() + "(" + basket.getProductType().substring(0, basket.getProductType().indexOf("_")) + ") x " + (basket.getCount()) + " = " + basket.getPrice() + "\n");
            sum = sum + basket.getPrice();
        }
        res = res.concat("\nUmumiy narx: " + sum);
        return res;
    }

    private String getCaptionBasket(List<Basket> baskets, boolean a) {
        String res = "";
        double sum = 0.0;
        for (Basket basket : baskets) {
            res = res.concat(basket.getProductName() + "(" + basket.getProductType().substring(0, basket.getProductType().indexOf("_")) + ") x " + (basket.getCount()) + " = " + basket.getPrice() + "\n");
            sum = sum + basket.getPrice();
        }

        return res;
    }

    private Double sum(List<Basket> baskets) {
        double sum = 0.0;
        for (Basket basket : baskets) {
            sum = sum + basket.getPrice();
        }
        return sum;
    }

    private Double getPrice(User user) {
        Double price = Double.valueOf(user.getHelperType().substring(user.getHelperType().indexOf("_") + 1, user.getHelperType().lastIndexOf(".")));
        return price * user.getCountProduct();
    }

    public void basket(User user) {
        List<Basket> baskets =user.getBaskets().stream().filter(Basket::getActive).toList();;
        String lang = user.getLang();
        bot.sendMessage(user.getChatId(), lang.equals("uz") ? "Savat: " : (lang.equals("en") ? "Basket:" : "корзина"), true);
        if (baskets.isEmpty()) {
            String msg = "";
            String msg1 = "";
            lang = user.getLang();
            if (lang.equals("uz")) {
                msg = "Hozircha savatingiz bo'm bo'sh";
            } else if (lang.equals("en")) {
                msg = "Your cart is currently empty";
            } else if (lang.equals("ru")) {
                msg = "Ваша корзина на данный момент пуста";
            }
            List<CallbackData> datas = new ArrayList<>();
            if (lang.equals("ru")) {
                msg1 = "🆑 Уборка";
            }
            if (lang.equals("uz")) {
                msg1 = "\uD83C\uDD91 Tozalash";
            }
            if (lang.equals("en")) {
                msg1 = "\uD83C\uDD91 Cleaning";
            }
            datas.add(new CallbackData(msg1, "clear"));
            bot.sendMessage(user.getChatId(), msg, kyb.setKeyboards(datas, 1));
        } else {
            bot.sendMessage(user.getChatId(), getCaptionBasket(baskets), kyb.basket(baskets, lang));
            user.setLevel(5);
            userService.save(user);
        }
    }
}
