package uz.zafar.primetech.db.service;

import uz.zafar.primetech.db.domain.Location;
import uz.zafar.primetech.db.domain.User;
import uz.zafar.primetech.dto.ResponseDto;

import java.util.List;

public interface LocationService {
    ResponseDto<List<Location>>findAllByUserId(User user) ;
    ResponseDto save(Location location);

}
