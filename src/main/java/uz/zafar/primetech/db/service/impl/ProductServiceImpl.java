package uz.zafar.primetech.db.service.impl;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uz.zafar.primetech.db.domain.Product;
import uz.zafar.primetech.db.repositories.ProductRepository;
import uz.zafar.primetech.db.service.ProductService;
import uz.zafar.primetech.dto.ResponseDto;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@Log4j2
public class ProductServiceImpl implements ProductService {
    @Autowired
    private ProductRepository productRepository;

    @Override
    public ResponseDto save(Product product) {
        try {
            productRepository.save(product);
            return new ResponseDto(true, "Ok");
        } catch (Exception e) {
            log.error(e);
            return new ResponseDto(true, e.getMessage());
        }
    }

    @Override
    public ResponseDto deleteById(Long productId) {
        try {
            productRepository.deleteById(productId);
            return new ResponseDto(true, "Ok");
        } catch (Exception e) {
            log.error(e);
            return new ResponseDto(false, e.getMessage());
        }
    }

    @Override
    public ResponseDto<Product> findById(Long id) {
        try {
            Optional<Product> pOp = productRepository.findById(id);
            return pOp.map(product -> new ResponseDto<>(true, "Ok", product)).orElseGet(() -> new ResponseDto<>(false, "Nimgadir topilmadi"));
        } catch (Exception e) {
            log.error(e);
            return new ResponseDto<>(false, e.getMessage());
        }
    }

    @Override
    public ResponseDto<Product> findByName(String name) {
        try {
            for (Product product : productRepository.findAll()) {
                if (
                        product.getNameUz().equals(name) ||
                        product.getNameRu().equals(name) ||
                        product.getNameEn().equals(name)
                ) {
                    return new ResponseDto<>(true, "Ok", product);
                }
            }
            return new ResponseDto<>(false, "Bunday nomli product mavjud emas");
        } catch (Exception e) {
            log.error(e);
            return new ResponseDto<>(false, e.getMessage());
        }
    }

    @Override
    public ResponseDto<List<Product>> findAll(String name) {
        try {
            List<Product> products = productRepository.findAll();
            products.sort(Comparator.comparingLong(Product::getId));
            return new ResponseDto<>(true, "Ok", products);
        } catch (Exception e) {
            log.error(e);
            return new ResponseDto<>(false, e.getMessage());
        }
    }
}
