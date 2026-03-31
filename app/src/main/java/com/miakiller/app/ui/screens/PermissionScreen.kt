package com.miakiller.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.miakiller.app.model.PermissionInfo
import com.miakiller.app.ui.theme.Danger
import com.miakiller.app.ui.theme.PermissionOrange
import com.miakiller.app.ui.theme.Success
import com.miakiller.app.viewmodel.MainViewModel

@Composable
fun PermissionScreen(viewModel: MainViewModel, onBack: () -> Unit) {
    val permissionApps by viewModel.permissionApps.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(Unit) { viewModel.loadPermissionApps() }

    val filteredApps = permissionApps.filter {
        searchQuery.isBlank() ||
        it.appName.contains(searchQuery, ignoreCase = true) ||
        it.packageName.contains(searchQuery, ignoreCase = true)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("权限管理") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "返回") } }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            OutlinedTextField(
                value = searchQuery, onValueChange = { searchQuery = it },
                placeholder = { Text("搜索应用...") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                singleLine = true, shape = RoundedCornerShape(12.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = PermissionOrange.copy(alpha = 0.1f))
            ) {
                Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Info, null, tint = PermissionOrange, modifier = Modifier.size(18.dp))
                    Text("显示应用申请的危险权限。可以单独撤销不必要的权限。",
                        fontSize = 12.sp, color = PermissionOrange)
                }
            }

            if (isLoading && permissionApps.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("正在扫描应用权限...")
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredApps, key = { it.packageName }) { permInfo ->
                        PermissionAppItem(permInfo) { perm ->
                            viewModel.togglePermission(permInfo.packageName, perm)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PermissionAppItem(
    permInfo: PermissionInfo,
    onTogglePermission: (com.miakiller.app.model.AppPermission) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val grantedCount = permInfo.permissions.count { it.isGranted }

    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp)) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded }.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SafeAppIcon(bitmap = permInfo.iconBitmap, contentDescription = permInfo.appName, size = 36.dp)

                Column(modifier = Modifier.weight(1f)) {
                    Text(permInfo.appName, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                    Text("$grantedCount/${permInfo.permissions.size} 个权限已授予", fontSize = 11.sp,
                        color = if (grantedCount > 3) PermissionOrange
                                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                }

                Icon(if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore, null,
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
            }

            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier.padding(start = 60.dp, end = 12.dp, bottom = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    permInfo.permissions.forEach { perm ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                if (perm.isGranted) Icons.Default.CheckCircle else Icons.Default.Cancel, null,
                                tint = if (perm.isGranted) Success else Color.Gray,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(perm.displayName, fontSize = 13.sp, modifier = Modifier.weight(1f))
                            TextButton(onClick = { onTogglePermission(perm) },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)) {
                                Text(if (perm.isGranted) "撤销" else "授予", fontSize = 11.sp,
                                    color = if (perm.isGranted) Danger else Success)
                            }
                        }
                    }
                }
            }
        }
    }
}
