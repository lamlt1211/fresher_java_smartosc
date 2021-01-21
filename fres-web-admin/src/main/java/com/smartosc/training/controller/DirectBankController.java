package com.smartosc.training.controller;
import com.smartosc.training.dto.DirectBankDTO;
import com.smartosc.training.entity.APIResponse;
import com.smartosc.training.service.RestService;
import com.smartosc.training.utils.RestPageImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.smartosc.training.dto.ReceivedDirectlyBankDTO;
import com.smartosc.training.entity.AppUserDetails;
import com.smartosc.training.utils.JWTUtil;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * fres-parent
 *
 * @author thanhttt
 * @created_at 21/04/2020 - 09:30 AM
 */
@Controller
@RequestMapping("/directBanks")
public class DirectBankController {

    @Value("${api.url}")
    private String url;

    @Value("${prefix.banks}")
    private String prefixURL;

    @Value("${prefix.directbanks}")
    private String urlDirectBank;

    @Autowired
    private RestService restService;

    @Autowired
    private JWTUtil jwtTokenUtil;
    //Get all directBank
    @GetMapping
    public String getDirectBankPage(Model model,
                                   @RequestParam(defaultValue = "", required = false) String searchValue,
                                   @RequestParam(defaultValue = "0", required = false) Integer pageNo,
                                   @RequestParam(defaultValue = "5", required = false) Integer pageSize,
                                   @RequestParam(defaultValue = "bankId", required = false) String sortBy) {

        //List bank status=1 thanhttt
        model.addAttribute("bankRequest", new ReceivedDirectlyBankDTO());
        String authToken = jwtTokenUtil.getJwtTokenFromSecurityContext();
        HttpHeaders header = new HttpHeaders();
        header.setBearerAuth(authToken);
        List<ReceivedDirectlyBankDTO> bankDetailDTOList = restService.execute(
                new StringBuffer(url).append(prefixURL).append("/byStatus").toString(), HttpMethod.GET, header, null,
                new ParameterizedTypeReference<APIResponse<List<ReceivedDirectlyBankDTO>>>() {
                }).getData();
        model.addAttribute("listBank", bankDetailDTOList);

        //GetAll direct Bank huupd
        RestPageImpl<DirectBankDTO> result = null;
        Map<String, Object> values = new HashMap<>();
        values.put("searchValue", searchValue);
        values.put("pageNo", pageNo);
        values.put("pageSize", pageSize);
        values.put("sortBy", sortBy);
        APIResponse<RestPageImpl<DirectBankDTO>> responseData = restService.execute(
                new StringBuilder(url).append(urlDirectBank)
                        .append("?searchValue={searchValue}&page={pageNo}&size={pageSize}&sortBy={sortBy}")
                        .toString(),
                HttpMethod.GET,
                null,
                null,
                new ParameterizedTypeReference<APIResponse<RestPageImpl<DirectBankDTO>>>() {
                },
                values
        );
        if (responseData.getStatus() == HttpStatus.OK.value()) {
            result = responseData.getData();
        }
        model.addAttribute("data", result);
        model.addAttribute("dataPageImpl", result);
        model.addAttribute("currentPage", pageNo);
        model.addAttribute("searchValue", searchValue);

        return "direct-bank";
    }

    // Save data for create function
    @PostMapping("/save")
    public String saveDirectlyBank(@ModelAttribute("bankRequest") ReceivedDirectlyBankDTO bankRequest) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        AppUserDetails userDetails = (AppUserDetails) auth.getPrincipal();
        String createdBy = userDetails.getUsername();
        String authToken = jwtTokenUtil.getJwtTokenFromSecurityContext();
        HttpHeaders header = new HttpHeaders();
        header.setBearerAuth(authToken);
        header.setContentType(MediaType.APPLICATION_JSON);
        bankRequest.setCreatedBy(createdBy);
        try {
            restService.execute(new StringBuffer(url).append(prefixURL).toString(), HttpMethod.POST, header, bankRequest,
                    new ParameterizedTypeReference<APIResponse<ReceivedDirectlyBankDTO>>() {
                    });
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/directBanks?error";
        }
        return "redirect:/directBanks?addSuccessful";
    }

    // Show bank detail by ID
    @GetMapping("/{id}")
    @ResponseBody
    public ReceivedDirectlyBankDTO findById(@PathVariable("id") Integer id) {
        String authToken = jwtTokenUtil.getJwtTokenFromSecurityContext();
        HttpHeaders header = new HttpHeaders();
        header.setBearerAuth(authToken);
        ReceivedDirectlyBankDTO bankDetail = restService.execute(
                new StringBuffer(url).append(prefixURL).append("/").toString() + id, HttpMethod.GET, header,
                null, new ParameterizedTypeReference<APIResponse<ReceivedDirectlyBankDTO>>() {
                }).getData();
        return bankDetail;
    }

    // Convert status in Bank Detail (for Received directly bank) from 1-Active or 0-Inactive, and conversely
    @PutMapping(value = "/update")
    @ResponseBody
    public ReceivedDirectlyBankDTO updateDirectlyBank(@RequestBody ReceivedDirectlyBankDTO bankRequest) {
        String modifiedBy = SecurityContextHolder.getContext().getAuthentication().getName();
        String authToken = jwtTokenUtil.getJwtTokenFromSecurityContext();
        HttpHeaders header = new HttpHeaders();
        header.setBearerAuth(authToken);
        header.setContentType(MediaType.APPLICATION_JSON);
        bankRequest.setModifiedBy(modifiedBy);
        Integer id = bankRequest.getId();
        ReceivedDirectlyBankDTO bankDetail = restService.execute(
                new StringBuffer(url).append(prefixURL).append("/").toString() + id, HttpMethod.PUT, header, bankRequest,
                new ParameterizedTypeReference<APIResponse<ReceivedDirectlyBankDTO>>() {
                }).getData();
        return bankDetail;

    }

}
