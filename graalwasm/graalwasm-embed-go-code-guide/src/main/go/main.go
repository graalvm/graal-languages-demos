package main

import (
    "fmt"
    "runtime"
    "syscall/js"
)

func add(this js.Value, args []js.Value) any {
    return args[0].Int() + args[1].Int()
}

func compilerAndVersion(this js.Value, args []js.Value) any {
    return js.ValueOf(fmt.Sprintf("Compiler: %s, Go version: %s", runtime.Compiler, runtime.Version()))
}

func reverseString(this js.Value, args []js.Value) any {
    if len(args) < 1 {
        return js.ValueOf("")
    }
    s := args[0].String()

    runes := []rune(s)
    for i, j := 0, len(runes)-1; i < j; i, j = i+1, j-1 {
        runes[i], runes[j] = runes[j], runes[i]
    }
    reversed := string(runes)
    return js.ValueOf(reversed)
}

func registerMainPackage() {
    main := js.Global().Get("Object").New()
    main.Set("add", js.FuncOf(add))
    main.Set("compilerAndVersion", js.FuncOf(compilerAndVersion))
    main.Set("reverseString", js.FuncOf(reverseString))
    js.Global().Set("main", main)
}

func main() {
    wait := make(chan struct{}, 0)
    registerMainPackage()
    <-wait
}