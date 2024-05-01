package uz.zafar.primetech.bot.superAdmin;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import uz.zafar.primetech.bot.TelegramBot;
import uz.zafar.primetech.db.domain.User;
import uz.zafar.primetech.db.service.UserService;

import java.util.List;

@Log4j2

@Controller
@RequiredArgsConstructor
public class SuperAdmin {
    @Lazy
    private final TelegramBot bot;

    @Lazy
    private final UserService userService;
    @Lazy
    private final SuperAdminKyb kyb;
    @Lazy
    private final SuperAdminMsg msg;
    private int size = 20;

    public void menu(User user, Update update) {
        if (update.hasCallbackQuery()) {
            String data = update.getCallbackQuery().getData();
            if (user.getEventCode().equals("search by username")) {
                searchByUsername(update.getCallbackQuery(), user);
            }
            if (user.getEventCode().equals("search by nickname")) {
                searchByNickname(update.getCallbackQuery(), user);
            }
            if (user.getEventCode().equals("menu")) {
                searchByAll(update.getCallbackQuery(),user);
            }
            if (user.getEventCode().equals("search by admin")){
                searchByAdmin(update.getCallbackQuery(),user);
            }
        } else if (update.hasMessage()) {
            Message message = update.getMessage();
            if (message.hasText()) {
                String text = message.getText();
                if (text.equals("/start")) {
                    startCommand(user);
                } else {
                    if (user.getEventCode().equals("menu")) {
                        mainMenu(user, text);
                    } else if (user.getEventCode().equals("search by username")) {
                        searchByUsername(update.getMessage().getText(), user);
                    } else if (user.getEventCode().equals("search by nickname")) {
                        searchByNickname(update.getMessage().getText(), user);
                    } else if (user.getEventCode().equals("search by id")) {
                        searchById(update.getMessage().getText(), user);
                    } else if (user.getEventCode().equals("search by chat id")) {
                        searchByChatId(update.getMessage().getText(), user);
                    } else if (user.getEventCode().equals("is success admin")) {
                        successAdmin(user, text);
                    } else if (user.getEventCode().equals("is remove admin")) {
                        removeAdmin(user, text);
                    }
                }
            }
        }
    }

    public void searchByUsername(String text, User user) {
        if (text.equals("\uD83D\uDD19 Orqaga qaytish")) {
            startCommand(user);
        } else {
            Page<User> userPage = userService.findAllByUsername(text, user.getPage(), size).getData();

            if (userPage.getContent().isEmpty()) {
                bot.sendMessage(user.getChatId(), "Bunday usernamega ega foydalanuvchi mavjud emas\n\nSiz tergan username: " + text, kyb.setKeyboard("\uD83D\uDD19 Orqaga qaytish"));
            } else {
                user.setPage(0);
                bot.sendMessage(user.getChatId(),
                        text + " ga o'xshash username ga ega foydlanuvchilar",
                        kyb.getUsers(userPage.getContent(), userPage.getTotalPages(), user.getPage())
                );
                user.setHelper(text);
                userService.save(user);
            }

        }
    }

    public void searchByNickname(String text, User user) {
        if (text.equals("\uD83D\uDD19 Orqaga qaytish")) {
            startCommand(user);
        } else {
            Page<User> userPage = userService.findAllByNickname(text, user.getPage(), size).getData();

            if (userPage.getContent().isEmpty()) {
                bot.sendMessage(user.getChatId(), "Bunday nickname ega foydalanuvchi mavjud emas\n\nSiz tergan username: " + text, kyb.setKeyboard("\uD83D\uDD19 Orqaga qaytish"));
            } else {
                user.setPage(0);
                bot.sendMessage(user.getChatId(),
                        text + " ga o'xshash nickname ga ega foydlanuvchilar",
                        kyb.getUsers(userPage.getContent(), userPage.getTotalPages(), user.getPage())
                );
                user.setHelper(text);
                userService.save(user);
            }

        }
    }

