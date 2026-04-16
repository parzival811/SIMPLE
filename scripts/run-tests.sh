#!/usr/bin/env bash

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
OUT_FILE="/tmp/simple-test.out"
ERR_FILE="/tmp/simple-test.err"

run_expect_success() {
  local label="$1"
  shift

  if "$@" >"$OUT_FILE" 2>"$ERR_FILE"; then
    printf '[PASS] %s\n' "$label"
  else
    printf '[FAIL] %s\n' "$label"
    cat "$ERR_FILE"
    exit 1
  fi
}

run_expect_failure() {
  local label="$1"
  shift

  if "$@" >"$OUT_FILE" 2>"$ERR_FILE"; then
    printf '[FAIL] %s\n' "$label"
    cat "$OUT_FILE"
    exit 1
  else
    printf '[PASS] %s\n' "$label"
  fi
}

make -C "$ROOT_DIR" compile-interpreter >/dev/null
make -C "$ROOT_DIR" compile-typechecker >/dev/null

INTERPRETER_CASES=(
  "tests/combined/valid/basic_assignment.simple"
  "tests/combined/valid/while_loop.simple"
  "tests/combined/valid/if_else.simple"
  "tests/combined/valid/nested_control.simple"
  "tests/combined/valid/function_square.simple"
  "tests/combined/valid/function_add_multi_param.simple"
  "tests/combined/valid/factorial_iterative.simple"
  "tests/combined/valid/gcd_euclidean.simple"
  "tests/combined/valid/prime_check.simple"
  "tests/combined/valid/scoped_blocks.simple"
  "tests/combined/valid/void_function_return.simple"
)

TYPECHECK_VALID_CASES=(
  "tests/combined/valid/basic_assignment.simple"
  "tests/combined/valid/while_loop.simple"
  "tests/combined/valid/if_else.simple"
  "tests/combined/valid/nested_control.simple"
  "tests/combined/valid/function_square.simple"
  "tests/combined/valid/function_add_multi_param.simple"
  "tests/combined/valid/factorial_iterative.simple"
  "tests/combined/valid/gcd_euclidean.simple"
  "tests/combined/valid/prime_check.simple"
  "tests/combined/valid/scoped_blocks.simple"
  "tests/combined/valid/void_function_return.simple"
  "tests/combined/valid/nested_return_all_paths.simple"
)

TYPECHECK_INVALID_CASES=(
  "tests/combined/invalid/scope_function_outer_var.simple"
  "tests/combined/invalid/scope_block_variable_leak.simple"
  "tests/combined/invalid/assign_bool_to_int.simple"
  "tests/combined/invalid/duplicate_parameter_names.simple"
  "tests/combined/invalid/missing_return_conditional.simple"
  "tests/combined/invalid/unreachable_code_after_return.simple"
  "tests/combined/invalid/wrong_return_type.simple"
  "tests/combined/invalid/mixed_return_types.simple"
  "tests/combined/invalid/missing_return_multiple_ifs.simple"
)

for file in "${INTERPRETER_CASES[@]}"; do
  run_expect_success "Interpreter accepts $(basename "$file")" \
    "$ROOT_DIR/run-interpreter" "$file"
done

for file in "${TYPECHECK_VALID_CASES[@]}"; do
  run_expect_success "Typechecker accepts $(basename "$file")" \
    "$ROOT_DIR/run-typechecker" "$file"
done

for file in "${TYPECHECK_INVALID_CASES[@]}"; do
  run_expect_failure "Typechecker rejects $(basename "$file")" \
    "$ROOT_DIR/run-typechecker" "$file"
done

printf 'All checks passed.\n'
