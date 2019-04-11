package com.uncc.mileagetracker.Models

class User {

    var firstName: String? = null
    var lastName: String? = null
    var description: String? = null
    var email: String? = null
    var photo: String? = null
    var gender: String? = null
    var id: String? = null
    var isAdmin:Boolean? =false
    var rides: ArrayList<Ride>? = null

    constructor(){}

    constructor(_firstName: String?,
                _lastName: String?,
                _description: String?,
                _email: String?,
                _photo: String?,
                _gender: String?,
                _id: String?,
                _isAdmin:Boolean) {

        this.firstName = _firstName
        this.lastName = _lastName
        this.description = _description
        this.email = _email
        this.photo = _photo
        this.gender= _gender
        this.id = _id
        this.isAdmin= _isAdmin

        this.rides = ArrayList<Ride>()
    }

}