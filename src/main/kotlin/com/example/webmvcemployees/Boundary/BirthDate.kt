package com.example.webmvcemployees.Boundary

class BirthDate (
    var day: String,
    var month: String,
    var year: String
){
    constructor() : this("", "", "")

    override fun toString(): String {
        return "$day-$month-$year"
    }

}
