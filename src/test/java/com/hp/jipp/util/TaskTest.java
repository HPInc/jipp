package com.hp.jipp.util;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

@RunWith(MockitoJUnitRunner.class)
public class TaskTest {

    @Mock
    Runnable runnable;

    @Mock
    Task.Listener<Boolean> listener;

    private BlockingQueue<String> queue = new LinkedBlockingQueue<>();

    Task.Listener<Boolean> forwardingListener = new Task.Listener<Boolean>() {
        @Override
        public void onResult(Boolean aBoolean) {
            listener.onResult(aBoolean);
            queue.add("onResult");
        }

        @Override
        public void onDone() {
            listener.onDone();
            queue.add("onDone");
        }

        @Override
        public void onError(Throwable thrown) {
            listener.onError(thrown);
            queue.add("onError");
        }
    };

    private ExecutorService runnerService = Executors.newSingleThreadExecutor();
    private ExecutorService listenerService = Executors.newSingleThreadExecutor();
    private Task<Boolean> task;

    abstract class TestTask<R> extends Task<R> {
        public TestTask() {
            super(runnerService, listenerService);
        }
    }

    @Test(timeout = 150)
    public void demo() throws Exception {
        task = new TestTask<Boolean>() {
            @Override
            public void run() {
                queue.add("run");
            }
        }.addListener(forwardingListener).start();
        assertEquals("run", queue.take());
    }

    @Test(timeout = 150)
    public void start() throws Exception {
        task = new TestTask<Boolean>() {
            @Override
            public void run() {
                runnable.run();
                queue.add("run");
            }
        }.start(forwardingListener);
        queue.take();
        verify(runnable).run();
    }

    @Test(timeout = 150)
    public void oneResult() throws Exception {
        task = new TestTask<Boolean>() {
            @Override
            protected void run() throws Exception {
                submit(true);
            }
        }.start(forwardingListener);

        queue.take();
        verify(listener).onResult(true);
    }

    @Test(timeout = 150)
    public void cancelAfterStart() throws Exception {
        final CountDownLatch runningLatch = new CountDownLatch(1);

        task = new TestTask<Boolean>() {
            @Override
            public void run() throws Exception {
                runningLatch.countDown();
                Thread.sleep(150);
                submit(true);
            }
        }.start(forwardingListener);
        runningLatch.await();
        task.cancel();
        queue.take();
        verify(listener, times(1)).onError(any(CancellationException.class));
    }

    @Test(timeout = 150)
    public void cancelBeforeStart() throws Exception {
        task = new TestTask<Boolean>() {
            @Override
            public void run() throws Exception {
                Thread.sleep(150);
                submit(true);
            }
        }.start(forwardingListener);
        task.cancel();
        queue.take();
        verify(listener, times(1)).onError(any(CancellationException.class));
    }

    @Test(timeout = 150)
    public void ignoreResultAfterCancel() throws Exception {
        final CountDownLatch runningLatch = new CountDownLatch(1);
        final CountDownLatch cancelLatch = new CountDownLatch(1);
        task = new TestTask<Boolean>() {
            @Override
            public void run() throws Exception {
                runningLatch.countDown();
                try {
                    cancelLatch.await();
                } catch (InterruptedException ignore) { }
            }
        }.start(forwardingListener);

        runningLatch.await();

        // Cancel and tell task to try to submit results
        task.cancel();
        cancelLatch.countDown();

        // Make sure we do not get results, only cancel
        queue.take();
        verify(listener, never()).onResult(true);
        verify(listener, times(1)).onError(any(CancellationException.class));
    }

    @Test(timeout = 150)
    public void done() throws Exception {
        task = new TestTask<Boolean>() {
            @Override
            public void run() throws Exception {
            }
        }.start(forwardingListener);

        queue.take();
        verify(listener).onDone();
    }

    @Test(timeout = 150)
    public void cancelAfterDone() throws Exception {
        task = new TestTask<Boolean>() {
            @Override
            public void run() throws Exception {
            }
        }.start(forwardingListener);

        queue.take();
        task.cancel();
        verify(listener).onDone();
        verify(listener, never()).onError(any(Throwable.class));
    }

    @Test(timeout = 150)
    public void unlisten() throws Exception {
        task = new TestTask<Boolean>() {
            @Override
            public void run() throws Exception {
                submit(true);
            }
        }.addListener(listener).removeListener(listener).start(forwardingListener);

        queue.take();
        // Only one signal
        verify(listener).onResult(true);
    }

    @Test(timeout = 150)
    public void multiListen() throws Exception {
        task = new TestTask<Boolean>() {
            @Override
            public void run() throws Exception {
                submit(true);
            }
        }.addListener(forwardingListener).start(forwardingListener);

        queue.take();
        queue.take();
        verify(listener, times(2)).onResult(true);
    }
}
