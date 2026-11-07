package com.gec.emergency.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gec.emergency.navigation.Routes

enum class BottomNavItem(
    val route: String,
    val title: String,
    val icon: ImageVector,
) {
    HOME(Routes.HOME, "Home", Icons.Default.Home),
    NOTIFICATIONS(Routes.NOTIFICATIONS, "Notifications", Icons.Default.Notifications),
    PROFILE(Routes.ACCOUNT, "Profile", Icons.Default.Person),
    SETTINGS(Routes.SETTINGS, "Settings", Icons.Default.Settings);
}

/**
 * Premium iOS-inspired Liquid Glass floating bottom dock for Public Android navigation.
 *
 * Employs a layered architecture separating the frosted translucent glass background
 * from the crisp, un-blurred foreground icons, text labels, and selection indicators.
 */
@Composable
fun LiquidGlassBottomNavigation(
    currentRoute: String?,
    unreadNotificationCount: Long,
    onNavItemSelect: (BottomNavItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(horizontal = 18.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        val glassBorderBrush = Brush.verticalGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.50f),
                Color.White.copy(alpha = 0.18f),
                Color.White.copy(alpha = 0.30f)
            )
        )

        val glassSpecularBrush = Brush.verticalGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.28f),
                Color.White.copy(alpha = 0.08f),
                Color.Transparent
            )
        )

        // 1. BACKGROUND GLASS LAYER (Shadow, Translucent Surface, Top Specular Line, Glass Border)
        Surface(
            modifier = Modifier
                .matchParentSize()
                .shadow(
                    elevation = 20.dp,
                    shape = RoundedCornerShape(32.dp),
                    ambientColor = Color.Black.copy(alpha = 0.45f),
                    spotColor = Color.Black.copy(alpha = 0.55f)
                ),
            shape = RoundedCornerShape(32.dp),
            color = Color(0x7A091A2C), // Translucent frosted glass tint
            border = BorderStroke(1.2.dp, glassBorderBrush)
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                // Top Specular Glass Reflection Line (clipped strictly to capsule top corners)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(24.dp)
                        .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                        .background(glassSpecularBrush)
                )
            }
        }

        // 2. FOREGROUND CONTENT LAYER (Icons, Labels, Badges, Active Pill) - 100% CRISP & SHARP
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomNavItem.entries.forEach { item ->
                val selected = currentRoute == item.route

                val activeBgColor by animateColorAsState(
                    targetValue = if (selected) Color(0xE60066CC) else Color.Transparent,
                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                    label = "activeTabBg"
                )

                val activeContentColor by animateColorAsState(
                    targetValue = if (selected) Color.White else Color.White.copy(alpha = 0.90f),
                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                    label = "activeTabContent"
                )

                val interactionSource = remember { MutableInteractionSource() }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(24.dp))
                        .background(activeBgColor)
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null
                        ) {
                            onNavItemSelect(item)
                        }
                        .padding(vertical = 8.dp, horizontal = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        if (item == BottomNavItem.NOTIFICATIONS && unreadNotificationCount > 0) {
                            BadgedBox(
                                badge = {
                                    Badge(
                                        containerColor = Color(0xFFE53935),
                                        contentColor = Color.White
                                    ) {
                                        Text(
                                            text = if (unreadNotificationCount > 99) "99+" else unreadNotificationCount.toString(),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.title,
                                    tint = activeContentColor,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        } else {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.title,
                                tint = activeContentColor,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(3.dp))

                        Text(
                            text = item.title,
                            color = activeContentColor,
                            fontSize = 11.sp,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}
