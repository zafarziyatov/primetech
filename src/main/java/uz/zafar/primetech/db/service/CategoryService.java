package uz.zafar.primetech.db.service;

import uz.zafar.primetech.db.domain.Category;
import uz.zafar.primetech.dto.ResponseDto;

import java.util.List;

public interface CategoryService {
    ResponseDto<Category>findByName(String name) ;
    ResponseDto<Category>findById(Long id) ;
    ResponseDto<List<Category>>findAll() ;
    ResponseDto save(Category category) ;
}
