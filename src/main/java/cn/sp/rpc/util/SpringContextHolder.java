package cn.sp.rpc.util;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Ship
 * @version 1.0.0
 * @description:
 * @date 2023/06/16
 */
public class SpringContextHolder implements ApplicationListener<ContextRefreshedEvent> {

    private AtomicBoolean init = new AtomicBoolean(false);

    private static ApplicationContext applicationContext;

    private SpringContextHolder() {

    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (init.get()) {
            return;
        }
        applicationContext = event.getApplicationContext();
        init.compareAndSet(false, true);
    }

    /**
     * 根据类获取bean
     *
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> T getBean(Class<T> clazz) {
        return applicationContext.getBean(clazz);
    }


    /**
     * 根据名称获取bean
     * @param name
     * @return
     */
    public static Object getBean(String name) {
        return applicationContext.getBean(name);
    }

}
