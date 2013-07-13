import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.transform.*;
import org.codehaus.groovy.ast.*
import org.codehaus.groovy.ast.expr.*
import org.codehaus.groovy.ast.stmt.*
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.ast.builder.AstBuilder;
import static org.objectweb.asm.Opcodes.*;

@GroovyASTTransformation(phase = CompilePhase.CANONICALIZATION)
public class WrapMethodTransformation implements ASTTransformation {

  public static final String PREFIX = "E5baEGDkQ0KSb4MAiuEE18f0o6g";

  void visit(ASTNode[] astNodes, SourceUnit sourceUnit) {
    // use guard clauses as a form of defensive programming.
    if(!astNodes) return;
    if(!astNodes[0] || !astNodes[1]) return;
    if(!(astNodes[0] instanceof AnnotationNode)) return;
    if(astNodes[0].classNode?.name != WrapMethod.class.name) return;
    if(!(astNodes[1] instanceof MethodNode)) return;

    MethodNode annotatedMethod = astNodes[1];
    ClassNode declaringClass = annotatedMethod.declaringClass;
    MethodNode newMethod = new MethodNode(PREFIX + annotatedMethod.name,
					  ACC_PUBLIC, annotatedMethod.returnType,
					  annotatedMethod.parameters,
					  annotatedMethod.exceptions,
					  annotatedMethod.code);
    declaringClass.addMethod(newMethod);
    
    MethodCallExpression loggingCall = new MethodCallExpression(
      new VariableExpression("this"),
      new ConstantExpression("println"),
      new ArgumentListExpression(new ConstantExpression("I'm inside " + annotatedMethod.name)));

    MethodCallExpression redirectedCall = new MethodCallExpression(
      new VariableExpression("this"),
      new ConstantExpression(PREFIX + annotatedMethod.name),
      new ArgumentListExpression(annotatedMethod.parameters));
    
    BlockStatement block = new BlockStatement();
    block.addStatements([new ExpressionStatement(loggingCall), new ReturnStatement(redirectedCall)]);
    annotatedMethod.code = block;
    //ClassNode declaringClass = annotatedMethod.declaringClass;
    //MethodNode mainMethod = makeMainMethod(annotatedMethod);
    //declaringClass.addMethod(mainMethod);
  }
}