package dev.joid.tool.docgen.source;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.AnnotationDeclaration;
import com.github.javaparser.ast.body.AnnotationMemberDeclaration;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.EnumConstantDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.EnclosedExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.LambdaExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.expr.SingleMemberAnnotationExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.nodeTypes.NodeWithAnnotations;
import com.github.javaparser.ast.nodeTypes.NodeWithModifiers;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.stmt.ThrowStmt;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.TypeParameter;

import dev.joid.tool.docgen.model.ApiMember;
import dev.joid.tool.docgen.model.ApiMessage;
import dev.joid.tool.docgen.model.ApiParameter;
import dev.joid.tool.docgen.model.ApiType;
import dev.joid.tool.docgen.model.MemberKind;
import dev.joid.tool.docgen.model.TypeKind;
import dev.joid.tool.docgen.model.TypeNames;

public final class SourceReader {

	private static final Set<String>  FACTORY_NAMES     = new HashSet<>(Arrays.asList("create", "of", "from", "inst"));
	private static final Set<String>  LOG_NAMES         = new HashSet<>(Arrays.asList("warn", "warnOnce", "devWarnOnce"));
	private static final List<String> CONSTRUCTOR_NAMES = Arrays.asList("AllArgsConstructor", "RequiredArgsConstructor", "NoArgsConstructor");

	private final File                 root;
	private final JavaParser           parser;
	private final List<ApiMessage>     messages;
	private final Map<String, ApiType> types;
	private final Map<String, String>  packages;

