package com.smartosc.training.service;

import com.smartosc.training.dto.AppCodeDTO;
import com.smartosc.training.dto.PageMetaData;
import com.smartosc.training.entity.APIResponse;
import com.smartosc.training.entity.AppCode;
import com.smartosc.training.repositories.AppCodeRepository;

import org.apache.xmlbeans.impl.piccolo.util.DuplicateKeyException;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Service
@Transactional
public class AppCodeService {
    @Autowired
    private AppCodeRepository appCodeRepository;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private ModelMapper modelMapper;

    /**
     * fres-parent
     *
     * @author Namtt
     * @created_at 20/04/2020 - 5:58 PM
     * @created_by Namtt
     * @since 20/04/2020
     */

    //list all appcode with pageable
    public Page<AppCodeDTO> getAllAppCode(Pageable pageable) {
        try {
            Page<AppCode> appCodes = appCodeRepository.findAll(pageable);
            logger.info("Get All AppCode ...", appCodes);
            Page<AppCodeDTO> dtos = null;
            logger.info("Get App Success ...");
            List<AppCodeDTO> appCodeDTOS = new ArrayList<>();
            for (AppCode appCode : appCodes) {
                AppCodeDTO appCodeDTO = modelMapper.map(appCode, AppCodeDTO.class);
                appCodeDTOS.add(appCodeDTO);
            }
            logger.info("Map Success AppCode to AppCodeDTO", appCodeDTOS);
            dtos = new PageImpl<>(appCodeDTOS, pageable, appCodes.getTotalElements());
            logger.info("Get Success All AppCode With Paging", dtos);
            return dtos;
        } catch (Exception e) {
            logger.error(e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    /*
     * @author: Namtt
     * @return no return
     * @function add new appcode
     * */

    //add new app code
    public void addAppCode(AppCodeDTO appCodeDTO) {
        try {
            logger.info("check duplicate code");
            if (appCodeRepository.findByCode(appCodeDTO.getCode()) == null) {
                logger.info("Get Data ...", appCodeDTO);
                AppCode appCode = modelMapper.map(appCodeDTO, AppCode.class);
                appCode.setCreatedBy(SecurityContextHolder.getContext().getAuthentication().getName());
                logger.info("Add data to database....");
                appCodeRepository.save(appCode);
                logger.info("save data success");
            } else {
                throw new DuplicateKeyException("Khong Add Duoc");
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }



    /*
     * author: Hieu
     * function: findByCodeOrDescription; save; delete; findAppCodeById
     * */

    //search appcode
    public List<AppCodeDTO> findByCodeOrDescription(String searchValue) {
        try {
            List<AppCode> appCodes = appCodeRepository.findByCodeOrDescription(searchValue, null);
            logger.info("Search AppCode by code or description...", appCodes);
            List<AppCodeDTO> appCodeDTOS = new ArrayList<>();
            for (AppCode appCode : appCodes) {
                appCodeDTOS.add(modelMapper.map(appCode, AppCodeDTO.class));
            }
            return appCodeDTOS;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    //save or update appcode
    public AppCodeDTO saveAppCode(AppCodeDTO appCodeDTO) {
        try {
            if (appCodeDTO.getAppCodeId() != null && appCodeDTO.getAppCodeId() > 0) {
                Optional<AppCode> appCode = appCodeRepository.findById(appCodeDTO.getAppCodeId());
                logger.info("Update AppCode, get AppCode by id", appCode);
                logger.info("Update AppCode, AppCode new", appCodeDTO);
                if (appCode.isPresent()) {
                    AppCode appCodeNew = appCode.get();
                    appCodeNew.setDescription(appCodeDTO.getDescription());
                    appCodeNew.setStatus(appCodeDTO.getStatus());
                    appCodeNew.setModifiedBy(SecurityContextHolder.getContext().getAuthentication().getName());
                    appCodeNew = appCodeRepository.save(appCodeNew);
                    return modelMapper.map(appCodeNew, AppCodeDTO.class);
                }
                return null;
            } else {
                AppCode appCode1 = modelMapper.map(appCodeDTO, AppCode.class);
                return modelMapper.map(appCodeRepository.save(appCode1), AppCodeDTO.class);
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    //get appcode by id
    public AppCodeDTO findAppCodeById(int id) {
        try {
            Optional<AppCode> appCode = appCodeRepository.findById(id);
            logger.info("Find AppCode by id ...", appCode);
            if (appCode.isPresent()) {
                return modelMapper.map(appCode.get(), AppCodeDTO.class);
            }
            return null;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    //set response data for view
    public APIResponse<List<AppCodeDTO>> getResponseData(Page<AppCodeDTO> dtos, int pageNo, int pageSize) {
        APIResponse<List<AppCodeDTO>> responseData = new APIResponse<>();
        responseData.setStatus(HttpStatus.OK.value());
        responseData.setMessage("Fill all app code successful");
        responseData.setData(dtos.getContent());


        PageMetaData pageMetaData = new PageMetaData();
        pageMetaData.setNumber(dtos.getNumber());
        pageMetaData.setTotalElements(dtos.getTotalElements());
        pageMetaData.setTotalPages(dtos.getTotalPages());
        pageMetaData.setPage(pageNo);
        pageMetaData.setSize(pageSize);
        pageMetaData.setFirst(dtos.isFirst());
        pageMetaData.setLast(dtos.isLast());

        responseData.setPageMetadata(pageMetaData);
        return responseData;
    }

}
