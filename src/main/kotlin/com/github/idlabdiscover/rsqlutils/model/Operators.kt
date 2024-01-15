package com.github.idlabdiscover.rsqlutils.model

import cz.jirutka.rsql.parser.ast.ComparisonOperator

object AdditionalBasicOperators {
    // Field exists operator
    val EX = ComparisonOperator("=ex=")
    // String regex match operator
    val RE = ComparisonOperator("=re=")
}
