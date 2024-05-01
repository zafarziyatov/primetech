package uz.zafar.primetech.bot;

import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.cache.CacheAutoConfiguration;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.*;
import org.telegram.telegrambots.meta.api.methods.send.*;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageCaption;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageMedia;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.media.InputMedia;
import org.telegram.telegrambots.meta.api.objects.media.InputMediaPhoto;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import uz.zafar.primetech.bot.admins.AdminKyb;
import uz.zafar.primetech.bot.admins.AdminMsg;
import uz.zafar.primetech.bot.admins.AdminRole;
import uz.zafar.primetech.bot.superAdmin.SuperAdmin;
import uz.zafar.primetech.bot.superAdmin.SuperAdminKyb;
import uz.zafar.primetech.bot.superAdmin.SuperAdminMsg;
import uz.zafar.primetech.bot.users.UserRole;
import uz.zafar.primetech.bot.users.kyb.order.OrderKyb;
import uz.zafar.primetech.bot.users.kyb.order.OrderMsg;
import uz.zafar.primetech.bot.users.kyb.pickup.PickupKyb;
import uz.zafar.primetech.bot.users.kyb.pickup.PickupMsg;
import uz.zafar.primetech.bot.users.kyb.user.UserKyb;
import uz.zafar.primetech.bot.users.kyb.user.UserMsg;
import uz.zafar.primetech.db.domain.Category;
import uz.zafar.primetech.db.domain.User;
import uz.zafar.primetech.db.repositories.AboutMeRepository;
import uz.zafar.primetech.db.repositories.BranchRepository;
import uz.zafar.primetech.db.service.*;
import uz.zafar.primetech.dto.ResponseDto;

import java.io.File;
import java.time.LocalDate;
import java.util.List;

@Service
@Log4j2
public class TelegramBot extends TelegramLongPollingBot {
    @Value("${divide.lists.size}")
    private Integer size;
    @Value("${super.admin.chat.id}")
    private Long superAdminChatId;
    @Value("${bot.username}")
    private String botUsername;
    @Value("${bot.token}")
    private String botToken;
    @Autowired
    private UserService userService;
    @Autowired
    private LocationService locationService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private ProductService productService;
    @Autowired
    private UserKyb kyb;
    @Autowired
    private UserMsg msg;
    @Autowired
    private BranchRepository branchRepository;
    @Autowired
    private OrderKyb orderKyb;
    @Autowired
    private OrderMsg orderMsg;
    @Autowired
    private AdminKyb adminKyb;
    @Autowired
    private AdminMsg adminMsg;
    @Autowired
    private BasketService basketService;
    @Autowired
    private PickupKyb pickupKyb;
    @Autowired
    private PickupMsg pickupMsg;
    @Autowired
    private AboutMeRepository aboutMeRepository;
    @Autowired
    private SuperAdminMsg superAdminMsg;
    @Autowired
    private SuperAdminKyb superAdminKyb;

