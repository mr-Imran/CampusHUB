package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Opportunity
import com.example.data.model.Registration
import com.example.ui.CampusViewModel
import com.example.ui.components.AdBanner
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DashboardScreen(
    viewModel: CampusViewModel,
    onOpportunityClick: (Int) -> Unit,
    onNotificationsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val userProfile by viewModel.userProfile.collectAsState()
    val isOrganizerMode by viewModel.isOrganizerMode.collectAsState()

    // Sub-tab selection for student: 0 = Discover, 1 = My Registrations
    var selectedStudentTab by remember { mutableStateOf(0) }

    val notifications by viewModel.allNotifications.collectAsState()
    val unreadNotificationsCount = notifications.count { !it.isRead }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                                Color.Transparent
                            )
                        )
                    )
                    .windowInsetsPadding(WindowInsets.safeDrawing)
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "CAMPUS HUB • ${userProfile?.name?.uppercase() ?: "STUDENT"}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.5.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "UNIFIED",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = (-1).sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = ".",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // In-App Notification Bell
                IconButton(
                    onClick = onNotificationsClick,
                    modifier = Modifier.testTag("notification_bell")
                ) {
                    BadgedBox(
                        badge = {
                            if (unreadNotificationsCount > 0) {
                                Badge {
                                    Text(
                                        text = unreadNotificationsCount.toString(),
                                        modifier = Modifier.testTag("notification_badge_count")
                                    )
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notifications Feed",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Mode Toggle Button (Student vs Organizer)
                FilledTonalButton(
                    onClick = { viewModel.isOrganizerMode.value = !isOrganizerMode },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = if (isOrganizerMode) {
                            MaterialTheme.colorScheme.secondaryContainer
                        } else {
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                        }
                    ),
                    modifier = Modifier.testTag("mode_toggle_button")
                ) {
                    Icon(
                        imageVector = if (isOrganizerMode) Icons.Default.ManageAccounts else Icons.Default.School,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isOrganizerMode) "Organizer" else "Discover",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Student Mode View
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Segmented Control (Tabs) as Capsule Buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val myRegs by viewModel.studentRegistrations.collectAsState()

                    // Discover Tab Button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(24.dp))
                            .background(
                                if (selectedStudentTab == 0) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.secondary
                            )
                            .clickable { selectedStudentTab = 0 }
                            .padding(horizontal = 20.dp, vertical = 10.dp)
                            .testTag("student_tab_discover"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Discover",
                            color = if (selectedStudentTab == 0) Color.White else MaterialTheme.colorScheme.onSecondary,
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp
                        )
                    }

                    // Registrations Tab Button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(24.dp))
                            .background(
                                if (selectedStudentTab == 1) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.secondary
                            )
                            .clickable { selectedStudentTab = 1 }
                            .padding(horizontal = 20.dp, vertical = 10.dp)
                            .testTag("student_tab_registrations"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "My Events (${myRegs.size})",
                            color = if (selectedStudentTab == 1) Color.White else MaterialTheme.colorScheme.onSecondary,
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp
                        )
                    }
                }

                if (selectedStudentTab == 0) {
                    DiscoverTabContent(
                        viewModel = viewModel,
                        onOpportunityClick = onOpportunityClick
                    )
                } else {
                    RegistrationsTabContent(
                        viewModel = viewModel,
                        onOpportunityClick = onOpportunityClick
                    )
                }
            }
            AdBanner()
        }
    }
}

@Composable
fun DiscoverTabContent(
    viewModel: CampusViewModel,
    onOpportunityClick: (Int) -> Unit
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val selectedTagFilter by viewModel.selectedTagFilter.collectAsState()

    val recommendedOps by viewModel.recommendedOpportunities.collectAsState()
    val filteredOps by viewModel.filteredOpportunities.collectAsState()

    val categories = listOf("All", "Event", "Competition", "Ambassador")
    val allTags = listOf("Coding", "Design", "Business", "Marketing", "Leadership", "Public Speaking")

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        // Search bar
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.searchQuery.value = it },
                placeholder = { Text("Search Hackathons, Roles, Seminars...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
                    .testTag("search_opportunities_input"),
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = "Search Icon")
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.searchQuery.value = "" }) {
                            Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear Search")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
        }

        // Category Filter Row
        item {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { category ->
                    val isSelected = selectedCategory == category
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.selectedCategory.value = category },
                        label = { Text(if (category == "All") "All Categories" else "${category}s") },
                        leadingIcon = {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("category_chip_$category")
                    )
                }
            }
        }

        // Horizontal tags filter
        item {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                item {
                    val isAllSelected = selectedTagFilter == null
                    InputChip(
                        selected = isAllSelected,
                        onClick = { viewModel.selectedTagFilter.value = null },
                        label = { Text("All Tags") },
                        shape = RoundedCornerShape(8.dp)
                    )
                }

                items(allTags) { tag ->
                    val isSelected = selectedTagFilter == tag
                    InputChip(
                        selected = isSelected,
                        onClick = { viewModel.selectedTagFilter.value = tag },
                        label = { Text(tag) },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("tag_chip_$tag")
                    )
                }
            }
        }

        // Personalized Recommendations Section (Only show if there are some matching recommendations)
        if (recommendedOps.isNotEmpty() && selectedCategory == "All" && searchQuery.isEmpty() && selectedTagFilter == null) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "RECOMMENDED FOR YOU",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = (-0.5).sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(recommendedOps) { op ->
                        RecommendationCard(opportunity = op, onClick = { onOpportunityClick(op.id) })
                    }
                }
            }
        }

        // Main List Header
        item {
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = (if (selectedCategory == "All") "ALL OPPORTUNITIES" else "CAMPUS ${selectedCategory}S").uppercase(),
                fontSize = 15.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = (-0.5).sp,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
            )
        }

        // Opportunities List
        if (filteredOps.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SearchOff,
                            contentDescription = "No results",
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Text(
                            text = "No opportunities found",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Try adjusting your filters or query",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(filteredOps) { op ->
                OpportunityCard(opportunity = op, onClick = { onOpportunityClick(op.id) })
            }
        }
    }
}

