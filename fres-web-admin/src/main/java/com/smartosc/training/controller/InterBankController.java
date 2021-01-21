package com.smartosc.training.controller;

import com.smartosc.training.dto.IntermediaryBankDTO;
import com.smartosc.training.dto.ReceivedDirectlyBankDTO;
import com.smartosc.training.entity.APIResponse;
import com.smartosc.training.entity.AppUserDetails;
import com.smartosc.training.request.UpdateInterBankReq;
import com.smartosc.training.service.RestService;
import com.smartosc.training.utils.JWTUtil;
import com.smartosc.training.utils.RestPageImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Controller
@RequestMapping(value = "/interBanks")
public class InterBankController {
    @Value("${api.url}")
    private String url;
    @Value("${prefix.interbanks}")
    private String urlPrefix;
    @Value("${prefix.banks}")
    private String prefixURL;

    @Autowired
    private RestService restService;

    @Autowired
    private JWTUtil jwtTokenUtil;

    @GetMapping
    public String getInterBankPage(Model model,
                                   @RequestParam(defaultValue = "", required = false) String searchValue,
                                   @RequestParam(defaultValue = "0", required = false) Integer pageNo,
                                   @RequestParam(defaultValue = "10", required = false) Integer pageSize,
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
        //
        RestPageImpl<IntermediaryBankDTO> result = null;
        Map<String, Object> values = new HashMap<>();
        values.put("searchValue", searchValue);
        values.put("pageNo", pageNo);
        values.put("pageSize", pageSize);
        values.put("sortBy", sortBy);
        APIResponse<RestPageImpl<IntermediaryBankDTO>> responseData = restService.execute(
                new StringBuilder(url).append(urlPrefix)
                        .append("?searchValue={searchValue}&page={pageNo}&size={pageSize}&sortBy={sortBy}")
                        .toString(),
                HttpMethod.GET,
                null,
                null,
                new ParameterizedTypeReference<APIResponse<RestPageImpl<IntermediaryBankDTO>>>() {
                },
                values
        );
        if (responseData.getStatus() == HttpStatus.OK.value()) {
            result = responseData.getData();
            log.debug("result: %d ");
        }
        model.addAttribute("data", result);
        model.addAttribute("dataPageImpl", result);
        model.addAttribute("currentPage", pageNo);
        model.addAttribute("searchValue", searchValue);
        return "inter-bank";
    }

    // Save data for create function
    @PostMapping("/save")
    public String saveInterBank(@ModelAttribute("bankRequest") IntermediaryBankDTO bankRequest) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        AppUserDetails userDetails = (AppUserDetails) auth.getPrincipal();
        String createdBy = userDetails.getUsername();
        String authToken = jwtTokenUtil.getJwtTokenFromSecurityContext();
        HttpHeaders header = new HttpHeaders();
        header.setBearerAuth(authToken);
        header.setContentType(MediaType.APPLICATION_JSON);
        bankRequest.setCreatedBy(createdBy);
        try {
            restService.execute(new StringBuffer(url).append(urlPrefix).toString(), HttpMethod.POST, header, bankRequest,
                    new ParameterizedTypeReference<APIResponse<IntermediaryBankDTO>>() {
                    });
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/interBanks?adderror";
        }
        return "redirect:/interBanks?addsuccess";
    }

    // Show bank detail by ID
    @GetMapping("/{id}")
    @ResponseBody
    public IntermediaryBankDTO findById(@PathVariable("id") Integer id) {
        String authToken = jwtTokenUtil.getJwtTokenFromSecurityContext();
        HttpHeaders header = new HttpHeaders();
        header.setBearerAuth(authToken);
        IntermediaryBankDTO newInterBank = restService.execute(
                new StringBuffer(url).append(urlPrefix).append("/").toString() + id, HttpMethod.GET, header,
                null, new ParameterizedTypeReference<APIResponse<IntermediaryBankDTO>>() {
                }).getData();
        return newInterBank;
    }

    // Convert status in Bank Detail (for Received directly bank) from 1-Active or 0-Inactive, and conversely
    @PutMapping(value = "/update")
    @ResponseBody
    public UpdateInterBankReq updateDirectlyBank(@RequestBody UpdateInterBankReq updateRequest) {
        String authToken = jwtTokenUtil.getJwtTokenFromSecurityContext();
        HttpHeaders header = new HttpHeaders();
        header.setBearerAuth(authToken);
        header.setContentType(MediaType.APPLICATION_JSON);
        Integer id = updateRequest.getId();
        UpdateInterBankReq bankDetail = restService.execute(
                new StringBuffer(url).append(urlPrefix).append("/").toString() + id, HttpMethod.PUT, header, updateRequest,
                new ParameterizedTypeReference<APIResponse<UpdateInterBankReq>>() {
                }).getData();
        return bankDetail;

    }

}


