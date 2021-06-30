package net.planner.exampleapp

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import net.planner.exampleapp.databinding.ActivityAddTaskBinding
import net.planner.planet.PlannerMediator
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class AddTaskActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "AddTaskActivity"

        private const val DAYS_SHIFTED_ID = "days_shifted"

        val dateFormatter = SimpleDateFormat("dd/MM/yyyy")
        val timeFormatter = SimpleDateFormat("HH:mm")

        private val ONE_DAY_MILLIS = TimeUnit.DAYS.toMillis(1)

        fun createIntent(context: Context, widgetId: Int, daysShifted:  Int): Intent {
            val intent = Intent(context, AddTaskActivity::class.java)
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
            intent.putExtra(DAYS_SHIFTED_ID, daysShifted)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            return intent
        }
    }

    private lateinit var mBinding: ActivityAddTaskBinding
    private lateinit var mediator: PlannerMediator

    private var widgetId: Int = 0
    private var numDaysShifted: Int = 0

    private val titleInput: String
        get() = mBinding.edtTitle.text.toString().trim { it <= ' '}

    private val deadlineDate: String
        get() = mBinding.deadlineDate.text.toString().trim { it <= ' '}

    private val deadlineTime: String
        get() = mBinding.deadlineTime.text.toString().trim { it <= ' '}

    private val duration: String
        get() = mBinding.estimatedDurationTime.text.toString().trim { it <= ' '}



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_add_task)
        mBinding.eventHandler = this

        supportActionBar?.title = ""
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Getting widget settings sent with Intent
        widgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,0)
        numDaysShifted = intent.getIntExtra(DAYS_SHIFTED_ID,0)

        // Updating view with initial values
        val currentCalendarTimeMillis = getCurrentCalendarWidgetTime()
        mBinding.deadlineDate.setText(dateFormatter.format(currentCalendarTimeMillis))
        mBinding.deadlineTime.setText(timeFormatter.format(currentCalendarTimeMillis))
        mBinding.estimatedDurationTime.setText("04:00")

        // Open date picker for date
        mBinding.deadlineDate.setOnClickListener {
            val currentTime = Calendar.getInstance().apply {
                timeInMillis = getCurrentCalendarWidgetTime()
            }
            val dialog = DatePickerDialog(this, DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
                // Make the text resource of the deadline in add_task_activity dhow the date selected
                val displayedMonth = month + 1
                mBinding.deadlineDate.setText("$dayOfMonth/$displayedMonth/$year")
            }, currentTime.get(Calendar.YEAR) , currentTime.get(Calendar.MONTH), currentTime.get(Calendar.DAY_OF_MONTH) )
            dialog.show()
        }

        // Open time picker for date
        mBinding.deadlineTime.setOnClickListener {
            val currentTime = Calendar.getInstance().apply {
                timeInMillis = getCurrentCalendarWidgetTime()
            }
            val dialog = TimePickerDialog(this,  TimePickerDialog.OnTimeSetListener { view, hourOfDay, minute ->
                // Make the text resource of the deadline in add_task_activity show the date selected
                mBinding.deadlineTime.setText(turnTimeToString(hourOfDay, minute))
            }, currentTime.get(Calendar.HOUR_OF_DAY) , currentTime.get(Calendar.MINUTE) , false)
            dialog.getWindow()?.setBackgroundDrawableResource(android.R.color.transparent)
            dialog.show()
        }

        // Open time picker for estimated duration
        mBinding.estimatedDurationTime.setOnClickListener {
            val dialog = TimePickerDialog(this, TimePickerDialog.OnTimeSetListener { view, hourOfDay, minute ->
                // Make the text resource of the deadline in add_task_activity dhow the date selected
                mBinding.estimatedDurationTime.setText(turnTimeToString(hourOfDay, minute))
            }, 4, 0, false)
            dialog.getWindow()?.setBackgroundDrawableResource(android.R.color.transparent)
            dialog.show()
        }

        // Initialise our PlannerManager
        mediator = PlannerMediator(true, this, System.currentTimeMillis())
    }

    private fun turnTimeToString(hour: Int, minutes: Int) : String {
        var timeString = ""
        if(hour < 10) {
            timeString += "0"
        }
        timeString += "$hour:"

        if(minutes < 10) {
            timeString += "0"
        }
        timeString += "$minutes"

        return timeString
    }

    private fun getCurrentCalendarWidgetTime() : Long {
        return System.currentTimeMillis() + numDaysShifted * ONE_DAY_MILLIS
    }

    fun onSaveButtonClick() {

        // Create task and save the events to google calendar
        val durationSplitted = duration.split(":")
        val durationMinutes = durationSplitted[1].toInt() * 60 + durationSplitted[0].toInt()

        val dateSplitted = deadlineDate.split("/")
        val day = dateSplitted[0].toInt()
        val month = dateSplitted[1].toInt()
        val year = dateSplitted[2].toInt()

        val timeSplitted = deadlineTime.split(":")
        val hour = timeSplitted[0].toInt()
        val minute = timeSplitted[1].toInt()

        val deadlineDate = Calendar.getInstance()
        deadlineDate.set(year, month, day, hour, minute)

        // Create the task and add to google calendar
        mediator.addTask(titleInput, deadlineDate.timeInMillis, durationMinutes)

        // send Intent to activate Widget
        val resultValue = Intent()
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
        resultValue.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
        setResult(RESULT_OK, resultValue)
        finish()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // send Intent to activate Widget
        val resultValue = Intent()
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
        resultValue.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
        setResult(RESULT_OK, resultValue)
        finish()
        return true
    }

}