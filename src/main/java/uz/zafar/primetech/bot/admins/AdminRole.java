package uz.zafar.primetech.bot.admins;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Controller;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.send.SendVideo;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageCaption;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageMedia;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.media.InputMedia;
import org.telegram.telegrambots.meta.api.objects.media.InputMediaPhoto;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import uz.zafar.primetech.bot.TelegramBot;
import uz.zafar.primetech.bot.users.kyb.order.OrderKybMsg;
import uz.zafar.primetech.db.domain.*;
import uz.zafar.primetech.db.domain.User;
import uz.zafar.primetech.db.repositories.AboutMeRepository;
import uz.zafar.primetech.db.repositories.BranchRepository;
import uz.zafar.primetech.db.service.CategoryService;
import uz.zafar.primetech.db.service.ProductService;
import uz.zafar.primetech.db.service.UserService;
import uz.zafar.primetech.dto.CallbackData;
import uz.zafar.primetech.dto.NameAndPrice;
import uz.zafar.primetech.dto.ResponseDto;
import uz.zafar.primetech.dto.UserDto;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Log4j2

@Controller
@RequiredArgsConstructor
public class AdminRole {
    @Lazy
    private final TelegramBot bot;
    @Lazy
    private final UserService userService;
    @Lazy
    private final CategoryService categoryService;
    @Lazy
    private final ProductService productService;
    @Lazy
    private final AdminKyb kyb;
    @Lazy
    private final AdminMsg msg;
    private final BranchRepository branchRepository;
    private final AboutMeRepository aboutMeRepository;