    public void searchByUsername(CallbackQuery callbackQuery, User user) {

        String data = callbackQuery.getData();

        if (data.equals("next")) {
            user.setPage(user.getPage() + 1);
            userService.save(user);
        } else if (data.equals("back")) {
            user.setPage(user.getPage() - 1);
            userService.save(user);
        } else {
            User user1 = userService.findById(Long.valueOf(data)).getData();
            String role = user1.getRole();
            user.setHelperProductId(user1.getId());

            if (role.equals("admin")) {
                bot.deleteMessage(user.getChatId(), callbackQuery.getMessage().getMessageId());

                String about = "";
                about = about.concat("To'liq ismi: " + user1.getNickname() + "\n");
                about = about.concat("Username: " + user1.getUsername() + "\n");
                String a = about.concat("""
                        <i>Foydalanuvchi bilan bog'lanish:</i> <a href="tg://user?id=%d">%s</a>""".formatted(user1.getChatId(), user1.getNickname()));
                about = about.concat(a);
                about = about.concat("\uD83C\uDD94 : " + user1.getId() + "\n");
                about = about.concat("\uD83C\uDD94 chat id: " + user1.getChatId() + "\n");
                about = about.concat("Role: " + role + "\n");

                bot.sendMessage(user.getChatId(), about + "\n\nQuyidagi foydalanuvchini adminlikdan olmoqchimisiz",
                        kyb.setKeyboards(new String[]{
                                "✅ Ha", "❌ Yo'q", "\uD83D\uDD19 Orqaga qaytish"
                        }, 2));
                user.setEventCode("is remove admin");
                user.setHelperType("username");
                userService.save(user);
            } else {
                bot.deleteMessage(user.getChatId(), callbackQuery.getMessage().getMessageId());

                String about = "";
                about = about.concat("To'liq ismi: " + user1.getNickname() + "\n");
                about = about.concat("Username: " + user1.getUsername() + "\n");
                String a = about.concat("""
                        <i>Foydalanuvchi bilan bog'lanish:</i> <a href="tg://user?id=%d">%s</a>\n""".formatted(user1.getChatId(), user1.getNickname()));
                about = about.concat(a);
                about = about.concat("\uD83C\uDD94 : " + user1.getId() + "\n");
                about = about.concat("\uD83C\uDD94 chat id: " + user1.getChatId() + "\n");
                about = about.concat("Role: " + role + "\n");
                user.setHelperType("username");
                bot.sendMessage(user.getChatId(), about + "\n\nQuyidagi foydalanuvchini admin qilmoqchimisiz",
                        kyb.setKeyboards(new String[]{
                                "✅ Ha", "❌ Yo'q", "\uD83D\uDD19 Orqaga qaytish"
                        }, 2));
                user.setEventCode("is success admin");
                userService.save(user);
            }
            return;
        }
        int page = user.getPage();
        Page<User> userPage = userService.findAllByUsername(user.getHelper(), user.getPage(), size).getData();
        List<User> list = userPage.getContent();
        bot.editMessageText(user.getChatId(),
                user.getHelper() + " ga o'xshash username ga ega foydlanuvchilar",
                callbackQuery.getMessage().getMessageId(), kyb.getUsers(list, userPage.getTotalPages(), page));

    }

