package net.samagames.uhcrun.gui;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.TreeMap;

public abstract class Gui
{
    protected TreeMap<Integer, String> actions = new TreeMap<>();
    protected Inventory inventory;

    public abstract void display(Player player);

    public void onClick(Player player, ItemStack stack, String action, ClickType clickType)
    {
        onClick(player, stack, action);
    }

    public abstract void onClick(Player player, ItemStack stack, String action);

    public Inventory getInventory()
    {
        return inventory;
    }

    public String getAction(int slot)
    {
        if (!actions.containsKey(slot))
        {
            return null;
        }
        return actions.get(slot);
    }

    public void setSlotData(Inventory inv, String name, Material material, int slot, String[] description, String action)
    {
        setSlotData(inv, name, new ItemStack(material, 1), slot, description, action);
    }

    public void setSlotData(String name, Material material, int slot, String[] description, String action)
    {
        setSlotData(inventory, name, new ItemStack(material, 1), slot, description, action);
    }

    public void setSlotData(String name, ItemStack item, int slot, String[] description, String action)
    {
        setSlotData(inventory, name, item, slot, description, action);
    }

    public void setSlotData(Inventory inv, String name, ItemStack item, int slot, String[] description, String action)
    {
        actions.put(slot, action);
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName(name);
        if (description != null)
        {
            meta.setLore(Arrays.asList(description));
        }

        item.setItemMeta(meta);
        inv.setItem(slot, item);
    }
}
