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
import org.codehaus.groovy.syntax.*;

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
    }
    else {
      println("other return type, I can handle that");
      theClass.addMethod(callableAsyncMethod(theMethod));
    }

    AstTransformUtils.fixupScopes(sourceUnit);
  }

  public MethodNode runnableAsyncMethod(MethodNode methodNode) {
    ClassNode runnableType = ClassHelper.make(Runnable);
    ClassNode threadType = ClassHelper.make(Thread);
    CastExpression cast = wrapAndCastMethodCall(methodNode, runnableType);
    DeclarationExpression initialization = makeInitialStatement(cast, threadType);
    MethodCallExpression methodCall = new MethodCallExpression(initialization.variableExpression,
							       "start", MethodCallExpression.NO_ARGUMENTS);
    BlockStatement block = new BlockStatement();
    block.addStatement(new ExpressionStatement(initialization));
    block.addStatement(new ExpressionStatement(methodCall));
    block.addStatement(new ReturnStatement(initialization.variableExpression));

    Parameter[] newMethodParameters = AstTransformUtils.copyParameters(methodNode.parameters);
    newMethodParameters.each { it.modifiers = ACC_FINAL; };
    return new MethodNode(asyncMethodName(methodNode.name), ACC_PUBLIC, ClassHelper.make(Thread),
			  newMethodParameters, ClassNode.EMPTY_ARRAY, block);
  }

  public MethodNode callableAsyncMethod(MethodNode methodNode) {
    ClassNode genericNode = ClassHelper.make(methodNode.returnType.name);
    GenericsType[] generics = [ new GenericsType(genericNode) ] as GenericsType[];
    ClassNode callableType = ClassHelper.make(Callable, false);
    callableType.genericsTypes = generics;
    CastExpression cast = wrapAndCastMethodCall(methodNode, callableType);
    DeclarationExpression initialization = makeCastStatement(cast, callableType);

    BlockStatement block = new BlockStatement();
    block.addStatement(new ExpressionStatement(initialization));
    block.addStatement(ReturnStatement.RETURN_NULL_OR_VOID);
    
    Parameter[] newMethodParameters = AstTransformUtils.copyParameters(methodNode.parameters);
    newMethodParameters.each { it.modifiers = ACC_FINAL; };
    ClassNode futureType = ClassHelper.make(Future, false);
    futureType.genericsTypes = generics;
    return new MethodNode(asyncMethodName(methodNode.name), ACC_PUBLIC, futureType,
			  newMethodParameters, ClassNode.EMPTY_ARRAY, block);
  }

  public CastExpression wrapAndCastMethodCall(MethodNode methodNode, ClassNode classNode) {
    Parameter[] callingParameters = AstTransformUtils.copyParameters(methodNode.parameters);
    MethodCallExpression stage1Inner = new MethodCallExpression(
      new VariableExpression("this"), new ConstantExpression(methodNode.name),
      new ArgumentListExpression(callingParameters));
    ReturnStatement stage1Outer = new ReturnStatement(stage1Inner);
    ClosureExpression closure = new ClosureExpression(Parameter.EMPTY_ARRAY, stage1Outer);
    return new CastExpression(classNode, closure);
  }

  public DeclarationExpression makeInitialStatement(CastExpression cast, ClassNode classNode) {
    ConstructorCallExpression cce = new ConstructorCallExpression(classNode, cast);
    VariableExpression variable = new VariableExpression("toCall", classNode); 
    return new DeclarationExpression(variable, Token.newSymbol(Types.ASSIGN, -1, -1), cce);
  }

  public DeclarationExpression makeCastStatement(CastExpression cast, ClassNode classNode) {
    VariableExpression variable = new VariableExpression("toCall", classNode); 
    return new DeclarationExpression(variable, Token.newSymbol(Types.ASSIGN, -1, -1), cast);
  }
}