import org.codehaus.groovy.transform.GroovyASTTransformationClass
import java.lang.annotation.*

@Retention(RetentionPolicy.SOURCE)
@Target([ElementType.TYPE])
@GroovyASTTransformationClass (classes=[NoPrintlnTransformation])
public @interface NoPrintln { }