@Composable
fun RegistrationsTabContent(
    viewModel: CampusViewModel,
    onOpportunityClick: (Int) -> Unit
) {
    val myRegistrations by viewModel.studentRegistrations.collectAsState()
    val allOps by viewModel.allOpportunities.collectAsState()

    if (myRegistrations.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(40.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.EventNote,
                    contentDescription = null,
                    modifier = Modifier.size(56.dp),
                    tint = MaterialTheme.colorScheme.outline
                )
                Text(
                    text = "No Registrations Yet",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Explore the Discover tab to seamlessly register for events, hackathons, and company roles.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(myRegistrations) { registration ->
                val op = allOps.find { it.id == registration.opportunityId }
                if (op != null) {
                    StudentRegistrationCard(
                        registration = registration,
                        opportunity = op,
                        onCancelClick = { viewModel.cancelRegistration(registration.id) },
                        onCardClick = { onOpportunityClick(op.id) }
                    )
                }
            }
        }
    }
}

// --- Composable Subcomponents ---

@Composable
fun RecommendationCard(
    opportunity: Opportunity,
    onClick: () -> Unit
) {
    val formatter = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }
    val formattedDate = formatter.format(Date(opportunity.dateTime))

    Card(
        modifier = Modifier
            .width(290.dp)
            .height(180.dp)
            .clickable(onClick = onClick)
            .testTag("recommendation_card_${opportunity.id}"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer // Light slate 100 background
        ),
        shape = RoundedCornerShape(24.dp) // Large rounded corners matching rounded-3xl
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = opportunity.organization.uppercase(),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp
                    )
                    
                    // High-contrast custom featured/recommended badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.primary)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "RECOMMENDED",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onPrimary,
                            letterSpacing = 0.5.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = opportunity.title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    lineHeight = 22.sp,
                    letterSpacing = (-0.5).sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = formattedDate,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.weight(1f))
                
                // Small round button inside
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                        .clickable(onClick = onClick),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = "Details",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun OpportunityCard(
    opportunity: Opportunity,
    onClick: () -> Unit
) {
    val monthFormatter = remember { SimpleDateFormat("MMM", Locale.getDefault()) }
    val dayFormatter = remember { SimpleDateFormat("dd", Locale.getDefault()) }
    val monthStr = monthFormatter.format(Date(opportunity.dateTime)).uppercase()
    val dayStr = dayFormatter.format(Date(opportunity.dateTime))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp)
            .clickable(onClick = onClick)
            .testTag("opportunity_card_${opportunity.id}"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) // Light violet background
        ),
        shape = RoundedCornerShape(20.dp) // rounded-2xl style
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Elegant left-side vertical date block
            Column(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primary)
                    .padding(vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = monthStr,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    lineHeight = 10.sp
                )
                Text(
                    text = dayStr,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    lineHeight = 18.sp
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Main Info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = opportunity.organization.uppercase(),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    CategoryBadge(category = opportunity.category)
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = opportunity.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface,
                    letterSpacing = (-0.25).sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(1.dp))

                Text(
                    text = "${opportunity.location} • ${opportunity.description}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Beautiful rounded secondary action icon on the right
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
                    .clickable(onClick = onClick),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

@Composable
fun StudentRegistrationCard(
    registration: Registration,
    opportunity: Opportunity,
    onCancelClick: () -> Unit,
    onCardClick: () -> Unit
) {
    val formatter = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }
    val formattedDate = formatter.format(Date(opportunity.dateTime))

    val statusColor = when (registration.status) {
        "Approved" -> Color(0xFF2E7D32)
        "Waitlisted" -> Color(0xFFEF6C00)
        else -> MaterialTheme.colorScheme.primary
    }

    val statusBg = when (registration.status) {
        "Approved" -> Color(0xFFE8F5E9)
        "Waitlisted" -> Color(0xFFFFF3E0)
        else -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onCardClick)
            .testTag("registration_card_${registration.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = opportunity.organization.uppercase(),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.outline
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(statusBg)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = registration.status,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = opportunity.title,
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.outline
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = formattedDate,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.weight(1f))
                TextButton(
                    onClick = onCancelClick,
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(imageVector = Icons.Default.Cancel, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Withdraw", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun CategoryBadge(category: String) {
    val containerColor = when (category) {
        "Event" -> MaterialTheme.colorScheme.secondaryContainer
        "Competition" -> MaterialTheme.colorScheme.tertiaryContainer
        "Ambassador" -> Color(0xFFE1BEE7)
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    val contentColor = when (category) {
        "Event" -> MaterialTheme.colorScheme.onSecondaryContainer
        "Competition" -> MaterialTheme.colorScheme.onTertiaryContainer
        "Ambassador" -> Color(0xFF4A148C)
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(containerColor)
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(
            text = category,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = contentColor
        )
    }
}
