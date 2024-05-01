package uz.zafar.primetech.bot.users;

import lombok.extern.log4j.Log4j2;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Controller;
import org.telegram.telegrambots.meta.api.methods.send.SendVenue;
import org.telegram.telegrambots.meta.api.methods.send.SendVideo;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageMedia;
import org.telegram.telegrambots.meta.api.objects.Contact;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.media.InputMediaVideo;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import uz.zafar.primetech.bot.TelegramBot;
import uz.zafar.primetech.bot.users.kyb.order.OrderKyb;
import uz.zafar.primetech.bot.users.kyb.order.OrderKybMsg;
import uz.zafar.primetech.bot.users.kyb.order.OrderMsg;
import uz.zafar.primetech.bot.users.kyb.pickup.PickupKyb;
import uz.zafar.primetech.bot.users.kyb.pickup.PickupMsg;
import uz.zafar.primetech.bot.users.kyb.user.UserKyb;
import uz.zafar.primetech.bot.users.kyb.user.UserKybMsg;
import uz.zafar.primetech.bot.users.kyb.user.UserMsg;
import uz.zafar.primetech.db.domain.AboutMe;
import uz.zafar.primetech.db.domain.Basket;
import uz.zafar.primetech.db.domain.Branch;
import uz.zafar.primetech.db.domain.User;
import uz.zafar.primetech.db.repositories.AboutMeRepository;
import uz.zafar.primetech.db.repositories.BranchRepository;
import uz.zafar.primetech.db.service.*;
import uz.zafar.primetech.dto.CallbackData;
import uz.zafar.primetech.dto.UserDto;
import uz.zafar.primetech.json.read.GetLocation;

import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

@Log4j2

@Controller
@RequiredArgsConstructor
public class UserRole {
    @Lazy
    private final TelegramBot bot;
    @Lazy
    private final UserKyb kyb;
    @Lazy
    private final UserMsg msg;
    @Lazy
    private final UserService userService;
    @Lazy
    private final OrderMsg orderMsg;
    @Lazy
    private final OrderKyb orderKyb;
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
    @Lazy
    private final PickupKyb pickupKyb;
    @Lazy
    private final PickupMsg pickupMsg;
    @Lazy
    private final AboutMeRepository aboutMeRepository;

