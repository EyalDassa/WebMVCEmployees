package com.example.webmvcemployees.Boundary

class ManagerEmailWrapper (
    var email: String?){

    constructor() : this(null)

    override fun toString(): String {
        return "managerEmailWrapper(email=$email)"
    }
}