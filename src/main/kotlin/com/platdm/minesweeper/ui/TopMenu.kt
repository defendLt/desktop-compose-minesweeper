import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.platdm.minesweeper.ui.MineSweeperStyles

@Composable
internal fun TopMenu(
    minesCount: String,
    statusName: String,
    timerValue: String
){
    Row(
        modifier = Modifier
            .padding(bottom = MineSweeperStyles.windowPaddingSize)
            .width(MineSweeperStyles.topMenuWidthSize)
            .padding(start = MineSweeperStyles.windowPaddingSize, end = MineSweeperStyles.windowPaddingSize),
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            modifier = Modifier.weight(0.5f),
            textAlign = TextAlign.Start,
            text = minesCount
        )
        Text(
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center,
            text = statusName
        )
        Text(
            modifier = Modifier.weight(0.5f),
            textAlign = TextAlign.End,
            text = timerValue
        )
    }
}