package cn.sp.rpc.client.generic;

import cn.sp.rpc.client.core.MethodInvoker;


/**
 * @Author: Ship
 * @Description:
 * @Date: Created in 2023/6/15
 */
public class DefaultGenericService implements GenericService {

    private MethodInvoker methodInvoker;

    private Class interfaceClazz;

    @Override
    public Object $invoke(String methodName, String[] parameterTypeNames, Object[] args) {
        return methodInvoker.$invoke(interfaceClazz, methodName, parameterTypeNames, args);
    }


    public void setMethodInvoker(MethodInvoker methodInvoker) {
        this.methodInvoker = methodInvoker;
    }

    public void setInterfaceClazz(Class interfaceClazz) {
        this.interfaceClazz = interfaceClazz;
    }
}
