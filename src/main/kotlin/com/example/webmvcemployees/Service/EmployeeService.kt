package com.example.webmvcemployees.Service

import com.example.webmvcemployees.Boundary.EmployeeBoundary

interface EmployeeService {
    fun create(newEmployee : EmployeeBoundary): EmployeeBoundary
    fun getEmployeeByEmail(email: String,password:String) : EmployeeBoundary
    fun getEmployees(page: Int, size: Int): List<EmployeeBoundary>
    fun getByDomain(value: String, page: Int, size: Int): List<EmployeeBoundary>
    fun getByRole(value: String, page: Int, size: Int): List<EmployeeBoundary>
    fun getByAge(value: String, page: Int, size: Int): List<EmployeeBoundary>
    fun clean()

    fun bind(employeeEmail: String, managerEmail: String)
    fun getEmployeesManager(employeeEmail: String): EmployeeBoundary
    fun getSubordinates(managerEmail: String, page: Int, size: Int): List<EmployeeBoundary>
    fun unbind(employeeEmail: String)
}