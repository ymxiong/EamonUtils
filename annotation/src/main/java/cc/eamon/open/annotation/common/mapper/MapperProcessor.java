package cc.eamon.open.annotation.common.mapper;

import cc.eamon.open.annotation.ProcessingException;
import cc.eamon.open.annotation.common.group.Group;
import cc.eamon.open.annotation.common.group.GroupMapper;
import com.squareup.javapoet.*;
import com.sun.tools.javac.code.Type;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementScanner6;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.util.*;

/**
 * Created by Eamon on 2018/9/30.
 */
public class MapperProcessor extends AbstractProcessor {

    private Types typeUtils;
    private Elements elementUtils;
    private Filer filer;
    private Messager messager;

    public MapperProcessor() {
    }


    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        typeUtils = processingEnv.getTypeUtils();
        elementUtils = processingEnv.getElementUtils();
        filer = processingEnv.getFiler();
        messager = processingEnv.getMessager();
    }


    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> annotations = new LinkedHashSet<>();
        annotations.add(Mapper.class.getCanonicalName());
        return annotations;
    }


    private class NameCheckScanner extends ElementScanner6<Void, Void> {

        @Override
        public Void scan(Element e, Void aVoid) {
            return super.scan(e, aVoid);
        }

    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    /**
     * 扫描类上注解
     *
     * @param elem            类信息
     * @param elemFields      属性信息
     * @param ignoreDetailMap ignore信息
     * @param modifyDetailMap modify信息
     * @param renameDetailMap rename信息
     */
    private void scanFields(
            Element elem,
            List<Element> elemFields,
            Map<String, MapperIgnoreDetail> ignoreDetailMap,
            Map<String, MapperModifyDetail> modifyDetailMap,
            Map<String, MapperRenameDetail> renameDetailMap) {
        Map<String, TypeMirror> methodType = new HashMap<>();

        for (Element elemMethod : elem.getEnclosedElements()) {
            if (elemMethod.getKind() == ElementKind.METHOD) {
                methodType.put(elemMethod.getSimpleName().toString(), elemMethod.asType());
            }
        }

        // 扫描ignore modify和rename项
        for (Element elemField : elem.getEnclosedElements()) {
            if (elemField.getKind().isField()) {
                // 排除static属性
                if (elemField.getModifiers().contains(Modifier.STATIC)) continue;

                elemFields.add(elemField);
                MapperIgnore ignore = elemField.getAnnotation(MapperIgnore.class);
                MapperModify modify = elemField.getAnnotation(MapperModify.class);
                MapperRename rename = elemField.getAnnotation(MapperRename.class);

                // 记录ignore
                if (ignore != null) {
                    MapperIgnoreDetail detail = new MapperIgnoreDetail();
                    detail.setFieldName(elemField.getSimpleName().toString());
                    for (String s : ignore.value()) {
                        detail.addValue(s);
                    }
                    ignoreDetailMap.put(detail.getFieldName(), detail);
                }
                // 记录modify及方法
                if (modify != null) {
                    MapperModifyDetail detail = new MapperModifyDetail();
                    detail.setFieldName(elemField.getSimpleName().toString());

                    // 生成detail信息
                    for (int i = 0; i < modify.value().length; i++) {
                        MapperModifyDetail.ModifyDetail modifyDetail = new MapperModifyDetail.ModifyDetail();
                        modifyDetail.setModifyName(modify.modify()[i]);
                        modifyDetail.setRecoverName(modify.recover()[i]);
                        modifyDetail.setTargetMapName(modify.value()[i]);
                        modifyDetail.setRecoverType(methodType.get(modify.modify()[i]));
                        detail.addValue(modifyDetail);
                    }
                    modifyDetailMap.put(detail.getFieldName(), detail);
                }
                // 记录rename
                if (rename != null) {
                    MapperRenameDetail detail = new MapperRenameDetail();
                    detail.setFieldName(elemField.getSimpleName().toString());

                    // 生成detail信息
                    for (int i = 0; i < rename.value().length; i++) {
                        MapperRenameDetail.RenameDetail renameDetail = new MapperRenameDetail.RenameDetail();
                        renameDetail.setTargetMapName(rename.value()[i]);
                        renameDetail.setRenameName(rename.name()[i]);
                        detail.addValue(renameDetail);
                    }
                    renameDetailMap.put(detail.getFieldName(), detail);
                }

            }
        }
    }

    /**
     * 生成GetMap方法
     *
     * @param self            原始类包信息
     * @param mapperName      方法名
     * @param elemFields      添加域信息
     * @param ignoreDetailMap ignore信息
     * @param modifyDetailMap modify信息
     * @param renameDetailMap rename信息
     * @return
     */
    private MethodSpec buildGetMapMethod(
            ClassName self,
            String mapperName,
            List<Element> elemFields,
            Map<String, MapperIgnoreDetail> ignoreDetailMap,
            Map<String, MapperModifyDetail> modifyDetailMap,
            Map<String, MapperRenameDetail> renameDetailMap
    ) {
        // 确定需要import的项
        ClassName string = ClassName.get("java.lang", "String");
        ClassName object = ClassName.get("java.lang", "Object");
        ClassName map = ClassName.get("java.util", "Map");
        ClassName linkedHashMap = ClassName.get("java.util", "LinkedHashMap");
        TypeName typeOfMap = ParameterizedTypeName.get(map, string, object);

        String realMethodName = "getMap";
        MethodSpec.Builder methodSpec = MethodSpec.methodBuilder(realMethodName)
                .addModifiers(Modifier.PUBLIC)
                .addModifiers(Modifier.STATIC)
                .addParameter(self, "obj")
                .returns(typeOfMap);

        // 创建resultMap
        methodSpec.addStatement("Map<String, Object> resultMap = new $T<>()", linkedHashMap);
        methodSpec.addStatement("if (obj == null) return resultMap");
        for (Element fieldElem : elemFields) {
            String field = fieldElem.getSimpleName().toString();
            if (ignoreDetailMap.get(field) != null) {
                // 如果加了ignore注解针对某方法 则直接跳过
                if (ignoreDetailMap.get(field).checkIn(mapperName)) continue;
            }
            // 有modify信息无rename信息
            if ((modifyDetailMap.get(field) != null) && (renameDetailMap.get(field) == null)) {
                // 检查该属性的modify信息是否与map绑定
                if (modifyDetailMap.get(field).getValue(mapperName) != null) {
                    MapperModifyDetail.ModifyDetail detail = modifyDetailMap.get(field).getValue(mapperName);
//                                methodSpec.addStatement("resultMap.put(\"" + field + "\", " + simpleName.toString() + "." + detail.getMethodName() + "(obj.get" + field.substring(0, 1).toUpperCase() + field.substring(1) + "()))");
                    methodSpec.addStatement("resultMap.put(\"" + field + "\", " + "obj." + detail.getModifyName() + "())");
                    continue;
                }
            }
            // 有rename信息无modify信息
            else if ((renameDetailMap.get(field) != null) && (modifyDetailMap.get(field) == null)) {
                // 检查该属性的rename信息是否与map绑定
                if (renameDetailMap.get(field).getValue(mapperName) != null) {
                    MapperRenameDetail.RenameDetail detail = renameDetailMap.get(field).getValue(mapperName);
                    methodSpec.addStatement("resultMap.put(\"" + detail.getRenameName() + "\", " + "obj.get" + field.substring(0, 1).toUpperCase() + field.substring(1) + "())");
                    continue;
                }
            }
            // 有rename信息和modify信息
            else if ((modifyDetailMap.get(field) != null) && (renameDetailMap.get(field) != null)) {
                // 检查该属性的modify和rename信息是否都与map绑定
                if ((modifyDetailMap.get(field).getValue(mapperName) != null) && (renameDetailMap.get(field).getValue(mapperName) != null)) {
                    MapperModifyDetail.ModifyDetail modifyDetail = modifyDetailMap.get(field).getValue(mapperName);
                    MapperRenameDetail.RenameDetail renameDetail = renameDetailMap.get(field).getValue(mapperName);
                    methodSpec.addStatement("resultMap.put(\"" + renameDetail.getRenameName() + "\", " + "obj." + modifyDetail.getModifyName() + "())");
                    continue;
                    // 检查该属性的rename信息是否都与map绑定
                } else if (renameDetailMap.get(field).getValue(mapperName) != null) {
                    MapperRenameDetail.RenameDetail detail = renameDetailMap.get(field).getValue(mapperName);
                    methodSpec.addStatement("resultMap.put(\"" + detail.getRenameName() + "\", " + "obj.get" + field.substring(0, 1).toUpperCase() + field.substring(1) + "())");
                    continue;
                    // 检查该属性的modify信息是否都与map绑定
                } else if (modifyDetailMap.get(field).getValue(mapperName) != null) {
                    MapperModifyDetail.ModifyDetail detail = modifyDetailMap.get(field).getValue(mapperName);
                    methodSpec.addStatement("resultMap.put(\"" + field + "\", " + "obj." + detail.getModifyName() + "())");
                    continue;
                }
            }
            // 若无属性绑定，直接生成方法信息
            methodSpec.addStatement("resultMap.put(\"" + field + "\", obj.get" + field.substring(0, 1).toUpperCase() + field.substring(1) + "())");

        }
        // 添加返回结果
        methodSpec.addStatement("return resultMap");
        return methodSpec.build();
    }


    /**
     * 生成GetEntity方法
     *
     * @param self            原始类包信息
     * @param mapperName      方法名
     * @param elemFields      添加域信息
     * @param ignoreDetailMap ignore信息
     * @param modifyDetailMap modify信息
     * @param renameDetailMap rename信息
     * @param typeSpec        类建造器
     * @return
     */
    private MethodSpec buildGetEntityMethod(
            ClassName self,
            String mapperName,
            List<Element> elemFields,
            Map<String, MapperIgnoreDetail> ignoreDetailMap,
            Map<String, MapperModifyDetail> modifyDetailMap,
            Map<String, MapperRenameDetail> renameDetailMap,
            TypeSpec.Builder typeSpec
    ) {
        String realMethodName = "getEntity";
        MethodSpec.Builder methodSpec = MethodSpec.methodBuilder(realMethodName)
                .addModifiers(Modifier.PUBLIC)
                .returns(self);
        methodSpec.addStatement("$T entity = new $T()", self, self);
        // 添加属性
        for (Element fieldElem : elemFields) {
            String field = fieldElem.getSimpleName().toString();
            if (ignoreDetailMap.get(field) != null) {
                // 如果加了ignore注解针对某方法 则直接跳过
                if (ignoreDetailMap.get(field).checkIn(mapperName)) continue;
            }
            // 有modify信息无rename信息
            if ((modifyDetailMap.get(field) != null) && (renameDetailMap.get(field) == null)) {
                // 检查该属性的modify信息是否与map绑定
                MapperModifyDetail.ModifyDetail modifyDetail = modifyDetailMap.get(field).getValue(mapperName);
                if (modifyDetail != null) {
                    String originName = fieldElem.getSimpleName().toString();
                    String recoverMethodName = modifyDetail.getRecoverName();
                    // recoverMethodName 为空的情况
                    if (recoverMethodName.equals("")) {
                        recoverMethodName = "set" + originName.substring(0, 1).toUpperCase() + originName.substring(1);
                    }
                    Type.MethodType recoverType = (Type.MethodType) modifyDetail.getRecoverType();

                    FieldSpec fieldSpec = FieldSpec.builder(
                            TypeName.get(recoverType.getReturnType()),
                            originName,
                            Modifier.PUBLIC)
                            .build();
                    typeSpec.addField(fieldSpec);
                    methodSpec.addStatement("entity." + recoverMethodName + "(this." + originName + ")");
                    continue;
                }
            }
            // 有rename信息无modify信息
            else if ((renameDetailMap.get(field) != null) && (modifyDetailMap.get(field) == null)) {
                // 检查该属性的rename信息是否与map绑定
                if (renameDetailMap.get(field).getValue(mapperName) != null) {
                    String originName = fieldElem.getSimpleName().toString();
                    String fieldName = renameDetailMap.get(field).getValue(mapperName).getRenameName();
                    FieldSpec fieldSpec = FieldSpec.builder(
                            TypeName.get(fieldElem.asType()),
                            fieldName,
                            Modifier.PUBLIC)
                            .build();
                    typeSpec.addField(fieldSpec);

                    methodSpec.addStatement("entity.set" + originName.substring(0, 1).toUpperCase() + originName.substring(1) + "(this." + fieldName + ")");
                    continue;
                }
            }
            // 有rename信息和modify信息
            else if ((modifyDetailMap.get(field) != null) && (renameDetailMap.get(field) != null)) {
                // 检查该属性的modify和rename信息是否都与map绑定
                MapperModifyDetail.ModifyDetail modifyDetail = modifyDetailMap.get(field).getValue(mapperName);
                if ((modifyDetail != null) && (renameDetailMap.get(field).getValue(mapperName) != null)) {
                    String originName = fieldElem.getSimpleName().toString();
                    String recoverMethodName = modifyDetail.getRecoverName();
                    // recoverMethodName 为空的情况
                    if (recoverMethodName.equals("")) {
                        recoverMethodName = "set" + originName.substring(0, 1).toUpperCase() + originName.substring(1);
                    }
                    String fieldName = renameDetailMap.get(field).getValue(mapperName).getRenameName();
                    Type.MethodType recoverType = (Type.MethodType) modifyDetail.getRecoverType();

                    FieldSpec fieldSpec = FieldSpec.builder(
                            TypeName.get(recoverType.getReturnType()),
                            fieldName,
                            Modifier.PUBLIC)
                            .build();
                    typeSpec.addField(fieldSpec);
                    methodSpec.addStatement("entity." + recoverMethodName + "(this." + fieldName + ")");
                    continue;
                    // 检查该属性的rename信息是否都与map绑定
                } else if (renameDetailMap.get(field).getValue(mapperName) != null) {
                    String originName = fieldElem.getSimpleName().toString();
                    String fieldName = renameDetailMap.get(field).getValue(mapperName).getRenameName();
                    FieldSpec fieldSpec = FieldSpec.builder(
                            TypeName.get(fieldElem.asType()),
                            fieldName,
                            Modifier.PUBLIC)
                            .build();
                    typeSpec.addField(fieldSpec);
                    methodSpec.addStatement("entity.set" + originName.substring(0, 1).toUpperCase() + originName.substring(1) + "(this." + fieldName + ")");
                    continue;
                    // 检查该属性的modify信息是否都与map绑定
                } else if (modifyDetail != null) {
                    String originName = fieldElem.getSimpleName().toString();
                    String recoverMethodName = modifyDetail.getRecoverName();
                    Type.MethodType recoverType = (Type.MethodType) modifyDetail.getRecoverType();

                    FieldSpec fieldSpec = FieldSpec.builder(
                            TypeName.get(recoverType.getReturnType()),
                            originName,
                            Modifier.PUBLIC)
                            .build();
                    typeSpec.addField(fieldSpec);
                    methodSpec.addStatement("entity." + recoverMethodName + "(this." + originName + ")");
                    continue;
                }
            }

            String originName = fieldElem.getSimpleName().toString();
            FieldSpec fieldSpec = FieldSpec.builder(
                    TypeName.get(fieldElem.asType()),
                    originName,
                    Modifier.PUBLIC)
                    .build();
            typeSpec.addField(fieldSpec);
            // 若无属性绑定，直接生成方法信息
            methodSpec.addStatement("entity.set" + originName.substring(0, 1).toUpperCase() + originName.substring(1) + "(this." + originName + ")");


        }


        // 添加返回结果
        methodSpec.addStatement("return entity");
        return methodSpec.build();
    }


    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        try {
            // 扫描类 过滤掉非类上注解
            for (Element elem : roundEnv.getElementsAnnotatedWith(Mapper.class)) {
                if (elem.getKind() != ElementKind.CLASS) {
                    throw new ProcessingException(elem, "Only classes can be annotated with @%s", Mapper.class.getSimpleName());
                }

                // 定义ignore modify和rename项
                List<Element> fields = new ArrayList<>();
                Map<String, MapperIgnoreDetail> ignoreDetailMap = new HashMap<>();
                Map<String, MapperModifyDetail> modifyDetailMap = new HashMap<>();
                Map<String, MapperRenameDetail> renameDetailMap = new HashMap<>();

                // 扫描ignore modify和rename项
                scanFields(elem, fields, ignoreDetailMap, modifyDetailMap, renameDetailMap);


                // 获取type信息
                TypeElement typeElement = (TypeElement) elem;

                Name qualifiedClassName = typeElement.getQualifiedName();
                Name simpleName = typeElement.getSimpleName();

                // 获取pkg信息
                PackageElement pkg = elementUtils.getPackageOf(typeElement);
                String packageName = pkg.isUnnamed() ? "" : pkg.getQualifiedName().toString();

                // 定义Mapper
                Mapper mapper = elem.getAnnotation(Mapper.class);
                // 检测GroupMapper
                GroupMapper groupMapper = elem.getAnnotation(GroupMapper.class);

                // 获取Mapper列表
                HashSet<String> mapperSet = new HashSet<>();
                mapperSet.add("default");
                mapperSet.addAll(Arrays.asList(mapper.value()));

                HashSet<String> groupMapperSet = new HashSet<>();
                if (groupMapper != null) {
                    // 获取GroupMapper列表
                    if (groupMapper.target().length == 0) {
                        groupMapperSet.add("default");
                    } else {
                        groupMapperSet.addAll(Arrays.asList(groupMapper.target()));
                    }
                }


                // 确定import本类
                ClassName self = ClassName.get(packageName, simpleName.toString());

                for (String mapperName : mapperSet) {

                    // 新建类
                    TypeSpec.Builder typeSpec = TypeSpec.classBuilder(simpleName.toString() + (mapperName.charAt(0) + "").toUpperCase() + mapperName.substring(1).toLowerCase() + "Mapper");
                    typeSpec.addModifiers(Modifier.PUBLIC);

                    // 出现GroupMapper注解
                    if (groupMapper != null) {
                        if (groupMapperSet.contains(mapperName)) {
                            int index = 0;
                            for (int i = 0; i < groupMapper.target().length; i++) {
                                if (mapperName.equals(groupMapper.target()[i])) {
                                    index = i;
                                }
                            }
                            AnnotationSpec.Builder annotationSpec = AnnotationSpec.builder(Group.class);
                            if (groupMapper.base().length > 0 && groupMapper.base().length < index) {
                                annotationSpec.addMember("base", groupMapper.base()[groupMapper.base().length - 1] + "");
                            } else if (groupMapper.base().length > 0) {
                                annotationSpec.addMember("base", groupMapper.base()[index] + "");
                            }

                            if (groupMapper.value().length < index) {
                                annotationSpec.addMember("value", "\"" + groupMapper.value()[groupMapper.value().length - 1] + "\"");
                            } else {
                                annotationSpec.addMember("value", "\"" + groupMapper.value()[index] + "\"");
                            }

                            if (groupMapper.name().length > 0 && groupMapper.name().length < index) {
                                annotationSpec.addMember("name", "\"" + groupMapper.name()[groupMapper.name().length - 1] + "\"");
                            } else if (groupMapper.name().length > 0) {
                                annotationSpec.addMember("name", "\"" + groupMapper.name()[index] + "\"");
                            }

                            if (groupMapper.list().length > 0 && groupMapper.list().length < index) {
                                annotationSpec.addMember("list", groupMapper.list()[groupMapper.list().length - 1] + "");
                            } else if (groupMapper.list().length > 0) {
                                annotationSpec.addMember("list", groupMapper.list()[index] + "");
                            }
                            typeSpec.addAnnotation(annotationSpec.build());
                        }
                    }

                    // 生成getMap方法
                    MethodSpec getMapMethod = buildGetMapMethod(self, mapperName, fields, ignoreDetailMap, modifyDetailMap, renameDetailMap);
                    // 生成getEntity方法
                    MethodSpec getEntityMethod = buildGetEntityMethod(self, mapperName, fields, ignoreDetailMap, modifyDetailMap, renameDetailMap, typeSpec);
                    // 添加方法
                    typeSpec.addMethod(getMapMethod);
                    typeSpec.addMethod(getEntityMethod);

                    // 写出数据
                    JavaFile.builder(packageName, typeSpec.build()).build().writeTo(filer);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }


}
