package jadx.plugins.list

import io.github.oshai.kotlinlogging.KotlinLogging
import jadx.plugins.list.ListBuilderCLI.ProcessMode.DELTA
import jadx.plugins.list.ListBuilderCLI.ProcessMode.FULL
import jadx.plugins.tools.JadxPluginsTools
import kotlinx.serialization.json.Json
import java.io.File

class Process(private val args: ListBuilderCLI) {

	private val log = KotlinLogging.logger {}

	fun exec() {
		val inputs = loadInputs()
		val output = when (args.mode) {
			FULL -> processFull(inputs)
			DELTA -> processDelta(inputs)
		}
		Bundle(args.outputZip).save(output)
	}

	private fun processFull(inputs: List<InputMetadata>): List<PluginListEntry> {
		return inputs.map(::resolve)
	}

	private fun processDelta(inputs: List<InputMetadata>): List<PluginListEntry> {
		TODO()
	}

	private fun loadInputs(): List<InputMetadata> {
		return File(args.inputDir)
			.walkTopDown()
			.filter { it.name.endsWith(".json") && it.isFile }
			.map {
				val data = Json.decodeFromString<InputMetadata>(it.readText())
				if (data.pluginId.isBlank()) {
					data.pluginId = it.name.removeSuffix(".json")
				}
				data
			}
			.toList()
	}

	private fun resolve(input: InputMetadata): PluginListEntry {
		log.debug { "resolving plugin: ${input.pluginId} from: ${input.locationId}" }
		val metadata = JadxPluginsTools.getInstance().resolveMetadata(input.locationId)
		return PluginListEntry.convert(metadata).apply {
			this.revision = input.revision
		}
	}
}
