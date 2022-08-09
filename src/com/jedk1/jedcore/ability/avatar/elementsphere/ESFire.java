package com.jedk1.jedcore.ability.avatar.elementsphere;

import com.jedk1.jedcore.JedCore;
import com.jedk1.jedcore.configuration.JedCoreConfig;
import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.Element.SubElement;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.AvatarAbility;
import com.projectkorra.projectkorra.ability.BlueFireAbility;
import com.projectkorra.projectkorra.ability.FireAbility;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.command.Commands;
import com.projectkorra.projectkorra.firebending.BlazeArc;
import com.projectkorra.projectkorra.util.DamageHandler;
import com.projectkorra.projectkorra.util.ParticleEffect;

import net.jafama.FastMath;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class ESFire extends AvatarAbility implements AddonAbility {

	private Location location;
	private Vector direction;
	private double travelled;

	@Attribute(Attribute.COOLDOWN)
	private long cooldown;
	@Attribute(Attribute.RANGE)
	private double range;
	@Attribute(Attribute.DAMAGE)
	private double damage;
	@Attribute(Attribute.FIRE_TICK)
	private long burnTime;
	@Attribute(Attribute.SPEED)
	private int speed;
	private boolean controllable;

	public ESFire(Player player) {
		super(player);
		if (!hasAbility(player, ElementSphere.class)) {
			return;
		}
		ElementSphere currES = getAbility(player, ElementSphere.class);
		if (currES.getFireUses() == 0) {
			return;
		}
		if (bPlayer.isOnCooldown("ESFire")) {
			return;
		}
		if (GeneralMethods.isRegionProtectedFromBuild(this, player.getTargetBlock(getTransparentMaterialSet(), (int) range).getLocation())) {
			return;
		}
		setFields();
		start();
		if (!isRemoved()) {
			bPlayer.addCooldown("ESFire", getCooldown());
			currES.setFireUses(currES.getFireUses() - 1);
			location = player.getEyeLocation().clone().add(player.getEyeLocation().getDirection().multiply(1));
			direction = location.getDirection().clone();
		}
	}
	
	public void setFields() {
		ConfigurationSection config = JedCoreConfig.getConfig(this.player);
		
		cooldown = config.getLong("Abilities.Avatar.ElementSphere.Fire.Cooldown");
		range = config.getDouble("Abilities.Avatar.ElementSphere.Fire.Range");
		damage = config.getDouble("Abilities.Avatar.ElementSphere.Fire.Damage");
		burnTime = config.getLong("Abilities.Avatar.ElementSphere.Fire.BurnDuration");
		speed = config.getInt("Abilities.Avatar.ElementSphere.Fire.Speed");
		controllable = config.getBoolean("Abilities.Avatar.ElementSphere.Fire.Controllable");
		
		applyModifiers();
	}
	
	private void applyModifiers() {
		if (bPlayer.canUseSubElement(SubElement.BLUE_FIRE)) {
			cooldown *= BlueFireAbility.getCooldownFactor();
			range *= BlueFireAbility.getRangeFactor();
			damage *= BlueFireAbility.getDamageFactor();
		}
	}

	@Override
	public void progress() {
		if (player.isDead() || !player.isOnline()) {
			remove();
			return;
		}
		if (travelled >= range) {
			remove();
			return;
		}
		advanceAttack();
	}

	private void advanceAttack() {
		for (int i = 0; i < speed; i++) {
			travelled++;
			if (travelled >= range)
				return;

			if (!player.isDead() && controllable)
				direction = GeneralMethods.getDirection(player.getLocation(), GeneralMethods.getTargetedLocation(player, range, Material.WATER)).normalize();

			location = location.add(direction.clone().multiply(1));
			if (GeneralMethods.isRegionProtectedFromBuild(this, location)) {
				travelled = range;
				return;
			}
			if (GeneralMethods.isSolid(location.getBlock()) || isWater(location.getBlock())) {
				travelled = range;
				return;
			}

			ParticleEffect flame = bPlayer.hasSubElement(Element.BLUE_FIRE) ? ParticleEffect.SOUL_FIRE_FLAME : ParticleEffect.FLAME;
			flame.display(location, 5, FastMath.random(), FastMath.random(), FastMath.random(), 0.02);
			ParticleEffect.SMOKE_LARGE.display(location, 2, FastMath.random(), FastMath.random(), FastMath.random(), 0.01);
			FireAbility.playFirebendingSound(location);

			placeFire();

			for (Entity entity : GeneralMethods.getEntitiesAroundPoint(location, 2.5)) {
				if (entity instanceof LivingEntity && entity.getEntityId() != player.getEntityId() && !(entity instanceof ArmorStand) && !GeneralMethods.isRegionProtectedFromBuild(this, entity.getLocation()) && !((entity instanceof Player) && Commands.invincible.contains(((Player) entity).getName()))) {
					DamageHandler.damageEntity(entity, damage, this);
					entity.setFireTicks(FastMath.round(burnTime / 50F));
					travelled = range;
				}
			}
		}
	}

	private void placeFire() {
		if (GeneralMethods.isSolid(location.getBlock().getRelative(BlockFace.DOWN))) {
			location.getBlock().setType(Material.FIRE);
			new BlazeArc(player, location, direction, 2);
		}
	}
	
	@Override
	public long getCooldown() {
		return cooldown;
	}

	@Override
	public Location getLocation() {
		return location;
	}

	@Override
	public String getName() {
		return "ElementSphere Fire";
	}
	
	@Override
	public boolean isHiddenAbility() {
		return true;
	}

	@Override
	public boolean isHarmlessAbility() {
		return false;
	}

	@Override
	public boolean isSneakAbility() {
		return false;
	}

	@Override
	public String getAuthor() {
		return JedCore.dev;
	}

	@Override
	public String getVersion() {
		return JedCore.version;
	}

	@Override
	public String getDescription() {
		return null;
	}

	public Vector getDirection() {
		return direction;
	}

	public void setDirection(Vector direction) {
		this.direction = direction;
	}

	public double getDistanceTravelled() {
		return travelled;
	}

	public void setDistanceTravelled(double travelled) {
		this.travelled = travelled;
	}

	public double getRange() {
		return range;
	}

	public void setRange(double range) {
		this.range = range;
	}

	public double getDamage() {
		return damage;
	}

	public void setDamage(double damage) {
		this.damage = damage;
	}

	public long getBurnTime() {
		return burnTime;
	}

	public void setBurnTime(long burnTime) {
		this.burnTime = burnTime;
	}

	public int getSpeed() {
		return speed;
	}

	public void setSpeed(int speed) {
		this.speed = speed;
	}

	public boolean isControllable() {
		return controllable;
	}

	public void setControllable(boolean controllable) {
		this.controllable = controllable;
	}

	@Override
	public void load() {}

	@Override
	public void stop() {}
	
	@Override
	public boolean isEnabled() {
		ConfigurationSection config = JedCoreConfig.getConfig(this.player);
		return config.getBoolean("Abilities.Avatar.ElementSphere.Enabled");
	}
}
