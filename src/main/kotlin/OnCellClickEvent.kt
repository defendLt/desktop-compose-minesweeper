internal sealed interface OnCellClickEvent {
    @JvmInline
    value class OnSingleClick(val index: Int) : OnCellClickEvent

    @JvmInline
    value class OnLongClick(val index: Int) : OnCellClickEvent

    @JvmInline
    value class OnDoubleClick(val index: Int) : OnCellClickEvent
}