package net.dainplay.rpgworldmod.network;

import net.dainplay.rpgworldmod.sounds.GuardianAttackSoundManager;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class ClientGuardianAttackData {
	private static final Map<Integer, AttackData> playerAttackData = new HashMap<>();

	public static void handle(int playerId, int targetId, int attackTime, boolean active, boolean damageDealt) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.level == null) return;

		if (active) {
			LivingEntity target = (LivingEntity) mc.level.getEntity(targetId);
			Player attacker = (Player) mc.level.getEntity(playerId);
			if (attacker == null || target == null) return;
			playerAttackData.put(playerId, new AttackData(attacker, target, attackTime));
			GuardianAttackSoundManager.startOrUpdate(playerId, attackTime, target != null);
		} else {
			if (damageDealt && mc.player != null && mc.player.getId() == playerId) {
				mc.player.swing(mc.player.getUsedItemHand());
			}
			playerAttackData.remove(playerId);
			GuardianAttackSoundManager.stop(playerId);
		}
	}

	@Nullable
	public static AttackData getForPlayer(int playerId) {
		return playerAttackData.get(playerId);
	}

	public static Map<Integer, AttackData> getAll() {
		return playerAttackData;
	}

	public static class AttackData {
		public final Player attacker;
		public final LivingEntity target;
		public final int attackTime;

		public AttackData(Player attacker, LivingEntity target, int attackTime) {
			this.attacker = attacker;
			this.target = target;
			this.attackTime = attackTime;
		}
	}
}