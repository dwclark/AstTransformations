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
import java.sql.*;

@GroovyASTTransformation(phase = CompilePhase.CANONICALIZATION)
public class DbClassTransformation implements ASTTransformation {

  void visit(ASTNode[] astNodes, SourceUnit sourceUnit) {
    if(!AstTransformUtils.legalClassAnnotation(astNodes, DbClass)) {
      return;
    }

    ClassNode classNode = astNodes[1];
    AnnotationNode annotation = astNodes[0];
    Map annotationInfo = AstTransformUtils.extractAnnotationInfo(annotation);
    String table = annotationInfo['table'];
    String connectionString = annotationInfo['connection'];
    Connection connection = getConnection(connectionString);
    Map fields = extractFields(connection, table);
    List propertyNodes = populateNodes(fields, classNode);
    propertyNodes.each { list ->
      classNode.addField(list[0]); classNode.addMethod(list[1]); classNode.addMethod(list[2]); };
    AstTransformUtils.fixupScopes(sourceUnit);
  }

  public Connection getConnection(String connectionString) {
    Class.forName("org.postgresql.Driver");
    return DriverManager.getConnection(connectionString);
  }

  public List populateNodes(Map fields, ClassNode classNode) {
    String code;
    List nodes = [];
    fields.each { fieldName, fieldType ->
      nodes.add( [ createField(fieldName, fieldType, classNode),
		   createGetter(fieldName, fieldType, classNode),
		   createSetter(fieldName, fieldType, classNode) ]);
    };

    return nodes;
  }

  public FieldNode createField(String fieldName, String fieldType, ClassNode parent) {
    return new FieldNode("_" + fieldName, ACC_PRIVATE, ClassHelper.make(fieldType), parent,
			 EmptyExpression.INSTANCE);
  }

  private String toUpper(String fieldName) {
    return fieldName.substring(0,1).toUpperCase() + fieldName.substring(1)
  }

  public MethodNode createGetter(String fieldName, String fieldType, ClassNode parent) {
    String name = "get" + toUpper(fieldName);
    return new MethodNode(name, ACC_PUBLIC, ClassHelper.make(fieldType), Parameter.EMPTY_ARRAY,
			  [] as ClassNode[], new ReturnStatement(new VariableExpression("_" + fieldName)));
  }

  public MethodNode createSetter(String fieldName, String fieldType, ClassNode parent) {
    String name = "set" + toUpper(fieldName);
    Parameter p = new Parameter(ClassHelper.make(fieldType), "val");
    p.modifiers = ACC_FINAL;
    BinaryExpression be = new BinaryExpression(new VariableExpression("_" + fieldName),
					       Token.newSymbol(Types.ASSIGN, -1, -1),
					       new VariableExpression("val"));
    return new MethodNode(name, ACC_PUBLIC, ClassHelper.VOID_TYPE, [ p ] as Parameter[],
			  [] as ClassNode[], new ExpressionStatement(be));
  }

  public Map extractFields(Connection con, String tableName) {
    def rset = con.metaData.getColumns(null, null, tableName, null);
    def fields = [:];
    while(rset.next()) {
      fields[rset.getString("COLUMN_NAME")] = sqlType(rset.getInt("DATA_TYPE"));
    }

    return fields;
  }

  public String sqlType(int type) {
    switch (type) {
    case java.sql.Types.BIGINT: return "Long";
    case java.sql.Types.BIT: return "Boolean";
    case java.sql.Types.BINARY: return "byte[]";
    case java.sql.Types.BLOB: return "byte[]";
    case java.sql.Types.BOOLEAN: return "Boolean";
    case java.sql.Types.CHAR: return "String";
    case java.sql.Types.CLOB: return "String";
    case java.sql.Types.DECIMAL: return "BigDecimal";
    case java.sql.Types.FLOAT: return "Float";
    case java.sql.Types.INTEGER: return "Integer";
    case java.sql.Types.LONGVARCHAR: return "String";
    case java.sql.Types.LONGVARBINARY: return "byte[]";
    case java.sql.Types.NUMERIC: return "BigDecimal";
    case java.sql.Types.REAL: return "Double";
    case java.sql.Types.SMALLINT: return "Short";
    case java.sql.Types.TIMESTAMP: return "Date";
    case java.sql.Types.TINYINT: return "Byte";
    case java.sql.Types.VARBINARY: return "byte[]";
    case java.sql.Types.VARCHAR: return "String";
    default: throw new Exception();
    }
  }

}