    public void menu(User user, Update update, String serverPath, String token) {
        Long chatId = user.getChatId();
        String eventCode = user.getEventCode();
        if (update.hasMessage()) {
            if (update.getMessage().hasText()) {
                String text = update.getMessage().getText();
                if (text.equals("/start")) {
                    startCommand(user);
                } else {
                    String[] eventCodes = {
                            "get edit product name en",
                            "get edit product name ru",
                            "get edit product caption ru",
                            "get edit product caption uz",
                            "get edit product caption en"
                    };
                    String[] menu = {
                            "Kategriyalar bo'limi",
                            "Reklama qilish bo'limi",
                            "Filiallar bo'limi",
                            "Biz haqimizda bo'limi"
                    };
                    if (eventCode.equals("menu")) {
                        if (text.equals(menu[0])) {
                            List<Category> categories = categoryService.findAll().getData();
                            bot.sendMessage(chatId, "Quyidagi kategoriyalardan birini tanlng", kyb.getAllCategories(categories));
                            eventCode(user, "check category");
                        } else if (text.equals(menu[1])) {
                            bot.sendMessage(chatId,
                                    "Menga biror bir reklamangizni yuboring(text , rasm , video)",
                                    kyb.setKeyboard(OrderKybMsg.back("uz"))
                            );
                            eventCode(user, "get reklama");
                        } else if (text.equals(menu[2])) {
                            List<Branch> all = branchRepository.findAll();
                            if (all.isEmpty()) {
                                bot.sendMessage(chatId, "Sizda filiallar mavjud emas\n\n " +
                                        "filial qo'shish uchun filial nomini kiriting aks holda ortga tugmasini bosing", kyb.setKeyboard(OrderKybMsg.back("uz")));
                                user.setEventCode("branches");
                                user.setEventCode2("add branch");
                            } else {
                                String[] branches = new String[all.size() + 2];
                                branches[0] = (OrderKybMsg.back("uz"));
                                branches[1] = ("Filial qo'shish");
                                for (int i = 0; i < all.size(); i++) {
                                    branches[i + 2] = all.get(i).getName();
                                }
                                bot.sendMessage(chatId, "Sizdagi filiallar ro'yxati", kyb.setKeyboards(branches, 2));
                                user.setEventCode("branches");
                                user.setEventCode2("get all branches");
                            }
                            userService.save(user);

                        } else if (text.equals(menu[3])) {
                            if (aboutMeRepository.findAll().isEmpty()) {
                                bot.sendPhoto(chatId,
                                        "https://t.me/usuwiwj/4",

                                        "Foydalanuvchi quyidagi joyga kirsa ko'rinadigan qismi uchun kiritishingiz kerak" +
                                                "\n\nKompaniyaning nomini kiriting",
                                        kyb.setKeyboard(OrderKybMsg.back("uz"))
                                );
                                user.setEventCode("get about me name");
                                userService.save(user);
                            } else {
                                AboutMe me = aboutMeRepository.findAll().get(0);
                                File file = new File(serverPath + "/aboutme/" + me.getImgName());
                                String caption = """
                                        Kompaniya nomi: %s
                                        ☎️ Aloqa markazi: (Telefon raqam): %s
                                        """.formatted(me.getCompanyName(), me.getPhone());
                                eventCode(user, "crud about me");
                                bot.sendMessage(chatId, "Biz haqimizda Menyusi", true);
                                if (me.getType().equals("photo")) {
                                    bot.sendPhoto(chatId, file, caption + "\n\nQuyidagilardan brini tanlang", kyb.crudBranch1());
                                    return;
                                }
                                if (me.getType().equals("video")) {
                                    SendVideo sendVideo = new SendVideo();
                                    sendVideo.setChatId(chatId);
                                    InputFile video = new InputFile();
                                    video.setMedia(file);
                                    sendVideo.setVideo(video);
                                    sendVideo.setCaption(caption + "\n\nQuyidagilardan brini tanlang");
                                    sendVideo.setReplyMarkup(kyb.crudBranch1());
                                    bot.executes(sendVideo);
                                    return;
                                }
                                bot.sendMessage(chatId, caption + "\n\nQuyidagilardan birini tanlang", kyb.crudBranch1());
                            }
                        } else bot.sendMessage(chatId, msg.errorMessage, kyb.menu());
                    } else if (eventCode.equals("getCompanyEditName")) {
                        getCompanyEditName(user, text, serverPath);
                    } else if (eventCode.equals("getCompanyEditPhone")) {
                        getCompanyEditPhone(user, text, serverPath);
                    } else if (eventCode.equals("get about me name")) {
                        if (text.equals(OrderKybMsg.back("uz"))) {
                            startCommand(user);
                            return;
                        }

                        AboutMe aboutMe = new AboutMe();
                        aboutMe.setCompanyName(text);
                        aboutMeRepository.save(aboutMe);
                        bot.sendMessage(chatId, "Aloqa uchun telefon raqam kiriting");
                        eventCode(user, "about me phone number");
                    } else if (eventCode.equals("about me phone number")) {
                        AboutMe me = aboutMeRepository.findAll().get(0);
                        me.setPhone(text);
                        aboutMeRepository.save(me);
                        bot.sendMessage(chatId, "Endi rasm yoki videoning nomini kiriting");
                        eventCode(user, "get name img or video");
                    } else if (eventCode.equals("get name img or video")) {
                        AboutMe me = aboutMeRepository.findAll().get(0);
                        me.setImgName(text);
                        aboutMeRepository.save(me);

                        bot.sendMessage(chatId, "Endi rasm yoki videoning yuboring ", kyb.setKeyboard("O'tkazib yuborish"));
                        eventCode(user, "get img or video");
                    } else if (eventCode.equals("get img or video")) {
                        if (text.equals("get img or video")) {
                            bot.sendMessage(chatId, "Muvaffaqqiyatli saqlandi");
                            startCommand(user);
                        }
                    } else if (eventCode.equals("check category")) {
                        checkCategory(user, text);
                    } else if (eventCode.equals("get category name")) {
                        getCategoryName(user, text, serverPath);
                    } else if (eventCode.equals("get name ru")) {
                        getCategoryNameRu(user, text);
                    } else if (eventCode.equals("get name en")) {
                        getCategoryNameEn(user, text);
                    } else if (eventCode.equals("add product")) {
                        addProduct(user, null, false);
                    } else if (eventCode.equals("get product name uz")) {
                        getProductNameUz(user, text, serverPath);
                    } else if (eventCode.equals("get product name ru")) {
                        getProductNameRu(user, text);
                    } else if (eventCode.equals("get product name en")) {
                        getProductNameEn(user, text);
                    } else if (eventCode.equals("get type count")) {
                        getTypeCount(user, text);
                    } else if (eventCode.equals("get type")) {
                        getType(user, text);
                    } else if (eventCode.equals("get img")) {
                        bot.sendMessage(chatId, msg.errorMessage, update.getMessage().getMessageId());
                    } else if (eventCode.equals("get caption en")) {
                        getCaptionEn(user, text);
                    } else if (eventCode.equals("get caption ru")) {
                        getCaptionRu(user, text);
                    } else if (eventCode.equals("get caption uz")) {
                        getCaptionUz(user, text);
                    } else if (eventCode.equals("get price")) {
                        getPrice(user, text);
                    } else if (eventCode.equals("get product price")) {
                        getProductPrice(user, text);
                    } else if (eventCode.equals("get lot type")) {
                        getLotType(user, text);
                    } else if (eventCode.equals("get products")) {
                        getProducts(user, text, serverPath);
                    } else if (eventCode.equals("get edit product name")) {
                        getEditProductName(user, text, serverPath);
                    } else if (eventCode.equals(eventCodes[0])) {
                        getEditProductName(user, text, serverPath, eventCode);
                    } else if (eventCode.equals(eventCodes[1])) {
                        getEditProductName(user, text, serverPath, eventCode);
                    } else if (eventCode.equals(eventCodes[2])) {
                        getEditProductName(user, text, serverPath, eventCode);
                    } else if (eventCode.equals(eventCodes[3])) {
                        getEditProductName(user, text, serverPath, eventCode);
                    } else if (eventCode.equals(eventCodes[4])) {
                        getEditProductName(user, text, serverPath, eventCode);
                    } else if (eventCode.equals("get edit product price")) {
                        getEditProductPrice(user, text, serverPath);
                    } else if (eventCode.equals("get edit category name")) {
                        getEditCategoryName(user, text, eventCode, serverPath);
                    } else if (eventCode.equals("get edit category name en")) {
                        getEditCategoryName(user, text, eventCode, serverPath);
                    } else if (eventCode.equals("get edit category name ru")) {
                        getEditCategoryName(user, text, eventCode, serverPath);
                    } else if (eventCode.equals("get reklama")) {
                        if (text.equals(OrderKybMsg.back("uz"))) {
                            startCommand(user);
                            return;
                        }
                        long count = 0L;
                        for (UserDto userDto : userService.findAll().getData()) {
                            try {
                                bot.copyMessage(user.getChatId(), userDto.getChatId(), update.getMessage().getMessageId());
                                count++;
                            } catch (Exception e) {
                                log.error(e);
                            }
                        }

                        bot.sendMessage(chatId, "Reklamangiz " + count + " kishiga muvaffaqiyatli yetkazildi");
                    } else if (eventCode.equals("branches")) {
                        Branches branches = new Branches(
                                bot, userService, kyb, msg, branchRepository
                        );
                        branches.menu(user, update);
                    }
                }
            } else if (update.getMessage().hasPhoto()) {

                if (eventCode.equals("getCompanyEditImgOrVideo")) {
                    getCompanyEditImgOrVideo(user, update.getMessage().getPhoto().get(update.getMessage().getPhoto().size() - 1), serverPath, token, update.getMessage().getMessageId());
                }
                if (eventCode.equals("get img")) {
                    getImg(
                            user, serverPath, token,
                            update.getMessage().getPhoto().get(update.getMessage().getPhoto().size() - 1),
                            update.getMessage().getMessageId()
                    );
                } else if (eventCode.equals("get edit product img")) {
                    getEditProductImg(user, update.getMessage().getPhoto(), token, serverPath, update.getMessage().getMessageId());
                } else if (eventCode.equals("get reklama")) {
                    long count = 0L;
                    for (UserDto userDto : userService.findAll().getData()) {
                        try {
                            bot.copyMessage(user.getChatId(), userDto.getChatId(), update.getMessage().getMessageId());
                            count++;
                        } catch (Exception e) {
                            log.error(e);
                        }
                    }

                    bot.sendMessage(chatId, "Reklamangiz " + count + " kishiga muvaffaqiyatli yetkazildi");
                }
                if (eventCode.equals("get img or video")) {
                    File file1 = new File(serverPath + "/aboutme");
                    for (String s : file1.list()) {
                        File f = new File(serverPath + "/aboutme/" + s);
                        boolean delete = f.delete();
                        if (!delete) log.error("O'chirish imkonsiz");
                        else log.info("Muvaffaqiyatli o'chirildi");
                    }
                    AboutMe me = aboutMeRepository.findAll().get(0);
                    me.setImgName(me.getImgName() + ".jpg");
                    me.setType("photo");
                    aboutMeRepository.save(me);

                    downloadImg(serverPath + "/aboutme/" + me.getImgName(), token, update.getMessage().getPhoto().get(update.getMessage().getPhoto().size() - 1));
                    File file = new File(serverPath + "/aboutme/" + me.getImgName());


                    me = aboutMeRepository.findAll().get(0);
                    File filex = new File(serverPath + "/aboutme/" + me.getImgName());
                    String caption = """
                            Kompaniya nomi: %s
                            ☎️ Aloqa markazi: (Telefon raqam): %s
                            """.formatted(me.getCompanyName(), me.getPhone());
                    eventCode(user, "crud about me");
                    if (me.getType().equals("photo")) {
                        bot.sendPhoto(chatId, filex, caption + "\n\nQuyidagilardan brini tanlang", kyb.crudBranch1());
                        eventCode(user, "crud about me");
                    }

                }

            } else if (update.getMessage().hasVideo()) {
                if (eventCode.equals("getCompanyEditImgOrVideo")) {
                    getCompanyEditImgOrVideo(user, update.getMessage().getVideo(), serverPath, token, update.getMessage().getMessageId());
                }
                if (eventCode.equals("get img or video")) {
                    File file1 = new File(serverPath + "/aboutme");
                    for (String s : file1.list()) {
                        File f = new File(serverPath + "/aboutme/" + s);
                        boolean delete = f.delete();
                        if (!delete) log.error("O'chirish imkonsiz");
                    }
                    AboutMe me = aboutMeRepository.findAll().get(0);
                    me.setImgName(me.getImgName() + ".mp4");
                    aboutMeRepository.save(me);
                    downloadVideo(serverPath + "/aboutme/" + me.getImgName(), token, update.getMessage().getVideo());

                    me = aboutMeRepository.findAll().get(0);
                    File filex = new File(serverPath + "/aboutme/" + me.getImgName());
                    String caption = """
                            Kompaniya nomi: %s
                            ☎️ Aloqa markazi: (Telefon raqam): %s
                            """.formatted(me.getCompanyName(), me.getPhone());
                    if (me.getType().equals("video")) {
                        SendVideo sendVideo1 = new SendVideo();
                        sendVideo1.setChatId(chatId);
                        InputFile video = new InputFile();
                        video.setMedia(filex);
                        sendVideo1.setVideo(video);
                        sendVideo1.setCaption(caption + "\n\n✅Muvaffaqiyatli saqlandi\n\nQuyidagilardan brini tanlang");
                        sendVideo1.setReplyMarkup(kyb.crudBranch1());
                        bot.executes(sendVideo1);
                        eventCode(user, "crud about me");
                        return;
                    }
                }
                if (eventCode.equals("get reklama")) {
                    long count = 0L;
                    for (UserDto userDto : userService.findAll().getData()) {
                        try {
                            bot.copyMessage(user.getChatId(), userDto.getChatId(), update.getMessage().getMessageId());
                            count++;
                        } catch (Exception e) {
                            log.error(e);
                        }
                    }

                    bot.sendMessage(chatId, "Reklamangiz " + count + " kishiga muvaffaqiyatli yetkazildi");
                }

            } else if (update.getMessage().hasLocation()) {
                if (eventCode.equals("branches")) {
                    Branches branches = new Branches(
                            bot, userService, kyb, msg, branchRepository
                    );
                    branches.menu(user, update);
                }
            }
        } else if (update.hasCallbackQuery()) {
            CallbackQuery callbackQuery = update.getCallbackQuery();
            String data = callbackQuery.getData();
            if (eventCode.equals("check category")) {
                checkCategory(user, update.getCallbackQuery());
            } else if (eventCode.equals("get products")) {
                getProducts(user, callbackQuery, serverPath);
            } else if (eventCode.equals("is delete product")) {
                isDeleteProduct(user, callbackQuery, serverPath);
            } else if (eventCode.equals("branches")) {
                Branches branches = new Branches(
                        bot, userService, kyb, msg, branchRepository
                );
                branches.menu(user, update);
            } else if (eventCode.equals("crud about me")) {
                if (data.equals("edit")) {
                    AboutMe me = aboutMeRepository.findAll().get(0);
                    File file = new File(serverPath + "/aboutme/" + me.getImgName());
                    String caption = """
                            Kompaniya nomi: %s
                            ☎️ Aloqa markazi: (Telefon raqam): %s
                            """.formatted(me.getCompanyName(), me.getPhone());
                    eventCode(user, "crud about me");
                    List<CallbackData> dataList = new ArrayList<>();

                    dataList.add(new CallbackData("Kompaniya nomini o'zgartirish", "edit company name"));
                    dataList.add(new CallbackData("Telefon raqamni almashtrish", "edit company phone"));
                    dataList.add(new CallbackData("Rasmni(vidyosini o'zgartirish)", "edit company img"));
                    dataList.add(new CallbackData("\uD83D\uDD19 Orqaga qaytish", "go back"));
                    EditMessageCaption editMessageCaption = new EditMessageCaption();
                    editMessageCaption.setCaption(caption + "Quyidagilardan birini tanlang");
                    editMessageCaption.setReplyMarkup(kyb.setKeyboards(dataList, 1));
                    editMessageCaption.setMessageId(callbackQuery.getMessage().getMessageId());
                    editMessageCaption.setChatId(chatId);
                    bot.executes(editMessageCaption);
                } else if (data.equals("edit company name")) {
                    bot.deleteMessage(chatId, update.getCallbackQuery().getMessage().getMessageId());
                    bot.sendMessage(chatId, "Kompaniyaning yangi nomini yuboring");
                    eventCode(user, "getCompanyEditName");
                } else if (data.equals("edit company phone")) {
                    bot.deleteMessage(chatId, update.getCallbackQuery().getMessage().getMessageId());
                    bot.sendMessage(chatId, "Kompaniyaning yangi telefon raqamini yuboring");
                    eventCode(user, "getCompanyEditPhone");
                } else if (data.equals("edit company img")) {
                    bot.deleteMessage(chatId, update.getCallbackQuery().getMessage().getMessageId());
                    bot.sendMessage(chatId, "Kompaniyaning yangi Rasm yoki vidyosini yuboring");
                    eventCode(user, "getCompanyEditImgOrVideo");
                } else if (data.equals("go back")) {
                    AboutMe me = aboutMeRepository.findAll().get(0);
                    File file = new File(serverPath + "/aboutme/" + me.getImgName());
                    String caption = """
                            Kompaniya nomi: %s
                            ☎️ Aloqa markazi: (Telefon raqam): %s
                            """.formatted(me.getCompanyName(), me.getPhone());
                    eventCode(user, "crud about me");
                    EditMessageCaption editMessageCaption = new EditMessageCaption();
                    editMessageCaption.setCaption(caption + "Quyidagilardan birini tanlang");
                    editMessageCaption.setReplyMarkup(kyb.crudBranch1());
                    editMessageCaption.setMessageId(callbackQuery.getMessage().getMessageId());
                    editMessageCaption.setChatId(chatId);
                    bot.executes(editMessageCaption);
                }
            }
        }
    }

