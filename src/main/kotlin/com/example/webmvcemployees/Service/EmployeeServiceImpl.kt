package com.example.webmvcemployees.Service

import com.example.webmvcemployees.Boundary.BirthDate
import com.example.webmvcemployees.Boundary.EmployeeBoundary
import com.example.webmvcemployees.Entity.EmployeeCrud
import com.example.webmvcemployees.Exceptions.EmailExistsException
import com.example.webmvcemployees.Exceptions.InvalidInputException
import com.example.webmvcemployees.Exceptions.UnauthorizedException
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.Year
import org.apache.commons.validator.routines.EmailValidator
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class EmployeeServiceImpl(
    val employeeCrud: EmployeeCrud) : EmployeeService {

    @Transactional(readOnly = false)
    override fun create(newEmployee: EmployeeBoundary): EmployeeBoundary {
        val rv = EmployeeBoundary()
        if (isValidEmail(newEmployee.email)) {
            rv.email = newEmployee.email
            if(!this.employeeCrud.findById(rv.email!!).isEmpty)
                throw EmailExistsException("Email already exists")
        } else{
            throw InvalidInputException("Invalid email format")
        }
        rv.name = newEmployee.name
        if(isValidPassword(newEmployee.password)) {
            rv.password = newEmployee.password
        } else{
            throw InvalidInputException("Invalid password")
        }
        if(isValidDate(newEmployee.birthdate)) {
            val birthdate = BirthDate()
            birthdate.day = newEmployee.birthdate!!.day
            birthdate.month = newEmployee.birthdate!!.month
            birthdate.year = newEmployee.birthdate!!.year
            rv.birthdate = birthdate
        } else{
            throw InvalidInputException("Invalid date")
        }
        if(newEmployee.roles == null || newEmployee.roles!!.isEmpty()){
            throw InvalidInputException("Invalid role: cannot be empty")
        }
        if(newEmployee.roles!!.contains("")){
            throw InvalidInputException("Invalid role: role cannot be empty string")
        }
        rv.roles = newEmployee.roles
        return EmployeeBoundary(this.employeeCrud.save(rv.toEntity()))

    }

    @Transactional(readOnly = true)
    override fun getEmployeeByEmail(email: String, password: String): EmployeeBoundary {
        if (!isValidEmail(email)) {
            throw InvalidInputException("Invalid email format")
        }

        val optionalEmployee = this.employeeCrud.findById(email)

        if (optionalEmployee.isPresent) {
            val employee = optionalEmployee.get()
            if (employee.password == password) {
                return employee.toBoundary()
            } else {
                throw UnauthorizedException("Invalid email or password")
            }
        } else {
            throw UnauthorizedException("Invalid email or password")
        }
    }

    @Transactional(readOnly = true)
    override fun getEmployees(page: Int, size: Int): List<EmployeeBoundary> {
        return this.employeeCrud.findAll(PageRequest.of(page, size, Sort.Direction.ASC,"email"))
            .map { EmployeeBoundary(it) }
            .toList()
    }

    @Transactional(readOnly = true)
    override fun getByDomain(value: String, page: Int, size: Int): List<EmployeeBoundary> {
        return this.employeeCrud
            .findByEmailEndingWith("@$value", PageRequest.of(page, size,Sort.Direction.ASC,"email"))
            .map { EmployeeBoundary(it) }
            .toList()
    }

    @Transactional(readOnly = true)
    override fun getByRole(value: String, page: Int, size: Int): List<EmployeeBoundary> {
        return this.employeeCrud
            .findByRole(value, PageRequest.of(page, size,Sort.Direction.ASC,"email"))
            .map { EmployeeBoundary(it) }
            .toList()
    }

    @Transactional(readOnly = true)
    override fun getByAge(value: String, page: Int, size: Int): List<EmployeeBoundary> {
        val age = value.toIntOrNull()
            ?: throw InvalidInputException("Invalid age: $value")

        val today = LocalDate.now()
        val startDate = today.minusYears(age.toLong() + 1).plusDays(1) // start of age
        val endDate = today.minusYears(age.toLong()) // end of age

        return employeeCrud
            .findByBirthDateBetween(startDate, endDate, PageRequest.of(page, size,Sort.Direction.ASC,"email"))
            .map { EmployeeBoundary(it) }
            .toList()
    }

    @Transactional(readOnly = false)
    override fun clean() {
        this.employeeCrud.deleteAll()
    }


    fun isValidDate(birthdate: BirthDate?): Boolean {
        if (birthdate == null) return false

        val day = birthdate.day
        val month = birthdate.month
        val year = birthdate.year

        // Check basic format rules
        val isValidDay = day.length == 2 && day.toIntOrNull() in 1..31
        val isValidMonth = month.length == 2 && month.toIntOrNull() in 1..12
        val currentYear = Year.now().value
        val isValidYear = year.length == 4 && year.toIntOrNull() in 1900..currentYear

        if (!isValidDay || !isValidMonth || !isValidYear) {
            return false
        }

        // Check if full date is valid (e.g., 31/02/2023 should fail)
        val dateStr = "${year}-${month}-${day}"
        return try {
            LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            true
        } catch (e: DateTimeParseException) {
            false
        }
    }

    fun isValidPassword(password: String?): Boolean {
        if (password == null) return false
        val hasMinimumLength = password.length >= 3
        val hasNumber = password.any { it.isDigit() }
        val hasUppercase = password.any { it.isUpperCase() }

        return hasMinimumLength && hasNumber && hasUppercase
    }


    fun isValidEmail(email: String?): Boolean {
        return EmailValidator.getInstance().isValid(email)
    }

}