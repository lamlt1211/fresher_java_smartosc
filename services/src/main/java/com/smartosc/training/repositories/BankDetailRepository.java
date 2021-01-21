package com.smartosc.training.repositories;

import com.smartosc.training.entity.BankDetail;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * @author anhdt
 * @project fres-parent
 * @created_at 20/04/2020 - 6:11 PM
 * @created_by anhdt
 * @since 20/04/2020
 */

/**
 * @author duongnch
 * @since 20/04/2020
 * Func findAllBankDirectConfiguration
 * Get Info Direct Bank Configuration
 */
@Repository
public interface BankDetailRepository extends JpaRepository<BankDetail, Integer> {

    /**
     * @param searchKey
     * @return
     * @author anhdt2
     */
    @Query("select b.code, b.legalName, bd.status, bd.modifiedBy, bd.modifiedDatetime from Bank b join BankDetail bd on b.bankId = bd.bankId " +
            "where bd.type = 'TRANSIT' " +
            "and (b.code like %:searchKey% or b.legalName like %:searchKey%)")
    List<Object[]> findInterBankConfigByCodeOrByLegalName(@Param("searchKey") String searchKey);


    @Query(value = "SELECT b.code, b.legal_name, bd.status, bd.modified_by, bd.modified_datetime FROM bank b JOIN bank_detail bd ON b.bank_id = bd.bank_id WHERE bd.type = 'DIRECT' AND (b.legal_name like %:searchKey% OR b.code like %:searchKey% OR b.prefixCard like %:searchKey%)", nativeQuery = true)
    List<Object[]> findAllBankDirectConfiguration(@Param("searchKey") String searchKey);

    Optional<BankDetail> findByBankIdAndType(int id, String type);
    /**
     * @param id
     * @param type
     * @return
     * @author anhdt2
     */
    Optional<BankDetail> findByIdAndType(int id, String type);

//    List<BankDetail> findAllByType(String types);
//
//
//
//    Optional<BankDetail> findByBankId(int bankId);

    /**
     * Get all intermediary banks
     *
     * @return
     * @author anhdt2
     */
    @Query("select bd.id ,b.code, b.legalName, bd.status, bd.modifiedBy, bd.modifiedDatetime from Bank b join BankDetail bd on b.bankId = bd.bankId " +
            "where bd.type = 'TRANSIT' ")
    List<Object[]> findAllInterBank();
}
