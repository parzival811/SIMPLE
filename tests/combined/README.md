# Combined SIMPLE Test Programs

This suite contains feature-oriented programs for both runtime and static checks.

## Layout

- `valid/`: programs expected to execute and typecheck successfully
- `invalid/`: programs expected to be rejected by the typechecker

## Usage

Run the full suite from the repository root:

```bash
make test
```

Run one program with the interpreter:

```bash
./run-interpreter tests/combined/valid/prime_check.simple
```

Run one program with the typechecker:

```bash
./run-typechecker tests/combined/invalid/wrong_return_type.simple
```
