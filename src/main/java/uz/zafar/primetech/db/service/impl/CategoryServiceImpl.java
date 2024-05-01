package uz.zafar.primetech.db.service.impl;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import uz.zafar.primetech.db.domain.Category;
import uz.zafar.primetech.db.domain.Product;
import uz.zafar.primetech.db.repositories.CategoryRepository;
import uz.zafar.primetech.db.service.CategoryService;
import uz.zafar.primetech.dto.ResponseDto;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@Log4j2
public class CategoryServiceImpl implements CategoryService {
    @Autowired
    private CategoryRepository categoryRepository;

    @Override
    public ResponseDto<List<Category>> findAll() {
        try {
            log.info("Success categories function is findAll from categoryRepository");
            List<Category> list = new java.util.ArrayList<>(categoryRepository.findAll(Sort.by("id")).stream().filter(category -> category.getSuccess() && category.getStatus().equals("open")).toList());
            list.sort(Comparator.comparingLong(Category::getId));
            return new ResponseDto<>(true, "Ok", list);
        } catch (Exception e) {
            log.error(e);
            return new ResponseDto<>(false, e.getMessage());
        }
    }

    @Override
    public ResponseDto save(Category category) {
        try {
            categoryRepository.save(category);
            return new ResponseDto(true, "OK");
        } catch (Exception e) {
            log.error(e);
            return new ResponseDto(false, e.getMessage());
        }
    }

    @Override
    public ResponseDto<Category> findByName(String name) {
        try {
            Category category = categoryRepository.findByName(name);
            if (category == null) {
                log.error("Categoriya toplmadi");
                return new ResponseDto<>(false, "Category topilmadi");
            }
            log.info("Success find category");
            return new ResponseDto<>(true, "Ok", category);
        } catch (Exception e) {
            log.error(e);
            return new ResponseDto<>(false, e.getMessage());
        }
    }

    @Override
    public ResponseDto<Category> findById(Long id) {
        try {
            Optional<Category> cOp = categoryRepository.findById(id);
            if (cOp.isEmpty())
                return new ResponseDto<>(false,
                        "Nimgadir topilmadi");
            return new ResponseDto<>(true, "Ok", cOp.get());
        } catch (Exception e) {
            log.error(e.getMessage());
            return new ResponseDto<>(false, e.getMessage());
        }
    }
}
