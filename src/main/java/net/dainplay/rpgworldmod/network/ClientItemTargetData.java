package net.dainplay.rpgworldmod.network;

import net.minecraft.world.entity.item.ItemEntity;

import java.util.ArrayList;
import java.util.List;

public class ClientItemTargetData {
	private static final List<ItemEntity> targets = new ArrayList<>();
	private static ItemEntity currentTarget;

	public static void set(ItemEntity target) {
		currentTarget = target;
	}

	public static ItemEntity get() {
		return currentTarget;
	}

	/**
	 * Добавляет предмет в список целей.
	 */
	public static void addTarget(ItemEntity target) {
		targets.add(target);
	}

	/**
	 * Удаляет предмет из списка.
	 */
	public static void removeTarget(ItemEntity target) {
		targets.remove(target);
	}

	/**
	 * Возвращает весь список предметов-целей (для чтения).
	 */
	public static List<ItemEntity> getTargets() {
		return targets;
	}

	/**
	 * Очищает список.
	 */
	public static void clear() {
		targets.clear();
	}

	/**
	 * Проверяет, содержится ли предмет в списке.
	 */
	public static boolean contains(ItemEntity target) {
		return targets.contains(target);
	}

	/**
	 * Возвращает количество предметов в списке.
	 */
	public static int size() {
		return targets.size();
	}
}