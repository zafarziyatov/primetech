package uz.zafar.primetech.db.service;

import uz.zafar.primetech.db.domain.Basket;
import uz.zafar.primetech.dto.ResponseDto;

public interface BasketService {
    ResponseDto save(Basket basket);
}
