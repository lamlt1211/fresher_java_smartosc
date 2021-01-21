package com.smartosc.training.controller;

import com.smartosc.training.dto.CategoryDTO;
import com.smartosc.training.entity.APIResponse;
import com.smartosc.training.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    HttpHeaders responseHeaders = new HttpHeaders();

    @GetMapping("categories/name/{name}")
    public ResponseEntity<Object> findByNameCategory(@PathVariable("name") String name) {
        APIResponse<List<CategoryDTO>> responseData = new APIResponse<>();
        responseData.setMessage("Find by name successful");
        responseData.setData(categoryService.findByNameCategory(name));
        responseData.setStatus(HttpStatus.OK.value());
        return new ResponseEntity<>(responseData, responseHeaders, HttpStatus.OK);
    }

    @GetMapping("categories/{categoryId}")
    public ResponseEntity<Object> findByIdCategory(@PathVariable("categoryId") Integer categoryId) {
        APIResponse<CategoryDTO> responseData = new APIResponse<>();
        responseData.setMessage("Find by Id successful");
        responseData.setData(categoryService.findById(categoryId));
        responseData.setStatus(HttpStatus.OK.value());

        return new ResponseEntity<>(responseData, responseHeaders, HttpStatus.OK);
    }

    @GetMapping("categories")
    public ResponseEntity<APIResponse<Page<CategoryDTO>>> getAllCategories(
            @RequestParam(defaultValue = "", required = false) String searchValue,
            @RequestParam(defaultValue = "0", required = false) Integer page,
            @RequestParam(defaultValue = "5", required = false) Integer size,
            @RequestParam(defaultValue = "categoryId", required = false) String sortBy) {
        Page<CategoryDTO> categories = categoryService.getAllCategory(searchValue, page, size, sortBy);
        APIResponse<Page<CategoryDTO>> responseData = new APIResponse<>();
        responseData.setStatus(HttpStatus.OK.value());
        responseData.setMessage("Fill all categories successful");
        responseData.setData(categories);
        return new ResponseEntity<>(responseData, HttpStatus.OK);
    }

    @PostMapping("categories")
    public ResponseEntity<Object> addCategory(@RequestBody CategoryDTO categoryDTO) {
        categoryService.addCategory(categoryDTO);
        APIResponse<CategoryDTO> responseData = new APIResponse<>();
        responseData.setStatus(HttpStatus.OK.value());
        responseData.setMessage("Add successful");
        responseData.setData(categoryDTO);
        return new ResponseEntity<>(responseData, responseHeaders, HttpStatus.OK);
    }

    @PostMapping("categories/import")
    public ResponseEntity<Object> importFile(@RequestBody List<CategoryDTO> categoryDTOList) {
        boolean importResult = categoryService.addCategoryImport(categoryDTOList);
        APIResponse<Boolean> response = new APIResponse<>();
        response.setData(importResult);
        response.setStatus(HttpStatus.OK.value());
        response.setMessage("Import success!");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @DeleteMapping("categories")
    public ResponseEntity<Object> deleteCategory(@RequestParam(name = "id") int categoryId) {
        categoryService.deleteCategory(categoryId);
        APIResponse<CategoryDTO> responseData = new APIResponse<>();
        responseData.setStatus(HttpStatus.OK.value());
        responseData.setMessage("Delete successful");
        return new ResponseEntity<>(responseData, responseHeaders, HttpStatus.OK);
    }

    @PutMapping("categories/{categoryId}")
    public ResponseEntity<Object> updateCategory(@RequestBody CategoryDTO categoryDTO,
                                                 @PathVariable("categoryId") int categoryId) {
        categoryService.updateCategory(categoryDTO, categoryId);
        APIResponse<CategoryDTO> responseData = new APIResponse<>();
        responseData.setStatus(HttpStatus.OK.value());
        responseData.setMessage("Update successful");
        return new ResponseEntity<>(responseData, responseHeaders, HttpStatus.OK);
    }

    //update
    @GetMapping("categories/categories")
    public ResponseEntity<APIResponse<List<CategoryDTO>>> findAllCategories() {
        APIResponse<List<CategoryDTO>> responseData = new APIResponse<>();
        responseData.setMessage("Find list category's name success!");
        responseData.setData(categoryService.getAllCategory());
        responseData.setStatus(HttpStatus.OK.value());

        return new ResponseEntity<>(responseData, null, HttpStatus.OK);
    }

    @GetMapping("categories/count")
    public ResponseEntity<APIResponse<Long>> getNumberOfUserIsUnblocked() {
        Long categoryNum = categoryService.countCategory();
        APIResponse<Long> responseData = new APIResponse<>();
        responseData.setStatus(HttpStatus.OK.value());
        responseData.setData(categoryNum);
        responseData.setMessage("get number of user unblocked successfull");
        return new ResponseEntity<>(responseData, HttpStatus.OK);
    }

    @GetMapping("categories/productName/{productName}")
    public ResponseEntity<APIResponse<List<CategoryDTO>>> findRoleByUsername(
            @PathVariable("productName") String productName) {
        APIResponse<List<CategoryDTO>> responseData = new APIResponse<>();
        List<CategoryDTO> roleDTOs = categoryService.getAllCateByProductName(productName);
        responseHeaders.set("MyResponseHeader", "acd");
        responseData.setMessage("Find by name successful");
        responseData.setData(roleDTOs);
        responseData.setStatus(HttpStatus.OK.value());
        return new ResponseEntity<>(responseData, responseHeaders, HttpStatus.OK);
    }
}