    @SneakyThrows
    @Override
    public void onUpdateReceived(Update update) {

        Long chatId = 0L;
        int messageId;
        User user;
        String nickname = "", username = "";
        try {
            if (update.hasMessage()) {
                nickname = update.getMessage().getFrom().getFirstName();
                if (update.getMessage().getFrom().getLastName() != null)
                    nickname = nickname.concat(update.getMessage().getFrom().getLastName());
                username = update.getMessage().getFrom().getUserName();
                chatId = update.getMessage().getChatId();
            } else {
                chatId = update.getCallbackQuery().getMessage().getChatId();
            }

        } catch (Exception e) {
            log.error(e);
        }
        if (chatId <= 1L) return;
        ResponseDto<User> checkUser = userService.findByChatId(chatId);
        if (checkUser.isSuccess()) {
            user = checkUser.getData();
        } else {
            user = new User();
            user.setChatId(chatId);
            user.setLevel(0);
            user.setRole("user");
            user.setPage(0);
            user.setEventCode("");
            user.setEventCode2("");
            user.setLevel(1);
            user.setCount(1350L);
            user.setDay(LocalDate.now());
            user.setUsername(update.getMessage().getFrom().getUserName());
            user.setNickname(
                    update.getMessage().getFrom().getFirstName() + " " +
                            ((update.getMessage().getFrom().getLastName() == null) ? "" :
                                    (update.getMessage().getFrom().getLastName()))
            );
            userService.save(user);
            user = userService.findByChatId(chatId).getData();
        }


        String role = user.getRole();
        File file = new File("src/main/java/uz/zafar/primetech/server");

        String url = file.getAbsolutePath();

        try {
            if (update.hasMessage()) {
                if (!user.getNickname().equals(nickname)) {
                    user.setNickname(nickname);
                    userService.save(user);
                }
                if (!user.getUsername().equals(username)) {
                    user.setUsername(username);
                    userService.save(user);
                }
            }
        } catch (Exception e) {
            log.error(e);
        }
        if (chatId.equals(superAdminChatId)) {
            SuperAdmin superAdmin = new SuperAdmin(
                    this, userService, superAdminKyb, superAdminMsg
            );
            superAdmin.menu(user, update);
        } else if (role.equals("admin")) {
            AdminRole adminRole = new AdminRole(
                    this, userService, categoryService,
                    productService, adminKyb, adminMsg, branchRepository, aboutMeRepository
            );
            adminRole.menu(user, update, url, getBotToken());
        } else if (role.equals("user")) {
            UserRole userRole = new UserRole(
                    this, kyb, msg,
                    userService, orderMsg,
                    orderKyb, locationService,
                    categoryService, productService,
                    basketService, branchRepository,
                    pickupKyb, pickupMsg, aboutMeRepository
            );
            userRole.menu(user, update, url, superAdminChatId);
        } else {
            sendMessage(chatId, "error");
        }
    }


    public void save() {
        Category category = new Category();
//        category.set  NameEn();
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }


    public void executes(SendMessage deleteMessage) {
        try {
            execute(deleteMessage);
        } catch (TelegramApiException e) {
            log.error(e);
        }
    }

