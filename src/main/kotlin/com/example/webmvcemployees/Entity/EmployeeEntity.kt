package com.example.webmvcemployees.Entity

import com.example.webmvcemployees.Boundary.EmployeeBoundary
import com.example.webmvcemployees.util.toBirthDate
import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import java.sql.Types
import java.time.LocalDate

@Entity
@Table(name = "employees")
class EmployeeEntity(
    @Id var email: String?,
    var name: String?,
    var password: String?,
    var birthDate: LocalDate?,

    @JdbcTypeCode(Types.ARRAY)
    @Column(name = "roles", columnDefinition = "text[]")
    var roles: List<String>?,

    @ManyToOne(fetch = FetchType.EAGER, optional = true)
    @JoinColumn(name = "manager_email")
    var manager: EmployeeEntity?) {

    constructor() : this(null, null, null, null, null, null)

    override fun toString(): String {
        return "EmployeeEntity(email=$email, name=$name, password=$password, birthDate=$birthDate, roles=$roles, manager=${manager?.email})"
    }

    fun toBoundary(): EmployeeBoundary {
        return EmployeeBoundary(this.email, this.name, "****", this.birthDate!!.toBirthDate(), this.roles)
    }
}

