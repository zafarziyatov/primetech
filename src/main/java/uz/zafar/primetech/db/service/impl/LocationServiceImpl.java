package uz.zafar.primetech.db.service.impl;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uz.zafar.primetech.db.domain.Location;
import uz.zafar.primetech.db.domain.User;
import uz.zafar.primetech.db.repositories.LocationRepository;
import uz.zafar.primetech.db.service.LocationService;
import uz.zafar.primetech.dto.ResponseDto;

import java.util.List;

@Log4j2
@Service
public class LocationServiceImpl implements LocationService {
    @Autowired
    private LocationRepository locationRepository;

    @Override
    public ResponseDto<List<Location>> findAllByUserId(User user) {
        try {
            return new ResponseDto<>(true, "Ok", locationRepository.findAllByUserId(user.getId()));
        } catch (Exception e) {
            log.error(e);
            return new ResponseDto<>(false, e.getMessage());
        }
    }

    @Override
    public ResponseDto save(Location location) {
        try {
            locationRepository.save(location);
            log.info("success save location");
            return new ResponseDto(true, "Ok");
        } catch (Exception e) {
            log.error(e);
            return new ResponseDto(false, e.getMessage());
        }
    }
}