	private SourceReader(final File root) {
		this.root     = root;
		this.parser   = new JavaParser(new ParserConfiguration().setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_8));
		this.messages = new ArrayList<>();
		this.types    = new LinkedHashMap<>();
		this.packages = new HashMap<>();
	}

	public static SourceReader create(final File root) {
		return new SourceReader(root);
	}

	public List<ApiMessage> getMessages() {
		return this.messages;
	}

	public Map<String, ApiType> read(final Map<String, String> modules) throws IOException {
		final List<CompilationUnit> units = new ArrayList<>();
		final List<String[]> locations = new ArrayList<>();
		for (final Map.Entry<String, String> module : modules.entrySet()) {
			final Path sources = new File(this.root, module.getValue() + "/src/main/java").toPath();
			if (!Files.isDirectory(sources)) {
				continue;
			}

			try (Stream<Path> files = Files.walk(sources)) {
				for (final Path file : files.filter(path -> path.toString().endsWith(".java")).sorted().collect(Collectors.toList())) {
					final ParseResult<CompilationUnit> result = this.parser.parse(file);
					if (!result.isSuccessful() || !result.getResult().isPresent()) {
						throw new IllegalStateException("Unable to parse " + file + ": " + result.getProblems());
					}

					final CompilationUnit unit = result.getResult().get();
					final String relative = this.root.toPath().relativize(file).toString().replace('\\', '/');
					if (SourceReader.isDemo(unit.getPackageDeclaration().map(declaration -> declaration.getNameAsString()).orElse(""))) {
						continue;
					}

					units.add(unit);
					this.collectMessages(unit, relative);
					locations.add(new String[] {module.getKey(), relative});
					unit.getTypes().forEach(type -> this.index(unit.getPackageDeclaration().map(declaration -> declaration.getNameAsString()).orElse(""), "", type));
				}
			}
		}

		for (int i = 0; i < units.size(); i++) {
			final CompilationUnit unit = units.get(i);
			final String packageName = unit.getPackageDeclaration().map(declaration -> declaration.getNameAsString()).orElse("");
			final Map<String, String> imports = this.imports(unit, packageName);
			for (final TypeDeclaration<?> type : unit.getTypes()) {
				this.visit(locations.get(i)[0], locations.get(i)[1], packageName, null, type, imports);
			}
		}

		for (final ApiType type : this.types.values()) {
			for (final String supertype : type.getExtendsTypes()) {
				this.addSupertype(type, supertype);
			}

			for (final String supertype : type.getImplementsTypes()) {
				this.addSupertype(type, supertype);
			}
		}
		return this.types;
	}

	private void index(final String packageName, final String outer, final TypeDeclaration<?> type) {
		final String name = outer.isEmpty() ? type.getNameAsString() : outer + "." + type.getNameAsString();
		this.packages.put(packageName + "." + name, packageName);
		for (final BodyDeclaration<?> member : type.getMembers()) {
			if (member instanceof TypeDeclaration) {
				this.index(packageName, name, (TypeDeclaration<?>) member);
			}
		}
	}

	private Map<String, String> imports(final CompilationUnit unit, final String packageName) {
		final Map<String, String> imports = new HashMap<>();
		for (final String fqcn : this.packages.keySet()) {
			if (this.packages.get(fqcn).equals(packageName)) {
				final String name = fqcn.substring(packageName.length() + 1);
				imports.put(name.contains(".") ? name.substring(0, name.indexOf('.')) : name, packageName + "." + (name.contains(".") ? name.substring(0, name.indexOf('.')) : name));
			}
		}

		for (final ImportDeclaration declaration : unit.getImports()) {
			if (declaration.isStatic() || declaration.isAsterisk()) {
				continue;
			}

			final String imported = declaration.getNameAsString();
			imports.put(imported.substring(imported.lastIndexOf('.') + 1), this.canonical(imported));
		}
		return imports;
	}

	private String canonical(final String imported) {
		String candidate = imported;
		final List<String> nested = new ArrayList<>();
		while (candidate.contains(".")) {
			if (this.packages.containsKey(candidate)) {
				final StringBuilder builder = new StringBuilder(candidate);
				for (int i = nested.size() - 1; i >= 0; i--) {
					builder.append('.').append(nested.get(i));
				}
				return builder.toString();
			}

			nested.add(candidate.substring(candidate.lastIndexOf('.') + 1));
			candidate = candidate.substring(0, candidate.lastIndexOf('.'));
		}
		return imported;
	}

	private void addSupertype(final ApiType type, final String supertype) {
		final String resolved = type.resolve(supertype);
		if (resolved != null && this.types.containsKey(resolved)) {
			type.getSupertypes().add(resolved);
		}
	}

	private void visit(final String module, final String file, final String packageName, final ApiType outer, final TypeDeclaration<?> declaration, final Map<String, String> imports) {
		final boolean interfaceOuter = outer != null && (outer.getKind() == TypeKind.INTERFACE || outer.getKind() == TypeKind.ANNOTATION);
		final boolean visible = declaration.isPublic() || interfaceOuter && !declaration.isPrivate() || outer != null && declaration.isProtected() && outer.isExtensible();
		if (!visible) {
			return;
		}

		final String name = outer == null ? declaration.getNameAsString() : outer.getName() + "." + declaration.getNameAsString();
		final ApiType type = ApiType.create(packageName + "." + name, name, module, file, SourceReader.kindOf(declaration));
		type.getImports().putAll(imports);
		this.registerNested(type, declaration, packageName);
		type.setDeprecated(declaration.getAnnotationByName("Deprecated").isPresent());
		this.types.put(type.getFqcn(), type);

		if (declaration instanceof ClassOrInterfaceDeclaration) {
			final ClassOrInterfaceDeclaration classOrInterface = (ClassOrInterfaceDeclaration) declaration;
			type.setTypeParameters(SourceReader.typeParameters(classOrInterface.getTypeParameters()));
			type.setAbstractType(classOrInterface.isAbstract());
			type.setFinalType(classOrInterface.isFinal());
			classOrInterface.getExtendedTypes().forEach(extended -> type.getExtendsTypes().add(TypeNames.clean(extended.asString())));
			classOrInterface.getImplementedTypes().forEach(implemented -> type.getImplementsTypes().add(TypeNames.clean(implemented.asString())));
		} else if (declaration instanceof EnumDeclaration) {
			final EnumDeclaration enumeration = (EnumDeclaration) declaration;
			type.setFinalType(true);
			enumeration.getImplementedTypes().forEach(implemented -> type.getImplementsTypes().add(TypeNames.clean(implemented.asString())));
			for (final EnumConstantDeclaration constant : enumeration.getEntries()) {
				final ApiMember member = ApiMember.create(MemberKind.VALUE, constant.getNameAsString());
				member.setDeprecated(constant.getAnnotationByName("Deprecated").isPresent());
				this.add(type, member);
			}
		}

		this.readMembers(module, file, packageName, type, declaration, imports);
		this.readSourceDefaults(type, declaration);
	}

	private void registerNested(final ApiType type, final TypeDeclaration<?> declaration, final String packageName) {
		for (final BodyDeclaration<?> member : declaration.getMembers()) {
			if (member instanceof TypeDeclaration) {
				final String nested = ((TypeDeclaration<?>) member).getNameAsString();
				type.getImports().put(nested, packageName + "." + type.getName() + "." + nested);
			}
		}
	}

	private void readMembers(final String module, final String file, final String packageName, final ApiType type, final TypeDeclaration<?> declaration, final Map<String, String> imports) {
		final boolean interfaceType = type.getKind() == TypeKind.INTERFACE || type.getKind() == TypeKind.ANNOTATION;
		final Map<String, Set<Integer>> declared = new HashMap<>();
		for (final BodyDeclaration<?> member : declaration.getMembers()) {
			if (member instanceof MethodDeclaration) {
				final MethodDeclaration method = (MethodDeclaration) member;
				declared.computeIfAbsent(method.getNameAsString().toLowerCase(Locale.ROOT), key -> new HashSet<>()).add(method.getParameters().size());
			}
		}

		final List<FieldDeclaration> fields = new ArrayList<>();
		for (final BodyDeclaration<?> member : declaration.getMembers()) {
			if (member instanceof TypeDeclaration) {
				final Map<String, String> nested = new HashMap<>(imports);
				nested.putAll(type.getImports());
				this.visit(module, file, packageName, type, (TypeDeclaration<?>) member, nested);
			} else if (member instanceof AnnotationMemberDeclaration) {
				final AnnotationMemberDeclaration element = (AnnotationMemberDeclaration) member;
				final ApiMember api = ApiMember.create(MemberKind.ELEMENT, element.getNameAsString());
				api.setReturnType(TypeNames.clean(element.getType().asString()));
				api.setDefaultValue(element.getDefaultValue().map(Expression::toString).orElse(null));
				this.add(type, api);
			} else if (member instanceof FieldDeclaration) {
				final FieldDeclaration field = (FieldDeclaration) member;
				fields.add(field);
				if (!this.isVisible(field, interfaceType, type)) {
					continue;
				}

				final boolean staticField = field.isStatic() || interfaceType;
				for (final VariableDeclarator variable : field.getVariables()) {
					final ApiMember api = ApiMember.create(staticField && (field.isFinal() || interfaceType) ? MemberKind.CONSTANT : MemberKind.FIELD, variable.getNameAsString());
					api.setReturnType(TypeNames.clean(variable.getType().asString()));
					api.setStaticMember(staticField);
					api.setProtectedAccess(field.isProtected());
					api.setDeprecated(field.getAnnotationByName("Deprecated").isPresent());
					api.setDefaultValue(variable.getInitializer().map(SourceReader::shortExpression).orElse(null));
					this.add(type, api);
				}
			} else if (member instanceof ConstructorDeclaration) {
				final ConstructorDeclaration constructor = (ConstructorDeclaration) member;
				if (constructor.isPublic() || constructor.isProtected() && type.isExtensible()) {
					final ApiMember api = ApiMember.create(MemberKind.CONSTRUCTOR, type.getSimpleName());
					api.setProtectedAccess(constructor.isProtected());
					api.setDeprecated(constructor.getAnnotationByName("Deprecated").isPresent());
					this.parameters(api, constructor.getParameters());
					this.add(type, api);
				}
			} else if (member instanceof MethodDeclaration) {
				final MethodDeclaration method = (MethodDeclaration) member;
				if (this.isVisible(method, interfaceType, type)) {
					this.add(type, this.method(type, method, interfaceType));
				}
			}
		}

		if (type.getKind() == TypeKind.CLASS || type.getKind() == TypeKind.ENUM) {
			this.addLombok(type, declaration, fields, declared);
		}

		final boolean implicit = declaration.getMembers().stream().noneMatch(member -> member instanceof ConstructorDeclaration) && SourceReader.CONSTRUCTOR_NAMES.stream().noneMatch(annotation -> declaration.getAnnotationByName(annotation).isPresent());
		if (type.getKind() == TypeKind.CLASS && implicit && (!declaration.isProtected() || type.isExtensible())) {
			final ApiMember api = ApiMember.create(MemberKind.CONSTRUCTOR, type.getSimpleName());
			api.setProtectedAccess(declaration.isProtected());
			this.add(type, api);
		}
	}

	private boolean isVisible(final NodeWithModifiers<?> node, final boolean interfaceType, final ApiType type) {
		if (node.hasModifier(Modifier.Keyword.PUBLIC)) {
			return true;
		}

		if (node.hasModifier(Modifier.Keyword.PROTECTED)) {
			return type.isExtensible();
		}
		return interfaceType && !node.hasModifier(Modifier.Keyword.PRIVATE);
	}

	private ApiMember method(final ApiType type, final MethodDeclaration method, final boolean interfaceType) {
		final String returnType = TypeNames.clean(method.getType().asString());
		final String typeParameters = SourceReader.typeParameters(method.getTypeParameters());
		final boolean staticMethod = method.isStatic();
		final String erased = TypeNames.erase(returnType);
		final boolean factory = staticMethod && !"void".equals(returnType) && (SourceReader.FACTORY_NAMES.contains(method.getNameAsString()) && (erased.equals(type.getSimpleName()) || SourceReader.isTypeVariable(erased, method.getTypeParameters())) || erased.equals(type.getSimpleName()));
		final ApiMember api = ApiMember.create(factory ? MemberKind.FACTORY : MemberKind.METHOD, method.getNameAsString());
		api.setReturnType(returnType);
		api.setTypeParameters(typeParameters);
		api.setStaticMember(staticMethod);
		api.setProtectedAccess(method.isProtected());
		api.setDefaultMember(method.isDefault());
		api.setFinalMember(method.isFinal());
		api.setDeprecated(method.getAnnotationByName("Deprecated").isPresent());
		api.setOverriding(method.getAnnotationByName("Override").isPresent());
		api.setAbstractMember(method.isAbstract() || interfaceType && !method.isDefault() && !staticMethod);
		api.setFluent(!staticMethod && (SourceReader.isTypeVariable(erased, method.getTypeParameters()) && SourceReader.returnsThis(method) || erased.equals(type.getSimpleName()) && SourceReader.returnsThis(method)));

		final boolean overridable = !staticMethod && !method.isFinal() && type.isExtensible();
		final boolean empty = method.getBody().map(body -> body.getStatements().isEmpty()).orElse(false);
		api.setHook(overridable && (api.isAbstractMember() && !interfaceType || method.isProtected() || empty));
		this.parameters(api, method.getParameters());
		method.getBody().ifPresent(body -> {
			for (final ThrowStmt statement : body.findAll(ThrowStmt.class)) {
				if (statement.getExpression() instanceof ObjectCreationExpr) {
					final ObjectCreationExpr creation = (ObjectCreationExpr) statement.getExpression();
					api.getThrowsList().add(ApiMessage.create(false, creation.getType().getNameAsString(), creation.getArguments().isEmpty() ? "" : SourceReader.messageText(creation.getArgument(0)), type.getFqcn() + "#" + method.getNameAsString(), type.getFile(), statement.getBegin().map(position -> position.line).orElse(0)));
				}
			}

			if (body.toString().contains("runOnRenderThread")) {
				api.setThread("render thread");
			}
		});
		return api;
	}

	private void parameters(final ApiMember api, final NodeList<Parameter> parameters) {
		for (final Parameter parameter : parameters) {
			api.getParameters().add(ApiParameter.create(TypeNames.clean(parameter.getType().asString()), parameter.getNameAsString(), parameter.isVarArgs()));
		}
	}

	private void addLombok(final ApiType type, final TypeDeclaration<?> declaration, final List<FieldDeclaration> fields, final Map<String, Set<Integer>> declared) {
		final String classGetter = SourceReader.lombokAccess(declaration.getAnnotations(), "Getter");
		final String classSetter = SourceReader.lombokAccess(declaration.getAnnotations(), "Setter");
		for (final FieldDeclaration field : fields) {
			final String getter = Optional.ofNullable(SourceReader.lombokAccess(field.getAnnotations(), "Getter")).orElse(field.isStatic() ? null : classGetter);
			final String setter = Optional.ofNullable(SourceReader.lombokAccess(field.getAnnotations(), "Setter")).orElse(field.isStatic() ? null : classSetter);
			final boolean fluent = SourceReader.accessors(field, "fluent", false);
			final boolean chain = SourceReader.accessors(field, "chain", fluent);
			for (final VariableDeclarator variable : field.getVariables()) {
				final String fieldType = TypeNames.clean(variable.getType().asString());
				final String fieldName = variable.getNameAsString();
				final boolean bool = "boolean".equals(fieldType);
				final String property = SourceReader.capitalize(bool && fieldName.matches("is[A-Z].*") ? fieldName.substring(2) : fieldName);
				final String capitalized = SourceReader.capitalize(fieldName);
				if (SourceReader.isExposed(getter, type)) {
					final String getterName = fluent ? fieldName : (bool ? "is" : "get") + property;
					final List<String> getterNames = fluent ? Arrays.asList(fieldName) : bool ? Arrays.asList("is" + property, "get" + property, "is" + capitalized, "get" + capitalized) : Arrays.asList("get" + property);
					if (!SourceReader.isDeclared(declared, getterNames, 0)) {
						final ApiMember api = ApiMember.create(MemberKind.METHOD, getterName);
						api.setReturnType(fieldType);
						api.setStaticMember(field.isStatic());
						api.setProtectedAccess("protected".equals(getter));
						api.setFinalMember(true);
						api.setLombok("Getter");
						this.add(type, api);
					}
				}

				if (SourceReader.isExposed(setter, type) && !field.isFinal()) {
					final String setterName = fluent ? fieldName : "set" + property;
					if (!SourceReader.isDeclared(declared, fluent ? Arrays.asList(fieldName) : Arrays.asList("set" + property, "set" + capitalized), 1)) {
						final ApiMember api = ApiMember.create(MemberKind.METHOD, setterName);
						api.setReturnType(chain ? type.getSimpleName() : "void");
						api.setFluent(chain);
						api.setStaticMember(field.isStatic());
						api.setProtectedAccess("protected".equals(setter));
						api.setFinalMember(true);
						api.setLombok("Setter");
						api.getParameters().add(ApiParameter.create(fieldType, fieldName, false));
						this.add(type, api);
					}
				}
			}
		}

		if (declaration.getAnnotationByName("ToString").isPresent() && !SourceReader.isDeclared(declared, Arrays.asList("toString"), 0)) {
			this.add(type, SourceReader.objectMethod("ToString", "toString", "String", null));
		}

		if (declaration.getAnnotationByName("EqualsAndHashCode").isPresent()) {
			if (!SourceReader.isDeclared(declared, Arrays.asList("equals"), 1)) {
				this.add(type, SourceReader.objectMethod("EqualsAndHashCode", "equals", "boolean", "Object"));
			}

			if (!SourceReader.isDeclared(declared, Arrays.asList("hashCode"), 0)) {
				this.add(type, SourceReader.objectMethod("EqualsAndHashCode", "hashCode", "int", null));
			}
		}

		for (final String annotation : SourceReader.CONSTRUCTOR_NAMES) {
			final String access = SourceReader.lombokAccess(declaration.getAnnotations(), annotation);
			if (!SourceReader.isExposed(access, type) || type.getKind() == TypeKind.ENUM) {
				continue;
			}

			final ApiMember api = ApiMember.create(MemberKind.CONSTRUCTOR, type.getSimpleName());
			api.setProtectedAccess("protected".equals(access));
			api.setLombok(annotation);
			for (final FieldDeclaration field : fields) {
				for (final VariableDeclarator variable : field.getVariables()) {
					final boolean included = !field.isStatic() && !variable.getInitializer().isPresent() && ("AllArgsConstructor".equals(annotation) || "RequiredArgsConstructor".equals(annotation) && (field.isFinal() || field.getAnnotationByName("NonNull").isPresent()));
					if (included) {
						api.getParameters().add(ApiParameter.create(TypeNames.clean(variable.getType().asString()), variable.getNameAsString(), false));
					}
				}
			}
			this.add(type, api);
		}
	}

	private void readSourceDefaults(final ApiType type, final TypeDeclaration<?> declaration) {
		for (final BodyDeclaration<?> member : declaration.getMembers()) {
			if (member instanceof FieldDeclaration) {
				for (final VariableDeclarator variable : ((FieldDeclaration) member).getVariables()) {
					type.getFieldNames().add(variable.getNameAsString());
					variable.getInitializer().ifPresent(initializer -> type.getSourceDefaults().put(variable.getNameAsString(), SourceReader.unwrap(initializer, new HashSet<>())));
				}
			}
		}

		for (final BodyDeclaration<?> member : declaration.getMembers()) {
			if (!(member instanceof ConstructorDeclaration)) {
				continue;
			}

			final ConstructorDeclaration constructor = (ConstructorDeclaration) member;
			final Set<String> parameters = constructor.getParameters().stream().map(Parameter::getNameAsString).collect(Collectors.toSet());
			for (final ExpressionStmt statement : constructor.getBody().findAll(ExpressionStmt.class)) {
				if (statement.getParentNode().map(parent -> parent != constructor.getBody()).orElse(true)) {
					continue;
				}

				if (statement.getExpression() instanceof MethodCallExpr) {
					final MethodCallExpr call = (MethodCallExpr) statement.getExpression();
					final boolean own = call.getScope().map(scope -> scope.isSuperExpr() || scope.isThisExpr()).orElse(true);
					if (own && call.getArguments().size() == 1) {
						type.getSourceDefaults().put(call.getNameAsString() + "#call", SourceReader.unwrap(call.getArgument(0), parameters));
					}
					continue;
				}

				if (!(statement.getExpression() instanceof AssignExpr)) {
					continue;
				}

				final AssignExpr assign = (AssignExpr) statement.getExpression();
				final String target = assign.getTarget() instanceof FieldAccessExpr ? ((FieldAccessExpr) assign.getTarget()).getNameAsString() : assign.getTarget() instanceof NameExpr ? ((NameExpr) assign.getTarget()).getNameAsString() : null;
				if (target != null && assign.getOperator() == AssignExpr.Operator.ASSIGN && !type.getSourceDefaults().containsKey(target + "#constructor")) {
					type.getSourceDefaults().put(target, SourceReader.unwrap(assign.getValue(), parameters));
					type.getSourceDefaults().put(target + "#constructor", "");
				}
			}
		}
	}

	private void collectMessages(final CompilationUnit unit, final String file) {
		final String packageName = unit.getPackageDeclaration().map(declaration -> declaration.getNameAsString()).orElse("");

		for (final ThrowStmt statement : unit.findAll(ThrowStmt.class)) {
			if (statement.getExpression() instanceof ObjectCreationExpr) {
				final ObjectCreationExpr creation = (ObjectCreationExpr) statement.getExpression();
				if (!creation.getArguments().isEmpty()) {
					final String text = SourceReader.messageText(creation.getArgument(0));
					final int line = statement.getBegin().map(position -> position.line).orElse(0);
					SourceReader.origins(unit, statement).forEach(origin -> this.messages.add(ApiMessage.create(false, creation.getType().getNameAsString(), text, SourceReader.origin(origin, packageName), file, line)));
				}
			}
		}

		for (final ObjectCreationExpr creation : unit.findAll(ObjectCreationExpr.class)) {
			final String name = creation.getType().getNameAsString();
			if (creation.getArguments().isEmpty() || creation.getParentNode().filter(parent -> parent instanceof ThrowStmt).isPresent() || !name.endsWith("Exception") && !name.endsWith("Error")) {
				continue;
			}

			final Expression argument = creation.getArgument(0);
			final String text = argument instanceof NameExpr ? SourceReader.builderText(creation, ((NameExpr) argument).getNameAsString()) : SourceReader.messageText(argument);
			if (!text.matches(".*[A-Za-z]{3}.*")) {
				continue;
			}

			final int line = creation.getBegin().map(position -> position.line).orElse(0);
			SourceReader.origins(unit, creation).forEach(origin -> this.messages.add(ApiMessage.create(false, name, text, SourceReader.origin(origin, packageName), file, line)));
		}

		for (final MethodCallExpr call : unit.findAll(MethodCallExpr.class)) {
			final int index = "warn".equals(call.getNameAsString()) ? 0 : 1;
			if ("JOIDLog".equals(call.getScope().map(Expression::toString).orElse("")) && SourceReader.LOG_NAMES.contains(call.getNameAsString()) && call.getArguments().size() > index) {
				final Expression argument = SourceReader.unwrapSupplier(call.getArgument(index));
				final String text = "[JOID] " + (argument instanceof NameExpr ? SourceReader.builderText(call, ((NameExpr) argument).getNameAsString()) : SourceReader.messageText(argument));
				final int line = call.getBegin().map(position -> position.line).orElse(0);
				SourceReader.origins(unit, call).forEach(origin -> this.messages.add(ApiMessage.create(true, "warning", text.replace("……", "…"), SourceReader.origin(origin, packageName), file, line)));
				continue;
			}

			if ("println".equals(call.getNameAsString()) && !file.endsWith("/JOIDLog.java") && call.getScope().map(scope -> "System.err".equals(scope.toString())).orElse(false) && !call.getArguments().isEmpty()) {
				final Expression argument = call.getArgument(0);
				final String text = argument instanceof NameExpr ? SourceReader.builderText(call, ((NameExpr) argument).getNameAsString()) : SourceReader.messageText(argument);
				if (text.startsWith("[JOID]") || (!file.startsWith("tool/") || SourceReader.isFailure(call)) && text.matches(".*[A-Za-z]{3}.*")) {
					final int line = call.getBegin().map(position -> position.line).orElse(0);
					final boolean failure = SourceReader.isFailure(call);
					SourceReader.origins(unit, call).forEach(origin -> this.messages.add(ApiMessage.create(!failure, failure ? "failure" : "warning", text, SourceReader.origin(origin, packageName), file, line)));
				}
			}
		}

		for (final MethodCallExpr call : unit.findAll(MethodCallExpr.class)) {
			if (!"add".equals(call.getNameAsString()) || call.getArguments().size() != 1 || !call.getScope().filter(scope -> scope instanceof NameExpr).isPresent()) {
				continue;
			}

			final String list = call.getScope().get().toString();
			final String type = SourceReader.ancestor(call, MethodDeclaration.class).map(method -> SourceReader.failureType(method, list)).orElse(null);
			final String text = SourceReader.messageText(call.getArgument(0));
			if (type != null && text.matches(".*[A-Za-z]{3}.*")) {
				final int line = call.getBegin().map(position -> position.line).orElse(0);
				SourceReader.origins(unit, call).forEach(origin -> this.messages.add(ApiMessage.create("warning".equals(type), type, text, SourceReader.origin(origin, packageName), file, line)));
			}
		}

		unit.getTypes().stream().map(TypeDeclaration::getNameAsString).filter(name -> name.endsWith("ContractSuite")).findFirst().ifPresent(suite -> {
			final Set<String> details = new HashSet<>();
			for (final MethodCallExpr call : unit.findAll(MethodCallExpr.class)) {
				if (call.getArguments().isEmpty() || !call.getNameAsString().startsWith("assert") && !"fail".equals(call.getNameAsString()) || !SourceReader.isProse(call.getArgument(0))) {
					continue;
				}

				final String text = SourceReader.messageText(call.getArgument(0));
				if (text.matches(".*[A-Za-z]{3}.*") && details.add(text)) {
					this.messages.add(ApiMessage.create("AssertionError", "A test of " + suite + " fails", packageName + "." + suite, file, call.getBegin().map(position -> position.line).orElse(0), text));
				}
			}
		});

		final String nonNull = "… is marked non-null but is null";
		if (!file.startsWith("core/") || this.messages.stream().anyMatch(message -> nonNull.equals(message.getText())) || unit.getImports().stream().noneMatch(declaration -> "lombok.NonNull".equals(declaration.getNameAsString()))) {
			return;
		}

		unit.findAll(Parameter.class).stream().filter(parameter -> parameter.isAnnotationPresent("NonNull")).findFirst().ifPresent(parameter -> this.messages.add(ApiMessage.create(false, "NullPointerException", nonNull, "", file, parameter.getBegin().map(position -> position.line).orElse(0))));
	}

	private static List<Node> origins(final CompilationUnit unit, final Node node) {
		final List<Node> origins = new ArrayList<>();
		SourceReader.callers(unit, node, new HashSet<>(), origins);
		if (origins.isEmpty()) {
			origins.add(node);
		}
		return origins;
	}

	private static void callers(final CompilationUnit unit, final Node node, final Set<MethodDeclaration> seen, final List<Node> origins) {
		final Optional<MethodDeclaration> method = SourceReader.ancestor(node, MethodDeclaration.class);
		if (!method.isPresent() || !method.get().isPrivate()) {
			if (seen.isEmpty()) {
				return;
			}
			origins.add(node);
			return;
		}

		if (!seen.add(method.get())) {
			return;
		}

		for (final MethodCallExpr call : unit.findAll(MethodCallExpr.class)) {
			if (call.getNameAsString().equals(method.get().getNameAsString()) && call.getArguments().size() == method.get().getParameters().size()) {
				SourceReader.callers(unit, call, seen, origins);
			}
		}
	}

	private static <N extends Node> Optional<N> ancestor(final Node node, final Class<N> type) {
		Optional<Node> current = node.getParentNode();
		while (current.isPresent() && !type.isInstance(current.get())) {
			current = current.get().getParentNode();
		}
		return current.map(type::cast);
	}

	private static boolean isDemo(final String packageName) {
		return packageName.startsWith("dev.joid.demo") || packageName.contains(".demo.") || packageName.endsWith(".demo");
	}

	private static Expression unwrapSupplier(final Expression expression) {
		if (expression instanceof LambdaExpr && ((LambdaExpr) expression).getParameters().isEmpty() && ((LambdaExpr) expression).getExpressionBody().isPresent()) {
			return ((LambdaExpr) expression).getExpressionBody().get();
		}
		return expression;
	}

	private static boolean isFailure(final MethodCallExpr call) {
		final Optional<BlockStmt> block = call.getParentNode().flatMap(Node::getParentNode).filter(parent -> parent instanceof BlockStmt).map(parent -> (BlockStmt) parent);
		if (!block.isPresent() || block.get().getStatements().size() != 2) {
			return false;
		}

		final NodeList<Statement> statements = block.get().getStatements();
		return statements.get(0).isAncestorOf(call) && "System.exit(1);".equals(statements.get(1).toString());
	}

	private static boolean isProse(final Expression expression) {
		if (expression instanceof BinaryExpr && ((BinaryExpr) expression).getOperator() == BinaryExpr.Operator.PLUS) {
			return SourceReader.isProse(((BinaryExpr) expression).getLeft()) || SourceReader.isProse(((BinaryExpr) expression).getRight());
		}
		return expression instanceof StringLiteralExpr;
	}

	private static String failureType(final MethodDeclaration method, final String list) {
		for (final MethodCallExpr call : method.findAll(MethodCallExpr.class)) {
			final String scope = call.getScope().map(Expression::toString).orElse("");
			if ("forEach".equals(call.getNameAsString()) && list.equals(scope) && call.getArguments().size() == 1 && "System.err::println".equals(call.getArgument(0).toString())) {
				return SourceReader.ancestor(call, BlockStmt.class).filter(block -> block.getStatements().stream().anyMatch(statement -> "System.exit(1);".equals(statement.toString()))).isPresent() ? "failure" : "warning";
			}

			if ("Assert".equals(scope) && call.getArguments().stream().anyMatch(argument -> argument.findAll(MethodCallExpr.class).stream().anyMatch(join -> "join".equals(join.getNameAsString()) && join.getArguments().stream().anyMatch(joined -> list.equals(joined.toString()))))) {
				return "AssertionError";
			}
		}
		return null;
	}

	private static String builderText(final Node call, final String name) {
		final Optional<BlockStmt> block = SourceReader.ancestor(call, BlockStmt.class);
		if (!block.isPresent()) {
			return "…";
		}

		final StringBuilder text = new StringBuilder();
		boolean declared = false;
		for (final Statement statement : block.get().getStatements()) {
			if (statement.isAncestorOf(call)) {
				break;
			}

			for (final VariableDeclarator variable : statement.findAll(VariableDeclarator.class)) {
				if (variable.getNameAsString().equals(name) && variable.getInitializer().isPresent()) {
					text.setLength(0);
					text.append(SourceReader.messageText(variable.getInitializer().get()));
					declared = true;
				}
			}

			if (declared && statement instanceof ExpressionStmt) {
				final List<Expression> appended = new ArrayList<>();
				Expression current = ((ExpressionStmt) statement).getExpression();
				while (current instanceof MethodCallExpr && "append".equals(((MethodCallExpr) current).getNameAsString()) && ((MethodCallExpr) current).getArguments().size() == 1) {
					appended.add(0, ((MethodCallExpr) current).getArgument(0));
					current = ((MethodCallExpr) current).getScope().orElse(null);
				}

				if (current instanceof NameExpr && ((NameExpr) current).getNameAsString().equals(name)) {
					appended.forEach(argument -> text.append(SourceReader.messageText(argument)));
				}
			}
		}
		return declared ? text.toString().replace("……", "…") : "…";
	}

	private void add(final ApiType type, final ApiMember member) {
		member.setDeclaringType(type.getFqcn());
		type.getMembers().add(member);
	}

	public static String unwrap(final Expression expression, final Set<String> parameters) {
		Expression current = expression;
		while (true) {
			if (current instanceof LambdaExpr && ((LambdaExpr) current).getParameters().isEmpty() && ((LambdaExpr) current).getExpressionBody().isPresent()) {
				current = ((LambdaExpr) current).getExpressionBody().get();
			} else if (current instanceof MethodCallExpr && ((MethodCallExpr) current).getArguments().size() == 1 && ((MethodCallExpr) current).getScope().map(scope -> "Signal".equals(scope.toString())).orElse(false)) {
				current = ((MethodCallExpr) current).getArgument(0);
			} else {
				break;
			}
		}

		final boolean parameter = current.findAll(NameExpr.class).stream().anyMatch(name -> parameters.contains(name.getNameAsString()));
		return parameter ? "@parameter" : SourceReader.shortExpression(current);
	}

	public static String messageText(final Expression expression) {
		if (expression instanceof StringLiteralExpr) {
			return ((StringLiteralExpr) expression).asString().replace('\n', ' ');
		}

		if (expression instanceof BinaryExpr && ((BinaryExpr) expression).getOperator() == BinaryExpr.Operator.PLUS) {
			final String left = SourceReader.messageText(((BinaryExpr) expression).getLeft());
			final String right = SourceReader.messageText(((BinaryExpr) expression).getRight());
			return (left + right).replace("……", "…");
		}

		if (expression instanceof EnclosedExpr) {
			return SourceReader.messageText(((EnclosedExpr) expression).getInner());
		}

		if (expression instanceof ObjectCreationExpr && "StringBuilder".equals(((ObjectCreationExpr) expression).getType().getNameAsString())) {
			final ObjectCreationExpr creation = (ObjectCreationExpr) expression;
			return creation.getArguments().isEmpty() ? "" : SourceReader.messageText(creation.getArgument(0));
		}

		if (expression instanceof MethodCallExpr && "append".equals(((MethodCallExpr) expression).getNameAsString()) && ((MethodCallExpr) expression).getArguments().size() == 1) {
			final MethodCallExpr append = (MethodCallExpr) expression;
			final String left = append.getScope().map(SourceReader::messageText).orElse("…");
			return (left + SourceReader.messageText(append.getArgument(0))).replace("……", "…");
		}

		if (expression instanceof MethodCallExpr && "format".equals(((MethodCallExpr) expression).getNameAsString()) && !((MethodCallExpr) expression).getArguments().isEmpty()) {
			return SourceReader.messageText(((MethodCallExpr) expression).getArgument(0)).replaceAll("%[-#+ 0,(]*\\d*(\\.\\d+)?[a-zA-Z%]", "…");
		}
		return "…";
	}

	private static String origin(final Node node, final String packageName) {
		final StringBuilder name = new StringBuilder();
		Optional<Node> current = node.getParentNode();
		String member = "";
		while (current.isPresent()) {
			final Node parent = current.get();
			if (parent instanceof MethodDeclaration && member.isEmpty()) {
				member = ((MethodDeclaration) parent).getNameAsString();
			} else if (parent instanceof ConstructorDeclaration && member.isEmpty()) {
				member = ((ConstructorDeclaration) parent).getNameAsString();
			} else if (parent instanceof TypeDeclaration) {
				name.insert(0, name.length() == 0 ? ((TypeDeclaration<?>) parent).getNameAsString() : ((TypeDeclaration<?>) parent).getNameAsString() + ".");
			}
			current = parent.getParentNode();
		}
		return packageName + "." + name + (member.isEmpty() ? "" : "#" + member);
	}

	private static String shortExpression(final Expression expression) {
		final String printed = expression.toString().replaceAll("\\s+", " ");
		return printed.length() > 80 ? null : printed;
	}

	private static boolean returnsThis(final MethodDeclaration method) {
		if (!method.getBody().isPresent()) {
			return true;
		}

		for (final ReturnStmt statement : method.getBody().get().findAll(ReturnStmt.class)) {
			final String returned = statement.getExpression().map(Expression::toString).orElse("");
			if (returned.matches("(?s)\\(\\w+\\) this|this|(this|super)\\.\\w+\\(.*\\)")) {
				return true;
			}
		}
		return false;
	}

	private static boolean isTypeVariable(final String type, final NodeList<TypeParameter> parameters) {
		return parameters.stream().anyMatch(parameter -> parameter.getNameAsString().equals(type));
	}

	private static String typeParameters(final NodeList<TypeParameter> parameters) {
		if (parameters.isEmpty()) {
			return "";
		}

		final List<String> printed = new ArrayList<>();
		for (final TypeParameter parameter : parameters) {
			final NodeList<ClassOrInterfaceType> bounds = parameter.getTypeBound();
			printed.add(parameter.getNameAsString() + (bounds.isEmpty() ? "" : " extends " + bounds.stream().map(bound -> TypeNames.clean(bound.asString())).collect(Collectors.joining(" & "))));
		}
		return "<" + String.join(", ", printed) + ">";
	}

	private static TypeKind kindOf(final TypeDeclaration<?> declaration) {
		if (declaration instanceof EnumDeclaration) {
			return TypeKind.ENUM;
		}

		if (declaration instanceof AnnotationDeclaration) {
			return TypeKind.ANNOTATION;
		}
		return ((ClassOrInterfaceDeclaration) declaration).isInterface() ? TypeKind.INTERFACE : TypeKind.CLASS;
	}

	private static String lombokAccess(final NodeList<AnnotationExpr> annotations, final String name) {
		for (final AnnotationExpr annotation : annotations) {
			if (!annotation.getNameAsString().endsWith(name)) {
				continue;
			}

			String value = "";
			if (annotation instanceof SingleMemberAnnotationExpr) {
				value = ((SingleMemberAnnotationExpr) annotation).getMemberValue().toString();
			} else if (annotation instanceof NormalAnnotationExpr) {
				value = ((NormalAnnotationExpr) annotation).getPairs().stream().filter(pair -> "value".equals(pair.getNameAsString()) || "access".equals(pair.getNameAsString())).map(pair -> pair.getValue().toString()).collect(Collectors.joining());
			}

			if (value.contains("PRIVATE") || value.contains("NONE") || value.contains("PACKAGE") || value.contains("MODULE")) {
				return "private";
			}
			return value.contains("PROTECTED") ? "protected" : "public";
		}
		return null;
	}

	private static boolean accessors(final FieldDeclaration field, final String name, final boolean fallback) {
		Node current = field;
		while (current != null) {
			if (current instanceof NodeWithAnnotations) {
				final Optional<AnnotationExpr> annotation = ((NodeWithAnnotations<?>) current).getAnnotationByName("Accessors");
				if (annotation.isPresent() && annotation.get() instanceof NormalAnnotationExpr) {
					final Optional<String> value = ((NormalAnnotationExpr) annotation.get()).getPairs().stream().filter(pair -> name.equals(pair.getNameAsString())).map(pair -> pair.getValue().toString()).findFirst();
					if (value.isPresent()) {
						return "true".equals(value.get());
					}
				}
			}
			current = current.getParentNode().orElse(null);
		}
		return fallback;
	}

	private static boolean isDeclared(final Map<String, Set<Integer>> declared, final List<String> names, final int parameters) {
		return names.stream().anyMatch(name -> declared.getOrDefault(name.toLowerCase(Locale.ROOT), new HashSet<>()).contains(parameters));
	}

	private static ApiMember objectMethod(final String annotation, final String name, final String returnType, final String parameter) {
		final ApiMember api = ApiMember.create(MemberKind.METHOD, name);
		api.setReturnType(returnType);
		api.setOverriding(true);
		api.setLombok(annotation);
		if (parameter != null) {
			api.getParameters().add(ApiParameter.create(parameter, "other", false));
		}
		return api;
	}

	private static boolean isExposed(final String access, final ApiType type) {
		return "public".equals(access) || "protected".equals(access) && type.isExtensible();
	}

	private static String capitalize(final String name) {
		return Character.toUpperCase(name.charAt(0)) + name.substring(1);
	}

}