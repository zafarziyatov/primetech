package uz.zafar.primetech.db.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uz.zafar.primetech.db.domain.Basket;
import uz.zafar.primetech.db.repositories.BasketRepository;
import uz.zafar.primetech.db.service.BasketService;
import uz.zafar.primetech.dto.ResponseDto;
@Service
@Log4j2
@RequiredArgsConstructor
public class BasketServiceImpl implements BasketService {
    @Autowired
    private BasketRepository basketRepository;

    @Override
    public ResponseDto save(Basket basket) {
        boolean success = true;
        try {
            basketRepository.save(basket);
        } catch (Exception e) {
            log.error(e);
            success = false;
        }
        return new ResponseDto(success, "Successfully saved the basket");
    }
}
