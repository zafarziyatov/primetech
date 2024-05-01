package uz.zafar.primetech.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserDto {
    private Long id ;
    private Long chatId ;
    private String username ;
    private String nickname ;
}
