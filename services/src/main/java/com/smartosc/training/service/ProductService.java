package com.smartosc.training.service;

import com.smartosc.training.dto.CategoryDTO;
import com.smartosc.training.dto.ProductDTO;
import com.smartosc.training.dto.PromotionDTO;
import com.smartosc.training.entity.Category;
import com.smartosc.training.entity.Product;
import com.smartosc.training.entity.ProductPromotion;
import com.smartosc.training.entity.Promotion;
import com.smartosc.training.repositories.CategoryRepository;
import com.smartosc.training.repositories.ProductPromotionRepository;
import com.smartosc.training.repositories.ProductRepository;
import com.smartosc.training.repositories.PromotionRepository;
import com.smartosc.training.utils.ConvertUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private PromotionRepository promotionRepository;

    @Autowired
    private ProductPromotionRepository productPromotionRepository;

    @Autowired
    private ModelMapper modelMapper;

    public List<ProductDTO> getAllProduct() {
        List<Product> products = productRepository.findAll();
        List<ProductDTO> listProductDTO = new ArrayList<>();
        products.forEach(p -> {
            ProductDTO productDTO = modelMapper.map(p, ProductDTO.class);
            List<Category> categories = p.getCategories();
            List<CategoryDTO> listCategoryDTO = new ArrayList<>();
            categories.forEach(c -> {
                CategoryDTO categoryDTO = new CategoryDTO();
                categoryDTO.setCategoryId(c.getCategoryId());
                categoryDTO.setCategoryName(c.getName());
                categoryDTO.setDescription(c.getDescription());
                categoryDTO.setImage(c.getImage());
                listCategoryDTO.add(categoryDTO);
            });
            productDTO.setCategories(listCategoryDTO);
            listProductDTO.add(productDTO);
        });
        return listProductDTO;
    }

    private ProductDTO convertProductDTO(Product p) {
        ProductDTO productDTO = new ProductDTO();
        productDTO.setProductId(p.getProductId());
        productDTO.setProductName(p.getName());
        productDTO.setDescription(p.getDescription());
        productDTO.setImage(p.getImage());
        productDTO.setPrice(p.getPrice());
        List<ProductPromotion> list = p.getProductPromotions();
        if (!list.isEmpty()) {
            List<Promotion> promotions = list.stream().map(ProductPromotion::getPromotion).collect(Collectors.toList());
            Promotion result = promotions.stream().max(Comparator.comparing(Promotion::getPercent)).get();
            if (result.getStatus() != 0) {
                List<PromotionDTO> results = new ArrayList<>();
                results.add(ConvertUtils.convertPromotionToPromotionDTO(result));
                productDTO.setPromotions(results);
            } else {
                productDTO.setPromotions(null);
            }
        } else {
            productDTO.setPromotions(null);
        }
        return productDTO;
    }

    public Page<ProductDTO> getAllCategoryById(Integer categoryId, Pageable pageable) {
        Page<Product> products = productRepository.getAllProductBycategory(categoryId, pageable);
        return products.map(this::convertProductDTO);
    }

    public List<ProductDTO> getProductByCategoryName(String categoryName) {
        List<Product> listProduct = productRepository.findByCategories_Name(categoryName);
        List<ProductDTO> listProductDTO = new ArrayList<>();
        listProduct.forEach(p -> {
            ProductDTO productDTO = new ProductDTO();
            productDTO.setProductId(p.getProductId());
            productDTO.setProductName(p.getName());
            productDTO.setDescription(p.getDescription());
            productDTO.setImage(p.getImage());
            productDTO.setPrice(p.getPrice());
            listProductDTO.add(productDTO);
        });
        return listProductDTO;
    }

    public List<ProductDTO> getProductByCategory(Integer categoryId) {
        List<Product> listProduct = productRepository.findByCategories_CategoryId(categoryId);
        List<ProductDTO> listProductDTO = new ArrayList<>();
        listProduct.forEach(p -> {
            ProductDTO productDTO = new ProductDTO();
            productDTO.setProductId(p.getProductId());
            productDTO.setProductName(p.getName());
            productDTO.setDescription(p.getDescription());
            productDTO.setImage(p.getImage());
            productDTO.setPrice(p.getPrice());
            listProductDTO.add(productDTO);
        });
        return listProductDTO;
    }

    public List<ProductDTO> findByNameProduct(String nameProduct) {
        List<Product> products = productRepository.findByNameProduct(nameProduct);
        List<ProductDTO> productDTO = new ArrayList<>();
        products.forEach(p -> {
            ProductDTO productDTO1 = modelMapper.map(p, ProductDTO.class);
            productDTO.add(productDTO1);
        });
        return productDTO;
    }

    public ProductDTO findById(Integer idProduct) {
        Optional<Product> response = productRepository.findById(idProduct);
        if (response.isPresent()) {
            return ConvertUtils.convertProductToProductDTO(response.get());
        } else return null;
    }

    public ProductDTO addProduct(ProductDTO productDTO) {
        Product product = modelMapper.map(productDTO, Product.class);
        List<Integer> listId = Arrays.stream(productDTO.getCategoryIds()).boxed().collect(Collectors.toList());
        List<Category> categories = categoryRepository.findAllById(listId);
        product.setCategories(categories);
        product.setStatus(1);
        Product responseData = productRepository.save(product);
        return ConvertUtils.convertProductToProductDTO(responseData);
    }

    public void updateProduct(ProductDTO productDTO, Integer idProduct) {
        Product product = modelMapper.map(productDTO, Product.class);
        product.setProductId(idProduct);
        List<Integer> listId = Arrays.stream(productDTO.getCategoryIds()).boxed().collect(Collectors.toList());
        List<Category> categories = categoryRepository.findAllById(listId);
        product.setCategories(categories);
        productRepository.save(product);
        modelMapper.map(product, ProductDTO.class);
    }

    public void deleteProductById(int id) {
        productRepository.deleteById(id);
    }

    /**
     * Get all product in page style
     *
     * @param searchValue
     * @param pageNo
     * @param sizeNo
     * @param sortBy
     * @return a page product
     */
    public Page<ProductDTO> getAllProduct(String searchValue, Integer pageNo, Integer sizeNo, String sortBy) {
        Pageable pageable = PageRequest.of(pageNo, sizeNo, Sort.by(sortBy));
        Page<Product> pageResult = productRepository.searchByNameAndDes(searchValue, pageable);
        return pageResult.map(ConvertUtils::convertProductToProductDTO);
    }

    public Long countProducts() {
        return productRepository.count();
    }

    public boolean importProduct(List<ProductDTO> listProductDTO) {
        try {
            listProductDTO.forEach(p -> {
                Product product = modelMapper.map(p, Product.class);
                List<CategoryDTO> listCategoryDTO = p.getCategories();
                List<Category> listCategory = new ArrayList<>();
                listCategoryDTO.forEach(c -> {
                    Category category = categoryRepository.findByOneName(c.getCategoryName());
                    listCategory.add(category);
                });
                product.setCategories(listCategory);
                productRepository.save(product);
            });
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public Boolean saveAllProducts(List<ProductDTO> users) {
        List<Product> prepareData = users.stream()
                .map(o -> modelMapper.map(o, Product.class)).collect(Collectors.toList());
        List<Product> result = productRepository.saveAll(prepareData);
        return !result.isEmpty();
    }

    /**
     * get top 8 hot products
     *
     * @return
     */
    public List<ProductDTO> getHotProducts() {
        List<Product> products = productRepository.findHotProducts();
        return products.stream().map(ConvertUtils::convertProductToProductDTO).collect(Collectors.toList());
    }

    /**
     * get newest promoted products top 8
     *
     * @return
     */
    public List<ProductDTO> getTop8NewestPromotedProducts(){
        Pageable pageable = PageRequest.of(0, 8, Sort.by(Sort.Direction.DESC, "updatedAt"));
        List<Product> products = productRepository.getTop8NewestPromotedProducts(pageable);
        return products.stream().map(ConvertUtils::convertProductToProductDTO).collect(Collectors.toList());
    }

    /**
     * get all newest products in homepage include promoted/not
     *
     * @return
     */
    public List<ProductDTO> getTopNewProducts() {
        Page<Product> productResult = productRepository.findAll(
                PageRequest.of(0, 8, Sort.by(Sort.Direction.DESC, "updatedAt")));
        List<Product> products = productResult.getContent();
        return products.stream()
                .map(ConvertUtils::convertProductToProductDTO).collect(Collectors.toList());
    }

    public Page<ProductDTO> getAllProductAndPromotion(Pageable pageable) {
        Page<Product> pageProduct = productRepository.getAllProductAndPromotion(pageable);
        return pageProduct.map(ConvertUtils::convertProductToProductDTO);
    }

    public Page<ProductDTO> getAllProduct(Pageable pageable) {
        Page<Product> pageProduct = productRepository.findAll(pageable);
        return pageProduct.map(ConvertUtils::convertProductToProductDTO);
    }
}