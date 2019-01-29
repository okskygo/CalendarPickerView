package rex.okskygo.calendarpicker

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import androidx.recyclerview.widget.GridLayoutManager
import kotlinx.android.synthetic.main.layout_calendar_picker_view.view.*
import java.util.*

class CalendarPickerView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val calendarPickerAdapter: CalendarPickerAdapter

    var lifecycleOwner: LifecycleOwner? = null
        set(value) {
            if (value != null) {
                val config = PagedList.Config.Builder()
                        .setPageSize(12)
                        .setInitialLoadSizeHint(12)
                        .setEnablePlaceholders(false)
                        .build()
                val liveData = LivePagedListBuilder(CalendarPickerDataSourceFactory(), config)
                        .build()
                liveData.observe(value, Observer { items ->
                    calendarPickerAdapter.submitList(items)
                })
            }
            field = value
        }

    var onSelectListener: ((Calendar, CalendarPickerType) -> Unit)? = null

    init {
        LayoutInflater.from(context).inflate(R.layout.layout_calendar_picker_view, this)

        val gridLayoutManager = GridLayoutManager(context, 7)
        gridLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                val adapter = recyclerView.adapter
                return if (adapter is CalendarPickerAdapter) {
                    when (adapter.getItemViewType(position)) {
                        CalendarPickerAdapter.EMPTY_TYPE -> 1
                        CalendarPickerAdapter.MONTH_TYPE -> 7
                        CalendarPickerAdapter.DAY_TYPE -> 1
                        else -> 1
                    }
                } else {
                    1
                }
            }
        }
        recyclerView.layoutManager = gridLayoutManager
        calendarPickerAdapter = CalendarPickerAdapter(this::onSelect)
        recyclerView.adapter = calendarPickerAdapter
        if (context is LifecycleOwner) {
            lifecycleOwner = context
        }
    }

    private fun onSelect(calendar: Calendar, type: CalendarPickerType) {
        onSelectListener?.invoke(calendar, type)
    }

}

enum class CalendarPickerType {
    BEGIN, END
}

fun Calendar.copy(): Calendar = this.clone() as Calendar