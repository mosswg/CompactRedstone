package net.mossx.compactredstone.block.redstonecomponent

import kotlin.math.min

class and_component(connected_components: List<redstonecomponent>) : redstonecomponent(connected_components) {
    override fun get_value(): Int {
        if (connected_components.size == 2) {
            return min(connected_components[0].get_value(), connected_components[1].get_value())
        }
        else {
            var out = 15
            for (component in connected_components) {
                out = min(out, component.get_value())
            }
            return out
        }
    }
}