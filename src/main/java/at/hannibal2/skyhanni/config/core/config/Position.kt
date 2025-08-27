/*
 * Copyright (C) 2022 NotEnoughUpdates contributors
 *
 * This file is part of NotEnoughUpdates.
 *
 * This file was translated to Kotlin and modified, 2024.
 *
 * NotEnoughUpdates is free software: you can redistribute it
 * and/or modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * NotEnoughUpdates is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with NotEnoughUpdates. If not, see <https://www.gnu.org/licenses/>.
 */
package at.hannibal2.skyhanni.config.core.config

import at.hannibal2.skyhanni.SkyHanniMod
import at.hannibal2.skyhanni.config.ConfigGuiManager.getEditorInstance
import at.hannibal2.skyhanni.config.core.config.Position.BorderState.BOTH
import at.hannibal2.skyhanni.config.core.config.Position.BorderState.NONE
import at.hannibal2.skyhanni.config.core.config.Position.BorderState.ONE
import at.hannibal2.skyhanni.config.core.config.Position.BorderState.TWO
import at.hannibal2.skyhanni.data.GuiEditManager.getDummySize
import at.hannibal2.skyhanni.test.command.ErrorManager
import at.hannibal2.skyhanni.utils.ColorUtils
import at.hannibal2.skyhanni.utils.GuiRenderUtils
import at.hannibal2.skyhanni.utils.GuiRenderUtils.RectBox
import at.hannibal2.skyhanni.utils.compat.GuiScreenUtils
import com.google.gson.JsonElement
import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.annotations.ConfigLink
import io.github.notenoughupdates.moulconfig.gui.GuiScreenElementWrapper
import java.awt.Color
import java.lang.reflect.Field

