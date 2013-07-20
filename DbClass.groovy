import org.codehaus.groovy.transform.GroovyASTTransformationClass
import java.lang.annotation.*

@Retention(RetentionPolicy.SOURCE)
@Target([ElementType.TYPE])
@GroovyASTTransformationClass (classes=[DbClassTransformation])
public @interface DbClass {
  String connection() default "";
  String table() default "";
}
