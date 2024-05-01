package uz.zafar.primetech.db.service;


import org.springframework.data.domain.Page;
import uz.zafar.primetech.db.domain.Basket;
import uz.zafar.primetech.db.domain.User;
import uz.zafar.primetech.dto.ResponseDto;
import uz.zafar.primetech.dto.UserDto;

import java.util.List;

public interface UserService {
    ResponseDto<User> findByChatId(Long chatId) ;
    ResponseDto<User> findById(Long chatId) ;
    ResponseDto save (User user) ;
    ResponseDto<Page<User>> findByRole (String role,int page , int size) ;
    ResponseDto<List<User>> findByRole (String role) ;
    ResponseDto<User>addUserBasket(User user, Basket basket);
    ResponseDto<User>removeUserBasket(Long userId, Long basketId);
    ResponseDto<Page<User>>getAll(int page, int size);
    ResponseDto<Page<User>>findAllByUsername(String username,int page, int size);
    ResponseDto<Page<User>>findAllByNickname(String nickname,int page, int size);
    ResponseDto<List<UserDto>>findAll();
}
