# SIMPLE

SIMPLE stands for **Simple IMperative Programming Language with Explicit typing**.

This repository packages the language as a single project with two complementary components:

- An **interpreter** for executing SIMPLE programs.
- A **static typechecker** for validating declarations, function signatures, and return-path correctness before execution.

The implementation is written in Java and uses **JFlex** for lexing and **Java CUP** for parsing.

## Highlights

- Explicitly typed variables and function parameters
- `int`, `bool`, and `void` types
- Arithmetic, comparison, and equality operators
- Lexically scoped `begin ... end` blocks
- `if / then / else / fi` and `while / do / done`
- Functions with typed parameters and return values
- Static checks for type-safe assignments, function calls, and missing returns

## Requirements

- Java **17 or newer**
- GNU Make

## Project Structure

- `src/frontend/`: shared SIMPLE lexer and parser grammar
- `src/interpreter/`: interpreter-specific sources
- `src/typechecker/`: typechecker-specific sources
- `lib/`: shared JFlex and CUP dependencies
- `build/`: compiled class files
- `examples/`: quick sample SIMPLE programs
- `tests/combined/valid/`: valid behavior tests
- `tests/combined/invalid/`: invalid-program tests
- `tests/legacy/`: numbered test corpus
- `docs/`: language and architecture notes
- `run-interpreter`: root wrapper for the interpreter
- `run-typechecker`: root wrapper for the typechecker

## Quick Start

Build everything:

```bash
make compile
```

Run the interpreter on an example:

```bash
./run-interpreter examples/factorial.simple
```

Run the typechecker on a valid example:

```bash
./run-typechecker examples/typecheck-valid.simple
```

Run the test suite:

```bash
make test
```

## Combined Test Programs

The repository includes a combined suite of feature-oriented programs.

Feature-focused valid programs include:

- `tests/combined/valid/basic_assignment.simple`
- `tests/combined/valid/while_loop.simple`
- `tests/combined/valid/if_else.simple`
- `tests/combined/valid/function_square.simple`
- `tests/combined/valid/function_add_multi_param.simple`
- `tests/combined/valid/factorial_iterative.simple`
- `tests/combined/valid/gcd_euclidean.simple`
- `tests/combined/valid/prime_check.simple`
- `tests/combined/valid/scoped_blocks.simple`
- `tests/combined/valid/void_function_return.simple`
- `tests/combined/valid/nested_return_all_paths.simple`

Representative invalid programs include:

- `tests/combined/invalid/scope_function_outer_var.simple`
- `tests/combined/invalid/scope_block_variable_leak.simple`
- `tests/combined/invalid/assign_bool_to_int.simple`
- `tests/combined/invalid/duplicate_parameter_names.simple`
- `tests/combined/invalid/missing_return_conditional.simple`
- `tests/combined/invalid/unreachable_code_after_return.simple`
- `tests/combined/invalid/wrong_return_type.simple`
- `tests/combined/invalid/mixed_return_types.simple`
- `tests/combined/invalid/missing_return_multiple_ifs.simple`

Try them directly:

```bash
./run-interpreter tests/combined/valid/prime_check.simple
./run-typechecker tests/combined/invalid/wrong_return_type.simple
```

## Architecture

At a high level, both stages follow the same frontend pipeline:

1. JFlex tokenizes source code.
2. Java CUP parses tokens into an AST.
3. The AST is consumed by either:
   - the interpreter for execution, or
   - the typechecker for static analysis.

More detail:

- [Language Specification](docs/language-specification.md)
- [Typechecking Semantics](docs/typechecking.md)