    public void menu(User user, Update update, String serverPath, Long adminChatId) {
        Long chatId = user.getChatId();
        String eventCode = user.getEventCode();
        if (update.hasMessage()) {
            if (update.getMessage().hasText()) {
                int messageId = update.getMessage().getMessageId();
                String text = update.getMessage().getText();
                if (text.equals("/start")) {
                    startCommand(user);
                } else {
                    if (eventCode.equals("choose lang")) {
                        chooseLang(text, user, null, messageId);
                    } else if (eventCode.equals("request contact")) {
                        requestContact(user, text);
                    } else if (eventCode.equals("menu")) {
                        mainMenu(user, messageId, text, serverPath);
                    } else if (eventCode.equals("video")) {
                        if (text.equals(OrderKybMsg.back(user.getLang()))) {
                            startCommand(user);
                        } else bot.deleteMessage(chatId, messageId);
                    } else if (eventCode.equals("order type")) {
                        Order order = new Order(
                                bot, orderKyb, orderMsg, userService, locationService,
                                categoryService, productService, basketService, branchRepository
                        );
                        order.menu(user, update, serverPath);
                    } else if (user.getEventCode().equals("pickup type")) {
                        Pickup pickup = new Pickup(
                                bot, pickupKyb, pickupMsg, userService, locationService, categoryService,
                                productService, basketService, branchRepository
                        );
                        pickup.menu(user, update, serverPath);
                    } else if (eventCode.equals("all branches")) {
                        allBranches(user, text);
                    } else if (eventCode.equals("comment")) {
                        comment(user, text);
                    } else if (eventCode.equals("settings")) {
                        settings(user, text);
                    } else if (eventCode.equals("get lang")) {
                        getLang(user, text, messageId);
                    } else if (eventCode.equals("get phone")) {
                        getPhone(user, text);
                    }

                }
            } else if (update.getMessage().hasLocation()) {
                if (user.getEventCode().equals("order type")) {
                    Order order = new Order(
                            bot, orderKyb, orderMsg, userService, locationService,
                            categoryService, productService, basketService, branchRepository);
                    order.menu(user, update, serverPath);
                } else if (user.getEventCode().equals("pickup type")) {
                    Pickup pickup = new Pickup(
                            bot, pickupKyb, pickupMsg, userService, locationService, categoryService,
                            productService, basketService, branchRepository
                    );
                    pickup.menu(user, update, serverPath);
                }
            } else if (update.getMessage().hasContact()) {
                if (user.getEventCode().equals("request contact")) {
                    requestContact(user, update.getMessage().getContact(), adminChatId);
                } else {
                    bot.deleteMessage(chatId, update.getMessage().getMessageId());
                }
            }
        } else if (update.hasCallbackQuery()) {
            if (user.getEventCode().equals("order type")) {
                Order order = new Order(
                        bot, orderKyb, orderMsg, userService, locationService,
                        categoryService, productService, basketService, branchRepository
                );
                order.menu(user, update, serverPath);
            } else if (user.getEventCode().equals("pickup type")) {
                Pickup pickup = new Pickup(
                        bot, pickupKyb, pickupMsg, userService, locationService, categoryService,
                        productService, basketService, branchRepository
                );
                pickup.menu(user, update, serverPath);
            } else if (eventCode.equals("video")) {
                if (update.getCallbackQuery().getData().equals("back")) {
                    while (true) {
                        try {
                            Long count = user.getCount();
                            count--;
                            if (count <= 1300) count = 3000L;
                            user.setCount(count);
                            userService.save(user);
                            EditMessageMedia editMessageMedia = new EditMessageMedia();
                            editMessageMedia.setMedia(new InputMediaVideo("https://t.me/Tik_Tok_Prikollar_Hazillar/" + count));
                            editMessageMedia.setMessageId(update.getCallbackQuery().getMessage().getMessageId());
                            editMessageMedia.setChatId(user.getChatId());
                            List<CallbackData> callbackData = new ArrayList<>();
                            callbackData.add(new CallbackData("⬅\uFE0F Back", "back"));
                            callbackData.add(new CallbackData("➡\uFE0F Next", "next"));
                            editMessageMedia.setReplyMarkup(kyb.setKeyboards(callbackData, 2));
                            bot.executes(editMessageMedia);
                            return;
                        } catch (Exception e) {
                            log.error(e);
                        }
                    }

                } else {
                    while (true) {
                        try {
                            Long count = user.getCount();
                            count++;
                            user.setCount(count);
                            userService.save(user);
                            EditMessageMedia editMessageMedia = new EditMessageMedia();
                            editMessageMedia.setMedia(new InputMediaVideo("https://t.me/Tik_Tok_Prikollar_Hazillar/" + count));
                            editMessageMedia.setMessageId(update.getCallbackQuery().getMessage().getMessageId());
                            editMessageMedia.setChatId(user.getChatId());
                            List<CallbackData> callbackData = new ArrayList<>();
                            callbackData.add(new CallbackData("⬅\uFE0F Back", "back"));
                            callbackData.add(new CallbackData("➡\uFE0F Next", "next"));
                            editMessageMedia.setReplyMarkup(kyb.setKeyboards(callbackData, 2));
                            bot.executes(editMessageMedia);
                            return;
                        } catch (Exception e) {
                            log.error(e);
                        }
                    }

                }
            }
        }
    }

