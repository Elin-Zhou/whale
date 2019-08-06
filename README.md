# Whale
whale is a cache framework that is simple to use

## 设计方向
1. 可以在方法上直接加注解来实现自动缓存
2. 手动读写，类似与guava的读写
3. 提供缓存类型的可选，可以选择使用本地缓存、远程缓存、两者兼使用
4. 具备集群间缓存同步的功能
5. 支持不同环境间数据隔离
6. 满足CAP理论中的AP原则，即放弃强一致性

## 技术选型
* 远程缓存 Redis
* 本地缓存 Caffenine
* 集群间同步？ ZooKeeper/Redis发布订阅

## 集群间同步
1. 通过redis发布订阅来广播缓存失效
2. 通过监听zk节点的状态来失效

## 全局参数
* 命名空间 默认使用spring.application.name
* 本地缓存 第一个版本仅支持Caffenine
* 远程缓存 第一个版本仅支持redis
* 集群同步服务 第一个版本仅支持redis，后续支持zk
* redis链接信息
* 全局缓存时间
* 全局最大本地缓存数量
* 全局一致性开关

## 缓存参数


参数名称 | 含义 | 默认值 | 是否必须 | 备注 
:-: | :-: | :-: | :-: | :-:
namespace | 命名空间 |spring.application.name|true
name | 缓存名称 | null | true
expire | 失效时间 | null |true
timeUnit |失效时间单位 |TimeUnit.SECOND |true
localExpire | 本地缓存 | null |false| 仅当使用了本地缓存时生效，如果不配置，则与expire相同
type | 缓存类型 |CacheType.LOCAL |true|CacheType.LOCAL,CacheType.Remote,CacheType.BOTH
sizeLimit|本地最大的缓存数量|Integer.MAX_VALUE|false|
consistency|是否需要保证一致性|false|false|在集群同步服务可用时会使用锁策略保证同一时间只有一个服务开启加载缓存操作；如果同步服务不可用，会停用本地缓存并清空所以已缓存数据
cacheNull|是否缓存null值|false|false



## 基本操作
### 通过在方法上加注解来实现缓存
在方法上加上自定义注解，Spring启动的时候当发现有方法上带有@Cached注解，就创建一个代理类，之后对该方法的请求都由代理类处理缓存逻辑
### 如何使用redis
因为本框架目前只支持在Spring环境下使用，所以默认会自动尝试调用AOP中的RedisTemplate，**如果RedisTemplate不存在，则将使用Jedis客户端，此时需要使用者提供redis链接信息。（第一个版本暂时不支持）**


## 问题？
### 如果集群间同步服务不可用，如何保证本地缓存的一致性？
1. 是否可以通过zookeeper来实现缓存在集群间的同步来保证一致性，如果zookeeper不可用，则直接清空本地缓存（可以把控制权交给开发者）
2. 轮询redis，如果发现不可用，则直接清空本地缓存（可以把控制权交给开发者）。如果使用轮询方案，即必定会出现两次轮询期间无法感知redis不可用的情况而影响数据一致性，第一个版本先忽略


## 故障演习
### 需要模拟Redis等远程服务不可用
* 在远程服务不可用时，必须要保证整体缓存框架可用，即本地缓存的可用性;如果未开启本地缓存，需要保证仍可以进行正常的数据库回源操作。
* 集群同步服务不可用时（如redis发布订阅不可用），开启了一致性保证的缓存需要全部执行数据库的回源操作，避免脏读缓存数据