    public void getCompanyEditName(User user, String text, String serverPath) {
        AboutMe me = aboutMeRepository.findAll().get(0);
        me.setCompanyName(text);
        aboutMeRepository.save(me);
        String caption = """
                Kompaniya nomi: %s
                ☎️ Aloqa markazi: (Telefon raqam): %s
                """.formatted(me.getCompanyName(), me.getPhone());
        eventCode(user, "crud about me");
        if (me.getType().equals("photo")) {
            bot.sendPhoto(user.getChatId(), new File(serverPath + "/aboutme/" + me.getImgName())
                    , caption + "\n\n✅ Muvaffaqiyatli o'gartirildi\n\n Quyidagiladan birini tanlang", kyb.crudBranch1()
            );
        } else if (me.getType().equals("video")) {
            sendVideo(user.getChatId(), serverPath + "/aboutme/" + me.getImgName(), caption + "\n\n✅ Muvaffaqiyatli o'gartirildi\n\n Quyidagiladan birini tanlang", kyb.crudBranch1());

        }
    }

    public void getCompanyEditPhone(User user, String text, String serverPath) {
        AboutMe me = aboutMeRepository.findAll().get(0);
        me.setPhone(text);
        aboutMeRepository.save(me);
        String caption = """
                Kompaniya nomi: %s
                ☎️ Aloqa markazi: (Telefon raqam): %s
                """.formatted(me.getCompanyName(), me.getPhone());
        eventCode(user, "crud about me");
        if (me.getType().equals("photo")) {
            bot.sendPhoto(user.getChatId(), new File(serverPath + "/aboutme/" + me.getImgName())
                    , caption + "\n\n✅ Muvaffaqiyatli o'gartirildi\n\n Quyidagiladan birini tanlang", kyb.crudBranch1()
            );
        } else if (me.getType().equals("video")) {
            sendVideo(user.getChatId(), serverPath + "/aboutme/" + me.getImgName(), caption + "\n\n✅ Muvaffaqiyatli o'gartirildi\n\n Quyidagiladan birini tanlang", kyb.crudBranch1());
        }
    }

    public void getCompanyEditImgOrVideo(User user, Video video, String serverPath, String token, int messageId) {
        AboutMe me = aboutMeRepository.findAll().get(0);
        me.setType("video");
        String caption = """
                Kompaniya nomi: %s
                ☎️ Aloqa markazi: (Telefon raqam): %s
                """.formatted(me.getCompanyName(), me.getPhone());
        String url = serverPath + "/aboutme/" + me.getImgName();
        File file = new File(url);
        boolean delete = file.delete();
        if (delete) log.info("Muvaffaqiyatli o'chirildi");
        else {
            log.error("Nimagadir o'chirilmadi");
            bot.sendMessage(user.getChatId(), "Kutilmagan xatolik rasmni qaytadan yuboring");
            return;
        }
        aboutMeRepository.save(me);
        bot.sendMessage(user.getChatId(), "Video yuklanmoqda...");
        downloadVideo(url, token, video);
        bot.editMessageText(user.getChatId(), "✅ Muvaffaqiyatli yuklandi", messageId + 1);
        eventCode(user, "crud about me");
        sendVideo(user.getChatId(), serverPath + "/aboutme/" + me.getImgName(), caption + "\n\n✅ Muvaffaqiyatli o'gartirildi\n\n Quyidagiladan birini tanlang", kyb.crudBranch1());
    }

    public void getCompanyEditImgOrVideo(User user, PhotoSize photo, String serverPath, String token, int messageId) {
        AboutMe me = aboutMeRepository.findAll().get(0);
        me.setType("photo");
        String caption = """
                Kompaniya nomi: %s
                ☎️ Aloqa markazi: (Telefon raqam): %s
                """.formatted(me.getCompanyName(), me.getPhone());
        String url = serverPath + "/aboutme/" + me.getImgName();
        File file = new File(url);
        boolean delete = file.delete();
        if (delete) log.info("Muvaffaqiyatli o'chirildi");
        else {
            log.error("Nimagadir o'chirilmadi");
            bot.sendMessage(user.getChatId(), "Kutilmagan xatolik rasmni qaytadan yuboring");
            return;
        }
        aboutMeRepository.save(me);
        bot.sendMessage(user.getChatId(), "Rasm yuklanmoqda...");
        downloadImg(url, token, photo);
        bot.editMessageText(user.getChatId(), "✅ Muvaffaqiyatli yuklandi", messageId + 1);
        eventCode(user, "crud about me");
        bot.sendPhoto(user.getChatId(), new File(serverPath + "/aboutme/" + me.getImgName())
                , caption + "\n\n✅ Muvaffaqiyatli o'gartirildi\n\n Quyidagiladan birini tanlang", kyb.crudBranch1());
    }


    public void sendVideo(Long chatId, String fileUrl, String caption, InlineKeyboardMarkup markup) {
        SendVideo sendVideo = new SendVideo();
        sendVideo.setChatId(chatId);
        sendVideo.setCaption(caption);
        sendVideo.setVideo(new InputFile(new File(fileUrl)));
        sendVideo.setReplyMarkup(markup);
        bot.executes(sendVideo);
    }

    public void startCommand(User user) {
        bot.sendMessage(user.getChatId(), msg.menu, kyb.menu());
        eventCode(user, "menu");
    }

    public void checkCategory(User user, String text) {
        if (text.equals("Kategroiya qo'shish")) {
            bot.sendMessage(user.getChatId(), "Yangi kategoriya nomini kiriting", true);
            eventCode(user, "get category name");
        } else if (text.equals("⬅\uFE0F Orqaga")) {
            startCommand(user);
        } else {
            try {
                Category category = categoryService.findByName(text).getData();
                user.setHelperCategoryId(category.getId());
                userService.save(user);
                bot.sendMessage(user.getChatId(), category.getNameUz(), kyb.crudCategory());
                eventCode(user, "check category");
            } catch (Exception e) {
                bot.sendMessage(user.getChatId(), msg.errorMessage, kyb.getAllCategories(categoryService.findAll().getData()));
            }
        }
    }

    public void getCategoryName(User user, String text, String serverPath) {
        Category category;
        category = new Category();
        category.setNameUz(text);
        category.setStatus("draft");
        ResponseDto save = categoryService.save(category);
        if (save.isSuccess()) {
            user.setHelperCategoryId(categoryService.findByName(text).getData().getId());
            userService.save(user);
            File file = new File(serverPath + "/" + text);
            if (!file.exists()) {
                try {
                    boolean a = file.mkdirs();
                    if (a) {
                        bot.sendMessage(user.getChatId(), "Endi ruscha nomini kiriting");
                        eventCode(user, "get name ru");
                    } else bot.sendMessage(user.getChatId(), "Kutilmagan xatolik iltimos qaytadan kiriting");
                } catch (Exception e) {
                    bot.sendMessage(user.getChatId(), "Nimagadir serverga saqlanmadi, iltimos qaytadan harakat qilib ko'ring");
                }
            }
        } else {
            bot.sendMessage(user.getChatId(), "Kechirasiz, bunday kategoriya nomi avvaldan mavjud bizga boshqa kategriya yuborishingiz keraak");
        }
    }

    public void getCategoryNameRu(User user, String text) {
        Category category;
        category = categoryService.findById(user.getHelperCategoryId()).getData();
        category.setNameRu(text);
        ResponseDto save = categoryService.save(category);
        if (save.isSuccess()) {
            bot.sendMessage(user.getChatId(), "Endi inglizcha nomini kiriting");
            eventCode(user, "get name en");
        } else {
            bot.sendMessage(user.getChatId(), "Kechirasiz, bunday kategoriya nomi avvaldan mavjud bizga boshqa kategriya yuborishingiz keraak");
        }
    }

    public void getCategoryNameEn(User user, String text) {
        Category category;
        category = categoryService.findById(user.getHelperCategoryId()).getData();
        category.setNameEn(text);
        category.setSuccess(true);
        category.setStatus("open");
        ResponseDto save = categoryService.save(category);
        if (save.isSuccess()) {
            bot.sendMessage(user.getChatId(), "Muvaffaqiyatli saqlandi");
            startCommand(user);
        } else {
            bot.sendMessage(user.getChatId(), "Kechirasiz, bunday kategoriya nomi avvaldan mavjud bizga boshqa kategriya yuborishingiz keraak");
        }
    }