    public void searchByNickname(CallbackQuery callbackQuery, User user) {

        String data = callbackQuery.getData();

        if (data.equals("next")) {
            user.setPage(user.getPage() + 1);
            userService.save(user);
        } else if (data.equals("back")) {
            user.setPage(user.getPage() - 1);
            userService.save(user);
        } else {
            User user1 = userService.findById(Long.valueOf(data)).getData();
            String role = user1.getRole();
            user.setHelperProductId(user1.getId());

            if (role.equals("admin")) {
                bot.deleteMessage(user.getChatId(), callbackQuery.getMessage().getMessageId());
                String about = "";
                about = about.concat("To'liq ismi: " + user1.getNickname() + "\n");
                about = about.concat("Username: " + user1.getUsername() + "\n");
                String a = about.concat("""
                        <i>Foydalanuvchi bilan bog'lanish:</i> <a href="tg://user?id=%d">%s</a>""".formatted(user1.getChatId(), user1.getNickname()));
                about = about.concat(a);
                about = about.concat("\uD83C\uDD94 : " + user1.getId() + "\n");
                about = about.concat("\uD83C\uDD94 chat id: " + user1.getChatId() + "\n");
                about = about.concat("Role: " + role + "\n");

                bot.sendMessage(user.getChatId(), about + "\n\nQuyidagi foydalanuvchini adminlikdan olmoqchimisiz",
                        kyb.setKeyboards(new String[]{
                                "✅ Ha", "❌ Yo'q", "\uD83D\uDD19 Orqaga qaytish"
                        }, 2));
                user.setEventCode("is remove admin");
                user.setHelperType("nickname");
                userService.save(user);
            } else {
                bot.deleteMessage(user.getChatId(), callbackQuery.getMessage().getMessageId());

                String about = "";
                about = about.concat("To'liq ismi: " + user1.getNickname() + "\n");
                about = about.concat("Username: " + user1.getUsername() + "\n");
                String a = about.concat("""
                        <i>Foydalanuvchi bilan bog'lanish:</i> <a href="tg://user?id=%d">%s</a>\n""".formatted(user1.getChatId(), user1.getNickname()));
                about = about.concat(a);
                about = about.concat("\uD83C\uDD94 : " + user1.getId() + "\n");
                about = about.concat("\uD83C\uDD94 chat id: " + user1.getChatId() + "\n");
                about = about.concat("Role: " + role + "\n");
                user.setHelperType("nickname");
                bot.sendMessage(user.getChatId(), about + "\n\nQuyidagi foydalanuvchini admin qilmoqchimisiz",
                        kyb.setKeyboards(new String[]{
                                "✅ Ha", "❌ Yo'q", "\uD83D\uDD19 Orqaga qaytish"
                        }, 2));
                user.setEventCode("is success admin");
                userService.save(user);
            }
            return;
        }
        int page = user.getPage();
        Page<User> userPage = userService.findAllByNickname(user.getHelper(), user.getPage(), size).getData();
        List<User> list = userPage.getContent();
        bot.editMessageText(user.getChatId(),
                user.getHelper() + " ga o'xshash username ga ega foydlanuvchilar",
                callbackQuery.getMessage().getMessageId(), kyb.getUsers(list, userPage.getTotalPages(), page));

    }
    public void searchByAll(CallbackQuery callbackQuery, User user) {
        String data = callbackQuery.getData();
        if (data.equals("next")) {
            user.setPage(user.getPage() + 1);
            userService.save(user);
        } else if (data.equals("back")) {
            user.setPage(user.getPage() - 1);
            userService.save(user);
        } else {
            User user1 = userService.findById(Long.valueOf(data)).getData();
            String role = user1.getRole();
            user.setHelperProductId(user1.getId());

            if (role.equals("admin")) {
                bot.deleteMessage(user.getChatId(), callbackQuery.getMessage().getMessageId());
                String about = "";
                about = about.concat("To'liq ismi: " + user1.getNickname() + "\n");
                about = about.concat("Username: " + user1.getUsername() + "\n");
                String a = about.concat("""
                        <i>Foydalanuvchi bilan bog'lanish:</i> <a href="tg://user?id=%d">%s</a>""".formatted(user1.getChatId(), user1.getNickname()));
                about = about.concat(a);
                about = about.concat("\uD83C\uDD94 : " + user1.getId() + "\n");
                about = about.concat("\uD83C\uDD94 chat id: " + user1.getChatId() + "\n");
                about = about.concat("Role: " + role + "\n");

                bot.sendMessage(user.getChatId(), about + "\n\nQuyidagi foydalanuvchini adminlikdan olmoqchimisiz",
                        kyb.setKeyboards(new String[]{
                                "✅ Ha", "❌ Yo'q", "\uD83D\uDD19 Orqaga qaytish"
                        }, 2));
                user.setEventCode("is remove admin");
                user.setHelperType("nickname");
                userService.save(user);
            } else {
                bot.deleteMessage(user.getChatId(), callbackQuery.getMessage().getMessageId());

                String about = "";
                about = about.concat("To'liq ismi: " + user1.getNickname() + "\n");
                about = about.concat("Username: " + user1.getUsername() + "\n");
                String a = about.concat("""
                        <i>Foydalanuvchi bilan bog'lanish:</i> <a href="tg://user?id=%d">%s</a>\n""".formatted(user1.getChatId(), user1.getNickname()));
                about = about.concat(a);
                about = about.concat("\uD83C\uDD94 : " + user1.getId() + "\n");
                about = about.concat("\uD83C\uDD94 chat id: " + user1.getChatId() + "\n");
                about = about.concat("Role: " + role + "\n");

                bot.sendMessage(user.getChatId(), about + "\n\nQuyidagi foydalanuvchini admin qilmoqchimisiz",
                        kyb.setKeyboards(new String[]{
                                "✅ Ha", "❌ Yo'q", "\uD83D\uDD19 Orqaga qaytish"
                        }, 2));
                user.setEventCode("is success admin");
                userService.save(user);
            }
            return;
        }
        int page = user.getPage();
        Page<User> userPage = userService.getAll(user.getPage(), size).getData();
        List<User> list = userPage.getContent();
        bot.editMessageText(user.getChatId(),
                "Barcha foydlanuvchilar",
                callbackQuery.getMessage().getMessageId(), kyb.getUsers(list, userPage.getTotalPages(), page));

    }
    public void searchByAdmin(CallbackQuery callbackQuery, User user) {
        String data = callbackQuery.getData();
        if (data.equals("next")) {
            user.setPage(user.getPage() + 1);
            userService.save(user);
        } else if (data.equals("back")) {
            user.setPage(user.getPage() - 1);
            userService.save(user);
        } else {
            User user1 = userService.findById(Long.valueOf(data)).getData();
            String role = user1.getRole();
            user.setHelperProductId(user1.getId());

            if (role.equals("admin")) {
                bot.deleteMessage(user.getChatId(), callbackQuery.getMessage().getMessageId());
                String about = "";
                about = about.concat("To'liq ismi: " + user1.getNickname() + "\n");
                about = about.concat("Username: " + user1.getUsername() + "\n");
                String a = about.concat("""
                        <i>Foydalanuvchi bilan bog'lanish:</i> <a href="tg://user?id=%d">%s</a>""".formatted(user1.getChatId(), user1.getNickname()));
                about = about.concat(a);
                about = about.concat("\uD83C\uDD94 : " + user1.getId() + "\n");
                about = about.concat("\uD83C\uDD94 chat id: " + user1.getChatId() + "\n");
                about = about.concat("Role: " + role + "\n");

                bot.sendMessage(user.getChatId(), about + "\n\nQuyidagi foydalanuvchini adminlikdan olmoqchimisiz",
                        kyb.setKeyboards(new String[]{
                                "✅ Ha", "❌ Yo'q", "\uD83D\uDD19 Orqaga qaytish"
                        }, 2));
                user.setEventCode("is remove admin");
                user.setHelperType("nickname");
                userService.save(user);
            } else {
                bot.deleteMessage(user.getChatId(), callbackQuery.getMessage().getMessageId());

                String about = "";
                about = about.concat("To'liq ismi: " + user1.getNickname() + "\n");
                about = about.concat("Username: " + user1.getUsername() + "\n");
                String a = about.concat("""
                        <i>Foydalanuvchi bilan bog'lanish:</i> <a href="tg://user?id=%d">%s</a>\n""".formatted(user1.getChatId(), user1.getNickname()));
                about = about.concat(a);
                about = about.concat("\uD83C\uDD94 : " + user1.getId() + "\n");
                about = about.concat("\uD83C\uDD94 chat id: " + user1.getChatId() + "\n");
                about = about.concat("Role: " + role + "\n");

                bot.sendMessage(user.getChatId(), about + "\n\nQuyidagi foydalanuvchini admin qilmoqchimisiz",
                        kyb.setKeyboards(new String[]{
                                "✅ Ha", "❌ Yo'q", "\uD83D\uDD19 Orqaga qaytish"
                        }, 2));
                user.setEventCode("is success admin");
                userService.save(user);
            }
            return;
        }
        int page = user.getPage();
        Page<User> userPage = userService.findByRole("admin",user.getPage(), size).getData();
        List<User> list = userPage.getContent();
        bot.editMessageText(user.getChatId(),
                "Barcha foydlanuvchilar",
                callbackQuery.getMessage().getMessageId(), kyb.getUsers(list, userPage.getTotalPages(), page));

    }

