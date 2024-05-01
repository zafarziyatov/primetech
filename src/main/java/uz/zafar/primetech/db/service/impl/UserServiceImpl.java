package uz.zafar.primetech.db.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import uz.zafar.primetech.bot.TelegramBot;
import uz.zafar.primetech.db.domain.Basket;
import uz.zafar.primetech.db.domain.User;
import uz.zafar.primetech.db.repositories.BasketRepository;
import uz.zafar.primetech.db.repositories.UserRepository;
import uz.zafar.primetech.dto.ResponseDto;
import uz.zafar.primetech.db.service.UserService;
import uz.zafar.primetech.dto.UserDto;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Log4j2
@Transactional
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private BasketRepository basketRepository;

    @Override
    public ResponseDto<User> findByChatId(Long chatId) {
        try {
            User user = userRepository.findByChatId(chatId);
            if (user == null) {
                log.error("Nimagadir user topilmadi");
                return new ResponseDto<>(false, "Nimagadir user topilmadi");
            }
            log.info("success find user");
            return new ResponseDto<>(true, "Ok", user);
        } catch (Exception e) {
            log.error(e);
            return new ResponseDto<>(false, e.getMessage());
        }
    }

    @Override
    public ResponseDto<User> findById(Long chatId) {
        try {
            Optional<User>op = userRepository.findById(chatId);
            if (op.isEmpty())
                return new ResponseDto<>(false,"Topilmadi");
            ResponseDto<User> ok = new ResponseDto<>(true,
                    "Ok",
                    op.get());
            return ok;
        } catch (Exception e) {
            return new ResponseDto<>(false, e.getMessage());
        }
    }

    @Override
    public ResponseDto<User> addUserBasket(User user, Basket basket) {
        try {
            user = userRepository.findById(user.getId()).orElse(null);
            assert user != null;
            user.getBaskets().add(basket);
            userRepository.save(user);
            return new ResponseDto<>(true, "Ok", user);
        } catch (Exception e) {
            log.error(e);
            return new ResponseDto<>(false, e.getMessage());
        }
    }

    @Override
    public ResponseDto<User> removeUserBasket(Long userId, Long basketId) {
        try {
            User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found"));
            Basket basket = basketRepository.findById(basketId).orElseThrow(() -> new IllegalArgumentException("Basket not found"));
            basket.setActive(false);
            basketRepository.save(basket);
            return new ResponseDto<>(true, "Ok", user);
        } catch (IllegalArgumentException e) {
            log.error(e);
            return new ResponseDto<>(false, "Nimagadir user topilmadi");
        }
    }

    @Override
    public ResponseDto<Page<User>> getAll(int page, int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<User> userPage = userRepository.findAll(pageable);
            return new ResponseDto<>(true, "Users retrieved successfully", userPage);
        } catch (Exception e) {
            log.error("Error occurred while retrieving users", e);
            return new ResponseDto<>(false, "Failed to retrieve users");
        }
    }

    @Override
    public ResponseDto<Page<User>> findAllByUsername(String username ,int page, int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<User> userPage = userRepository.findAllByUsername(username,pageable);
            return new ResponseDto<>(true, "Users retrieved successfully", userPage);
        } catch (Exception e) {
            log.error(e);
            return new ResponseDto<>(false, "Failed to retrieve users");
        }
    }

    @Override
    public ResponseDto<Page<User>> findAllByNickname(String nickname,int page, int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<User> userPage = userRepository.findAllByNickname(nickname,pageable);
            return new ResponseDto<>(true, "Users retrieved successfully", userPage);
        } catch (Exception e) {
            log.error(e);
            return new ResponseDto<>(false, "Failed to retrieve users");
        }

    }

    @Override
    public ResponseDto<List<UserDto>> findAll() {
        try {
            List<UserDto>list = new ArrayList<>();
            for (User user : userRepository.findAll()) {
                UserDto dto = new UserDto();
                dto.setId(user.getId());
                dto.setChatId(user.getChatId());
                dto.setNickname(user.getNickname());
                dto.setUsername(user.getUsername());
                list.add(dto);
            }
            return new ResponseDto<>(true , "Ok",list);
        } catch (Exception e) {
            log.error(e);
            return new ResponseDto<>(false , e.getMessage());
        }
    }

    @Override
    public ResponseDto save(User user) {
        try {
            userRepository.save(user);
            return new ResponseDto(true, "Ok");
        } catch (Exception e) {
            log.error(e.getMessage());
            return new ResponseDto(false, e.getMessage());
        }
    }

    @Override
    public ResponseDto<List<User>> findByRole(String role) {
        try {
            return new ResponseDto<>(true, "Ok", userRepository.findAllByRole(role));
        } catch (Exception e) {
            log.error(e);
            return new ResponseDto<>(false, e.getMessage());
        }
    }
    @Override
    public ResponseDto<Page<User>> findByRole(String role,int page , int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<User> userPage = userRepository.findAllByRole(role,pageable);
            return new ResponseDto<>(true, "Users retrieved successfully", userPage);
        } catch (Exception e) {
            log.error(e);
            return new ResponseDto<>(false, e.getMessage());
        }
    }

}
