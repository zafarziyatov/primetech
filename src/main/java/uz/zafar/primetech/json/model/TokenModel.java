package uz.zafar.primetech.json.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TokenModel {
    private String message ;
    private Data data ;
    private String token_type ;
}
