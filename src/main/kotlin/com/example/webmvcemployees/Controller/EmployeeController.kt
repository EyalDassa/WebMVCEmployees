package com.example.webmvcemployees.Controller

import com.example.webmvcemployees.Boundary.EmployeeBoundary
import com.example.webmvcemployees.Boundary.ManagerEmailWrapper
import com.example.webmvcemployees.Exceptions.InvalidInputException
import com.example.webmvcemployees.Service.EmployeeService
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/employees")
class EmployeeController(
    val employeeService: EmployeeService
) {

    @PostMapping(
        consumes = [MediaType.APPLICATION_JSON_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE])
    fun create(@RequestBody newEmployee : EmployeeBoundary) : EmployeeBoundary {
        return this.employeeService.create(newEmployee)
    }

    @GetMapping(
        produces = [MediaType.APPLICATION_JSON_VALUE],
        path = ["/{employeeEmail}"])
    fun findByEmail(@PathVariable("employeeEmail") employeeEmail: String,
                    @RequestParam(name = "password", required = true) password:String): EmployeeBoundary {
        return this.employeeService.getEmployeeByEmail(employeeEmail,password)
    }

    @GetMapping(
        produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getEmployees(@RequestParam("criteria", required = false) criteria: String?,
                               @RequestParam("value", required = false) value: String?,
                               @RequestParam("page", required = true, defaultValue = "0") page: Int,
                               @RequestParam("size", required = true, defaultValue = "10") size: Int): List<EmployeeBoundary> {
        if (criteria != null && value.isNullOrBlank()) {
            throw InvalidInputException("Missing or empty 'value' parameter for criteria: $criteria")
        }
        if (size <= 0 || page < 0){
            throw InvalidInputException("Invalid pagination parameters: page must be >= 0 and size > 0")
        }
        return when (criteria) {
            null -> this.employeeService.getEmployees(page,size)
            "byEmailDomain" -> this.employeeService.getByDomain(value!!,page,size)
            "byRole" -> this.employeeService.getByRole(value!!,page,size)
            "byAge" -> this.employeeService.getByAge(value!!,page,size)
            else -> throw InvalidInputException("Invalid criteria: $criteria")
        }
    }

    @DeleteMapping()
    fun clean(){
        this.employeeService.clean()
    }

    @PutMapping(
        path = ["/{employeeEmail}/manager"],
        consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun bind(@PathVariable("employeeEmail") employeeEmail: String,
                              @RequestBody managerEmailWrapper: ManagerEmailWrapper) {
        return employeeService.bind(employeeEmail,managerEmailWrapper.email!!)
    }

    @GetMapping(
        path = ["/{employeeEmail}/manager"],
        produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getEmployeesManager(@PathVariable("employeeEmail") employeeEmail: String): EmployeeBoundary {
        return this.employeeService.getEmployeesManager(employeeEmail)
    }

    @GetMapping(
        path = ["/{managerEmail}/subordinates"],
        produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getSubordinates(@PathVariable("managerEmail") managerEmail: String,
                                @RequestParam("page", required = true, defaultValue = "0") page: Int,
                                @RequestParam("size", required = true, defaultValue = "10") size: Int): List<EmployeeBoundary>{
        if (size <= 0 || page < 0){
            throw InvalidInputException("Invalid pagination parameters: page must be >= 0 and size > 0")
        }
        return this.employeeService.getSubordinates(managerEmail,page,size)
    }

    @DeleteMapping(
        path = ["/{employeeEmail}/manager"],
        produces = [MediaType.APPLICATION_JSON_VALUE])
    fun unbind(@PathVariable("employeeEmail") employeeEmail: String){
        return this.employeeService.unbind(employeeEmail)
    }


}