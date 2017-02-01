package cofh.core.item;

import cofh.api.core.IModelRegister;
import cofh.core.render.FontRendererCore;
import cofh.lib.util.helpers.ItemHelper;
import cofh.lib.util.helpers.SecurityHelper;
import cofh.lib.util.helpers.StringHelper;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import gnu.trove.map.TMap;
import gnu.trove.map.hash.THashMap;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ItemArmorMulti extends ItemArmor implements IModelRegister {

	private static final UUID[] ARMOR_MODIFIERS = new UUID[] { UUID.fromString("845DB27C-C624-495F-8C9F-6020A9A58B6B"), UUID.fromString("D8499B04-0E66-4726-AB29-64469D734E0D"), UUID.fromString("9F3D476D-C118-4544-8365-64846904B48E"), UUID.fromString("2AD3F246-FEE1-4E67-B886-69FD380BB150") };

	protected TMap<Integer, ArmorEntry> itemMap = new THashMap<Integer, ArmorEntry>();
	protected ArrayList<Integer> itemList = new ArrayList<Integer>(); // This is actually more memory efficient than a LinkedHashMap
	protected TMap<Integer, ModelResourceLocation> textureMap = new THashMap<Integer, ModelResourceLocation>();

	protected String name;
	protected String modName;
	protected boolean showInCreative = true;

	public ItemArmorMulti(EntityEquipmentSlot type) {

		this("cofh", type);
	}

	public ItemArmorMulti(String modName, EntityEquipmentSlot type) {

		super(ArmorMaterial.IRON, 0, type);
		this.modName = modName;
		setMaxStackSize(1);
		setHasSubtypes(true);
	}

	public ItemArmorMulti setShowInCreative(boolean showInCreative) {

		this.showInCreative = showInCreative;
		return this;
	}

	protected void addInformationDelegate(ItemStack stack, EntityPlayer player, List<String> list, boolean check) {

		int i = ItemHelper.getItemDamage(stack);
		if (!itemMap.containsKey(Integer.valueOf(i))) {
			return;
		}
		ArmorEntry item = itemMap.get(i);

		list.add(StringHelper.getInfoText("info." + modName + "." + name + "." + item.name));
	}

	protected int getStackDamage(ItemStack stack) {

		if (stack.getTagCompound() == null) {
			stack.setTagCompound(new NBTTagCompound());
			stack.getTagCompound().setInteger("Damage", 0);
		}
		return stack.getTagCompound().getInteger("Damage");
	}

	protected String getRepairIngot(ItemStack stack) {

		int i = ItemHelper.getItemDamage(stack);
		if (!itemMap.containsKey(Integer.valueOf(i))) {
			return "cobblestone";
		}
		return itemMap.get(ItemHelper.getItemDamage(stack)).ingot;
	}

	protected ItemArmor.ArmorMaterial getArmorMaterial(ItemStack stack) {

		int i = ItemHelper.getItemDamage(stack);
		if (!itemMap.containsKey(Integer.valueOf(i))) {
			return ArmorMaterial.IRON;
		}
		return itemMap.get(ItemHelper.getItemDamage(stack)).material;
	}

	/* ADD ITEMS */
	public ItemStack addItem(int number, ArmorEntry entry) {

		if (itemMap.containsKey(Integer.valueOf(number))) {
			return null;
		}
		itemMap.put(Integer.valueOf(number), entry);
		itemList.add(Integer.valueOf(number));

		ItemStack stack = new ItemStack(this, 1, number);
		stack.setTagCompound(new NBTTagCompound());
		stack.getTagCompound().setInteger("Damage", 0);
		return stack;
	}

	public ItemStack addItem(int number, String name, ItemArmor.ArmorMaterial material, String[] textures, String ingot, EnumRarity rarity) {

		return addItem(number, new ArmorEntry(name, material, textures, ingot, rarity));
	}

	public ItemStack addItem(int number, String name, ItemArmor.ArmorMaterial material, String[] textures, String ingot) {

		return addItem(number, new ArmorEntry(name, material, textures, ingot));
	}

	/* STANDARD METHODS */
	@Override
	@SideOnly (Side.CLIENT)
	public void getSubItems(@Nonnull Item item, CreativeTabs tab, List<ItemStack> list) {

		if (!showInCreative) {
			return;
		}
		for (int i = 0; i < itemList.size(); i++) {
			ItemStack stack = new ItemStack(item, 1, itemList.get(i));
			stack.setTagCompound(new NBTTagCompound());
			stack.getTagCompound().setInteger("Damage", 0);

			list.add(stack);
		}
	}

	@Override
	public void setDamage(ItemStack stack, int damage) {

		if (stack.getTagCompound() == null) {
			stack.setTagCompound(new NBTTagCompound());
			stack.getTagCompound().setInteger("Damage", 0);
		}
		if (damage < 0) {
			damage = 0;
		}
		stack.getTagCompound().setInteger("Damage", damage);
	}

	@Override
	public boolean getIsRepairable(ItemStack itemToRepair, ItemStack stack) {

		return ItemHelper.isOreNameEqual(stack, getRepairIngot(stack));
	}

	@Override
	public boolean hasCustomEntity(ItemStack stack) {

		return SecurityHelper.isSecure(stack);
	}

	@Override
	public boolean isDamaged(ItemStack stack) {

		return getStackDamage(stack) > 0;
	}

	@Override
	public boolean isItemTool(ItemStack stack) {

		return false;
	}

	@Override
	public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {

		return !ItemHelper.itemsEqualWithMetadata(oldStack, newStack);
	}

	@Override
	public boolean showDurabilityBar(ItemStack stack) {

		return getStackDamage(stack) > 0;
	}

	@Override
	public int getDamage(ItemStack stack) {

		return getStackDamage(stack);
	}

	@Override
	public int getMetadata(ItemStack stack) {

		return getStackDamage(stack);
	}

	@Override
	public int getItemEnchantability(ItemStack stack) {

		int i = ItemHelper.getItemDamage(stack);
		if (!itemMap.containsKey(Integer.valueOf(i))) {
			return 0;
		}
		return itemMap.get(ItemHelper.getItemDamage(stack)).material.getEnchantability();
	}

	@Override
	public int getMaxDamage(ItemStack stack) {

		return getArmorMaterial(stack).getDurability(armorType);
	}

	@Override
	public Entity createEntity(World world, Entity location, ItemStack stack) {

		if (SecurityHelper.isSecure(stack)) {
			location.invulnerable = true;
			location.isImmuneToFire = true;
			((EntityItem) location).lifespan = Integer.MAX_VALUE;
		}
		return null;
	}

	public Multimap<String, AttributeModifier> getItemAttributeModifiers(EntityEquipmentSlot slot, ItemStack stack) {

		Multimap<String, AttributeModifier> multimap = HashMultimap.<String, AttributeModifier>create();

		if (slot == this.armorType) {
			multimap.put(SharedMonsterAttributes.ARMOR.getAttributeUnlocalizedName(), new AttributeModifier(ARMOR_MODIFIERS[slot.getIndex()], "Armor modifier", (double) getArmorMaterial(stack).getDamageReductionAmount(slot), 0));
			multimap.put(SharedMonsterAttributes.ARMOR_TOUGHNESS.getAttributeUnlocalizedName(), new AttributeModifier(ARMOR_MODIFIERS[slot.getIndex()], "Armor toughness", (double) getArmorMaterial(stack).getToughness(), 0));
		}
		return multimap;
	}

	@Override
	@SideOnly (Side.CLIENT)
	public FontRenderer getFontRenderer(ItemStack stack) {

		return FontRendererCore.loadFontRendererStack(stack);
	}

	@Override
	public EnumRarity getRarity(ItemStack stack) {

		int i = ItemHelper.getItemDamage(stack);
		if (!itemMap.containsKey(Integer.valueOf(i))) {
			return EnumRarity.COMMON;
		}
		return itemMap.get(ItemHelper.getItemDamage(stack)).rarity;
	}

	@Override
	public String getArmorTexture(ItemStack stack, Entity entity, EntityEquipmentSlot slot, String type) {

		int i = ItemHelper.getItemDamage(stack);
		if (!itemMap.containsKey(Integer.valueOf(i))) {
			return "item.invalid";
		}
		ArmorEntry item = itemMap.get(i);

		if (slot == EntityEquipmentSlot.LEGS) {
			return item.textures[1];
		}
		return item.textures[0];
	}

	@Override
	public String getUnlocalizedName(ItemStack stack) {

		int i = ItemHelper.getItemDamage(stack);
		if (!itemMap.containsKey(Integer.valueOf(i))) {
			return "item.invalid";
		}
		ArmorEntry item = itemMap.get(i);
		return getUnlocalizedName() + "." + item.name;
	}

	@Override
	public Item setUnlocalizedName(String name) {

		GameRegistry.register(setRegistryName(name));
		this.name = name;
		name = modName + ".armor." + name;
		return super.setUnlocalizedName(name);
	}

	public Item setUnlocalizedName(String name, String registrationName) {

		GameRegistry.register(setRegistryName(registrationName));
		this.name = name;
		name = modName + ".armor." + name;
		return super.setUnlocalizedName(name);
	}

	/* IModelRegister */
	@Override
	@SideOnly (Side.CLIENT)
	public void registerModels() {

		ModelLoader.setCustomMeshDefinition(this, new ArmorMeshDefinition());

		for (Map.Entry<Integer, ArmorEntry> entry : itemMap.entrySet()) {

			ModelResourceLocation texture = new ModelResourceLocation(modName + ":armor/" + name + "_" + entry.getValue().name, "inventory");

			textureMap.put(entry.getKey(), texture);
			ModelBakery.registerItemVariants(this, texture);
		}
	}

	/* ITEM MESH DEFINITION */
	@SideOnly (Side.CLIENT)
	public class ArmorMeshDefinition implements ItemMeshDefinition {

		public ModelResourceLocation getModelLocation(ItemStack stack) {

			return textureMap.get(ItemHelper.getItemDamage(stack));
		}
	}

	/* ITEM ENTRY */
	public class ArmorEntry {

		public String name;
		public ItemArmor.ArmorMaterial material;
		public String[] textures;
		public String ingot;
		public EnumRarity rarity;

		ArmorEntry(String name, ItemArmor.ArmorMaterial material, String[] textures, String ingot, EnumRarity rarity) {

			this.name = name;
			this.material = material;
			this.textures = textures;
			this.ingot = ingot;
			this.rarity = rarity;
		}

		ArmorEntry(String name, ItemArmor.ArmorMaterial material, String[] textures, String ingot) {

			this(name, material, textures, ingot, EnumRarity.COMMON);
		}
	}

}
