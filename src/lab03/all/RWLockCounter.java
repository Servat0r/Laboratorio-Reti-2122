package lab03.all;

import java.util.concurrent.locks.*;

public final class RWLockCounter extends Counter {
	
	private ReadWriteLock lock;
	
	public RWLockCounter() {
		super();
		this.lock = new ReentrantReadWriteLock(true);
	}
	
	@Override
	public void increment() {
		try {
			this.lock.writeLock().lock();
			super.increment();
		} finally {
			this.lock.writeLock().unlock();
		}
	}
	
	@Override
	public int get() {
		int res = 0;
		try {
			this.lock.readLock().lock();
			res = super.get();
		} finally {
			this.lock.readLock().unlock();
		}
		return res;
	}
}