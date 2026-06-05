package com.example.presentation.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.presentation.HealthViewModel
import com.example.ui.theme.*

@Composable
fun WaterScreen(
    viewModel: HealthViewModel,
    modifier: Modifier = Modifier
) {
    val todayLog by viewModel.todayHealthLog.collectAsState()
    val profile by viewModel.userProfile.collectAsState()
    val scrollState = rememberScrollState()

    // Liquid fill level animation
    val progress = if (profile.waterGoalMl > 0) {
        (todayLog.waterMl.toFloat() / profile.waterGoalMl.toFloat()).coerceIn(0f, 1f)
    } else 0f

    val fillHeightPercent by animateFloatAsState(
        targetValue = progress,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessVeryLow),
        label = "fillSpring"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .statusBarsPadding()
                .padding(bottom = 80.dp)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- HEADER ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                Column {
                    Text(
                        text = "Hydration, Water Tracker",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Reach your daily hydration target: ${profile.waterGoalMl} ml",
                        fontSize = 13.sp,
                        color = Color.Gray
                    )
                }
            }

            // --- VISUAL HYDRATION WATER GLASS ---
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "VIRTUAL WATER TANK",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.5f),
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Draw Glass
                    Box(
                        modifier = Modifier
                            .width(130.dp)
                            .height(180.dp)
                            .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp, topStart = 6.dp, topEnd = 6.dp))
                            .background(Color.White.copy(alpha = 0.04f))
                            .padding(4.dp),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        // Filling water vector
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight(fillHeightPercent)
                                .clip(RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp, topStart = 2.dp, topEnd = 2.dp))
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            HydrationBlue.copy(alpha = 0.7f),
                                            TealPrimary.copy(alpha = 0.9f)
                                        )
                                    )
                                )
                        )

                        // Glass outer frame gloss overlay
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            // Draw measuring indicators on side
                            val lineCount = 4
                            for (i in 1..lineCount) {
                                val y = (size.height / (lineCount + 1)) * i
                                drawLine(
                                    color = Color.White.copy(alpha = 0.15f),
                                    start = Offset(x = 10f, y = y),
                                    end = Offset(x = 40f, y = y),
                                    strokeWidth = 3f
                                )
                            }
                        }

                        // Status indicators in center of glass
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.align(Alignment.Center)
                        ) {
                            Text(
                                text = "${todayLog.waterMl}",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                style = LocalTextStyle.current.copy(
                                    shadow = androidx.compose.ui.graphics.Shadow(Color.Black, blurRadius = 10f)
                                )
                            )
                            Text(
                                text = "of ${profile.waterGoalMl}ml",
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.8f),
                                style = LocalTextStyle.current.copy(
                                    shadow = androidx.compose.ui.graphics.Shadow(Color.Black, blurRadius = 10f)
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Log Add and Subtract Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { viewModel.logWater(-250) },
                            modifier = Modifier
                                .size(48.dp)
                                .background(Color.White.copy(alpha = 0.05f), CircleShape)
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "Minus water", tint = Color.White)
                        }

                        Text(
                            text = String.format("%.0f%% Logged", progress * 100f),
                            fontWeight = FontWeight.Bold,
                            color = TealAccent,
                            fontSize = 15.sp
                        )

                        IconButton(
                            onClick = { viewModel.logWater(250) },
                            modifier = Modifier
                                .size(48.dp)
                                .background(HydrationBlue.copy(alpha = 0.2f), CircleShape)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Plus water", tint = HydrationBlue)
                        }
                    }
                }
            }

            // --- AI WATER REMINDERS ---
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = TealDark.copy(alpha = 0.25f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "AI Wellness Advisor",
                        tint = TealAccent,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "AI HYDRATION SUGGESTIONS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TealAccent,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (todayLog.steps > 4000) {
                                "Excellent work on those ${todayLog.steps} steps! Due to high active cardio calorie burn, you need 450ml more water today to avoid hydration strain."
                            } else {
                                "Weather check: It is 22°C with light wind. We advise drinking 250ml every 2.5 hours to sustain optimal metabolism levels."
                            },
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.85f),
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            // --- PRESET SELECTION CUP BUTTONS ---
            Text(
                text = "SWEET SPOT QUICK ADD CUPS",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = 0.5f),
                modifier = Modifier.align(Alignment.Start).padding(top = 8.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // 200ml
                PresetCupCard(
                    title = "Teacup",
                    volume = 200,
                    icon = Icons.Default.Coffee,
                    onClick = { viewModel.logWater(200) },
                    modifier = Modifier.weight(1f)
                )
                // 330ml
                PresetCupCard(
                    title = "Standard glass",
                    volume = 330,
                    icon = Icons.Default.LocalDrink,
                    onClick = { viewModel.logWater(330) },
                    modifier = Modifier.weight(1f)
                )
                // 500ml
                PresetCupCard(
                    title = "Sports Bottle",
                    volume = 500,
                    icon = Icons.Default.WaterDrop,
                    onClick = { viewModel.logWater(500) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun PresetCupCard(
    title: String,
    volume: Int,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = HydrationBlue,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Gray,
                maxLines = 1,
                textAlign = TextAlign.Center
            )
            Text(
                text = "$volume ml",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )
        }
    }
}
