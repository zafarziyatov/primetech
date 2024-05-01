package uz.zafar.primetech.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DtoLocation {
    private String fullAddress;
    private Double lat ;
    private Double lon ;
    private Double distance ;
}
