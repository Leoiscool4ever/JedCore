package com.jedk1.jedcore.ability.airbending;

import com.jedk1.jedcore.JedCore;
import com.jedk1.jedcore.configuration.JedCoreConfig;
import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.AirAbility;
import com.projectkorra.projectkorra.attribute.Attribute;
import com.projectkorra.projectkorra.util.ParticleEffect;

import net.jafama.FastMath;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class Meditate extends AirAbility implements AddonAbility {

	private double startHealth;

	private String unfocusMsg;
	private long warmup;
	@Attribute(Attribute.COOLDOWN)
	private long cooldown;
	@Attribute(Attribute.DURATION)
	private int boostDuration;
	private int particleDensity;
	private boolean lossFocusMessage;
	private int absorptionBoost;
	private int speedBoost;
	private int jumpBoost;

	public Meditate(Player player) {
		super(player);
		if (!bPlayer.canBend(this)) {
			return;
		}

		setFields();
		start();
	}
	
	public void setFields() {
		ConfigurationSection config = JedCoreConfig.getConfig(this.player);
		
		unfocusMsg = config.getString("Abilities.Air.Meditate.UnfocusMessage");
		lossFocusMessage = config.getBoolean("Abilities.Air.Meditate.LossFocusMessage");
		warmup = config.getLong("Abilities.Air.Meditate.ChargeTime");
		cooldown = config.getLong("Abilities.Air.Meditate.Cooldown");
		boostDuration = config.getInt("Abilities.Air.Meditate.BoostDuration");
		particleDensity = config.getInt("Abilities.Air.Meditate.ParticleDensity");
		absorptionBoost = config.getInt("Abilities.Air.Meditate.AbsorptionBoost");
		speedBoost = config.getInt("Abilities.Air.Meditate.SpeedBoost");
		jumpBoost = config.getInt("Abilities.Air.Meditate.JumpBoost");

		startHealth = player.getHealth();
	}

	@Override
	public void progress() {
		if (player.isDead() || !player.isOnline()) {
			remove();
			return;
		}
		if (!bPlayer.canBendIgnoreCooldowns(this)) {
			remove();
			return;
		}
		if (player.getHealth() < startHealth) {
			if (lossFocusMessage) {
				player.sendMessage(Element.AIR.getColor() + unfocusMsg);
			}
			remove();
			return;
		}
		if (System.currentTimeMillis() > getStartTime() + warmup) {
			ParticleEffect.SPELL_INSTANT.display(player.getLocation(), particleDensity, FastMath.random(), FastMath.random(), FastMath.random(), 0.0);
			if (!player.isSneaking()) {
				bPlayer.addCooldown(this);
				givePlayerBuffs();
				remove();
			}
		} else if (player.isSneaking()) {
			ParticleEffect.SPELL_MOB_AMBIENT.display(player.getLocation(), particleDensity, FastMath.random(), FastMath.random(), FastMath.random(), 0.0);
		} else {
			remove();
		}
	}

	private void givePlayerBuffs() {
		if (player.hasPotionEffect(PotionEffectType.SPEED)) {
			player.removePotionEffect(PotionEffectType.SPEED);
		}
		player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, boostDuration/50, speedBoost - 1));

		if (player.hasPotionEffect(PotionEffectType.JUMP)) {
			player.removePotionEffect(PotionEffectType.JUMP);
		}
		player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, boostDuration/50, jumpBoost - 1));

		if (player.hasPotionEffect(PotionEffectType.ABSORPTION)) {
			player.removePotionEffect(PotionEffectType.ABSORPTION);
		}
		player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, boostDuration/50, absorptionBoost - 1));
	}
	
	@Override
	public long getCooldown() {
		return cooldown;
	}

	@Override
	public Location getLocation() {
		return player.getLocation();
	}

	@Override
	public String getName() {
		return "Meditate";
	}

	@Override
	public boolean isHarmlessAbility() {
		return true;
	}

	@Override
	public boolean isSneakAbility() {
		return true;
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
		return "* JedCore Addon *\n" + config.getString("Abilities.Air.Meditate.Description");
	}

	public double getStartHealth() {
		return startHealth;
	}

	public void setStartHealth(double startHealth) {
		this.startHealth = startHealth;
	}

	public String getUnfocusMsg() {
		return unfocusMsg;
	}

	public void setUnfocusMsg(String unfocusMsg) {
		this.unfocusMsg = unfocusMsg;
	}

	public boolean hasUnfocusMessage() {
		return lossFocusMessage;
	}

	public void setHasUnfocusMessage(boolean hasUnfocusMessage) {
		this.lossFocusMessage = hasUnfocusMessage;
	}

	public long getWarmup() {
		return warmup;
	}

	public void setWarmup(long warmup) {
		this.warmup = warmup;
	}

	public int getBoostDuration() {
		return boostDuration;
	}

	public void setBoostDuration(int boostDuration) {
		this.boostDuration = boostDuration;
	}

	public int getParticleDensity() {
		return particleDensity;
	}

	public void setParticleDensity(int particleDensity) {
		this.particleDensity = particleDensity;
	}

	public int getAbsorptionBoost() {
		return absorptionBoost;
	}

	public void setAbsorptionBoost(int absorptionBoost) {
		this.absorptionBoost = absorptionBoost;
	}

	public int getSpeedBoost() {
		return speedBoost;
	}

	public void setSpeedBoost(int speedBoost) {
		this.speedBoost = speedBoost;
	}

	public int getJumpBoost() {
		return jumpBoost;
	}

	public void setJumpBoost(int jumpBoost) {
		this.jumpBoost = jumpBoost;
	}

	@Override
	public void load() {}

	@Override
	public void stop() {}
	
	@Override
	public boolean isEnabled() {
		ConfigurationSection config = JedCoreConfig.getConfig(this.player);
		return config.getBoolean("Abilities.Air.Meditate.Enabled");
	}
}