    public void checkCategory(User user, CallbackQuery callbackQuery) {
        String data = callbackQuery.getData();
        Integer messageId = callbackQuery.getMessage().getMessageId();
        Category category = categoryService.findById(user.getHelperCategoryId()).getData();
        if (data.equals("edit")) {
            bot.deleteMessage(user.getChatId(), callbackQuery.getMessage().getMessageId());
            bot.sendMessage(
                    user.getChatId(),
                    "Kategoriya nomini o'zgartirish uchun nom kiriting\n\neski nomi: (<code>" + category.getNameUz() + "</code>)",
                    kyb.setKeyboard("Bekor qilish")
            );
            eventCode(user, "get edit category name");
        } else if (data.equals("get products")) {
            List<Product> list = categoryService.findById(user.getHelperCategoryId()).getData().getProducts();
            if (list.isEmpty()) {
                bot.alertMessage(callbackQuery, """
                        Ushbu kategoryda mahsulotlar mavjud emas
                        """);
                bot.editMessageText(user.getChatId(), "Sizda mahsulotlar mavjud emas, mahsulot qo'shishni istasangiz pastdagi mahsulot qo'shish tugmasini bosing", messageId, kyb.addProduct());
            } else {
                bot.deleteMessage(user.getChatId(), messageId);
                bot.sendMessage(user.getChatId(), category.getNameUz() + "ning mahsulotlar ro'yxati",
                        kyb.getAllProducts(
                                category.getProducts(), category.getNameUz()
                        ));
                eventCode(user, "get products");
            }
        } else if (data.equals("delete")) {
            bot.editMessageText(user.getChatId(), "Siz haqiqatdan ham %s ni o'chirmoqchimisiz ?".formatted(category.getNameUz()), messageId, kyb.isSuccess());
        } else if (data.equals("yes delete")) {
            category.setSuccess(false);
            ResponseDto save = categoryService.save(category);
            boolean success = save.isSuccess();
            if (success) {
                bot.deleteMessage(user.getChatId(), callbackQuery.getMessage().getMessageId());
                bot.sendMessage(user.getChatId(), category.getNameUz() + " muvaffaqiyatli o'chirildi\n\nQuyidagilardan birini tanlang", kyb.getAllCategories(categoryService.findAll().getData()));
            } else bot.alertMessage(callbackQuery, save.getMessage());
        } else if (data.equals("no delete")) {
            bot.alertMessage(callbackQuery, category.getNameUz() + " kategoriyasi o'chirilmadi");
            bot.editMessageText(user.getChatId(), "O'chirilmmadi\n\n" + category.getNameUz(), callbackQuery.getMessage().getMessageId(), kyb.crudCategory());
        } else if (data.equals("add product")) {
            addProduct(user, messageId, true);
        }
    }

    public void getEditCategoryName(User user, String text, String eventCode, String serverPath) {
        if (text.equals("Bekor qilish")) {
            try {
                Category category = categoryService.findById(user.getHelperCategoryId()).getData();
                bot.sendMessage(user.getChatId(), "Kategoriya nomi o'zgartirilmadi", kyb.getAllCategories(categoryService.findAll().getData()));

                bot.sendMessage(user.getChatId(), category.getNameUz(), kyb.crudCategory());
                eventCode(user, "check category");
            } catch (Exception e) {
                bot.sendMessage(user.getChatId(), msg.errorMessage, kyb.getAllCategories(categoryService.findAll().getData()));
            }
        } else {
            if (eventCode.equals("get edit category name")) {
                Category category = categoryService.findById(user.getHelperCategoryId()).getData();
                File oldFile = new File(serverPath + "/" + category.getNameUz());
                File newFile = new File(serverPath + "/" + text);
                boolean rename = oldFile.renameTo(newFile);
                if (!rename) {
                    try {
                        bot.sendMessage(user.getChatId(), "Kategoriya nomi o'zgartirilmadi, Buning sababi kategira kategoriya nomlari bir xil bo'lganligi", kyb.getAllCategories(categoryService.findAll().getData()));
                        bot.sendMessage(user.getChatId(), category.getNameUz(), kyb.crudCategory());
                        eventCode(user, "check category");
                        return;
                    } catch (Exception e) {
                        bot.sendMessage(user.getChatId(), msg.errorMessage, kyb.getAllCategories(categoryService.findAll().getData()));
                    }
                }
                category.setNameUz(text);
                boolean success = categoryService.save(category).isSuccess();
                if (!success) {
                    bot.sendMessage(user.getChatId(), "Bu nomni kirita olmaysiz chunki boshqa kategorya nomlari bilan bir xil bo'lb qoldi\n\nBoshqa nom kiriting");
                    return;
                }
                category = categoryService.findById(user.getHelperCategoryId()).getData();
                bot.sendMessage(user.getChatId(),
                        "Inglizcha nomini kiriting \n\nEski nomi: (<code>" + category.getNameEn() + "</code>)", true);
                eventCode(user, "get edit category name en");

            } else if (eventCode.equals("get edit category name en")) {
                Category category = categoryService.findById(user.getHelperCategoryId()).getData();
                category.setNameEn(text);
                boolean success = categoryService.save(category).isSuccess();
                if (!success) {
                    bot.sendMessage(user.getChatId(), "Bu nomni kirita olmaysiz chunki boshqa kategorya nomlari bilan bir xil bo'lb qoldi\n\nBoshqa nom kiriting");
                    return;
                }
                category = categoryService.findById(user.getHelperCategoryId()).getData();
                bot.sendMessage(user.getChatId(), "Rucha nomini kiriting \n\nEski nomi: (<code>" + category.getNameRu() + "</code>)");
                eventCode(user, "get edit category name ru");
            } else if (eventCode.equals("get edit category name ru")) {
                Category category = categoryService.findById(user.getHelperCategoryId()).getData();
                category.setNameRu(text);
                boolean success = categoryService.save(category).isSuccess();
                if (!success) {
                    bot.sendMessage(user.getChatId(), "Bu nomni kirita olmaysiz chunki boshqa kategorya nomlari bilan bir xil bo'lb qoldi\n\nBoshqa nom kiriting");
                    return;
                }
                category = categoryService.findById(user.getHelperCategoryId()).getData();
                bot.sendMessage(user.getChatId(), "Muvaffaqiyatli o'zgartirildi", kyb.getAllCategories(categoryService.findAll().getData()));
                bot.sendMessage(user.getChatId(), category.getNameUz(), kyb.crudCategory());
                eventCode(user, "check category");
            }


        }
    }

    public void isDeleteProduct(User user, CallbackQuery callbackQuery, String serverPath) {
        Product product = productService.findById(user.getHelperProductId()).getData();
        Category category = categoryService.findById(user.getHelperCategoryId()).getData();
        String data = callbackQuery.getData(), msg;
        String url = serverPath + "/" + category.getNameUz() + "/" + product.getNameUz();

        if (data.equals("yes delete")) {
            File file = new File(url);
            if (!file.exists()) {
                msg = "Fayl mavjud emas";
                bot.sendMessage(user.getChatId(), url);
                bot.alertMessage(callbackQuery, msg);
                return;
            }
            ResponseDto delete = productService.deleteById(product.getId());
            boolean isDelete = delete.isSuccess();
            if (isDelete) {
                msg = product.getNameUz() + " mahsulot o'chirildi";
                bot.alertMessage(callbackQuery, msg);
                bot.deleteMessage(user.getChatId(), callbackQuery.getMessage().getMessageId());
                List<Category> categories = categoryService.findAll().getData();
                bot.sendMessage(user.getChatId(), "Quyidagi kategoriyalardan birini tanlng", kyb.getAllCategories(categories));
                eventCode(user, "check category");
                return;
            } else {
                msg = "Mahsulot o'chirilmadi. Xatolik: " + delete.getMessage();
            }
        } else if (data.equals("no delete")) {
            msg = product.getNameUz() + " mahsulot o'chirilmadi";
            bot.alertMessage(callbackQuery, msg);

            userService.save(user);

            SendPhoto sendPhoto = new SendPhoto();
            String packageUrl = serverPath + "/" + category.getNameUz() + "/" + product.getNameUz();
            String[] photos = new File(packageUrl).list();
            List<NameAndPrice> list;
            list = new ArrayList<>();
            Double price = 0.0;
            for (int i = 0; i < photos.length; i++) {
                String[] example = photos[i].split("_");
                NameAndPrice nameAndPrice = new NameAndPrice();
                nameAndPrice.setNameUz(example[0]);
                nameAndPrice.setPrice(Double.valueOf(example[1].substring(0, example[1].length() - 4)));
                nameAndPrice.setFullNameAndPrice(photos[i]);
                if (user.getHelperType().equals(nameAndPrice.getFullNameAndPrice()))
                    price = nameAndPrice.getPrice();
                list.add(nameAndPrice);
            }
            productService.save(product);
            String res = "";
            res = res.concat("O'zbekcha nomi: <code>%s</code>\n".formatted(product.getNameUz()));
            res = res.concat("Ruscha nomi: <code>%s</code>\n".formatted(product.getNameRu()));
            res = res.concat("Inglizcha nomi: <code>%s</code>\n".formatted(product.getNameEn()));
            res = res.concat("O'zbekcha tavsifi: <code>%s</code>\n".formatted(product.getCaptionUz()));
            res = res.concat("Ruscha tavsifi: <code>%s</code>\n".formatted(product.getCaptionRu()));
            res = res.concat("Inglizcha tavsifi: <code>%s</code>\n\n".formatted(product.getCaptionEn()));
            res = res.concat("Narxi: %s".formatted(String.valueOf(price)));
            sendPhoto.setCaption(res);
            InputFile file = new InputFile();
            file.setMedia(new File(packageUrl + "/" + user.getHelperType()));
            sendPhoto.setPhoto(
                    file
            );
            sendPhoto.setParseMode("HTML");
            sendPhoto.setReplyMarkup(kyb.crudProduct(list, user.getHelperType()));
            sendPhoto.setChatId(user.getChatId());
            category = categoryService.findById(user.getHelperCategoryId()).getData();
            bot.sendMessage(user.getChatId(), "Mahsulot O'chirilmadi", kyb.getAllProducts(category.getProducts(), category.getNameUz()));
            EditMessageCaption caption = new EditMessageCaption();
            caption.setMessageId(callbackQuery.getMessage().getMessageId());
            caption.setReplyMarkup(kyb.crudProduct(list, user.getHelperType()));
            caption.setChatId(user.getChatId());
            caption.setCaption(res);
            caption.setParseMode("HTML");
            try {
                bot.execute(caption);
                eventCode(user, "get products");
            } catch (TelegramApiException e) {
                log.error(e);
            }

        } else {
            msg = "Noto'g'ri so'rov: " + data;
            bot.alertMessage(callbackQuery, "Kutilmagan xatolik");
            return;
        }
        bot.alertMessage(callbackQuery, msg);
    }

