package repulica.resetting;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.component.ComponentMap;
import net.minecraft.component.DataComponentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.resource.JsonDataLoader;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.Pair;
import net.minecraft.util.profiler.Profiler;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ComponentManager extends JsonDataLoader implements IdentifiableResourceReloadListener {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
	public static final ComponentManager INSTANCE = new ComponentManager();

	private final Map<Item, ComponentMap> components = new HashMap<>();

	public ComponentManager() {
		super(GSON, "components");
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	@Override
	protected void apply(Map<Identifier, JsonElement> prepared, ResourceManager manager, Profiler profiler) {
		for (Identifier id : prepared.keySet()) {
			Item item = Registries.ITEM.get(id);
			JsonObject json = prepared.get(id).getAsJsonObject();
			JsonObject comps = JsonHelper.getObject(json, "components");
			ComponentMap.Builder builder = ComponentMap.builder();
			if (!JsonHelper.getBoolean(json, "replace", false)) {
				builder.addAll(item.getComponents());
			} else if (JsonHelper.getBoolean(json, "defaults", true)) {
				builder.addAll(DataComponentTypes.DEFAULT_ITEM_COMPONENTS);
			}
			for (String key : comps.keySet()) {
				//god I wish there was a way to do this sanely but I can't find one
				DataComponentType type = Registries.DATA_COMPONENT_TYPE.get(new Identifier(key));
				if (type == null) throw new IllegalArgumentException("No such component type " + key);
				builder.add(type, parseComponent(type, comps.get(key)));
			}
			components.put(item, builder.build());
		}
	}

	private <T> T parseComponent(DataComponentType<T> type, JsonElement elem) {
		DataResult<T> result = type.getCodecOrThrow().parse(JsonOps.INSTANCE, elem);
		if (result.result().isPresent()) return result.result().get();
		else throw new IllegalArgumentException("Error decoding comopnent: " + result.error().get());
	}

	public boolean hasOverrides(Item item) {
		return components.containsKey(item);
	}

	@Nullable
	public ComponentMap getComponents(Item item) {
		return components.get(item);
	}

	@Override
	public Identifier getFabricId() {
		return new Identifier("resetting", "components");
	}
}