    public void mainMenu(User user, int messageId, String text, String serverPath) {
        String[] texts = UserKybMsg.menu(user.getLang());
        ReplyKeyboardMarkup markup;
        String msgToUser;
        if (text.equals(texts[0])) {
            user.setEventCode("order type");
            user.setLevel(0);
            markup = kyb.checkOrderType(user.getLang());
            msgToUser = msg.checkOrderType(user.getLang());
        } else if (text.equals(texts[1])) {
            String[] branches = new String[branchRepository.findAll().size() + 1];
            branches[0] = OrderKybMsg.back(user.getLang());
            for (int i = 0; i < branchRepository.findAll().size(); i++) {
                branches[i + 1] = branchRepository.findAll().get(i).getName();
            }
            bot.sendMessage(user.getChatId(), msg.allBranches(user.getLang()), kyb.setKeyboards(branches, 2));
            user.setEventCode("all branches");
            userService.save(user);
            return;
        } else if (text.equals(texts[2])) {
            markup = kyb.setKeyboard(OrderKybMsg.back(user.getLang()));
            msgToUser = msg.comment(user.getLang());
            user.setEventCode("comment");
        } else if (text.equals(texts[3])) {
            markup = kyb.settings(user.getLang());
            msgToUser = msg.settings(user.getLang());
            user.setEventCode("settings");
        } else if (text.equals(texts[4])) {
            markup = kyb.setKeyboard(OrderKybMsg.back(user.getLang()));
            msgToUser = msg.myOrder(user.getLang());
        } else if (text.equals(OrderKybMsg.back(user.getLang()))) {
            startCommand(user);
            return;
        } else if (text.equals(texts[5])) {
            markup = kyb.setKeyboard(OrderKybMsg.back(user.getLang()));
            AboutMe me = aboutMeRepository.findAll().get(0);

            if (me.getType() == null) {
                bot.sendMessage(user.getChatId(), msg.aboutMe(me, user.getLang()), markup);
                return;
            }
            File file = new File(serverPath + "/aboutme/" + me.getImgName());
            if (me.getType().equals("photo")) {
                bot.sendPhoto(user.getChatId(), file, msg.aboutMe(me, user.getLang()), markup);
            } else if (me.getType().equals("video")) {
                SendVideo sendVideo = new SendVideo();
                InputFile inputFile = new InputFile(file);
                sendVideo.setVideo(inputFile);
                sendVideo.setChatId(user.getChatId());
                sendVideo.setCaption(msg.aboutMe(me, user.getLang()));
                sendVideo.setReplyMarkup(markup);
                bot.executes(sendVideo);
            }
            return;
        } else if (text.equals(texts[6])) {
            markup = kyb.menu(user.getLang());
            String lang = user.getLang();
            if (lang.equals("uz")) {
                msgToUser = "Hozirda vakasiyalar mavjud emas";
            } else if (lang.equals("en")) {
                msgToUser = "There are currently no vacancies";
            } else {
                msgToUser = "На данный момент вакансий нет";
            }
        } else if (text.equals(texts[7])) {
            String lang = user.getLang(), a;
            if (lang.equals("uz")) a = "Video ko'rish sahifasi";
            else if (lang.equals("en")) a = "Video viewing page";
            else a = "Страница просмотра видео";
            bot.sendMessage(user.getChatId(), a, kyb.setKeyboard(OrderKybMsg.back(user.getLang())));
//            bot.sendVideo(user.getChatId(), "https://t.me/Tik_Tok_Prikollar_Hazillar/1353");
            while (true) {
                try {
                    user = userService.findByChatId(user.getChatId()).getData();
                    long n = user.getCount();
                    if (n < 1353) n = 1353L;
                    user.setCount(n + 1);
                    userService.save(user);
                    List<CallbackData> data = new ArrayList<>();
                    data.add(new CallbackData("➡\uFE0F Next", "next"));
                    bot.sendVideo(user.getChatId(), "https://t.me/Tik_Tok_Prikollar_Hazillar/" + (n + 1), kyb.setKeyboards(data, 1));
                    user.setEventCode("video");
                    userService.save(user);

                    return;
                } catch (Exception e) {
                    log.error(e);
                }
            }

        } else {
            bot.sendMessage(user.getChatId(), msg.errorMsg(user.getLang()));
            return;
        }
        bot.sendMessage(user.getChatId(), msgToUser, markup);
        userService.save(user);
    }

    public void settings(User user, String text) {
        String[] texts = UserKybMsg.settings(user.getLang());
        if (text.equals(texts[0])) {
            String lang = user.getLang();
            if (lang.equals("uz"))
                text = """
                        Yangi telefon raqam kiriting
                                                    
                         Raqamni +998********* shaklida yuboring.
                        """;
            else if (lang.equals("en"))
                text = """
                        Enter a new phone number
                                                   
                         Send the number as +998***********.
                        """;
            else text = """
                        Введите новый номер телефона
                                                   
                         Отправьте номер как +998***********.
                        """;
            bot.sendMessage(user.getChatId(), text, kyb.setKeyboard(OrderKybMsg.back(lang)));
            user.setEventCode("get phone");
            userService.save(user);
        } else if (texts[1].equals(text)) {
            bot.sendMessage(user.getChatId(), msg.requestLang1(user.getLang()), kyb.requestLang());
            user.setEventCode("get lang");
            userService.save(user);
        } else if (text.equals(texts[2])) {
            startCommand(user);
        } else {
            bot.sendMessage(user.getChatId(), msg.errorMsg(user.getLang()), kyb.settings(user.getLang()));
        }

    }