    public void getProducts(User user, String text, String serverPath) {
        Category category = categoryService.findById(user.getHelperCategoryId()).getData();
        if (text.equals("Mahsulot qo'shish")) {
            addProduct(user, 1, false, category);
        } else if (text.equals("⬅\uFE0F Orqaga")) {
            List<Category> categories = categoryService.findAll().getData();
            bot.sendMessage(user.getChatId(), "Quyidagi kategoriyalardan birini tanlng", kyb.getAllCategories(categories));
            eventCode(user, "check category");
        } else {
            ResponseDto<Product> dto = productService.findByName(text);
            if (dto.isSuccess()) {
                Product product = dto.getData();
                user.setHelperProductId(product.getId());
                userService.save(user);
                SendPhoto sendPhoto = new SendPhoto();
                String packageUrl = serverPath + "/" + category.getNameUz() + "/" + product.getNameUz();
                String[] photos = new File(packageUrl).list();
                List<NameAndPrice> list;
                list = new ArrayList<>();
                for (int i = 0; i < photos.length; i++) {
                    String[] example = photos[i].split("_");
                    NameAndPrice nameAndPrice = new NameAndPrice();
                    nameAndPrice.setNameUz(example[0]);
                    nameAndPrice.setPrice(Double.valueOf(example[1].substring(0, example[1].length() - 4)));
                    nameAndPrice.setFullNameAndPrice(photos[i]);
                    list.add(nameAndPrice);
                }
                String res = "";
                res = res.concat("O'zbekcha nomi: <code>%s</code>\n".formatted(product.getNameUz()));
                res = res.concat("Ruscha nomi: <code>%s</code>\n".formatted(product.getNameRu()));
                res = res.concat("Inglizcha nomi: <code>%s</code>\n".formatted(product.getNameEn()));
                res = res.concat("O'zbekcha tavsifi: <code>%s</code>\n".formatted(product.getCaptionRu()));
                res = res.concat("Ruscha tavsifi: <code>%s</code>\n".formatted(product.getCaptionRu()));
                res = res.concat("Inglizcha tavsifi: <code>%s</code>\n\n".formatted(product.getCaptionEn()));
                res = res.concat("Narxi: %s".formatted(String.valueOf(list.get(0).getPrice())));
                user.setHelperType(list.get(0).getFullNameAndPrice());
                userService.save(user);
                sendPhoto.setCaption(res);
                InputFile file = new InputFile();
                file.setMedia(new File(packageUrl + "/" + list.get(0).getFullNameAndPrice()));
                sendPhoto.setPhoto(
                        file
                );
                sendPhoto.setParseMode("HTML");
                sendPhoto.setReplyMarkup(kyb.crudProduct(list, list.get(0).getFullNameAndPrice()));
                sendPhoto.setChatId(user.getChatId());
                bot.executes(sendPhoto);
            } else {
                bot.sendMessage(user.getChatId(), msg.errorMessage);
            }
        }
    }

    public void getProducts(User user, CallbackQuery callbackQuery, String serverPath) {
        String data = callbackQuery.getData();
        Integer messageId = callbackQuery.getMessage().getMessageId();
        ResponseDto<Product> dto = productService.findById(user.getHelperProductId());
        Category category = categoryService.findById(user.getHelperCategoryId()).getData();
        Product product = dto.getData();
        String packageUrl = serverPath + "/" + category.getNameUz() + "/" + product.getNameUz();
        String[] photos = new File(packageUrl).list();
        List<NameAndPrice> list;
        list = new ArrayList<>();
        Double price = 0.0;
        for (int i = 0; i < photos.length; i++) {
            String[] example = photos[i].split("_");
            NameAndPrice nameAndPrice = new NameAndPrice();
            nameAndPrice.setNameUz(example[0]);
            nameAndPrice.setPrice(Double.valueOf(example[1].substring(0, example[1].length() - 4)));
            if (user.getHelperType().equals(photos[i])) {
                price = nameAndPrice.getPrice();
            }
            nameAndPrice.setFullNameAndPrice(photos[i]);
            list.add(nameAndPrice);
        }
        String res = "";
        res = res.concat("O'zbekcha nomi: <code>%s</code>\n".formatted(product.getNameUz()));
        res = res.concat("Ruscha nomi: <code>%s</code>\n".formatted(product.getNameRu()));
        res = res.concat("Inglizcha nomi: <code>%s</code>\n".formatted(product.getNameEn()));
        res = res.concat("O'zbekcha tavsifi: <code>%s</code>\n".formatted(product.getCaptionUz()));
        res = res.concat("Ruscha tavsifi: <code>%s</code>\n".formatted(product.getCaptionRu()));
        res = res.concat("Inglizcha tavsifi: <code>%s</code>\n\n".formatted(product.getCaptionEn()));
        res = res.concat("Narxi: %s".formatted(String.valueOf(price)));
        if (data.equals("edit")) {
            String message = product.getNameUz() + "ning nimalarini o'zgartirmoqchisiz";
            InlineKeyboardMarkup markup = kyb.editProduct();
            EditMessageCaption caption = new EditMessageCaption();
            caption.setCaption(message + "\n\n" + res);
            caption.setParseMode("HTML");
            caption.setReplyMarkup(markup);
            caption.setMessageId(messageId);
            caption.setChatId(user.getChatId());
            try {
                bot.execute(caption);
            } catch (TelegramApiException e) {
                log.error(e);
            }
        } else if (data.equals("nameUz")) {
            bot.deleteMessage(user.getChatId(), messageId);
            bot.sendMessage(user.getChatId(), "Yangi nomini kiriting (eski nomi: <code>%s</code>)".formatted(product.getNameUz()), true);
            eventCode(user, "get edit product name");
        } else if (data.equals("nameEn")) {
            bot.deleteMessage(user.getChatId(), messageId);
            bot.sendMessage(user.getChatId(), "Yangi inglizcha nomini kiriting (eski nomi: <code>%s</code>)".formatted(product.getNameEn()), true);
            eventCode(user, "get edit product name en");
        } else if (data.equals("nameRu")) {
            bot.deleteMessage(user.getChatId(), messageId);
            bot.sendMessage(user.getChatId(), "Yangi ruscha nomini kiriting (eski nomi: <code>%s</code>)".formatted(product.getNameEn()), true);
            eventCode(user, "get edit product name ru");
        } else if (data.equals("captionUz")) {
            bot.deleteMessage(user.getChatId(), messageId);
            bot.sendMessage(user.getChatId(), "Yangi o'zbekcha tavsifini kiriting (eski nomi: <code>%s</code>)".formatted(product.getCaptionUz()), true);
            eventCode(user, "get edit product caption uz".trim());
        } else if (data.equals("captionRu")) {
            bot.deleteMessage(user.getChatId(), messageId);
            bot.sendMessage(user.getChatId(), "Yangi ruscha tavsifini kiriting (eski nomi: <code>%s</code>)".formatted(product.getCaptionRu()), true);
            eventCode(user, "get edit product caption ru");
        } else if (data.equals("captionEn")) {
            bot.deleteMessage(user.getChatId(), messageId);
            bot.sendMessage(user.getChatId(), "Yangi inglizcha tavsifini kiriting (eski nomi: <code>%s</code>)".formatted(product.getCaptionEn()), true);
            eventCode(user, "get edit product caption en");
        } else if (data.equals("price")) {
            bot.deleteMessage(user.getChatId(), messageId);
            String a = user.getHelperType();
            String pr = a.substring(a.indexOf("_") + 1, a.lastIndexOf("."));
            bot.sendMessage(user.getChatId(), "Yangi narxni kiriting (eski nomi: <code>%s</code>)".formatted(pr), true);
            eventCode(user, "get edit product price");
        } else if (data.equals("img")) {
            String mesg = "Bu eski rasm \n\nRasmni o'zgartirish uchun yangi rasmni kiriting";
            SendPhoto sendPhoto = new SendPhoto();
            sendPhoto.setChatId(user.getChatId());
            sendPhoto.setCaption(mesg);
            InputFile file = new InputFile(new File(serverPath + "/" + category.getNameUz() + "/" + product.getNameUz() + "/" + user.getHelperType()));
            sendPhoto.setPhoto(file);
            sendPhoto.setReplyMarkup(new ReplyKeyboardRemove(true));
            bot.deleteMessage(user.getChatId(), messageId);
            bot.executes(sendPhoto);
            eventCode(user, "get edit product img");
        } else if (data.equals("delete")) {
            EditMessageCaption caption = new EditMessageCaption();
            caption.setCaption("Siz haqiqatdan ham %s mahsulotini o'chirmoqchimisiz".formatted(productService.findById(user.getHelperProductId()).getData().getNameUz()));
            caption.setReplyMarkup(kyb.isSuccess());
            caption.setChatId(user.getChatId());
            caption.setMessageId(messageId);
            try {
                bot.execute(caption);
                eventCode(user, "is delete product");
            } catch (TelegramApiException e) {
                log.error(e);
            }
        } else if (data.equals("to back")) {
            editProduct(
                    user, product, category, serverPath, user.getHelperType(), messageId
            );

        } else {
            editProduct(
                    user, product, category, serverPath, data, messageId
            );
        }
    }

