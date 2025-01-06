package dev.rvbsm.fsit.api.player;

public interface PlayerLastSneakTime {

    void fsit$updateLastSneakTime();

    void fsit$resetLastSneakTime();

    long fsit$getLastSneakTime();
}
