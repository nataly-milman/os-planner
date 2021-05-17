package net.planner.planet

import android.accounts.Account
import android.accounts.AccountManager
import android.content.Context

class User(context: Context) {
    // Gets user accounts from Android AccountManager
    //@TODO might need to use OAuth2

    // https://developer.android.com/training/id-auth/identify
    // https://developer.android.com/reference/android/accounts/AccountManager

    private val googleUsers: ArrayList<String>

    init {
        googleUsers = ArrayList<String>()

        // Find all the relevant users from AccountManager
        val accountManager: AccountManager = AccountManager.get(context)
        val accounts: Array<out Account> = accountManager.getAccountsByType("com.google")

    }

}