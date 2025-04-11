package com.example.webmvcemployees.Boundary

import com.example.webmvcemployees.Entity.EmployeeEntity
import com.example.webmvcemployees.util.toBirthDate
import com.example.webmvcemployees.util.toLocalDate

class EmployeeBoundary(
    var email: String?,
    var name: String?,
    var password: String?,
    var birthdate: BirthDate?,
    var roles: List<String>? ) {
    constructor() : this(null,null,null,null,null)
    constructor(entity: EmployeeEntity): this(entity.email,entity.name,"****",entity.birthDate!!.toBirthDate(),entity.roles)

    override fun toString(): String {
        return "EmployeeBoundary(email='$email', name='$name', password='$password', birthDate=$birthdate, roles=$roles)"
    }

    fun toEntity(): EmployeeEntity {
        val rv = EmployeeEntity()
        rv.email = email
        rv.name = name
        rv.password = password
        rv.birthDate = birthdate!!.toLocalDate()
        rv.roles = roles
        return rv
    }
}