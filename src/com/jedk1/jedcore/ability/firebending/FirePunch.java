package com.jedk1.jedcore.ability.firebending;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import com.jedk1.jedcore.JedCore;
import com.jedk1.jedcore.configuration.JedCoreConfig;
import com.jedk1.jedcore.util.FireTick;
import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.BlueFireAbility;
import com.projectkorra.projectkorra.ability.FireAbility;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.firebending.util.FireDamageTimer;
import com.projectkorra.projectkorra.util.DamageHandler;
import com.projectkorra.projectkorra.util.ParticleEffect;

public class FirePunch extends FireAbility implements AddonAbility {

	@Attribute(Attribute.COOLDOWN)
	private long cooldown;
	@Attribute(Attribute.DAMAGE)
	private double damage;
	@Attribute(Attribute.FIRE_TICK)
	private int fireTicks;

	private Location location;
	
	public FirePunch(Player player) {
		super(player);

		setFields();

		start();
	}

	private void setFields() {
		ConfigurationSection config = JedCoreConfig.getConfig(player);

		cooldown = config.getLong("Abilities.Fire.FirePunch.Cooldown");
		damage = config.getDouble("Abilities.Fire.FirePunch.Damage");
		fireTicks = config.getInt("Abilities.Fire.FirePunch.FireTicks");

		applyModifiers();
	}

	private void applyModifiers() {
		if (bPlayer.canUseSubElement(Element.BLUE_FIRE)) {
			cooldown *= BlueFireAbility.getCooldownFactor();
			damage *= BlueFireAbility.getDamageFactor();
		}

		if (isDay(player.getWorld())) {
			cooldown -= ((long) getDayFactor(cooldown) - cooldown);
			damage = getDayFactor(damage);
		}
	}

	@Override
	public void progress() {
		if (!player.isOnline() || player.isDead() || !bPlayer.canBend(this)) {
			remove();
			return;
		}

		location = GeneralMethods.getRightSide(player.getLocation(), 0.55)
				.add(0, 1.2, 0)
				.add(player.getLocation().getDirection().multiply(0.8));
		playFirebendingParticles(location, 3, 0, 0, 0);
		ParticleEffect.SMOKE_NORMAL.display(location, 1);
	}

	public void punch(LivingEntity target) {
		remove();
		DamageHandler.damageEntity(target, damage, this);
		FireTick.set(target, fireTicks / 50);
		if (cooldown > fireTicks) {
			new FireDamageTimer(target, player);
		}
		bPlayer.addCooldown(this);
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
		return "FirePunch";
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
		ConfigurationSection config = JedCoreConfig.getConfig(this.player);
		return "* JedCore Addon *\n" + config.getString("Abilities.Fire.FirePunch.Description");
	}

	public void setCooldown(long cooldown) {
		this.cooldown = cooldown;
	}

	public double getDamage() {
		return damage;
	}

	public void setDamage(double damage) {
		this.damage = damage;
	}

	public int getFireTicks() {
		return fireTicks;
	}

	public void setFireTicks(int fireTicks) {
		this.fireTicks = fireTicks;
	}

	@Override
	public void load() {}

	@Override
	public void stop() {}

	@Override
	public boolean isEnabled() {
		ConfigurationSection config = JedCoreConfig.getConfig(this.player);
		return config.getBoolean("Abilities.Fire.FirePunch.Enabled");
	}
}
