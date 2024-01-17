package com.github.idlabdiscover.rsqlutils.builder;

import com.github.idlabdiscover.rsqlutils.model.IntegerProperty;
import com.github.idlabdiscover.rsqlutils.model.StringProperty;

public interface JavaQuery extends Builder<JavaQuery> {

    StringProperty<JavaQuery> stringProperty();

    IntegerProperty<JavaQuery> intProperty();

}
