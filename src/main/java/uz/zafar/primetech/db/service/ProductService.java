package uz.zafar.primetech.db.service;

import uz.zafar.primetech.db.domain.Product;
import uz.zafar.primetech.dto.ResponseDto;

import java.util.List;

public interface ProductService {
    ResponseDto save (Product product) ;
    ResponseDto deleteById (Long productId) ;
    ResponseDto<Product>findById (Long id) ;
    ResponseDto<Product>findByName (String name) ;
    ResponseDto<List<Product>>findAll (String name) ;
}
