package net.planner.exampleapp

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import androidx.databinding.DataBindingUtil
import net.planner.exampleapp.databinding.ActivityAddTaskBinding

class AddTaskActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "AddTaskActivity"

        fun createIntent(context: Context, widgetId: Int): Intent {
            val intent = Intent(context, AddTaskActivity::class.java)
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
            return intent
        }
    }

    private val mContent = Content()
    private lateinit var mBinding: ActivityAddTaskBinding

    private var widgetId: Int = 0

    private val titleInput: String
        get() = mBinding.edtTitle.text.toString().trim { it <= ' '}

    private val deadline: String
        get() = mBinding.deadlineDate.text.toString().trim { it <= ' '}

    private val duration: String
        get() = mBinding.estimatedDurationTime.text.toString().trim { it <= ' '}

    private val minSessionTime: String
        get() = mBinding.minSessionTime.text.toString().trim { it <= ' '}



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_add_task)
        mBinding.content = mContent
        mBinding.eventHandler = this

        widgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,0)


        // @TODO need to instantiate savedInstanceState with WidgetProvider information like which calendars to use

        // Initialise our PlannerManager
//        val plannerManager = PlannerManager()

    }

    fun onActionButtonClick() {
        // Event handler for save clicked

        // send Intent to activate Widget
        val resultValue = Intent()
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
        resultValue.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
        setResult(RESULT_OK, resultValue)
        finish()
    }

    // @TODO add listener for planner work finish


    class Content : BaseObservable() {
        @get:Bindable
        var title: String = "Task"
            set(customerName) {
                field = customerName
                notifyPropertyChanged(BR.title)
            }

        @get:Bindable
        var deadline: String = "29/06/21"
            set(customerName) {
                field = customerName
                notifyPropertyChanged(BR.deadline)
            }

        @get:Bindable
        var duration: String = ""
            set(customerName) {
                field = customerName
                notifyPropertyChanged(BR.duration)
            }

        @get:Bindable
        var minSessionTime: String = "15"
            set(customerName) {
                field = customerName
                notifyPropertyChanged(BR.minSessionTime)
            }

    }



}