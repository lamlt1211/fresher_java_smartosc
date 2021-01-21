package com.smartosc.training.controller;

import com.smartosc.training.dto.PageMetaData;
import com.smartosc.training.dto.ProductDTO;
import com.smartosc.training.entity.APIResponse;
import com.smartosc.training.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/")
public class ProductController {

    @Autowired
    private ProductService productService;
    
    @GetMapping("products")
    public ResponseEntity<APIResponse<Page<ProductDTO>>> getAllProducts(
			@RequestParam(defaultValue = "", required = false) String searchValue,
			@RequestParam(defaultValue = "0", required = false) Integer page,
			@RequestParam(defaultValue = "8", required = false) Integer size,
			@RequestParam(defaultValue = "productId", required = false) String sortBy) {
        Page<ProductDTO> products = productService.getAllProduct(searchValue, page, size, sortBy);
        APIResponse<Page<ProductDTO>> responseData = new APIResponse<>();
        responseData.setStatus(HttpStatus.OK.value());
        responseData.setData(products);
        responseData.setMessage("get all products successful");
        return new ResponseEntity<>(responseData, HttpStatus.OK);
    }


    @GetMapping("products/export")
    public ResponseEntity<APIResponse<List<ProductDTO>>> exportProducts() {
        List<ProductDTO> products = productService.getAllProduct();
        APIResponse<List<ProductDTO>> responseData = new APIResponse<>();
        responseData.setStatus(HttpStatus.OK.value());
        responseData.setData(products);
        responseData.setMessage("get successful");
        return new ResponseEntity<>(responseData, HttpStatus.OK);
    }

    @GetMapping("products/name/{name}")
    public ResponseEntity<APIResponse<List<ProductDTO>>> findByNameProduct(@PathVariable("name") String name) {

        List<ProductDTO> productDTO = productService.findByNameProduct(name);
        APIResponse<List<ProductDTO>> responseData = new APIResponse<>();
        responseData.setMessage("Find by Id successful");
        responseData.setData(productDTO);
        responseData.setStatus(HttpStatus.OK.value());

        return new ResponseEntity<>(responseData, HttpStatus.OK);
    }

    @GetMapping("products/{id}")
    public ResponseEntity<Object> findById(@PathVariable("id") Integer idProduct) {
        ProductDTO productDTO = productService.findById(idProduct);
        APIResponse<ProductDTO> responseData = new APIResponse<>();
        responseData.setStatus(HttpStatus.OK.value());

        responseData.setMessage("Find by Id successful");
        responseData.setData(productDTO);
        return new ResponseEntity<>(responseData, HttpStatus.OK);
    }

    @PostMapping("products")
    public ResponseEntity<APIResponse<ProductDTO>> addProduct(@RequestBody ProductDTO productDTO) {
        ProductDTO result = productService.addProduct(productDTO);
        APIResponse<ProductDTO> responseData = new APIResponse<>();
        responseData.setStatus(HttpStatus.OK.value());
        responseData.setMessage("Add successful");
        responseData.setData(result);
        return new ResponseEntity<>(responseData, HttpStatus.OK);
    }
    @PostMapping("products/import")
    public ResponseEntity<Object> importProduct(@RequestBody List<ProductDTO> listProductDTO) {
        Boolean check = productService.importProduct(listProductDTO);
        APIResponse<Boolean> responseData = new APIResponse<>();
        responseData.setStatus(HttpStatus.OK.value());
        responseData.setMessage("Add successful");
        responseData.setData(check);
        return new ResponseEntity<>(responseData, HttpStatus.OK);
    }

    @DeleteMapping("products/{id}")
    public ResponseEntity<Object> deleteProductById(@PathVariable(name = "id") int id) {
        productService.deleteProductById(id);
        APIResponse<ProductDTO> responseData = new APIResponse<>();
        responseData.setStatus(HttpStatus.OK.value());
        responseData.setMessage("Delete successful");
        return new ResponseEntity<>(responseData, HttpStatus.OK);
    }

    @PutMapping("products/{id}")
    public ResponseEntity<Object> updateProduct(@RequestBody ProductDTO productDTO, @PathVariable("id") Integer idProduct) {
        productService.updateProduct(productDTO, idProduct);
        APIResponse<ProductDTO> responseData = new APIResponse<>();
        responseData.setStatus(HttpStatus.OK.value());
        responseData.setMessage("Update successful");
        return new ResponseEntity<>(responseData, HttpStatus.OK);
    }
    
    @GetMapping("products/promotions/{page}/{size}")
    public ResponseEntity<Object> listProduct(
            @PathVariable("page") Integer page,
            @PathVariable("size") Integer size) {

        Page<ProductDTO> productPage = productService.getAllProduct(PageRequest.of(page,size));
        APIResponse<List<ProductDTO>> responseData = new APIResponse<>();
        responseData.setStatus(HttpStatus.OK.value());
        responseData.setMessage("get all page successful");
        if(productPage != null && !CollectionUtils.isEmpty(productPage.getContent())) {
            responseData.setData(productPage.getContent());
            PageMetaData metaData = new PageMetaData(page, size, productPage.getTotalPages(),productPage.getNumber(), productPage.getTotalElements(),productPage.hasPrevious(),productPage.hasNext());
            responseData.setPageMetadata(metaData);
        }
        return new ResponseEntity<>(responseData, HttpStatus.OK);
    }
    @GetMapping("products/{page}/{size}")
    public ResponseEntity<Object> listProductHasPromotion(
            @PathVariable("page") Integer page,
            @PathVariable("size") Integer size) {

        Page<ProductDTO> productPage = productService.getAllProductAndPromotion(PageRequest.of(page,size));
        APIResponse<List<ProductDTO>> responseData = new APIResponse<>();
        responseData.setStatus(HttpStatus.OK.value());
        responseData.setMessage("get all page successful");
        if(productPage != null && !CollectionUtils.isEmpty(productPage.getContent())) {
            responseData.setData(productPage.getContent());
            PageMetaData metaData = new PageMetaData(page, size, productPage.getTotalPages(),productPage.getNumber(), productPage.getTotalElements(),productPage.hasPrevious(),productPage.hasNext());
            responseData.setPageMetadata(metaData);
        }
        return new ResponseEntity<>(responseData, HttpStatus.OK);
    }
    
