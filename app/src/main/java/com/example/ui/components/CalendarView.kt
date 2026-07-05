package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Opportunity
import com.example.ui.CampusViewModel
import java.text.SimpleDateFormat
import java.util.*

data class CalendarDay(
    val day: Int,
    val isCurrentMonth: Boolean,
    val month: Int,
    val year: Int
)

@Composable
fun CalendarTabContent(
    viewModel: CampusViewModel,
    onOpportunityClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val allOpportunities by viewModel.allOpportunities.collectAsState()

    // Setup Calendar Timezone & Today Reference
    val todayCal = remember { Calendar.getInstance() }
    val todayDay = todayCal.get(Calendar.DAY_OF_MONTH)
    val todayMonth = todayCal.get(Calendar.MONTH)
    val todayYear = todayCal.get(Calendar.YEAR)

    // Selection States
    var selectedDay by remember { mutableStateOf(todayDay) }
    var selectedMonth by remember { mutableStateOf(todayMonth) }
    var selectedYear by remember { mutableStateOf(todayYear) }

    // Navigation (Viewing Month) States
    var viewedMonth by remember { mutableStateOf(todayMonth) }
    var viewedYear by remember { mutableStateOf(todayYear) }

    // Helpers
    val monthNames = remember {
        listOf(
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
        )
    }

    // Recalculate 42 days grid for the viewed month
    val daysList = remember(viewedMonth, viewedYear) {
        val list = mutableListOf<CalendarDay>()

        // Current Month Setup
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, viewedYear)
            set(Calendar.MONTH, viewedMonth)
            set(Calendar.DAY_OF_MONTH, 1)
        }
        val firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK) // 1 = Sun, 2 = Mon...
        val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)

        // Previous Month Setup
        val prevMonthCal = Calendar.getInstance().apply {
            set(Calendar.YEAR, viewedYear)
            set(Calendar.MONTH, viewedMonth - 1)
        }
        val daysInPrevMonth = prevMonthCal.getActualMaximum(Calendar.DAY_OF_MONTH)

        // Previous month padding
        val padding = firstDayOfWeek - 1
        val prevMonth = if (viewedMonth == 0) 11 else viewedMonth - 1
        val prevYear = if (viewedMonth == 0) viewedYear - 1 else viewedYear
        for (i in padding - 1 downTo 0) {
            list.add(CalendarDay(daysInPrevMonth - i, false, prevMonth, prevYear))
        }

        // Current month days
        for (i in 1..daysInMonth) {
            list.add(CalendarDay(i, true, viewedMonth, viewedYear))
        }

        // Next month padding to complete 42 cells (6 rows of 7)
        val nextMonth = if (viewedMonth == 11) 0 else viewedMonth + 1
        val nextYear = if (viewedMonth == 11) viewedYear + 1 else viewedYear
        val remaining = 42 - list.size
        for (i in 1..remaining) {
            list.add(CalendarDay(i, false, nextMonth, nextYear))
        }

        list
    }

    // Filter items matching the selected day
    val occurrencesForSelectedDay = remember(allOpportunities, selectedDay, selectedMonth, selectedYear) {
        val events = allOpportunities.filter { isSameDay(it.dateTime, selectedDay, selectedMonth, selectedYear) }
            .map { CalendarItem(it, isDeadline = false) }
        val deadlines = allOpportunities.filter { isSameDay(it.deadline, selectedDay, selectedMonth, selectedYear) }
            .map { CalendarItem(it, isDeadline = true) }
        events + deadlines
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("calendar_tab_container"),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        // Month Header Selector
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${monthNames[viewedMonth]} $viewedYear",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                        Row {
                            IconButton(
                                onClick = {
                                    if (viewedMonth == 0) {
                                        viewedMonth = 11
                                        viewedYear -= 1
                                    } else {
                                        viewedMonth -= 1
                                    }
                                },
                                modifier = Modifier.testTag("calendar_prev_month")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ChevronLeft,
                                    contentDescription = "Previous Month",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            IconButton(
                                onClick = {
                                    if (viewedMonth == 11) {
                                        viewedMonth = 0
                                        viewedYear += 1
                                    } else {
                                        viewedMonth += 1
                                    }
                                },
                                modifier = Modifier.testTag("calendar_next_month")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = "Next Month",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Days of Week Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val daysOfWeek = listOf("Su", "Mo", "Tu", "We", "Th", "Fr", "Sa")
                        daysOfWeek.forEach { day ->
                            Text(
                                text = day,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Black),
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // 6 Rows of 7 Days Grid
                    val chunks = daysList.chunked(7)
                    chunks.forEach { week ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            week.forEach { calendarDay ->
                                val isSelected = selectedDay == calendarDay.day &&
                                        selectedMonth == calendarDay.month &&
                                        selectedYear == calendarDay.year

                                val isToday = todayDay == calendarDay.day &&
                                        todayMonth == calendarDay.month &&
                                        todayYear == calendarDay.year

                                val dayEvents = allOpportunities.filter {
                                    isSameDay(it.dateTime, calendarDay.day, calendarDay.month, calendarDay.year)
                                }
                                val dayDeadlines = allOpportunities.filter {
                                    isSameDay(it.deadline, calendarDay.day, calendarDay.month, calendarDay.year)
                                }

                                DayCell(
                                    calendarDay = calendarDay,
                                    isSelected = isSelected,
                                    isToday = isToday,
                                    events = dayEvents,
                                    deadlines = dayDeadlines,
                                    onClick = {
                                        selectedDay = calendarDay.day
                                        selectedMonth = calendarDay.month
                                        selectedYear = calendarDay.year
                                        if (!calendarDay.isCurrentMonth) {
                                            viewedMonth = calendarDay.month
                                            viewedYear = calendarDay.year
                                        }
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Section Title
        item {
            val dateLabel = remember(selectedDay, selectedMonth, selectedYear) {
                val cal = Calendar.getInstance().apply {
                    set(Calendar.DAY_OF_MONTH, selectedDay)
                    set(Calendar.MONTH, selectedMonth)
                    set(Calendar.YEAR, selectedYear)
                }
                SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault()).format(cal.time)
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Event,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Schedule for $dateLabel",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        // List of Events and Deadlines on Selected Day
        if (occurrencesForSelectedDay.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No events or deadlines",
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Tap other dates with colored dots above to plan your schedule.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 32.dp)
                        )
                    }
                }
            }
        } else {
            items(occurrencesForSelectedDay) { calendarItem ->
                CalendarEventCard(
                    opportunity = calendarItem.opportunity,
                    isDeadline = calendarItem.isDeadline,
                    onClick = { onOpportunityClick(calendarItem.opportunity.id) }
                )
            }
        }
    }
}

data class CalendarItem(
    val opportunity: Opportunity,
    val isDeadline: Boolean
)

@Composable
fun DayCell(
    calendarDay: CalendarDay,
    isSelected: Boolean,
    isToday: Boolean,
    events: List<Opportunity>,
    deadlines: List<Opportunity>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .clip(CircleShape)
            .background(
                when {
                    isSelected -> MaterialTheme.colorScheme.primary
                    else -> Color.Transparent
                }
            )
            .border(
                width = if (isToday && !isSelected) 1.5.dp else 0.dp,
                color = if (isToday && !isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                shape = CircleShape
            )
            .clickable(onClick = onClick)
            .testTag("calendar_day_${calendarDay.year}_${calendarDay.month}_${calendarDay.day}"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        val textColor = when {
            isSelected -> Color.White
            !calendarDay.isCurrentMonth -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
            isToday -> MaterialTheme.colorScheme.primary
            else -> MaterialTheme.colorScheme.onSurface
        }

        Text(
            text = calendarDay.day.toString(),
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal
            ),
            color = textColor
        )

        Spacer(modifier = Modifier.height(2.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (events.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) Color.White else MaterialTheme.colorScheme.primary)
                )
            }
            if (deadlines.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) Color.White else Color(0xFFEF5350))
                )
            }
        }
    }
}

