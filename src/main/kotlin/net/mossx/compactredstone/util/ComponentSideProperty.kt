package net.mossx.compactredstone.util

import com.google.common.collect.Lists
import net.minecraft.state.property.EnumProperty
import net.mossx.compactredstone.block.redstonecomponent.component_side
import java.util.*
import java.util.function.Predicate
import java.util.stream.Collectors

/**
 * Represents a property that has direction values.
 *
 *
 * See [net.minecraft.state.property.Properties] for example
 * usages.
 */
class ComponentSideProperty : EnumProperty<component_side> {

    protected constructor(name: String?, values: Collection<component_side?>) : super(name, component_side::class.java, values)

    companion object {
        /**
         * Creates a direction property with all directions as values.
         *
         * @param name the name of the property; see [the note on the][Property.name]
         */
        fun of(name: String): ComponentSideProperty {
            return of(
                name
            ) { direction: component_side? -> true }
        }

        /**
         * Creates a direction property with the values allowed by the given
         * filter out of all 6 directions.
         *
         * @see .of
         * @param filter the filter which specifies if a value is allowed; required to allow
         * 2 or more values
         * @param name the name of the property; see [the note on the][Property.name]
         */
        fun of(name: String?, filter: Predicate<component_side?>?): ComponentSideProperty {
            return of(name, Arrays.stream(component_side.values()).filter(filter).collect(Collectors.toList()))
        }

        /**
         * Creates a direction property with the given values.
         *
         * @see .of
         * @param values the values the property contains; required to have 2 or more values
         * @param name the name of the property; see [the note on the][Property.name]
         */
        fun of(name: String?, vararg values: component_side?): ComponentSideProperty {
            return of(name, Lists.newArrayList(*values))
        }

        /**
         * Creates a direction property with the given values.
         *
         * @see .of
         * @param values the values the property contains; required to have 2 or more values
         * @param name the name of the property; see [the note on the][Property.name]
         */
        fun of(name: String?, values: Collection<component_side?>): ComponentSideProperty {
            return ComponentSideProperty(name, values)
        }
    }
}