package uz.zafar.primetech.bot.admins;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Controller;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Location;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import uz.zafar.primetech.bot.TelegramBot;
import uz.zafar.primetech.bot.users.kyb.order.OrderKybMsg;
import uz.zafar.primetech.db.domain.Branch;
import uz.zafar.primetech.db.domain.User;
import uz.zafar.primetech.db.repositories.BranchRepository;
import uz.zafar.primetech.db.service.CategoryService;
import uz.zafar.primetech.db.service.ProductService;
import uz.zafar.primetech.db.service.UserService;
import uz.zafar.primetech.dto.CallbackData;
import uz.zafar.primetech.json.read.GetLocation;

import java.util.ArrayList;
import java.util.List;

@Log4j2

@Controller
@RequiredArgsConstructor
public class Branches {
    @Lazy
    private final TelegramBot bot;
    @Lazy
    private final UserService userService;
    @Lazy
    private final AdminKyb kyb;
    @Lazy
    private final AdminMsg msg;
    @Lazy
    private final BranchRepository branchRepository;

    public void menu(User user, Update update) {
        Long chatId = user.getChatId();
        String eventCode = user.getEventCode2();
        if (update.hasMessage()) {
            Message message = update.getMessage();
            if (message.hasText()) {
                String text = message.getText();
                if (
                        eventCode.equals("edit name") ||
                                eventCode.equals("edit time last") ||
                                eventCode.equals("edit time begin") ||
                                eventCode.equals("edit landmark") ||
                                eventCode.equals("edit phone")
                ) {
                    edit(user, text, eventCode);
                } else if (eventCode.equals("get all branches")) {
                    if (text.equals(OrderKybMsg.back("uz"))) {
                        bot.sendMessage(user.getChatId(), msg.menu, kyb.menu());
                        user.setEventCode("menu");
                        userService.save(user);
                    } else if (text.equals("Filial qo'shish")) {
                        setAddBranch(chatId, user);
                    } else {
                        try {
                            Branch branch = branchRepository.findByName(text);

                            if (branch == null) {
                                List<Branch> all = branchRepository.findAll();
                                String[] branches = new String[all.size() + 2];
                                branches[0] = (OrderKybMsg.back("uz"));
                                branches[1] = ("Filial qo'shish");
                                for (int i = 0; i < all.size(); i++) {
                                    branches[i + 2] = all.get(i).getName();
                                }
                                bot.sendMessage(chatId, msg.errorMessage, kyb.setKeyboards(branches, 2));
                            } else {
                                String msg;
                                msg = """
                                        Filialning nomi: %s
                                        Filialning orientri: %s
                                        Filialning ish vaqti: %d:00 %d:00
                                        Filial bilan bog'lanish: %s
                                        Filial manzili: %s
                                        """.formatted(
                                        branch.getName(),
                                        branch.getLandmark(),
                                        branch.getBegin(), branch.getLast(),
                                        branch.getPhone(),
                                        branch.getAddress()
                                );
                                user.setHelperCategoryId(branch.getId());
                                userService.save(user);
                                bot.sendMessage(chatId, "Quyidagilardan birini tanlang", kyb.setKeyboards(new String[]{
                                        "Filial qo'shish", OrderKybMsg.back("uz")
                                }, 2));
                                bot.sendMessage(chatId, msg, kyb.crudBranch());
                                eventCode(user, "branches crud");
                            }
                        } catch (Exception e) {
                            log.error(e);
                        }
                    }
                } else if (eventCode.equals("get phone")) {
                    getPhone(user, text);
                } else if (eventCode.equals("add branch")) {
                    addBranch(user, text);
                } else if (eventCode.equals("get time begin")) {
                    getTimeBegin(user, text);
                } else if (eventCode.equals("get time last")) {
                    getTimeLast(user, text);
                } else if (eventCode.equals("get landmark")) {
                    getLandMark(user, text);
                } else if (eventCode.equals("get location")) {
                    bot.sendMessage(chatId, "Lokatsiya yuborishingiz kerak qaytadan yuboring");
                } else if (eventCode.equals("branches crud")) {
                    if (text.equals(OrderKybMsg.back("uz"))) {
                        List<Branch> all = branchRepository.findAll();
                        String[] branches = new String[all.size() + 2];
                        branches[0] = (OrderKybMsg.back("uz"));
                        branches[1] = ("Filial qo'shish");
                        for (int i = 0; i < all.size(); i++) {
                            branches[i + 2] = all.get(i).getName();
                        }
                        bot.sendMessage(chatId, "Quyidagilardan birini tanlang", kyb.setKeyboards(branches, 2));
                        eventCode(user, "get all branches");
                    } else if (text.equals("Filial qo'shish")) {
                        setAddBranch(chatId, user);
                    } else {
                        bot.sendMessage(chatId, msg.errorMessage, message.getMessageId());
                    }
                }
            } else if (message.hasLocation()) {
                if (eventCode.equals("get location")) {
                    getLocation(user, message.getLocation());
                }
            }
        } else if (update.hasCallbackQuery()) {
            CallbackQuery callbackQuery = update.getCallbackQuery();
            String data = callbackQuery.getData();
            int messageId = callbackQuery.getMessage().getMessageId();
            if (eventCode.equals("branches crud")) {
                if (data.equals("delete")) {
                    bot.editMessageText(chatId, "Haqiqatdan ham ushbu filialni o'chirmoqchimisiz", messageId, kyb.isSuccess());
                } else if (data.equals("edit")) {
                    Branch branch = branchRepository.findById(user.getHelperCategoryId()).get();
                    String msg1 = """
                            Filialning nomi: %s
                            Filialning orientri: %s
                            Filialning ish vaqti: %d:00 %d:00
                            Filial bilan bog'lanish: %s
                            Filial manzili: %s
                            """.formatted(
                            branch.getName(),
                            branch.getLandmark(),
                            branch.getBegin(), branch.getLast(),
                            branch.getPhone(),
                            branch.getAddress()
                    );
                    List<CallbackData> crudData = new ArrayList<>();
                    crudData.add(new CallbackData("Nomini o'zgartirish", "edit name"));
                    crudData.add(new CallbackData("Orientrni o'zgartirish", "edit landmark"));
                    crudData.add(new CallbackData("Telefon raqamni o'zgartirish", "edit phone"));
                    crudData.add(new CallbackData("Ish boshlanish vaqtini o'zgartirish", "edit time begin"));
                    crudData.add(new CallbackData("Ish tugash vaqtini o'gartirish", "edit time last"));
                    crudData.add(new CallbackData(OrderKybMsg.back("uz"), "back"));

                    bot.editMessageText(chatId, msg1 + "\n\n\nQuyidagilardan birini tanlang", messageId, kyb.setKeyboards(crudData, 1));
                } else if (data.equals("yes delete")) {
                    Branch branch = branchRepository.findById(user.getHelperCategoryId()).get();
                    branchRepository.deleteById(branch.getId());
                    String msg1 = """
                            Filialning nomi: %s
                            Filialning orientri: %s
                            Filialning aloqa markazi: %s
                            Filialning ish vaqti: %d:00 %d:00
                            Filial bilan bog'lanish: %s
                            Filial manzili: %s
                            """.formatted(
                            branch.getName(),
                            branch.getLandmark(),
                            branch.getPhone(),
                            branch.getBegin(), branch.getLast(),
                            branch.getPhone(),
                            branch.getAddress()
                    );
                    List<Branch> all = branchRepository.findAll();
                    String[] branches = new String[all.size() + 2];
                    branches[0] = (OrderKybMsg.back("uz"));
                    branches[1] = ("Filial qo'shish");
                    bot.alertMessage(callbackQuery, "❌ Quyidagi filial o'chirildi");
                    bot.deleteMessage(chatId, messageId);
                    for (int i = 0; i < all.size(); i++) {
                        branches[i + 2] = all.get(i).getName();
                    }
                    eventCode(user, "get all branches");
                    bot.sendMessage(chatId, msg1 + "\n\nUshbu filial o'chirib tashlandi\n\nQuyidagilardan birini tanlag", kyb.setKeyboards(branches, 2));
                } else if (data.equals("no delete")) {
                    Branch branch = branchRepository.findById(user.getHelperCategoryId()).get();

                    String msg;
                    msg = """
                            Filialning nomi: %s
                            Filialning orientri: %s
                            Filialning aloqa markazi: %s
                            Filialning ish vaqti: %d:00 %d:00
                            Filial bilan bog'lanish: %s
                            Filial manzili: %s
                            """.formatted(
                            branch.getName(),

                            branch.getLandmark(),
                            branch.getPhone(),
                            branch.getBegin(), branch.getLast(),
                            branch.getPhone(),
                            branch.getAddress()
                    );

                    user.setHelperCategoryId(branch.getId());
                    userService.save(user);
                    bot.alertMessage(callbackQuery, "❌ Quyidagi filial o'chirilmadi");
                    bot.deleteMessage(chatId, messageId);
                    bot.sendMessage(chatId, "Quyidagi filial o'chrilmadi \n\n" + "Quyidagilardan birini tanlang", kyb.setKeyboards(new String[]{
                            "Filial qo'shish", OrderKybMsg.back("uz")
                    }, 2));
                    bot.sendMessage(chatId, msg, kyb.crudBranch());
                    eventCode(user, "branches crud");
                } else {
                    if (!data.equals("back")) {
                        bot.editMessageText(chatId, "Yangi " + data + "ni kiriting", messageId);
                        eventCode(user, data);
                    } else {
                        Branch branch = branchRepository.findById(user.getHelperCategoryId()).get();
                        String msg;
                        msg = """
                                Filialning nomi: %s
                                Filialning orientri: %s
                                Filialning ish vaqti: %d:00 %d:00
                                Filial bilan bog'lanish: %s
                                Filial manzili: %s
                                """.formatted(
                                branch.getName(),
                                branch.getLandmark(),
                                branch.getBegin(), branch.getLast(),
                                branch.getPhone(),
                                branch.getAddress()
                        );
                        user.setHelperCategoryId(branch.getId());
                        userService.save(user);
                        bot.editMessageText(chatId, msg, messageId,kyb.crudBranch());
                        eventCode(user, "branches crud");
                    }
                }
            }
        }
    }


