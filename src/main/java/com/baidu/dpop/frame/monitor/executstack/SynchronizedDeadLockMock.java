package com.baidu.dpop.frame.monitor.executstack;

/**
 * 
 * @author huhailiang
 */
public class SynchronizedDeadLockMock implements Runnable {

    private static byte[] lockFrist = new byte[] {};
    private static byte[] lockSecond = new byte[] {};

    private int lockIndex = 1;

    private SynchronizedDeadLockMock(int lockIndex) {
        this.lockIndex = lockIndex;
    }

    public static void startMock() {
        Thread t1 = new Thread(new SynchronizedDeadLockMock(1));
        Thread t2 = new Thread(new SynchronizedDeadLockMock(2));
        t1.start();
        t2.start();
    }

    @Override
    public void run() {
        byte[] lock01 = lockFrist;
        byte[] lock02 = lockSecond;
        if (lockIndex != 1) {
            lock01 = lockSecond;
            lock02 = lockFrist;
        }
        synchronized (lock01) {
            try {
                Thread.sleep(80);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            synchronized (lock02) {
                System.out.println(String.format("lockIndex[%d]", lockIndex));
            }
        }
    }

}
