package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Opportunity
import com.example.data.model.Registration
import com.example.ui.CampusViewModel
import com.example.ui.components.AdBanner
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OrganizerDashboardScreen(
    viewModel: CampusViewModel,
    onOpportunityClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val userProfile by viewModel.userProfile.collectAsState()
    val allOps by viewModel.allOpportunities.collectAsState()
    val allRegistrations by viewModel.allRegistrations.collectAsState()

    // Filter opportunities posted by this user
    val myPostedOps = remember(allOps, userProfile) {
        val email = userProfile?.email ?: "organizer@university.edu"
        allOps.filter { it.posterEmail.equals(email, ignoreCase = true) }
    }

    var expandedOpId by remember { mutableStateOf<Int?>(null) }
    var showPostForm by remember { mutableStateOf(false) }
    var showPostSuccessOverlay by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header Toggle Section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.safeDrawing)
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "ORGANIZER FEED • ${userProfile?.name?.uppercase() ?: "STAFF"}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.5.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "DASHBOARD",
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

                FilledTonalButton(
                    onClick = { viewModel.isOrganizerMode.value = false },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Icon(imageVector = Icons.Default.Explore, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Discover", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Quick Dashboard Stats for Posters (from Design HTML spec)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = "ACTIVE POSTS",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = (-0.5).sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Live Insights",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Card 1: Total Posts
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "TOTAL POSTS",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                letterSpacing = 1.sp
                            )
                            Row(
                                verticalAlignment = Alignment.Bottom,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = myPostedOps.size.toString(),
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Active",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(bottom = 3.dp)
                                )
                            }
                            Text(
                                text = "Published listings",
                                fontSize = 9.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Card 2: Total Registrants
                    val totalApplicants = remember(allRegistrations, myPostedOps) {
                        allRegistrations.filter { reg -> myPostedOps.any { it.id == reg.opportunityId } }.size
                    }
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "REGISTRANTS",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                letterSpacing = 1.sp
                            )
                            Row(
                                verticalAlignment = Alignment.Bottom,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = totalApplicants.toString(),
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "+100%",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF2E7D32),
                                    modifier = Modifier.padding(bottom = 3.dp)
                                )
                            }
                            Text(
                                text = "Total registrations",
                                fontSize = 9.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            // Posted Opportunities List
            if (myPostedOps.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Campaign,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Text(
                            text = "No Opportunities Posted",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Publish your first Competition, Campus Ambassador application, or Event. Click the '+' button below to get started!",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(myPostedOps) { op ->
                        val opRegistrations = remember(allRegistrations) {
                            allRegistrations.filter { it.opportunityId == op.id }
                        }
                        val isExpanded = expandedOpId == op.id

                        OrganizerOpportunityCard(
                            opportunity = op,
                            registrantsCount = opRegistrations.size,
                            isExpanded = isExpanded,
                            onToggleExpand = {
                                expandedOpId = if (isExpanded) null else op.id
                            },
                            onDeleteClick = { viewModel.deleteOpportunity(op) },
                            registrantsContent = {
                                if (opRegistrations.isEmpty()) {
                                    Text(
                                        text = "No registrations yet for this opportunity.",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.outline,
                                        modifier = Modifier.padding(16.dp)
                                    )
                                } else {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 8.dp)
                                    ) {
                                        Text(
                                            text = "Active Registrations (${opRegistrations.size})",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                                        )
                                        opRegistrations.forEach { reg ->
                                            RegistrantRow(
                                                registration = reg,
                                                onApprove = {
                                                    viewModel.updateRegistrationStatus(reg, "Approved", op.title)
                                                },
                                                onWaitlist = {
                                                    viewModel.updateRegistrationStatus(reg, "Waitlisted", op.title)
                                                },
                                                onDelete = {
                                                    viewModel.cancelRegistration(reg.id)
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        )
                    }
                }
            }
            AdBanner()
        }

        // FAB to post new opportunity
        FloatingActionButton(
            onClick = { showPostForm = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .navigationBarsPadding()
                .padding(24.dp)
                .testTag("post_opportunity_fab"),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Post Opportunity")
        }

        // Animated Full Screen slide-up Post Form Panel
        AnimatedVisibility(
            visible = showPostForm,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it })
        ) {
            PostOpportunityFormPanel(
                viewModel = viewModel,
                onDismiss = { showPostForm = false },
                onSuccess = {
                    showPostForm = false
                    showPostSuccessOverlay = true
                }
            )
        }

        // Custom Visual Checkmark Success Feedback Overlay for Organizer
        AnimatedVisibility(
            visible = showPostSuccessOverlay,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            var animateCheck by remember { mutableStateOf(false) }
            LaunchedEffect(showPostSuccessOverlay) {
                if (showPostSuccessOverlay) {
                    animateCheck = true
                } else {
                    animateCheck = false
                }
            }

            val scale by animateFloatAsState(
                targetValue = if (animateCheck) 1f else 0.2f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                ),
                label = "scale"
            )
            val rotation by animateFloatAsState(
                targetValue = if (animateCheck) 360f else 0f,
                animationSpec = tween(durationMillis = 600, easing = LinearOutSlowInEasing),
                label = "rotation"
            )
            val contentAlpha by animateFloatAsState(
                targetValue = if (animateCheck) 1f else 0f,
                animationSpec = tween(durationMillis = 400, delayMillis = 300),
                label = "contentAlpha"
            )
            val contentOffsetY by animateFloatAsState(
                targetValue = if (animateCheck) 0f else 40f,
                animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy),
                label = "contentOffsetY"
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.85f))
                    .clickable { showPostSuccessOverlay = false },
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Success",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .size(96.dp)
                            .graphicsLayer(scaleX = scale, scaleY = scale, rotationZ = rotation)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.graphicsLayer(alpha = contentAlpha, translationY = contentOffsetY)
                    ) {
                        Text(
                            text = "Opportunity Published! 🚀",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Your posting is now live on the Campus Discover feed for all students.",
                            fontSize = 14.sp,
                            color = Color.LightGray,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        Spacer(modifier = Modifier.height(32.dp))
                        Button(
                            onClick = { showPostSuccessOverlay = false },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("Done", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OrganizerOpportunityCard(
    opportunity: Opportunity,
    registrantsCount: Int,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onDeleteClick: () -> Unit,
    registrantsContent: @Composable () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("organizer_card_${opportunity.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CategoryBadge(category = opportunity.category)
                        Text(
                            text = "${registrantsCount} Registered",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = opportunity.title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                IconButton(onClick = onDeleteClick) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error
                    )
                }

                IconButton(
                    onClick = onToggleExpand,
                    modifier = Modifier.testTag("expand_registrations_button_${opportunity.id}")
                ) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = "Expand Applicants"
                    )
                }
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                ) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                    registrantsContent()
                }
            }
        }
    }
}

