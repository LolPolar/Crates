package me.polar.crates.util;

import me.polar.crates.data.CrateReward;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class RewardUtils {
    public static CrateReward getRandomReward(List<CrateReward> list) {
        double count = list.stream().mapToDouble(CrateReward::getChance).sum();
        double rand = ThreadLocalRandom.current().nextDouble(0.0, count);
        for (CrateReward reward : list) {
            if (!((rand -= reward.getChance()) <= 0.0)) continue;
            return reward;
        }
        return list.get(list.size() - 1);
    }

    public static double getPercentageCount(List<CrateReward> list) {
        return list.stream().mapToDouble(CrateReward::getChance).sum();
    }
}