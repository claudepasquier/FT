# Makefile for the FOURT C core.
#
# Targets:
#   test-native  — build & run unit tests natively (gcc)
#   wasm         — build the Emscripten module into ../web/src/wasm/
#   clean

CC      ?= gcc
EMCC    ?= emcc
CFLAGS  ?= -O2 -Wall -Wextra -std=c99
EMFLAGS  = -O3 -flto -sMODULARIZE=1 -sEXPORT_ES6=1 \
           -sENVIRONMENT=web,worker -sALLOW_MEMORY_GROWTH=1 \
           -sEXPORTED_FUNCTIONS=_fourt_compute,_fourt_version,_fourt_fft,_malloc,_free \
           -sEXPORTED_RUNTIME_METHODS=ccall,cwrap,HEAPF32,stringToUTF8,UTF8ToString,getValue \
           --no-entry

BUILD   := build
WASMDIR := ../web/src/wasm

.PHONY: test test-native wasm clean

test: test-native

test-native: $(BUILD)/test_fourt
	$(BUILD)/test_fourt

$(BUILD)/test_fourt: test/test_fourt.c src/fourt.c include/fourt.h
	@mkdir -p $(BUILD)
	$(CC) $(CFLAGS) -Iinclude -o $@ test/test_fourt.c src/fourt.c -lm

# CLI reference tool (golden fixtures / cross-checks)
$(BUILD)/refcli: tools/refcli.c src/fourt.c include/fourt.h
	@mkdir -p $(BUILD)
	$(CC) $(CFLAGS) -Iinclude -o $@ tools/refcli.c src/fourt.c -lm

refcli: $(BUILD)/refcli

# Requires the Emscripten SDK in PATH (https://emscripten.org).
wasm:
	@mkdir -p $(WASMDIR)
	$(EMCC) src/fourt.c -Iinclude $(EMFLAGS) \
		-o $(WASMDIR)/fourt.js
	@echo "WASM module written to $(WASMDIR)/fourt.js + fourt.wasm"

clean:
	rm -rf $(BUILD) $(WASMDIR)/fourt.js $(WASMDIR)/fourt.wasm
