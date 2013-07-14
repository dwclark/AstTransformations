import org.codehaus.groovy.transform.GroovyASTTransformationClass
import java.lang.annotation.*

//TODO: Add optional value for executor
@Retention(RetentionPolicy.SOURCE)
@Target([ElementType.METHOD])
@GroovyASTTransformationClass (classes=[MakeAsyncPairTransformation])
public @interface MakeAsyncPair { }
