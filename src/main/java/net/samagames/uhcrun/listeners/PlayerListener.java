package net.samagames.uhcrun.listeners;

import net.samagames.api.games.GameUtils;
import net.samagames.api.games.Status;
import net.samagames.uhcrun.UHCRun;
import net.samagames.uhcrun.datasaver.DamageDone;
import net.samagames.uhcrun.datasaver.DamageReceived;
import net.samagames.uhcrun.datasaver.HealingSource;
import net.samagames.uhcrun.datasaver.SavedPlayer;
import net.samagames.uhcrun.game.BasicGame;
import net.samagames.uhcrun.game.SoloGame;
import net.samagames.uhcrun.game.Team;
import net.samagames.uhcrun.game.TeamGame;
import net.samagames.uhcrun.generator.WorldLoader;
import net.samagames.uhcrun.utils.Gui;
import net.samagames.uhcrun.utils.GuiSelectTeam;
import net.samagames.uhcrun.utils.Metadatas;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

/**
 * This file is a part of the SamaGames Project CodeBase
 * This code is absolutely confidential.
 * Created by {USER}
 * (C) Copyright Elydra Network 2014 & 2015
 * All rights reserved.
 */
public class PlayerListener implements Listener {

    protected BasicGame game;

    public PlayerListener(BasicGame game) {
        this.game = game;
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player)
            if (!game.isDamages())
                event.setCancelled(true);
    }

    public static String getDisplayName(EntityType entityType) {
        switch (entityType) {
            case ENDER_PEARL:
                return "EnderPearl";
            case PRIMED_TNT:
            case MINECART_TNT:
                return "TNT";
            case FALLING_BLOCK:
                return "Bloc";
            case CREEPER:
                return "Creeper";
            case SKELETON:
                return "Squelette";
            case SPIDER:
                return "Araignée";
            case ZOMBIE:
                return "Zombie";
            case SLIME:
                return "Slime";
            case GHAST:
                return "Ghast";
            case ENDERMAN:
                return "Enderman";
            case CAVE_SPIDER:
                return "Araignée des Cavernes";
            case SILVERFISH:
                return "Silverfish";
            case BLAZE:
            case FIREBALL:
            case SMALL_FIREBALL:
                return "Blaze";
            case IRON_GOLEM:
                return "Golem de Fer";
            case WOLF:
                return "Loup";
            case LIGHTNING:
                return "Foudre";
            default:
                return entityType.name();
        }
    }

    @EventHandler (ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void saveDamages(EntityDamageEvent event) {
        if (event.getFinalDamage() == 0)
            return;

        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            if (game.isInGame(player.getUniqueId())) {
                SavedPlayer pl = game.getStoredGame().getPlayer(player.getUniqueId(), player.getName());
                DamageReceived received;

                if (event instanceof EntityDamageByEntityEvent) {
                    Entity source = ((EntityDamageByEntityEvent) event).getDamager();
                    if (source instanceof Player) {
                        ItemStack inHand = ((Player) source).getItemInHand();
                        received = new DamageReceived(((inHand == null) ? Material.AIR : inHand.getType()), ((Player) source).getName(), ((Player) source).getDisplayName(), event.getFinalDamage());
                        SavedPlayer sh = game.getStoredGame().getPlayer(source.getUniqueId(), ((Player) source).getName());
                        sh.doDamage(new DamageDone(((inHand == null) ? Material.AIR : inHand.getType()), player.getName(), player.getDisplayName(), event.getFinalDamage()));

                    } else if (source instanceof Projectile) {
                        Projectile arrow = (Projectile) source;
                        Entity shooter = (Entity) arrow.getShooter();
                        if (shooter instanceof Player) {
                            received = new DamageReceived(Material.ARROW, ((Player) shooter).getName(), ((Player) shooter).getDisplayName(), event.getFinalDamage());
                            SavedPlayer sh = game.getStoredGame().getPlayer(shooter.getUniqueId(), ((Player) shooter).getName());
                            sh.doDamage(new DamageDone(Material.ARROW, player.getName(), player.getDisplayName(), event.getFinalDamage()));
                        } else {
                            received = new DamageReceived(Material.ARROW, shooter.getType().toString(), getDisplayName(shooter.getType()), event.getFinalDamage());
                        }
                    } else {
                        Material mat = Material.AIR;
                        if (source instanceof LivingEntity) {
                            try {
                                mat = ((LivingEntity) source).getEquipment().getItemInHand().getType();
                            } catch (Exception ignored) {}
                        }
                        received = new DamageReceived(mat, source.getType().toString(), getDisplayName(source.getType()), event.getFinalDamage());
                    }
                } else {
                    received = new DamageReceived(Material.AIR, BasicGame.getDamageCause(event.getCause()), BasicGame.getDamageCause(event.getCause()), event.getFinalDamage());
                }

                pl.takeDamage(received);
            }
        }
    }

    @EventHandler (ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void regainHealth(EntityRegainHealthEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            String reason;
            switch (event.getRegainReason()) {
                case REGEN:
                case MAGIC_REGEN:
                    reason = "Régénération";
                    break;
                case MAGIC:
                    reason = "Potion";
                    break;
                default:
                    reason = "Inconnue";
            }

            SavedPlayer pl = game.getStoredGame().getPlayer(player.getUniqueId(), player.getName());
            pl.heal(new HealingSource(reason, event.getAmount()));
        }
    }

    @EventHandler
    public void onItemConsume(PlayerItemConsumeEvent event) {
        if (event.getItem().getType() == Material.GOLDEN_APPLE) {
            event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 10 * 20, 1));
        }
    }

    @EventHandler
    public void onConsumeBucket(PlayerBucketEmptyEvent event) {
        if (event.getBucket() == Material.LAVA_BUCKET && !game.isPvpEnabled()) {
            event.getPlayer().sendMessage(ChatColor.RED + "Le placement de lave est interdit avant l'activation du PVP.");
        }
    }

    @EventHandler
    public void inventoryClose(InventoryCloseEvent event) {
        if(UHCRun.instance.getPlayerGui(event.getPlayer().getUniqueId()) != null) {
            UHCRun.instance.removePlayerFromList(event.getPlayer().getUniqueId());
        }
    }

    @EventHandler
    public void playerInteractEvent(PlayerInteractEvent event) {
		if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			if (event.getItem() != null && event.getItem().getType() == Material.LAVA_BUCKET && !game.isPvpEnabled()) {
				event.setCancelled(true);
				event.getPlayer().sendMessage(ChatColor.RED + "Le placement de lave est interdit avant l'activation du PVP.");
				return;
			}
		}

        if(event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if(event.getItem() != null) {
                if (! UHCRun.instance.game.isGameStarted()) {
                    event.setCancelled(true);

                    if(event.getItem().getType() == Material.WOOD_DOOR)
                        game.getAPI().getGameManager().kickPlayer(event.getPlayer(), "");

                    else if (event.getItem().getType() == Material.NETHER_STAR)
                        UHCRun.instance.openGui(event.getPlayer(), new GuiSelectTeam());
                } else {
                    if(! UHCRun.instance.game.hasPlayer(event.getPlayer().getUniqueId())) {
                        event.setCancelled(true);

                        if(event.getItem().getType() == Material.WOOD_DOOR)
                            game.getAPI().getGameManager().kickPlayer(event.getPlayer(), "");
                    }
                }
            }
        }
    }

    @EventHandler
    public void clickEvent(InventoryClickEvent event) {
        if(event.getWhoClicked() instanceof Player) {
            Player player = (Player) event.getWhoClicked();

            if (! UHCRun.instance.game.isGameStarted() && event.getView().getType() == InventoryType.PLAYER) {
                event.setCancelled(true);
            }

            if (event.getCurrentItem() != null && event.getCurrentItem().getItemMeta() != null) {
                Gui gui = UHCRun.instance.getPlayerGui(player.getUniqueId());
                if (gui != null) {
                    String action = gui.getAction(event.getSlot());
                    if (action != null)
                        gui.onClick(player, event.getCurrentItem(), action, event.getClick());
                    event.setCancelled(true);
                }
            }
        }
    }

    private void breakLeaf(World world, int x, int y, int z) {
        Block block = world.getBlockAt(x, y, z);
        byte data = block.getData();

        if((data & 4) == 4)
        {
            return; // player placed leaf, ignore
        }

        byte range = 4;
        byte max = 32;
        int[] blocks = new int[max * max * max];
        int off = range + 1;
        int mul = max * max;
        int div = max / 2;

        if(validChunk(world, x - off, y - off, z - off, x + off, y + off, z + off))
        {
            int offX;
            int offY;
            int offZ;
            int type;

            for(offX = -range; offX <= range; offX++)
            {
                for(offY = -range; offY <= range; offY++)
                {
                    for(offZ = -range; offZ <= range; offZ++)
                    {
                        Material mat = world.getBlockAt(x + offX, y + offY, z + offZ).getType();
                        if ((mat == Material.LEAVES || mat == Material.LEAVES_2))
                            type = Material.LEAVES.getId();
                        else if ((mat == Material.LOG || mat == Material.LOG_2))
                            type = Material.LOG.getId();
                        blocks[(offX + div) * mul + (offY + div) * max + offZ + div] = ((mat == Material.LOG || mat == Material.LOG_2) ? 0 : ((mat == Material.LEAVES || mat == Material.LEAVES_2) ? -2 : -1));
                    }
                }
            }

            for(offX = 1; offX <= 4; offX++)
            {
                for(offY = -range; offY <= range; offY++)
                {
                    for(offZ = -range; offZ <= range; offZ++)
                    {
                        for(type = -range; type <= range; type++)
                        {
                            if(blocks[(offY + div) * mul + (offZ + div) * max + type + div] == offX - 1)
                            {
                                if(blocks[(offY + div - 1) * mul + (offZ + div) * max + type + div] == -2)
                                    blocks[(offY + div - 1) * mul + (offZ + div) * max + type + div] = offX;

                                if(blocks[(offY + div + 1) * mul + (offZ + div) * max + type + div] == -2)
                                    blocks[(offY + div + 1) * mul + (offZ + div) * max + type + div] = offX;

                                if(blocks[(offY + div) * mul + (offZ + div - 1) * max + type + div] == -2)
                                    blocks[(offY + div) * mul + (offZ + div - 1) * max + type + div] = offX;

                                if(blocks[(offY + div) * mul + (offZ + div + 1) * max + type + div] == -2)
                                    blocks[(offY + div) * mul + (offZ + div + 1) * max + type + div] = offX;

                                if(blocks[(offY + div) * mul + (offZ + div) * max + (type + div - 1)] == -2)
                                    blocks[(offY + div) * mul + (offZ + div) * max + (type + div - 1)] = offX;

                                if(blocks[(offY + div) * mul + (offZ + div) * max + type + div + 1] == -2)
                                    blocks[(offY + div) * mul + (offZ + div) * max + type + div + 1] = offX;
                            }
                        }
                    }
                }
            }
        }

        if(blocks[div * mul + div * max + div] < 0) {
            LeavesDecayEvent event = new LeavesDecayEvent(block);
            Bukkit.getServer().getPluginManager().callEvent(event);

            if(event.isCancelled()) {
                return;
            }

            block.breakNaturally();

            if(10 > new Random().nextInt(100))
            {
                world.playEffect(block.getLocation(), Effect.STEP_SOUND, Material.LEAVES.getId());
            }
        }
    }

    public boolean validChunk(World world, int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        if(maxY >= 0 && minY < world.getMaxHeight())
        {
            minX >>= 4;
            minZ >>= 4;
            maxX >>= 4;
            maxZ >>= 4;

            for(int x = minX; x <= maxX; x++)
            {
                for(int z = minZ; z <= maxZ; z++)
                {
                    if(!world.isChunkLoaded(x, z))
                    {
                        return false;
                    }
                }
            }

            return true;
        }

        return false;
    }

    @EventHandler
    public void onSignChange(SignChangeEvent event) {
        BasicGame bgame = UHCRun.instance.game;
        if (!(bgame instanceof TeamGame))
            return;

        TeamGame game = (TeamGame) bgame;
        if (!game.isGameStarted()) {
            event.getBlock().setType(Material.AIR);
            Team team = game.getPlayerTeam(event.getPlayer().getUniqueId());
            String name = event.getLine(0);
            name = name.trim();

            if(!name.equals("")) {
                team.setTeamName(name);
                event.getPlayer().sendMessage(game.getCoherenceMachine().getGameTag()+ChatColor.GREEN + "Le nom de votre équipe est désormais : "+team.getChatColor()+team.getTeamName());
                UHCRun.instance.openGui(event.getPlayer(), new GuiSelectTeam());
            } else {
                event.getPlayer().sendMessage(game.getCoherenceMachine().getGameTag() + ChatColor.RED + "Le nom de l'équipe ne peut être vide.");
                UHCRun.instance.openGui(event.getPlayer(), new GuiSelectTeam());
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncPlayerChatEvent event) {
        if (UHCRun.instance.game instanceof SoloGame)
            return;

        TeamGame game = (TeamGame) UHCRun.instance.game;
        if (!game.isGameStarted())
            return;

        if (event.getMessage().startsWith("!")) {
            String message = event.getMessage().substring(1);
            Team team = game.getPlayerTeam(event.getPlayer().getUniqueId());
            if (team != null) {
                event.setFormat(team.getChatColor() + "[" + team.getTeamName() + "] " + event.getPlayer().getName() + " : " + ChatColor.WHITE + message);
            }
        } else {
            Team team = game.getPlayerTeam(event.getPlayer().getUniqueId());
            if (team != null) {
                event.setCancelled(true);
                String message = team.getChatColor() + "(Equipe) " + event.getPlayer().getName() + " : " + ChatColor.GOLD + ChatColor.ITALIC + event.getMessage();
                for (UUID id : team.getPlayers()) {
                    Player player = Bukkit.getPlayer(id);
                    if (player != null)
                        player.sendMessage(message);
                }
            }
        }
    }

    @EventHandler
    public void brewevent(BrewEvent event) {
        if(event.getContents().getIngredient().getType() == Material.GLOWSTONE_DUST)
            event.setCancelled(true);
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        Metadatas.setMetadata(event.getItemDrop(), "playerDrop", true);
    }

    @EventHandler
    public void itemSpawn(ItemSpawnEvent event) {
        if (Metadatas.getMetadata(event.getEntity(), "playerDrop") != null)
            return;

        String CHECK_LINE = ChatColor.GRAY + "© Aperture Science - All rights reserved";

        ArrayList<String> customLore = new ArrayList<>();
        Material mat = event.getEntity().getItemStack().getType();
        ItemMeta me = event.getEntity().getItemStack().getItemMeta();
        if (me != null && me.getLore() != null && me.getLore().contains(CHECK_LINE))
            return;

        switch (mat) {
            case IRON_ORE:
                event.getEntity().setItemStack(new ItemStack(Material.IRON_INGOT, 2));
                break;
            case SAND:
                event.getEntity().setItemStack(new ItemStack(Material.GLASS, 1));
                break;
            case GRAVEL:
            case FLINT:
                if (new Random().nextDouble() < 0.75) {
                    ItemStack loot = new ItemStack(Material.ARROW, 3);
                    ItemMeta meta = loot.getItemMeta();
                    customLore.add(ChatColor.GRAY + "Aperture™ Companion Arrow");
                    customLore.add(CHECK_LINE);
                    meta.setLore(customLore);
                    loot.setItemMeta(meta);
                    event.getEntity().setItemStack(loot);
                }
                break;
            case GOLD_ORE:
                event.getEntity().setItemStack(new ItemStack(Material.GOLD_INGOT, 2));
                break;
            case COAL:
                event.getEntity().setItemStack(new ItemStack(Material.TORCH, 3));
                break;
            case DIAMOND:
                ItemStack loot = new ItemStack(Material.DIAMOND, event.getEntity().getItemStack().getAmount() * 2);
                ItemMeta meta = loot.getItemMeta();
                customLore.add(ChatColor.GRAY + "Aperture™ Companion Diamond");
                customLore.add(CHECK_LINE);
                meta.setLore(customLore);
                loot.setItemMeta(meta);
                event.getEntity().setItemStack(loot);
                break;
            case CACTUS:
                event.getEntity().setItemStack(new ItemStack(Material.LOG, 2));
        }
    }



    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onChestOpen(PlayerInteractEvent event) {
        if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK) && event.getClickedBlock().getType().equals(Material.CHEST)) {
            Chest chest = (Chest) event.getClickedBlock().getState();
            int slot = 0;
            while (slot < chest.getInventory().getSize()) {
                ItemStack stack = chest.getInventory().getItem(slot);
                if (stack == null) {
                    slot ++;
                    continue;
                }

                if (stack.getType() == Material.DIAMOND) {
                    String CHECK_LINE = ChatColor.GRAY + "© Aperture Science - All rights reserved";
                    ItemMeta meta = stack.getItemMeta();
                    ArrayList<String> customLore = new ArrayList<>();
                    customLore.add(ChatColor.GRAY + "Aperture™ Companion Diamond");
                    customLore.add(CHECK_LINE);
                    meta.setLore(customLore);
                    stack.setItemMeta(meta);

                    chest.getInventory().setItem(slot, stack);
                }
                slot ++;
            }
        }
    }

    @EventHandler
    public void onBeginBreak(BlockDamageEvent event) {
        event.getPlayer().removePotionEffect(PotionEffectType.SLOW_DIGGING);
        if (event.getBlock().getType() == Material.OBSIDIAN)
            event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, 20000, 2, true, true));
        else
            event.getPlayer().removePotionEffect(PotionEffectType.FAST_DIGGING);
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        Material mat = event.getBlock().getType();
        Location loc = event.getBlock().getLocation();
        event.getPlayer().removePotionEffect(PotionEffectType.FAST_DIGGING);

        switch (mat) {
            case LOG: case LOG_2:
                final List<Block> bList = new ArrayList<>();
                checkLeaves(event.getBlock());
                bList.add(event.getBlock());
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        for (int i = 0; i < bList.size(); i++) {
                            Block block = bList.get(i);
                            if (block.getType() == Material.LOG || block.getType() == Material.LOG_2) {
                                for (ItemStack item : block.getDrops()) {
                                    block.getWorld().dropItemNaturally(block.getLocation(), item);
                                }

                                block.setType(Material.AIR);
                                checkLeaves(block);
                            }
                            for (BlockFace face : BlockFace.values()) {
                                if (block.getRelative(face).getType() == Material.LOG || block.getRelative(face).getType() == Material.LOG_2) {
                                    bList.add(block.getRelative(face));
                                }
                            }
                            bList.remove(block);
                        }
                        if (bList.size() == 0)
                            cancel();
                    }
                }.runTaskTimer(UHCRun.instance, 1, 1);
                break;
            case DIAMOND_ORE:
            case LAPIS_ORE:
            case GOLD_ORE:
            case OBSIDIAN:
            case IRON_ORE:
            case REDSTONE_ORE:
            case QUARTZ_ORE:
                event.setCancelled(true);
                event.getBlock().breakNaturally(new ItemStack(Material.DIAMOND_PICKAXE));
        }
        event.getPlayer().giveExp(event.getExpToDrop() * 2);
    }

    private void checkLeaves(Block block) {
        Location loc = block.getLocation();
        final World world = loc.getWorld();
        final int x = loc.getBlockX();
        final int y = loc.getBlockY();
        final int z = loc.getBlockZ();
        final int range = 4;
        final int off = range + 1;

        if(!validChunk(world, x - off, y - off, z - off, x + off, y + off, z + off))
        {
            return;
        }

        Bukkit.getServer().getScheduler().runTask(UHCRun.instance, new Runnable() {
            @Override
            public void run() {
                for (int offX = - range; offX <= range; offX++) {
                    for (int offY = - range; offY <= range; offY++) {
                        for (int offZ = - range; offZ <= range; offZ++) {
                            if ((world.getBlockAt(x + offX, y + offY, z + offZ).getType() == Material.LEAVES || world.getBlockAt(x + offX, y + offY, z + offZ).getType() == Material.LEAVES_2)) {
                                breakLeaf(world, x + offX, y + offY, z + offZ);
                            }
                        }
                    }
                }
            }
        });
    }

    @EventHandler
    public void onCraft(CraftItemEvent event) {
        if (event.getRecipe().getResult().getType() == Material.GOLDEN_APPLE && event.getRecipe().getResult().getDurability() == 1)
            event.getInventory().setResult(new ItemStack(Material.AIR));
        else if (event.getRecipe().getResult().getType() == Material.WOOD_PICKAXE)
            event.getInventory().setResult(new ItemStack(Material.STONE_PICKAXE));
        else if (event.getRecipe().getResult().getType() == Material.WOOD_AXE)
            event.getInventory().setResult(new ItemStack(Material.STONE_AXE));
        else if (event.getRecipe().getResult().getType() == Material.WOOD_SWORD)
            event.getInventory().setResult(new ItemStack(Material.STONE_SWORD));
        else if (event.getRecipe().getResult().getType() == Material.WOOD_SPADE)
            event.getInventory().setResult(new ItemStack(Material.STONE_SPADE));
        else if (event.getRecipe().getResult().getType() == Material.DIAMOND) {
            String CHECK_LINE = ChatColor.GRAY + "© Aperture Science - All rights reserved";
            ArrayList<String> customLore = new ArrayList<>();
            customLore.add(ChatColor.GRAY + "Aperture™ Companion Diamond");
            customLore.add(CHECK_LINE);

            ItemStack res = event.getInventory().getResult();
            ItemMeta meta = res.getItemMeta();
            meta.setLore(customLore);
            res.setItemMeta(meta);
            event.getInventory().setResult(res);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (!game.isPvpEnabled() && (event.getBlockPlaced().getType() == Material.LAVA || event.getBlockPlaced().getType() == Material.STATIONARY_LAVA)) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.RED + "Le PVP est désactivé, l'utilisation de sources de lave est interdite.");
        }

        int x = event.getBlockPlaced().getX();
        int y = event.getBlockPlaced().getY();
        int z = event.getBlockPlaced().getZ();

        if (x > -50 && x < 50 && z > -50 && z < 50 && y > WorldLoader.getHighestNaturalBlockAt(x, z) + 17) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(ChatColor.DARK_RED + "[" + ChatColor.RED + "Towers" + ChatColor.DARK_RED + "] " + ChatColor.RED + "Les Towers sont interdites en UHCRun.");
        }
    }

    @EventHandler
    public void onEntitySpawn(EntitySpawnEvent event) {
        if (event.getEntityType() == EntityType.WITCH)
            event.setCancelled(true);
    }

	@EventHandler
	public void onMove(PlayerMoveEvent event) {
		Location to = event.getTo();
		double max = to.getWorld().getWorldBorder().getSize() / 2.0;
		max += 50;
		if (to.getZ() > max || to.getX() > max || to.getZ() < -max || to.getX() < -max) {
			event.setCancelled(true);
			event.getPlayer().teleport(new Location(event.getTo().getWorld(), 0, 128, 0));
			event.getPlayer().sendMessage(ChatColor.RED + "Ne t'éloigne pas trop, tu pourrais te perdre.");
		}
	}

    @EventHandler
    public void onCraft(PrepareItemCraftEvent event) {
        if (event.getRecipe().getResult().getType() == Material.GOLDEN_APPLE && event.getRecipe().getResult().getDurability() == 1)
            event.getInventory().setResult(new ItemStack(Material.AIR));
        else if (event.getRecipe().getResult().getType() == Material.WOOD_PICKAXE)
            event.getInventory().setResult(new ItemStack(Material.STONE_PICKAXE));
        else if (event.getRecipe().getResult().getType() == Material.WOOD_AXE)
            event.getInventory().setResult(new ItemStack(Material.STONE_AXE));
		else if (event.getRecipe().getResult().getType() == Material.WOOD_SPADE)
			event.getInventory().setResult(new ItemStack(Material.STONE_SPADE));
        else if (event.getRecipe().getResult().getType() == Material.DIAMOND) {
            String CHECK_LINE = ChatColor.GRAY + "© Aperture Science - All rights reserved";
            ArrayList<String> customLore = new ArrayList<>();
            customLore.add(ChatColor.GRAY + "Aperture™ Uncrafted Companion Diamond");
            customLore.add(CHECK_LINE);

            ItemStack res = event.getInventory().getResult();
            ItemMeta meta = res.getItemMeta();
            meta.setLore(customLore);
            res.setItemMeta(meta);
            event.getInventory().setResult(res);
        }
    }

    @EventHandler (priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDamage(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player) {
            Player damaged = (Player) event.getEntity();
            Entity damager = event.getDamager();

            if (damager instanceof Player) {
                if (!game.isPvpEnabled()) {
                    event.setCancelled(true);
                    return;
                }
                Metadatas.setMetadata(damaged, "lastDamager", damager);

                if (((Player) damager).hasPotionEffect(PotionEffectType.INCREASE_DAMAGE)) {
                    event.setDamage(EntityDamageEvent.DamageModifier.MAGIC, event.getDamage(EntityDamageEvent.DamageModifier.MAGIC) / 2);
                }
            } else if (damager instanceof Projectile) {
                Projectile  arrow = (Projectile) damager;
                Entity shooter = (Entity) arrow.getShooter();
                if (shooter instanceof Player) {
                    if (!game.isPvpEnabled()) {
                        event.setCancelled(true);
                        return;
                    }
                    Metadatas.setMetadata(damaged, "lastDamager", shooter);

                    if (((Player) shooter).hasPotionEffect(PotionEffectType.INCREASE_DAMAGE)) {
                        event.setDamage(EntityDamageEvent.DamageModifier.MAGIC, event.getDamage(EntityDamageEvent.DamageModifier.MAGIC)/2);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onLogout(PlayerQuitEvent event) {
        if (game.isInGame(event.getPlayer().getUniqueId())) {
            game.stumpPlayer(event.getPlayer(), true);
            if (game.getStatus() == Status.IN_GAME) {
                Location l = event.getPlayer().getLocation();
                World w = l.getWorld();
                for (ItemStack stack : event.getPlayer().getInventory().getContents()) {
                    if (stack != null) {
                        w.dropItemNaturally(l, stack);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        if (game.isInGame(event.getEntity().getUniqueId())) {
            game.stumpPlayer(event.getEntity(), false);
            event.getDrops().add(new ItemStack(Material.GOLDEN_APPLE));
            if (event.getEntity().getKiller() != null)
                event.getEntity().getKiller().addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 20*20, 1));
            GameUtils.broadcastSound(Sound.WITHER_DEATH);
        }
        event.setDeathMessage(game.getCoherenceMachine().getGameTag()+event.getDeathMessage());
    }

	@EventHandler
	public void onBreak(BlockSpreadEvent event) {
		event.setCancelled(true);
		if (event.getBlock().getType() == Material.CACTUS) {
			event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), new ItemStack(Material.LOG, 2));
			event.getBlock().setType(Material.AIR);
		}
	}

	@EventHandler
	public void onBreak(BlockFadeEvent event) {
		event.setCancelled(true);
		if (event.getBlock().getType() == Material.CACTUS) {
			event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), new ItemStack(Material.LOG, 2));
			event.getBlock().setType(Material.AIR);
		}
	}

    @EventHandler
    public void entitySpawn(EntitySpawnEvent event) {
        EntityType entity = event.getEntityType();
        if (entity == EntityType.GUARDIAN)
            event.setCancelled(true);
    }

    @EventHandler
    public void onDeath(EntityDeathEvent event) {
        Random random = new Random();
        LivingEntity entity = event.getEntity();
        if (entity instanceof Cow) {
            List<ItemStack> newDrops = new ArrayList<>();
            for (ItemStack stack : event.getDrops()) {
                if (stack.getType() == Material.RAW_BEEF)
                    newDrops.add(new ItemStack(Material.COOKED_BEEF, stack.getAmount()*2));
                else if (stack.getType() == Material.LEATHER)
                    newDrops.add(new ItemStack(Material.LEATHER, stack.getAmount()*2));
            }
            event.getDrops().clear();
            event.getDrops().addAll(newDrops);
        } else if (entity instanceof Sheep) {
            List<ItemStack> newDrops = new ArrayList<>();
            for (ItemStack stack : event.getDrops()) {
                if (stack.getType() == Material.MUTTON)
                    newDrops.add(new ItemStack(Material.COOKED_MUTTON, stack.getAmount()*2));
            }
            if (random.nextInt(32) >= 16)
                newDrops.add(new ItemStack(Material.LEATHER, random.nextInt(5)+1));
            if (random.nextInt(32) >= 16)
                newDrops.add(new ItemStack(Material.STRING, random.nextInt(2)+1));
            event.getDrops().clear();
            event.getDrops().addAll(newDrops);
        } else if (entity instanceof Pig) {
            List<ItemStack> newDrops = new ArrayList<>();
            for (ItemStack stack : event.getDrops()) {
                if (stack.getType() == Material.PORK)
                    newDrops.add(new ItemStack(Material.GRILLED_PORK, stack.getAmount()*2));
            }
            if (random.nextInt(32) >= 16)
                newDrops.add(new ItemStack(Material.LEATHER, random.nextInt(5)+1));
            event.getDrops().clear();
            event.getDrops().addAll(newDrops);
        } else if (entity instanceof Rabbit) {
            List<ItemStack> newDrops = new ArrayList<>();
            for (ItemStack stack : event.getDrops()) {
                if (stack.getType() == Material.RABBIT)
                    newDrops.add(new ItemStack(Material.COOKED_RABBIT, stack.getAmount()*2));
            }
            event.getDrops().clear();
            event.getDrops().addAll(newDrops);
        } else if (entity instanceof Chicken) {
            List<ItemStack> newDrops = new ArrayList<>();
            for (ItemStack stack : event.getDrops()) {
                if (stack.getType() == Material.RAW_CHICKEN)
                    newDrops.add(new ItemStack(Material.COOKED_CHICKEN, stack.getAmount()*2));
                if (stack.getType() == Material.FEATHER) {
                    ItemStack loot = new ItemStack(Material.ARROW, stack.getAmount());
                    ItemMeta meta = loot.getItemMeta();
                    String CHECK_LINE = ChatColor.GRAY + "© Aperture Science - All rights reserved";
                    ArrayList<String> customLore = new ArrayList<>();
                    customLore.add(ChatColor.GRAY + "Aperture™ Companion Arrow");
                    customLore.add(CHECK_LINE);
                    meta.setLore(customLore);
                    loot.setItemMeta(meta);
                    newDrops.add(loot);
                }
            }
            event.getDrops().clear();
            event.getDrops().addAll(newDrops);
        } else if (entity instanceof Squid) {
            List<ItemStack> newDrops = new ArrayList<>();
			if (random.nextInt(32) >= 8)
				newDrops.add(new ItemStack(Material.COOKED_FISH, random.nextInt(5)+1));
            event.getDrops().clear();
            event.getDrops().addAll(newDrops);
        } else if (entity instanceof Skeleton) {
            List<ItemStack> newDrops = new ArrayList<>();
            for (ItemStack stack : event.getDrops()) {
                if (stack.getType() == Material.ARROW)
                    newDrops.add(new ItemStack(Material.ARROW, stack.getAmount()*2)) ;
                if (stack.getType() == Material.BOW) {
                    stack.setDurability((short) 0);
                    newDrops.add(stack);
                }
            }
            event.getDrops().clear();
            event.getDrops().addAll(newDrops);
        }
        event.setDroppedExp(event.getDroppedExp() * 2);
    }
}