    public void removeAdmin(User user, String text) {
        if (text.equals("✅ Ha")) {
            User user1 = userService.findById(user.getHelperProductId()).getData();
            bot.sendMessage(user1.getChatId(), "Siz adminlikdan olindingiz");
            user1.setRole("user");
            userService.save(user1);
            bot.sendMessage(user.getChatId(), "Ushbu foydalanuvchi haqiqatdan ham user role ga o'tkazildi");
            startCommand(user);
        } else if (text.equals("❌ Yo'q")) {
            bot.sendMessage(user.getChatId(), "Ushbu foydalanuvchi user role ga o'tkazilmadi");
            startCommand(user);
        } else if (text.equals("\uD83D\uDD19 Orqaga qaytish")) {
            if (user.getHelperType().equals("username")) {
                Page<User> userPage = userService.findAllByUsername(user.getHelper(), user.getPage(), size).getData();
                if (userPage.getContent().isEmpty()) {
                    bot.sendMessage(user.getChatId(), "Bunday usernamega ega foydalanuvchi mavjud emas\n\nSiz tergan username: " + text, kyb.setKeyboard("\uD83D\uDD19 Orqaga qaytish"));
                } else {

                    bot.sendMessage(user.getChatId(),
                            text + " ga o'xshash username ga ega foydlanuvchilar",
                            kyb.getUsers(userPage.getContent(), userPage.getTotalPages(), user.getPage())
                    );
                    user.setHelper(text);
                    userService.save(user);
                }
            }
            if (user.getHelperType().equals("nickname")) {
                Page<User> userPage = userService.findAllByNickname(user.getHelper(), user.getPage(), size).getData();
                if (userPage.getContent().isEmpty()) {
                    bot.sendMessage(user.getChatId(), "Bunday usernamega ega foydalanuvchi mavjud emas\n\nSiz tergan username: " + text, kyb.setKeyboard("\uD83D\uDD19 Orqaga qaytish"));
                } else {

                    bot.sendMessage(user.getChatId(),
                            text + " ga o'xshash username ga ega foydlanuvchilar",
                            kyb.getUsers(userPage.getContent(), userPage.getTotalPages(), user.getPage())
                    );
                    user.setHelper(text);
                    userService.save(user);
                }
            }

        } else bot.sendMessage(user.getChatId(), "❌ Tugmalardan foydalanig");
    }

