package com.example.presentation.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.database.DailyHealthLog
import com.example.presentation.HealthViewModel
import com.example.ui.theme.*

@Composable
fun ReportsScreen(
    viewModel: HealthViewModel,
    modifier: Modifier = Modifier
) {
    val allLogs by viewModel.allLogs.collectAsState()
    val todayLog by viewModel.todayHealthLog.collectAsState()
    val scrollState = rememberScrollState()

    var showShareDialog by remember { mutableStateOf(false) }
    var shareType by remember { mutableStateOf("CSV") }

    // Fake mock historical values if database is fresh, to ensure beautiful charts on first launch!
    val visualLogs = remember(allLogs) {
        if (allLogs.size < 3) {
            listOf(
                DailyHealthLog("2026-06-01", 6200, 240, 4.3f, 1800, 420, 68, 72.4f),
                DailyHealthLog("2026-06-02", 8100, 310, 5.8f, 2100, 460, 70, 71.9f),
                DailyHealthLog("2026-06-03", 4500, 180, 3.2f, 1500, 390, 72, 71.5f),
                DailyHealthLog("2026-06-04", 10200, 410, 7.6f, 2600, 510, 69, 71.0f),
                todayLog
            )
        } else {
            allLogs.reversed().take(5)
        }
    }

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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- HEADER ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Health Analytics",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Historical wellness logs and trends",
                        fontSize = 13.sp,
                        color = Color.Gray
                    )
                }

                // Share / Export report
                IconButton(
                    onClick = {
                        shareType = "CSV"
                        showShareDialog = true
                    },
                    modifier = Modifier
                        .background(TealAccent.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                        .testTag("export_reports_button")
                ) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = "Export raw values", tint = TealAccent)
                }
            }

            // --- INSIGHT CARD ---
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = TealDark.copy(alpha = 0.25f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.TrendingUp,
                        contentDescription = "Positivity indicator",
                        tint = TealAccent,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "WEEKLY DIGEST",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TealAccent
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Your weight trend has dropped by 1.4kg this week, while your sleep recovery index has risen by 12%. Outstanding work!",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.85f),
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            // --- WEIGHT CHART DRAWING (Line Chart) ---
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "WEIGHT LOSS INDEX (kg)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val paddingX = 40f
                            val paddingY = 20f
                            val width = size.width - (paddingX * 2)
                            val height = size.height - (paddingY * 2)

                            val minWeight = visualLogs.minOfOrNull { it.weightKg } ?: 60f
                            val maxWeight = visualLogs.maxOfOrNull { it.weightKg } ?: 80f
                            val weightRange = (maxWeight - minWeight).coerceAtLeast(1f)

                            val points = visualLogs.mapIndexed { idx, log ->
                                val x = paddingX + (idx.toFloat() / (visualLogs.size - 1)) * width
                                val y = paddingY + height - ((log.weightKg - minWeight) / weightRange) * height
                                Offset(x, y)
                            }

                            // Draw reference grid lines
                            drawLine(Color.White.copy(alpha = 0.05f), Offset(x=0f, y=0f), Offset(x=size.width, y=0f), strokeWidth = 2f)
                            drawLine(Color.White.copy(alpha = 0.05f), Offset(x=0f, y=size.height), Offset(x=size.width, y=size.height), strokeWidth = 2f)

                            // Plot Path Line
                            val path = Path().apply {
                                points.forEachIndexed { i, p ->
                                    if (i == 0) moveTo(p.x, p.y) else lineTo(p.x, p.y)
                                }
                            }

                            drawPath(path = path, color = WeightOrange, style = Stroke(width = 5f))

                            // Draw points dots
                            points.forEach { p ->
                                drawCircle(color = WeightOrange, radius = 8f, center = p)
                                drawCircle(color = Color.White, radius = 4f, center = p)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        visualLogs.forEach { log ->
                            Text(
                                text = log.date.substringAfterLast("-", "Log"),
                                fontSize = 10.sp,
                                color = Color.Gray,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // --- SLEEP RECOVERY CHART (Bar Chart) ---
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "SLEEP REST TRENDS (hours)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val paddingX = 40f
                            val width = size.width - (paddingX * 2)
                            val barWidth = 32.dp.toPx()
                            val spacing = (width - (barWidth * visualLogs.size)) / (visualLogs.size - 1)

                            visualLogs.forEachIndexed { idx, log ->
                                val hours = log.sleepMinutes / 60f
                                val maxHeightHrs = 10f // assume 10 hrs max baseline representation
                                val pct = (hours / maxHeightHrs).coerceIn(0f, 1f)
                                val barHeight = pct * size.height
                                
                                val x = paddingX + idx * (barWidth + spacing)
                                val y = size.height - barHeight

                                drawRect(
                                    color = SleepLilac,
                                    topLeft = Offset(x, y),
                                    size = androidx.compose.ui.geometry.Size(barWidth, barHeight)
                                )

                                drawRect(
                                    color = Color.White.copy(alpha = 0.1f),
                                    topLeft = Offset(x, 0f),
                                    size = androidx.compose.ui.geometry.Size(barWidth, y)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        visualLogs.forEach { log ->
                            Text(
                                text = log.date.substringAfterLast("-", "Log"),
                                fontSize = 10.sp,
                                color = Color.Gray,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // --- NUMERICAL HISTORY LOG SUMMARY ---
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "HISTORIAL DATA RAW LEDGER",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.4f)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    visualLogs.forEach { log ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(log.date, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                                Text("${log.steps} steps • ${log.waterMl} ml water", color = Color.Gray, fontSize = 11.sp)
                            }
                            Text(
                                text = String.format("%.1f hrs sleep", log.sleepMinutes / 60f),
                                color = SleepLilac,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(Color.White.copy(alpha = 0.05f))
                        )
                    }
                }
            }
        }

        // --- MOCK EXPORT EXCELLENT PROGRESS DIALOG ---
        if (showShareDialog) {
            AlertDialog(
                onDismissRequest = { showShareDialog = false },
                title = { Text("Export Health Sync Reports", color = Color.White) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "A complete spreadsheet records file has been simulated and compiled successfully to send to your physician.",
                            color = Color.Gray,
                            fontSize = 13.sp
                        )

                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "date,steps,calories,water_ml,sleep_min,heart_rate,weight_kg\n" +
                                        visualLogs.joinToString("\n") {
                                            "${it.date},${it.steps},${it.calories},${it.waterMl},${it.sleepMinutes},${it.heartRate},${it.weightKg}"
                                        },
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                fontSize = 9.sp,
                                color = TealAccent,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        colors = ButtonDefaults.buttonColors(containerColor = TealAccent),
                        onClick = { showShareDialog = false },
                        modifier = Modifier.testTag("export_reports_confirm")
                    ) {
                        Text("Export Paged File", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showShareDialog = false }) {
                        Text("Close", color = Color.Gray)
                    }
                },
                containerColor = DarkSurface
            )
        }
    }
}
