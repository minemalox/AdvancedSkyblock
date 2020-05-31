package dev.minemalox.advancedskyblock.utils;

import dev.minemalox.advancedskyblock.AdvancedSkyblock;
import dev.minemalox.advancedskyblock.utils.item.ItemUtils;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.init.Enchantments;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.potion.PotionType;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.Constants;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter
public class Backpack {

    private static final Pattern BACKPACK_ID_PATTERN = Pattern.compile("([A-Z]+)_BACKPACK");

    @Setter
    private int x;
    @Setter
    private int y;
    private ItemStack[] items;
    private String backpackName;
    private BackpackColor backpackColor;

    public Backpack(ItemStack[] items, String backpackName, BackpackColor backpackColor) {
        this.items = items;
        this.backpackName = backpackName;
        this.backpackColor = backpackColor;
    }

    public Backpack(ItemStack[] items, String backpackName, BackpackColor backpackColor, int x, int y) {
        this(items, backpackName, backpackColor);
        this.x = x;
        this.y = y;
    }

    public static Backpack getFromItem(ItemStack stack) {
        if (stack == null) return null;
        AdvancedSkyblock main = AdvancedSkyblock.getInstance();
        String id = ItemUtils.getSkyBlockItemID(stack);
        if (id != null) {
            NBTTagCompound extraAttributes = stack.getTagCompound().getCompoundTag("ExtraAttributes");
            Matcher matcher = BACKPACK_ID_PATTERN.matcher(id);
            boolean matches = matcher.matches();
            if (matches || (main.getConfigValues().isEnabled(Feature.CAKE_BAG_PREVIEW) // If it's a backpack OR it's a cake
                    && "NEW_YEAR_CAKE_BAG".equals(id))) { //                              bag and they have the setting enabled.
                byte[] bytes = null;
                for (String key : extraAttributes.getKeySet()) {
                    if (key.endsWith("backpack_data") || key.equals("new_year_cake_bag_data")) {
                        bytes = extraAttributes.getByteArray(key);
                        break;
                    }
                }
                try {
                    int length = 0;
                    if (matches) {
                        String backpackType = matcher.group(1);
                        switch (backpackType) { // because sometimes the size of the tag is not updated (etc. when you upcraft it)
                            case "SMALL":
                                length = 9;
                                break;
                            case "MEDIUM":
                                length = 18;
                                break;
                            case "LARGE":
                                length = 27;
                                break;
                            case "GREATER":
                                length = 36;
                                break;
                        }
                    }
                    ItemStack[] items = new ItemStack[length];
                    if (bytes != null) {
                        NBTTagCompound nbtTagCompound = CompressedStreamTools.readCompressed(new ByteArrayInputStream(bytes));
                        NBTTagList list = nbtTagCompound.getTagList("i", Constants.NBT.TAG_COMPOUND);
                        if (list.tagCount() > length) {
                            length = list.tagCount();
                            items = new ItemStack[length];
                        }
                        for (int i = 0; i < length; i++) {
                            NBTTagCompound item = list.getCompoundTagAt(i);
                            // This fixes an issue in Hypixel where enchanted potatoes have the wrong id (potato block instead of item).
                            short itemID = item.getShort("id");

                            boolean enchanted = false;
                            ResourceLocation resLoc;
                            String newItemId = "air";
                            if ((resLoc = Item.getItemById(itemID).getRegistryName()) != null) {
                                newItemId = resLoc.toString();
                            }

                            if (itemID == 142) { // Potato Block -> Potato Item
                                item.setShort("id", (short) 392);
                            } else if (itemID == 141) { // Carrot Block -> Carrot Item
                                item.setShort("id", (short) 391);
                            }

                            if (item.hasKey("tag")) {
                                NBTTagCompound tag = item.getCompoundTag("tag");

                                if (tag.hasKey("ExtraAttributes")) {
                                    NBTTagCompound itemAttributes = tag.getCompoundTag("ExtraAttributes");
                                    String attributeID = itemAttributes.getString("id");

                                    if (attributeID.startsWith("ENCHANTED_")) {
                                        enchanted = true;
                                    }
                                }
                            }

                            // Convert item id from 1.8 to 1.12 format
                            item.removeTag("id");
                            item.setString("id", newItemId);
                            ItemStack itemStack = new ItemStack(item);

                            // Fix potions
                            if ("minecraft:potion".equals(newItemId)) {
                                NBTTagCompound tag = item.getCompoundTag("tag");
                                ItemStack potion = new ItemStack(Items.POTIONITEM, 1);
                                List<PotionEffect> effects = PotionUtils.getEffectsFromTag(tag);
                                PotionUtils.appendEffects(potion, effects);

                                if (!effects.isEmpty()) {
                                    int pid = Potion.getIdFromPotion(effects.get(0).getPotion());
                                    PotionType potionType = PotionType.REGISTRY.getObjectById(pid);
                                    PotionUtils.addPotionToItemStack(potion, potionType);
                                }

                                // TODO: Fix non-potion potions (awkward, water, etc.)
                                itemStack = potion;
                            }

                            if (enchanted)
                                itemStack.addEnchantment(Enchantments.PROTECTION, 1);


                            items[i] = itemStack;
                        }
                    }
                    BackpackColor color = BackpackColor.WHITE;
                    if (extraAttributes.hasKey("backpack_color")) {
                        try {
                            color = BackpackColor.valueOf(extraAttributes.getString("backpack_color"));
                        } catch (IllegalArgumentException ignored) {
                        }
                    }
                    return new Backpack(items, TextUtils.stripColor(stack.getDisplayName()), color);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }
}
