package lab03.all;

import java.util.concurrent.locks.*;

public final class LockCounter extends Counter {
	
	private Lock lock;
	
	public LockCounter() {
		super();
		this.lock = new ReentrantLock(true);
	}
	
	@Override
	public void increment() {
		try {
			this.lock.lock();
			super.increment();
		} finally {
			this.lock.unlock();
		}
	}
	
	@Override
	public int get() {
		int res = 0;
		try {
			this.lock.lock();
			res = super.get();
		} finally {
			this.lock.unlock();
		}
		return res;
	}
}