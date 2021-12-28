package com.cognitionbox.petra.examples.trafficsignalling2;

public interface PedestrianLightState {
    SafeToWalk safeToWalk();
    WalkRequested walkRequested();
    default boolean isWalk(){
        return safeToWalk().isSafe() && walkRequested().isTrue();
    }
    default boolean isDontWalk(){
        return safeToWalk().isNotSafe() || walkRequested().isFalse();
    }
}
