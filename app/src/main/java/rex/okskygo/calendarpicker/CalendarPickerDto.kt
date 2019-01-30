package rex.okskygo.calendarpicker

import java.util.Calendar

sealed class CalendarPickerDto

class EmptyDto : CalendarPickerDto() {
    var selectState = DaySelectState.NONE
}

data class MonthDto(val calendar: Calendar) : CalendarPickerDto()

data class DayDto(val calendar: Calendar) : CalendarPickerDto() {

    enum class DayEnum {
        TODAY, BEFORE, AFTER
    }

    var selectState = DaySelectState.NONE

    val compareDay by lazy {
        val today = Calendar.getInstance()
        if (today.get(Calendar.DAY_OF_YEAR) == calendar.get(Calendar.DAY_OF_YEAR) &&
                today.get(Calendar.YEAR) == calendar.get(Calendar.YEAR)) {
            DayEnum.TODAY
        } else if (today.get(Calendar.DAY_OF_YEAR) > calendar.get(Calendar.DAY_OF_YEAR) &&
                today.get(Calendar.YEAR) >= calendar.get(Calendar.YEAR)) {
            DayEnum.BEFORE
        } else {
            DayEnum.AFTER
        }
    }

}

enum class DaySelectState {
    NONE, LONELY, BEGIN, MIDDLE, END
}