    public void executes(ForwardMessage forwardMessage) {
        try {
            execute(forwardMessage);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    public void executes(CopyMessage copyMessage) {
        try {
            execute(copyMessage);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    public void executes(SendPhoto sendPhoto) {
        try {
            execute(sendPhoto);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    public void executes(SendVideo sendVideo) {
        try {
            execute(sendVideo);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    public void executes(EditMessageMedia editMessageMedia) {
        try {
            execute(editMessageMedia);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    public void executes(EditMessageText editMessageText) {
        try {
            execute(editMessageText);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    public void executes(SendAudio sendAudio) {
        try {
            execute(sendAudio);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    public void executes(EditMessageCaption editMessageCaption) {
        try {
            execute(editMessageCaption);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    public void executes(SendVenue sendAudio) {
        try {
            execute(sendAudio);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    public void executes(SendDice sendAudio) {
        try {
            execute(sendAudio);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    public void executes(SendVideoNote sendVideoNote) {
        try {
            execute(sendVideoNote);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    public void executes(SendAnimation sendAnimation) {
        try {
            execute(sendAnimation);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    public void executes(SendChatAction sendAnimation) {
        try {
            execute(sendAnimation);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    public void executes(SendContact sendContact) {
        try {
            execute(sendContact);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    public void executes(SendDocument sendDocument) {
        try {
            execute(sendDocument);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    public void executes(SendLocation sendLocation) {
        try {
            execute(sendLocation);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    public void executes(SendMediaGroup sendMediaGroup) {
        try {
            execute(sendMediaGroup);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    public void executes(SendSticker sendSticker) {
        try {
            execute(sendSticker);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    public void executes(AnswerCallbackQuery answerCallbackQuery) {
        try {
            execute(answerCallbackQuery);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendMessage(Long chat_id, String text, Integer message_id, InlineKeyboardMarkup inlineKeyboardMarkup) {
        try {
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(chat_id);
            sendMessage.setText(text);
            sendMessage.enableHtml(true);
            sendMessage.setDisableWebPagePreview(true);
            sendMessage.setReplyToMessageId(message_id);
            if (inlineKeyboardMarkup != null) sendMessage.setReplyMarkup(inlineKeyboardMarkup);
            execute(sendMessage);
        } catch (TelegramApiException e) {
            System.out.println(e);
        }
    }

    public void sendMessage(Long chat_id, String text, Integer message_id, ReplyKeyboardMarkup replyKeyboardMarkup) {
        try {
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(chat_id);
            sendMessage.setText(text);
            sendMessage.enableHtml(true);
            sendMessage.setDisableWebPagePreview(true);
            sendMessage.setReplyToMessageId(message_id);
            sendMessage.setReplyMarkup(replyKeyboardMarkup);
            execute(sendMessage);
        } catch (TelegramApiException e) {
            System.out.println(e);
        }
    }

    public void sendMessage(Long chat_id, String text, InlineKeyboardMarkup inlineKeyboardMarkup) {
        try {
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(chat_id);
            sendMessage.setText(text);
            sendMessage.enableHtml(true);
            sendMessage.setDisableWebPagePreview(true);
            if (inlineKeyboardMarkup != null) sendMessage.setReplyMarkup(inlineKeyboardMarkup);
            execute(sendMessage);
        } catch (TelegramApiException e) {
            System.out.println(e);
        }
    }

    public void sendMessage(Long chat_id, String text, ReplyKeyboardMarkup replyKeyboardMarkup) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chat_id);
        sendMessage.setText(text);
        sendMessage.enableHtml(true);
        sendMessage.setDisableWebPagePreview(true);
        sendMessage.setReplyMarkup(replyKeyboardMarkup);
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            log.error(e);
        }

    }


    public void sendMessage(Long chat_id, String text, Integer message_id) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chat_id);
        sendMessage.setText(text);
        sendMessage.enableHtml(true);
        sendMessage.setDisableWebPagePreview(true);
        sendMessage.setReplyToMessageId(message_id);
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            log.error(e);
        }
    }


    public void sendMessage(Long chat_id, String text) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chat_id);
        sendMessage.setText(text);
        sendMessage.enableHtml(true);
        sendMessage.setDisableWebPagePreview(true);
        executes(sendMessage);
    }

    public void sendMessage(Long chat_id, String text, Boolean removeKyb) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chat_id);
        sendMessage.setText(text);
        sendMessage.enableHtml(true);
        sendMessage.setDisableWebPagePreview(true);
        sendMessage.setReplyMarkup(new ReplyKeyboardRemove(removeKyb));
        executes(sendMessage);
    }


    //DELETE MESSAGE

    public void deleteMessage(Long chat_id, Integer message_id) {
        DeleteMessage deleteMessage = new DeleteMessage();
        deleteMessage.setMessageId(message_id);
        deleteMessage.setChatId(chat_id);
        try {
            execute(deleteMessage);
        } catch (TelegramApiException e) {
            System.out.println(e);
        }
    }


//    COPY MESSAGE

    public void copyMessage(Long fromChatId, Long chatId, Integer message_id) {
        CopyMessage copyMessage = new CopyMessage();
        copyMessage.setFromChatId(fromChatId);
        copyMessage.setChatId(chatId);
        copyMessage.setMessageId(message_id);
        executes(copyMessage);
    }


    //    SendChatAction
    public void SendChatAction(Long chat_id) {
        SendChatAction sendChatAction = new SendChatAction(); // obyekt yaratib olamiz
        sendChatAction.setAction(ActionType.TYPING);          // ActionType ni set qilamiz
        sendChatAction.setChatId(chat_id);                    // chatId ni set qilamiz (qaysi chatga yubormoqchi bolgan)
        executes(sendChatAction);
    }

    // FORWARD MESSAGE

    public void forwardMessage(Long fromChatId, Long chatId, Integer message_id) {
        ForwardMessage forwardMessage = new ForwardMessage();
        forwardMessage.setFromChatId(fromChatId);
        forwardMessage.setChatId(chatId);
        forwardMessage.setMessageId(message_id);
        try {
            execute(forwardMessage);
        } catch (TelegramApiException e) {
            System.out.println(e);
        }
    }

//    SEND PHOTO

    public void sendPhoto(Long chat_id, String photo, Integer message_id) {
        SendPhoto sendPhoto = new SendPhoto();
        sendPhoto.setChatId(chat_id);

        InputFile photoFile = new InputFile(photo);
        sendPhoto.setPhoto(photoFile);
        sendPhoto.setParseMode("html");

        sendPhoto.setReplyToMessageId(message_id);
        executes(sendPhoto);
    }

    public void sendPhoto(Long chat_id, String photo, Integer message_id, InlineKeyboardMarkup inlineKeyboardMarkup) {
        SendPhoto sendPhoto = new SendPhoto();
        sendPhoto.setChatId(chat_id);

        InputFile photoFile = new InputFile(photo);
        sendPhoto.setPhoto(photoFile);

        sendPhoto.setParseMode("html");
        sendPhoto.setReplyToMessageId(message_id);
        sendPhoto.setReplyMarkup(inlineKeyboardMarkup);
        executes(sendPhoto);
    }

    public void sendPhoto(Long chat_id, File photo, String caption, InlineKeyboardMarkup inlineKeyboardMarkup) {
        SendPhoto sendPhoto = new SendPhoto();
        sendPhoto.setChatId(chat_id);

        InputFile photoFile = new InputFile(photo);
        sendPhoto.setPhoto(photoFile);

        sendPhoto.setParseMode("html");
        sendPhoto.setCaption(caption);
        sendPhoto.setReplyMarkup(inlineKeyboardMarkup);
        executes(sendPhoto);
    }

    public void sendPhoto(Long chat_id, String photo, Integer message_id, ReplyKeyboardMarkup replyKeyboardMarkup) {
        SendPhoto sendPhoto = new SendPhoto();
        sendPhoto.setChatId(chat_id);

        InputFile photoFile = new InputFile(photo);
        sendPhoto.setPhoto(photoFile);

        sendPhoto.setParseMode("html");
        sendPhoto.setReplyToMessageId(message_id);
        sendPhoto.setReplyMarkup(replyKeyboardMarkup);
        executes(sendPhoto);
    }

    public void sendPhoto(Long chat_id, File photo, Integer message_id, ReplyKeyboardMarkup replyKeyboardMarkup) {
        SendPhoto sendPhoto = new SendPhoto();
        sendPhoto.setChatId(chat_id);

        InputFile photoFile = new InputFile(photo);
        sendPhoto.setPhoto(photoFile);

        sendPhoto.setParseMode("html");
        sendPhoto.setReplyToMessageId(message_id);
        sendPhoto.setReplyMarkup(replyKeyboardMarkup);
        executes(sendPhoto);
    }
//////////////////////////////////////////////////////////////////////////////////

    public void sendPhoto(Long chat_id, String photo) {
        SendPhoto sendPhoto = new SendPhoto();
        sendPhoto.setChatId(chat_id);

        InputFile photoFile = new InputFile(photo);
        sendPhoto.setPhoto(photoFile);
        sendPhoto.setParseMode("html");


        try {
            execute(sendPhoto);
        } catch (TelegramApiException e) {
            System.out.println(e);
        }
    }

    public void sendPhoto(Long chat_id, File photo) {
        SendPhoto sendPhoto = new SendPhoto();
        sendPhoto.setChatId(chat_id);

        InputFile photoFile = new InputFile(photo);
        sendPhoto.setPhoto(photoFile);
        sendPhoto.setParseMode("html");


        try {
            execute(sendPhoto);
        } catch (TelegramApiException e) {
            System.out.println(e);
        }
    }

    public void sendPhoto(Long chat_id, String photo, InlineKeyboardMarkup inlineKeyboardMarkup) {
        SendPhoto sendPhoto = new SendPhoto();
        sendPhoto.setChatId(chat_id);

        InputFile photoFile = new InputFile(photo);
        sendPhoto.setPhoto(photoFile);

        sendPhoto.setParseMode("html");

        sendPhoto.setReplyMarkup(inlineKeyboardMarkup);
        executes(sendPhoto);
    }

    public void sendPhoto(Long chat_id, File photo, InlineKeyboardMarkup inlineKeyboardMarkup) {
        SendPhoto sendPhoto = new SendPhoto();
        sendPhoto.setChatId(chat_id);

        InputFile photoFile = new InputFile(photo);
        sendPhoto.setPhoto(photoFile);
        sendPhoto.setParseMode("html");

        sendPhoto.setReplyMarkup(inlineKeyboardMarkup);
        executes(sendPhoto);
    }

    public void sendPhoto(Long chat_id, File photo, ReplyKeyboardMarkup replyKeyboardMarkup) {
        SendPhoto sendPhoto = new SendPhoto();
        sendPhoto.setChatId(chat_id);

        InputFile photoFile = new InputFile(photo);
        sendPhoto.setPhoto(photoFile);

        sendPhoto.setParseMode("html");

        sendPhoto.setReplyMarkup(replyKeyboardMarkup);
        executes(sendPhoto);
    }

    public void sendPhoto(Long chat_id, File photo, String caption, ReplyKeyboardMarkup replyKeyboardMarkup) {
        SendPhoto sendPhoto = new SendPhoto();
        sendPhoto.setChatId(chat_id);

        InputFile photoFile = new InputFile(photo);
        sendPhoto.setPhoto(photoFile);
        sendPhoto.setCaption(caption);
        sendPhoto.setParseMode("html");

        sendPhoto.setReplyMarkup(replyKeyboardMarkup);
        executes(sendPhoto);
    }

    public void sendPhoto(Long chat_id, String photo, ReplyKeyboardMarkup replyKeyboardMarkup) {
        SendPhoto sendPhoto = new SendPhoto();
        sendPhoto.setChatId(chat_id);

        InputFile photoFile = new InputFile(photo);
        sendPhoto.setPhoto(photoFile);

        sendPhoto.setParseMode("html");

        sendPhoto.setReplyMarkup(replyKeyboardMarkup);
        executes(sendPhoto);
    }

    //////////////////////////////////////////////////////////////////////////////////
    public void sendPhoto(Long chat_id, String photo, String caption, Integer message_id) {
        SendPhoto sendPhoto = new SendPhoto();
        sendPhoto.setChatId(chat_id);

        InputFile photoFile = new InputFile(photo);
        sendPhoto.setPhoto(photoFile);
        sendPhoto.setParseMode("html");

        sendPhoto.setCaption(caption);
        sendPhoto.setReplyToMessageId(message_id);
        executes(sendPhoto);
    }

    public void sendPhoto(Long chat_id, String photo, String caption, Integer message_id, InlineKeyboardMarkup inlineKeyboardMarkup) {
        SendPhoto sendPhoto = new SendPhoto();
        sendPhoto.setChatId(chat_id);

        InputFile photoFile = new InputFile(photo);
        sendPhoto.setPhoto(photoFile);
        sendPhoto.setCaption(caption);
        sendPhoto.setParseMode("html");
        sendPhoto.setReplyToMessageId(message_id);
        sendPhoto.setReplyMarkup(inlineKeyboardMarkup);
        executes(sendPhoto);
    }

    public void sendPhoto(Long chat_id, String photo, String caption, Integer message_id, ReplyKeyboardMarkup replyKeyboardMarkup) {
        SendPhoto sendPhoto = new SendPhoto();
        sendPhoto.setChatId(chat_id);

        InputFile photoFile = new InputFile(photo);
        sendPhoto.setPhoto(photoFile);
        sendPhoto.setCaption(caption);
        sendPhoto.setParseMode("html");
        sendPhoto.setReplyToMessageId(message_id);
        sendPhoto.setReplyMarkup(replyKeyboardMarkup);
        executes(sendPhoto);
    }

    //////////////////////////////////////////////////////////////////////////////////
    public void sendPhoto(Long chat_id, String photo, String caption) {
        SendPhoto sendPhoto = new SendPhoto();
        sendPhoto.setChatId(chat_id);

        InputFile photoFile = new InputFile(photo);
        sendPhoto.setPhoto(photoFile);
        sendPhoto.setParseMode("html");

        sendPhoto.setCaption(caption);

        executes(sendPhoto);
    }

    public void sendPhoto(Long chat_id, String photo, String caption, InlineKeyboardMarkup inlineKeyboardMarkup) {
        SendPhoto sendPhoto = new SendPhoto();
        sendPhoto.setChatId(chat_id);

        InputFile photoFile = new InputFile(photo);
        sendPhoto.setPhoto(photoFile);
        sendPhoto.setCaption(caption);
        sendPhoto.setParseMode("html");

        sendPhoto.setReplyMarkup(inlineKeyboardMarkup);
        executes(sendPhoto);
    }

    public void sendPhoto(Long chat_id, String photo, String caption, ReplyKeyboardMarkup replyKeyboardMarkup) {
        SendPhoto sendPhoto = new SendPhoto();
        sendPhoto.setChatId(chat_id);

        InputFile photoFile = new InputFile(photo);
        sendPhoto.setPhoto(photoFile);
        sendPhoto.setCaption(caption);
        sendPhoto.setParseMode("html");

        sendPhoto.setReplyMarkup(replyKeyboardMarkup);
        executes(sendPhoto);
    }

    ////////////////////////SEND VIDEO/////////////////////////////////////////////

    public void sendVideo(Long chat_id, String video, Integer message_id) {
        SendVideo sendVideo = new SendVideo();
        sendVideo.setChatId(chat_id);

        InputFile videoFile = new InputFile(video);
        sendVideo.setVideo(videoFile);
        sendVideo.setParseMode("html");

        sendVideo.setReplyToMessageId(message_id);
        executes(sendVideo);
    }

    public void sendVideo(Long chat_id, String video, Integer message_id, InlineKeyboardMarkup inlineKeyboardMarkup) {
        SendVideo sendVideo = new SendVideo();
        sendVideo.setChatId(chat_id);

        InputFile videoFile = new InputFile(video);
        sendVideo.setVideo(videoFile);
        sendVideo.setParseMode("html");
        sendVideo.setReplyMarkup(inlineKeyboardMarkup);
        sendVideo.setReplyToMessageId(message_id);
        executes(sendVideo);
    }

    public void sendVideo(Long chat_id, String video, Integer message_id, ReplyKeyboardMarkup replyKeyboardMarkup) {
        SendVideo sendVideo = new SendVideo();
        sendVideo.setChatId(chat_id);

        InputFile videoFile = new InputFile(video);
        sendVideo.setVideo(videoFile);
        sendVideo.setParseMode("html");
        sendVideo.setReplyMarkup(replyKeyboardMarkup);
        sendVideo.setReplyToMessageId(message_id);
        executes(sendVideo);
    }
//////////////////////////////////////////////////////////////////////////////////

    public void sendVideo(Long chat_id, String video) {
        SendVideo sendVideo = new SendVideo();
        sendVideo.setChatId(chat_id);

        InputFile videoFile = new InputFile(video);
        sendVideo.setVideo(videoFile);
        sendVideo.setParseMode("html");
        executes(sendVideo);
    }

    public void sendVideo(Long chat_id, String video, InlineKeyboardMarkup inlineKeyboardMarkup) {
        SendVideo sendVideo = new SendVideo();
        sendVideo.setChatId(chat_id);
        sendVideo.setProtectContent(true);
        InputFile videoFile = new InputFile(video);
        sendVideo.setVideo(videoFile);
        sendVideo.setParseMode("html");
        sendVideo.setReplyMarkup(inlineKeyboardMarkup);
        executes(sendVideo);
    }

    public void sendVideo(Long chat_id, String video, ReplyKeyboardMarkup replyKeyboardMarkup) {
        SendVideo sendVideo = new SendVideo();
        sendVideo.setChatId(chat_id);

        InputFile videoFile = new InputFile(video);
        sendVideo.setVideo(videoFile);
        sendVideo.setParseMode("html");
        sendVideo.setReplyMarkup(replyKeyboardMarkup);
        executes(sendVideo);
    }

    //////////////////////////////////////////////////////////////////////////////////
    public void sendVideo(Long chat_id, String video, String caption, Integer message_id) {
        SendVideo sendVideo = new SendVideo();
        sendVideo.setChatId(chat_id);

        InputFile videoFile = new InputFile(video);
        sendVideo.setVideo(videoFile);
        sendVideo.setParseMode("html");

        sendVideo.setReplyToMessageId(message_id);
        executes(sendVideo);
    }

    public void sendVideo(Long chat_id, String video, String caption, Integer message_id, InlineKeyboardMarkup inlineKeyboardMarkup) {
        SendVideo sendVideo = new SendVideo();
        sendVideo.setChatId(chat_id);

        InputFile videoFile = new InputFile(video);
        sendVideo.setCaption(caption);
        sendVideo.setVideo(videoFile);
        sendVideo.setParseMode("html");
        sendVideo.setReplyMarkup(inlineKeyboardMarkup);
        sendVideo.setReplyToMessageId(message_id);
        executes(sendVideo);
    }

    public void sendVideo(Long chat_id, String video, String caption, Integer message_id, ReplyKeyboardMarkup replyKeyboardMarkup) {
        SendVideo sendVideo = new SendVideo();
        sendVideo.setChatId(chat_id);

        InputFile videoFile = new InputFile(video);
        sendVideo.setCaption(caption);
        sendVideo.setVideo(videoFile);
        sendVideo.setParseMode("html");
        sendVideo.setReplyMarkup(replyKeyboardMarkup);
        sendVideo.setReplyToMessageId(message_id);
        executes(sendVideo);
    }

    //////////////////////////////////////////////////////////////////////////////////
    public void sendVideo(Long chat_id, String video, String caption) {
        SendVideo sendVideo = new SendVideo();
        sendVideo.setChatId(chat_id);

        InputFile videoFile = new InputFile(video);
        sendVideo.setCaption(caption);
        sendVideo.setVideo(videoFile);
        sendVideo.setParseMode("html");
        executes(sendVideo);
    }

    public void sendVideo(Long chat_id, String video, String caption, InlineKeyboardMarkup inlineKeyboardMarkup) {
        SendVideo sendVideo = new SendVideo();
        sendVideo.setChatId(chat_id);

        InputFile videoFile = new InputFile(video);
        sendVideo.setCaption(caption);
        sendVideo.setVideo(videoFile);
        sendVideo.setParseMode("html");
        sendVideo.setReplyMarkup(inlineKeyboardMarkup);
        executes(sendVideo);
    }

    public void sendVideo(Long chat_id, String photo, String caption, ReplyKeyboardMarkup replyKeyboardMarkup) {
        SendPhoto sendPhoto = new SendPhoto();
        sendPhoto.setChatId(chat_id);

        InputFile photoFile = new InputFile(photo);
        sendPhoto.setPhoto(photoFile);
        sendPhoto.setCaption(caption);
        sendPhoto.setParseMode("html");

        sendPhoto.setReplyMarkup(replyKeyboardMarkup);
        executes(sendPhoto);
    }

    //////////////////////////////////////////////////////////////////////////////////


/////////////////EDIT MESSAGE TEXT

    public void editMessageText(Long chat_id, String text, Integer message_id, InlineKeyboardMarkup markup) {
        EditMessageText editMessageText = new EditMessageText();
        editMessageText.setChatId(chat_id);
        editMessageText.setParseMode("html");
        editMessageText.setText(text);
        editMessageText.setMessageId(message_id);
        editMessageText.setReplyMarkup(markup);
        executes(editMessageText);
    }


    public void editMessageText(Long chat_id, String text, Integer message_id) {
        EditMessageText editMessageText = new EditMessageText();
        editMessageText.setChatId(chat_id);
        editMessageText.setParseMode("html");
        editMessageText.setText(text);
        editMessageText.setMessageId(message_id);
        executes(editMessageText);
    }


    public void editMessagePhoto(Long chat_id, File file, String caption,
                                 Integer message_id, InlineKeyboardMarkup inlineKeyboardMarkup) {
        EditMessageMedia editMessageMedia = new EditMessageMedia();
        editMessageMedia.setChatId(chat_id);
        editMessageMedia.setReplyMarkup(inlineKeyboardMarkup);
        editMessageMedia.setMessageId(Math.toIntExact(message_id));
        InputMedia inputMedia = new InputMediaPhoto();
        inputMedia.setMedia(file, "file name");
        inputMedia.setParseMode("HTML");
        inputMedia.setCaption(caption);
        editMessageMedia.setMedia(inputMedia);
        executes(editMessageMedia);
    }

    public void getUserProfilePhotos(Long fromChatId, Long toChatId) {

        GetUserProfilePhotos getUserProfilePhotos = new GetUserProfilePhotos();
        getUserProfilePhotos.setUserId(fromChatId);


        UserProfilePhotos userProfilePhotos = null;
        try {
            userProfilePhotos = execute(getUserProfilePhotos);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }


        if (userProfilePhotos != null && !userProfilePhotos.getPhotos().isEmpty()) {
            for (List<PhotoSize> photo : userProfilePhotos.getPhotos()) {
                List<PhotoSize> photos = photo;
                String photoUrl = photos.get(photos.size() - 1).getFileId();
                sendPhoto(toChatId, photoUrl);
            }
        } else sendMessage(toChatId, "Profilingizda rasm mavjud emas yoki u botni hali ishga tushirmagan");
    }

    public void alertMessage(CallbackQuery callbackQuery, String alertMessageText) {
        String callbackQueryId = callbackQuery.getId();
        AnswerCallbackQuery answerCallbackQuery = new AnswerCallbackQuery();
        answerCallbackQuery.setShowAlert(true);
        answerCallbackQuery.setText(alertMessageText);
        answerCallbackQuery.setCallbackQueryId(callbackQueryId);
        executes(answerCallbackQuery);
    }
}
