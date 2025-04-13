package com.example.webmvcemployees.Entity

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDate

interface EmployeeCrud : JpaRepository<EmployeeEntity, String> {
    fun findByEmailEndingWith(domain: String, pageable: Pageable): Page<EmployeeEntity>

    @Query(value = "SELECT * FROM Employees WHERE :role = ANY(roles)", nativeQuery = true)
    fun findByRole(@Param("role") role: String, pageable: Pageable): Page<EmployeeEntity>

    @Query(
        """
        SELECT e FROM EmployeeEntity e 
        WHERE e.birthDate BETWEEN :startDate AND :endDate
        """)
    fun findByBirthDateBetween(
        @Param("startDate") startDate: LocalDate,
        @Param("endDate") endDate: LocalDate,
        pageable: Pageable
    ): Page<EmployeeEntity>

    fun findAllByManagerEmail(managerEmail: String, pageable: Pageable): Page<EmployeeEntity>

}