    public void getPhone(User user, String text) {
        if (text.equals(OrderKybMsg.back(user.getLang()))) {
            ReplyKeyboardMarkup markup = kyb.settings(user.getLang());
            String msgToUser = msg.settings(user.getLang());
            user.setEventCode("settings");
            userService.save(user);
            bot.sendMessage(user.getChatId(), msgToUser, markup);
        } else {
            boolean success = false;
            try {
                Long.valueOf(text.substring(1));
                if (text.substring(0, 4).equals("+998") && text.length() == 13) {
                    success = true;
                }
            } catch (Exception e) {
                bot.sendMessage(user.getChatId(), msg.errorPhone(user.getLang()), kyb.setKeyboard(OrderKybMsg.back(user.getLang())));
                return;
            }
            if (success) {
                user.setPhone(text);
                userService.save(user);
                ReplyKeyboardMarkup markup = kyb.settings(user.getLang());

                String msgToUser1 = msg.successPhone(user.getLang());
                String msgToUser = msg.settings(user.getLang());
                user.setEventCode("settings");
                userService.save(user);

                bot.sendMessage(user.getChatId(), msgToUser1);
                bot.sendMessage(user.getChatId(), msgToUser, markup);
            }
        }
    }

    public void getLang(User user, String text, int messageId) {
        chooseLang(text, user, user.getLang(), messageId);
    }

    public void comment(User user, String text) {
        if (text.equals(OrderKybMsg.back(user.getLang()))) {
            startCommand(user);
        } else {
            bot.sendMessage(user.getChatId(), msg.successComment(user.getLang()));
            startCommand(user);

            for (User admin : userService.findByRole("admin").getData()) {
                bot.sendMessage(admin.getChatId(), """
                        Botga izoh qoldirdi
                                                
                        <i>Foydalanuvchining id si:</i> <b>%d</b>
                        <i>Foydalanuvchining niki:</i> <b>%s</b>
                        <i>Foydalanuvchining telefon nomeri:</i> <b>%s</b>
                        <i>Foydalanuvchi bilan bog'lanish:</i> <a href="tg://user?id=%d">%s</a>
                        <i>Foydalanuvchining botga qoldirgan izohi:</i> <code>%s</code>
                                                
                        """.formatted(
                        user.getId(), user.getNickname(), user.getPhone(), user.getChatId(), user.getNickname(), text
                ));
            }
        }
    }

    public void allBranches(User user, String text) {
        if (text.equals(OrderKybMsg.back(user.getLang()))) {
            startCommand(user);
        } else {
            String[] branches = new String[branchRepository.findAll().size() + 1];
            branches[0] = OrderKybMsg.back(user.getLang());
            for (int i = 0; i < branchRepository.findAll().size(); i++) {
                branches[i + 1] = branchRepository.findAll().get(i).getName();
            }
            try {
                Branch branch = branchRepository.findByName(text);
                if (branch == null) {

                    bot.sendMessage(user.getChatId(), msg.errorMsg(user.getLang()), kyb.setKeyboards(branches, 2));
                } else {
                    bot.sendMessage(user.getChatId(), msg.aboutBranch(user.getLang(), branch), kyb.setKeyboards(branches, 2));
                    SendVenue sendVenue = new SendVenue();
                    sendVenue.setTitle(branch.getName());
                    sendVenue.setLatitude(branch.getLat());
                    sendVenue.setLongitude(branch.getLon());
                    sendVenue.setAddress(Objects.requireNonNull(GetLocation.getLocation(branch.getLat(), branch.getLon())).getDisplay_name());
                    sendVenue.setChatId(user.getChatId());
                    bot.executes(sendVenue);
                }
            } catch (Exception e) {
                bot.sendMessage(user.getChatId(), msg.errorMsg(user.getLang()), kyb.setKeyboards(branches, 2));

            }
        }
    }

