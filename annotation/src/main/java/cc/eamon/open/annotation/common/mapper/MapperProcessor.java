package cc.eamon.open.annotation.common.mapper;

import cc.eamon.open.annotation.ProcessingException;
import com.squareup.javapoet.*;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.util.ElementScanner6;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Eamon on 2018/9/30.
 */

@SupportedAnnotationTypes(
        {
                "cc.eamon.open.annotation.annotation.mapper.Mapper",
                "cc.eamon.open.annotation.annotation.mapper.MapperIgnore",
                "cc.eamon.open.annotation.annotation.mapper.MapperModify"
        }
)
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

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        try {


            // 扫描类 过滤掉非类上注解
            for (Element elem : roundEnv.getElementsAnnotatedWith(Mapper.class)) {
                if (elem.getKind() != ElementKind.CLASS) {
                    throw new ProcessingException(elem, "Only classes can be annotated with @%s", Mapper.class.getSimpleName());
                }

                // 定义ignore与modify项
                List<String> fields = new ArrayList<>();
                Map<String, MapperIgnoreDetail> ignoreDetailMap = new HashMap<>();
                Map<String, MapperModifyDetail> modifyDetailMap = new HashMap<>();
                Map<String, MapperRenameDetail> renameDetailMap = new HashMap<>();

                // 扫描ignore与modify项
                for (Element elemField : elem.getEnclosedElements()) {
                    if (elemField.getKind().isField()) {
                        if (elemField.getModifiers().contains(Modifier.STATIC)) continue;


                        fields.add(elemField.getSimpleName().toString());
                        MapperIgnore ignore = elemField.getAnnotation(MapperIgnore.class);
                        MapperModify modify = elemField.getAnnotation(MapperModify.class);
                        MapperRename rename = elemField.getAnnotation(MapperRename.class);

                        if (ignore != null) {
                            MapperIgnoreDetail detail = new MapperIgnoreDetail();
                            detail.setFieldName(elemField.getSimpleName().toString());
                            for (String s : ignore.value()) {
                                detail.addValue(s);
                            }
                            ignoreDetailMap.put(detail.getFieldName(), detail);
                        }
                        if (modify != null) {
                            MapperModifyDetail detail = new MapperModifyDetail();
                            detail.setFieldName(elemField.getSimpleName().toString());

                            for (int i = 0; i < modify.value().length; i++) {
                                MapperModifyDetail.ModifyDetail modifyDetail = new MapperModifyDetail.ModifyDetail();
                                modifyDetail.setMethodName(modify.method()[i]);
                                modifyDetail.setTargetMapName(modify.value()[i]);
                                detail.addValue(modifyDetail);
                            }
                            modifyDetailMap.put(detail.getFieldName(), detail);
                        }
                        if (rename != null) {
                            MapperRenameDetail detail = new MapperRenameDetail();
                            detail.setFieldName(elemField.getSimpleName().toString());

                            for (int i = 0; i < rename.value().length; i++) {
                                MapperRenameDetail.RenameDetail renameDetail = new MapperRenameDetail.RenameDetail();
                                renameDetail.setOriginMapName(rename.value()[i]);
                                renameDetail.setRenameMapName(rename.name()[i]);
                                detail.addValue(renameDetail);
                            }
                            renameDetailMap.put(detail.getFieldName(), detail);
                        }

                    }
                }


                // 准备新建type
                TypeElement typeElement = (TypeElement) elem;

                Name qualifiedClassName = typeElement.getQualifiedName();
                Name simpleName = typeElement.getSimpleName();

                // 获取pkg信息
                PackageElement pkg = elementUtils.getPackageOf(typeElement);
                String packageName = pkg.isUnnamed() ? "" : pkg.getQualifiedName().toString();

                // 定义Mapper
                Mapper mapper = elem.getAnnotation(Mapper.class);

                // 获取Mapper指定method列表
                HashSet<String> methodSet = new HashSet<>();
                methodSet.add("default");
                for (String value : mapper.value()) {
                    methodSet.add(value);
                }


                // 新建类
                TypeSpec.Builder typeSpec = TypeSpec.classBuilder(simpleName.toString() + "Mapper");
                typeSpec.addModifiers(Modifier.PUBLIC);

                // 确定需要import的项
                ClassName self = ClassName.get(packageName, simpleName.toString());
                ClassName string = ClassName.get("java.lang", "String");
                ClassName object = ClassName.get("java.lang", "Object");
                ClassName map = ClassName.get("java.util", "Map");
                ClassName linkedHashMap = ClassName.get("java.util", "LinkedHashMap");
                TypeName typeOfMap = ParameterizedTypeName.get(map, string, object);

                // 添加方法
                for (String methodName : methodSet) {
                    String realMethodName = "get" + (methodName.charAt(0) + "").toUpperCase() + methodName.substring(1).toLowerCase() + "Map";
                    MethodSpec.Builder methodSpec = MethodSpec.methodBuilder(realMethodName)
                            .addModifiers(Modifier.PUBLIC)
                            .addModifiers(Modifier.STATIC)
                            .addParameter(self, "obj")
                            .returns(typeOfMap);

                    methodSpec.addStatement("Map<String, Object> resultMap = new $T<>()", linkedHashMap);
                    methodSpec.addStatement("if (obj == null) return resultMap");
                    for (String field : fields) {
                        if (ignoreDetailMap.get(field) != null) {
                            // 如果加了ignore注解针对某方法 则直接跳过
                            if (ignoreDetailMap.get(field).checkIn(methodName)) continue;
                        }
                        if ((modifyDetailMap.get(field) != null) && (renameDetailMap.get(field) == null)) {
                            if (modifyDetailMap.get(field).getValue(methodName) != null) {
                                MapperModifyDetail.ModifyDetail detail = modifyDetailMap.get(field).getValue(methodName);
//                                String getX = "get" + field.substring(0, 1).toUpperCase() + field.substring(1) + "()";
//                                methodSpec.beginControlFlow("if ((Object)(obj."+ getX +")!=null)");
                                methodSpec.addStatement("resultMap.put(\"" + field + "\", " + simpleName.toString() + "." + detail.getMethodName() + "(obj.get" + field.substring(0, 1).toUpperCase() + field.substring(1) + "()))");
//                                methodSpec.endControlFlow();
                                continue;
                            }
                        }
                        if ((renameDetailMap.get(field) != null) && (modifyDetailMap.get(field) == null)) {
                            if (renameDetailMap.get(field).getValue(methodName) != null) {
                                MapperRenameDetail.RenameDetail detail = renameDetailMap.get(field).getValue(methodName);
//                                String getX = "get" + field.substring(0, 1).toUpperCase() + field.substring(1) + "()";
//                                methodSpec.beginControlFlow("if ((Object)(obj."+ getX +")!=null)");
                                methodSpec.addStatement("resultMap.put(\"" + detail.getRenameMapName() + "\", " + "obj.get" + field.substring(0, 1).toUpperCase() + field.substring(1) + "())");
//                                methodSpec.endControlFlow();
                                continue;
                            }
                        }
                        if ((modifyDetailMap.get(field) != null) && (renameDetailMap.get(field) != null)) {
                            if ((modifyDetailMap.get(field).getValue(methodName) != null) && (renameDetailMap.get(field).getValue(methodName) != null)) {
                                MapperModifyDetail.ModifyDetail modifyDetail = modifyDetailMap.get(field).getValue(methodName);
                                MapperRenameDetail.RenameDetail renameDetail = renameDetailMap.get(field).getValue(methodName);
                                methodSpec.addStatement("resultMap.put(\"" + renameDetail.getRenameMapName() + "\", " + simpleName.toString() + "." + modifyDetail.getMethodName() + "(obj.get" + field.substring(0, 1).toUpperCase() + field.substring(1) + "()))");
                                continue;
                            }else if (renameDetailMap.get(field).getValue(methodName) != null) {
                                MapperRenameDetail.RenameDetail detail = renameDetailMap.get(field).getValue(methodName);
//                                String getX = "get" + field.substring(0, 1).toUpperCase() + field.substring(1) + "()";
//                                methodSpec.beginControlFlow("if ((Object)(obj."+ getX +")!=null)");
                                methodSpec.addStatement("resultMap.put(\"" + detail.getRenameMapName() + "\", " + "obj.get" + field.substring(0, 1).toUpperCase() + field.substring(1) + "())");
//                                methodSpec.endControlFlow();
                                continue;
                            }else if (modifyDetailMap.get(field).getValue(methodName) != null) {
                                MapperModifyDetail.ModifyDetail detail = modifyDetailMap.get(field).getValue(methodName);
//                                String getX = "get" + field.substring(0, 1).toUpperCase() + field.substring(1) + "()";
//                                methodSpec.beginControlFlow("if ((Object)(obj."+ getX +")!=null)");
                                methodSpec.addStatement("resultMap.put(\"" + field + "\", " + simpleName.toString() + "." + detail.getMethodName() + "(obj.get" + field.substring(0, 1).toUpperCase() + field.substring(1) + "()))");
//                                methodSpec.endControlFlow();
                                continue;
                            }
                        }


//                        String getX = "get" + field.substring(0, 1).toUpperCase() + field.substring(1) + "()";
//                        methodSpec.beginControlFlow("if ((Object)(obj."+ getX +")!=null)");
                        methodSpec.addStatement("resultMap.put(\"" + field + "\", obj.get" + field.substring(0, 1).toUpperCase() + field.substring(1) + "())");
//                        methodSpec.endControlFlow();
                    }
                    methodSpec.addStatement("return resultMap");

                    typeSpec.addMethod(methodSpec.build());
                }

                JavaFile.builder(packageName, typeSpec.build()).build().writeTo(filer);

            }
        } catch (Exception e) {
            System.out.println(e.toString());
        }

        return false;
    }


}
