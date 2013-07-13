import org.codehaus.groovy.transform.*;
import org.codehaus.groovy.ast.*
import org.codehaus.groovy.ast.expr.*
import org.codehaus.groovy.ast.stmt.*

public class AstTransformUtils {

  public static boolean legalMethodAnnotation(ASTNode[] astNodes, Class requiredAnnotation) {
    if(!astNodes) return false;
    if(!astNodes[0] || !astNodes[1]) return false;
    if(!(astNodes[0] instanceof AnnotationNode)) return false;
    if(astNodes[0].classNode?.name != requiredAnnotation.name) return false;
    if(!(astNodes[1] instanceof MethodNode)) return false;

    return true;
  }
}