    public void getEditProductPrice(User user, String text, String serverPath) {
        try {
            Category category = categoryService.findById(user.getHelperCategoryId()).getData();
            Product product = productService.findById(user.getHelperProductId()).getData();
            String cName = category.getNameUz();
            String pName = product.getNameUz();
            String a = user.getHelperType();
            Double oldPrice = Double.valueOf(a.substring(a.indexOf("_") + 1, a.lastIndexOf(".")));
            double newPrice = Double.parseDouble(text);
            String imgName = a.substring(0, a.indexOf("_"));
            String baseUrl = serverPath + "/" + cName + "/" + pName;
            File oldFile = new File(baseUrl + "/" + user.getHelperType());
            File newFile = new File(baseUrl + "/" + imgName + "_" + newPrice + ".jpg");
            boolean b = oldFile.renameTo(newFile);
            if (b) {
                String t = imgName + "_" + newPrice + ".jpg";
                user.setHelperType(t);
                userService.save(user);
                SendPhoto sendPhoto = new SendPhoto();
                String packageUrl = serverPath + "/" + category.getNameUz() + "/" + product.getNameUz();
                String[] photos = new File(packageUrl).list();
                List<NameAndPrice> list;
                list = new ArrayList<>();
                Double price = 0.0;
                for (int i = 0; i < photos.length; i++) {
                    String[] example = photos[i].split("_");
                    NameAndPrice nameAndPrice = new NameAndPrice();
                    nameAndPrice.setNameUz(example[0]);
                    nameAndPrice.setPrice(Double.valueOf(example[1].substring(0, example[1].length() - 4)));
                    nameAndPrice.setFullNameAndPrice(photos[i]);
                    if (user.getHelperType().equals(nameAndPrice.getFullNameAndPrice()))
                        price = nameAndPrice.getPrice();
                    list.add(nameAndPrice);
                }
                String res = "";
                res = res.concat("O'zbekcha nomi: <code>%s</code>\n".formatted(product.getNameUz()));
                res = res.concat("Ruscha nomi: <code>%s</code>\n".formatted(product.getNameRu()));
                res = res.concat("Inglizcha nomi: <code>%s</code>\n".formatted(product.getNameEn()));
                res = res.concat("O'zbekcha tavsifi: <code>%s</code>\n".formatted(product.getCaptionUz()));
                res = res.concat("Ruscha tavsifi: <code>%s</code>\n".formatted(product.getCaptionRu()));
                res = res.concat("Inglizcha tavsifi: <code>%s</code>\n\n".formatted(product.getCaptionEn()));
                res = res.concat("Narxi: %s".formatted(String.valueOf(price)));
                sendPhoto.setCaption(res);
                InputFile file = new InputFile();
                file.setMedia(new File(packageUrl + "/" + user.getHelperType()));
                sendPhoto.setPhoto(
                        file
                );
                sendPhoto.setParseMode("HTML");
                sendPhoto.setReplyMarkup(kyb.crudProduct(list, user.getHelperType()));
                sendPhoto.setChatId(user.getChatId());
                category = categoryService.findById(user.getHelperCategoryId()).getData();
                bot.sendMessage(user.getChatId(), "✅ Muvaffaqiyatli o'zgartitildi", kyb.getAllProducts(category.getProducts(), category.getNameUz()));
                bot.executes(sendPhoto);
                eventCode(user, "get products");
            } else {
                bot.sendMessage(user.getChatId(), "Kutilmagan xatolik iltimos narxni qaytadan kiriting");
            }
        } catch (NumberFormatException e) {
            bot.sendMessage(user.getChatId(), "Narxni sonda kiritishingiz kerak");
        }
    }

