package com.rs.game.player.content.ectofuntus;

import java.util.HashMap;
import java.util.Map;

/**
 * An enumeration containing the different types of bone-meals.
 * 
 * @author _Waterfiend <skype:alco-wahidi>
 *
 *         Created in Apr 29, 2017 at 10:54:22 AM.
 */
public enum Bonemeal {

	BAT(530, 4256),
	NORMAL(526, 4255),
	BIG(532, 4257),
	BABY_DRAGON(534, 4260),
	DRAGON(536, 4261),
	DAGANNOTH(6729, 6728),
	WYVERN(6812, 6810),
	OURG(4834, 4855),
	FROST_DRAGON(18830, 18834),
	AIRUT(30209, 30211);

	/**
	 * The id of the bone used in the bone-meal.
	 */
	private final int boneId;

	/**
	 * The id of the bone-meal used to worship at the {@link Ectofuntus}.
	 */
	private final int mealId;

	/**
	 * A constructor of the {@link Ectofuntus} activity.
	 * 
	 * @param boneId
	 *            The id of the bone.
	 * @param mealId
	 *            The id of the meal.
	 */
    Bonemeal(int boneId, int mealId) {
		this.boneId = boneId;
		this.mealId = mealId;
	}

	/**
	 * A hashmap including the bones of the {@link Ectofuntus}.
	 */
	private static final Map<Integer, Bonemeal> bones = new HashMap<Integer, Bonemeal>();

	/**
	 * Fetches the id of the bone.
	 * 
	 * @param boneId
	 *            The id of the bone.
	 * @return the boneId
	 */
	public static Bonemeal fetchBoneId(int boneId) {
		return boneMeal.get(boneId);
	}

	/**
	 * Gets the id of the bone used in the {@link Ectofuntus} activity.
	 * 
	 * @return the boneId
	 */
	public int getBoneId() {
		return boneId;
	}

	/**
	 * A hashmap including the bone-meal of the {@link Ectofuntus}.
	 */
	private static final Map<Integer, Bonemeal> boneMeal = new HashMap<Integer, Bonemeal>();

	/**
	 * Fetches the id of the bone-meal.
	 * 
	 * @param mealId
	 *            The id of the bone-meal.
	 * @return the mealId
	 */
	public static Bonemeal fetchMealId(int mealId) {
		return bones.get(mealId);
	}

	/**
	 * Gets the id of the bone-meal.
	 * 
	 * @return the mealId
	 */
	public int getMealId() {
		return mealId;
	}

	static {
		for (final Bonemeal meal : Bonemeal.values()) {
			boneMeal.put(meal.boneId, meal);
		}
		for (final Bonemeal bonemeal : Bonemeal.values()) {
			bones.put(bonemeal.mealId, bonemeal);
		}
	}
}