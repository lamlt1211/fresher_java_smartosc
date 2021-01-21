package com.smartosc.training.service;

import com.smartosc.training.dto.CategoryDTO;
import com.smartosc.training.dto.ProductDTO;
import com.smartosc.training.entity.Category;
import com.smartosc.training.entity.Product;
import com.smartosc.training.repositories.CategoryRepository;
import com.smartosc.training.repositories.ProductRepository;
import com.smartosc.training.utils.ConvertUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ModelMapper modelMapper;

    public List<CategoryDTO> findByNameCategory(String name) {
        List<Category> category = categoryRepository.findByName(name);
        List<CategoryDTO> categoryDTO = new ArrayList<>();
        category.forEach(p -> {
            CategoryDTO categoryDTO1 = modelMapper.map(p, CategoryDTO.class);
            categoryDTO.add(categoryDTO1);
        });
        return categoryDTO;
    }

    public CategoryDTO findById(Integer id) {
        Optional<Category> response = categoryRepository.findById(id);
        Category categoryOp = new Category();
        if (response.isPresent()) {
            categoryOp = response.get();
        }
        CategoryDTO categoryDTO = modelMapper.map(categoryOp, CategoryDTO.class);
        List<Product> products = categoryOp.getProducts();
        List<ProductDTO> listProductDTO = new ArrayList<>();
        products.forEach(p -> {
            ProductDTO productDTO = new ProductDTO();
            productDTO.setProductId(p.getProductId());
            productDTO.setProductName(p.getName());
            productDTO.setDescription(p.getDescription());
            productDTO.setImage(p.getImage());
            productDTO.setPrice(p.getPrice());
            productDTO.setStatus(p.getStatus());
            listProductDTO.add(productDTO);
        });
        categoryDTO.setProducts(listProductDTO);
        return categoryDTO;
    }

    public Category addCategory(CategoryDTO categoryDTO) {
        Category category = modelMapper.map(categoryDTO, Category.class);
        return categoryRepository.save(category);
    }

    public boolean addCategoryImport(List<CategoryDTO> categoryDTOList) {
        List<Category> categories = categoryDTOList.stream().map(o -> modelMapper.map(o, Category.class)).collect(Collectors.toList());
        categoryRepository.saveAll(categories);
        return true;
    }

    public void updateCategory(CategoryDTO categoryDTO, int id) {
        Category category = modelMapper.map(categoryDTO, Category.class);
        category.setCategoryId(id);
        List<Product> products = productRepository.findByCategories_CategoryId(id);
        category.setProducts(products);
        categoryRepository.save(category);
        modelMapper.map(category, CategoryDTO.class);
    }

    /**
     * Delete Category
     *
     * 
     * @param id
     */
    public void deleteCategory(int id) {
        categoryRepository.deleteById(id);
    }

    /**
     * Get all category in page style
     *
     * @param searchValue
     * @param pageNo
     * @param pageSize
     * @param sortBy
     * @return a page category
     */
    public Page<CategoryDTO> getAllCategory(String searchValue, Integer pageNo, Integer pageSize, String sortBy) {
        Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by(sortBy));
        Page<Category> pageResult = categoryRepository.findBySearchValue(searchValue, pageable);
        return pageResult.map(ConvertUtils::convertCategoryToCategoryDTO);
    }

    /**
     * Get list category
     *
     * @return list category
     */
    public List<CategoryDTO> getAllCategory() {
        List<Category> categories = categoryRepository.findAll();
        return categories.stream()
                .map(ConvertUtils::convertCategoryToCategoryDTO)
                .collect(Collectors.toList());
    }

    public Long countCategory() {
        return categoryRepository.count();
    }

    public List<CategoryDTO> getAllCateByProductName(String productName) {
        List<Category> categories = categoryRepository.findByProducts_Name(productName);
        return categories != null ? categories.stream().map(ConvertUtils::convertCategoryToCategoryDTO).collect(Collectors.toList()) : null;
    }
}
