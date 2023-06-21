# ship-rpc-spring-boot-starter
![](https://img.shields.io/github/stars/2YSP/rpc-spring-boot-starter.svg)
![](https://img.shields.io/github/forks/2YSP/rpc-spring-boot-starter.svg)
![](https://img.shields.io/github/release/2YSP/rpc-spring-boot-starter.svg)
![](https://img.shields.io/github/downloads/2YSP/rpc-spring-boot-starter/total.svg)

基于netty实现的高性能可扩展RPC框架

# 一、特性
- 支持多种序列化协议，包括java，protobuf，kryo和hessian
- 客户端调用支持多种负载均衡算法，包括随机、轮询、加权轮询和平滑加权轮询
- 支持主流注册中心Nacos和Zookeeper
- 支持泛化调用

# 二、使用方法


## 2.1 pom.xml
添加maven依赖到你的项目中
 ```xml
        <dependency>
            <groupId>io.github.2ysp</groupId>
            <artifactId>ship-rpc-spring-boot-starter</artifactId>
            <version>1.0.0-RELEASE</version>
        </dependency>
 ```
 ## 2.2 客户端


 
**1. 普通RPC调用**

引入接口依赖，使用@InjectService注解注入远程方法
 ```java
 @RestController
@RequestMapping("test")
public class TestController {

    @InjectService
    private UserService userService;

    @GetMapping("/user")
    public ApiResult<User> getUser(@RequestParam("id")Long id){
        return userService.getUser(id);
    }
}
 ```

**2. 泛化调用**
 ```java
@RestController
@RequestMapping("/GenericTest")
public class GenericTestController {


    @GetMapping("/user")
    public String getUserString(@RequestParam("id") Long id) {
        //cn.sp.UserService.getUserString
        GenericService instance = GenericServiceFactory.getInstance("cn.sp.UserService");
        Object result = instance.$invoke("getUserString", new String[]{"java.lang.Long"}, new Object[]{id});
        return result.toString();
    }


    @GetMapping("")
    public String getUser(@RequestParam("id") Long id) {
        //cn.sp.UserService.getUserString
        GenericService instance = GenericServiceFactory.getInstance("cn.sp.UserService");
        Object result = instance.$invoke("getUser", new String[]{"java.lang.Long"}, new Object[]{id});
        return result.toString();
    }
}
 ```


 **配置项：**
|    属性 |含义      |  可选项   |
| --- | --- | --- |
|   sp.rpc.protocol  | 消息序列化协议        |  java，protobuf，kryo，hessian   |
|    sp.rpc.register-address |  注册中心地址      |  默认localhost:2181   |
|    sp.rpc.register-center-type |  注册中心类型     | nacos<br>zk|
|    sp.rpc.load-balance |  负载均衡算法     | random<br>round<br>weightRound<br>smoothWeightRound|

 ## 2.3 服务端
 提供远程方法并注入IOC

 ```java
@Service
public class UserServiceImpl implements UserService{

    private static  Logger logger = LoggerFactory.getLogger(UserService.class);


    @Override
    public ApiResult<User> getUser(Long id) {
        logger.info("现在是【3】号提供服务");
        User user = new User(1L,"XX",2,"www.aa.com");
        return ApiResult.success(user);
    }

    @Override
    public String getUserString(Long id) {
        logger.info("getUserString");
        User user = new User(1L,"XX",2,"www.aa.com");
        return JSON.toJSONString(ApiResult.success(user));
    }
}
 ```
 **注意：** 这里的@Service注解不是Spring的，而是com.github.ship.annotation.Service。
 
 **配置项：**
|    属性 |含义      |  可选项   |
| --- | --- | --- |
|   sp.rpc.protocol  | 消息序列化协议        |  java，protobuf，kryo   |
|    sp.rpc.register-address |  注册中心地址      |  默认localhost:2181   |
|    sp.rpc.register-center-type |  注册中心类型     | nacos<br>zk|
|    sp.rpc.server-port |  服务端通信端口号     |  默认9999|
| sp.rpc.weight | 权重 |默认1  |  

**启动顺序：** 注册中心——> 服务端 ——> 客户端

**用法示例：** https://github.com/2YSP/rpc-example

**文章：** https://www.cnblogs.com/2YSP/p/13545217.html

# 三、TODO List
- 执行链动态Filter
- 支持链路追踪
- RPC上下文传递等


