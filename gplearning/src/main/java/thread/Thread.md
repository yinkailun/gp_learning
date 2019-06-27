## 1。线程五种状态
    1. (new),新建线程，继承Thread或者实现runnable()接口。
    2. (runnable),可执行，使用start()方法,变得可执行，获取运行所需的全部资源，只需等待获取CPU使用权。
    3. (running)获取CPU使用权，执行代码
    4. 阻塞(block)
        由于某种原因放弃CPU使用权，线程挂起，停止运行，直到线程通过某种途径变成runnable.
        1.等待阻塞：调用wait()方法，线程释放所有CPU资源，jvm将其放入等待池，这种状态不能自动唤醒，只能其他线程调用notify或者notifyAll唤醒
        2.同步阻塞，执行获取对象同步锁，该同步锁被其他线程占用，jvm将其方法锁池中。
        3.其他阻塞，join()或者sleep()，或者发出I/O请求（发起系统调用system call），jvm将其设为阻塞状态，直到join线程结束或者超时，sleep超时，I/O请求结束，转入可执行状态。
    5. (dead),线程完成或者异常结束run方法，线程结束。
    
## 2. wait和sleep区别
    1.wait是object的方法，sleep是Thread的方法，wait会释放CPU资源，释放对象锁，只有其他线程通过notify或者notifyAll才可进入runnable,
    2.sleep不会释放对象锁，线程挂起。
