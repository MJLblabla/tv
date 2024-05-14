import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import ennity.DetectorType
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource
import tvboxassistant.composeapp.generated.resources.Res
import tvboxassistant.composeapp.generated.resources.ic_xiala


@OptIn(ExperimentalResourceApi::class)
@Composable
fun DetectTypeDropdownMenu(detectorType: DetectorType, onSelect: (detectorType: DetectorType) -> Unit) {
    val options = listOf(DetectorType.LAN, DetectorType.USB, DetectorType.IP)
    val expanded = remember { mutableStateOf(false) }

    Column {
        DropdownMenu(
            expanded = expanded.value,
            onDismissRequest = { expanded.value = false }
        ) {
            options.forEachIndexed { index, option ->
                DropdownMenuItem(onClick = {
                    onSelect(option)
                    expanded.value = false
                }) {
                    Text(option.str)
                }
            }
        }

        RoundedCornerBorderBackground(modifier = Modifier.width(300.dp).clickable {
            expanded.value = true
        }) {
            Row(
                modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(detectorType.str, color = Color.White)
                Image(painter = painterResource(Res.drawable.ic_xiala), "")

            }
        }

    }
}

@Composable
fun RoundedCornerBorderBackground(modifier: Modifier, content: @Composable BoxScope.() -> Unit) {
    Box(
        modifier.background(
            color = Color(0x55FFFFFF),
            shape = RoundedCornerShape(16.dp) // 设置圆角半径为 16dp
        )
            .border(
                width = 1.dp,
                color = Color.White,
                shape = RoundedCornerShape(16.dp) // 设置边框圆角半径为 16dp，与背景相同
            ).padding(8.dp), content = content
    )
}
