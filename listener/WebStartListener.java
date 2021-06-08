package com.wwq.meetings.framwork.listener;

import com.alibaba.druid.util.StringUtils;
import com.wwq.meetings.framwork.annotation.*;
import com.wwq.meetings.framwork.beanDefinition.BeanDefinitionContainer;
import com.wwq.meetings.framwork.exception.FramworkException;
import com.wwq.meetings.framwork.exception.GobleFramwordExcepetion;
import com.wwq.meetings.framwork.pojo.Beandefinition;
import com.wwq.meetings.framwork.pojo.MethodBeanDefinition;
import com.wwq.meetings.framwork.pojo.ParameterBeanDefinition;
import com.wwq.meetings.framwork.result.ResponseCode;
import com.wwq.meetings.framwork.utils.ExceptionContainerUtils;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.io.File;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 监听器找到配置文件，获取controller的路径并读取所有的controller文件 存入描述信息
 * 类的信息、方法、参数，存入对应的容器
 * @author wwq
 * @date 2021/5/17-15:37
 */
public class WebStartListener implements ServletContextListener {
    /**
     * 存放控制器的class对象
     */
   private static final List<Class> files = new ArrayList<>();
    /**
     * 存放控制器的文件对象，主要用来获取class
     */
    private static final List<File> allFiles = new ArrayList<>();

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        //获取配置文件中的异常标记
        getGlobleException(sce);
        //获取全局参数 通过读取配置文件的值，来获取controller所在文件的全限定名称
        String scanPath = checkConfig(sce);
        //找到全路径 即从磁盘的根目录到controller
        String absolutePath = getAbsolutePath(scanPath, sce);
        //获取所有的文件
        List<File> allFiles = getAllFiles(absolutePath);
        for (File f : allFiles) {
            System.out.println("找到所有文件,文件名称"+ f.getName());
        }
        //获取所有文件的class文件集合
        List<Class> classFile = getClassFile(allFiles,scanPath);
        //通过class对象装配bean信息
        addBeandefinitionToContainer(classFile);
    }

    /**
     * 将全局处理异常的类添加到异常处理的容器中
     * @param sce
     */
    private void getGlobleException(ServletContextEvent sce) {
        Class<?> clazz = getConfigaClass(sce);
        //判断配置文件是否异常处理注解
        EnableGlobleException annotation = clazz.getAnnotation(EnableGlobleException.class);

        if (null != annotation){
            String exception = annotation.exceptionPath();
            if (StringUtils.isEmpty(exception)){
                //没有注解异常处理
                throw new FramworkException(ResponseCode.EXCEPTION_NO_ERROR.getCode(),ResponseCode.EXCEPTION_NO_ERROR.getMessage());
            }
            try {
                //将全局异常类添加到容器中
                Class<?> aClass = Class.forName(exception);
                GobleFramwordExcepetion o = (GobleFramwordExcepetion) aClass.newInstance();
                ExceptionContainerUtils.setGobleFramwordExcepetion(o);
            } catch (Exception e) {
                throw new FramworkException(ResponseCode.CLASS_TO_ERROR.getCode(),ResponseCode.CLASS_TO_ERROR.getMessage());
            }
        }
    }

    /**
     * 装配控制器的信息、方法以及参数
     * 将每一个控制器+方法的mapping 存入map集合中
     * @param classList
     */
    private void addBeandefinitionToContainer(List<Class> classList){
        //判断该类是否拥有指定的注解
        for (Class clazz : classList) {
            //是否有controller注解
            Controller controller = (Controller) clazz.getAnnotation(Controller.class);
            if (null==controller){
                //没有controller注解，跳过
                continue;
            }else {
                //是否有requestMapping注解
                RequestMapping requestMapping = (RequestMapping) clazz.getAnnotation(RequestMapping.class);
                //接受控制器上面的地址
                String classRequestMapping = "";
                if (null!=requestMapping){
                        if (!StringUtils.isEmpty(requestMapping.mapping())) {
                            classRequestMapping = requestMapping.mapping();
                        }
                    }
                //获取所有方法
                Method[] declaredMethods = clazz.getDeclaredMethods();
                for (Method m : declaredMethods) {
                    RequestMapping requestmapping = m.getAnnotation(RequestMapping.class);

                    if(null!=requestmapping){
                        //声明beandefinition
                        Beandefinition beandefinition = new Beandefinition();
                        //类上面request mapping地址
                        beandefinition.setRequestMapping(classRequestMapping);
                        beandefinition.setClazz(clazz);
                        beandefinition.setTypeName(clazz.getName());
                        //方法上的地址
                        String mapping = requestmapping.mapping();
                        //方法必须填写mapping地址
                        if (StringUtils.isEmpty(mapping)){
                            throw new FramworkException(ResponseCode.CONFIG_ANNOATION_ERROR.getCode(), ResponseCode.CONFIG_ANNOATION_ERROR.getMessage());
                        }
                        //设置方法的参数、方法的返回类型、注解对象、方法名称、方法注解值、方法对象
                        MethodBeanDefinition<Object> methodBeanDefinition = new MethodBeanDefinition<>();
                        methodBeanDefinition.setReturnType(m.getReturnType().getTypeName());
                        methodBeanDefinition.setAnnotationClass(RequestMapping.class);
                        methodBeanDefinition.setMethodName(m.getName());
                        methodBeanDefinition.setRequestMapping(mapping);
                        methodBeanDefinition.setMethod(m);

                        //判断是否需要加密、解密
                        BeforeAdvisor annotation1 = m.getAnnotation(BeforeAdvisor.class);
                        AfterAdvisor annotation2 = m.getAnnotation(AfterAdvisor.class);

                        if (null!=annotation1){
                            methodBeanDefinition.setBeforeAdvisorOrNot(true);
                        }
                        if (null!=annotation2){
                            methodBeanDefinition.setAfterAdvisorOrNot(true);
                        }


                        //判断方法是否被responseBody注解
                        ResponseBody annotation = m.getAnnotation(ResponseBody.class);
                        if (null!=annotation){
                            methodBeanDefinition.setResponseBodyOrNot(true);
                        }else {
                            methodBeanDefinition.setResponseBodyOrNot(false);
                        }

                        //参数个数
                        int parameterCount = m.getParameterCount();
                        //参数的类型
                        Class<?>[] parameterTypes = m.getParameterTypes();
                        //获取所有的参数
                        Parameter[] parameters = m.getParameters();
                        //遍历所有的参数
                        List<ParameterBeanDefinition> parameterList = new ArrayList<>();
                        //初始化每一个参数
                        for (int i = 0; i < parameterCount; i++) {
                            //参数的名称、参数出现的位置、参数的类型
                            ParameterBeanDefinition<Object> parameterBeanDefinition = new ParameterBeanDefinition<>();
                            parameterBeanDefinition.setParameterName(parameters[i].getName());
                            parameterBeanDefinition.setPosition(i+1);
                            parameterBeanDefinition.setParameterType(parameterTypes[i].getTypeName());
                            //判断该参数是否被注解requestBody
                            RequestBody requestBody = parameters[i].getAnnotation(RequestBody.class);
                            if (null!=requestBody){
                                parameterBeanDefinition.setRequestBodyOrNot(true);
                            }else {
                                parameterBeanDefinition.setRequestBodyOrNot(false);
                            }
                            //将参数放入集合中，以便方法进行维护
                            parameterList.add(parameterBeanDefinition);
                        }
                        //装配对象参数-》方法-》类
                        methodBeanDefinition.setMethodParameters(parameterList);
                        beandefinition.setMethodBeanDefinition(methodBeanDefinition);
                        Map<String, Beandefinition> container = BeanDefinitionContainer.getRequestPathAndBeanDefinitionContainer();
                        //将每一个路径的值，对应的bean信息存入容器中key：控制器的mapping+方法的mapping，value:beanInfo
                        container.put(beandefinition.getRequestMapping()+beandefinition.getMethodBeanDefinition().getRequestMapping(),beandefinition);

                    }

                }
            }
            }
        }

    /**
     * 返回所有文件的class对象
     * @param allFiles
     * @param scanPath
     * @return 获取所有的class文件
     */
    private List<Class> getClassFile(List<File> allFiles,String scanPath) {
        for (File f : allFiles) {
            if (f.getName().endsWith(".class")){
                String absolutePath = f.getAbsolutePath();
                System.out.println("该文件的全路径名称："+absolutePath);
                absolutePath = absolutePath.replace("\\",".");
                //截取需要路径
                absolutePath = absolutePath.substring(absolutePath.indexOf(scanPath),absolutePath.lastIndexOf("."));
                System.out.println("截取的全限定名称"+absolutePath);

                Class clazz = null;
                try {
                     clazz = Class.forName(absolutePath);
                } catch (ClassNotFoundException e) {
                    System.out.println("转换class对象出错"+e);
                }
                files.add(clazz);
            }
        }
        return files;
    }

    /**
     * 根据控制器全路径获取 所有的文件
     * @param absolutPath 绝对路径
     * @return 控制器中包下的所有文件
     */
    private List<File> getAllFiles(String absolutPath){
        System.out.println("通过该路径获取控制器所在文件的的file对象："+absolutPath);
        File file = new File(absolutPath);
        File[] files = file.listFiles();
        if (null != files && files.length != 0) {
            for (File f : files) {
                if (f.isFile()) {
                    allFiles.add(f);
                } else {
                    getAllFiles(f.getAbsolutePath());
                }
            }
        }
        return allFiles;
    }

    /**
     * 获取controller所在绝对路径
     * @param scannerPath
     * @param sce
     * @return 获取控制器的绝对路径
     */
    private String getAbsolutePath(String scannerPath,ServletContextEvent sce){
        //idea 默认部署在ROOT目录下 getRealPath 可以定位到ROOT
        String path = sce.getServletContext().getRealPath("/") + "\\WEB-INF\\classes\\";
        System.out.println("项目的部署路径"+path);
        scannerPath = scannerPath.replace(".","\\");
         path = path + scannerPath;
        System.out.println("获取的全路径"+path);
        return path;
    }

    /**
     * 获取配置文件的注解准确性，解析并提取value
     * @param sce
     * @return controller所在全限定名称
     */
    private String checkConfig(ServletContextEvent sce) {
        Class<?> clazz = getConfigaClass(sce);
        //判断是否有配置住注解 并获取该注解 即Configuration
        Configuration configuration = clazz.getAnnotation(Configuration.class);
        if (null==configuration){
            throw new FramworkException(ResponseCode.CONFIG_NO_ERROR.getCode(), ResponseCode.CONFIG_NO_ERROR.getMessage());
        }
        //获取配置文件注解的值 即controller所在包的全限定名称
        String scannerPath = configuration.scannerPath();
        System.out.println("控制器所在文件的全限定名称："+scannerPath);
        if (StringUtils.isEmpty(scannerPath)){
            throw new FramworkException(ResponseCode.CONFIG_NO_SCANNERPATH_ERROR.getCode(), ResponseCode.CONFIG_NO_SCANNERPATH_ERROR.getMessage());
        }
        return scannerPath;
    }

    private Class<?> getConfigaClass(ServletContextEvent sce) {
        //获取配置文件web.xml的全局参数 即config配置文件所在位置全限定名称
        String config = sce.getServletContext().getInitParameter("config");
        //配置文件的class文件
        Class<?> clazz;
        try {
            clazz = Class.forName(config);
        } catch (ClassNotFoundException e) {
            throw new FramworkException(ResponseCode.CONFIG_ERROR.getCode(),ResponseCode.CONFIG_ERROR.getMessage());
        }
        return clazz;
    }


    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {

    }
}
