package org.hime.parse

import org.hime.cast
import org.hime.core.SymbolTable
import org.hime.core.eval
import org.hime.parse.Type.*
import java.math.BigDecimal
import java.math.BigInteger
import java.util.ArrayList

typealias Hime_HimeFunction = (List<Token>) -> Token
typealias Hime_Function = (List<Token>, SymbolTable) -> Token
typealias Hime_StaticFunction = (ASTNode, SymbolTable) -> Token

val TRUE = Token(BOOL, true)
val FALSE = Token(BOOL, false)
val NIL = Token(Type.NIL, "nil")

val LB = Token(Type.LB, "(")
val RB = Token(Type.RB, ")")

class Token(val type: Type, val value: Any) {
    override fun toString(): String {
        return when(this.type) {
            STR, Type.LB, Type.RB, EMPTY, Type.NIL, ID -> cast<String>(this.value)
            BOOL -> cast<Boolean>(this.value).toString()
            NUM -> cast<Int>(this.value).toString()
            REAL -> cast<Float>(this.value).toString()
            BIG_NUM -> cast<BigInteger>(this.value).toString()
            BIG_REAL -> cast<BigDecimal>(this.value).toPlainString()
            LIST ->  {
                val builder = StringBuilder("[")
                val list = cast<List<Token>>(this.value)
                for (i in list.indices)
                    builder.append(if (i == 0) list[i].toString() else ", ${list[i]}")
                builder.append("]")
                return builder.toString()
            }
            FUNCTION, STATIC_FUNCTION, HIME_FUNCTION -> "<Function: ${this.value.hashCode()}>"
            UNKNOWN -> this.toString()
        }
    }
}

fun structureHimeFunction(functionParameters: ArrayList<String>, ast: List<ASTNode>, symbolTable: SymbolTable): Token {
    return Token(HIME_FUNCTION,
        fun(parameters: List<Token>): Token {
            assert(parameters.size >= functionParameters.size)
            val newSymbolTable = symbolTable.createChild()
            for (i in functionParameters.indices)
                newSymbolTable.put(functionParameters[i], parameters[i])
            var result = NIL
            for (astNode in ast)
                result = eval(astNode.copy(), newSymbolTable)
            return result
        })
}

enum class Type {
    UNKNOWN,
    LB, RB, EMPTY, NIL,
    ID, BOOL, STR, LIST,
    NUM, REAL, BIG_NUM, BIG_REAL,
    FUNCTION, STATIC_FUNCTION, HIME_FUNCTION
}