    public void startCommand(User user) {
        for (Basket basket : user.getBaskets().stream().filter(Basket::getActive).toList()) {
            basket.setActive(false);
            basketService.save(basket);
        }
        String message;
        ReplyKeyboardMarkup markup;
        Long chatId = user.getChatId();
        if (user.getLang() == null) {
            message = msg.requestLang(user.getNickname());
            markup = kyb.requestLang();
            user.setEventCode("choose lang");
        } else {
            if (user.getPhone() == null) {
                message = msg.requestContact(user.getLang());
                markup = kyb.requestContact(UserKybMsg.requestContact(user.getLang()));
                user.setEventCode("request contact");
            } else {
                message = msg.menu(user.getLang());
                markup = kyb.menu(user.getLang());
                user.setEventCode("menu");
            }

        }
        bot.sendMessage(chatId, message, markup);
        userService.save(user);
    }

    public void chooseLang(String text, User user, String helpLang, int messegId) {
        String[] texts = UserKybMsg.requestLang();
        String lang = null;
        if (texts[0].equals(text)) lang = "uz";
        else if (texts[1].equals(text)) lang = "ru";
        else if (texts[2].equals(text)) lang = "en";
        else {
            if (helpLang != null)
                bot.sendMessage(user.getChatId(), msg.errorMsg(helpLang), messegId, kyb.settings(helpLang));
            else bot.deleteMessage(user.getChatId(), messegId);
            return;
        }
        user.setLang(lang);
        userService.save(user);
        startCommand(user);
    }

    public void requestContact(User user, Contact contact, Long adminChatId) {
        String phone = contact.getPhoneNumber();
        if (phone.charAt(0) != '+') phone = "+" + phone;
        user.setPhone(phone);
        userService.save(user);
        String message = msg.menu(user.getLang());
        ReplyKeyboardMarkup markup = kyb.menu(user.getLang());
        user.setEventCode("menu");
        bot.sendMessage(user.getChatId(), message, markup);
        userService.save(user);
        String msg = "Yangi foydalanuvchi ro'yxatdan o'tdi\n\n";

        msg = msg.concat("\nFoydalanuvchining ismi: " + user.getNickname());
        msg = msg.concat("\nFoydalanuvchining usernamesi: @" + user.getUsername());
        msg = msg.concat("\nFoydalanuvchining telefon raqami: " + user.getPhone());
        msg = msg.concat("\nFoydalanuvchining botga kirgan vaqti: " + getTime(user.getDay()));
        bot.sendMessage(adminChatId, msg);
    }

    public String getTime(LocalDate date) {
        String time = date.toString(), res = "";
        String[] times = time.split("-");

        res = times[0] + " - yil ";
        int month = Integer.parseInt(times[1]);
        int a = Integer.parseInt(times[2]);
        if (month == 1) res = res.concat(a + " - yanvar");
        if (month == 2) res = res.concat(a + " - fevral");
        if (month == 3) res = res.concat(a + " - mart");
        if (month == 4) res = res.concat(a + " - aprel");
        if (month == 5) res = res.concat(a + " - may");
        if (month == 6) res = res.concat(a + " - iyun");
        if (month == 7) res = res.concat(a + " - iyul");
        if (month == 8) res = res.concat(a + " - avgust");
        if (month == 9) res = res.concat(a + " - sentabr");
        if (month == 10) res = res.concat(a + " - oktabr");
        if (month == 11) res = res.concat(a + " - noyabr");
        if (month == 12) res = res.concat(a + " - dekabr");

        return res;

    }

    public void requestContact(User user, String text) {
        try {
            String phone = text;
            if (phone.length() != 13 || !phone.substring(0, 4).equals("+998"))
                throw new ArithmeticException(msg.errorPhone(user.getLang()));
            user.setPhone(phone);
            userService.save(user);
            String message = msg.menu(user.getLang());
            ReplyKeyboardMarkup markup = kyb.menu(user.getLang());
            user.setEventCode("menu");
            bot.sendMessage(user.getChatId(), message, markup);
            userService.save(user);
        } catch (ArithmeticException e) {
            try {
                bot.sendMessage(user.getChatId(), e.getMessage());
            } catch (Exception ex) {
                log.error(ex);
            }
        }
    }
}
