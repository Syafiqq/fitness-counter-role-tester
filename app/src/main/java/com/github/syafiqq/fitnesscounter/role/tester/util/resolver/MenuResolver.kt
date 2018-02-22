package com.github.syafiqq.fitnesscounter.role.tester.util.resolver

import com.github.syafiqq.fitnesscounter.role.tester.R

/**
 * This fitness-counter-role-tester project created by :
 * Name         : syafiq
 * Date / Time  : 23 February 2018, 6:12 AM.
 * Email        : id.muhammad.syafiq@gmail.com
 * Github       : Syafiqq
 */
object MenuResolver
{
    val dashboardDrawerResolver = mutableMapOf(
            // @formatter:off
            "Medical Check"  to R.id.nav_health,
            "Ulinois"        to R.id.nav_ulinois,
            "Vertical Jump"  to R.id.nav_vertical_jump,
            "Throwing Ball"  to R.id.nav_throwing_ball,
            "Push Up"        to R.id.nav_push_up,
            "Sit Up"         to R.id.nav_sit_up,
            "Run 1600 m"     to R.id.nav_run_1600m
            // @formatter:on
    )
}