@Composable
fun RegistrantRow(
    registration: Registration,
    onApprove: () -> Unit,
    onWaitlist: () -> Unit,
    onDelete: () -> Unit
) {
    val statusColor = when (registration.status) {
        "Approved" -> Color(0xFF2E7D32)
        "Waitlisted" -> Color(0xFFEF6C00)
        else -> MaterialTheme.colorScheme.primary
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .testTag("registrant_row_${registration.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
        shape = RoundedCornerShape(10.dp)
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
                    text = registration.studentName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = registration.status,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = statusColor,
                    modifier = Modifier
                        .background(statusColor.copy(alpha = 0.12f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = "${registration.studentMajor} | ${registration.studentUniversity}",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "Email: ${registration.studentEmail} | Grad Year: ${registration.gradYear}",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.outline
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Organizer Registration Management Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (registration.status != "Approved") {
                    FilledTonalButton(
                        onClick = onApprove,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = Color(0xFFE8F5E9),
                            contentColor = Color(0xFF2E7D32)
                        ),
                        modifier = Modifier
                            .height(32.dp)
                            .testTag("approve_button_${registration.id}")
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Approve", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                if (registration.status != "Waitlisted") {
                    FilledTonalButton(
                        onClick = onWaitlist,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = Color(0xFFFFF3E0),
                            contentColor = Color(0xFFEF6C00)
                        ),
                        modifier = Modifier
                            .height(32.dp)
                            .testTag("waitlist_button_${registration.id}")
                    ) {
                        Icon(Icons.Default.HourglassEmpty, contentDescription = null, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Waitlist", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Remove registration",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PostOpportunityFormPanel(
    viewModel: CampusViewModel,
    onDismiss: () -> Unit,
    onSuccess: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Event") } // "Event", "Competition", "Ambassador"
    var organization by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("Main Auditorium") }
    var requirements by remember { mutableStateOf("Laptop, Enrollment Certificate") }
    var contactEmail by remember { mutableStateOf("") }

    val categories = listOf("Event", "Competition", "Ambassador")
    val interestTags = listOf("Coding", "Design", "Business", "Marketing", "Leadership", "Public Speaking", "Writing", "Science")
    val selectedTags = remember { mutableStateListOf<String>() }

    var showError by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    // Prepopulate fields based on user profile
    val userProfile by viewModel.userProfile.collectAsState()
    LaunchedEffect(userProfile) {
        userProfile?.let {
            organization = it.university
            contactEmail = it.email
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Panel Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.safeDrawing)
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Post New Opportunity",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            // Scrollable Form Fields
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scrollState)
                    .padding(horizontal = 20.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    placeholder = { Text("e.g. Android App Development Hackathon") },
                    modifier = Modifier.fillMaxWidth().testTag("post_form_title"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Category Chips
                Text(
                    text = "Category",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categories.forEach { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat) },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("post_form_cat_$cat")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = organization,
                    onValueChange = { organization = it },
                    label = { Text("Hosting Organization") },
                    placeholder = { Text("e.g. Google Developers Club") },
                    modifier = Modifier.fillMaxWidth().testTag("post_form_org"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Full Description") },
                    placeholder = { Text("Provide details about the program timelines, schedules, prizes, etc.") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .testTag("post_form_desc"),
                    maxLines = 5
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Location") },
                    placeholder = { Text("e.g. Engineering Block Hall B, or Virtual") },
                    modifier = Modifier.fillMaxWidth().testTag("post_form_loc"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = requirements,
                    onValueChange = { requirements = it },
                    label = { Text("Prerequisites (Comma separated)") },
                    placeholder = { Text("e.g. Laptop, Python Basics, Undergrad Student") },
                    modifier = Modifier.fillMaxWidth().testTag("post_form_req"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = contactEmail,
                    onValueChange = { contactEmail = it },
                    label = { Text("Contact Email") },
                    modifier = Modifier.fillMaxWidth().testTag("post_form_contact"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Tag Selection
                Text(
                    text = "Target Interests / Tags",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    interestTags.forEach { tag ->
                        val isSelected = selectedTags.contains(tag)
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                if (isSelected) {
                                    selectedTags.remove(tag)
                                } else {
                                    selectedTags.add(tag)
                                }
                            },
                            label = { Text(tag) },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("post_form_tag_$tag")
                        )
                    }
                }

                if (showError) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Please complete all fields (Title, Organization, Description, Contact, and select at least one tag) before publishing.",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = {
                        if (title.isNotBlank() && organization.isNotBlank() && description.isNotBlank() && contactEmail.isNotBlank() && selectedTags.isNotEmpty()) {
                            val now = System.currentTimeMillis()
                            val futureDate = now + (15 * 24 * 60 * 60 * 1000L) // 15 days in future
                            val deadlineDate = now + (10 * 24 * 60 * 60 * 1000L) // 10 days in future

                            viewModel.createOpportunity(
                                title = title,
                                description = description,
                                category = selectedCategory,
                                organization = organization,
                                dateTime = futureDate,
                                location = location,
                                deadline = deadlineDate,
                                contactEmail = contactEmail,
                                requirements = requirements,
                                tags = selectedTags
                            )
                            onSuccess()
                        } else {
                            showError = true
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("publish_opportunity_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Publish Opportunity", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(48.dp))
            }
        }
    }
}
