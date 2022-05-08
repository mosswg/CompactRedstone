package net.mossx.compactredstone.block.redstonecomponent

class compact_redstone_component(var logic: circuit, connected_components: List<redstonecomponent>) : redstonecomponent(connected_components) {

    override fun get_value(): Int {
        val front_power = connected_components[component_side.front.index].get_value()
        val right_power = connected_components[component_side.right.index].get_value()
        val left_power = connected_components[component_side.left.index].get_value()
        val back_power = connected_components[component_side.back.index].get_value()
        return logic.execute(arrayOf(front_power, right_power, back_power, left_power))
    }
}