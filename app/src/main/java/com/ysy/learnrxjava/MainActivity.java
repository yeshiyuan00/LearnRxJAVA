package com.ysy.learnrxjava;

import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    private static final String tag = "com.ysy.learnrxjava.tag";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //  创建 Observer
        Observer<String> observer = new Observer<String>() {
            @Override
            public void onCompleted() {
                Log.d(tag, "Completed!");
            }

            @Override
            public void onError(Throwable e) {
                Log.d(tag, "Error!");
            }

            @Override
            public void onNext(String s) {
                Log.d(tag, "Item: " + s);
            }
        };

        Subscriber<String> subscriber = new Subscriber<String>() {
            @Override
            public void onCompleted() {
                Log.d(tag, "Completed!");
            }

            @Override
            public void onError(Throwable e) {
                Log.d(tag, "Error!");
            }

            @Override
            public void onNext(String s) {
                Log.d(tag, "Item: " + s);
            }
        };

        // 2) 创建 Observable
        Observable observable = Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                subscriber.onNext("Hello");
                subscriber.onNext("Hi");
                subscriber.onNext("Aloha");
                subscriber.onCompleted();
            }


        });

        //3) Subscribe (订阅)
        observable.subscribe(observer);

        //just(T...): 将传入的参数依次发送出来。
        Observable observable1 = Observable.just("Hello1", "Hi1", "Aloha1");
        // 将会依次调用：
        // onNext("Hello");
        // onNext("Hi");
        // onNext("Aloha");
        // onCompleted();

        observable1.subscribe(subscriber);

        //from(T[]) / from(Iterable<? extends T>) : 将传入的数组或 Iterable 拆分成具体对象后，依次发送出来。
        String[] words = new String[]{"Hello2", "Hi2", "Aloha2"};
        Observable observable2 = Observable.from(words);
        // 将会依次调用：
        // onNext("Hello");
        // onNext("Hi");
        // onNext("Aloha");
        // onCompleted();
        observable2.subscribe(observer);
        //上面 just(T...) 的例子和 from(T[]) 的例子，都和之前的 create(OnSubscribe) 的例子是等价的。

        //除了 subscribe(Observer) 和 subscribe(Subscriber) ，subscribe() 还支持不完整定义的回调，
        // RxJava 会自动根据定义创建出 Subscriber 。形式如下：
        Action1<String> onNextAction = new Action1<String>() {
            // onNext()
            @Override
            public void call(String s) {
                Log.d(tag, s);
            }
        };

        Action1<Throwable> onErrorAction = new Action1<Throwable>() {
            // onError()
            @Override
            public void call(Throwable throwable) {
                // Error handling
            }
        };

        Action0 onCompletedAction = new Action0() {
            // onCompleted()
            @Override
            public void call() {
                Log.d(tag, "completed3");
            }
        };

        // 自动创建 Subscriber ，并使用 onNextAction 来定义 onNext()
        observable.subscribe(onNextAction);
        // 自动创建 Subscriber ，并使用 onNextAction 和 onErrorAction 来定义 onNext() 和 onError()
        observable.subscribe(onNextAction, onErrorAction);
        // 自动创建 Subscriber ，并使用 onNextAction、 onErrorAction 和 onCompletedAction 来定义
        // onNext()、 onError() 和 onCompleted()
        observable.subscribe(onNextAction, onErrorAction, onCompletedAction);

        //        a. 打印字符串数组
        //        将字符串数组 names 中的所有字符串依次打印出来：
        String[] names = new String[]{"yeshiyuan", "zhangxianggui", "yuanweihjia"};
        Observable.from(names).subscribe(new Action1<String>() {
            @Override
            public void call(String name) {
                Log.d(tag, name);
            }
        });

        //  b. 由 id 取得图片并显示
        //  由指定的一个 drawable 文件 id drawableRes 取得图片，并显示在 ImageView 中，并在出现异常的时候打印 Toast 报错：
        final int drawableRes = R.drawable.p1;
        final ImageView img_view = (ImageView) findViewById(R.id.id_img_view);

        Observable.create(new Observable.OnSubscribe<Drawable>() {
            @Override
            public void call(Subscriber<? super Drawable> subscriber) {
                Drawable drawable = null;

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    drawable = getTheme().getDrawable(drawableRes);
                } else {
                    drawable = getResources().getDrawable(drawableRes);
                }
                subscriber.onNext(drawable);
                subscriber.onCompleted();
            }
        }).subscribe(new Observer<Drawable>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {
                Toast.makeText(MainActivity.this
                        , "Error!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNext(Drawable drawable) {
                img_view.setImageDrawable(drawable);
            }
        });
        //正如上面两个例子这样，创建出 Observable 和 Subscriber ，再用 subscribe() 将它们串起来
        // ，一次 RxJava 的基本使用就完成了。非常简单。

        //在 RxJava 的默认规则中，事件的发出和消费都是在同一个线程的。也就是说，
        // 如果只用上面的方法，实现出来的只是一个同步的观察者模式。
        // 观察者模式本身的目的就是『后台处理，前台回调』的异步机制，因此异步对于
        // RxJava 是至关重要的。而要实现异步，则需要用到 RxJava 的另一个概念： Scheduler 。


        //3. 线程控制 —— Scheduler
        //在不指定线程的情况下， RxJava 遵循的是线程不变的原则，
        // 即：在哪个线程调用 subscribe()，就在哪个线程生产事件；
        // 在哪个线程生产事件，就在哪个线程消费事件。如果需要切换线程，
        // 就需要用到 Scheduler （调度器）。

        //subscribeOn(): 指定 subscribe() 所发生的线程，即 Observable.OnSubscribe
        // 被激活时所处的线程。或者叫做事件产生的线程。
        //observeOn(): 指定 Subscriber 所运行在的线程。或者叫做事件消费的线程。
        Observable.just(1, 2, 3, 4)
                .subscribeOn(Schedulers.io())   // 指定 subscribe() 发生在 IO 线程
                .observeOn(AndroidSchedulers.mainThread())  // 指定 Subscriber 的回调发生在主线程
                .subscribe(new Action1<Integer>() {
                    @Override
                    public void call(Integer number) {
                        Log.d(tag, "number:" + number);
                    }
                });

        final int drawableRes1 = R.drawable.p1;
        final ImageView img_view1 = (ImageView) findViewById(R.id.id_img_view);

        Observable.create(new Observable.OnSubscribe<Drawable>() {
            @Override
            public void call(Subscriber<? super Drawable> subscriber) {
                Drawable drawable = getResources().getDrawable(drawableRes);
                subscriber.onNext(drawable);
                subscriber.onCompleted();
                Log.d(tag,"I just want to test this,will delete it later!");
            }
        }).observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Observer<Drawable>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Toast.makeText(MainActivity.this
                                , "Error!", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onNext(Drawable drawable) {
                        img_view.setImageDrawable(drawable);
                    }
                });
        //  那么，加载图片将会发生在 IO 线程，而设置图片则被设定在了主线程。
        // 这就意味着，即使加载图片耗费了几十甚至几百毫秒的时间，也不会造成丝毫界面的卡顿。


     /*   Observable.just(1, 2, 3, 4) // IO 线程，由 subscribeOn() 指定
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.newThread())
                .map(mapOperator) // 新线程，由 observeOn() 指定
                .observeOn(Schedulers.io())
                .map(mapOperator2) // IO 线程，由 observeOn() 指定
                .observeOn(AndroidSchedulers.mainThread)
                .subscribe(subscriber);  // Android 主线程，由 observeOn() 指定*/
    }
}
