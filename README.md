#前言
由于当前正在写的项目集成了两个网络请求框架（Volley and Retrofit）对比之下也是选择了Retrofit。既然选择那自然要让自己以后开发更加省力（就是懒）。于是我在Retrofit中加入了Rxjava，这也是当下蛮流行的一个请求框架。然后又利用了Kotlin的一些新特性，使网络请求变得特别简单，代码量特别少。

#正文
####导包
首先需要导入相关的包，包括Rxjava（我这里使用的是1.x的版本，如果你使用的是2.x的版本影响不大）、Retrofit。
```
    //Rxjava
    compile 'io.reactivex:rxandroid:1.2.0'
    compile 'io.reactivex:rxjava:1.2.0'
    //Retrofit
    compile 'com.squareup.retrofit2:retrofit:2.3.0'
    compile 'com.squareup.retrofit2:converter-gson:2.3.0'
    compile 'com.squareup.retrofit2:adapter-rxjava:2.3.0'
    compile 'com.squareup.okhttp3:logging-interceptor:3.8.0'
```
####初始化Retrofit
```
      retrofit = Retrofit.Builder()
                .client(build.build())
                .baseUrl("你的url")
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build().create(RetrofitUrl::class.java)
```
`client`传入的是一个`OkhttpClient`，这里我们需要创建一个`OkhttpClient`对象，这个可以用来加入一些拦截器、连接等待时间等，以下是我的`client`：
```
        val build = OkHttpClient.Builder().connectTimeout(15,TimeUnit.SECONDS)
                .writeTimeout(15,TimeUnit.SECONDS)
                .readTimeout(15,TimeUnit.SECONDS)
        val logging = HttpLoggingInterceptor(HttpLoggingInterceptor.Logger {
            Log.e("retrofit url",it)
        })
        logging.level = HttpLoggingInterceptor.Level.BODY
```
我这里只设置了连接、读、写超时时间和一个拦截器，用于在用`Retrofit`请求网络的时候可以获取到请求的信息。然后是`baseUrl`这里是设置网络请求的通用的地址，格式类似于`http://ip:端口/后台项目名/`，需要以`/`结尾。而后的`addConverterFactory`和`addCallAdapterFactory`是我们刚刚导入的包，用于添加`gson`和`RxJava`支持，其中如果解析的时候有要求时间格式，可自定义一个`gson`传入：
```
val gson = GsonBuilder().setDateFormat("yyyy-MM-dd hh:mm:ss").create()
```
如果不要求时间格式，`GsonConverterFactory.create(此处可以不传参数)`。然后就是`RetrofitUrl`，这是一个接口，名字可以根据个人喜好进行定义，其内放置请求的接口：
```
interface RetrofitUrl {
    //方法名自定义
    @GET("接口地址")
    fun load():Observable<对应实体类>

    @FormUrlEncoded
    @Post("接口地址")
    fun load():Observable<对应实体类>

    //需要传递参数，多个参数逗号隔开
    @GET("接口地址")
    fun load(@Query("参数名字") 参数名字（可自定义）:参数类型):Observable<对应实体类>

    @FormUrlEncoded
    @Post("接口地址")
    fun load(@Field("参数名字") 参数名字（可自定义）:参数类型):Observable<对应实体类>

    //示例
    @GET("load")
    fun load():Observable<NetOuter<Orgs>>

    @GET("load")
    fun load(@Query("id") id:Int):Observable<NetOuter<Orgs>>
}
```
以上就是初始化大概过程，初始化我是放在了自定义的`Application`中完成，使用时通过`Application`获取到`Retrofit`。
####请求网络
以上都做完了就可以开始请求网络了。
通过`Application`获取到`Retrofit`后，我们就可以通过它去调用我们刚刚在接口中定义的方法，因为配置了`RxJava`，所以调用方法后会返回一个`Observable`，这也是我们在接口中定义的返回类型，如果没有添加`RxJava`，返回类型为`Call`。这样子我们就可以按照`RxJava`的习惯去写了：
```
  retrofit().load()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object :Subscriber<NetOuter<Orgs>>(){
                override fun onCompleted() {
                               
                }
                override fun onNext(t: NetOuter<Orgs>?) { 
                      //可以在这里对获取到的数据进行处理
                }
                override fun onError(e: Throwable?) { 
                     //请求失败
                }
             )
```
这样子我们就完成了一个网络请求，这里就进行了线程调度的操作，具体看操作者的需求，也可以加入以下`RxJava`的操作符。
虽然这样子可以进行网络请求，可如果每次请求都要去写线程调度又觉得太麻烦了，都是一样的代码。这时候我们就用到了`Kotlin`的一个特性，扩展函数。我们新建一个`Kotlin File`文件，在其中写入我们修改了的代码：
```
fun <T> runRx(observable: Observable<T>, subscriber: Subscriber<T>): Subscription = 
    observable.subscribeOn(Schedulers.io())
              .observeOn(AndroidSchedulers.mainThread())
              .subscribe(subscriber)
```
让我们调调这个方法看看效果：
```
runRx(retrofit().load(),object : Subscriber<NetOuter<Orgs>>() {
        override fun onCompleted() {}

        override fun onNext(t: NetOuter<Orgs>?) { 
            //可以在这里对获取到的数据进行处理
        }

        override fun onError(e: Throwable?) { 
            //请求失败
       }
)
```
通过这一层的封装，省去了线程调度的代码，在大量请求的时候，可以省去不少代码。但是，就这个程度，还是觉得要一直写`object : Subscriber...`，这个也不想写，懒嘛。怎么办？只能继续封装，这时候就想到了`Kotlin`的另一个特性，高阶函数。`Kotlin`允许把一个方法当做一个参数进行使用，使用时通过`Lambda`的方式展示，一样在我们刚刚写`runRx`那个文件：
```
fun <T> runRxLambda(observable: Observable<T>,next:(T)->Unit,error:(e: Throwable?)->Unit,completed:() -> Unit = { Log.e("completed","completed") }): Subscription{
    runRx(observable, object : Subscriber<T>() {
        override fun onCompleted() { completed }
        override fun onNext(t: T) { next(t) }
        override fun onError(e: Throwable?) { error(e) }
    })
}
```
这里通过`next:(T)->Unit`将方法当做一个参数，其中`next`为这个参数的参数名字，冒号后面的括号里面为这个方法需要的参数，多个参数逗号隔开，`Unit`是返回类型，`Unit`相当于`Java`中的`void`。其中还看到了`completed:() -> Unit = { Log.e("completed","completed") }`这里用到了`Kotlin`的参数默认值，通过`=`号将右边当做左边方法的默认实现，如果操作者没有实现这个方法，就用这个默认操作。`runRxLambda`的方法内也就是调用了我们刚刚写的`runRx`方法，然后将对应的方法传入就可以了。接下来看看效果：
```
runRxLambda(retrofit().load(),{
        //我们在这里的操作就相当于在onNext中的操作，参数可以通过it获取
    },{
        //这里就是onError的实现，参数也可以通过it获取
    })

runRxLambda(retrofit().load(),{
        //我们在这里的操作就相当于在onNext中的操作，参数可以通过it获取
    },{
        //这里就是onError的实现，参数也可以通过it获取
    },{
       //这里是onCompleted,不实现也可以
    })
```
####结尾
以上就是所有的内容了，这里的一些实现方式不止用在这里，这篇文章也只是当做一个抛砖引玉，其中可能也有很多操作不到位，讲的不到位的，希望喷的小声点。有什么更好的想法也希望可以私信留言。