    public void edit(User user, String text, String eventCode) {
        Branch branch = branchRepository.findById(user.getHelperCategoryId()).get();

        if (eventCode.equals("edit name"))
            branch.setName(text);
        else if (eventCode.equals("edit landmark"))
            branch.setLandmark(text);
        else if (eventCode.equals("edit phone")) {
            boolean success = getPhone1(user, text, branch);
            if (!success) return;
        } else if (eventCode.equals("edit time begin")) {
            try {
                branch.setBegin(Integer.valueOf(text));
            } catch (NumberFormatException e) {
                bot.sendMessage(user.getChatId(), "Sonda kiritishingiz kerak misol uchun 08:00 bolsa 8 sonini kiritishingiz kerak");
                return;
            }
        } else if (eventCode.equals("edit time last")) {
            try {
                branch.setLast(Integer.valueOf(text));
            } catch (NumberFormatException e) {
                bot.sendMessage(user.getChatId(), "Sonda kiritishingiz kerak misol uchun 18:00 bolsa 18 sonini kiritishingiz kerak");
                return;
            }
        } else return;
        branchRepository.save(branch);
        branch = branchRepository.findById(user.getHelperCategoryId()).get();
        String msg1 = """
                Filialning nomi: %s
                Filialning orientri: %s
                Filialning ish vaqti: %d:00 %d:00
                Filial bilan bog'lanish: %s
                Filial manzili: %s
                """.formatted(
                branch.getName(),
                branch.getLandmark(),
                branch.getBegin(), branch.getLast(),
                branch.getPhone(),
                branch.getAddress()
        );
        List<CallbackData> crudData = new ArrayList<>();
        crudData.add(new CallbackData("Nomini o'zgartirish", "edit name"));
        crudData.add(new CallbackData("Orientrni o'zgartirish", "edit landmark"));
        crudData.add(new CallbackData("Telefon raqamni o'zgartirish", "edit phone"));
        crudData.add(new CallbackData("Ish boshlanish vaqtini o'zgartirish", "edit time begin"));
        crudData.add(new CallbackData("Ish tugash vaqtini o'gartirish", "edit time last"));
        bot.sendMessage(user.getChatId(), msg1 + "\n\n✅ Muvaffaqiyatli o'zgartirildi\n\nQuyidagilardan birini tanlang", kyb.setKeyboards(crudData, 1));
        eventCode(user, "branches crud");
    }

