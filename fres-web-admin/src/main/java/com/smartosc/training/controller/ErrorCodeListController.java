//huupd
package com.smartosc.training.controller;


import com.smartosc.training.dto.AppCodeDTO;
import com.smartosc.training.entity.APIResponse;
import com.smartosc.training.service.impl.AppCodeServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author Namtt hieu
 */
@Controller
@RequestMapping(value = "/errorcodelist")
public class ErrorCodeListController {
    @Autowired
    private AppCodeServiceImpl appCodeService;


    /**
     * get error code list page
     *
     * @param model
     * @param pageNo
     * @param pageSize
     * @return view error-code-list
     * @author Namtt
     */
    @GetMapping
    public String errorCodeList(Model model, @RequestParam(value = "pageNo", defaultValue = "0", required = false) Integer pageNo,
                                @RequestParam(value = "pageSize", defaultValue = "5", required = false) Integer pageSize) {
        Pageable pageable = PageRequest.of(pageNo, pageSize);
        APIResponse<List<AppCodeDTO>> listAPIResponse = appCodeService.getAllAppCode(pageable);
        model.addAttribute("list", listAPIResponse.getData());
        if (listAPIResponse.getPageMetadata() != null) {
            model.addAttribute("pages", listAPIResponse.getPageMetadata());
        }
        return "error-code-list";
    }

    /*
     * @return data json list appcode find by code or description*/
    @GetMapping("/{searchValue}")
    @ResponseBody
    public List<AppCodeDTO> searchAppCode(@PathVariable("searchValue") String searchValue) {
        return appCodeService.searchAppCode(searchValue);
    }

    /*
     * @return data json find appcode by id*/
    @GetMapping("/find/{id}")
    @ResponseBody
    public AppCodeDTO findAppCodeById(@PathVariable("id") int id) {
        return appCodeService.findAppCodeById(id);
    }

    /*
     * @return data json after update*/
    @PutMapping
    @ResponseBody
    public AppCodeDTO updateAppCode(@RequestBody AppCodeDTO appCodeDTO) {
        return appCodeService.updateAppCode(appCodeDTO);
    }

    /*
     * @return data json after add*/
    @PostMapping
    @ResponseBody
    public AppCodeDTO addNewAppCode(@RequestBody AppCodeDTO appCodeDTO) {
        return appCodeService.addNewAppCode(appCodeDTO);
    }
}
