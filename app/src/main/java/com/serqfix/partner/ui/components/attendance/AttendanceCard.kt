package com.serqfix.partner.ui.components.attendance

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.serqfix.partner.data.api.AttendanceRecord
import com.serqfix.partner.ui.navigation.NavRoute
import com.serqfix.partner.utils.DateTimeUtils
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AttendanceCard(
    todayRecord: AttendanceRecord?,
    onPunchIn: () -> Unit,
    onPunchOut: () -> Unit,
    onViewDetails: () -> Unit,
    loading: Boolean = false,
    navController: NavController
) {
    val statusLabel = when (todayRecord?.status) {
        "present" -> "Present"
        "half-day" -> "Half Day"
        "leave" -> "On Leave"
        "absent" -> "Absent"
        else -> "Not Marked"
    }
    
    val hasLogin = todayRecord?.login_time != null
    val hasLogout = todayRecord?.logout_time != null
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5f.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(3.dp, Color(0xFF1976D2), RoundedCornerShape(8.dp))
                .padding(horizontal = 10.dp, vertical = 8.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = null,
                        tint = Color(0xFF1976D2),
                        modifier = Modifier.size(20.dp)
                    )
                    Column {
                        Text(
                            text = "Today Attendance",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color(0xFF212121)
                        )
                        Text(
                            text = statusLabel,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            color = Color(0xFF616161)
                        )
                    }
                }
                
                // Hours Badge
                if (todayRecord?.total_hours != null) {
                    Row(
                        modifier = Modifier
                            .background(Color(0xFFE8F5E9), RoundedCornerShape(12.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = null,
                            tint = Color(0xFF2E7D32),
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "${String.format("%.2f", todayRecord.total_hours)} hrs",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF2E7D32)
                            )
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(6.dp))
            
            // Info Row - In/Out times
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "In",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.sp,
                            color = Color(0xFF757575)
                        )
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (hasLogin) {
                            DateTimeUtils.formatToLocaleTime(todayRecord?.login_time)
                        } else {
                            "--"
                        },
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF212121)
                        )
                    )
                }
                
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(30.dp)
                        .background(Color(0xFFE0E0E0))
                )
                
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Out",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.sp,
                            color = Color(0xFF757575)
                        )
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (hasLogout) {
                            DateTimeUtils.formatToLocaleTime(todayRecord?.logout_time)
                        } else {
                            "--"
                        },
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF212121)
                        )
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Actions Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Punch In Button
                Button(
                    onClick = onPunchIn,
                    enabled = !loading && !hasLogin,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (hasLogin || loading) Color(0xFFECEFF1) else Color(0xFFE3F2FD),
                        disabledContainerColor = Color(0xFFECEFF1)
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        width = 1.dp,
                        color = if (hasLogin || loading) Color(0xFFCFD8DC) else Color(0xFF1976D2)
                    ),
                    shape = RoundedCornerShape(999.dp),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = if (hasLogin || loading) 0.dp else 1.dp
                    )
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (hasLogin) Icons.Default.CheckCircle else Icons.Default.ArrowForward,
                            contentDescription = null,
                            tint = if (hasLogin || loading) Color(0xFFB0BEC5) else Color(0xFF1976D2),
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = if (hasLogin) "Punched In" else "Punch In",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.3.sp,
                                color = if (hasLogin || loading) Color(0xFFB0BEC5) else Color(0xFF1976D2)
                            )
                        )
                    }
                }
                
                // Punch Out Button
                Button(
                    onClick = onPunchOut,
                    enabled = !loading && hasLogin && !hasLogout,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (!hasLogin || hasLogout || loading) Color(0xFFECEFF1) else Color(0xFFE3F2FD),
                        disabledContainerColor = Color(0xFFECEFF1)
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        width = 1.dp,
                        color = if (!hasLogin || hasLogout || loading) Color(0xFFCFD8DC) else Color(0xFF1976D2)
                    ),
                    shape = RoundedCornerShape(999.dp),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = if (!hasLogin || hasLogout || loading) 0.dp else 1.dp
                    )
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (hasLogout) Icons.Default.CheckCircle else Icons.Default.ArrowBack,
                            contentDescription = null,
                            tint = if (!hasLogin || hasLogout || loading) Color(0xFFB0BEC5) else Color(0xFF1976D2),
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = if (hasLogout) "Punched Out" else "Punch Out",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.3.sp,
                                color = if (!hasLogin || hasLogout || loading) Color(0xFFB0BEC5) else Color(0xFF1976D2)
                            )
                        )
                    }
                }
                
                // Details Link
                TextButton(
                    onClick = {
                        navController.navigate(NavRoute.AttendanceDetails.route)
                    },
                    enabled = !loading
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Details",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF1976D2)
                            )
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = null,
                            tint = Color(0xFF1976D2),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}
