package com.coding.fullstack.search;

import java.util.concurrent.*;

import org.junit.jupiter.api.Test;

public class ThreadTests {

    @Test
    public void testThread() {
        Thread t = new Thread(() -> {
            System.out.println("当前线程：" + Thread.currentThread().getId());
            int i = 10 / 2;
            System.out.println("运行结果 = " + i);
        });
        t.start();
    }

    @Test
    public void testRunnable() {
        Runnable r = () -> {
            System.out.println("当前线程：" + Thread.currentThread().getId());
            int i = 10 / 2;
            System.out.println("运行结果 = " + i);
        };
        new Thread(r).start();
    }

    @Test
    public void testFutureTask() throws ExecutionException, InterruptedException {
        FutureTask<Integer> futureTask = new FutureTask<>(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                System.out.println("当前线程：" + Thread.currentThread().getId());
                int i = 10 / 2;
                System.out.println("运行结果 = " + i);
                return i;
            }
        });
        new Thread(futureTask).start();
        System.out.println(futureTask.get());
    }

    @Test
    public void testExecutors() {
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        executorService.execute(() -> {
            System.out.println("当前线程：" + Thread.currentThread().getId());
            int i = 10 / 2;
            System.out.println("运行结果 = " + i);
        });
    }

    // @formatter:off
    /**
     * @corePoolSize - 核心线程数[一致存在，除非设置allowCoreThreadTimeOut]；线程池，创建好以后就准备就绪的线程数量。
     * @maximumPoolSize - 最大线程数；控制资源。
     * @keepAliveTime - 存活时间；如果当前的线程数量大于核心数量，空闲线程最大等待而不必被回收的时间。
     * @unit - 时间单位
     * @workQueue - 阻塞队列。如果任务有很多，就会将目前多的任务放在队列里面。只要有线程空闲，就会去队列里面取出新的任务继续执行。
     * @threadFactory - 线程的创建工厂
     * @handler - 队列满了，按照策略执行任务
     *
     * 工作顺序：
     * 1、线程池创建，准备好core数量的核心线程，准备接受任务。
     * 1.1、core满了，就会把新进来的任务放入阻塞队列队列中。空闲的core就会自己去阻塞队列获取任务执行。
     * 1.2、如果队列满了，就会创建非核心线程，最大不能超过max。
     * 1.3、如果队列也满了，且没有空闲的线程，就会使用handler处理。
     * 1.4、如果线程池中的线程数量大于core，如果线程处于空闲状态，超过keepAliveTime，就会回收。
     *      LinkedBlockingDeque 默认 Integer.MAX_VALUE 个元素！！！
     */
    // @formatter:on
    @Test
    public void testThreadPoolExecutor() {
        ThreadPoolExecutor threadPoolExecutor =
            new ThreadPoolExecutor(10, 10, 1000, TimeUnit.MILLISECONDS, new LinkedBlockingDeque<Runnable>(100),
                Executors.defaultThreadFactory(), new ThreadPoolExecutor.AbortPolicy());
        //
        Executors.newCachedThreadPool(); // core是0，所有都可回收
        Executors.newFixedThreadPool(10); // 固定大小，core=max；都不可回收
        Executors.newScheduledThreadPool(10); // 定时任务的线程池
        Executors.newSingleThreadExecutor(); // 单线程的线程池，后台从队列里面获取任务，挨个执行
        System.out.println("主线程：" + Thread.currentThread().getId() + " stop");
    }

    public static ExecutorService executor = Executors.newFixedThreadPool(10);

    // ==================================================华丽的分割线==================================================

    // 无返回值 计算完成时回调方法
    @Test
    public void testCompletableFuture_run_whenComplete_exceptionally() {
        System.out.println("主线程：" + Thread.currentThread().getId() + " start");
        CompletableFuture<Void> completableFuture = CompletableFuture.runAsync(() -> {
            System.out.println("当前线程：" + Thread.currentThread().getId());
            int i = 10 / 2;
            System.out.println("运行结果 = " + i);
        }, executor).whenComplete((result, exception) -> {
            System.out.println("异步任务执行完成，result = " + result + ", exception = " + exception);
        }).exceptionally(throwable -> {
            System.out.println("异常处理");
            return null;
        });
        System.out.println("主线程：" + Thread.currentThread().getId() + " stop");
    }

    // 有返回值 计算完成时回调方法
    @Test
    public void testCompletableFuture_supply_whenComplete_exceptionally()
        throws ExecutionException, InterruptedException {
        System.out.println("主线程：" + Thread.currentThread().getId() + " start");
        CompletableFuture<Integer> completableFuture = CompletableFuture.supplyAsync(() -> {
            System.out.println("当前线程：" + Thread.currentThread().getId());
            int i = 10 / 0;
            System.out.println("运行结果 = " + i);
            return i;
        }, executor).whenComplete((result, exception) -> {
            System.out.println("异步任务执行完成，result = " + result + ", exception = " + exception);
        }).exceptionally(throwable -> {
            System.out.println("异常处理");
            return -1;
        });
        System.out.println(completableFuture.get());
        System.out.println("主线程：" + Thread.currentThread().getId() + " stop");
    }

    // 有返回值
    @Test
    public void testCompletableFuture_supply_Return_handle() throws ExecutionException, InterruptedException {
        System.out.println("主线程：" + Thread.currentThread().getId() + " start");
        CompletableFuture<Integer> completableFuture = CompletableFuture.supplyAsync(() -> {
            System.out.println("当前线程：" + Thread.currentThread().getId());
            int i = 10 / 0;
            System.out.println("运行结果 = " + i);
            return i;
        }, executor).handle((result, exception) -> {
            System.out.println("异步任务执行完成，result = " + result + ", exception = " + exception);
            return exception != null ? -1 : result;
        });
        System.out.println(completableFuture.get());
        System.out.println("主线程：" + Thread.currentThread().getId() + " stop");
    }

    // ==================================================华丽的分割线==================================================

    // 无返回值 线程串行化方法
    @Test
    public void testCompletableFuture_Return_thenRunAsync() throws ExecutionException, InterruptedException {
        System.out.println("主线程：" + Thread.currentThread().getId() + " start");
        CompletableFuture<Void> completableFuture = CompletableFuture.supplyAsync(() -> {
            System.out.println("当前线程：" + Thread.currentThread().getId());
            int i = 10 / 2;
            System.out.println("运行结果 = " + i);
            return i;
        }, executor).thenRunAsync(() -> {
            System.out.println("异步任务2启动");
        }, executor);
        System.out.println("主线程：" + Thread.currentThread().getId() + " stop");
    }

    // 无返回值 线程串行化方法
    @Test
    public void testCompletableFuture_Return_thenAcceptAsync() throws ExecutionException, InterruptedException {
        System.out.println("主线程：" + Thread.currentThread().getId() + " start");
        CompletableFuture<Void> completableFuture = CompletableFuture.supplyAsync(() -> {
            System.out.println("当前线程：" + Thread.currentThread().getId());
            int i = 10 / 2;
            System.out.println("运行结果 = " + i);
            return i;
        }, executor).thenAcceptAsync(result -> {
            System.out.println("异步任务2启动，上一步执行结果：" + result);
        }, executor);
        System.out.println("主线程：" + Thread.currentThread().getId() + " stop");
    }

    // 有返回值 线程串行化方法
    @Test
    public void testCompletableFuture_Return_thenApplyAsync() throws ExecutionException, InterruptedException {
        System.out.println("主线程：" + Thread.currentThread().getId() + " start");
        CompletableFuture<String> completableFuture = CompletableFuture.supplyAsync(() -> {
            System.out.println("当前线程：" + Thread.currentThread().getId());
            int i = 10 / 2;
            System.out.println("运行结果 = " + i);
            return i;
        }, executor).thenApplyAsync(result -> {
            System.out.println("异步任务2启动，上一步执行结果：" + result);
            return result * 2 + "";
        }, executor);
        System.out.println(completableFuture.get());
        System.out.println("主线程：" + Thread.currentThread().getId() + " stop");
    }

    // ==================================================华丽的分割线==================================================

    // 无返回值 组合两个future，不需要获取future的结果，只需要两个future处理完任务后，处理该任务。
    @Test
    public void testCompletableFuture_Return_runAfterBoth() throws ExecutionException, InterruptedException {
        System.out.println("主线程：" + Thread.currentThread().getId() + " start");
        CompletableFuture<Integer> future1 = CompletableFuture.supplyAsync(() -> {
            System.out.println("任务1开始：" + Thread.currentThread().getId());
            int i = 10 / 2;
            System.out.println("任务1结束：" + Thread.currentThread().getId());
            return i;
        }, executor);
        CompletableFuture<String> future2 = CompletableFuture.supplyAsync(() -> {
            System.out.println("任务2开始：" + Thread.currentThread().getId());
            System.out.println("任务2结束：" + Thread.currentThread().getId());
            return "Hello";
        }, executor);
        future1.runAfterBothAsync(future2, () -> {
            System.out.println("任务3开始：" + Thread.currentThread().getId());
            System.out.println("任务3结束：" + Thread.currentThread().getId());
        }, executor);
        System.out.println("主线程：" + Thread.currentThread().getId() + " stop");
    }

    // 无返回值 组合两个future，获取两个future任务的返回结果，然后处理任务，没有返回值
    @Test
    public void testCompletableFuture_Return_thenAcceptBothAsync() throws ExecutionException, InterruptedException {
        System.out.println("主线程：" + Thread.currentThread().getId() + " start");
        CompletableFuture<Integer> future1 = CompletableFuture.supplyAsync(() -> {
            System.out.println("任务1开始：" + Thread.currentThread().getId());
            int i = 10 / 2;
            System.out.println("任务1结束：" + Thread.currentThread().getId());
            return i;
        }, executor);
        CompletableFuture<String> future2 = CompletableFuture.supplyAsync(() -> {
            System.out.println("任务2开始：" + Thread.currentThread().getId());
            System.out.println("任务2结束：" + Thread.currentThread().getId());
            return "Hello";
        }, executor);
        future1.thenAcceptBothAsync(future2, (result1, result2) -> {
            System.out.println("任务3开始：" + Thread.currentThread().getId());
            System.out.println(result1 + result2);
            System.out.println("任务3结束：" + Thread.currentThread().getId());
        }, executor);
        System.out.println("主线程：" + Thread.currentThread().getId() + " stop");
    }

    // 有返回值 组合两个future，获取两个future任务的返回结果并返回当前任务的返回值
    @Test
    public void testCompletableFuture_Return_thenCombineAsync() throws ExecutionException, InterruptedException {
        System.out.println("主线程：" + Thread.currentThread().getId() + " start");
        CompletableFuture<Integer> future1 = CompletableFuture.supplyAsync(() -> {
            System.out.println("任务1开始：" + Thread.currentThread().getId());
            int i = 10 / 2;
            System.out.println("任务1结束：" + Thread.currentThread().getId());
            return i;
        }, executor);
        CompletableFuture<String> future2 = CompletableFuture.supplyAsync(() -> {
            System.out.println("任务2开始：" + Thread.currentThread().getId());
            System.out.println("任务2结束：" + Thread.currentThread().getId());
            return "Hello";
        }, executor);
        CompletableFuture<String> future3 = future1.thenCombineAsync(future2, (result1, result2) -> {
            System.out.println("任务3开始：" + Thread.currentThread().getId());
            System.out.println(result1 + result2);
            System.out.println("任务3结束：" + Thread.currentThread().getId());
            return result1 + ":" + result2;
        }, executor);
        System.out.println(future3.get());
        System.out.println("主线程：" + Thread.currentThread().getId() + " stop");
    }

    // ==================================================华丽的分割线==================================================

    // 无返回值 两个任务有一个执行完成（另外一个执行的慢而不是异常），不需要获取future的结果，处理任务，也没有返回值。
    @Test
    public void testCompletableFuture_Return_runAfterEitherAsync() throws ExecutionException, InterruptedException {
        System.out.println("主线程：" + Thread.currentThread().getId() + " start");
        CompletableFuture<Integer> future1 = CompletableFuture.supplyAsync(() -> {
            System.out.println("任务1开始：" + Thread.currentThread().getId());
            int i = 10 / 2;
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            System.out.println("任务1结束：" + Thread.currentThread().getId());
            return i;
        }, executor);
        CompletableFuture<String> future2 = CompletableFuture.supplyAsync(() -> {
            System.out.println("任务2开始：" + Thread.currentThread().getId());
            System.out.println("任务2结束：" + Thread.currentThread().getId());
            return "Hello";
        }, executor);
        future1.runAfterEitherAsync(future2, () -> {
            System.out.println("任务3开始：" + Thread.currentThread().getId());
            System.out.println("任务3结束：" + Thread.currentThread().getId());
        }, executor);
        Thread.sleep(3000);
        System.out.println("主线程：" + Thread.currentThread().getId() + " stop");
    }

    // 无返回值 两个任务有一个执行完成（另外一个执行的慢而不是异常），获取它的返回值，处理任务，没有新的返回值
    @Test
    public void testCompletableFuture_Return_acceptEitherAsync() throws ExecutionException, InterruptedException {
        System.out.println("主线程：" + Thread.currentThread().getId() + " start");
        CompletableFuture<Integer> future1 = CompletableFuture.supplyAsync(() -> {
            System.out.println("任务1开始：" + Thread.currentThread().getId());
            int i = 10 / 2;
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            System.out.println("任务1结束：" + Thread.currentThread().getId());
            return i;
        }, executor);
        CompletableFuture<Integer> future2 = CompletableFuture.supplyAsync(() -> {
            System.out.println("任务2开始：" + Thread.currentThread().getId());
            System.out.println("任务2结束：" + Thread.currentThread().getId());
            return "Hello World".length();
        }, executor);
        future1.acceptEitherAsync(future2, (result) -> {
            System.out.println("任务3开始：" + Thread.currentThread().getId());
            System.out.println(result);
            System.out.println("任务3结束：" + Thread.currentThread().getId());
        }, executor);
        Thread.sleep(3000);
        System.out.println("主线程：" + Thread.currentThread().getId() + " stop");
    }

    // 无返回值 两个任务有一个执行完成（另外一个执行的慢而不是异常），获取它的返回值，处理任务并返回新的返回值
    @Test
    public void testCompletableFuture_Return_applyToEitherAsync() throws ExecutionException, InterruptedException {
        System.out.println("主线程：" + Thread.currentThread().getId() + " start");
        CompletableFuture<Integer> future1 = CompletableFuture.supplyAsync(() -> {
            System.out.println("任务1开始：" + Thread.currentThread().getId());
            int i = 10 / 2;
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            System.out.println("任务1结束：" + Thread.currentThread().getId());
            return i;
        }, executor);
        CompletableFuture<Integer> future2 = CompletableFuture.supplyAsync(() -> {
            System.out.println("任务2开始：" + Thread.currentThread().getId());
            System.out.println("任务2结束：" + Thread.currentThread().getId());
            return "Hello World".length();
        }, executor);
        CompletableFuture<Integer> future3 = future1.applyToEitherAsync(future2, (result) -> {
            System.out.println("任务3开始：" + Thread.currentThread().getId());
            System.out.println(result);
            System.out.println("任务3结束：" + Thread.currentThread().getId());
            return result + 1;
        }, executor);
        System.out.println(future3.get());
        System.out.println("主线程：" + Thread.currentThread().getId() + " stop");
    }

    // ==================================================华丽的分割线==================================================

    // 所有任务都完成
    @Test
    public void testCompletableFuture_Return_allOf() throws ExecutionException, InterruptedException {
        CompletableFuture<String> futureImg = CompletableFuture.supplyAsync(() -> {
            System.out.println("查询商品的图片信息");
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return "hello.jpg";
        });
        CompletableFuture<String> futureAttr = CompletableFuture.supplyAsync(() -> {
            System.out.println("查询商品的属性信息");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return "黑色+256G";
        });
        CompletableFuture<String> futureBrand = CompletableFuture.supplyAsync(() -> {
            System.out.println("查询商品的品牌信息");
            try {
                Thread.sleep(2500);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return "华为";
        });

        CompletableFuture<Void> allOf = CompletableFuture.allOf(futureImg, futureAttr, futureBrand);
        allOf.get(); // 等待所有结果完成
        System.out.println("allOf.get()");
    }

    // 任一任务完成
    @Test
    public void testCompletableFuture_Return_anyOf() throws ExecutionException, InterruptedException {
        CompletableFuture<String> futureImg = CompletableFuture.supplyAsync(() -> {
            System.out.println("查询商品的图片信息");
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return "hello.jpg";
        });
        CompletableFuture<String> futureAttr = CompletableFuture.supplyAsync(() -> {
            System.out.println("查询商品的属性信息");
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return "黑色+256G";
        });
        CompletableFuture<String> futureBrand = CompletableFuture.supplyAsync(() -> {
            System.out.println("查询商品的品牌信息");
            try {
                Thread.sleep(2500);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return "华为";
        });

        CompletableFuture<Object> anyOf = CompletableFuture.anyOf(futureImg, futureAttr, futureBrand);
        Object o = anyOf.get();// 等待任一结果完成
        System.out.println("anyOf.get()" + o);
    }
}
