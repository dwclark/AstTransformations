import org.codehaus.groovy.transform.*;
import org.codehaus.groovy.ast.*
import org.codehaus.groovy.ast.expr.*
import org.codehaus.groovy.ast.stmt.*
import org.codehaus.groovy.classgen.VariableScopeVisitor;
import org.codehaus.groovy.control.SourceUnit;

public class AstTransformUtils {

  public static boolean legalMethodAnnotation(ASTNode[] astNodes, Class requiredAnnotation) {
    if(!astNodes) return false;
    if(!astNodes[0] || !astNodes[1]) return false;
    if(!(astNodes[0] instanceof AnnotationNode)) return false;
    if(astNodes[0].classNode?.name != requiredAnnotation.name) return false;
    if(!(astNodes[1] instanceof MethodNode)) return false;

    return true;
  }

  public static Parameter[] copyParameters(Parameter[] params) {
    params.collect { param ->
      new Parameter(param.type, param.name, param.initialExpression); } as Parameter[];
  }

  public static void fixupScopes(SourceUnit sourceUnit) {
    //This stuff is PFM!!
    VariableScopeVisitor scopeVisitor = new VariableScopeVisitor(sourceUnit);
    sourceUnit.AST.classes.each { scopeVisitor.visitClass(it); };
  }

  public static ClassNode makeGenericClassNode(Class base, List classNodes) {
    GenericsType[] generics = classNodes.collect { genericNode ->
      new GenericsType(ClassHelper.make(genericNode.name)); } as GenericsType[];
    ClassNode baseType = ClassHelper.make(base, false);
    baseType.genericsTypes = generics;
    return baseType;
  }
}