    public void successAdmin(User user, String text) {
        if (text.equals("✅ Ha")) {
            User user1 = userService.findById(user.getHelperProductId()).getData();
            user1.setRole("admin");
            userService.save(user1);
            bot.sendMessage(user1.getChatId(), "Tabriklaymiz\n Siz admin qilindingiz");
            bot.sendMessage(user.getChatId(), "Ushbu foydalanuvchi haqiqatdan ham admin role ga o'tkazildi");
            startCommand(user);
        } else if (text.equals("❌ Yo'q")) {
            bot.sendMessage(user.getChatId(), "Ushbu foydalanuvchi admin role ga o'tkazilmadi");
            startCommand(user);
        } else if (text.equals("\uD83D\uDD19 Orqaga qaytish")) {
            if (user.getHelperType().equals("username")) {
                user.setEventCode("search by username");
                Page<User> userPage = userService.findAllByUsername(user.getHelper(), user.getPage(), size).getData();
                if (userPage.getContent().isEmpty()) {
                    bot.sendMessage(user.getChatId(), "Bunday usernamega ega foydalanuvchi mavjud emas\n\nSiz tergan username: " + text, kyb.setKeyboard("\uD83D\uDD19 Orqaga qaytish"));
                } else {

                    bot.sendMessage(user.getChatId(),
                            user.getHelper() + " ga o'xshash username ga ega foydlanuvchilar",
                            kyb.getUsers(userPage.getContent(), userPage.getTotalPages(), user.getPage())
                    );
                    userService.save(user);
                }
                return;
            }
            if (user.getHelperType().equals("nickname")) {
                user.setEventCode("search by nickname");
                Page<User> userPage = userService.findAllByNickname(user.getHelper(), user.getPage(), size).getData();
                if (userPage.getContent().isEmpty()) {
                    bot.sendMessage(user.getChatId(), "Bunday nickname ega foydalanuvchi mavjud emas\n\nSiz tergan username: " + text, kyb.setKeyboard("\uD83D\uDD19 Orqaga qaytish"));
                } else {

                    bot.sendMessage(user.getChatId(),
                            user.getHelper() + " ga o'xshash nickname ga ega foydlanuvchilar",
                            kyb.getUsers(userPage.getContent(), userPage.getTotalPages(), user.getPage())
                    );
                    userService.save(user);
                }
            }
        } else bot.sendMessage(user.getChatId(), "❌ Tugmalardan foydalanig");
    }

