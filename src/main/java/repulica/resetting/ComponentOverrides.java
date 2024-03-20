package repulica.resetting;


import net.minecraft.component.Component;
import net.minecraft.component.ComponentMap;
import net.minecraft.component.DataComponentType;
import net.minecraft.item.Item;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;

public record ComponentOverrides(Map<Item, ComponentMap> components) implements CustomPayload {
	public static final Id<ComponentOverrides> ID = new Id<>(new Identifier("resetting", "component_overrides"));
	public static final PacketCodec<RegistryByteBuf, ComponentOverrides> PACKET_CODEC = PacketCodec.of(ComponentOverrides::write, ComponentOverrides::read);

	@SuppressWarnings({"unchecked", "rawtypes"})
	public static ComponentOverrides read(RegistryByteBuf buf) {
		Map<Item, ComponentMap> components = new HashMap<>();
		int itemCount = buf.readVarInt();
		for (int i = 0; i < itemCount; i++) {
			RegistryKey<Item> itemKey = buf.readRegistryKey(RegistryKeys.ITEM);
			Item item = Registries.ITEM.get(itemKey);
			int compsCount = buf.readVarInt();
			ComponentMap.Builder builder = ComponentMap.builder();
			for (int j = 0; j < compsCount; j++) {
				RegistryKey<DataComponentType<?>> compKey = buf.readRegistryKey(RegistryKeys.DATA_COMPONENT_TYPE);
				DataComponentType type = Registries.DATA_COMPONENT_TYPE.get(compKey);
				builder.add(type, type.getPacketCodec().decode(buf));
			}
			components.put(item, builder.build());
		}
		return new ComponentOverrides(components);
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	public void write(RegistryByteBuf buf) {
		buf.writeVarInt(components.size());
		for (Item item : components.keySet()) {
			ComponentMap map = components.get(item);
			buf.writeRegistryKey(Registries.ITEM.getKey(item).orElseThrow(() -> new IllegalStateException("Unregistered items should be impossible")));
			buf.writeVarInt(map.size());
			for (Component<?> comp : map) {
				DataComponentType type = comp.type();
				buf.writeRegistryKey(Registries.DATA_COMPONENT_TYPE.getKey(type).orElseThrow(() -> new IllegalStateException("Unregistered data component type")));
				type.getPacketCodec().encode(buf, comp.value());
			}
		}
	}

	@Override
	public Id<? extends CustomPayload> getId() {
		return ID;
	}
}
