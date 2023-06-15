package cn.sp.rpc.client.generic;

import cn.sp.rpc.client.core.MethodInvoker;

/**
 * @Author: Ship
 * @Description:
 * @Date: Created in 2023/6/15
 */
public class GenericServiceFactory {

    private static MethodInvoker methodInvoker;



    private GenericServiceFactory(){

    }

    public static GenericService getInstance(Class interfaceClazz){
        DefaultGenericService genericService = new DefaultGenericService();
        genericService.setInterfaceClazz(interfaceClazz);
        genericService.setMethodInvoker(methodInvoker);
        return genericService;
    }
}
