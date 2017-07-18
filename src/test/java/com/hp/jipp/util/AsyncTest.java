package com.hp.jipp.util;

import org.jetbrains.annotations.NotNull;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;

import kotlin.Unit;

import static org.junit.Assert.*;

public class AsyncTest {
    private final static long DELAY = 500;

    private final Exception boom = new RuntimeException("ha");
    private final MyAsyncListener listener = new MyAsyncListener();
    private Async<Integer> async;

    private static class MyAsyncListener implements Async.Listener<Integer> {
        Integer value;
        Throwable error;
        final CountDownLatch latch = new CountDownLatch(1);
        @Override
        public void onSuccess(Integer value) {
            this.value = value;
            latch.countDown();
        }

        @Override
        public void onError(@NotNull Throwable thrown) {
            this.error = thrown;
            latch.countDown();
        }

        void await() {
            try {
                latch.await();
            } catch (InterruptedException ignored) {
            }
        }
    }

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @After
    public void tearDown() throws Exception {
        if (async != null) async.cancel();
    }

    @Test
    public void background() throws Exception {
        async = Async.work(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                Thread.sleep(5);
                return 5;
            }
        });
        async.onDone(listener);

        // Get the value and ensure everything is in the right state
        assertEquals(Integer.valueOf(5), async.get(25));
        assertTrue(async.isComplete());
        assertTrue(async.isValue());
        assertEquals(Integer.valueOf(5), async.getValue());
        assertFalse(async.isError());
        assertNull(async.getError());

        listener.await();
        assertNull(listener.error);
        assertEquals(Integer.valueOf(5), listener.value);
    }

    @Test
    public void incomplete() throws Exception {
        async = Async.work(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                Thread.sleep(500);
                return 5;
            }
        });
        assertFalse(async.isComplete());
        assertFalse(async.isError());
        assertFalse(async.isValue());

    }
    @Test
    public void error() throws Exception {
        async = Async.work(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                throw boom;
            }
        });
        async.onDone(listener);
        assertNull(async.await(50));
        assertTrue(async.isComplete());
        assertFalse(async.isValue());
        assertNull(async.getValue());
        assertTrue(async.isError());
        assertEquals(boom, async.getError());

        listener.await();
        assertNull(listener.value);
        assertEquals(boom, listener.error);
    }

    @Test
    public void cancel() throws Exception {
        async = Async.work(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                // May be cancelled before we are even scheduled
                Thread.sleep(5000);
                return 5;
            }
        });
        async.cancel();
        async.await(50);
        assertTrue(async.isError());
    }

    @Test
    public void get() throws Exception {
        async = Async.work(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                // May be cancelled before we are even scheduled
                Thread.sleep(50);
                return 5;
            }
        });
        assertNull(async.getValue());
        assertNull(async.getError());
    }

    @Test
    public void getNotDone() throws Exception {
        async = Async.work(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                // May be cancelled before we are even scheduled
                Thread.sleep(500);
                return 5;
            }
        });
        assertNull(async.get(10));
    }

    @Test
    public void lateCancel() throws Exception {
        async = Async.work(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                Thread.sleep(5000);
                return 5;
            }
        });
        // Let future start up
        Thread.sleep(10);
        async.cancel();
        async.await(50);
        assertTrue(async.isError());
    }

    @Test
    public void map() throws Exception {
        async = Async.work(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                return 5;
            }
        });

        Async<Integer> async2 = async.map(new Async.Mapper<Integer, Integer>() {
            @Override
            public Integer map(Integer integer) {
                return integer + 1;
            }
        });

        assertEquals(Integer.valueOf(6), async2.get(500));
    }

    @Test
    public void cancelMap() throws Exception {
        async = Async.work(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                Thread.sleep(10);
                return 5;
            }
        });

        Async<Integer> async2 = async.map(new Async.Mapper<Integer, Integer>() {
            @Override
            public Integer map(Integer integer) {
                return integer + 1;
            }
        });

        async2.cancel();
        assertEquals(null, async2.await(DELAY));
        assertTrue(async2.isError());
        assertEquals(Integer.valueOf(5), async.await(DELAY));
    }

    @Test
    public void cancelMapFirst() throws Exception {
        async = Async.work(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                Thread.sleep(5000);
                return 5;
            }
        });

        Async<Integer> async2 = async.map(new Async.Mapper<Integer, Integer>() {
            @Override
            public Integer map(Integer integer) {
                return integer + 1;
            }
        });

        async.cancel();
        assertEquals(null, async2.await(DELAY));
        assertTrue(async2.isError());
    }

    @Test
    public void cancelMapLate() throws Exception {
        async = Async.work(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                Thread.sleep(20);
                return 5;
            }
        });

        Async<Integer> async2 = async.map(new Async.Mapper<Integer, Integer>() {
            @Override
            public Integer map(Integer integer) {
                try { Thread.sleep(20); } catch (Exception e) { throw new RuntimeException(e); }
                return integer + 1;
            }
        });

        Thread.sleep(25);
        async2.cancel();
        assertEquals(null, async2.await(DELAY));
        assertTrue(async2.isError());
    }

    @Test
    public void flatMap() throws Exception {
        async = Async.work(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                Thread.sleep(10);
                return 5;
            }
        });

        Async<Integer> async2 = async.flatMap(new Async.Mapper<Integer, Async<Integer>>() {
            @Override
            public Async<Integer> map(final Integer integer) {
                return Async.work(new Callable<Integer>() {
                    @Override
                    public Integer call() throws Exception {
                        return integer + 1;
                    }
                });
            }
        });

        assertEquals(Integer.valueOf(6), async2.await(DELAY));
    }

    @Test
    public void flatMapException() throws Exception {
        async = Async.work(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                return 5;
            }
        });

        Async<Integer> async2 = async.flatMap(new Async.Mapper<Integer, Async<Integer>>() {
            @Override
            public Async<Integer> map(final Integer integer) throws Exception {
                throw boom;
            }
        });

        assertNull(async2.await(100));
        assertEquals(boom, async2.getError());
    }

    @Test
    public void recover() throws Exception {
        async = Async.work(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                throw boom;
            }
        }).recover(new Async.Mapper<Throwable, Integer>() {
            @Override
            public Integer map(Throwable from) throws Throwable {
                return 5;
            }
        });
        assertEquals(Integer.valueOf(5), async.await(DELAY));
    }

    @Test
    public void delay() throws Exception {
        async = Async.delay(150, new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                return 5;
            }
        });
        Thread.sleep(10);
        assertFalse(async.isComplete());
    }

    @Test
    public void delayRunnable() throws Exception {
        Async<Unit> async = Async.delay(150, new Runnable() {
            @Override
            public void run() {

            }
        });
        Thread.sleep(10);
        assertFalse(async.isComplete());
    }

    @Test
    public void runRunnable() throws Exception {
        Async<Unit> async = Async.run(new Runnable() {
            @Override
            public void run() {
            }
        });
        assertEquals(Unit.INSTANCE, async.get(DELAY));
    }

    @Test
    public void cancelDelay() throws Exception {
        async = Async.<Integer>success(5).delay(50).onDone(listener);
        async.cancel();
        Thread.sleep(70);
        assertNull(listener.value);
    }


    @Test
    public void cancelSlowDelay() throws Exception {
        async = Async.work(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                Thread.sleep(15);
                return 5;
            }
        }).delay(50).onDone(listener);
        Thread.sleep(50);
        async.cancel(); // Cancel after result obtained but before delay is over
        Thread.sleep(50);
        assertNull(listener.value);
    }

    @Test
    public void errorDelay() throws Exception {
        async = Async.<Integer>error(boom).delay(50).onDone(listener);
        listener.await();
        assertEquals(boom, listener.error);
    }

    @Test
    public void onSuccess() {
        async = Async.work(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                return 4;
            }
        }).onSuccess(listener);
        listener.await();
        assertEquals(Integer.valueOf(4), listener.value);
    }

    @Test
    public void onSuccessError() throws Exception {
        async = Async.work(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                throw boom;
            }
        }).onSuccess(listener);
        async.await(DELAY);
        Thread.sleep(25);
        assertNull(null, listener.error);
        assertNull(null, listener.value);
    }

    @Test
    public void onError() {
        async = Async.work(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                throw boom;
            }
        }).onError(listener);
        listener.await();
        assertEquals(boom, listener.error);
    }

    @Test
    public void onErrorSuccess() throws Exception {
        async = Async.work(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                return 5;
            }
        }).onError(listener);
        async.await(DELAY);
        Thread.sleep(25);
        assertNull(null, listener.error);
        assertNull(null, listener.value);
    }

    @Test
    public void timeout() throws Exception {
        async = Async.<Integer>success(5).delay(DELAY);
        async.timeout(5);
        async.await(DELAY);
        assertTrue(async.isComplete());
        assertTrue(async.isError());
    }

    @Test
    public void settable() throws Exception {
        SettableAsync<Integer> settableAsync = new SettableAsync<>();
        settableAsync.onDone(listener);
        settableAsync.setSuccess(5);
        listener.await();
        assertEquals(Integer.valueOf(5), listener.value);
    }

    @Test
    public void settableError() throws Exception {
        SettableAsync<Integer> settableAsync = new SettableAsync<>();
        settableAsync.onDone(listener);
        settableAsync.setError(boom);
        listener.await();
        assertEquals(boom, listener.error);
    }

}
