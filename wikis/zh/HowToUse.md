# 使用方法

## 简单三个步骤快速使用缓存框架

### 1.引入依赖

```
    <dependency>
        <groupId>com.xxelin</groupId>
        <artifactId>whale-spring-boot-starter</artifactId>
        <version>1.1.1-RELEASE</version>
    </dependency>

```

### 2.开启缓存
在application.properties中增加一条配置来开启缓存服务

```
whale.enable=true
```

### 3.配置缓存
在需要启用缓存的方法上增加注解(开启缓存的对象必须注册到IOC容器中)

```
 @Cached(expire = 60)
```
 其中expire=60表示缓存60秒
 
 
 
1...2...3...仅需简单三步，启动你的项目试试效果吧~

## 细节配置

### 为某个方法/类开启缓存
如果想要在某个方法中开启缓存功能，可以像之前简单三步的例子中那样，在对应的方法上增加一个```@Cache```注解

如果想让某个类所有的方法均开启缓存，可以在该类上增加```@Cache```注解；如果该类中某个方法不想开启缓存，可以在该方法上单独加上```@Cache(enable=false)```

同理，所有在方法上的配置优先级将高于在类的配置，类的配置优先级高于全局配置。

注意，该框架目前依赖于Spring，所以开启缓存的对象必须注册到IOC容器中

### 配置缓存时长
#### 方法一
在application.properties中增加配置```whale.expireSeconds=60```，
其中60表示缓存的时长，单位为秒，该配置全局生效，所有在@Cached注解上没有设置expire参数的缓存均生效

#### 方法二
在@Cached注解中有三个参数可选，分别是expire、timeUnit、localExpire，其中timeUnit表示缓存的单位，expire表示远程缓存的时长，localExpire表示本地缓存的时长，如果localExpire没有设置，则使用expire覆盖


### 配置缓存类型
@Cached注解中有type字段用来设置缓存的类型，有三个值可选，LOCAL、REMOTE、BOTH。

* LOCAL表示仅使用本地缓存，当前版本使用Caffeine作为缓存实现
* REMOTE表示仅使用远程缓存，当前版本暂不支持
* BOTH表示同时使用本地缓存和远程缓存，优先使用本地缓存，当前版本暂不支持


### 配置缓存的命中规则

#### 默认规则
当没有单独配置命中规则时，框架会自动以类名+方法+参数值作为缓存的key，其中参数值使用FASTJSON进行序列化，所以在默认规则下，需要所有参数精准匹配才能命中。

#### 自定义缓存key
@Cached中有一个idExpress字段可以自定义缓存key，当不设置该字段时，将使用默认匹配规则。
idExpress使用[SpEL表达式](https://docs.spring.io/spring/docs/4.2.x/spring-framework-reference/html/expressions.html),开发者可以自定义缓存key的规则。

#### 动态命中缓存
一些场景下，开发者希望某些条件不命中缓存而直接进行回源操作，所以在@Cached中有一个condition字段，可以在此字段书写[SpEL表达式](https://docs.spring.io/spring/docs/4.2.x/spring-framework-reference/html/expressions.html)，通过返回true/false来控制是否需要命中缓存，当返回false时，强制不命中缓存，即进行回源操作。

### 防止缓存击穿
为了避免大量请求不存在的值时导致的大量回源操作，进而导致后端的数据库或其他资源压力加大，@Cached提供了cacheNull字段，当此字段设置为true时，将会对返回结果为null的情况进行缓存。
如果需要全局开启null值缓存功能，可以在application.properties中增加配置
```
whale.cacheNull=true
```

#### 手动缓存失效
除了通过时间或者总量的限制来使缓存自动失效，还提供了细粒度的手动缓存失效方式。
首先，需要单独设置缓存的方法名，在@Cached中设置value字段，此字段作为该类中的唯一方法标识

```
//方法一 直接传入参数
CacheUtils.invalidateWithParams(bean, "value", req);
//方法二 手动拼接key,即开发自行计算@Cached中idExpress的结果传入
CacheUtils.invalidateWithId(bean, "value", req.getUserId());
```

需要注意，由于实现方式的限制，如果需要失效当前类中的某个方法的缓存，不能在上述的两种方式的第一个字段中传入```this```。因为此处的参数必须要求为Spring中的bean，但是由于框架增加的种种功能，```this```引用的对象不一定是Spring中对应的bean。

如果需要在手动失效当前类中的某个缓存，请使用下文中介绍的***在类中注入当前类对应的Bean***，并将注入的bean传入上述方法中来实现缓存的失效。


## 其他功能
### 在类中注入当前类对应的Bean
如果因为一些原因，需要拿到当前类对应的bean，可以让当前类实现```com.xxelin.whale.processor.SelfAware```接口，并实现```void setSelf(T bean);```方法，该方法将在Spring启动时自动传入当前类对应的bean对象，开发者手动保存此对象已供后续使用。

*通过此方式可以解决私有方法无法正常使用注解式事务的情况，读者可以自行研究*
