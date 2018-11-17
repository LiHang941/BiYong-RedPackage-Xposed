## BiYong红包插件开发记录

> Git: https://github.com/wawa2222/BiYong-RedPackage-Xposed.git


## 更新记录
这个项目很久之后我都没有管了,之后一位老大哥找到我,问我搞的出来不,当然,肯定搞的出来,然后他就找人花了几百大洋整了源码给我,我看到也很兴奋,花了1天时间吧插件写出来,之后也测试了很久,修复了很多问提.

目前各种红包都能抢,但是答题红包是随机的,因为不知道答案.




### - BiYong聊天工具逆向

第一步查壳

- 网易易盾加壳,蛋疼。。。

- 找历史版本,看是否有壳,我这里找到个没壳的,直接反编译,看到些敏感信息的类文件


编写Xposed文件，这里我直接用别人写好的插件改了下


```java
XposedHelpers.findAndHookMethod(XposedHelpers.findClass("java.lang.ClassLoader", loadPackageParam.classLoader), "loadClass", String.class, new XC_MethodHook() {
    @Override
    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
        super.afterHookedMethod(param);
        Class<?> clazz = (Class<?>) param.getResult();
        if( clazz!= null && clazz.getName().indexOf("org.telegram")!=-1){
            Method[] declaredMethods = clazz.getDeclaredMethods();
            Log.e("[执行方法]:" , "-------------init--------------" + Arrays.toString(declaredMethods));
            for (final Method method : declaredMethods) {
                if(!set.contains(method)){
                    set.add(method);
                    XposedBridge.hookMethod(method, new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            super.afterHookedMethod(param);
                            Log.e("[执行方法]:" ,
                                    "class -- " + param.thisObject == null ? null : param.thisObject.getClass()
                                    + "-- methodName -- "  + param.method.getName()
                                    + "-- ParameterTypes --" + Arrays.toString(method.getParameterTypes())
                                    + "-- result -- " + param.getResult()
                                    + "-- param.args -- " + Arrays.toString(param.args) + "--" );
                        }
                    });
                }
            }
        }
    }
});


```

- Hook注入看日志

> 未领取红包领取红包日志，领取红包再领取红包的日志

- 有了这些日志,当然hook到抢红包的方法就是时间问题了

- 记得用文件差异分析，今天先溜了



###  2018.6.24 第二次分析

- 抢红包操作走的 ReceiveRedPacketActivity.class 这个类
- 红包数据是this.getIntent().getSerializableExtra("red_packet_block")); // 这个应该是红包id还是怎么的
- 新老版本的数据有变化，不过流程还是差不多的
- 本次使用了新的hook代码 
- 抢红包应该是initData() 方法  


#### 问题

- 对于加壳应用怎么创建 ReceiveRedPacketActivity 并传参数
- 没有hook 到接受红包数据

```java

XposedHelpers.findAndHookMethod(XposedHelpers.findClass("java.lang.ClassLoader", loadPackageParam.classLoader)
                , "loadClass", String.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                Class<?> clazz = (Class<?>) param.getResult();
                if( clazz!= null && clazz.getName().indexOf("org.telegram")!=-1){
                    Method[] declaredMethods = clazz.getDeclaredMethods();
                    Log.e("[执行方法]:" , "-------------init--------------" + Arrays.toString(declaredMethods));
                    for (final Method method : declaredMethods) {
                        if(!set.contains(method)){
                            set.add(method);
                            XposedBridge.hookMethod(method, new XC_MethodHook() {
                                @Override
                                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                                    super.afterHookedMethod(param);
                                    Log.e("[执行方法]:" ,
                                            "class -- " + param.thisObject == null ? null : param.thisObject.getClass()
                                            + "-- methodName -- "  + param.method.getName()
                                            + "-- ParameterTypes --" + Arrays.toString(method.getParameterTypes())
                                            + "-- result -- " + param.getResult()
                                            + "-- param.args -- " + Arrays.toString(param.args) + "--" );

                                    if (param.thisObject != null){

                                        Field[] declaredFields = param.thisObject.getClass().getDeclaredFields();


                                        Log.e("[执行方法-查看属性]:" , "=================start==================");
                                        for (Field declaredField : declaredFields) {
                                            declaredField.setAccessible(true);
                                            Object o = declaredField.get(param.thisObject);
                                            Log.e("[执行方法-查看属性]:" ,( "name -- " + declaredField.getName() + " -- class -- "+  declaredField.getType())  +  (o!= null ?( " -- result -- " + o.toString()) : null));
                                        }
                                        Log.e("[执行方法-查看属性]:" , "=================end==================");
                                    }
                                }
                            });
                        }
                    }
                }
            }
        });


```

## 下周继续整




