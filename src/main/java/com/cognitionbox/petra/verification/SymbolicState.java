package com.cognitionbox.petra.verification;

import java.util.List;
import java.util.Set;

public final class SymbolicState {
    private final Set<List<String>> symbolicStates;
    private final boolean isForall;

    public Set<List<String>> getSymbolicStates() {
        return symbolicStates;
    }

    public boolean isForall() {
        return isForall;
    }

    public SymbolicState(Set<List<String>> symbolicStates, boolean isForall) {
        this.symbolicStates = symbolicStates;
        this.isForall = isForall;
    }
}
