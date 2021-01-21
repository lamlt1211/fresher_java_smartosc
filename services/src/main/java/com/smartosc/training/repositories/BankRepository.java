package com.smartosc.training.repositories;

import com.smartosc.training.entity.Bank;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import org.springframework.data.repository.query.Param;
import java.util.Optional;


import java.util.List;

/**
 * fres-parent
 *
 * @author andy98
 * @created_at 20/04/2020 - 18:27
 * @created_by andy98
 * @since 20/04/2020
 */

@Repository
public interface BankRepository extends JpaRepository<Bank, Integer> {

    // find all transit banks or search them by searchKey
    @Query("SELECT b " +
            "FROM Bank b inner join BankDetail bd on b.bankId = bd.bankId " +
            "where bd.type like %:bankType% " +
            "AND (b.legalName like %:searchKey% " +
            "OR b.code like %:searchKey% " +
            "OR b.prefixCard like %:searchKey%)")
    Page<Bank> findAllIntermediaryBank(@Param("searchKey") String searchKey, @Param("bankType") String bankType , Pageable pageable);

    // find transit bank by id
    @Query("SELECT b FROM Bank b inner join BankDetail bd on b.bankId = bd.bankId " +
            "where bd.type like %:bankType% " +
            "AND bd.id = :bankDetailId")
    Optional<Bank> findIntermediaryBankById(@Param("bankDetailId") int bankDetailId, @Param("bankType") String bankType);


    @Query("SELECT b FROM Bank b inner join BankDetail bd on b.bankId = bd.bankId " +
            "where bd.type like %:bankType% " +
            "AND (b.legalName like %:searchKey% " +
            "OR b.code like %:searchKey% " +
            "OR b.prefixCard like %:searchKey%)")
    Page<Bank> finAllDirectBank(@Param("searchKey") String searchKey,@Param("bankType") String bankType ,Pageable pageable);

    /**
     * @author thanhttt
     *  @created_at 21/04/2020 - 10:50 AM
     */
    @Query(value ="from Bank b where b.status = 1")
    List<Bank> getAllBanksByStatus();
}
