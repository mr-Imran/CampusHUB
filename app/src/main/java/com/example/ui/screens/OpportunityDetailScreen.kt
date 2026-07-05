package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Opportunity
import com.example.ui.CampusViewModel
import com.example.ui.components.AdBanner
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OpportunityDetailScreen(
    opportunityId: Int,
    viewModel: CampusViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val allOps by viewModel.allOpportunities.collectAsState()
    val opportunity = allOps.find { it.id == opportunityId }

    val userProfile by viewModel.userProfile.collectAsState()
    val myRegistrations by viewModel.studentRegistrations.collectAsState()

    val isRegistered = myRegistrations.any { it.opportunityId == opportunityId }
    val registrationDetails = myRegistrations.find { it.opportunityId == opportunityId }

    var showRegisterDialog by remember { mutableStateOf(false) }
    var showSuccessOverlay by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    val formatter = remember { SimpleDateFormat("EEEE, MMM dd, yyyy 'at' hh:mm a", Locale.getDefault()) }
    val deadlineFormatter = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }

    if (opportunity == null) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Opportunity details not found.")
        }
        return
    }

    val formattedDate = formatter.format(Date(opportunity.dateTime))
    val formattedDeadline = deadlineFormatter.format(Date(opportunity.deadline))

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.safeDrawing)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier.testTag("detail_back_button")
                ) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Go Back")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Opportunity Details",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            // Scrollable Details
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scrollState)
                    .padding(horizontal = 20.dp)
            ) {
                // Category & Org
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CategoryBadge(category = opportunity.category)
                    Text(
                        text = opportunity.organization,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Title
                Text(
                    text = opportunity.title,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = (-0.5).sp,
                    color = MaterialTheme.colorScheme.onBackground,
                    lineHeight = 30.sp,
                    modifier = Modifier.testTag("detail_title")
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Logistics Cards
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        LogisticsRow(icon = Icons.Default.CalendarToday, title = "Date & Time", detail = formattedDate)
                        LogisticsRow(icon = Icons.Default.LocationOn, title = "Location", detail = opportunity.location)
                        LogisticsRow(
                            icon = Icons.Default.Alarm,
                            title = "Registration Deadline",
                            detail = formattedDeadline,
                            highlightColor = MaterialTheme.colorScheme.error
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Description
                Text(
                    text = "DESCRIPTION",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = opportunity.description,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Requirements
                Text(
                    text = "PREREQUISITES & REQUIREMENTS",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                opportunity.requirements.split(",").forEach { req ->
                    if (req.isNotBlank()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .size(16.dp)
                                    .offset(y = 2.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = req.trim(),
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Contact Email
                Text(
                    text = "CONTACT INFORMATION",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Email, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = opportunity.contactEmail,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onBackground,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Tags
                Text(
                    text = "TAGS",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    opportunity.tags.split(",").map { it.trim() }.forEach { tag ->
                        if (tag.isNotBlank()) {
                            SuggestionChip(
                                onClick = {},
                                label = { Text(tag) }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }

            // Bottom CTA Area
            Surface(
                tonalElevation = 8.dp,
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(16.dp)
                ) {
                    if (isRegistered) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.TaskAlt,
                                    contentDescription = null,
                                    tint = Color(0xFF2E7D32)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "You are Registered!",
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF2E7D32),
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = "Status: ${registrationDetails?.status ?: "Registered"}",
                                        fontSize = 12.sp,
                                        color = Color(0xFF1B5E20)
                                    )
                                }
                            }
                        }
                    } else {
                        Button(
                            onClick = { showRegisterDialog = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("register_now_button"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "Register Seamlessly",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
            AdBanner()
        }

        // Seamless Pre-filled Registration Dialog
        if (showRegisterDialog) {
            var studentName by remember { mutableStateOf(userProfile?.name ?: "") }
            var studentEmail by remember { mutableStateOf(userProfile?.email ?: "") }
            var studentUni by remember { mutableStateOf(userProfile?.university ?: "") }
            var studentMajor by remember { mutableStateOf(userProfile?.major ?: "") }
            var gradYear by remember { mutableStateOf("2027") }

            AlertDialog(
                onDismissRequest = { showRegisterDialog = false },
                title = { Text("Confirm Registration") },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Registering for \"${opportunity.title}\" by ${opportunity.organization}. Your profile details have been automatically filled.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        OutlinedTextField(
                            value = studentName,
                            onValueChange = { studentName = it },
                            label = { Text("Name") },
                            modifier = Modifier.fillMaxWidth().testTag("reg_form_name"),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = studentEmail,
                            onValueChange = { studentEmail = it },
                            label = { Text("Email") },
                            modifier = Modifier.fillMaxWidth().testTag("reg_form_email"),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = studentUni,
                            onValueChange = { studentUni = it },
                            label = { Text("University") },
                            modifier = Modifier.fillMaxWidth().testTag("reg_form_uni"),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = studentMajor,
                            onValueChange = { studentMajor = it },
                            label = { Text("Major") },
                            modifier = Modifier.fillMaxWidth().testTag("reg_form_major"),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = gradYear,
                            onValueChange = { gradYear = it },
                            label = { Text("Expected Graduation Year") },
                            modifier = Modifier.fillMaxWidth().testTag("reg_form_grad_year"),
                            singleLine = true
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (studentName.isNotBlank() && studentEmail.isNotBlank()) {
                                viewModel.registerForOpportunity(
                                    opportunity = opportunity,
                                    name = studentName,
                                    email = studentEmail,
                                    university = studentUni,
                                    major = studentMajor,
                                    gradYear = gradYear
                                )
                                showRegisterDialog = false
                                showSuccessOverlay = true
                            }
                        },
                        modifier = Modifier.testTag("submit_registration_form")
                    ) {
                        Text("Register")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showRegisterDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Custom Visual Checkmark Success Feedback Overlay
        AnimatedVisibility(
            visible = showSuccessOverlay,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            var animateCheck by remember { mutableStateOf(false) }
            LaunchedEffect(showSuccessOverlay) {
                if (showSuccessOverlay) {
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
                    .clickable { showSuccessOverlay = false },
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
                        tint = Color(0xFF4CAF50),
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
                            text = "Successfully Registered! 🎉",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "You're all set! Organizers have been notified. An in-app receipt has been logged.",
                            fontSize = 14.sp,
                            color = Color.LightGray,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        Spacer(modifier = Modifier.height(32.dp))
                        Button(
                            onClick = { showSuccessOverlay = false },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
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
fun LogisticsRow(
    icon: ImageVector,
    title: String,
    detail: String,
    highlightColor: Color = Color.Unspecified
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (highlightColor != Color.Unspecified) highlightColor else MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = title,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                fontWeight = FontWeight.Bold
            )
            Text(
                text = detail,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (highlightColor != Color.Unspecified) highlightColor else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
