package k.t.cameraxsample.utils

import java.util.concurrent.Executor
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Wraps an existing executor to provide a {@link #shutdown} method that allows subsequent
 * cancellation of submitted runnables.
 */
class ScopedExecutor(private val executor: Executor) : Executor {
    private val shutdown = AtomicBoolean()

    override fun execute(command: Runnable?) {
        // Return early if this object has been shut down
        if (shutdown.get()) {
            return
        }

        executor.execute {
            if (!shutdown.get()) {
                command?.run()
            }
        }
    }

    /**
     * After this method is called, no runnables that have been submitted or are subsequently
     * submitted will start to execute, turning this executor into a no-op.
     *
     * <p>Runnables that have already started to execute will continue.
     */
    fun shutdown() {
        shutdown.set(true)
    }
}