package com.gec.emergency.feature.safety

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gec.emergency.R
import com.gec.emergency.domain.model.SafetyInformation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SafetyInformationScreen(
    safetyPosts: List<SafetyInformation>,
    onBackClick: () -> Unit,
    onPostClick: (SafetyInformation) -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedCategory by remember { mutableStateOf<String?>(null) }

    val filteredPosts = safetyPosts.filter { post ->
        selectedCategory == null || post.category.equals(selectedCategory, ignoreCase = true)
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Public Safety Guidance") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.content_desc_back),
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            // Category Filter Bar
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    FilterChip(
                        selected = selectedCategory == null,
                        onClick = { selectedCategory = null },
                        label = { Text("All Safety") },
                        leadingIcon = { Icon(Icons.Default.FilterList, contentDescription = null) },
                    )
                }
                item {
                    FilterChip(
                        selected = selectedCategory == "FIRE_SAFETY",
                        onClick = { selectedCategory = if (selectedCategory == "FIRE_SAFETY") null else "FIRE_SAFETY" },
                        label = { Text("Fire Safety") },
                    )
                }
                item {
                    FilterChip(
                        selected = selectedCategory == "ROAD_SAFETY",
                        onClick = { selectedCategory = if (selectedCategory == "ROAD_SAFETY") null else "ROAD_SAFETY" },
                        label = { Text("Road Safety") },
                    )
                }
                item {
                    FilterChip(
                        selected = selectedCategory == "MEDICAL_SAFETY",
                        onClick = { selectedCategory = if (selectedCategory == "MEDICAL_SAFETY") null else "MEDICAL_SAFETY" },
                        label = { Text("Medical") },
                    )
                }
                item {
                    FilterChip(
                        selected = selectedCategory == "FLOOD_SAFETY",
                        onClick = { selectedCategory = if (selectedCategory == "FLOOD_SAFETY") null else "FLOOD_SAFETY" },
                        label = { Text("Flood & Disaster") },
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (filteredPosts.isNotEmpty()) {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredPosts, key = { it.id }) { post ->
                        SafetyPostCard(post = post, onClick = { onPostClick(post) })
                    }
                }
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No safety advisories published for this category.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun SafetyPostCard(
    post: SafetyInformation,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isWarning = post.severity.equals("WARNING", ignoreCase = true) || post.severity.equals("CRITICAL", ignoreCase = true)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isWarning) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (isWarning) Icons.Default.Warning else Icons.Default.HealthAndSafety,
                    contentDescription = null,
                    tint = if (isWarning) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = post.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                AssistChip(
                    onClick = onClick,
                    label = { Text("Issued by: ${post.agencyName}") },
                    colors = AssistChipDefaults.assistChipColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                )

                AssistChip(
                    onClick = onClick,
                    label = { Text(post.severity) },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = if (isWarning) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer
                    )
                )
            }
        }
    }
}
