package org.example.concurrency.chachefrombook;

import lombok.Data;
import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.Immutable;
import net.jcip.annotations.ThreadSafe;

@ThreadSafe
public class T {

    @GuardedBy("this")
    private String some;

    public String getSome() {
        return some;
    }

    public void setSome(String some) {
        this.some = some;
    }
}
