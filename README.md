# InterouteSpring

What is InterouteSpring? InterouteSpring is the implementation of [Intetoure](https://github.com/JonathanxD/Interoute) for Spring RestTemplate.

## Simple router

```java
@RouterInterface(SpringRestBackend.class)
@BackendConfigurer(SimpleConfigurer.class)
public interface UserRouter {
    @RouteTo("/user")
    @Post
    SpringRoute<Void> register(@Body User user);
}
```

### Simple configurer

Configurer is required to setup the `root url` (example: `https://github.com`) and the `RestTemplate` to use to execute requests, here we will use a configurer with static variables to provide these values, but you are free to use other approach. 

Obs: At the moment you could not use Spring Dependency Injection naturally because `Interoute` does not totally integrate with `Spring`.

```java
public class SimpleConfigurer implements InterouteBackendConfigurer<SpringRestBackendConfiguration> {

    private static String ROOT_INVOCATION_PATH;
    private static RestTemplate REST_TEMPLATE;

    static void setup(String root, RestTemplate restTemplate) {
        SimpleConfigurer.ROOT_INVOCATION_PATH = root;
        SimpleConfigurer.REST_TEMPLATE = restTemplate;
    }

    @NotNull
    @Override
    public SpringRestBackendConfiguration configure(@NotNull SpringRestBackendConfiguration configuration) {
        return configuration.toBuilder()
                .rootInvocationPath(() -> ROOT_INVOCATION_PATH)
                .restTemplate(() -> REST_TEMPLATE).build();
    }
}
```


### Simple configuration

A configuration is required to create the router implementation (and to inject them with `@Autowired`).

```java
@Configuration
public class MyRouterConfiguration extends SpringInterouteConfiguration {
    @Bean
    public UserRouter userRouter() {
        return this.createRouter(UserRouter.class);
    }
}
```


### Using the Router

```java
@Service
public class UserService {
    private final UserRouter userRouter;
    
    @Autowired
    public UserService(UserRouter userRouter) {
        this.userRouter = userRouter;
    }
    
    public boolean registerUser(User user) {
        return this.userRouter.register(user)
                              .execute()
                              .get()
                              .flatMap(RouteRequestResponse::getStatusAsResult)
                              .map(HttpStatus::is2xxSuccessful)
                              .successOr(false);
    }
}
```

## Why execute and get

You should have noticed that we call `execute` and `get`. The invocation of a `route method` does not trigger the routing logic, instead it returns a `Router` which contains the specification of origin and target of routing and how to route invocation. The `execute` method of `Router` executes the routing logic asynchronously, and the `get` method blocks the `current thread` until the execution is finished.

## Custom path url

InterouteSpring also support URL with positional PathVariables:

```java
interface Router {
    @RouteTo("/user/{0}")
    Router<User> getUser(String name);
}
```

You could also add comments in front of the position of argument (but they will be ignored):

```java
interface Router {
    @RouteTo("/user/{0 name}")
    Router<User> getUser(String name);
}
```