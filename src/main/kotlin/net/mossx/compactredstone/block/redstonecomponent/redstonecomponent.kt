package net.mossx.compactredstone.block.redstonecomponent

import net.minecraft.util.StringIdentifiable
import net.mossx.compactredstone.CompactRedstone

enum class component_side(var index: Int): StringIdentifiable {
    none(-1) {
        override fun asString(): String {
            return "none"
        }
    },
    front(0) {
        override fun asString(): String {
            return "front"
        }
    },
    right(1) {
        override fun asString(): String {
            return "right"
        }
    },
    back(2) {
        override fun asString(): String {
            return "back"
        }
    },
    left(3) {
        override fun asString(): String {
            return "left"
        }
    };

    fun rotate(): component_side {
        if (this.index == 3) {
            return front
        }
        return values()[this.index + 2]
    }
}

abstract class redstonecomponent(var connected_components: List<redstonecomponent>) {
    abstract fun get_value(): Int
}