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

@GroovyASTTransformation(phase = CompilePhase.CANONICALIZATION)
public class IntegrateTransformation implements ASTTransformation {
  
  private static final ClassNode TYPECHECKED_CLASSNODE = ClassHelper.make(TypeChecked.class);
  private static final ClassNode COMPILESTATIC_CLASSNODE = ClassHelper.make(CompileStatic.class);
  private static final List TYPECHECKED_ANNOTATIONS =  [ TYPECHECKED_CLASSNODE, COMPILESTATIC_CLASSNODE ];

  private static final String VSUM = "sum_pry1M5Tz90L1vHUehxHsxN7VefQ";
  private static final String VI = "i_pry1M5Tz90L1vHUehxHsxN7VefQ";
  private static final String VDELTA = "delta_pry1M5Tz90L1vHUehxHsxN7VefQ";

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
  
  /*
    I had hoped to use the Ast Builder for this method.  However, it proved to be impossible
    to use here.  Ultimately I think this comes down to the fact that ast builder appears
    to be clever, but not complete.  If you can make builder syntax work for your needs, it
    can really cut down on the boiler plate code.  I have not been able to.
   */
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

    BinaryExpression intervalSizeExpression = new BinaryExpression(new VariableExpression("upper"),
								   Token.newSymbol(Types.MINUS, -1, -1), new VariableExpression("lower"));
    BinaryExpression stepAssignExpression = new BinaryExpression(intervalSizeExpression,
								 Token.newSymbol(Types.DIVIDE, -1, -1), new VariableExpression("steps"));

    DeclarationExpression deltaDeclare = new DeclarationExpression(new VariableExpression(VDELTA, ClassHelper.double_TYPE),
							   Token.newSymbol(Types.ASSIGN, -1, -1), stepAssignExpression);
    integrateBlock.addStatement(new ExpressionStatement(deltaDeclare));

    DeclarationExpression side = new DeclarationExpression(new VariableExpression(variableName, ClassHelper.double_TYPE),
							   Token.newSymbol(Types.ASSIGN, -1, -1), new VariableExpression("lower"));
    integrateBlock.addStatement(new ExpressionStatement(side));

    BlockStatement innerBlock = new BlockStatement();
    for(int i = 0; i < block.statements.size() - 1; ++i) {
      innerBlock.addStatement(block.statements[i]);
    }
    
    BinaryExpression area = new BinaryExpression(new VariableExpression(VDELTA), Token.newSymbol(Types.MULTIPLY, -1, -1), retStmt.expression);
    BinaryExpression addToSum = new BinaryExpression(new VariableExpression(VSUM), Token.newSymbol(Types.PLUS_EQUAL, -1, -1), area);
    innerBlock.addStatement(new ExpressionStatement(addToSum));
    innerBlock.addStatement(new ExpressionStatement(new BinaryExpression(new VariableExpression(variableName),
									 Token.newSymbol(Types.PLUS_EQUAL, -1, -1),
									 new VariableExpression(VDELTA))));

    ForStatement fstmt = new ForStatement(
      ForStatement.FOR_LOOP_DUMMY,
      new ClosureListExpression(
	[
	  new DeclarationExpression(
	    new VariableExpression(VI, ClassHelper.int_TYPE),
	    new Token(Types.EQUALS, "=", -1, -1),
	    new ConstantExpression(0, true)),
	  new BinaryExpression(
	    new VariableExpression(VI),
	    new Token(Types.COMPARE_LESS_THAN, "<", -1, -1),
	    new VariableExpression("steps")),
	  new PrefixExpression(
	    new Token(Types.PLUS_PLUS, "++", -1, -1),
	    new VariableExpression(VI)) ]),
      innerBlock);
      
      integrateBlock.addStatement(fstmt);
      integrateBlock.addStatement(new ReturnStatement(new VariableExpression(VSUM)));

      MethodNode retNode = new MethodNode(integrateMethodName(methodNode.name), ACC_PUBLIC, ClassHelper.double_TYPE,
					  params, ClassNode.EMPTY_ARRAY, integrateBlock);
      retNode.addAnnotation(new AnnotationNode(COMPILESTATIC_CLASSNODE));
      return retNode;
  }

}
