# EamonUtils - Java Web

[![License](https://img.shields.io/badge/license-Apache%202.0%20License-blue.svg)](https://github.com/ymxiong/util/blob/master/LICENSE)

EamonUtils is a small library that simplifies your web development.

## Project Contains

+ [Annotation](#Annotation)

+ [File](#File)

+ [Permission](#Permission)

+ [Security](#Security)

+ [Status](#Status)
+ [Task](#Task)

## [Annotation](id:Annotation)



## [File](id:File)



## [Permission](id:Permission)

### Target

Using `@Permission` and `@PermissionLimit` annotation to simplify your permission control. 

### Installation

Add the dependency to your pom.xml

```xml
<dependency>
    <groupId>cc.eamon.open</groupId>
    <artifactId>permission</artifactId>
    <version>${permission-version}</version>
</dependency>
```

### Usage

#### Start Up

1. Add `@Permission` annotation to your controller, the value of `@Permission` is required

2. Add `@PermissionLimit` annotation to your method, the value of `@PermissionLimit` can be empty. 

3. Method with `@PermissionLimit`(such as: adminLogin()) will be intercepted, others will not.

   ```java
   @Permission("user")
   @RequestMapping("/user")
   @RestController
   public class UserController {
   
       @RequestMapping(value = "login", method = RequestMethod.GET)
       @ResponseBody
       public Object login(){
           return "Permission Test";
       }
       
       @PermissionLimit
       @RequestMapping(value = "admin/login", method = RequestMethod.GET)
       @ResponseBody
       public Object adminLogin(){
           return "Permission Admin Test";
       }
   
   }
   ```

4. Complie your project, Abstract class`DefaultChecker.class` will automatically generate.

5. Define your own checker. It is recommended to inherit `DefaultChecker`

   ```java
   public class RoleMethodChecker extends DefaultChecker {
   
       @Override
       public boolean checkRole(...) throws StatusException {
           return false;
       }
       
       @Override
       public boolean checkUserAdminLogin(...) throws StatusException {
           return super.checkUserAdminLogin(request, response);
       }
   
       @Override
       ...
       
   }
   ```

##### Integration with SpringMVC

```xml
<bean id="roleMethodChecker" class="?.RoleMethodChecker"/>
<mvc:interceptors>
	<bean class="cc.eamon.open.permission.mvc.PermissionInterceptor" >
		<property name="checker" ref="roleMethodChecker" />
	</bean>
</mvc:interceptors>
```
##### Integration with SpringBoot

```java
@Configuration
public class RoleMethodConfig extends WebMvcConfigurerAdapter {

    @Bean
    public RoleMethodChecker roleMethodChecker(){
        return new RoleMethodChecker();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new PermissionInterceptor(roleMethodChecker()))
            .addPathPatterns("/**");
        super.addInterceptors(registry);
    }
}
```

>  Next is the time to witness miracles

#### Api Management

1. Override the method from DefaultChecker.

2. Decide whether to intercept by return statement.

   ***Example Pass***

   ```java
   @Override public boolean checkUserAdminLogin(...){return true;}
   ```

   ***Result***

   >  localhost:8080/user/admin/login -> Permission Admin Test

   ***Example Intercept***

   ```java
   @Override public boolean checkUserAdminLogin(...){return false;}
   ```

   ***Result***

   >  localhost:8080/user/admin/login -> {"result":false ...}

3. You can get the generated method description from `PermissionValue.class`, or use your own way to manage the method.

4. Manage the method permission information using your own storage solution, you can choose a relational database, a memory database, or just use memory.

   ***Simple Example***

   ```java
   @Autowired
   private UserService userService;
   
   @Override
   public boolean checkUserAdminLogin(...) ... {
       Map<String, String[]> reqMap = request.getParameterMap();
       String account = reqMap.get("account")[0];
       User user = userService.findOneByAccount(account);
       for (String method:user.getMethods()){
           if (method.equals(PermissionValue.USER_ADMINLOGIN))return true;
       }
       return false;
   }
   ```


#### Role Management

1. Add `permission.properties` to your classpath. 

   ```properties
   #Super Admin Accessible Interface
   SUPER_PRIVATE	=	SUPER
   #Admin Accessible Interface
   ADMIN_PRIVATE	=	SUPER,ADMIN
   #User Personal Accessible Interface
   USER_PRIVATE	=	SUPER,ADMIN,USER
   #User Accessible Interface After Login
   USER_PUBLIC		=	SUPER,ADMIN,USER
   ```

2. Manage the role information using your own storage solution, you can choose a relational database, a memory database, or just use memory.

   ***Example***

   | id    |   password   | role  |
   | ----- | :----------: | :---: |
   | 10001 | \*\*\*\*\*\* | SUPER |
   | 10002 | *\*\*\*\*\*  | ADMIN |

3. Complie your project. `PermissionRole.class` will automatically generate.

   ```java
   public class PermissionRole {
     public static final String ADMIN_PRIVATE = "admin_private";
   
     public static final String USER_PRIVATE = "user_private";
   
     public static final String USER_PUBLIC = "user_public";
   
     public static final String SUPER_PRIVATE = "super_private";
   }
   ```



#### Extention

1. Implement your own checker

```java
public class YourOwnChecker implements PermissionChecker {

    @Override
    public boolean preHandle(...) throws Exception {
        return true;
    }

    @Override
    public void postHandle(...) throws Exception {}

    @Override
    public void afterCompletion(...) throws Exception {}

    @Override
    public boolean check(...) throws StatusException {
        return true;
    }

}
```



## [Security](id:Security)



## [Status](id:Status)



## [Task](id:Task)





## License

```
Copyright 2018 eamon

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```