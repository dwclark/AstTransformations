import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.transform.*;
import org.codehaus.groovy.ast.*
import org.codehaus.groovy.ast.expr.*
import org.codehaus.groovy.ast.stmt.*
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.ast.builder.AstBuilder;
import static org.objectweb.asm.Opcodes.*;
import org.codehaus.groovy.syntax.*;
import groovy.transform.*;

@GroovyASTTransformation(phase = CompilePhase.SEMANTIC_ANALYSIS)
public class IntegrateTransformation implements ASTTransformation {

  private static final ClassNode TYPECHECKED_CLASSNODE = ClassHelper.make(TypeChecked.class);
  private static final ClassNode COMPILESTATIC_CLASSNODE = ClassHelper.make(CompileStatic.class);
  private static final List TYPECHECKED_ANNOTATIONS =  [ TYPECHECKED_CLASSNODE, COMPILESTATIC_CLASSNODE ];

  private static final String VSUM = "pry1M5Tz90L1vHUehxHsxN7VefQ_sum";
  private static final String VNEXT = "pry1M5Tz90L1vHUehxHsxN7VefQ_next";
  private static final String VI = "pry1M5Tz90L1vHUehxHsxN7VefQ_i";

  private static String integrateMethodName(String originalMethodName) {
    return "integrate" +  originalMethodName.substring(0,1).toUpperCase() +
      originalMethodName.substring(1);
  }

  void visit(ASTNode[] astNodes, SourceUnit sourceUnit) {
    if(!AstTransformUtils.legalMethodAnnotation(astNodes, Integrate)) {
      return;
    }

    MethodNode methodNode = astNodes[1];
    methodNode.declaringClass.addMethod(integrationNode(methodNode));
    AstTransformUtils.fixupScopes(sourceUnit);
  }

  public MethodNode integrationNode(MethodNode methodNode) {
    Parameter parameter = methodNode.parameters[0];
    String variableName = parameter.name;
    BlockStatement block = (BlockStatement) methodNode.code;
    ReturnStatement retStmt = block.statements[block.statements.size() - 1];

    Parameter[] params = [ new Parameter(methodNode.returnType, "lower"),
			    new Parameter(methodNode.returnType, "upper"),
			   new Parameter(ClassHelper.int_TYPE, "steps") ] as Parameter[];
    params.each { param -> param.modifiers = ACC_FINAL; };

    BlockStatement integrateBlock = new BlockStatement();
    
    DeclarationExpression sum = new DeclarationExpression(new VariableExpression(VSUM, ClassHelper.double_TYPE),
							  Token.newSymbol(Types.ASSIGN, -1, -1), new ConstantExpression(0.0d, true));
    integrateBlock.addStatement(new ExpressionStatement(sum));
    
    DeclarationExpression next = new DeclarationExpression(new VariableExpression(VNEXT, ClassHelper.double_TYPE),
							   Token.newSymbol(Types.ASSIGN, -1, -1), new ConstantExpression(0.0d, true));
    integrateBlock.addStatement(new ExpressionStatement(next));

    BinaryExpression accum = new BinaryExpression(new VariableExpression(VSUM),
						  Token.newSymbol(Types.PLUS_EQUAL, -1, -1), retStmt.expression);

    ForStatement fstmt = new ForStatement(
      ForStatement.FOR_LOOP_DUMMY,
      new ClosureListExpression(
	[
	  new DeclarationExpression(
	    new VariableExpression("x", ClassHelper.int_TYPE),
	    new Token(Types.EQUALS, "=", -1, -1),
	    new ConstantExpression(0, true)),
	  new BinaryExpression(
	    new VariableExpression("x"),
	    new Token(Types.COMPARE_LESS_THAN, "<", -1, -1),
	    new ConstantExpression(10, true)),
	  new PrefixExpression(
	    new Token(Types.PLUS_PLUS, "++", -1, -1),
	    new VariableExpression("x")) ]),
      
      new BlockStatement(
	[
	  new ExpressionStatement(
	    new MethodCallExpression(
	      new VariableExpression("this"),
	      new ConstantExpression("println"),
	      new ArgumentListExpression(new VariableExpression("x")))) ],
	new VariableScope()));

      integrateBlock.addStatement(fstmt);
      integrateBlock.addStatement(new ReturnStatement(new ConstantExpression(0.0d)));

      MethodNode retNode = new MethodNode(integrateMethodName(methodNode.name), ACC_PUBLIC, ClassHelper.double_TYPE,
					  params, ClassNode.EMPTY_ARRAY, integrateBlock);
      retNode.addAnnotation(new AnnotationNode(COMPILESTATIC_CLASSNODE));
      return retNode;
  }

}