    public void setAddBranch(Long chatId, User user) {
        bot.sendMessage(chatId, "Endi yangi filial nomini kiriting", kyb.setKeyboard(OrderKybMsg.back("uz")));
        eventCode(user, "add branch");
    }

    public void addBranch(User user, String text) {
        if (branchRepository.findAll().isEmpty()) {
            if (text.equals(OrderKybMsg.back("uz"))) {
                List<Branch> all = branchRepository.findAll();
                String[] branches = new String[all.size() + 2];
                branches[0] = (OrderKybMsg.back("uz"));
                branches[1] = ("Filial qo'shish");
                for (int i = 0; i < all.size(); i++) {
                    branches[i + 2] = all.get(i).getName();
                }
                bot.sendMessage(user.getChatId(), "Quyidagilardan birini tanlang", kyb.setKeyboards(branches, 2));
                eventCode(user, "get all branches");
                return;
            }
        } else {
            if (text.equals(OrderKybMsg.back("uz"))) {

                List<Branch> all = branchRepository.findAll();
                String[] branches = new String[all.size() + 2];
                branches[0] = (OrderKybMsg.back("uz"));
                branches[1] = ("Filial qo'shish");
                for (int i = 0; i < all.size(); i++) {
                    branches[i + 2] = all.get(i).getName();
                }
                eventCode(user, "get all branches");
                bot.sendMessage(user.getChatId(), "Quyidagilardan birini tanlag", kyb.setKeyboards(branches, 2));
                return;
            }
        }
        bot.sendMessage(user.getChatId(), "Ish vaqti ertalab soat nechida boshlanishini yozing", true);
        Branch branch = new Branch();
        branch.setName(text);
        branchRepository.save(branch);
        branch = branchRepository.findByName(text);
        user.setHelperCategoryId(branch.getId());
        userService.save(user);
        eventCode(user, "get time begin");
    }