class Position @JvmOverloads constructor(
    x: Int,
    y: Int,
    scale: Float = DEFAULT_SCALE,
    centerX: Boolean = false,
    centerY: Boolean = true,
) {
    @JvmOverloads
    constructor(
        x: Int,
        y: Int,
        centerX: Boolean,
        centerY: Boolean = true,
    ) : this(x, y, DEFAULT_SCALE, centerX, centerY)

    constructor() : this(0, 0)

    @Expose
    var x: Int = x
        private set

    @Expose
    var y: Int = y
        private set

    @Expose
    var scale: Float = scale
        get() = if (field <= 0f) DEFAULT_SCALE else field

    @Expose
    var centerX: Boolean = centerX
        private set

    // Note: currently unused?
    @Expose
    var centerY: Boolean = centerY
        private set

    @Expose
    private var ignoreCustomScale = false

    @Transient
    var linkField: Field? = null
        private set

    var clicked: Boolean = false
    var internalName: String? = null
        private set

    val effectiveScale: Float
        get() = if (ignoreCustomScale) DEFAULT_SCALE else (scale * SkyHanniMod.feature.gui.globalScale).coerceIn(MIN_SCALE, MAX_SCALE)

    fun set(other: Position): Position {
        this.x = other.x
        this.y = other.y
        this.centerX = other.centerX
        this.centerY = other.centerY
        this.scale = other.scale
        return this
    }

    fun getOrSetInternalName(lazy: () -> String): String {
        return internalName ?: lazy().also { internalName = it }
    }

    fun moveTo(x: Int, y: Int): Position {
        this.x = x
        this.y = y
        return this
    }

    fun getAbsX0(objWidth: Int): Int {
        val width = GuiScreenUtils.scaledWindowWidth

        return calcAbs0(x, horizontalState, centerXSize, width, objWidth)
    }

    fun getAbsY0(objHeight: Int): Int {
        val height = GuiScreenUtils.scaledWindowHeight

        return calcAbs0(y, verticalState, centerYSize, height, objHeight)
    }

    private fun calcAbs0(axis: Int, borderState: BorderState, centerSize: Int?, length: Int, objLength: Int): Int {
        var ret = when (borderState) {
            NONE -> axis - objLength / 2
            ONE -> axis
            BOTH -> axis - centerSize!! / 2
            TWO -> axis - objLength
        }
        // TODO
        if (axis < 0) {
            ret = length + axis - objLength
        }

        if (ret < 0) ret = 0
        if (ret > length - objLength) ret = length - objLength

        return ret
    }

    fun moveX(deltaX: Int, objWidth: Int): Int {
        var newDeltaX = deltaX
        val screenWidth = GuiScreenUtils.scaledWindowWidth
        val wasPositiveX = x >= 0
        this.x += newDeltaX

        if (wasPositiveX) {
            if (x < 0) {
                newDeltaX -= x
                this.x = 0
            } else if (x > screenWidth) {
                newDeltaX += screenWidth - x
                this.x = screenWidth
            }
        } else {
            if (x + 1 > 0) {
                newDeltaX += -1 - x
                this.x = -1
            } else if (x + screenWidth < 0) {
                newDeltaX += -screenWidth - x
                this.x = -screenWidth
            }
        }

        if (x >= 0 && x + objWidth / 2 > screenWidth / 2) {
            this.x -= screenWidth - objWidth
        } else if (x < 0 && x + objWidth / 2 <= -screenWidth / 2) {
            x += screenWidth - objWidth
        }
        return newDeltaX
    }

    fun moveY(deltaY: Int, objHeight: Int): Int {
        var newDeltaY = deltaY
        val screenHeight = GuiScreenUtils.scaledWindowHeight
        val wasPositiveY = y >= 0
        this.y += newDeltaY

        if (wasPositiveY) {
            if (y < 0) {
                newDeltaY -= y
                this.y = 0
            } else if (y > screenHeight) {
                newDeltaY += screenHeight - y
                this.y = screenHeight
            }
        } else {
            if (y + 1 > -0) {
                newDeltaY += -1 - y
                this.y = -1
            } else if (y + screenHeight < 0) {
                newDeltaY += -screenHeight - y
                this.y = -screenHeight
            }
        }

        if (y >= 0 && y - objHeight / 2 > screenHeight / 2) {
            this.y -= screenHeight - objHeight
        } else if (y < 0 && y - objHeight / 2 <= -screenHeight / 2) {
            this.y += screenHeight - objHeight
        }
        return newDeltaY
    }

    fun ignoreScale(value: Boolean = true): Position {
        this.ignoreCustomScale = value
        return this
    }

    fun canJumpToConfigOptions(): Boolean {
        val field = linkField ?: return false
        return getEditorInstance().getOptionFromField(field) != null
    }

    fun jumpToConfigOptions() {
        val editor = getEditorInstance()
        val field = linkField ?: return
        val option = editor.getOptionFromField(field) ?: return
        editor.search("")
        if (!editor.goToOption(option)) return
        SkyHanniMod.screenToOpen = GuiScreenElementWrapper(editor)
    }

    fun setLink(configLink: ConfigLink) {
        try {
            linkField = configLink.owner.java.getDeclaredField(configLink.field)
        } catch (e: NoSuchFieldException) {
            ErrorManager.logErrorWithData(
                FieldNotFoundException(configLink.field, configLink.owner.java),
                "Failed to set ConfigLink for ${configLink.field} in ${configLink.owner}",
                "owner" to configLink.owner,
                "field" to configLink.field,
            )
            ErrorManager.crashInDevEnv("Couldn't set config links") { e }
        }
    }

    @Expose
    var centerXSize: Int? = null

    @Expose
    var centerYSize: Int? = null

    @Expose
    var horizontalState: BorderState = ONE

    @Expose
    var verticalState: BorderState = ONE

    enum class Border {
        TOP {
            override fun getBox(x: Int, y: Int, width: Int, height: Int, border: Int) = RectBox(
                left = x,
                top = y - border,
                right = x + width,
                bottom = y,
            )

            override fun getEnabled(postion: Position) = when (postion.verticalState) {
                ONE, BOTH -> true
                else -> false
            }
        },
        LEFT {
            override fun getBox(x: Int, y: Int, width: Int, height: Int, border: Int) = RectBox(
                left = x - border,
                top = y - border,
                right = x,
                bottom = y + height + border,
            )

            override fun getEnabled(postion: Position) = when (postion.horizontalState) {
                ONE, BOTH -> true
                else -> false
            }
        },
        RIGHT {
            override fun getBox(x: Int, y: Int, width: Int, height: Int, border: Int) = RectBox(
                left = x + width,
                top = y - border,
                right = x + width + border,
                bottom = y + height + border,
            )

            override fun getEnabled(postion: Position) = when (postion.horizontalState) {
                TWO, BOTH -> true
                else -> false
            }
        },
        BOTTOM {
            override fun getBox(x: Int, y: Int, width: Int, height: Int, border: Int) = RectBox(
                left = x,
                top = y + height,
                right = x + width,
                bottom = y + height + border,
            )

            override fun getEnabled(postion: Position) = when (postion.verticalState) {
                TWO, BOTH -> true
                else -> false
            }
        },

        ;

        abstract fun getBox(x: Int, y: Int, width: Int, height: Int, border: Int): RectBox
        abstract fun getEnabled(postion: Position): Boolean

        fun isHovered(mouseX: Int, mouseY: Int, x: Int, y: Int, width: Int, height: Int, border: Int): Boolean =
            isHovered(
                mouseX = mouseX,
                mouseY = mouseY,
                box = getBox(
                    x = x,
                    y = y,
                    width = width,
                    height = height,
                    border = border,
                ),
            )

        fun isHovered(mouseX: Int, mouseY: Int, box: RectBox): Boolean =
            GuiRenderUtils.isPointInRect(
                x = mouseX,
                y = mouseY,
                left = box.left,
                top = box.top,
                width = box.right - box.left,
                height = box.bottom - box.top,
            )

        fun drawRect(
            mouseX: Int,
            mouseY: Int,
            x: Int,
            y: Int,
            width: Int,
            height: Int,
            border: Int,
            postion: Position,
            hoveredColor: Color,
            enabledColor: Color,
        ) {
            val box = getBox(
                x = x,
                y = y,
                width = width,
                height = height,
                border = border,
            )
            val color = if (isHovered(mouseX, mouseY, box)) {
                if (getEnabled(postion)) {
                    ColorUtils.alphaBlend(hoveredColor, enabledColor)
                } else hoveredColor
            } else if (getEnabled(postion)) {
                enabledColor
            } else {
                return
            }
            GuiRenderUtils.drawRect(
                left = box.left,
                top = box.top,
                right = box.right,
                bottom = box.bottom,
                color = color.rgb,
            )
        }
    }

    /**
     * [NONE] Saves the cord middle
     * [ONE] Saves the "normal" coordinate
     * [BOTH] Saves the cord middle + the size
     * [TWO] Saves the flipped "normal" coordinate (so "normal" + the size at the time)
     */
    enum class BorderState {
        NONE,

        /**
         * LEFT/TOP
         */
        ONE,
        BOTH,

        /**
         * RIGHT/BOTTOM
         */
        TWO
    }

    fun toggleBorder(border: Border) = when (border) {
        Border.TOP -> when (verticalState) {
            NONE -> {
                y -= getDummySize().y / 2
                verticalState = ONE
            }

            ONE -> {
                y += getDummySize().y / 2
                verticalState = NONE
            }

            BOTH -> {
                centerYSize = null
                y += getDummySize().y / 2
                verticalState = TWO
            }

            TWO -> {
                val ySize = getDummySize().y
                centerYSize = ySize
                y -= ySize / 2
                verticalState = BOTH
            }
        }

        Border.BOTTOM -> when (verticalState) {
            NONE -> {
                y += getDummySize().y / 2
                verticalState = TWO
            }

            ONE -> {
                val ySize = getDummySize().y
                centerYSize = ySize
                y += ySize / 2
                verticalState = BOTH
            }

            BOTH -> {
                centerYSize = null
                y -= getDummySize().y / 2
                verticalState = ONE
            }

            TWO -> {
                y -= getDummySize().y / 2
                verticalState = NONE
            }
        }

        Border.RIGHT -> when (horizontalState) {
            NONE -> {
                x += getDummySize().x / 2
                horizontalState = TWO
            }

            ONE -> {
                val xSize = getDummySize().x
                centerXSize = xSize
                x += xSize / 2
                horizontalState = BOTH
            }

            BOTH -> {
                x -= getDummySize().x / 2
                horizontalState = ONE
                centerXSize = null
            }

            TWO -> {
                x -= getDummySize().x / 2
                horizontalState = NONE
            }
        }

        Border.LEFT -> when (horizontalState) {
            NONE -> {
                x -= getDummySize().x / 2
                horizontalState = ONE
            }

            ONE -> {
                x += getDummySize().x / 2
                horizontalState = NONE
            }

            BOTH -> {
                centerXSize = null
                x += getDummySize().x / 2
                horizontalState = TWO
            }

            TWO -> {
                val xSize = getDummySize().x
                centerXSize = xSize
                x -= xSize / 2
                horizontalState = BOTH
            }
        }
    }

    companion object {
        const val DEFAULT_SCALE = 1f
        const val MIN_SCALE = 0.1f
        const val MAX_SCALE = 10f

        private class FieldNotFoundException(field: String, owner: Class<*>) :
            Exception("Config Link for field $field in class $owner not found")

        fun migrate(element: JsonElement): JsonElement {
            val obj = element.asJsonObject
            val center = obj["center"]?.asBoolean ?: return element
            if (center) obj.addProperty("centerX", true)
            return obj
        }
    }
}
