package dev.fixify.client.feature

import net.minecraft.client.Minecraft
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraft.network.chat.Style
import net.minecraft.network.chat.contents.PlainTextContents
import net.minecraft.util.FormattedCharSequence

object NameReplaceFeature {
	@Volatile
	private var nameRegex: Regex? = null

	@Volatile
	private var replacementText: String = ""

	@Volatile
	private var replacementComponent: Component = Component.empty()

	@Volatile
	private var startChar: Char? = null

	@JvmStatic
	fun isEnabled(): Boolean = nameRegex != null

	fun refresh() {
		if (!FixifyFeatures.nameReplaceEnabled) {
			clear()
			return
		}
		val source = Minecraft.getInstance().user.name.trim()
		val replacement = FixifyFeatures.nameReplacement.trim()
		if (source.isEmpty() || replacement.isEmpty()) {
			clear()
			return
		}

		nameRegex = Regex("(?<![A-Za-z0-9_])(${Regex.escape(source)})(?![A-Za-z0-9_])")
		replacementText = replacement
		replacementComponent = Component.literal(replacement).withStyle(
			Style.EMPTY.withColor(FixifyFeatures.nameReplaceColor.argb and 0x00FFFFFF),
		)
		startChar = source.firstOrNull()
	}

	@JvmStatic
	fun replaceStringIfNeeded(text: String): String {
		val regex = nameRegex ?: return text
		if (text.isBlank() || !regex.containsMatchIn(text)) {
			return text
		}
		return regex.replace(text) { replacementText }
	}

	@JvmStatic
	fun replaceComponentIfNeeded(component: Component): Component? {
		val regex = nameRegex ?: return null
		if (!regex.containsMatchIn(component.string)) {
			return null
		}
		return transformComponent(component, regex)
	}

	@JvmStatic
	fun replaceSequenceIfNeeded(text: FormattedCharSequence): FormattedCharSequence {
		if (!hasStartChar(text)) {
			return text
		}
		val regex = nameRegex ?: return text
		val segments = ArrayList<Pair<Style, String>>()
		var currentStyle: Style? = null
		val currentText = StringBuilder()
		text.accept { _, style, codePoint ->
			if (currentStyle != null && style != currentStyle) {
				segments.add(currentStyle!! to currentText.toString())
				currentText.clear()
			}
			currentStyle = style
			currentText.appendCodePoint(codePoint)
			true
		}
		if (currentText.isNotEmpty()) {
			segments.add((currentStyle ?: Style.EMPTY) to currentText.toString())
		}
		if (segments.none { regex.containsMatchIn(it.second) }) {
			return text
		}

		val root = Component.empty()
		for ((style, segment) in segments) {
			appendReplaced(root, segment, style, regex)
		}
		return root.visualOrderText
	}

	private fun transformComponent(component: Component, regex: Regex): MutableComponent {
		val contents = component.contents
		val output = if (contents is PlainTextContents && regex.containsMatchIn(contents.text())) {
			Component.empty().also { appendReplaced(it, contents.text(), component.style, regex) }
		} else {
			component.plainCopy()
		}
		for (sibling in component.siblings) {
			output.append(transformComponent(sibling, regex))
		}
		return output
	}

	private fun appendReplaced(output: MutableComponent, text: String, style: Style, regex: Regex) {
		var start = 0
		for (match in regex.findAll(text)) {
			if (match.range.first > start) {
				output.append(Component.literal(text.substring(start, match.range.first)).withStyle(style))
			}
			output.append(replacementComponent.copy())
			start = match.range.last + 1
		}
		if (start < text.length) {
			output.append(Component.literal(text.substring(start)).withStyle(style))
		}
	}

	private fun hasStartChar(text: FormattedCharSequence): Boolean {
		val first = startChar ?: return false
		var found = false
		text.accept { _, _, codePoint ->
			if (codePoint <= Char.MAX_VALUE.code && codePoint.toChar() == first) {
				found = true
				false
			} else {
				true
			}
		}
		return found
	}

	private fun clear() {
		nameRegex = null
		replacementText = ""
		replacementComponent = Component.empty()
		startChar = null
	}
}
