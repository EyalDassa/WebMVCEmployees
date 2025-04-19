package com.example.webmvcemployees.Service

import com.example.webmvcemployees.Boundary.BirthDate
import com.example.webmvcemployees.Boundary.EmployeeBoundary
import com.example.webmvcemployees.Entity.EmployeeCrud
import com.example.webmvcemployees.Exceptions.EmailExistsException
import com.example.webmvcemployees.Exceptions.InvalidInputException
import com.example.webmvcemployees.Exceptions.NotFoundException
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
        val email = newEmployee.email ?: throw InvalidInputException("Email cannot be null")

        if (!isValidEmail(email)) {
            throw InvalidInputException("Invalid email format")
        }

        if (employeeCrud.existsById(email)) {
            throw EmailExistsException("Email already exists")
        }

        if (!isValidPassword(newEmployee.password)) {
            throw InvalidInputException("Invalid password")
        }

        if (!isValidDate(newEmployee.birthdate)) {
            throw InvalidInputException("Invalid date")
        }

        if (newEmployee.roles.isNullOrEmpty()) {
            throw InvalidInputException("Invalid role: cannot be null or empty")
        }

        if (newEmployee.roles!!.any { it.isBlank() }) {
            throw InvalidInputException("Invalid role: role cannot be empty string")
        }

        return EmployeeBoundary(this.employeeCrud.save(newEmployee.toEntity()))
    }

    @Transactional(readOnly = true)
    override fun getEmployeeByEmail(email: String, password: String): EmployeeBoundary {
        if (!isValidEmail(email)) {
            throw InvalidInputException("Invalid email format")
        }

        val employee = employeeCrud.findById(email)
            .orElseThrow { UnauthorizedException("Invalid email or password") }

        if (employee.password != password) {
            throw UnauthorizedException("Invalid email or password")
        }

        return employee.toBoundary()
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

    @Transactional(readOnly = false)
    override fun bind(employeeEmail: String, managerEmail: String) {
        if(!isValidEmail(employeeEmail)) {
            throw InvalidInputException("Invalid employee email")
        }
        if(!isValidEmail(managerEmail)) {
            throw InvalidInputException("Invalid manager email")
        }

        val employee = employeeCrud.findById(employeeEmail)
            .orElseThrow { NotFoundException("Employee not found: $employeeEmail") }

        val manager = employeeCrud.findById(managerEmail)
            .orElseThrow { NotFoundException("Manager not found: $managerEmail") }

        // Prevent setting themselves as their own manager
        if (employee.email == manager.email) {
            throw InvalidInputException("An employee cannot be their own manager")
        }

        employee.manager = manager
        employeeCrud.save(employee)
    }

    @Transactional(readOnly = true)
    override fun getEmployeesManager(employeeEmail: String): EmployeeBoundary {
        if(!isValidEmail(employeeEmail)) {
            throw InvalidInputException("Invalid employee email")
        }
        val employee = employeeCrud.findById(employeeEmail)
            .orElseThrow { NotFoundException("Employee not found: $employeeEmail") }

        val manager = employee.manager ?: throw NotFoundException("Employee $employeeEmail has no manager")

        return manager.toBoundary()
    }

    @Transactional(readOnly = true)
    override fun getSubordinates(managerEmail: String, page: Int, size: Int): List<EmployeeBoundary> {
        if (!isValidEmail(managerEmail)) {
            throw InvalidInputException("Invalid email format")
        }

//        if (!employeeCrud.existsById(managerEmail)) {
//            throw NotFoundException("Manager not found: $managerEmail")
//        }

        return this.employeeCrud.findAllByManagerEmail(managerEmail,PageRequest.of(page, size, Sort.Direction.ASC,"email"))
            .map { EmployeeBoundary(it) }
            .toList()
    }

    @Transactional(readOnly = false)
    override fun unbind(employeeEmail: String) {
        if (!isValidEmail(employeeEmail)) {
            throw InvalidInputException("Invalid email format")
        }

        val employee = employeeCrud.findById(employeeEmail)
            .orElseThrow { NotFoundException("Employee not found: $employeeEmail") }

        employee.manager = null
        employeeCrud.save(employee)
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