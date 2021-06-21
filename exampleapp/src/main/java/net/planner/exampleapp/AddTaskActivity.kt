package net.planner.exampleapp

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import net.planner.planet.PlannerManager

class AddTaskActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "AddTaskActivity"

        fun createIntent(context: Context): Intent {
            return Intent(context, AddTaskActivity::class.java)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_task)

        // @TODO need to instantiate savedInstanceState with WidgetProvider information like which calendars to use

        // Initialise our PlannerManager
//        val plannerManager = PlannerManager()

    }
}