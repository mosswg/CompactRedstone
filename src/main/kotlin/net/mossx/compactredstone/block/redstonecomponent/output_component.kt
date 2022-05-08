package net.mossx.compactredstone.block.redstonecomponent

import net.mossx.compactredstone.block.CompactRedstoneBlock

class output_component(connected_components: List<or_component>, var block_side: component_side): redstonecomponent(connected_components) {

    fun set_value(value: Int, block: CompactRedstoneBlock) {
        block.set_power(value, this.block_side)
//
//        val executor = Executors.newSingleThreadScheduledExecutor()
//        executor.schedule({
//            if (block.side_values[this.block_side.index] == 0) {
//                block.set_power(15, this.block_side)
//            } else {
//                block.set_power(0, this.block_side)
//            }
//        }, 2, TimeUnit.SECONDS)
    }

    override fun get_value(): Int {
        return 0
    }
}