@Composable
fun CalendarEventCard(
    opportunity: Opportunity,
    isDeadline: Boolean,
    onClick: () -> Unit
) {
    val timeFormatter = remember { SimpleDateFormat("h:mm a", Locale.getDefault()) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable(onClick = onClick)
            .testTag("calendar_item_${opportunity.id}_$isDeadline"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Accent color indicator on the left side
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(48.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(if (isDeadline) Color(0xFFEF5350) else MaterialTheme.colorScheme.primary)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = opportunity.organization.uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = MaterialTheme.colorScheme.outline,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                if (isDeadline) Color(0xFFFFEBEE)
                                else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                            )
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (isDeadline) "⚠️ DEADLINE" else "📅 EVENT",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Black,
                            color = if (isDeadline) Color(0xFFC62828) else MaterialTheme.colorScheme.primary,
                            letterSpacing = 0.5.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = opportunity.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = "Time",
                            modifier = Modifier.size(12.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = timeFormatter.format(Date(if (isDeadline) opportunity.deadline else opportunity.dateTime)),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (opportunity.location.lowercase() == "virtual") Icons.Default.LaptopMac else Icons.Default.Place,
                            contentDescription = "Location",
                            modifier = Modifier.size(12.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = opportunity.location,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Details",
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
        }
    }
}

fun isSameDay(timestamp: Long, day: Int, month: Int, year: Int): Boolean {
    val cal = Calendar.getInstance()
    cal.timeInMillis = timestamp
    return cal.get(Calendar.DAY_OF_MONTH) == day &&
            cal.get(Calendar.MONTH) == month &&
            cal.get(Calendar.YEAR) == year
}