    public void getEditProductImg(User user, List<PhotoSize> photos, String token, String serverPath, Integer messageId) {
        try {
            PhotoSize photo = photos.get(photos.size() - 1);
            GetFile getFileMethod = new GetFile();
            getFileMethod.setFileId(photo.getFileId());

            org.telegram.telegrambots.meta.api.objects.File file = bot.execute(getFileMethod);
            String url;

            Category category = categoryService.findById(user.getHelperCategoryId()).getData();
            Product product = productService.findById(user.getHelperProductId()).getData();
            String c = category.getNameUz(), p = product.getNameUz();
            url = serverPath + "/" + c + "/" + p + "/" + user.getHelperType();
            File file1 = new File(url);
            if (file1.exists()) {
                boolean delete = file1.delete();
                if (delete) log.info("Muvaffaiyatli o'chirildi");
                else {
                    bot.sendMessage(user.getChatId(), "Kutilmagan xatolik Rasmni qytadan yuboring");
                    return;
                }
            }
            URL fileUrl = new URL(file.getFileUrl(token));
            try (InputStream in = fileUrl.openStream();
                 FileOutputStream out = new FileOutputStream(url)) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
                out.close();
                SendPhoto sendPhoto = new SendPhoto();
                String packageUrl = serverPath + "/" + category.getNameUz() + "/" + product.getNameUz();
                String[] photos1 = new File(packageUrl).list();
                List<NameAndPrice> list;
                list = new ArrayList<>();
                Double price = 0.0;
                for (int i = 0; i < photos1.length; i++) {
                    String[] example = photos1[i].split("_");
                    NameAndPrice nameAndPrice = new NameAndPrice();
                    nameAndPrice.setNameUz(example[0]);
                    nameAndPrice.setPrice(Double.valueOf(example[1].substring(0, example[1].length() - 4)));
                    nameAndPrice.setFullNameAndPrice(photos1[i]);
                    if (user.getHelperType().equals(nameAndPrice.getFullNameAndPrice()))
                        price = nameAndPrice.getPrice();
                    list.add(nameAndPrice);
                }
                String res = "";
                res = res.concat("O'zbekcha nomi: <code>%s</code>\n".formatted(product.getNameUz()));
                res = res.concat("Ruscha nomi: <code>%s</code>\n".formatted(product.getNameRu()));
                res = res.concat("Inglizcha nomi: <code>%s</code>\n".formatted(product.getNameEn()));
                res = res.concat("O'zbekcha tavsifi: <code>%s</code>\n".formatted(product.getCaptionUz()));
                res = res.concat("Ruscha tavsifi: <code>%s</code>\n".formatted(product.getCaptionRu()));
                res = res.concat("Inglizcha tavsifi: <code>%s</code>\n\n".formatted(product.getCaptionEn()));
                res = res.concat("Narxi: %s".formatted(String.valueOf(price)));
                bot.sendMessage(user.getChatId(), "kirdi",
                        kyb.getAllProducts(category.getProducts(), category.getNameUz()));

                SendPhoto sendPhoto1 = new SendPhoto();
                sendPhoto1.setCaption("✅ Rasm muvaffaqiyatli o'zgartirildi\n\n" + res);
                sendPhoto1.setChatId(user.getChatId());
                sendPhoto1.setParseMode("HTML");
                file1 = new File(url);
                sendPhoto1.setPhoto(new InputFile(file1));
                sendPhoto1.setReplyMarkup(kyb.crudProduct(list, user.getHelperType()));
                eventCode(user, "get products");
                bot.executes(sendPhoto1);
                userService.save(user);
            } catch (IOException e) {
                bot.sendMessage(user.getChatId(), "Rasm yuklashda muammo chiqdi rasmni qatadan yuboring");
            }

        } catch (TelegramApiException e) {
            bot.sendMessage(user.getChatId(), "Nimagadir rasm yubora oladim");
        } catch (MalformedURLException e) {
            bot.sendMessage(user.getChatId(), "Rasm yuklashda muammo chiqdi rasmni qatadan yuboring");
        }
    }

    public void getEditProductName(User user, String text, String serverPath, String eventCode) {
        Category category = categoryService.findById(user.getHelperCategoryId()).getData();
        Product product = productService.findById(user.getHelperProductId()).getData();
        SendPhoto sendPhoto = new SendPhoto();
        String packageUrl = serverPath + "/" + category.getNameUz() + "/" + product.getNameUz();
        String[] photos = new File(packageUrl).list();
        List<NameAndPrice> list;
        list = new ArrayList<>();
        Double price = 0.0;
        for (int i = 0; i < photos.length; i++) {
            String[] example = photos[i].split("_");
            NameAndPrice nameAndPrice = new NameAndPrice();
            nameAndPrice.setNameUz(example[0]);
            nameAndPrice.setPrice(Double.valueOf(example[1].substring(0, example[1].length() - 4)));
            nameAndPrice.setFullNameAndPrice(photos[i]);
            if (user.getHelperType().equals(nameAndPrice.getFullNameAndPrice()))
                price = nameAndPrice.getPrice();
            list.add(nameAndPrice);
        }
        if (eventCode.equals("get edit product name en")) product.setNameEn(text);
        if (eventCode.equals("get edit product name ru")) product.setNameRu(text);
        if (eventCode.equals("get edit product caption uz")) product.setCaptionUz(text);
        if (eventCode.equals("get edit product caption ru")) product.setCaptionRu(text);
        if (eventCode.equals("get edit product caption en")) product.setCaptionEn(text);
        productService.save(product);
        String res = "";
        res = res.concat("O'zbekcha nomi: <code>%s</code>\n".formatted(product.getNameUz()));
        res = res.concat("Ruscha nomi: <code>%s</code>\n".formatted(product.getNameRu()));
        res = res.concat("Inglizcha nomi: <code>%s</code>\n".formatted(product.getNameEn()));
        res = res.concat("O'zbekcha tavsifi: <code>%s</code>\n".formatted(product.getCaptionUz()));
        res = res.concat("Ruscha tavsifi: <code>%s</code>\n".formatted(product.getCaptionRu()));
        res = res.concat("Inglizcha tavsifi: <code>%s</code>\n\n".formatted(product.getCaptionEn()));
        res = res.concat("Narxi: %s".formatted(String.valueOf(price)));
        sendPhoto.setCaption(res);
        InputFile file = new InputFile();
        file.setMedia(new File(packageUrl + "/" + user.getHelperType()));
        sendPhoto.setPhoto(
                file
        );
        sendPhoto.setParseMode("HTML");
        sendPhoto.setReplyMarkup(kyb.crudProduct(list, user.getHelperType()));
        sendPhoto.setChatId(user.getChatId());
        category = categoryService.findById(user.getHelperCategoryId()).getData();
        bot.sendMessage(user.getChatId(), "✅ Muvaffaqiyatli o'zgartitildi", kyb.getAllProducts(category.getProducts(), category.getNameUz()));
        bot.executes(sendPhoto);
        eventCode(user, "get products");
    }

    public void getEditProductName(User user, String text, String serverPath) {
        try {
            Category category = categoryService.findById(user.getHelperCategoryId()).getData();
            Product product = productService.findById(user.getHelperProductId()).getData();
            String packageUrlOld = serverPath + "/" + category.getNameUz() + "/" + product.getNameUz();
            String packageUrlNew = serverPath + "/" + category.getNameUz() + "/" + text;
            File oldFile = new File(packageUrlOld);
            File newFile = new File(packageUrlNew);
            boolean success = oldFile.renameTo(newFile);
            if (success) {
                product.setNameUz(text);
                ResponseDto save = productService.save(product);
                if (save.isSuccess()) {
                    SendPhoto sendPhoto = new SendPhoto();
                    String packageUrl = serverPath + "/" + category.getNameUz() + "/" + text;
                    String[] photos = new File(packageUrl).list();
                    List<NameAndPrice> list;
                    list = new ArrayList<>();
                    Double price = 0.0;
                    for (int i = 0; i < photos.length; i++) {
                        String[] example = photos[i].split("_");
                        NameAndPrice nameAndPrice = new NameAndPrice();
                        nameAndPrice.setNameUz(example[0]);
                        nameAndPrice.setPrice(Double.valueOf(example[1].substring(0, example[1].length() - 4)));
                        nameAndPrice.setFullNameAndPrice(photos[i]);
                        if (user.getHelperType().equals(nameAndPrice.getFullNameAndPrice()))
                            price = nameAndPrice.getPrice();
                        list.add(nameAndPrice);
                    }
                    String res = "";
                    res = res.concat("O'zbekcha nomi: <code>%s</code>\n".formatted(product.getNameUz()));
                    res = res.concat("Ruscha nomi: <code>%s</code>\n".formatted(product.getNameRu()));
                    res = res.concat("Inglizcha nomi: <code>%s</code>\n".formatted(product.getNameEn()));
                    res = res.concat("O'zbekcha tavsifi: <code>%s</code>\n".formatted(product.getCaptionRu()));
                    res = res.concat("Ruscha tavsifi: <code>%s</code>\n".formatted(product.getCaptionRu()));
                    res = res.concat("Inglizcha tavsifi: <code>%s</code>\n\n".formatted(product.getCaptionEn()));
                    res = res.concat("Narxi: %s".formatted(String.valueOf(price)));
                    sendPhoto.setCaption(res);
                    InputFile file = new InputFile();
                    file.setMedia(new File(packageUrl + "/" + user.getHelperType()));
                    sendPhoto.setPhoto(
                            file
                    );
                    sendPhoto.setParseMode("HTML");
                    sendPhoto.setReplyMarkup(kyb.crudProduct(list, user.getHelperType()));
                    sendPhoto.setChatId(user.getChatId());
                    category = categoryService.findById(user.getHelperCategoryId()).getData();
                    bot.sendMessage(user.getChatId(), "✅ Muvaffaqiyatli o'zgartitildi", kyb.getAllProducts(category.getProducts(), category.getNameUz()));
                    bot.executes(sendPhoto);
                    eventCode(user, "get products");
                } else {
                    bot.sendMessage(user.getChatId(), "Nomini o'zgartirishda muammo chiqdi, mahsulot nomini qaytadan kiriting");
                }
            } else {
                bot.sendMessage(user.getChatId(), "Kutilmagan xatolik, nomini qaytadan kiriting");
            }
        } catch (Exception e) {
            bot.sendMessage(user.getChatId(), "Nomini o'zgartirishda muammo chiqdi, mahsulot nomini qaytadan kiriting\n\nBuning sababi <code>%s</code>".formatted(e.getMessage()));
        }

    }

    public void editProduct(User user, Product product, Category category, String serverPath, String data, Integer messageId) {

        user.setHelperProductId(product.getId());
        userService.save(user);
        String packageUrl = serverPath + "/" + category.getNameUz() + "/" + product.getNameUz();
        String[] photos = new File(packageUrl).list();
        List<NameAndPrice> list;
        list = new ArrayList<>();
        Double price = 0.0;
        for (int i = 0; i < photos.length; i++) {
            String[] example = photos[i].split("_");
            NameAndPrice nameAndPrice = new NameAndPrice();
            nameAndPrice.setNameUz(example[0]);
            nameAndPrice.setPrice(Double.valueOf(example[1].substring(0, example[1].length() - 4)));
            if (data.equals(photos[i])) {
                price = nameAndPrice.getPrice();
            }
            nameAndPrice.setFullNameAndPrice(photos[i]);
            list.add(nameAndPrice);
        }
        user.setHelperType(data);
        userService.save(user);
        String res = "";
        res = res.concat("O'zbekcha nomi: <code>%s</code>\n".formatted(product.getNameUz()));
        res = res.concat("Ruscha nomi: <code>%s</code>\n".formatted(product.getNameRu()));
        res = res.concat("Inglizcha nomi: <code>%s</code>\n".formatted(product.getNameEn()));
        res = res.concat("O'zbekcha tavsifi: <code>%s</code>\n".formatted(product.getCaptionRu()));
        res = res.concat("Ruscha tavsifi: <code>%s</code>\n".formatted(product.getCaptionRu()));
        res = res.concat("Inglizcha tavsifi: <code>%s</code>\n\n".formatted(product.getCaptionEn()));
        res = res.concat("Narxi: %s".formatted(String.valueOf(price)));
        EditMessageMedia editMessageMedia = new EditMessageMedia();
        InputMedia file = new InputMediaPhoto();
        file.setCaption(res);
        file.setParseMode("HTML");
        file.setMedia(new File(packageUrl + "/" + data), data);
        editMessageMedia.setMedia(file);
        editMessageMedia.setMessageId(messageId);

        editMessageMedia.setReplyMarkup(kyb.crudProduct(list, data));
        editMessageMedia.setChatId(user.getChatId());
        bot.executes(editMessageMedia);
    }

    public void addProduct(User user, Integer messageId, boolean isData) {
        if (isData) {
            bot.editMessageText(user.getChatId(), "Yangi mahsulot nomini kiriting(O'zek tilida)", messageId);
        } else {
            bot.sendMessage(user.getChatId(), "Yangi mahsulot nomini kiriting(O'zek tilida)", true);
        }
        eventCode(user, "get product name uz");
    }

    public void addProduct(User user, Integer messageId, boolean isData, Category category) {
        if (isData) {
            bot.editMessageText(user.getChatId(), "Yangi mahsulot nomini kiriting(O'zek tilida)", messageId);
        } else {
            bot.sendMessage(user.getChatId(), category.getNameUz() + "ga mahsulot qo'shmoqdasiz\n\nYangi mahsulot nomini kiriting(O'zek tilida)", true);
        }
        eventCode(user, "get product name uz");
    }

    public void getProductNameUz(User user, String text, String serverPath) {
        try {
            Product product;
            product = new Product();
            product.setNameUz(text);
            Category category = categoryService.findById(user.getHelperCategoryId()).getData();
            product.setCategory(category);
            productService.save(product);
            user.setHelperProductId(productService.findByName(text).getData().getId());
            userService.save(user);
            File file = new File(serverPath + "/" + category.getNameUz() + "/" + text);
            if (!file.exists()) {
                boolean successFile = file.mkdir();
                if (successFile) {
                    bot.sendMessage(user.getChatId(), "Endi inglizcha nomini kiriting", true);
                    eventCode(user, "get product name en");
                } else {
                    bot.sendMessage(user.getChatId(), "Kutilmagan xatolik iltimos mahsulot nomini kiriting");
                }

            }
        } catch (Exception e) {
            bot.sendMessage(user.getChatId(), text + " nomli produkt kirita olmaysiz boshqa nom kiriting", true);
        }
    }

    public void getProductNameRu(User user, String text) {
        try {
            Product product = productService.findById(user.getHelperProductId()).getData();
            product.setNameRu(text);
            productService.save(product);
            bot.sendMessage(user.getChatId(),
                    "Mahsulot uchun tavsif qo'shing (O'zbek tilida)");

            eventCode(user, "get caption uz");
        } catch (Exception e) {
            bot.sendMessage(user.getChatId(), text + " nomli produkt kirita olmaysiz boshqa nom kiriting", true);
        }
    }

    public void getCaptionUz(User user, String text) {
        try {
            Product product = productService.findById(user.getHelperProductId()).getData();
            product.setCaptionUz(text);
            productService.save(product);
            bot.sendMessage(user.getChatId(),
                    "Mahsulot uchun tavsif qo'shing (Rus tilida) ");
            eventCode(user, "get caption ru");
        } catch (Exception e) {
            bot.sendMessage(user.getChatId(), text + " nomli produkt kirita olmaysiz boshqa nom kiriting", true);
        }
    }

    public void getCaptionRu(User user, String text) {
        try {
            Product product = productService.findById(user.getHelperProductId()).getData();
            product.setCaptionRu(text);
            productService.save(product);
            bot.sendMessage(user.getChatId(),
                    "Mahsulot uchun tavsif qo'shing (Ingliz tilida) ");
            eventCode(user, "get caption en");
        } catch (Exception e) {
            bot.sendMessage(user.getChatId(), text + " nomli produkt kirita olmaysiz boshqa nom kiriting", true);
        }
    }

    public void getCaptionEn(User user, String text) {
        try {
            Product product = productService.findById(user.getHelperProductId()).getData();
            product.setCaptionEn(text);
            productService.save(product);
            bot.sendMessage(user.getChatId(),
                    "Mahsulotningizning turi 1 tadan ko'pmi",
                    kyb.setKeyboards(new String[]{
                            "✅ 1 tadan ko'p",
                            "❌ yo'q 1 ta"
                    }, 2));
            eventCode(user, "get type count");
        } catch (Exception e) {
            bot.sendMessage(user.getChatId(), text + " nomli produkt kirita olmaysiz boshqa nom kiriting", true);
        }
    }

    public void getProductNameEn(User user, String text) {
        try {
            Product product = productService.findById(user.getHelperProductId()).getData();
            product.setNameEn(text);
            productService.save(product);
            bot.sendMessage(user.getChatId(), "Endi rus tilida kiritng");
            eventCode(user, "get product name ru");
        } catch (Exception e) {
            bot.sendMessage(user.getChatId(), text + " nomli produkt kirita olmaysiz boshqa nom kiriting", true);
        }

    }

    public void getTypeCount(User user, String text) {
        try {
            int count = Integer.parseInt(text);
            user.setCountImg(count);
            userService.save(user);
            bot.sendMessage(user.getChatId(), count + " - mahsulot narxini kiriting", true);
            eventCode(user, "get price");
        } catch (Exception e) {
            if (text.equals("✅ 1 tadan ko'p")) {
                bot.sendMessage(user.getChatId(), "Sonini kiriting (sonda bo'lishi kerak) ", true);
                eventCode(user, "get type count");
            } else if (text.equals("❌ yo'q 1 ta")) {
                user.setCountImg(1);
                userService.save(user);
                bot.sendMessage(user.getChatId(), "mahsulot narxini kiriting", true);
                eventCode(user, "get product price");
            } else {
                bot.sendMessage(user.getChatId(), msg.errorMessage);
            }
        }
    }

    public void getLotType(User user, String text) {
        try {
            user.setCountImg(Integer.valueOf(text));
            userService.save(user);
            bot.sendMessage(user.getChatId(), "Mahsulot narxini kiriting");
            eventCode(user, "get product price");
        } catch (Exception e) {
            bot.sendMessage(user.getChatId(), "❌ sonini raqamda kiriting");
        }
    }

    public void getProductPrice(User user, String text) {
        try {
            user.setHelperPrice(Double.valueOf(text));
            userService.save(user);
            bot.sendMessage(user.getChatId(), "Mahsulot turini kiriting");
            eventCode(user, "get type");
        } catch (NumberFormatException e) {
            bot.sendMessage(user.getChatId(), "❌ sonini raqamda kiriting");
        }
    }


    public void getPrice(User user, String text) {
        try {
            user.setHelperPrice(Double.valueOf(text));
            userService.save(user);
            bot.sendMessage(user.getChatId(), "mahsulot turini kiriting");
            eventCode(user, "get type");
        } catch (Exception e) {
            bot.sendMessage(user.getChatId(), "Narni sonda kiritishingiz kerak");
        }
    }

    public void getType(User user, String text) {
        if (user.getCountImg() == 1) {
            user.setHelperImgName(text);
            user.setCountImg(user.getCountImg());
            userService.save(user);
            bot.sendMessage(user.getChatId(), "Mahsulot rasmini yuboring");
            eventCode(user, "get img");
        } else {
            if (user.getCountImg() == 0) {
                checkCategory(user, categoryService.findById(user.getHelperCategoryId()).getData().getNameUz());
            } else {
                user.setHelperImgName(text);
                userService.save(user);
                bot.sendMessage(user.getChatId(), "Mahsulot rasmini yuboring");
                eventCode(user, "get img");
            }
        }
    }


    public void getImg(User user, String serverPath, String token, PhotoSize photo, Integer messageId) {
        bot.sendMessage(user.getChatId(), "Ilimos kuting...");
        GetFile getFileMethod = new GetFile();
        getFileMethod.setFileId(photo.getFileId());
        Category category = categoryService.findById(user.getHelperCategoryId()).getData();
        Product product = productService.findById(user.getHelperProductId()).getData();
        String c = category.getNameUz(), p = product.getNameUz();
        try {
            org.telegram.telegrambots.meta.api.objects.File file = bot.execute(getFileMethod);
            String url;
            url = serverPath + "/" + c + "/" + p + "/" +
                    user.getHelperImgName() + "_" + user.getHelperPrice() + ".jpg";
            URL fileUrl = new URL(file.getFileUrl(token));
            try (InputStream in = fileUrl.openStream();
                 FileOutputStream out = new FileOutputStream(url)) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }

                out.close();
                user.setCountImg(user.getCountImg() - 1);
                bot.editMessageText(user.getChatId(), "✅ Rasm serverga muvaffaqiyatli yuklandi", messageId + 1);
                bot.sendMessage(user.getChatId(), user.getCountImg() == 0 ? "Quyidagilardan birini tanlang" :
                                (user.getCountImg() + " - mahsulot turining narxini kiriting"), messageId,
                        user.getCountImg() != 0 ? null :
                                kyb.getAllCategories(categoryService.findAll().getData())
                );
                userService.save(user);
                if (user.getCountImg() == 0) getType(user, "b");
                else eventCode(user, "get price");
            } catch (IOException e) {
                log.error(e);
            }

        } catch (TelegramApiException | MalformedURLException e) {
            log.error(e);
        }
    }

    public void downloadImg(String url, String token, PhotoSize photo) {
        GetFile getFileMethod = new GetFile();
        getFileMethod.setFileId(photo.getFileId());
        try {
            org.telegram.telegrambots.meta.api.objects.File file = bot.execute(getFileMethod);
            URL fileUrl = new URL(file.getFileUrl(token));
            try (InputStream in = fileUrl.openStream();
                 FileOutputStream out = new FileOutputStream(url)) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }

                out.close();

            } catch (IOException e) {
                log.error(e);
            }

        } catch (TelegramApiException e) {
            log.error(e);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    public void downloadVideo(String url, String token, Video video) {
        GetFile getFileMethod = new GetFile();
        getFileMethod.setFileId(video.getFileId());
        try {
            org.telegram.telegrambots.meta.api.objects.File file = bot.execute(getFileMethod);
            URL fileUrl = new URL(file.getFileUrl(token));
            try (InputStream in = fileUrl.openStream();
                 FileOutputStream out = new FileOutputStream(url)) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }

                out.close();

            } catch (IOException e) {
                log.error(e);
            }

        } catch (TelegramApiException e) {
            log.error(e);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    public void eventCode(User user, String text) {
        user.setEventCode(text);
        userService.save(user);
    }

}
