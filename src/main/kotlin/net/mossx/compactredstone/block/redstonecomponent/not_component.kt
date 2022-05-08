package net.mossx.compactredstone.block.redstonecomponent

import net.mossx.compactredstone.CompactRedstone
import kotlin.math.min

class not_component(connected_components: List<redstonecomponent>) : redstonecomponent(connected_components) {
    override fun get_value(): Int {
        CompactRedstone.LOGGER.info("NOT: ${connected_components[0].get_value()}")
        return if (connected_components[0].get_value() == 0) 15 else 0
    }
}