package main

import "syscall/js"

func add(this js.Value, args []js.Value) interface{} {
    return args[0].Int() + args[1].Int()
}

func registerCallbacks() {
    js.Global().Set("add", js.FuncOf(add))
}

func main() {
    c := make(chan struct{}, 0)
    registerCallbacks()
    <-c // Keep the function alive
}