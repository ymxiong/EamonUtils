package cc.eamon.open.annotation.common.group;


import com.squareup.javapoet.TypeName;

import javax.lang.model.type.TypeMirror;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Eamon on 2018/10/3.
 */
public class GroupDetail {

    private boolean base = false;

    private boolean list = false;

    private String value = "";

    private String name = "";

    private TypeName typeName = null;

    public boolean isBase() {
        return base;
    }

    public void setBase(boolean base) {
        this.base = base;
    }

    public boolean isList() { return list; }

    public void setList(boolean list) { this.list = list; }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public TypeName getTypeName() {
        return typeName;
    }

    public void setTypeName(TypeName typeName) {
        this.typeName = typeName;
    }
}
