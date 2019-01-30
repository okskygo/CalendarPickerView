package rex.okskygo.calendarpicker

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.holder_calendar_picker.view.calendarBackground
import kotlinx.android.synthetic.main.holder_calendar_picker.view.textView
import kotlinx.android.synthetic.main.holder_month_calendar_picker.view.monthTextView
import java.util.Calendar

class CalendarPickerAdapter(val onSelectListener: (Calendar, CalendarPickerType) -> Unit)
    : PagedListAdapter<CalendarPickerDto, RecyclerView.ViewHolder>(DIFF) {

    companion object {

        const val MONTH_TYPE = 1
        const val DAY_TYPE = 2
        const val EMPTY_TYPE = 3

        private val DIFF = object : DiffUtil.ItemCallback<CalendarPickerDto>() {

            override fun areItemsTheSame(oldItem: CalendarPickerDto, newItem: CalendarPickerDto): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: CalendarPickerDto, newItem: CalendarPickerDto): Boolean {
                return oldItem == newItem
            }
        }

    }

    private var beginCalendar: Calendar? = null
    private var endCalendar: Calendar? = null

    private val clickListener: (Calendar) -> Unit = { calendar ->
        val lastBeginCalendar = beginCalendar
        val lastEndCalendar = endCalendar
        if (lastBeginCalendar != null && endCalendar != null) {
            val endPair = find(lastEndCalendar)
            val beginPair = find(lastBeginCalendar)
            val endIndex = endPair.first
            val beginIndex = beginPair.first
            if (endIndex != null && beginIndex != null) {
                val list = currentList?.subList(beginIndex, endIndex + 1)
                if (list != null) {
                    list.forEach { dto ->
                        when (dto) {
                            is DayDto -> dto.selectState = DaySelectState.NONE
                            is EmptyDto -> dto.selectState = DaySelectState.NONE
                        }
                    }
                    notifyItemRangeChanged(beginIndex, list.size, "select")
                }
            }
            find(calendar).let { pair ->
                pair.second?.selectState = DaySelectState.LONELY
                pair.first?.let { notifyItemChanged(it, "select") }
            }
            beginCalendar = calendar.copy()
            endCalendar = null
            onSelectListener.invoke(calendar, CalendarPickerType.BEGIN)
        } else if (lastBeginCalendar == null || lastBeginCalendar.time >= calendar.time) {
            beginCalendar = calendar.copy()
            endCalendar = null
            find(lastBeginCalendar).let { pair ->
                pair.second?.selectState = DaySelectState.NONE
                pair.first?.let { notifyItemChanged(it, "select") }
            }
            find(calendar).let { pair ->
                pair.second?.selectState = DaySelectState.LONELY
                pair.first?.let { notifyItemChanged(it, "select") }
            }
            onSelectListener.invoke(calendar, CalendarPickerType.BEGIN)
        } else {
            endCalendar = calendar.copy()
            val endPair = find(calendar)
            val beginPair = find(lastBeginCalendar)
            val endIndex = endPair.first
            val beginIndex = beginPair.first
            if (endIndex != null && beginIndex != null) {
                val list = currentList?.subList(beginIndex, endIndex + 1)
                if (list != null) {
                    list.forEachIndexed { index, dto ->
                        val state = when (index) {
                            0 -> DaySelectState.BEGIN
                            list.lastIndex -> DaySelectState.END
                            else -> DaySelectState.MIDDLE
                        }
                        when (dto) {
                            is DayDto -> dto.selectState = state
                            is EmptyDto -> dto.selectState = state
                        }
                    }
                    notifyItemRangeChanged(beginIndex, list.size, "select")
                }
                onSelectListener.invoke(calendar, CalendarPickerType.END)
            }
        }
    }

    private fun find(calendar: Calendar?): Pair<Int?, DayDto?> {
        if (calendar == null) {
            return Pair(null, null)
        }
        val index = currentList?.indexOfFirst {
            (it is DayDto
                    && calendar.get(Calendar.DAY_OF_YEAR) == it.calendar.get(Calendar.DAY_OF_YEAR)
                    && calendar.get(Calendar.YEAR) == it.calendar.get(Calendar.YEAR))
        }
        val dayDto = index?.let { getItem(it) } as? DayDto
        return Pair(index, dayDto)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            MONTH_TYPE -> CalendarPickerMonthViewHolder(parent)
            DAY_TYPE -> CalendarPickerViewHolder(parent, clickListener)
            EMPTY_TYPE -> CalendarPickerViewHolder(parent, clickListener)
            else -> throw IllegalArgumentException()
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int, payloads: MutableList<Any>) {
        onBindViewHolder(holder, position)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        if (holder is CalendarPickerViewHolder) {
            when (item) {
                is EmptyDto -> holder.bindEmpty(item)
                is DayDto -> holder.bind(item)
            }
        } else if (holder is CalendarPickerMonthViewHolder) {
            if (item is MonthDto) {
                holder.bind(item)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        val item = getItem(position)
        return when (item) {
            is EmptyDto -> EMPTY_TYPE
            is MonthDto -> MONTH_TYPE
            is DayDto -> DAY_TYPE
            else -> throw IllegalArgumentException()
        }
    }

    fun findTodayPosition(): Int? {
        val today = Calendar.getInstance()
        return find(today).first
    }

    private class CalendarPickerViewHolder(parent: ViewGroup, clickListener: (Calendar) -> Unit) :
            RecyclerView.ViewHolder(
                    LayoutInflater.from(parent.context).inflate(R.layout.holder_calendar_picker, parent, false)) {

        private var dayDto: DayDto? = null

        init {
            itemView.setOnClickListener {
                if (dayDto?.compareDay != DayDto.DayEnum.BEFORE) {
                    dayDto?.calendar?.let(clickListener)
                }
            }
        }

        fun bindEmpty(emptyDto: EmptyDto) {
            dayDto = null
            itemView.textView.text = ""
            bindSelectState(emptyDto.selectState)
        }

        fun bind(dayDto: DayDto) {
            this.dayDto = dayDto
            itemView.calendarBackground.setBackgroundResource(0)
            itemView.textView.text = dayDto.calendar.get(Calendar.DATE).toString()
            if (dayDto.selectState == DaySelectState.NONE) {
                when (dayDto.compareDay) {
                    DayDto.DayEnum.TODAY -> {
                        itemView.textView.setTextColor(ContextCompat.getColor(itemView.context, R.color.colorTextHigh))
                        itemView.calendarBackground.setBackgroundResource(R.drawable.bg_calendar_today)
                    }
                    DayDto.DayEnum.BEFORE -> {
                        itemView.textView.setTextColor(
                                ContextCompat.getColor(itemView.context, R.color.colorTextDisabled))
                    }
                    DayDto.DayEnum.AFTER -> {
                        itemView.textView.setTextColor(ContextCompat.getColor(itemView.context, R.color.colorTextHigh))
                    }
                }
            } else {
                bindSelectState(dayDto.selectState)
            }

        }

        private fun bindSelectState(selectState: DaySelectState) {
            when (selectState) {
                DaySelectState.NONE -> {
                    itemView.calendarBackground.setBackgroundResource(0)
                }
                DaySelectState.LONELY -> {
                    itemView.textView.setTextColor(Color.WHITE)
                    itemView.calendarBackground.setBackgroundResource(R.drawable.bg_calendar_lonely)
                }
                DaySelectState.BEGIN -> {
                    itemView.textView.setTextColor(Color.WHITE)
                    itemView.calendarBackground.setBackgroundResource(R.drawable.bg_calendar_begin)
                }
                DaySelectState.MIDDLE -> {
                    itemView.textView.setTextColor(Color.WHITE)
                    itemView.calendarBackground.setBackgroundResource(R.drawable.bg_calendar_middle)
                }
                DaySelectState.END -> {
                    itemView.textView.setTextColor(Color.WHITE)
                    itemView.calendarBackground.setBackgroundResource(R.drawable.bg_calendar_end)
                }
            }
        }

    }

    private class CalendarPickerMonthViewHolder(parent: ViewGroup) :
            RecyclerView.ViewHolder(
                    LayoutInflater.from(parent.context).inflate(R.layout.holder_month_calendar_picker, parent, false)) {

        fun bind(monthDto: MonthDto) {
            itemView.monthTextView.text = "${monthDto.calendar.get(Calendar.MONTH) + 1}月"
        }

    }
}
