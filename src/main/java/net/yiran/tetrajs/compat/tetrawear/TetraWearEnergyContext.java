package net.yiran.tetrajs.compat.tetrawear;

public final class TetraWearEnergyContext {
    private static final ThreadLocal<String> REASON = new ThreadLocal<>();

    private TetraWearEnergyContext() {
    }

    public static void run(String reason, Runnable runnable) {
        if (REASON.get() != null) {
            runnable.run();
            return;
        }
        REASON.set(reason);
        try {
            runnable.run();
        } finally {
            REASON.remove();
        }
    }

    public static String getReason() {
        String reason = REASON.get();
        return reason == null ? "unknown" : reason;
    }
}