    public void getTimeBegin(User user, String text) {
        Branch branch = branchRepository.findById(user.getHelperCategoryId()).get();
        try {
            branch.setBegin(Integer.valueOf(text));
            branchRepository.save(branch);
            bot.sendMessage(user.getChatId(), "Endi soat nechida tugashini yozing");
            eventCode(user, "get time last");
        } catch (NumberFormatException e) {
            bot.sendMessage(user.getChatId(), "Sonda kiritshingiz kerak misol uchun ertalab 08:00 da boshlansa 8 sonini kiritishingiz kerak");
        }
    }

    public void getTimeLast(User user, String text) {
        Branch branch = branchRepository.findById(user.getHelperCategoryId()).get();
        try {
            branch.setLast(Integer.valueOf(text));
            branchRepository.save(branch);
            bot.sendMessage(user.getChatId(), "Endi menga filial orinetrini yuboring");
            eventCode(user, "get landmark");
        } catch (NumberFormatException e) {
            bot.sendMessage(user.getChatId(), "Sonda kiritshingiz kerak misol uchun kechki 18:00 da tugasa 18 sonini kiritishingiz kerak");
        }

    }

    public void getLandMark(User user, String text) {
        Branch branch = branchRepository.findById(user.getHelperCategoryId()).get();
        branch.setLandmark(text);
        branchRepository.save(branch);
        bot.sendMessage(user.getChatId(), "Endi mega filial filial bilan bog'lanish uchun telefon raqamni yuboring (+998 xx xxx xx xx) formatda bo'lishi shart");
//        bot.sendMessage(user.getChatId(), "Endi mega filial lokatsiasini yuboring");
        eventCode(user, "get phone");
    }

    public void getPhone(User user, String text) {

        boolean success = false;
        try {

            Long.valueOf(text.substring(1));
            if (text.substring(0, 4).equals("+998") && text.length() == 13)
                success = true;
        } catch (Exception e) {
            log.error(e);
        }
        if (success) {
            Branch branch = branchRepository.findById(user.getHelperCategoryId()).get();
            branch.setPhone(text);
            branchRepository.save(branch);
            bot.sendMessage(user.getChatId(), "Endi mega filial lokatsiasini yuboring");
            eventCode(user, "get location");
        } else {
            bot.sendMessage(user.getChatId(), "Telefon raqam ko'rsatlgan tartibda kiritilmadi");
        }
    }

    public boolean getPhone1(User user, String text, Branch branch) {
        boolean success = false;
        try {

            Long.valueOf(text.substring(1));
            if (text.substring(0, 4).equals("+998") && text.length() == 13)
                success = true;
        } catch (Exception e) {
            log.error(e);
        }
        if (success) {
            branch.setPhone(text);
            branchRepository.save(branch);
        } else {
            bot.sendMessage(user.getChatId(), "Telefon raqam ko'rsatlgan tartibda kiritilmadi");
        }
        return success;
    }

    public void getLocation(User user, Location location) {
        double lat = location.getLatitude(), lon = location.getLongitude();
        String address = GetLocation.getLocation(lat, lon).getDisplay_name();
        Branch branch = branchRepository.findById(user.getHelperCategoryId()).get();
        branch.setLat(lat);
        branch.setLon(lon);
        branch.setAddress(address);
        branchRepository.save(branch);


        String msg1 = """
                Filialning nomi: %s
                Filialning orientri: %s
                Filialning aloqa markazi: %s
                Filialning ish vaqti: %d:00 %d:00
                Filial bilan bog'lanish: %s
                Filial manzili: %s
                """.formatted(
                branch.getName(),
                branch.getPhone(),
                branch.getLandmark(),
                branch.getBegin(), branch.getLast(),
                branch.getPhone(),
                branch.getAddress()
        );
        List<Branch> all = branchRepository.findAll();
        String[] branches = new String[all.size() + 2];
        branches[0] = (OrderKybMsg.back("uz"));
        branches[1] = ("Filial qo'shish");

        for (int i = 0; i < all.size(); i++) {
            branches[i + 2] = all.get(i).getName();
        }
        eventCode(user, "get all branches");
        bot.sendMessage(user.getChatId(), msg1 + "\n\nUshbu filial Muvaffaqyatli qo'shildi\n\nQuyidagilardan birini tanlag", kyb.setKeyboards(branches, 2));

    }

    public void eventCode(User user, String text) {
        user.setEventCode2(text);
        userService.save(user);
    }
}
