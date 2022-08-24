package com.example.payminder.util

import com.example.payminder.database.entities.Customer

data class CustomerListFilter(
    val email:Int=ALL,
    val mobile:Int=ALL,
    val overdue:Int=ALL,
    val emailIntimation:Int=ALL,
    val smsIntimation:Int=ALL
) {
    companion object{
        const val ALL=0
        const val INCLUDE=1
        const val EXCLUDE=2
    }

    fun isFilterEnabled()=
        !(email==ALL && mobile==ALL && overdue==ALL && emailIntimation==ALL && smsIntimation==ALL)

    fun apply(listToFilter:List<Customer>):List<Customer>{

        if(listToFilter.isEmpty() || !isFilterEnabled())
            return listToFilter

        return listToFilter
            .filterByEmail()
            .filterByMobileNumber()
            .filterByOverdue()
            .filterByEmailIntimation()
            .filterBySmsIntimation()
    }

    private fun List<Customer>.filterByEmail():List<Customer>{

        return when(email){
            ALL ->{ this }
            INCLUDE->{ this.filter { it.hasEmailAddress() } }
            EXCLUDE->{this.filter { !it.hasEmailAddress() }}
            else->{error("Invalid filter option")}
        }
    }

    private fun List<Customer>.filterByMobileNumber():List<Customer>{

        return when(mobile){
            ALL ->{ this }
            INCLUDE->{ this.filter { it.hasMobileNumber() } }
            EXCLUDE->{this.filter { !it.hasMobileNumber() }}
            else->{error("Invalid filter option")}
        }
    }

    private fun List<Customer>.filterByOverdue():List<Customer>{

        return when(overdue){
            ALL ->{ this }
            INCLUDE->{ this.filter { it.overdueAmount > 0.0 } }
            EXCLUDE->{this.filter { it.overdueAmount == 0.0 }}
            else->{error("Invalid filter option")}
        }
    }

    private fun List<Customer>.filterByEmailIntimation():List<Customer>{

        return when(emailIntimation){
            ALL ->{ this }
            INCLUDE->{ this.filter { it.emailSent } }
            EXCLUDE->{this.filter { !it.emailSent }}
            else->{error("Invalid filter option")}
        }
    }

    private fun List<Customer>.filterBySmsIntimation():List<Customer>{

        return when(smsIntimation){
            ALL ->{ this }
            INCLUDE->{ this.filter { it.smsSent } }
            EXCLUDE->{this.filter { !it.smsSent }}
            else->{error("Invalid filter option")}
        }
    }

}