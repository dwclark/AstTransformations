import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.transform.*;
import org.codehaus.groovy.ast.*
import org.codehaus.groovy.ast.builder.*;
import org.codehaus.groovy.ast.expr.*
import org.codehaus.groovy.ast.stmt.*
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.ast.builder.AstBuilder;
import static org.objectweb.asm.Opcodes.*;
import java.util.concurrent.*;

@GroovyASTTransformation(phase = CompilePhase.CANONICALIZATION)
public class MakeAsyncPairTransformation implements ASTTransformation {

  private static String asyncMethodName(String origMethodName) {
    return "async" + origMethodName.substring(0,1).toUpperCase() +
      origMethodName.substring(1);
  }

  void visit(ASTNode[] astNodes, SourceUnit sourceUnit) {
    if(!AstTransformUtils.legalMethodAnnotation(astNodes, MakeAsyncPair)) {
      return;
    }

    MethodNode theMethod = astNodes[1];
    ClassNode theClass = theMethod.declaringClass;
    if(theMethod.returnType == ClassHelper.VOID_TYPE) {
      println("void return type, I can handle that");
      theClass.addMethod(runnableAsyncMethod(theMethod));
      AstTransformUtils.fixupScopes(sourceUnit);
    }
    else {
      println("Sorry, can't yet handle that return type");
    }    
  }

  public MethodNode runnableAsyncMethod(MethodNode methodNode) {
    /* What we want to express is something like the following:
       public void asyncMethodName(parameters) { <-- stage #3
         Thread.start {  <-- stage #2
	   methodName(parameters);   <-- stage #1
	 }
       }
    */
    
    Parameter[] callingParameters = AstTransformUtils.copyParameters(methodNode.parameters);
    MethodCallExpression stage1Inner = new MethodCallExpression(
      new VariableExpression("this"), new ConstantExpression(methodNode.name),
      new ArgumentListExpression(callingParameters));
    ReturnStatement stage1Outer = new ReturnStatement(stage1Inner);

    ClosureExpression stage2Inner = new ClosureExpression(Parameter.EMPTY_ARRAY, stage1Outer);
    StaticMethodCallExpression stage2Outer = new StaticMethodCallExpression(
      ClassHelper.make(Thread.class), "start", new ArgumentListExpression(stage2Inner));

    Parameter[] newMethodParameters = AstTransformUtils.copyParameters(methodNode.parameters);
    newMethodParameters.each { it.modifiers = ACC_FINAL; };
    return new MethodNode(asyncMethodName(methodNode.name), ACC_PUBLIC, ClassHelper.make(Thread),
			  newMethodParameters, ClassNode.EMPTY_ARRAY, new ReturnStatement(stage2Outer));
  }
}