    public void getUsers(User user) {
        user.setPage(0);
        user.setEventCode("get users");
        userService.save(user);
        int page = user.getPage();
        Page<User> userPage = userService.getAll(page, size).getData();
        List<User> list = userPage.getContent();
        bot.sendMessage(
                user.getChatId(), "Foydalanuvchilar ro'yxati",
                kyb.getUsers(list, userPage.getTotalPages(), page)
        );
    }

    public void startCommand(User user) {
        user.setPage(0);
        userService.save(user);
        bot.sendMessage(user.getChatId(), "Quyidagilardan birini tanlang", kyb.menu());
        user.setEventCode("menu");
        userService.save(user);
    }

    public void mainMenu(User user, String text) {
        String[] menus = {
                "Foydalanuvchilarni username orqali izlash",
                "Foydalanuvchilarni nickname orqali izlash",
                "Foydalanuvchilarni chat id orqali izlash",
                "Foydalanuvchilarni id orqali izlash",
                "Barcha foydalanuvchilar ro'yxati",
                "Adminlar ro'yxati"
        };
        if (text.equals(menus[0])) {
            bot.sendMessage(user.getChatId(), "Biror bir username kiriting", kyb.setKeyboard("\uD83D\uDD19 Orqaga qaytish"));
            user.setEventCode("search by username");
            userService.save(user);
        } else if (text.equals(menus[1])) {
            bot.sendMessage(user.getChatId(), "Biror bir nickname kiriting", kyb.setKeyboard("\uD83D\uDD19 Orqaga qaytish"));
            user.setEventCode("search by nickname");
            userService.save(user);
        } else if (text.equals(menus[2])) {
            bot.sendMessage(user.getChatId(), "Biror bir chat id kiriting", kyb.setKeyboard("\uD83D\uDD19 Orqaga qaytish"));
            user.setEventCode("search by chat id");
            userService.save(user);
        } else if (text.equals(menus[3])) {
            bot.sendMessage(user.getChatId(), "Biror bir id kiriting", kyb.setKeyboard("\uD83D\uDD19 Orqaga qaytish"));
            user.setEventCode("search by id");
            userService.save(user);
        }else if (text.equals(menus[4])) {

            Page<User> userPage = userService.getAll(user.getPage(), size).getData();
            if (userPage.getContent().isEmpty()) {
                bot.sendMessage(user.getChatId(), "Bunday nickname ega foydalanuvchi mavjud emas\n\nSiz tergan username: " + text, kyb.setKeyboard("\uD83D\uDD19 Orqaga qaytish"));
            } else {
                user.setPage(0);

                bot.sendMessage(user.getChatId(),
                       "Barcha foydlanuvchilar",
                        kyb.getUsers(userPage.getContent(), userPage.getTotalPages(), user.getPage())
                );
                user.setHelper(text);
                userService.save(user);
                userService.save(user);
            }



        }else if (text.equals(menus[5])) {
            Page<User> userPage = userService.findByRole("admin",user.getPage(), size).getData();
            if (userPage.getContent().isEmpty()) {
                bot.sendMessage(user.getChatId(), "Bunday nickname ega foydalanuvchi mavjud emas\n\nSiz tergan username: " + text, kyb.setKeyboard("\uD83D\uDD19 Orqaga qaytish"));
            } else {
                user.setPage(0);
                bot.sendMessage(user.getChatId(), "Quyidagilardan birini tanlang" , kyb.setKeyboard("\uD83D\uDD19 Orqaga qaytish"));
                bot.sendMessage(user.getChatId(),
                        "Adminlaring ro'yxati",
                        kyb.getUsers(userPage.getContent(), userPage.getTotalPages(), user.getPage())
                );

                user.setEventCode("search by admin");
                userService.save(user);
            }

        } else bot.sendMessage(user.getChatId(), "error");

    }

