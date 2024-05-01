package uz.zafar.primetech.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ResponseDto<T> {
    private boolean success ;
    private String message ;
    private T data ;




    public ResponseDto(Boolean success, String message) {
        this.success = success;
        this.message = message;
    }
}
