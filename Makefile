SHELL := /bin/bash

JAVA ?= java
JAVAC ?= javac
JAVA_RELEASE ?= 17
JAVAC_FLAGS ?= --release $(JAVA_RELEASE)

SRC_DIR := src
COMMON_AST_FILE := $(SRC_DIR)/ASTNodes.java
FRONTEND_SRC_DIR := $(SRC_DIR)/frontend
INTERPRETER_SRC_DIR := $(SRC_DIR)/interpreter
TYPECHECKER_SRC_DIR := $(SRC_DIR)/typechecker

LIB_DIR := lib
BUILD_DIR := build
INTERPRETER_BUILD_DIR := $(BUILD_DIR)/interpreter
TYPECHECKER_BUILD_DIR := $(BUILD_DIR)/typechecker
FRONTEND_GEN_DIR := $(BUILD_DIR)/generated/frontend

JFLEX_JAR := $(LIB_DIR)/jflex-1.7.0.jar
CUP_JAR := $(LIB_DIR)/java-cup-0.11b.jar
CUP_RUNTIME_JAR := $(LIB_DIR)/java-cup-0.11b-runtime.jar

INTERPRETER_CLASSPATH := $(CUP_RUNTIME_JAR):$(INTERPRETER_BUILD_DIR)
TYPECHECKER_CLASSPATH := $(CUP_RUNTIME_JAR):$(TYPECHECKER_BUILD_DIR)

INTERPRETER_FILE ?= examples/factorial.simple
TYPECHECK_FILE ?= examples/typecheck-valid.simple

.PHONY: all compile compile-interpreter compile-typechecker interpreter typecheck test clean

all: compile

compile: compile-interpreter compile-typechecker

$(FRONTEND_GEN_DIR):
	mkdir -p $(FRONTEND_GEN_DIR)

$(FRONTEND_GEN_DIR)/SimpleLexer.java: $(FRONTEND_SRC_DIR)/SIMPLE.flex | $(FRONTEND_GEN_DIR)
	$(JAVA) -cp $(JFLEX_JAR):$(CUP_RUNTIME_JAR) jflex.Main -d $(FRONTEND_GEN_DIR) $(FRONTEND_SRC_DIR)/SIMPLE.flex

$(FRONTEND_GEN_DIR)/SimpleParser.java $(FRONTEND_GEN_DIR)/sym.java: $(FRONTEND_SRC_DIR)/SIMPLE.cup | $(FRONTEND_GEN_DIR)
	$(JAVA) -jar $(CUP_JAR) -destdir $(FRONTEND_GEN_DIR) -parser SimpleParser -symbols sym $(FRONTEND_SRC_DIR)/SIMPLE.cup

$(INTERPRETER_BUILD_DIR):
	mkdir -p $(INTERPRETER_BUILD_DIR)

$(TYPECHECKER_BUILD_DIR):
	mkdir -p $(TYPECHECKER_BUILD_DIR)

compile-interpreter: $(INTERPRETER_BUILD_DIR) $(COMMON_AST_FILE) $(FRONTEND_GEN_DIR)/SimpleLexer.java $(FRONTEND_GEN_DIR)/SimpleParser.java $(FRONTEND_GEN_DIR)/sym.java
	$(JAVAC) $(JAVAC_FLAGS) -cp $(INTERPRETER_CLASSPATH) -d $(INTERPRETER_BUILD_DIR) $(COMMON_AST_FILE) $(INTERPRETER_SRC_DIR)/*.java $(FRONTEND_GEN_DIR)/SimpleLexer.java $(FRONTEND_GEN_DIR)/SimpleParser.java $(FRONTEND_GEN_DIR)/sym.java

compile-typechecker: $(TYPECHECKER_BUILD_DIR) $(COMMON_AST_FILE) $(FRONTEND_GEN_DIR)/SimpleLexer.java $(FRONTEND_GEN_DIR)/SimpleParser.java $(FRONTEND_GEN_DIR)/sym.java
	$(JAVAC) $(JAVAC_FLAGS) -cp $(TYPECHECKER_CLASSPATH) -d $(TYPECHECKER_BUILD_DIR) $(COMMON_AST_FILE) $(TYPECHECKER_SRC_DIR)/*.java $(FRONTEND_GEN_DIR)/SimpleLexer.java $(FRONTEND_GEN_DIR)/SimpleParser.java $(FRONTEND_GEN_DIR)/sym.java

interpreter: compile
	$(JAVA) -cp $(INTERPRETER_CLASSPATH) Evaluator $(INTERPRETER_FILE)

typecheck: compile
	$(JAVA) -cp $(TYPECHECKER_CLASSPATH) TypeEvaluator $(TYPECHECK_FILE)

test:
	./scripts/run-tests.sh

clean:
	rm -rf $(BUILD_DIR)
