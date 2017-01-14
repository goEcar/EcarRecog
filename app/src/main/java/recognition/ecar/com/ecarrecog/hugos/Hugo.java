package recognition.ecar.com.ecarrecog.hugos;

import android.os.Build;
import android.os.Looper;
import android.os.Trace;
import android.util.Log;

import com.ecar.ecaraspectjlib.HugoUtil;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.CodeSignature;
import org.aspectj.lang.reflect.MethodSignature;

import java.util.concurrent.TimeUnit;

/**
 * 示例代码  在方法前后打印方法的执行时间，类名，方法名，参数值等信息
 */
@Aspect
public class Hugo {

    /*****************
     * 定义注解的类型
     ******************/

    //带有DebugLog注解的所有类
    @Pointcut("within(@com.ecar.recogdemo.anotation.DebugLog *)")
    public void withinAnnotatedClass() {
    }

    //在带有DebugLog注解的所有类，除去synthetic修饰的方法
    @Pointcut("execution(!synthetic * *(..)) && withinAnnotatedClass()")
    public void methodInsideAnnotatedType() {
    }

    //在带有DebugLog注解的所有类，除去synthetic修饰的构造方法
    @Pointcut("execution(!synthetic *.new(..)) && withinAnnotatedClass()")
    public void constructorInsideAnnotatedType() {
    }

    //在带有DebugLog注解的方法
    @Pointcut("execution(@com.ecar.recogdemo.anotation.DebugLog * *(..)) || methodInsideAnnotatedType()")
    public void method() {
    }

    //在带有DebugLog注解的构造方法
    @Pointcut("execution(@com.ecar.recogdemo.anotation.DebugLog *.new(..)) || constructorInsideAnnotatedType()")
    public void constructor() {
    }


    /*****************
     * 执行方法
     ******************/

    @Around("method() || constructor()")
    public Object logAndExecute(ProceedingJoinPoint joinPoint) throws Throwable {
        //执行方法前
        enterMethod(joinPoint);
        long startNanos = System.nanoTime();

        //执行方法本体
        Object result = joinPoint.proceed();

        //执行方法后
        exitMethod(joinPoint, result, startNanos);
        return result;
    }


    private void enterMethod(JoinPoint joinPoint) {

        CodeSignature codeSignature = (CodeSignature) joinPoint.getSignature();
        /************
         * 获取值
         *********************/

        //获取类名
        Class<?> clazz = HugoUtil.getCls((ProceedingJoinPoint) joinPoint);

        //获取方法名
        String methodName = HugoUtil.getMethodName((ProceedingJoinPoint) joinPoint);

        //参数名
        String[] parameterNames = HugoUtil.getParmNames((ProceedingJoinPoint) joinPoint);

        //参数值
        Object[] parameterValues = HugoUtil.getParmValues((ProceedingJoinPoint) joinPoint);

        /************
         * 打印
         *********************/

        StringBuilder builder = new StringBuilder("开始  ");
        builder.append(methodName).append('(');
        for (int i = 0; i < parameterValues.length; i++) {
            if (i > 0) {
                builder.append(", ");
            }
            builder.append(parameterNames[i]).append('=');
            builder.append(parameterValues[i] == null ? "" : parameterValues[i].toString());
        }
        builder.append(')');

        if (Looper.myLooper() != Looper.getMainLooper()) {
            builder.append(" [Thread:\"").append(Thread.currentThread().getName()).append("\"]");
        }

        Log.i(asTag(clazz), builder.toString());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            final String section = builder.toString().substring(2);
            Trace.beginSection(section);
        }
    }

    private void exitMethod(JoinPoint joinPoint, Object result, long startNanos) {
        long stopNanos = System.nanoTime();
        long lengthMillis = TimeUnit.NANOSECONDS.toMillis(stopNanos - startNanos);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            Trace.endSection();
        }


        Class<?> cls = HugoUtil.getCls((ProceedingJoinPoint) joinPoint);
        String methodName = HugoUtil.getMethodName((ProceedingJoinPoint) joinPoint);
        boolean hasReturnType = joinPoint.getSignature() instanceof MethodSignature  //是否是方法
                && ((MethodSignature) joinPoint.getSignature()).getReturnType() != void.class;

        StringBuilder builder = new StringBuilder("结束 ")
                .append(methodName)
                .append(" [")
                .append(lengthMillis)
                .append("ms]");

        if (hasReturnType) {
            builder.append(" = ");
            builder.append(result == null ? "" : result.toString());
        }

        Log.i(asTag(cls), builder.toString());
    }

    private static String asTag(Class<?> cls) {
        if (cls.isAnonymousClass()) {
            return asTag(cls.getEnclosingClass());
        }
        return cls.getSimpleName();
    }
}

