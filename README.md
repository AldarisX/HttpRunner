# HttpRunner

HttpRunner是一款请求工具，想让测试时更轻松

## 使用到的关键依赖

* JDK17(java.base,java.sql,java.management,java.desktop,java.logging)
* groovy(大部分groovy模块都引入了)
* httpclient
* gson、fastjson
* flatlaf(com.formdev.flatlaf仿intellij主题的swing主题)
* intellij ui-design

## 功能

* 对目标地址发送指定的请求(GET、POST)
* 保存当前的请求地址、参数、使用的脚本方便以后加载立即使用
* 可以配置传递给脚本的变量传递自定义参数
* 支持插件(可以实现任意功能，jvm做得到的话。。。，任意功能除了修改UI，只允许在Plugins菜单下新增)
* (开发中)连续请求，方便对业务流程测试
* (开发中)压力测试，对api做简易的压力测试

## 区别于其他常见工具

* 方便使用项目特定的加密、签名要求。通常在postman等工具上实现很麻烦
* 可以从任意位置加载配置。对script/env.groovy,script/script.groovy重写即可实现
* 使用Groovy作为脚本(也可以从任意位置加载)，请求前会将需要的参数传递给选择的脚本，请求结束后会将请求前参数与响应传递给选择的脚本
* 使用Groovy作为扩展，可以添加自己需要的功能。比如JMX监控(script/plugins/jmx.groovy已经实现简单了一个的版本)

