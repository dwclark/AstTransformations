import org.codehaus.groovy.transform.GroovyASTTransformationClass
import java.lang.annotation.*

@Retention(RetentionPolicy.SOURCE)
@Target([ElementType.METHOD])
@GroovyASTTransformationClass (classes=[MakeAsyncPairTransformation])
public @interface MakeAsyncPair {
  String value() default "";
}

