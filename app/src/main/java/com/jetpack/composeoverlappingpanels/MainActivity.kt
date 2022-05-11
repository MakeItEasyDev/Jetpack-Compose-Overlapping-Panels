package com.jetpack.composeoverlappingpanels

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jetpack.composeoverlappingpanels.ui.theme.ComposeOverlappingPanelsTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ComposeOverlappingPanelsTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    Scaffold(
                        topBar = {
                            TopAppBar(
                                title = {
                                    Text(
                                        text = "Compose Overlapping Panels",
                                        modifier = Modifier.fillMaxWidth(),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            )
                        }
                    ) {
                        ComposeOverlappingPanels()
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ComposeOverlappingPanels() {
    val panelState = rememberOverlappingPanelsState()
    val coroutineScope = rememberCoroutineScope()
    val gesturesEnabled by remember { mutableStateOf(true) }

    OverlappingPanels(
        modifier = Modifier.fillMaxSize(),
        panelsState = panelState,
        gesturesEnabled = gesturesEnabled,
        panelStart = {
            PanelSurface {
                PanelColumn {
                    PanelHeaderText(
                        text = stringResource(id = R.string.start_panel_name),
                        modifier = Modifier.align(CenterHorizontally)
                    )
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                panelState.closePanels()
                            }
                        }
                    ) {
                        Text(
                            text = stringResource(id = R.string.close_panel_button_text)
                        )
                    }
                }
            }
        },
        panelCenter = {
            PanelSurface {
                PanelColumn {
                    PanelHeaderText(
                        text = stringResource(id = R.string.center_panel_name),
                        modifier = Modifier.align(CenterHorizontally)
                    )
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                panelState.openStartPanel()
                            }
                        }
                    ) {
                        Text(
                            text = stringResource(id = R.string.open_start_panel_button_text)
                        )
                    }
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                panelState.openEndPanel()
                            }
                        }
                    ) {
                        Text(text = stringResource(R.string.open_end_panel_button_text))
                    }
                }
            }
        },
        panelEnd = {
            PanelSurface {
                PanelColumn {
                    PanelHeaderText(
                        text = stringResource(id = R.string.end_panel_name),
                        modifier = Modifier.align(CenterHorizontally)
                    )
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                panelState.closePanels()
                            }
                        }
                    ) {
                        Text(
                            text = stringResource(id = R.string.close_panel_button_text)
                        )
                    }
                }
            }
        }
    )
}























