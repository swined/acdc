package util;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;                   
import java.lang.reflect.Proxy;                    
import java.util.HashMap;                          
import java.util.HashSet;                          

public class EventDispatcher {

    private final ProxyHandler handler = new ProxyHandler();
    private final HashSet<Object> subscribers = new HashSet<Object>();
    private final HashMap<Class<?>, Object> proxies = new HashMap<Class<?>, Object>();

    private class ProxyHandler implements InvocationHandler {
        
        private Object invoke(Method m, Object[] args) throws InvocationTargetException, IllegalAccessException {
            Object result = getDefaultValue(m.getReturnType());
            final Class<?> mc = m.getDeclaringClass();
            for (Object subscriber : subscribers)
                if (mc.isAssignableFrom(subscriber.getClass()))
                    result = m.invoke(subscriber, args);
            return result;
        }

        public Object invoke(Object proxy, Method m, Object[] args) throws Throwable {
            try {
                return invoke(m, args);
            } catch (InvocationTargetException e) {
                throw e.getCause();
            }
        }

    }

    private static Object getDefaultValue(Class<?> cl) {
        if (cl == void.class)
            return null;
        Object array = Array.newInstance(cl, 1);
        return Array.get(array, 0);
    }
    
    public void register(Object subscriber) {
        subscribers.add(subscriber);
    }

    public void unregister(Object subscriber) {
        subscribers.remove(subscriber);
    }

    private Object createProxy(Class<?> cl) {
        ClassLoader loader = cl.getClassLoader();
        Class<?>[] interfaces = new Class[]{ cl };
        return Proxy.newProxyInstance(loader, interfaces, handler);
    }

    private Object getProxy(Class<?> cl) {
        Object proxy = proxies.get(cl);
        if (proxy == null) {
            proxy = createProxy(cl);
            proxies.put(cl, proxy);
        }
        return proxy;
    }

    @SuppressWarnings("unchecked")
    public<T> T invoke(Class<T> cl) {
        return (T)getProxy(cl);
    }

}
