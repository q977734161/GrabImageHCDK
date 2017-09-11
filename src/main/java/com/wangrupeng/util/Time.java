package com.wangrupeng.util;

/**
 * Created by WangRupeng on 2017/8/17 0017.
 */

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class Time {
	private static AtomicBoolean simulating = new AtomicBoolean(false);
	private static volatile Map<Thread, AtomicLong> threadSleepTimes;
	private static final Object sleepTimesLock = new Object();
	private static AtomicLong simulatedCurrTimeMs;

	public Time() {
	}

	public static void startSimulating() {
		Object var0 = sleepTimesLock;
		synchronized(sleepTimesLock) {
			simulating.set(true);
			simulatedCurrTimeMs = new AtomicLong(0L);
			threadSleepTimes = new ConcurrentHashMap();
		}
	}

	public static void stopSimulating() {
		Object var0 = sleepTimesLock;
		synchronized(sleepTimesLock) {
			simulating.set(false);
			threadSleepTimes = null;
		}
	}

	public static boolean isSimulating() {
		return simulating.get();
	}

	public static void sleepUntil(long targetTimeMs) throws InterruptedException {
		if(simulating.get()) {
			boolean var14 = false;

			Object var2;
			try {
				var14 = true;
				var2 = sleepTimesLock;
				synchronized(sleepTimesLock) {
					threadSleepTimes.put(Thread.currentThread(), new AtomicLong(targetTimeMs));
				}

				while(simulatedCurrTimeMs.get() < targetTimeMs) {
					Thread.sleep(10L);
				}

				var14 = false;
			} finally {
				if(var14) {
					Object var6 = sleepTimesLock;
					synchronized(sleepTimesLock) {
						if(simulating.get()) {
							threadSleepTimes.remove(Thread.currentThread());
						}

					}
				}
			}

			var2 = sleepTimesLock;
			synchronized(sleepTimesLock) {
				if(simulating.get()) {
					threadSleepTimes.remove(Thread.currentThread());
				}
			}
		} else {
			long sleepTime = targetTimeMs - currentTimeMillis();
			if(sleepTime > 0L) {
				Thread.sleep(sleepTime);
			}
		}

	}

	public static void sleep(long ms) throws InterruptedException {
		sleepUntil(currentTimeMillis() + ms);
	}

	public static long currentTimeMillis() {
		return simulating.get()?simulatedCurrTimeMs.get():System.currentTimeMillis();
	}

	public static int currentTimeSecs() {
		return (int)(currentTimeMillis() / 1000L);
	}

	public static void advanceTime(long ms) {
		if(!simulating.get()) {
			throw new IllegalStateException("Cannot simulate time unless in simulation mode");
		} else {
			simulatedCurrTimeMs.set(simulatedCurrTimeMs.get() + ms);
		}
	}

	public static boolean isThreadWaiting(Thread t) {
		if(!simulating.get()) {
			throw new IllegalStateException("Must be in simulation mode");
		} else {
			Object var1 = sleepTimesLock;
			AtomicLong time;
			synchronized(sleepTimesLock) {
				time = (AtomicLong)threadSleepTimes.get(t);
			}

			return !t.isAlive() || time != null && currentTimeMillis() < time.longValue();
		}
	}
}
