package net.mossx.compactredstone.block.redstonecomponent

class input_component(var block_side: component_side): redstonecomponent(listOf()) {
    var value: Int = 0

    override fun get_value(): Int {
        return value
    }
}