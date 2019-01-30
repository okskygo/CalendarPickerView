package rex.okskygo.calendarpicker

import androidx.paging.DataSource
import androidx.paging.PageKeyedDataSource
import java.util.Calendar

class CalendarPickerDataSourceFactory : DataSource.Factory<Calendar, CalendarPickerDto>() {

    override fun create(): DataSource<Calendar, CalendarPickerDto> {

        return object : PageKeyedDataSource<Calendar, CalendarPickerDto>() {
            override fun loadInitial(params: LoadInitialParams<Calendar>,
                                     callback: LoadInitialCallback<Calendar, CalendarPickerDto>) {
                val firstTodayOfMonth = Calendar.getInstance().apply {
                    firstDayOfWeek = Calendar.SUNDAY
                    set(Calendar.DAY_OF_MONTH, 1)
                }
                val list = (0 until params.requestedLoadSize)
                        .map { firstTodayOfMonth.copy().apply { add(Calendar.MONTH, it) } }
                        .flatMap {
                            val list = mutableListOf<CalendarPickerDto>(MonthDto(it.copy()))

                            val firstDayOfMonth = it.copy().apply { set(Calendar.DAY_OF_MONTH, 1) }
                            val firstDayOfWeek = firstDayOfMonth.get(Calendar.DAY_OF_WEEK)
                            (1 until firstDayOfWeek).forEach { list.add(EmptyDto()) }
                            val maximum = it.getActualMaximum(Calendar.DAY_OF_MONTH)
                            (1..maximum).forEach { day ->
                                list.add(DayDto(firstDayOfMonth.copy()
                                                        .apply { set(Calendar.DATE, day) }))
                            }
                            val lastDayOfWeek = firstDayOfMonth.copy()
                                    .apply { set(Calendar.DATE, maximum) }
                                    .get(Calendar.DAY_OF_WEEK)
                            (lastDayOfWeek until 7).forEach { list.add(EmptyDto()) }
                            list
                        }
                        .toList()

                callback.onResult(list,
                                  null,
                                  firstTodayOfMonth.copy().apply { set(Calendar.MONTH, params.requestedLoadSize) })
            }

            override fun loadAfter(params: LoadParams<Calendar>, callback: LoadCallback<Calendar, CalendarPickerDto>) {
                val today = params.key.copy().apply {
                    firstDayOfWeek = Calendar.SUNDAY
                    set(Calendar.DAY_OF_MONTH, 1)
                }
                val list = (0 until params.requestedLoadSize)
                        .map { today.copy().apply { add(Calendar.MONTH, it) } }
                        .flatMap {
                            val list = mutableListOf<CalendarPickerDto>(MonthDto(it.copy()))

                            val firstDayOfMonth = it.copy().apply { set(Calendar.DAY_OF_MONTH, 1) }
                            val firstDayOfWeek = firstDayOfMonth.get(Calendar.DAY_OF_WEEK)
                            (1 until firstDayOfWeek).forEach { list.add(EmptyDto()) }
                            val maximum = it.getActualMaximum(Calendar.DAY_OF_MONTH)
                            (1..maximum).forEach { day ->
                                list.add(DayDto(firstDayOfMonth.copy().apply { set(Calendar.DATE, day) }))
                            }
                            val lastDayOfWeek = firstDayOfMonth.copy().apply { set(Calendar.DATE, maximum) }.get(
                                    Calendar.DAY_OF_WEEK)
                            (lastDayOfWeek until 7).forEach { list.add(EmptyDto()) }
                            list
                        }
                        .toList()

                callback.onResult(list,
                                  today.copy().apply { set(Calendar.MONTH, params.requestedLoadSize) })
            }

            override fun loadBefore(params: LoadParams<Calendar>,
                                    callback: LoadCallback<Calendar, CalendarPickerDto>) {
            }

        }
    }

}