package net.mossx.compactredstone.block.redstonecomponent

class blockcomponent(connected_components: List<redstonecomponent>) : redstonecomponent(connected_components) {
    override fun get_value(): Int {
        return 0
    }

}