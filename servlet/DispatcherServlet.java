package com.wwq.meetings.framwork.servlet;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.wwq.meetings.framwork.pojo.Model;
import com.wwq.meetings.framwork.beanDefinition.BeanDefinitionContainer;
import com.wwq.meetings.framwork.exception.FramworkException;
import com.wwq.meetings.framwork.pojo.Beandefinition;
import com.wwq.meetings.framwork.pojo.ModelAndView;
import com.wwq.meetings.framwork.pojo.ParameterBeanDefinition;
import com.wwq.meetings.framwork.result.ResponseCode;
import com.wwq.meetings.framwork.utils.Constaces;
import com.wwq.meetings.framwork.utils.DateConvertUtils;
import com.wwq.meetings.framwork.utils.ExceptionContainerUtils;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.converters.DateConverter;

import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * @author wwq
 * @date 2021/5/17-17:46
 */
public class DispatcherServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws  IOException {
        this.doPost(req,resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws  IOException {

        Model model = new Model();
        req.setCharacterEncoding("UTF-8");
        /**
         * 1 获取请求地址的uri就是8080后面那一段，？前面部分
         * 2 去容器中找到这个uri
         * 3 获取method
         * 4 获取 参数类型 参数名称 -》给这个参数设置数据
         * 5 method.invoke执行这个方法
         * 6 将结果转换为json格式、或者重定向、转发
         */
        String requestURI = req.getRequestURI();
        //获取容器与uri进行对比，找到需要执行的方法
        Map<String, Beandefinition> container = BeanDefinitionContainer.getRequestPathAndBeanDefinitionContainer();
        //得到了该uri对应的bean信息
        Beandefinition beandefinition = container.get(requestURI);
        //获取到了应该执行bean信息
        if (null != beandefinition){
            //当前方法的method
            Method method = beandefinition.getMethodBeanDefinition().getMethod();
            //存储当前控制器对象
            Object target;
            try {
                target = beandefinition.getClazz().newInstance();
                //处理参数 将参数通过保存的parameter信息中的名称 来获取请求数据中的value
                Object[] params = handleParameters(req,resp,beandefinition,model);
                Object result = null;
                try {
                    //执行该方法
                    result = method.invoke(target, params);
                }catch(InvocationTargetException  e){
                    // TODO 调用异常处理模板
                    // 判断是否有异常需要进行处理,抛出的异常是invocationTargetException,通过getTargetException获取原始异常信息
                    if(null!=ExceptionContainerUtils.getGobleFramwordExcepetion()){
                        ExceptionContainerUtils.getGobleFramwordExcepetion().prehandleException((Exception) ((InvocationTargetException) e.getTargetException()).getTargetException(),req,resp);
                    }
                }


                //判断是否是modelansview
                if ("com.wwq.meetings.framwork.pojo.ModelAndView".equals(beandefinition.getMethodBeanDefinition().getReturnType())){
                    ModelAndView modelAndView = (ModelAndView) result;
                    //返回路径
                    String viewName = modelAndView.getViewName();
                    //返回的结果
                    Map<String, Object> attribute = modelAndView.getAttribute();
                    listMap(req,attribute);
                    req.getRequestDispatcher(viewName).forward(req,resp);
                }
                // 若不是返回的ModelAndView
                // 根据方法返回的结果处理，是否，返回值、重定向、转发
                jumpPage(req, resp, model, beandefinition,  result);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }else {
            //请求地址错误
            throw new FramworkException(ResponseCode.REQUEST_PATH_ERROR.getCode(),ResponseCode.REQUEST_PATH_ERROR.getMessage());
        }
    }

    /**
     * 根据方法的返回值，判断处理：返回值、重定向、转发
     * @param req
     * @param resp
     * @param model
     * @param beandefinition
     * @param results
     * @throws IOException
     * @throws ServletException
     */
    private void jumpPage(HttpServletRequest req, HttpServletResponse resp, Model model, Beandefinition beandefinition, Object results) throws IOException, ServletException {
        //判断方法是否返回json还是跳转界面 通过判断是否有注解 responseBody
        if (beandefinition.getMethodBeanDefinition().isResponseBodyOrNot()){
            String s = JSON.toJSONString(results);
            sendToResponse(s, req, resp);
            return;
        }
        String result = String.valueOf(results);
        //没有responsBody 跳转界面
        if (result.startsWith("r:")){
            //重定向地址
            System.out.println("重定向地址："+ result);
            resp.sendRedirect(result.substring(2));
        }else {
            //转发
            Map<String, Object> att = model.getAttributes();
            listMap(req, att);
            req.getRequestDispatcher(result).forward(req, resp);
        }
    }

    /**
     * 将map封装到request
     * @param req
     * @param att
     */
    private void listMap(HttpServletRequest req, Map<String, Object> att) {
        Set<Map.Entry<String, Object>> entries = att.entrySet();
        System.out.println("需要存入域中的值："+entries);
        Iterator<Map.Entry<String, Object>> iterator = entries.iterator();
        //将转发的数据存入域对象 request
        while (iterator.hasNext()){
            Map.Entry<String, Object> next = iterator.next();
            req.setAttribute(next.getKey(),next.getValue());
        }
    }

    /**
     * 将一个请求中的参数获取，并于参数的bean信息进行对比，通过名称在请求中获取值，存入参数数组中
     * @param req 一个请求
     * @param beandefinition bean信息
     * @return 参数结果的集合
     */
    private Object[] handleParameters(HttpServletRequest req, HttpServletResponse resp, Beandefinition beandefinition,Model model) throws IOException, ClassNotFoundException, IllegalAccessException, InstantiationException, InvocationTargetException {
        //获取所有的参数集合
        List<ParameterBeanDefinition> parameterBeanDefinitions = beandefinition.getMethodBeanDefinition().getMethodParameters();
        //申明一个参数的数组 存放参数的值
        Object[] pp=new Object[parameterBeanDefinitions.size()];
        int parameterCount;
        //循环每一个参数信息
        for (int i = 0; i < parameterBeanDefinitions.size(); i++) {
            parameterCount=i;
            ParameterBeanDefinition p = parameterBeanDefinitions.get(i);
            //参数的基本信息
            String parameterName = p.getParameterName();
            String parameterType = (String) p.getParameterType();
            System.out.println("当前参数的名称："+parameterName);
            System.out.println("当前参数的类型："+parameterType);
            //判断参数是否是基本数据类型
            boolean b = jugmentTypeIsJavaType(parameterType);
            if (b){
                //获取参数的value值
                String parameter = req.getParameter(parameterName);
                assembleParameters(pp, parameterCount, parameterType, parameter);
            }else if("javax.servlet.http.HttpServletRequest".equals(parameterType)){
                pp[parameterCount]=req;
            }else if("javax.servlet.http.HttpServletResponse".equals(parameterType)){
                pp[i]=resp;
            }else if("com.wwq.meetings.framwork.pojo.Model".equals(parameterType)){
                pp[i] = model;
            }else if ("java.lang.String[]".equals(parameterType)){
                //前端传递的是数组
                String[] parameter = req.getParameterValues(parameterName);
                pp[i]=parameter;
                //System.out.println("传递的数组："+Arrays.toString(parameter));
            }else{
                    //判断需要处理的参数类型，普通的json,如：{"id":1,"username":"www}是否需要转换为json格式
                    if (p.isRequestBodyOrNot()){
                        //post才能进行这个请求，返回json数据
                        String methodName = req.getMethod();
                        if ("GET".equals(methodName)){
                            throw new FramworkException(ResponseCode.CONFIG_METHOD_ERROR.getCode(), ResponseCode.CONFIG_METHOD_ERROR.getMessage());
                        }
                        //请求数据的值 post提交方式
                        String data = getData(req);
                        try {
                            //将json数据转换为对象
                            Object jsonObject = JSON.parseObject(data, Class.forName(parameterType));
                            pp[i]=jsonObject;
                        }catch (Exception e){
                            throw new FramworkException(ResponseCode.CONFIG_JSON_ERROR.getCode(), ResponseCode.CONFIG_JSON_ERROR.getMessage());
                        }
                    }else {
                        /**
                         * 传递的是其他的Java对象,如：user
                         * 类型是该类的全路径如：user=>com.wwq.pojo.user
                         * 类型是该类的全路径如：student=>com.wwq.pojo.student
                         */
                        System.out.println("该参数的类型；"+parameterType);
                        //获取对象并赋值给该对象
                        Class<?> clazz = Class.forName(parameterType);
                        Object o = clazz.newInstance();
//                        // post提交的参数通过流来获取请求体里面的数据
//                        String data = getData(req);
//                        Map<String,Object> parse = (Map) JSON.parse(data);
                        //get提交方式
                        Map<String, String[]> parse = req.getParameterMap();
                        ConvertUtils.register(new DateConvertUtils(),Date.class);
                        BeanUtils.populate(o,parse);
                        pp[i]=o;
                    }
                 }
            }
        return pp;
    }

    /**
     * 处理Java的基本数据类型，直接获取值，然后进行强制转换 并存入数组中
     * @param pp
     * @param parameterCount
     * @param parameterType
     * @param parameter
     */
    private void assembleParameters(Object[] pp, int parameterCount, String parameterType, String parameter) {
        switch (parameterType){
            case "int":
            case "java.lang.Integer":
                // TODO 判断是否需要解密

                //转换成int类型
                int p1 = Integer.parseInt(parameter);
                //设置到这个数组中去
                pp[parameterCount]=p1;
                parameterCount++;
                break;
            case  "String":
            case  "java.lang.String":
                String p2 = parameter;
                pp[parameterCount] = p2;
                parameterCount++;
                break;
            case "float":
            case "java.lang.Float":
                float p3 = Float.parseFloat(parameter);
                pp[parameterCount] = p3;
                parameterCount++;
                break;
            default:
                break;
        }
    }

    /**
     * 将请求流中的数据读取出来，读取的格式为json如：{"id":1,"name":"hello,wwq","password":"123"}
     * @param req
     * @return 请求数据中的数据
     * @throws IOException
     */
    private String getData(HttpServletRequest req) throws IOException {
        req.setCharacterEncoding("utf-8");
        ServletInputStream inputStream = req.getInputStream();
        byte[] bytes = new byte[4096];
        inputStream.read(bytes);
        //读取完毕之后进行转换string,格式为：{"id":1,"name":"hello,wwq","password":"123"}
        String str = new String(bytes, StandardCharsets.UTF_8);
        System.out.println(str);
        inputStream.close();
        return str;
    }

    /**
     * 这个方法就是用来判断这个数据类型是不是Java中基本数据类型
     * @param parameterType
     * @return
     */
    private boolean jugmentTypeIsJavaType(String parameterType) {

        //强制类型转换数据类型
        String parameterTypes=  parameterType;
        //遍历Java中基本的数据类型 来进行判断是不是  如果是 那么返回true   否则返回 false
        for (String p: Constaces.getJavaTypes()) {
            if(parameterTypes.equals(p)){
                return true;
            }
        }
        return false;
    }

    /**
     * 将结果响应给浏览器 结果为：需要的数据
     * @param s 响应的结果
     * @param req 请求数据
     * @param resp 响应数据
     */
    private void sendToResponse(String s, HttpServletRequest req, HttpServletResponse resp) {
        resp.setContentType("text/html;charset=utf-8");
        try {
            resp.getWriter().print(s);
            resp.getWriter().flush();
            resp.getWriter().close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