    @GetMapping("products/{categoryId}/{page}/{size}")
    public ResponseEntity<Object> getAllCategoryById(
            @PathVariable("categoryId") Integer categoryId,
            @PathVariable("page") Integer page,
            @PathVariable("size") Integer size) {

        Pageable pageable = PageRequest.of(page,size);
        Page<ProductDTO> productPage = productService.getAllCategoryById(categoryId,pageable);
        APIResponse<List<ProductDTO>> responseData = new APIResponse<>();
        responseData.setStatus(HttpStatus.OK.value());
        responseData.setMessage("get product by category successful");
        if(productPage != null && !CollectionUtils.isEmpty(productPage.getContent())) {
            responseData.setData(productPage.getContent());
            PageMetaData metaData = new PageMetaData(page, size, productPage.getTotalPages(),productPage.getNumber(), productPage.getTotalElements(),productPage.hasPrevious(),productPage.hasNext());
            responseData.setPageMetadata(metaData);
        }
        return new ResponseEntity<>(responseData, HttpStatus.OK);
    }

    @GetMapping("/shop/{categoryId}")
    public ResponseEntity<Object> getProductByCategy(@PathVariable("categoryId") Integer idCategory) {
        List<ProductDTO> productDTO = productService.getProductByCategory(idCategory);
        APIResponse<List<ProductDTO>> responseData = new APIResponse<>();
        responseData.setStatus(HttpStatus.OK.value());

        responseData.setMessage("Find by Category successful");
        responseData.setData(productDTO);
        return new ResponseEntity<>(responseData, HttpStatus.OK);
    }
    
    //    filter product by category name
    @GetMapping("/category/name/{name}")
    public ResponseEntity<Object> getProductByCategoryName(@PathVariable("name") String categoryName){
        List<ProductDTO> productDTO = productService.getProductByCategoryName(categoryName);
        APIResponse<List<ProductDTO>> responseData = new APIResponse<>();
        responseData.setStatus(HttpStatus.OK.value());

        responseData.setMessage("Find product by category successful");
        responseData.setData(productDTO);

        return new ResponseEntity<>(responseData, HttpStatus.OK);
    }
    
    @GetMapping("products/count")
	public ResponseEntity<APIResponse<Long>> getNumberOfOrderOnHold() {
		Long productNum = productService.countProducts();
		APIResponse<Long> responseData = new APIResponse<>();
		responseData.setStatus(HttpStatus.OK.value());
		responseData.setData(productNum);
		responseData.setMessage("get number of order on hold successfull");
		return new ResponseEntity<>(responseData, HttpStatus.OK);
	}
    
    @GetMapping("products/newest-all")
    public ResponseEntity<APIResponse<List<ProductDTO>>> getTopNewProduct() {
    	List<ProductDTO> products = productService.getTopNewProducts();
    	APIResponse<List<ProductDTO>> responseData = new APIResponse<>();
		responseData.setStatus(HttpStatus.OK.value());
		responseData.setData(products);
		responseData.setMessage("get list new product successfull");
		return new ResponseEntity<>(responseData, HttpStatus.OK);
    }
    @GetMapping("products/all")
    public ResponseEntity<APIResponse<List<ProductDTO>>> getAllProducts() {
        List<ProductDTO> users = productService.getAllProduct();
        APIResponse<List<ProductDTO>> responseData = new APIResponse<>();
        responseData.setStatus(HttpStatus.OK.value());
        responseData.setData(users);
        responseData.setMessage("Get all product successfully!");
        return new ResponseEntity<>(responseData, HttpStatus.OK);
    }

    @PostMapping("products/save_all")
    public ResponseEntity<APIResponse<Boolean>> saveAllUsers(@RequestBody List<ProductDTO> users) {
        Boolean result = productService.saveAllProducts(users);
        APIResponse<Boolean> responseData = new APIResponse<>();
        responseData.setStatus(HttpStatus.OK.value());
        responseData.setData(result);
        responseData.setMessage("Insert all product successfully!");
        return new ResponseEntity<>(responseData, HttpStatus.OK);
    }

    @GetMapping("products/hot")
    public ResponseEntity<APIResponse<List<ProductDTO>>> getHotProducts(){
        List<ProductDTO> productDTOS = productService.getHotProducts();
        APIResponse<List<ProductDTO>> responseData = new APIResponse<>();
        responseData.setStatus(HttpStatus.OK.value());
        responseData.setData(productDTOS);
        responseData.setMessage("Get hot product successfully!");
        return new ResponseEntity<>(responseData, HttpStatus.OK);
    }

    /**
     * Get newest promoted products
     * @return
     */
    @GetMapping("products/newest-promoted")
    public ResponseEntity<APIResponse<List<ProductDTO>>> getTop8NewestPromotedProducts(){
        List<ProductDTO> productDTOList = productService.getTop8NewestPromotedProducts();
        APIResponse<List<ProductDTO>> responseData = new APIResponse<>();
        responseData.setStatus(HttpStatus.OK.value());
        responseData.setData(productDTOList);
        responseData.setMessage("get list newest product successfully");
        return new ResponseEntity<>(responseData, HttpStatus.OK);
    }
}