    public void searchById(String text, User user) {
        if (text.equals("\uD83D\uDD19 Orqaga qaytish")) {
            startCommand(user);
        } else {
            try {
                User user1 = userService.findById(Long.valueOf(text)).getData();
                if (user1 != null) {
                    String about = "";
                    about = about.concat("To'liq ismi: " + user1.getNickname() + "\n");
                    about = about.concat("Username: " + user1.getUsername() + "\n");
                    String a = about.concat("""
                            <i>Foydalanuvchi bilan bog'lanish:</i> <a href="tg://user?id=%d">%s</a>\n""".formatted(user1.getChatId(), user1.getNickname()));
                    about = about.concat(a);
                    about = about.concat("\uD83C\uDD94 : " + user1.getId() + "\n");
                    about = about.concat("\uD83C\uDD94 chat id: " + user1.getChatId() + "\n");
                    about = about.concat("Role: " + user1.getRole() + "\n");
                    user.setHelperType("");
                    bot.sendMessage(user.getChatId(), about + "\n\nQuyidagi foydalanuvchini admin qilmoqchimisiz",
                            kyb.setKeyboards(new String[]{
                                    "✅ Ha", "❌ Yo'q"
                            }, 2));
                    user.setEventCode("is success admin");
                    userService.save(user);
                } else {
                    bot.sendMessage(user.getChatId(), "Bunaqa id li foydallauvchi topilmadi", kyb.setKeyboard("\uD83D\uDD19 Orqaga qaytish"));
                }
            } catch (NumberFormatException e) {
                log.error(e);
                bot.sendMessage(user.getChatId(), "Harf kiritish mumkin emas");
            }
        }
    }

    public void searchByChatId(String text, User user) {
        if (text.equals("\uD83D\uDD19 Orqaga qaytish")) {
            startCommand(user);
        } else {
            try {
                User user1 = userService.findByChatId(Long.valueOf(text)).getData();
                if (user1 != null) {
                    String about = "";
                    about = about.concat("To'liq ismi: " + user1.getNickname() + "\n");
                    about = about.concat("Username: " + user1.getUsername() + "\n");
                    String a = about.concat("""
                            <i>Foydalanuvchi bilan bog'lanish:</i> <a href="tg://user?id=%d">%s</a>\n""".formatted(user1.getChatId(), user1.getNickname()));
                    about = about.concat(a);
                    about = about.concat("\uD83C\uDD94 : " + user1.getId() + "\n");
                    about = about.concat("\uD83C\uDD94 chat id: " + user1.getChatId() + "\n");
                    about = about.concat("Role: " + user1.getRole() + "\n");
                    user.setHelperType("");
                    bot.sendMessage(user.getChatId(), about + "\n\nQuyidagi foydalanuvchini admin qilmoqchimisiz",
                            kyb.setKeyboards(new String[]{
                                    "✅ Ha", "❌ Yo'q"
                            }, 2));
                    user.setEventCode("is success admin");
                    userService.save(user);
                } else {
                    bot.sendMessage(user.getChatId(), "Bunaqa is li foydallauvchi topilmadi");
                }
            } catch (NumberFormatException e) {
                log.error(e);
                bot.sendMessage(user.getChatId(), "Harf kiritish mumkin emas");
            }
        }
    }
}
